package com.tjut.edu.vaccine.application.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StockTransferResponse {

    private Long id;
    private String transferNo;
    private Long batchId;
    private String batchNo;
    private Integer fromType;
    private Long fromId;
    private String fromLocationName;
    private Integer toType;
    private Long toId;
    private String toLocationName;
    private Integer quantity;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime transferTime;
    private String remark;
    private LocalDateTime createTime;
}
