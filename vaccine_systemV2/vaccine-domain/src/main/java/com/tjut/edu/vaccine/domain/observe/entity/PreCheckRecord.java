package com.tjut.edu.vaccine.domain.observe.entity;

import com.tjut.edu.vaccine.common.enums.CheckResult;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预检记录实体
 */
@Getter
@Setter
public class PreCheckRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long appointmentId;
    private LocalDateTime checkTime;
    private BigDecimal bodyTemperature;
    private BigDecimal weight;
    private BigDecimal height;
    private String healthStatus;
    private String allergyHistory;
    private String medicationRecent;
    private String diseaseHistory;
    private String vaccinationRecent;
    private CheckResult checkResult;
    private String failReason;
    private Long doctorId;
    private LocalDateTime createTime;

    public PreCheckRecord() {
    }

    public PreCheckRecord(Long appointmentId, Long doctorId,
                          BigDecimal bodyTemperature, BigDecimal weight, BigDecimal height,
                          String healthStatus, String allergyHistory, String medicationRecent,
                          String diseaseHistory, String vaccinationRecent) {
        if (appointmentId == null) {
            throw new IllegalArgumentException("预约ID不能为空");
        }
        this.appointmentId = appointmentId;
        this.doctorId = doctorId;
        this.checkTime = LocalDateTime.now();
        this.bodyTemperature = bodyTemperature;
        this.weight = weight;
        this.height = height;
        this.healthStatus = healthStatus;
        this.allergyHistory = allergyHistory;
        this.medicationRecent = medicationRecent;
        this.diseaseHistory = diseaseHistory;
        this.vaccinationRecent = vaccinationRecent;
        this.createTime = LocalDateTime.now();
    }

    public void pass() {
        this.checkResult = CheckResult.PASS;
    }

    public void fail(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("预检失败原因不能为空");
        }
        this.checkResult = CheckResult.FAIL;
        this.failReason = reason;
    }

    public boolean isPassed() {
        return this.checkResult == CheckResult.PASS;
    }

    public boolean isFailed() {
        return this.checkResult == CheckResult.FAIL;
    }
}
