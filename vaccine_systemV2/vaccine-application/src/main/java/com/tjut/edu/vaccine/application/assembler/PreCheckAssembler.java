package com.tjut.edu.vaccine.application.assembler;

import com.tjut.edu.vaccine.application.dto.response.PreCheckRecordResponse;
import com.tjut.edu.vaccine.domain.observe.entity.PreCheckRecord;

import java.time.format.DateTimeFormatter;

public class PreCheckAssembler {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static PreCheckRecordResponse toResponse(PreCheckRecord record) {
        if (record == null) {
            return null;
        }
        PreCheckRecordResponse response = new PreCheckRecordResponse();
        response.setId(record.getId());
        response.setAppointmentId(record.getAppointmentId());
        response.setCheckTime(formatDateTime(record.getCheckTime()));
        response.setBodyTemperature(record.getBodyTemperature());
        response.setWeight(record.getWeight());
        response.setHeight(record.getHeight());
        response.setHealthStatus(record.getHealthStatus());
        response.setAllergyHistory(record.getAllergyHistory());
        response.setMedicationRecent(record.getMedicationRecent());
        response.setDiseaseHistory(record.getDiseaseHistory());
        response.setVaccinationRecent(record.getVaccinationRecent());
        response.setCheckResult(record.getCheckResult() != null ? record.getCheckResult().getDescription() : null);
        response.setFailReason(record.getFailReason());
        response.setDoctorId(record.getDoctorId());
        response.setCreateTime(formatDateTime(record.getCreateTime()));
        return response;
    }

    private static String formatDateTime(java.time.LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : null;
    }
}
