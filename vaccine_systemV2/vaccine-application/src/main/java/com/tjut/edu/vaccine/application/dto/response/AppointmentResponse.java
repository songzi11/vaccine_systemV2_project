package com.tjut.edu.vaccine.application.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AppointmentResponse {

    private Long id;
    private String appointmentNo;
    private Long childId;
    private Long vaccineId;
    private LocalDate appointmentDate;
    private String timeSlot;
    private Integer status;
    private String createTime;

    /** 关联字段 */
    private String childName;
    private String vaccineName;

    /** 窗口信息 */
    private String currentWindow;
    private String windowName;
}
