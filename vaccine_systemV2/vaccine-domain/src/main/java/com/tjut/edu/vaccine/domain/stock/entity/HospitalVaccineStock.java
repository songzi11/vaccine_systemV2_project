package com.tjut.edu.vaccine.domain.stock.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 医院疫苗库存明细实体
 *
 * 库存模型：
 *   totalStock     = 批次导入总数，导入后不随接种变化
 *   availableStock = 可直接使用的库存
 *   lockedStock    = 接种执行中临时锁定的库存
 */
@Getter
@Setter
public class HospitalVaccineStock implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long hospitalId;
    private Long batchId;
    private Integer locationType;
    private Long locationId;
    private int totalStock;
    private int availableStock;
    private int lockedStock;

    public HospitalVaccineStock() {
    }

    public HospitalVaccineStock(Long hospitalId, Long batchId, int availableStock) {
        if (hospitalId == null) {
            throw new IllegalArgumentException("医院ID不能为空");
        }
        if (batchId == null) {
            throw new IllegalArgumentException("批次ID不能为空");
        }
        this.hospitalId = hospitalId;
        this.batchId = batchId;
        this.locationType = 0;
        this.locationId = null;
        this.totalStock = availableStock;
        this.availableStock = availableStock;
        this.lockedStock = 0;
    }

    public HospitalVaccineStock(Long hospitalId, Long batchId, Integer locationType, Long locationId, int availableStock) {
        if (hospitalId == null) {
            throw new IllegalArgumentException("医院ID不能为空");
        }
        if (batchId == null) {
            throw new IllegalArgumentException("批次ID不能为空");
        }
        this.hospitalId = hospitalId;
        this.batchId = batchId;
        this.locationType = locationType != null ? locationType : 0;
        this.locationId = locationId;
        this.totalStock = availableStock;
        this.availableStock = availableStock;
        this.lockedStock = 0;
    }

    /**
     * 锁定库存：从可用库存转为预约预留库存
     */
    public void lockStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("锁定数量必须大于0");
        }
        if (quantity > this.availableStock) {
            throw new IllegalStateException("可用库存不足，无法锁定");
        }
        this.availableStock -= quantity;
        this.lockedStock += quantity;
    }

    /**
     * 扣减库存：接种出库，消耗已预留库存（totalStock 不变，代表初始入库量）
     */
    public void deductStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("扣减数量必须大于0");
        }
        if (quantity > this.lockedStock) {
            throw new IllegalStateException("库存不足，无法扣减");
        }
        this.lockedStock -= quantity;
    }

    /**
     * 释放锁定：取消预留，退回可用库存
     */
    public void releaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("释放数量必须大于0");
        }
        if (quantity > this.lockedStock) {
            throw new IllegalStateException("释放库存不能超过锁定库存");
        }
        this.lockedStock -= quantity;
        this.availableStock += quantity;
    }

    public void addStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("入库数量必须大于0");
        }
        this.availableStock += quantity;
    }

    /** 可用库存 */
    public int getTrulyAvailable() {
        return this.availableStock;
    }
}
