package com.tjut.edu.vaccine.application.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FEFOBatchResponse {

    private Long batchId;
    private String batchNo;
    private String manufacturer;
    private LocalDate productionDate;
    private LocalDate expiryDate;
    private String status;
    private Integer availableStock;
}
