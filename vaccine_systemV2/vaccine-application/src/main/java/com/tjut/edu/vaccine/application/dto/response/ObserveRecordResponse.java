package com.tjut.edu.vaccine.application.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ObserveRecordResponse {

    private Long id;
    private Long appointmentId;
    private String injectionId;
    private String startTime;
    private String finishTime;
    private Integer duration;
    private String observeResult;
    private Long doctorId;
    private String createTime;
}
