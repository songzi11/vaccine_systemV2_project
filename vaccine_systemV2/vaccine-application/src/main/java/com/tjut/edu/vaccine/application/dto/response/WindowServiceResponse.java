package com.tjut.edu.vaccine.application.dto.response;

import lombok.Data;

@Data
public class WindowServiceResponse {

    private Long id;
    private String windowCode;
    private String businessName;
    private String businessDesc;
    private String businessDetail;
    private Integer estimatedTime;
    private String tips;
    private String requiredItems;
    private String createTime;
}
