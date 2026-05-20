package com.tjut.edu.vaccine.application.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VerifyCodeResponse {
    private Long id;
    private String code;
    private Integer statusCode;
    private String statusText;
    private Long createdBy;
    private String creatorName;
    private Long usedBy;
    private String usedByName;
    private LocalDateTime usedAt;
    private LocalDateTime createTime;
}
