package com.tjut.edu.vaccine.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("batch_dispose_log")
public class BatchDisposeLogPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String disposeNo;

    private Long batchId;

    private Integer disposeQuantity;

    private String disposeReason;

    private Long operatorId;

    private LocalDateTime disposeTime;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
