package com.tjut.edu.vaccine.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pre_check_record")
public class PreCheckRecordPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long appointmentId;

    private LocalDateTime checkTime;

    private BigDecimal bodyTemperature;

    private BigDecimal weight;

    private BigDecimal height;

    private String healthStatus;

    private String allergyHistory;

    private String medicationRecent;

    private String diseaseHistory;

    private String vaccinationRecent;

    private String checkResult;

    private String failReason;

    private Long doctorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
