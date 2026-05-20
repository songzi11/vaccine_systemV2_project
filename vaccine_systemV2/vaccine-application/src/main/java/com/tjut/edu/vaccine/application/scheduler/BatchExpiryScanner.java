package com.tjut.edu.vaccine.application.scheduler;

import com.tjut.edu.vaccine.common.enums.AlertType;
import com.tjut.edu.vaccine.domain.stock.entity.StockAlertLog;
import com.tjut.edu.vaccine.domain.stock.repository.StockAlertLogRepository;
import com.tjut.edu.vaccine.domain.stock.repository.VaccineBatchRepository;
import com.tjut.edu.vaccine.domain.stock.aggregate.VaccineBatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchExpiryScanner {

    private static final Long DEFAULT_HOSPITAL_ID = 1L;

    private final VaccineBatchRepository vaccineBatchRepository;
    private final StockAlertLogRepository stockAlertLogRepository;

    @Scheduled(fixedRateString = "${batch.expiry_scan_interval:3600000}")
    public void scanBatchExpiry() {
        LocalDate today = LocalDate.now();
        LocalDate warningDate = today.plusDays(30);

        // 1. 扫描临期批次
        List<VaccineBatch> nearExpiryBatches = vaccineBatchRepository.findNearExpiry(warningDate);
        int nearExpiryCount = 0;
        for (VaccineBatch batch : nearExpiryBatches) {
            try {
                if (!stockAlertLogRepository.existsUnhandledByBatchIdAndType(
                        batch.getId(), AlertType.EXPIRY_SOON.getCode())) {
                    StockAlertLog alert = new StockAlertLog();
                    alert.setAlertType(AlertType.EXPIRY_SOON);
                    alert.setVaccineId(batch.getVaccineId());
                    alert.setBatchId(batch.getId());
                    alert.setExpiryDate(batch.getExpiryDate());
                    stockAlertLogRepository.save(alert);
                    nearExpiryCount++;
                }
                vaccineBatchRepository.markNearExpiry(List.of(batch.getId()));
            } catch (Exception e) {
                log.error("处理临期批次失败: batchId={}", batch.getId(), e);
            }
        }

        // 2. 扫描过期批次
        List<VaccineBatch> expiredBatches = vaccineBatchRepository.findExpired(today);
        int expiredCount = 0;
        for (VaccineBatch batch : expiredBatches) {
            try {
                if (!stockAlertLogRepository.existsUnhandledByBatchIdAndType(
                        batch.getId(), AlertType.EXPIRED.getCode())) {
                    StockAlertLog alert = new StockAlertLog();
                    alert.setAlertType(AlertType.EXPIRED);
                    alert.setVaccineId(batch.getVaccineId());
                    alert.setBatchId(batch.getId());
                    alert.setExpiryDate(batch.getExpiryDate());
                    stockAlertLogRepository.save(alert);
                }
                vaccineBatchRepository.markExpired(List.of(batch.getId()));
                expiredCount++;
            } catch (Exception e) {
                log.error("处理过期批次失败: batchId={}", batch.getId(), e);
            }
        }

        log.info("批次过期扫描完成: 临期{}条, 过期{}条, 日期={}", nearExpiryCount, expiredCount, today);
    }
}
