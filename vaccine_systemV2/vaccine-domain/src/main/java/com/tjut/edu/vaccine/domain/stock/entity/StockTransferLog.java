package com.tjut.edu.vaccine.domain.stock.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 库存调拨记录实体
 */
@Getter
@Setter
public class StockTransferLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 来源类型常量: 0=仓库, 1=医院
     */
    public static final int FROM_TYPE_WAREHOUSE = 0;
    public static final int FROM_TYPE_HOSPITAL = 1;

    private Long id;
    private String transferNo;
    private Long batchId;
    private int fromType;
    private Long fromId;
    private int toType;
    private Long toId;
    private int quantity;
    private Long operatorId;
    private LocalDateTime transferTime;
    private String remark;
    private LocalDateTime createTime;

    public StockTransferLog() {
    }

    public StockTransferLog(String transferNo, Long batchId, int fromType, Long fromId,
                            int toType, Long toId, int quantity, Long operatorId, String remark) {
        if (transferNo == null || transferNo.isBlank()) {
            throw new IllegalArgumentException("调拨编号不能为空");
        }
        if (batchId == null) {
            throw new IllegalArgumentException("批次ID不能为空");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("调拨数量必须大于0");
        }
        this.transferNo = transferNo;
        this.batchId = batchId;
        this.fromType = fromType;
        this.fromId = fromId;
        this.toType = toType;
        this.toId = toId;
        this.quantity = quantity;
        this.operatorId = operatorId;
        this.transferTime = LocalDateTime.now();
        this.remark = remark;
        this.createTime = LocalDateTime.now();
    }
}
