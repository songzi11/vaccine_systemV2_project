package com.tjut.edu.vaccine.infrastructure.persistence.converter;

import com.tjut.edu.vaccine.domain.vaccinate.aggregate.VaccinationRecord;
import com.tjut.edu.vaccine.infrastructure.persistence.po.VaccinationRecordPO;

public class VaccinationRecordConverter {

    public static VaccinationRecord toDomain(VaccinationRecordPO po) {
        if (po == null) {
            return null;
        }
        VaccinationRecord record = new VaccinationRecord();
        record.setId(po.getId());
        record.setAppointmentId(po.getAppointmentId());
        record.setInjectionId(po.getInjectionId());
        record.setInjectionTime(po.getInjectionTime());
        record.setDoctorId(po.getDoctorId());
        record.setInjectionSite(po.getInjectionSite());
        record.setBatchId(po.getBatchId());
        record.setBatchNo(po.getBatchNo());
        record.setCreateTime(po.getCreateTime());
        record.setUpdateTime(po.getUpdateTime());
        return record;
    }

    public static VaccinationRecordPO toPO(VaccinationRecord record) {
        if (record == null) {
            return null;
        }
        VaccinationRecordPO po = new VaccinationRecordPO();
        po.setId(record.getId());
        po.setAppointmentId(record.getAppointmentId());
        po.setInjectionId(record.getInjectionId());
        po.setInjectionTime(record.getInjectionTime());
        po.setDoctorId(record.getDoctorId());
        po.setInjectionSite(record.getInjectionSite());
        po.setBatchId(record.getBatchId());
        po.setBatchNo(record.getBatchNo());
        po.setCreateTime(record.getCreateTime());
        po.setUpdateTime(record.getUpdateTime());
        return po;
    }
}
