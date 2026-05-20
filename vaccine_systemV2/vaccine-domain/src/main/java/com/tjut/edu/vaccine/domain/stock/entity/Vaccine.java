package com.tjut.edu.vaccine.domain.stock.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 疫苗实体
 */
@Getter
@Setter
public class Vaccine implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 疫苗类型常量
     */
    public static final String TYPE_CLASS_I = "CLASS_I";
    public static final String TYPE_CLASS_II = "CLASS_II";

    private Long id;
    private String vaccineName;
    private String vaccineType;
    private String manufacturer;
    private String description;
    /**
     * 是否上架: 0=下架, 1=上架
     */
    private int isOnShelf;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Vaccine() {
    }

    public Vaccine(String vaccineName, String vaccineType, String manufacturer, String description) {
        if (vaccineName == null || vaccineName.isBlank()) {
            throw new IllegalArgumentException("疫苗名称不能为空");
        }
        if (vaccineType == null || vaccineType.isBlank()) {
            throw new IllegalArgumentException("疫苗类型不能为空");
        }
        this.vaccineName = vaccineName;
        this.vaccineType = vaccineType;
        this.manufacturer = manufacturer;
        this.description = description;
        this.isOnShelf = 1;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    public void update(String vaccineName, String vaccineType, String manufacturer, String description) {
        if (vaccineName != null && !vaccineName.isBlank()) {
            this.vaccineName = vaccineName;
        }
        if (vaccineType != null && !vaccineType.isBlank()) {
            this.vaccineType = vaccineType;
        }
        this.manufacturer = manufacturer;
        this.description = description;
        this.updateTime = LocalDateTime.now();
    }

    public void onShelf() {
        this.isOnShelf = 1;
        this.updateTime = LocalDateTime.now();
    }

    public void offShelf() {
        this.isOnShelf = 0;
        this.updateTime = LocalDateTime.now();
    }

    public boolean isOnShelf() {
        return this.isOnShelf == 1;
    }
}
