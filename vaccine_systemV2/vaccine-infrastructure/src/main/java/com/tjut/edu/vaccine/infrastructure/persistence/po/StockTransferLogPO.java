package com.tjut.edu.vaccine.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("stock_transfer_log")
public class StockTransferLogPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String transferNo;

    private Long batchId;

    private Integer fromType;

    private Long fromId;

    private Integer toType;

    private Long toId;

    private Integer quantity;

    private Long operatorId;

    private LocalDateTime transferTime;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
