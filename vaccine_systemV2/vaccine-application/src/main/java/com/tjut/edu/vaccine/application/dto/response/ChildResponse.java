package com.tjut.edu.vaccine.application.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ChildResponse {

    private Long id;
    private Long parentId;
    private String parentIdCard;
    private String name;
    private String gender;
    private LocalDate birthDate;
    private String idCardType;
    private String idCardNo;
    private String nativePlace;
    private String nation;
    private String medicalHistory;
    private String allergyHistory;
    private String createTime;
}
