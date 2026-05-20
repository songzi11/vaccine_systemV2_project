package com.tjut.edu.vaccine.infrastructure.persistence.converter;

import com.tjut.edu.vaccine.domain.identity.entity.VerifyCode;
import com.tjut.edu.vaccine.infrastructure.persistence.po.VerifyCodePO;

public class VerifyCodeConverter {

    public static VerifyCode toDomain(VerifyCodePO po) {
        if (po == null) return null;
        VerifyCode vc = new VerifyCode();
        vc.setId(po.getId());
        vc.setCode(po.getCode());
        vc.setStatus(po.getStatus());
        vc.setCreatedBy(po.getCreatedBy());
        vc.setUsedBy(po.getUsedBy());
        vc.setUsedAt(po.getUsedAt());
        vc.setCreateTime(po.getCreateTime());
        vc.setUpdateTime(po.getUpdateTime());
        return vc;
    }

    public static VerifyCodePO toPO(VerifyCode vc) {
        if (vc == null) return null;
        VerifyCodePO po = new VerifyCodePO();
        if (vc.getId() != null) po.setId(vc.getId());
        po.setCode(vc.getCode());
        po.setStatus(vc.getStatus());
        po.setCreatedBy(vc.getCreatedBy());
        po.setUsedBy(vc.getUsedBy());
        po.setUsedAt(vc.getUsedAt());
        po.setCreateTime(vc.getCreateTime());
        po.setUpdateTime(vc.getUpdateTime());
        return po;
    }
}
