package com.tjut.edu.vaccine.application.scheduler;

import com.tjut.edu.vaccine.common.enums.AppointmentStatus;
import com.tjut.edu.vaccine.domain.appointment.aggregate.Appointment;
import com.tjut.edu.vaccine.domain.appointment.repository.AppointmentRepository;
import com.tjut.edu.vaccine.domain.stock.repository.VaccineStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentExpiryScanner {

    private final AppointmentRepository appointmentRepository;
    private final VaccineStockRepository vaccineStockRepository;

    @Value("${vaccine.default-hospital-id:1}")
    private Long defaultHospitalId;

    @Scheduled(cron = "${schedule.cron_expire:0 30 0 * * ?}")
    @Transactional
    public void scanExpiredAppointments() {
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
                count++;
            } catch (Exception e) {
                log.error("处理过期预约失败: appointmentId={}", appointment.getId(), e);
            }
        }

        log.info("预约过期扫描完成: 共处理{}条, 日期={}", count, today);
    }
}
