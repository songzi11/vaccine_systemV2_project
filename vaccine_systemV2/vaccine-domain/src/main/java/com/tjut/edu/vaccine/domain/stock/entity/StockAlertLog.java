package com.tjut.edu.vaccine.domain.stock.entity;

import com.tjut.edu.vaccine.common.enums.AlertType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 库存预警记录实体
 */
@Getter
@Setter
public class StockAlertLog implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private AlertType alertType;
    private Long vaccineId;
    private Long batchId;
    private BigDecimal alertValue;
    private LocalDate expiryDate;
    /**
     * 是否已处理
     */
    private boolean handled;
    private LocalDateTime createTime;

    public StockAlertLog() {
    }

    public StockAlertLog(AlertType alertType, Long vaccineId, Long batchId,
                         BigDecimal alertValue, LocalDate expiryDate) {
        if (alertType == null) {
            throw new IllegalArgumentException("预警类型不能为空");
        }
        if (vaccineId == null) {
            throw new IllegalArgumentException("疫苗ID不能为空");
        }
        this.alertType = alertType;
        this.vaccineId = vaccineId;
        this.batchId = batchId;
        this.alertValue = alertValue;
        this.expiryDate = expiryDate;
        this.handled = false;
        this.createTime = LocalDateTime.now();
    }

    public void markHandled() {
        this.handled = true;
    }
}
