package com.tjut.edu.vaccine.application.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class StockAlertResponse {

    private Long id;
    private String alertType;
    private String alertTypeCode;
    private Long vaccineId;
    private String vaccineName;
    private Long batchId;
    private String batchNo;
    private BigDecimal alertValue;
    private LocalDate expiryDate;
    private Boolean handled;
    private String detail;
    private String createTime;
}
