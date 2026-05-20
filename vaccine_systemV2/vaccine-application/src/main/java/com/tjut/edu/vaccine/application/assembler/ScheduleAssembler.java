package com.tjut.edu.vaccine.application.assembler;

import com.tjut.edu.vaccine.application.dto.response.ScheduleResponse;
import com.tjut.edu.vaccine.application.dto.response.WindowResponse;
import com.tjut.edu.vaccine.domain.identity.entity.DoctorSchedule;
import com.tjut.edu.vaccine.domain.identity.entity.HospitalWindow;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ScheduleAssembler {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String[] SCHEDULE_STATUS = {"正常", "请假", "取消"};
    private static final String[] WINDOW_STATUS = {"启用", "禁用"};

    public static ScheduleResponse toScheduleResponse(DoctorSchedule schedule) {
        if (schedule == null) {
            return null;
        }
        ScheduleResponse response = new ScheduleResponse();
        response.setId(schedule.getId());
        response.setDoctorId(schedule.getDoctorId());
        response.setWindowId(schedule.getWindowId());
        response.setScheduleDate(schedule.getScheduleDate());
        response.setTimeSlot(schedule.getTimeSlot());
        response.setStatus(schedule.getStatus());
        response.setMaxCapacity(schedule.getMaxCapacity());
        response.setCreateTime(formatDateTime(schedule.getCreateTime()));
        return response;
    }

    public static List<ScheduleResponse> toScheduleResponseList(List<DoctorSchedule> schedules) {
        if (schedules == null) {
            return List.of();
        }
        return schedules.stream()
                .map(ScheduleAssembler::toScheduleResponse)
                .collect(Collectors.toList());
    }

    public static WindowResponse toWindowResponse(HospitalWindow window) {
        if (window == null) {
            return null;
        }
        WindowResponse response = new WindowResponse();
        response.setId(window.getId());
        response.setWindowCode(window.getWindowCode());
        response.setWindowName(window.getWindowName());
        response.setWindowFunctionType(window.getWindowFunctionType());
        response.setStatus(window.getStatus());
        response.setAvgHandleTime(window.getAvgHandleTime());
        response.setSortOrder(window.getSortOrder());
        response.setDoctorId(window.getDoctorId());
        response.setCreateTime(formatDateTime(window.getCreateTime()));
        return response;
    }

    public static List<WindowResponse> toWindowResponseList(List<HospitalWindow> windows) {
        if (windows == null) {
            return List.of();
        }
        return windows.stream()
                .map(ScheduleAssembler::toWindowResponse)
                .collect(Collectors.toList());
    }

    private static String getStatusDesc(int status, String[] descriptions) {
        return status >= 0 && status < descriptions.length ? descriptions[status] : String.valueOf(status);
    }

    private static String formatDateTime(java.time.LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : null;
    }
}
