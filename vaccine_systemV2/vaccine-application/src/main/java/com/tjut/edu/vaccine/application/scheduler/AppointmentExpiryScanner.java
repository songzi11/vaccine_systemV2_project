package com.tjut.edu.vaccine.application.scheduler;

import com.tjut.edu.vaccine.common.enums.AppointmentStatus;
import com.tjut.edu.vaccine.domain.appointment.aggregate.Appointment;
import com.tjut.edu.vaccine.domain.appointment.repository.AppointmentRepository;
import com.tjut.edu.vaccine.domain.identity.aggregate.User;
import com.tjut.edu.vaccine.domain.identity.entity.SysConfig;
import com.tjut.edu.vaccine.domain.identity.entity.SystemNotice;
import com.tjut.edu.vaccine.domain.identity.repository.ConfigRepository;
import com.tjut.edu.vaccine.domain.identity.repository.SystemNoticeRepository;
import com.tjut.edu.vaccine.domain.identity.repository.UserRepository;
import com.tjut.edu.vaccine.domain.stock.repository.VaccineStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentExpiryScanner {

    private final AppointmentRepository appointmentRepository;
    private final VaccineStockRepository vaccineStockRepository;
    private final UserRepository userRepository;
    private final SystemNoticeRepository systemNoticeRepository;
    private final ConfigRepository configRepository;

    @Value("${vaccine.default-hospital-id:1}")
    private Long defaultHospitalId;

    @Scheduled(cron = "${schedule.cron_expire:0 30 0 * * ?}")
    @Transactional
    public void scanExpiredAppointments() {
        doScan();
    }

    /**
     * 应用启动后补偿扫描：防止定时任务未执行期间（如停机）遗漏过期预约
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void scanOnStartup() {
        log.info("启动补偿扫描：检查遗漏的过期预约...");
        doScan();
    }

    private void doScan() {
        LocalDate today = LocalDate.now();
        List<Appointment> expiredList = appointmentRepository.findExpired(today);

        if (expiredList.isEmpty()) {
            return;
        }

        int count = 0;
        for (Appointment appointment : expiredList) {
            try {
                // 加行锁重新读取，防止与用户取消操作并发导致库存双重释放
                Appointment locked = appointmentRepository.findByIdForUpdate(appointment.getId());
                if (locked == null) {
                    continue;
                }
                // 再次确认仍为 APPOINTED 状态（可能已被用户取消）
                if (locked.getStatus() != AppointmentStatus.APPOINTED.getCode()) {
                    log.info("预约已被其他操作处理，跳过: appointmentId={}, currentStatus={}",
                            locked.getId(), locked.getStatus());
                    continue;
                }

                locked.transitionStatus(AppointmentStatus.EXPIRED.getCode());
                appointmentRepository.updateStatus(locked);

                // 释放已锁定的库存（如果已分配批次）
                if (locked.getBatchId() != null) {
                    try {
                        vaccineStockRepository.releaseStock(locked.getBatchId(), defaultHospitalId);
                        locked.assignBatch(null);
                        appointmentRepository.updateStatus(locked);
                    } catch (Exception e) {
                        log.warn("释放库存失败: appointmentId={}, batchId={}",
                                locked.getId(), locked.getBatchId(), e);
                    }
                }

                // === 爽约惩罚逻辑 ===
                handleNoShowPenalty(locked, today);

                count++;
            } catch (Exception e) {
                log.error("处理过期预约失败: appointmentId={}", appointment.getId(), e);
            }
        }

        log.info("预约过期扫描完成: 共处理{}条, 日期={}", count, today);
    }

    /**
     * 处理爽约惩罚：累计当月爽约次数，发送通知，达到阈值则冻结用户
     */
    private void handleNoShowPenalty(Appointment appointment, LocalDate today) {
        Long userId = appointment.getUserId();
        try {
            // 1. 增加用户爽约次数
            userRepository.incrementNoShowCount(userId);

            // 2. 查询当月已过期预约数（含本次）
            LocalDate startOfMonth = today.withDayOfMonth(1);
            int monthlyExpiredCount = appointmentRepository.countExpiredByUserInMonth(userId, startOfMonth, today);

            // 3. 读取配置
            int freezeThreshold = getConfigInt("no_show.freeze_threshold", 3);
            int freezeDays = getConfigInt("no_show.freeze_days", 30);

            // 4. 发送违约通知
            String title = "预约违约通知";
            String content;
            if (monthlyExpiredCount >= freezeThreshold) {
                // 达到阈值，冻结用户并发送冻结通知
                userRepository.freezeForNoShow(userId, freezeDays);
                User user = userRepository.findById(userId);
                String freezeEndDate = user != null && user.getFreezeEndTime() != null
                        ? user.getFreezeEndTime().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"))
                        : (today.plusDays(freezeDays).format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")));
                content = String.format(
                    "您本月已累计%d次预约未签到（爽约），系统已暂停您的预约功能至%s。如有疑问请联系工作人员。",
                    monthlyExpiredCount, freezeEndDate);
                log.info("用户因爽约被冻结: userId={}, 本月爽约{}次, 冻结{}天", userId, monthlyExpiredCount, freezeDays);
            } else {
                content = String.format(
                    "您于%s的预约（编号：%s）因未按时签到已过期。本月已累计爽约%d次，累计%d次将暂停预约功能%d天，请按时就诊。",
                    appointment.getAppointmentDate().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
                    appointment.getAppointmentNo(),
                    monthlyExpiredCount,
                    freezeThreshold,
                    freezeDays);
            }

            // 5. 创建个人通知
            SystemNotice notice = new SystemNotice(title, content, "PERSONAL", 0L);
            notice.setTargetUserId(userId);
            // 直接发布，无需审核
            notice.publish(0L, today, today.plusMonths(1));
            systemNoticeRepository.save(notice);

            log.info("爽约通知已发送: userId={}, 本月爽约次数={}", userId, monthlyExpiredCount);
        } catch (Exception e) {
            log.error("处理爽约惩罚失败: userId={}, appointmentId={}", userId, appointment.getId(), e);
        }
    }

    private int getConfigInt(String key, int defaultValue) {
        SysConfig config = configRepository.findByKey(key);
        if (config == null || config.getConfigValue() == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(config.getConfigValue());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
