package com.tjut.edu.vaccine.application.dto.response;

import lombok.Data;

@Data
public class VaccinePublicResponse {

    private Long id;
    private String vaccineName;
    private String vaccineCode;
    /** 疫苗分类（CLASS_I / CLASS_II） */
    private String category;
    private String manufacturer;
    private String specification;
    private String description;
    private Integer isOnShelf;
    private Integer doses;
    private Integer intervalDays;
    private Integer minAgeMonth;
    private Integer maxAgeMonth;
    private java.math.BigDecimal price;
}
