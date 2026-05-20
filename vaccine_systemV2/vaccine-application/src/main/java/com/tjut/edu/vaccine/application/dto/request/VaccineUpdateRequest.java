package com.tjut.edu.vaccine.application.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VaccineUpdateRequest {

    private String vaccineCode;
    private String vaccineName;
    /** 疫苗分类（CLASS_I / CLASS_II） */
    private String category;
    private String manufacturer;
    private String specification;
    private Integer minAge;
    private Integer maxAge;
    private Integer doses;
    private Integer intervalDays;
    private String description;
    private String programDesc;
    private String contraindications;
    private String adverseReactions;
    private BigDecimal price;
}
