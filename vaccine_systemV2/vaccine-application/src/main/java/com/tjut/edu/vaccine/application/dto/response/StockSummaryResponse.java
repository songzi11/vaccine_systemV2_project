package com.tjut.edu.vaccine.application.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class StockSummaryResponse {

    private int totalVaccines;
    private int alertCount;
    private int expiredCount;
    private int todayTransfers;
    private List<HospitalStockResponse> vaccines;
}
