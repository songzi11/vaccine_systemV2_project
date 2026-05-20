package com.tjut.edu.vaccine.application.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class VaccineBatchResponse {

    private Long id;
    private String batchNo;
    private Long vaccineId;
    private String vaccineName;
    private String vaccineType;
    private String manufacturer;
    private LocalDate productionDate;
    private LocalDate expiryDate;
    private Integer warningDays;
    private String status;
    private Integer availableStock;
    private Integer lockedStock;
    private Integer totalStock;
    private String createTime;
}
