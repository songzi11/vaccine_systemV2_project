package com.tjut.edu.vaccine.infrastructure.persistence.converter;

import com.tjut.edu.vaccine.domain.appointment.aggregate.Appointment;
import com.tjut.edu.vaccine.infrastructure.persistence.po.AppointmentPO;

public class AppointmentConverter {

    public static Appointment toDomain(AppointmentPO po) {
        if (po == null) {
            return null;
        }
        Appointment appointment = new Appointment();
        appointment.setId(po.getId());
        appointment.setAppointmentNo(po.getAppointmentNo());
        appointment.setUserId(po.getUserId());
        appointment.setChildId(po.getChildId());
        appointment.setVaccineId(po.getVaccineId());
        appointment.setAppointmentDate(po.getAppointmentDate());
        appointment.setTimeSlot(po.getTimeSlot());
        appointment.setStatus(po.getStatus() != null ? po.getStatus() : 0);
        appointment.setCurrentWindow(po.getCurrentWindow());
        appointment.setSigninTime(po.getSigninTime());
        appointment.setCancelTime(po.getCancelTime());
        appointment.setCancelReason(po.getCancelReason());
        appointment.setBatchId(po.getBatchId());
        appointment.setCreateTime(po.getCreateTime());
        appointment.setUpdateTime(po.getUpdateTime());
        return appointment;
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
