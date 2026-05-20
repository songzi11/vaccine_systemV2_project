package com.tjut.edu.vaccine.infrastructure.persistence.converter;

import com.tjut.edu.vaccine.common.enums.Gender;
import com.tjut.edu.vaccine.common.enums.IdCardType;
import com.tjut.edu.vaccine.domain.identity.vo.ChildId;
import com.tjut.edu.vaccine.domain.identity.aggregate.ChildProfile;
import com.tjut.edu.vaccine.infrastructure.persistence.po.ChildProfilePO;

public class ChildProfileConverter {

    public static ChildProfile toDomain(ChildProfilePO po) {
        if (po == null) {
            return null;
        }
        ChildProfile child = new ChildProfile();
        child.setId(new ChildId(po.getId()));
        child.setParentId(po.getParentId());
        child.setParentIdCard(po.getParentIdCard());
        child.setName(po.getName());
        child.setGender(po.getGender() != null ? Gender.fromCode(po.getGender()) : null);
        child.setBirthDate(po.getBirthDate());
        child.setIdCardType(po.getIdCardType() != null ? IdCardType.fromCode(po.getIdCardType()) : null);
        child.setIdCardNo(po.getIdCardNo());
        child.setNativePlace(po.getNativePlace());
        child.setNation(po.getNation());
        child.setMedicalHistory(po.getMedicalHistory());
        child.setAllergyHistory(po.getAllergyHistory());
        child.setCreateTime(po.getCreateTime());
        child.setUpdateTime(po.getUpdateTime());
        return child;
    }

    public static ChildProfilePO toPO(ChildProfile child) {
        if (child == null) {
            return null;
        }
        ChildProfilePO po = new ChildProfilePO();
        if (child.getId() != null) {
            po.setId(child.getId().value());
        }
        po.setParentId(child.getParentId());
        po.setParentIdCard(child.getParentIdCard());
        po.setName(child.getName());
        po.setGender(child.getGender() != null ? child.getGender().getCode() : null);
        po.setBirthDate(child.getBirthDate());
        po.setIdCardType(child.getIdCardType() != null ? child.getIdCardType().getCode() : null);
        po.setIdCardNo(child.getIdCardNo());
        po.setNativePlace(child.getNativePlace());
        po.setNation(child.getNation());
        po.setMedicalHistory(child.getMedicalHistory());
        po.setAllergyHistory(child.getAllergyHistory());
        po.setCreateTime(child.getCreateTime());
        po.setUpdateTime(child.getUpdateTime());
        return po;
    }
}
