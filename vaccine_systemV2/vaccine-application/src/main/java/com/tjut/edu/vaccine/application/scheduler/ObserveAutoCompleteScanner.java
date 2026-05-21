package com.tjut.edu.vaccine.application.scheduler;

import com.tjut.edu.vaccine.common.enums.AppointmentStatus;
import com.tjut.edu.vaccine.domain.appointment.aggregate.Appointment;
import com.tjut.edu.vaccine.domain.appointment.repository.AppointmentRepository;
import com.tjut.edu.vaccine.domain.observe.aggregate.ObserveRecord;
import com.tjut.edu.vaccine.domain.observe.repository.ObserveRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 留观自动完成扫描器：
 * 当日留观超过30分钟后，若医生未手动确认结束留观，系统自动完成此次接种流程。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ObserveAutoCompleteScanner {

    private final ObserveRecordRepository observeRecordRepository;
    private final AppointmentRepository appointmentRepository;

    /**
     * 每5分钟扫描一次超时留观
     */
    @Scheduled(cron = "${schedule.cron_observe_auto:0 */5 * * * ?}")
    @Transactional
    public void scanOverdueObservations() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(ObserveRecord.MIN_OBSERVE_MINUTES);
        List<ObserveRecord> overdueRecords = observeRecordRepository.findUnfinishedBefore(threshold);

        if (overdueRecords.isEmpty()) {
            return;
        }

        int count = 0;
        for (ObserveRecord record : overdueRecords) {
            try {
                // 自动完成留观记录
                record.autoFinish();
                observeRecordRepository.update(record);

                // 将预约状态从留观中(10)转为已完成(2)
                Appointment appointment = appointmentRepository.findByIdForUpdate(record.getAppointmentId());
                if (appointment != null && appointment.getStatus() == AppointmentStatus.OBSERVING.getCode()) {
                    appointment.transitionStatus(AppointmentStatus.COMPLETED.getCode());
                    appointmentRepository.updateStatus(appointment);
                }

                count++;
            } catch (Exception e) {
                log.error("自动完成留观失败: observeRecordId={}, appointmentId={}",
                        record.getId(), record.getAppointmentId(), e);
            }
        }

        if (count > 0) {
            log.info("留观自动完成扫描: 共处理{}条超时留观记录", count);
        }
    }
}
