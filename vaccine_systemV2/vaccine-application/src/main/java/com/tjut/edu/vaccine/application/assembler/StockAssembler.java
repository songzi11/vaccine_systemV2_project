package com.tjut.edu.vaccine.application.assembler;

import com.tjut.edu.vaccine.application.dto.response.HospitalStockResponse;
import com.tjut.edu.vaccine.application.dto.response.StockAlertResponse;
import com.tjut.edu.vaccine.application.dto.response.VaccineBatchResponse;
import com.tjut.edu.vaccine.common.enums.BatchStatus;
import com.tjut.edu.vaccine.domain.stock.aggregate.VaccineBatch;
import com.tjut.edu.vaccine.domain.stock.entity.HospitalVaccineStock;
import com.tjut.edu.vaccine.domain.stock.entity.StockAlertLog;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class StockAssembler {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static VaccineBatchResponse toBatchResponse(VaccineBatch batch) {
        if (batch == null) {
            return null;
        }
        VaccineBatchResponse response = new VaccineBatchResponse();
        response.setId(batch.getId());
        response.setBatchNo(batch.getBatchNo());
        response.setVaccineId(batch.getVaccineId());
        response.setManufacturer(batch.getManufacturer());
        response.setProductionDate(batch.getProductionDate());
        response.setExpiryDate(batch.getExpiryDate());
        response.setWarningDays(batch.getWarningDays());
        response.setStatus(batch.getStatus() != null ? batch.getStatus().getDescription() : null);
        response.setCreateTime(formatDateTime(batch.getCreateTime()));
        return response;
    }

    public static List<VaccineBatchResponse> toBatchResponseList(List<VaccineBatch> batches) {
        if (batches == null) {
            return List.of();
        }
        return batches.stream()
                .map(StockAssembler::toBatchResponse)
                .collect(Collectors.toList());
    }

    public static HospitalStockResponse toStockResponse(HospitalVaccineStock stock, Long vaccineId,
                                                         String vaccineName, String vaccineType,
                                                         String batchNo, String status) {
        if (stock == null) {
            return null;
        }
        HospitalStockResponse response = new HospitalStockResponse();
        response.setId(stock.getId());
        response.setVaccineId(vaccineId);
        response.setVaccineName(vaccineName);
        response.setVaccineType(vaccineType);
        response.setBatchNo(batchNo);
        response.setAvailableStock(stock.getAvailableStock());
        response.setLockedStock(stock.getLockedStock());
        response.setTotalStock(stock.getTotalStock());
        response.setStatus(status);
        return response;
    }

    public static StockAlertResponse toAlertResponse(StockAlertLog alert) {
        if (alert == null) {
            return null;
        }
        StockAlertResponse response = new StockAlertResponse();
        response.setId(alert.getId());
        response.setAlertType(alert.getAlertType() != null ? alert.getAlertType().getDescription() : null);
        response.setAlertTypeCode(alert.getAlertType() != null ? alert.getAlertType().getCode() : null);
        response.setVaccineId(alert.getVaccineId());
        response.setBatchId(alert.getBatchId());
        response.setAlertValue(alert.getAlertValue());
        response.setExpiryDate(alert.getExpiryDate());
        response.setHandled(alert.isHandled());
        response.setCreateTime(formatDateTime(alert.getCreateTime()));
        return response;
    }

    public static List<StockAlertResponse> toAlertResponseList(List<StockAlertLog> alerts) {
        if (alerts == null) {
            return List.of();
        }
        return alerts.stream()
                .map(StockAssembler::toAlertResponse)
                .collect(Collectors.toList());
    }

    private static String formatDateTime(java.time.LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : null;
    }
}
