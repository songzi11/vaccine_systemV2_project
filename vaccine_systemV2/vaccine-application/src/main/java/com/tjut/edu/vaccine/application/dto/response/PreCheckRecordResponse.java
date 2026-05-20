package com.tjut.edu.vaccine.application.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PreCheckRecordResponse {

    private Long id;
    private Long appointmentId;
    private String checkTime;
    private BigDecimal bodyTemperature;
    private BigDecimal weight;
    private BigDecimal height;
    private String healthStatus;
    private String allergyHistory;
    private String medicationRecent;
    private String diseaseHistory;
    private String vaccinationRecent;
    private String checkResult;
    private String failReason;
    private Long doctorId;
    private String createTime;
}
