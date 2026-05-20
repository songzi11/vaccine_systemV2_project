package com.tjut.edu.vaccine.application.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AppointmentDetailResponse {

    private Long id;
    private String appointmentNo;
    private Long userId;
    private Long childId;
    private Long vaccineId;
    private LocalDate appointmentDate;
    private String timeSlot;
    private Integer status;
    private String currentWindow;
    private LocalDateTime signinTime;
    private LocalDateTime cancelTime;
    private String cancelReason;
    private Long batchId;
    private String createTime;
    private String updateTime;

    /** 关联字段 */
    private String childName;
    private Integer childGender;
    private String childBirthDate;
    private String vaccineName;
    private String vaccineCategory;
    private String manufacturer;

    /** 窗口与医生信息 */
    private String windowName;
    private String windowFunctionType;
    private String doctorName;
}
