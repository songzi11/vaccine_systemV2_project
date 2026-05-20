package com.tjut.edu.vaccine.domain.stock.aggregate;

import com.tjut.edu.vaccine.common.enums.BatchStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 疫苗批次聚合根
 */
@Getter
@Setter
public class VaccineBatch implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String batchNo;
    private Long vaccineId;
    private String manufacturer;
    private LocalDate productionDate;
    private LocalDate expiryDate;
    /**
     * 预警天数
     */
    private int warningDays;
    private BatchStatus status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public VaccineBatch() {
    }

    public VaccineBatch(String batchNo, Long vaccineId, String manufacturer,
                        LocalDate productionDate, LocalDate expiryDate, int warningDays) {
        if (batchNo == null || batchNo.isBlank()) {
            throw new IllegalArgumentException("批次编号不能为空");
        }
        if (vaccineId == null) {
            throw new IllegalArgumentException("疫苗ID不能为空");
        }
        if (expiryDate == null) {
            throw new IllegalArgumentException("过期日期不能为空");
        }
        if (productionDate != null && !productionDate.isBefore(expiryDate)) {
            throw new IllegalArgumentException("生产日期必须早于过期日期");
        }
        this.batchNo = batchNo;
        this.vaccineId = vaccineId;
        this.manufacturer = manufacturer;
        this.productionDate = productionDate;
        this.expiryDate = expiryDate;
        this.warningDays = warningDays > 0 ? warningDays : 30;
        this.status = BatchStatus.NORMAL;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 标记为即将过期
     */
    public void markNearExpiry() {
        if (this.status == BatchStatus.EXPIRED || this.status == BatchStatus.DISPOSED) {
            throw new IllegalStateException("已过期或已处置的批次不能标记为即将过期");
        }
        this.status = BatchStatus.NEAR_EXPIRY;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 标记为已过期
     */
    public void markExpired() {
        if (this.status == BatchStatus.DISPOSED) {
            throw new IllegalStateException("已处置的批次不能标记为过期");
        }
        this.status = BatchStatus.EXPIRED;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 处置
     */
    public void dispose() {
        this.status = BatchStatus.DISPOSED;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 判断是否可用
     */
    public boolean isAvailable() {
        return this.status == BatchStatus.NORMAL || this.status == BatchStatus.NEAR_EXPIRY;
    }

    /**
     * 判断是否已过期
     */
    public boolean isExpired() {
        return this.status == BatchStatus.EXPIRED || this.status == BatchStatus.DISPOSED;
    }
}
