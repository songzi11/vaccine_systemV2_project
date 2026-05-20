package com.tjut.edu.vaccine.application.assembler;

import com.tjut.edu.vaccine.application.dto.response.FEFOBatchResponse;
import com.tjut.edu.vaccine.application.dto.response.VaccinationRecordResponse;
import com.tjut.edu.vaccine.common.enums.BatchStatus;
import com.tjut.edu.vaccine.common.enums.InjectionSite;
import com.tjut.edu.vaccine.domain.stock.aggregate.VaccineBatch;
import com.tjut.edu.vaccine.domain.vaccinate.aggregate.VaccinationRecord;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class VaccinateAssembler {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static VaccinationRecordResponse toResponse(VaccinationRecord record) {
        if (record == null) {
            return null;
        }
        VaccinationRecordResponse response = new VaccinationRecordResponse();
        response.setId(record.getId());
        response.setAppointmentId(record.getAppointmentId());
        response.setInjectionId(record.getInjectionId());
        response.setInjectionTime(formatDateTime(record.getInjectionTime()));
        response.setDoctorId(record.getDoctorId());
        response.setInjectionSite(getSiteDescription(record.getInjectionSite()));
        response.setBatchId(record.getBatchId());
        response.setBatchNo(record.getBatchNo());
        response.setCreateTime(formatDateTime(record.getCreateTime()));
        return response;
    }

    public static List<VaccinationRecordResponse> toResponseList(List<VaccinationRecord> records) {
        if (records == null) {
            return List.of();
        }
        return records.stream()
                .map(VaccinateAssembler::toResponse)
                .collect(Collectors.toList());
    }

    public static FEFOBatchResponse toFEFOBatchResponse(VaccineBatch batch, int availableStock) {
        if (batch == null) {
            return null;
        }
        FEFOBatchResponse response = new FEFOBatchResponse();
        response.setBatchId(batch.getId());
        response.setBatchNo(batch.getBatchNo());
        response.setManufacturer(batch.getManufacturer());
        response.setProductionDate(batch.getProductionDate());
        response.setExpiryDate(batch.getExpiryDate());
        response.setStatus(batch.getStatus() != null ? batch.getStatus().getDescription() : null);
        response.setAvailableStock(availableStock);
        return response;
    }

    private static String getSiteDescription(String siteCode) {
        try {
            return InjectionSite.fromCode(siteCode).getDescription();
        } catch (IllegalArgumentException e) {
            return siteCode;
        }
    }

    private static String formatDateTime(java.time.LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : null;
    }
}
