package com.tjut.edu.vaccine.domain.stock.aggregate;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 医院疫苗库存汇总聚合根
 */
@Getter
@Setter
public class HospitalVaccineSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long hospitalId;
    private Long vaccineId;
    private int totalStock;
    private int availableStock;
    private int warningThreshold;
    /**
     * 乐观锁版本号
     */
    private int version;
    private LocalDateTime updateTime;

    public HospitalVaccineSummary() {
    }

    public HospitalVaccineSummary(Long hospitalId, Long vaccineId, int warningThreshold) {
        if (hospitalId == null) {
            throw new IllegalArgumentException("医院ID不能为空");
        }
        if (vaccineId == null) {
            throw new IllegalArgumentException("疫苗ID不能为空");
        }
        this.hospitalId = hospitalId;
        this.vaccineId = vaccineId;
        this.totalStock = 0;
        this.availableStock = 0;
        this.warningThreshold = warningThreshold;
        this.version = 0;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 重新计算库存汇总
     */
    public void recalculate(int total, int available) {
        if (total < 0) {
            throw new IllegalArgumentException("总库存不能为负数");
        }
        if (available < 0) {
            throw new IllegalArgumentException("可用库存不能为负数");
        }
        if (available > total) {
            throw new IllegalArgumentException("可用库存不能大于总库存");
        }
        this.totalStock = total;
        this.availableStock = available;
        this.version++;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 判断是否库存不足
     */
    public boolean isLowStock() {
        return this.availableStock <= this.warningThreshold;
    }
}
