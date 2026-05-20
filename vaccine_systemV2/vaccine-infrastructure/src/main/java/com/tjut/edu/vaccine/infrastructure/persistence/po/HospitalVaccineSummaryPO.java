package com.tjut.edu.vaccine.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("hospital_vaccine_summary")
public class HospitalVaccineSummaryPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long hospitalId;

    private Long vaccineId;

    private Integer totalStock;

    private Integer availableStock;

    private Integer warningThreshold;

    private Integer version;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
