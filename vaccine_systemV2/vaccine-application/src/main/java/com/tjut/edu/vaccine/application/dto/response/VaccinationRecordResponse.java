package com.tjut.edu.vaccine.application.dto.response;

import lombok.Data;

@Data
public class VaccinationRecordResponse {

    private Long id;
    private Long appointmentId;
    private String injectionId;
    private String injectionTime;
    private Long doctorId;
    private String doctorName;
    private String injectionSite;
    private Long batchId;
    private String batchNo;
    private String childName;
    private Integer childGender;
    private String childBirthDate;
    private Long childId;
    private String vaccineName;
    private String createTime;
}
