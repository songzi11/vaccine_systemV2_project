package com.tjut.edu.vaccine.application.assembler;

import com.tjut.edu.vaccine.application.dto.response.AppointmentDetailResponse;
import com.tjut.edu.vaccine.application.dto.response.AppointmentResponse;
import com.tjut.edu.vaccine.common.enums.AppointmentStatus;
import com.tjut.edu.vaccine.domain.appointment.aggregate.Appointment;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AppointmentAssembler {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static AppointmentResponse toResponse(Appointment appointment) {
        if (appointment == null) {
            return null;
        }
        AppointmentResponse response = new AppointmentResponse();
        response.setId(appointment.getId());
        response.setAppointmentNo(appointment.getAppointmentNo());
        response.setChildId(appointment.getChildId());
        response.setVaccineId(appointment.getVaccineId());
        response.setAppointmentDate(appointment.getAppointmentDate());
        response.setTimeSlot(appointment.getTimeSlot());
        response.setStatus(appointment.getStatus());
        response.setCreateTime(formatDateTime(appointment.getCreateTime()));
        response.setCurrentWindow(appointment.getCurrentWindow());
        return response;
    }

    public static AppointmentDetailResponse toDetailResponse(Appointment appointment) {
        if (appointment == null) {
            return null;
        }
        AppointmentDetailResponse response = new AppointmentDetailResponse();
        response.setId(appointment.getId());
        response.setAppointmentNo(appointment.getAppointmentNo());
        response.setUserId(appointment.getUserId());
        response.setChildId(appointment.getChildId());
        response.setVaccineId(appointment.getVaccineId());
        response.setAppointmentDate(appointment.getAppointmentDate());
        response.setTimeSlot(appointment.getTimeSlot());
        response.setStatus(appointment.getStatus());
        response.setCurrentWindow(appointment.getCurrentWindow());
        response.setSigninTime(appointment.getSigninTime());
        response.setCancelTime(appointment.getCancelTime());
        response.setCancelReason(appointment.getCancelReason());
        response.setBatchId(appointment.getBatchId());
        response.setCreateTime(formatDateTime(appointment.getCreateTime()));
        response.setUpdateTime(formatDateTime(appointment.getUpdateTime()));
        return response;
    }

    public static List<AppointmentResponse> toResponseList(List<Appointment> appointments) {
        if (appointments == null) {
            return List.of();
        }
        return appointments.stream()
                .map(AppointmentAssembler::toResponse)
                .collect(Collectors.toList());
    }

    public static List<AppointmentDetailResponse> toDetailResponseList(List<Appointment> appointments) {
        if (appointments == null) {
            return List.of();
        }
        return appointments.stream()
                .map(AppointmentAssembler::toDetailResponse)
                .collect(Collectors.toList());
    }

    private static String getStatusDescription(int statusCode) {
        try {
            return AppointmentStatus.fromCode(statusCode).getDescription();
        } catch (IllegalArgumentException e) {
            return String.valueOf(statusCode);
        }
    }

    private static String formatDateTime(java.time.LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : null;
    }
}
