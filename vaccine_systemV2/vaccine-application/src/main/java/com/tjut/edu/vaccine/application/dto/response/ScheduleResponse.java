package com.tjut.edu.vaccine.application.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ScheduleResponse {

    private Long id;
    private Long doctorId;
    private Long windowId;
    private LocalDate scheduleDate;
    private String timeSlot;
    private Integer status;
    private Integer maxCapacity;
    private String doctorName;
    private String windowName;
    private String createTime;
}
