package com.tjut.edu.vaccine.infrastructure.persistence.converter;

import com.tjut.edu.vaccine.common.enums.CheckResult;
import com.tjut.edu.vaccine.domain.observe.entity.PreCheckRecord;
import com.tjut.edu.vaccine.infrastructure.persistence.po.PreCheckRecordPO;

public class PreCheckRecordConverter {

    public static PreCheckRecord toDomain(PreCheckRecordPO po) {
        if (po == null) {
            return null;
        }
        PreCheckRecord record = new PreCheckRecord();
        record.setId(po.getId());
        record.setAppointmentId(po.getAppointmentId());
        record.setCheckTime(po.getCheckTime());
        record.setBodyTemperature(po.getBodyTemperature());
        record.setWeight(po.getWeight());
        record.setHeight(po.getHeight());
        record.setHealthStatus(po.getHealthStatus());
        record.setAllergyHistory(po.getAllergyHistory());
        record.setMedicationRecent(po.getMedicationRecent());
        record.setDiseaseHistory(po.getDiseaseHistory());
        record.setVaccinationRecent(po.getVaccinationRecent());
        record.setCheckResult(po.getCheckResult() != null ? CheckResult.fromCode(po.getCheckResult()) : null);
        record.setFailReason(po.getFailReason());
        record.setDoctorId(po.getDoctorId());
        record.setCreateTime(po.getCreateTime());
        return record;
    }

    public static PreCheckRecordPO toPO(PreCheckRecord record) {
        if (record == null) {
            return null;
        }
        PreCheckRecordPO po = new PreCheckRecordPO();
        po.setId(record.getId());
        po.setAppointmentId(record.getAppointmentId());
        po.setCheckTime(record.getCheckTime());
        po.setBodyTemperature(record.getBodyTemperature());
        po.setWeight(record.getWeight());
        po.setHeight(record.getHeight());
        po.setHealthStatus(record.getHealthStatus());
        po.setAllergyHistory(record.getAllergyHistory());
        po.setMedicationRecent(record.getMedicationRecent());
        po.setDiseaseHistory(record.getDiseaseHistory());
        po.setVaccinationRecent(record.getVaccinationRecent());
        po.setCheckResult(record.getCheckResult() != null ? record.getCheckResult().getCode() : null);
        po.setFailReason(record.getFailReason());
        po.setDoctorId(record.getDoctorId());
        po.setCreateTime(record.getCreateTime());
        return po;
    }
}
