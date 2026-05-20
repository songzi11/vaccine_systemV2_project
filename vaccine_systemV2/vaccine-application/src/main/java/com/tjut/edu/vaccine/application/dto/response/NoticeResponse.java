package com.tjut.edu.vaccine.application.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class NoticeResponse {

    private Long id;
    private String title;
    private String content;
    private String noticeType;
    private String status;
    private Long authorId;
    private String publisherName;
    private Integer statusCode;
    private LocalDate startTime;
    private LocalDate endTime;
    private String publishTime;
    private String createTime;
    private String auditReason;
}
