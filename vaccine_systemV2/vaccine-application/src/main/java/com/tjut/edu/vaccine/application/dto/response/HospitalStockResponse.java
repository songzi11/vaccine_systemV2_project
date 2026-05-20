package com.tjut.edu.vaccine.application.dto.response;

import lombok.Data;

@Data
public class HospitalStockResponse {

    private Long id;
    private Long vaccineId;
    private String vaccineName;
    private String vaccineType;
    private String batchNo;
    private Integer availableStock;
    private Integer lockedStock;
    private Integer totalStock;
    private String status;
}
