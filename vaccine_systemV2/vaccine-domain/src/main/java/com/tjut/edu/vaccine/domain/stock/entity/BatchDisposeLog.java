package com.tjut.edu.vaccine.domain.stock.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 批次处置记录实体
 */
@Getter
@Setter
public class BatchDisposeLog implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String disposeNo;
    private Long batchId;
    private int disposeQuantity;
    private String disposeReason;
    private Long operatorId;
    private LocalDateTime disposeTime;
    private String remark;
    private LocalDateTime createTime;

    public BatchDisposeLog() {
    }

    public BatchDisposeLog(String disposeNo, Long batchId, int disposeQuantity,
                           String disposeReason, Long operatorId, String remark) {
        if (disposeNo == null || disposeNo.isBlank()) {
            throw new IllegalArgumentException("处置编号不能为空");
        }
        if (batchId == null) {
            throw new IllegalArgumentException("批次ID不能为空");
        }
        if (disposeQuantity <= 0) {
            throw new IllegalArgumentException("处置数量必须大于0");
        }
        this.disposeNo = disposeNo;
        this.batchId = batchId;
        this.disposeQuantity = disposeQuantity;
        this.disposeReason = disposeReason;
        this.operatorId = operatorId;
        this.disposeTime = LocalDateTime.now();
        this.remark = remark;
        this.createTime = LocalDateTime.now();
    }
}
