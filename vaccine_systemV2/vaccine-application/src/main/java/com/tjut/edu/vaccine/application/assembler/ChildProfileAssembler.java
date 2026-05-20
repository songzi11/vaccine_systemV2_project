package com.tjut.edu.vaccine.application.assembler;

import com.tjut.edu.vaccine.application.dto.response.ChildResponse;
import com.tjut.edu.vaccine.domain.identity.aggregate.ChildProfile;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ChildProfileAssembler {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static ChildResponse toResponse(ChildProfile child) {
        if (child == null) {
            return null;
        }
        ChildResponse response = new ChildResponse();
        response.setId(child.getId().value());
        response.setParentId(child.getParentId());
        response.setParentIdCard(child.getParentIdCard());
        response.setName(child.getName());
        response.setGender(child.getGender() != null ? child.getGender().getDescription() : null);
        response.setBirthDate(child.getBirthDate());
        response.setIdCardType(child.getIdCardType() != null ? child.getIdCardType().getDescription() : null);
        response.setIdCardNo(child.getIdCardNo());
        response.setNativePlace(child.getNativePlace());
        response.setNation(child.getNation());
        response.setMedicalHistory(child.getMedicalHistory());
        response.setAllergyHistory(child.getAllergyHistory());
        response.setCreateTime(child.getCreateTime() != null ? child.getCreateTime().format(DATE_FORMATTER) : null);
        return response;
    }

    public static List<ChildResponse> toResponseList(List<ChildProfile> children) {
        if (children == null) {
            return List.of();
        }
        return children.stream()
                .map(ChildProfileAssembler::toResponse)
                .collect(Collectors.toList());
    }
}
