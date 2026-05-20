package com.tjut.edu.vaccine.infrastructure.persistence.converter;

import com.tjut.edu.vaccine.common.enums.AppointmentStatus;
import com.tjut.edu.vaccine.domain.appointment.aggregate.Appointment;
import com.tjut.edu.vaccine.infrastructure.persistence.po.AppointmentPO;

public class AppointmentConverter {

    public static Appointment toDomain(AppointmentPO po) {
        if (po == null) {
            return null;
        }
        return Appointment.reconstruct(
                po.getId(), po.getAppointmentNo(), po.getUserId(), po.getChildId(),
                po.getVaccineId(), po.getAppointmentDate(), po.getTimeSlot(),
                po.getStatus() != null ? po.getStatus() : AppointmentStatus.APPOINTED.getCode(), po.getCurrentWindow(),
                po.getSigninTime(), po.getCancelTime(), po.getCancelReason(), po.getBatchId(),
                po.getCreateTime(), po.getUpdateTime()
        );
    }

    public static AppointmentPO toPO(Appointment appointment) {
        if (appointment == null) {
            return null;
        }
        AppointmentPO po = new AppointmentPO();
        po.setId(appointment.getId());
        po.setAppointmentNo(appointment.getAppointmentNo());
        po.setUserId(appointment.getUserId());
        po.setChildId(appointment.getChildId());
        po.setVaccineId(appointment.getVaccineId());
        po.setAppointmentDate(appointment.getAppointmentDate());
        po.setTimeSlot(appointment.getTimeSlot());
        po.setStatus(appointment.getStatus());
        po.setCurrentWindow(appointment.getCurrentWindow());
        po.setSigninTime(appointment.getSigninTime());
        po.setCancelTime(appointment.getCancelTime());
        po.setCancelReason(appointment.getCancelReason());
        po.setBatchId(appointment.getBatchId());
        po.setCreateTime(appointment.getCreateTime());
        po.setUpdateTime(appointment.getUpdateTime());
        return po;
    }
}
