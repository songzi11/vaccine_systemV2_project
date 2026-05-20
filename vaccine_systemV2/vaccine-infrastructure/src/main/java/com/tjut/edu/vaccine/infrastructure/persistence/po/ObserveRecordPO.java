package com.tjut.edu.vaccine.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("observe_record")
public class ObserveRecordPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long appointmentId;

    private String injectionId;

    private LocalDateTime startTime;

    private LocalDateTime finishTime;

    private Integer duration;

    private String observeResult;

    private Long doctorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
