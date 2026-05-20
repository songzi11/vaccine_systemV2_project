package com.tjut.edu.vaccine.infrastructure.persistence.converter;

import com.tjut.edu.vaccine.domain.identity.entity.DoctorSchedule;
import com.tjut.edu.vaccine.infrastructure.persistence.po.DoctorSchedulePO;

public class DoctorScheduleConverter {

    public static DoctorSchedule toDomain(DoctorSchedulePO po) {
        if (po == null) {
            return null;
        }
        DoctorSchedule schedule = new DoctorSchedule();
        schedule.setId(po.getId());
        schedule.setDoctorId(po.getDoctorId());
        schedule.setWindowId(po.getWindowId());
        schedule.setScheduleDate(po.getScheduleDate());
        schedule.setTimeSlot(po.getTimeSlot());
        schedule.setStatus(po.getStatus());
        schedule.setMaxCapacity(po.getMaxCapacity());
        schedule.setCreateTime(po.getCreateTime());
        schedule.setUpdateTime(po.getUpdateTime());
        return schedule;
    }

    public static DoctorSchedulePO toPO(DoctorSchedule schedule) {
        if (schedule == null) {
            return null;
        }
        DoctorSchedulePO po = new DoctorSchedulePO();
        po.setId(schedule.getId());
        po.setDoctorId(schedule.getDoctorId());
        po.setWindowId(schedule.getWindowId());
        po.setScheduleDate(schedule.getScheduleDate());
        po.setTimeSlot(schedule.getTimeSlot());
        po.setStatus(schedule.getStatus());
        po.setMaxCapacity(schedule.getMaxCapacity());
        return po;
    }
}
