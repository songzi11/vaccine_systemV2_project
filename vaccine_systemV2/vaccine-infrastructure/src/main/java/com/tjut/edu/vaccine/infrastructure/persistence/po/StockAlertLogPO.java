package com.tjut.edu.vaccine.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("stock_alert_log")
public class StockAlertLogPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String alertType;

    private Long vaccineId;

    private Long batchId;

    private BigDecimal alertValue;

    private LocalDate expiryDate;

    private Integer isHandled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
