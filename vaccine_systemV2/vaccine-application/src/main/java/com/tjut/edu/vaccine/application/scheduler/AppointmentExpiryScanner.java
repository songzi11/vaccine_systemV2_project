package com.tjut.edu.vaccine.application.scheduler;

import com.tjut.edu.vaccine.common.enums.AppointmentStatus;
import com.tjut.edu.vaccine.domain.appointment.aggregate.Appointment;
import com.tjut.edu.vaccine.domain.appointment.repository.AppointmentRepository;
import com.tjut.edu.vaccine.domain.stock.repository.VaccineStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Scheduled(cron = "${schedule.cron_expire:0 30 0 * * ?}")
    public void scanExpiredAppointments() {
        LocalDate today = LocalDate.now();
        List<Appointment> expiredList = appointmentRepository.findExpired(today);

        if (expiredList.isEmpty()) {
            return;
        }

        int count = 0;
        for (Appointment appointment : expiredList) {
            try {
                appointment.transitionStatus(AppointmentStatus.EXPIRED.getCode());
                appointmentRepository.updateStatus(appointment);

                // 释放已锁定的库存（如果已分配批次）
                if (appointment.getBatchId() != null) {
                    try {
                        vaccineStockRepository.releaseStock(appointment.getBatchId());
                    } catch (Exception e) {
                        log.warn("释放库存失败: appointmentId={}, batchId={}",
                                appointment.getId(), appointment.getBatchId(), e);
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
