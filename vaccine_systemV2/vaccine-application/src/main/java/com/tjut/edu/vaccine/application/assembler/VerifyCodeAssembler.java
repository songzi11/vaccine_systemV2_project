package com.tjut.edu.vaccine.application.assembler;

import com.tjut.edu.vaccine.application.dto.response.VerifyCodeResponse;
import com.tjut.edu.vaccine.domain.identity.entity.VerifyCode;

public class VerifyCodeAssembler {

    public static VerifyCodeResponse toResponse(VerifyCode vc) {
        VerifyCodeResponse resp = new VerifyCodeResponse();
        resp.setId(vc.getId());
        resp.setCode(vc.getCode());
        resp.setStatusCode(vc.getStatus());
        resp.setStatusText(switch (vc.getStatus()) {
            case VerifyCode.STATUS_UNUSED -> "未使用";
            case VerifyCode.STATUS_USED -> "已使用";
            case VerifyCode.STATUS_REVOKED -> "已撤销";
            default -> "未知";
        });
        resp.setCreatedBy(vc.getCreatedBy());
        resp.setUsedBy(vc.getUsedBy());
        resp.setUsedAt(vc.getUsedAt());
        resp.setCreateTime(vc.getCreateTime());
        return resp;
    }
}
