package com.tjut.edu.vaccine.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("adverse_reaction")
public class AdverseReactionPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long observeRecordId;

    private Long appointmentId;

    private String reactionType;

    private String description;

    private String severity;

    private LocalDateTime reportTime;

    private LocalDateTime handleTime;

    private String handleResult;

    private Long handlerId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
