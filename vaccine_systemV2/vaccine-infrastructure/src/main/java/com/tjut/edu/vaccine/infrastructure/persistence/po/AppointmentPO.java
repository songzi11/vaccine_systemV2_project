package com.tjut.edu.vaccine.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("appointment")
public class AppointmentPO {

    @TableId(type = IdType.AUTO)
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

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
