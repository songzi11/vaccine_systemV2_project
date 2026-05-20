package com.tjut.edu.vaccine.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("system_notice")
public class SystemNoticePO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    private String noticeType;

    private Integer status;

    private Long authorId;

    private Long auditUserId;

    private LocalDateTime auditTime;

    private String auditReason;

    private LocalDateTime publishTime;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
