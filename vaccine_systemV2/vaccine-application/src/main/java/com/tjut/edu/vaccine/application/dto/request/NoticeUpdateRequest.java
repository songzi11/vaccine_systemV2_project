package com.tjut.edu.vaccine.application.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class NoticeUpdateRequest {

    @Size(max = 200, message = "标题最多200个字符")
    private String title;

    private String content;

    @Size(max = 20, message = "公告类型最多20个字符")
    private String noticeType;

    private Integer status;

    private LocalDate startTime;

    private LocalDate endTime;
}
