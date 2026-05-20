package com.tjut.edu.vaccine.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("vaccination_record")
public class VaccinationRecordPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long appointmentId;

    private String injectionId;

    private LocalDateTime injectionTime;

    private Long doctorId;

    private String injectionSite;

    private Long batchId;

    private String batchNo;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
