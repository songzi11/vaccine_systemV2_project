package com.tjut.edu.vaccine.application.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VaccinateVerifyResponse {

    // ---- 预约信息 ----
    private Long appointmentId;
    private String appointmentNo;
    private String appointmentDate;
    private String timeSlot;

    // ---- 儿童信息 ----
    private String childName;
    private Integer childGender;
    private String childBirthDate;

    // ---- 疫苗信息 ----
    private String vaccineName;

    // ---- 预检信息 ----
    private BigDecimal bodyTemperature;
    private String healthStatus;

    // ---- 批次信息 ----
    private Long batchId;
    private String batchNo;
    private String manufacturer;
    private String expiryDate;
    private Integer totalStock;
    private Integer availableStock;
    private Integer lockedStock;
}
