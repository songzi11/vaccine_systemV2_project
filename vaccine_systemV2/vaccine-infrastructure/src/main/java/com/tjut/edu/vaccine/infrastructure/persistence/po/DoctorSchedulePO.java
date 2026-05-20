package com.tjut.edu.vaccine.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("doctor_schedule")
public class DoctorSchedulePO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long doctorId;

    private Long windowId;

    private LocalDate scheduleDate;

    private String timeSlot;

    private Integer status;

    private Integer maxCapacity;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
