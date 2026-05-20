package com.tjut.edu.vaccine.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("vaccine_batch")
public class VaccineBatchPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String batchNo;

    private Long vaccineId;

    private String manufacturer;

    private LocalDate productionDate;

    private LocalDate expiryDate;

    private Integer warningDays;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
