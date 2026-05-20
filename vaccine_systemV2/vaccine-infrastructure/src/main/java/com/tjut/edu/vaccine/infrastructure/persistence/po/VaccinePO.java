package com.tjut.edu.vaccine.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("vaccine")
public class VaccinePO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String vaccineName;

    private String vaccineType;

    private String manufacturer;

    private String description;

    private Integer isOnShelf;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
