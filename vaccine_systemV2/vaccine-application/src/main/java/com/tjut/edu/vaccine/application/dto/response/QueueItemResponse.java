package com.tjut.edu.vaccine.application.dto.response;

import lombok.Data;

@Data
public class QueueItemResponse {

    private Long id;
    private Long appointmentId;
    private Long queueId;
    private String queueNo;
    private String childName;
    private Long childId;
    private String vaccineName;
    private Long vaccineId;

    /** 预约状态码（对应 APPOINTMENT_STATUS_TEXT） */
    private Integer status;

    /** 排队状态码（对应 QUEUE_STATUS_TEXT：0待叫号/1已叫号/2已到达/3已过号） */
    private Integer queueStatus;

    private String signinTime;
    private String precheckTime;
    private String registerTime;
    private String appointmentDate;
    private String timeSlot;
    private String currentWindow;
    private String createTime;

    // ---- 留观专用字段 ----
    private Long injectionId;
    private String injectionNo;
    private String injectionTime;
    private Long elapsedSeconds;
    private Boolean hasAdverseReaction;

    // ---- 排队位置信息 ----
    private Integer currentQueue;
    private Integer estimatedWaitMinutes;
}
