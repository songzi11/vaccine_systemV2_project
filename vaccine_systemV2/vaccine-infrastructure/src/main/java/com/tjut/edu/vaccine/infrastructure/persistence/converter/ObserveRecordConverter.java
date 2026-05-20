package com.tjut.edu.vaccine.infrastructure.persistence.converter;

import com.tjut.edu.vaccine.common.enums.ObserveResult;
import com.tjut.edu.vaccine.domain.observe.aggregate.ObserveRecord;
import com.tjut.edu.vaccine.infrastructure.persistence.po.ObserveRecordPO;

public class ObserveRecordConverter {

    public static ObserveRecord toDomain(ObserveRecordPO po) {
        if (po == null) {
            return null;
        }
        ObserveRecord record = new ObserveRecord();
        record.setId(po.getId());
        record.setAppointmentId(po.getAppointmentId());
        record.setInjectionId(po.getInjectionId());
        record.setStartTime(po.getStartTime());
        record.setFinishTime(po.getFinishTime());
        record.setDuration(po.getDuration());
        record.setObserveResult(po.getObserveResult() != null ? ObserveResult.fromCode(po.getObserveResult()) : null);
        record.setDoctorId(po.getDoctorId());
        record.setCreateTime(po.getCreateTime());
        return record;
    }

    public static ObserveRecordPO toPO(ObserveRecord record) {
        if (record == null) {
            return null;
        }
        ObserveRecordPO po = new ObserveRecordPO();
        po.setId(record.getId());
        po.setAppointmentId(record.getAppointmentId());
        po.setInjectionId(record.getInjectionId());
        po.setStartTime(record.getStartTime());
        po.setFinishTime(record.getFinishTime());
        po.setDuration(record.getDuration());
        po.setObserveResult(record.getObserveResult() != null ? record.getObserveResult().getCode() : null);
        po.setDoctorId(record.getDoctorId());
        po.setCreateTime(record.getCreateTime());
        return po;
    }
}
