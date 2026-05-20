package com.tjut.edu.vaccine.application.dto.response;

import lombok.Data;

@Data
public class AdverseReactionResponse {

    private Long id;
    private Long observeRecordId;
    private Long appointmentId;
    private String reactionType;
    private String description;
    private String severity;
    private String reportTime;
    private String handleTime;
    private String handleResult;
    private Long handlerId;
    private String createTime;
}
