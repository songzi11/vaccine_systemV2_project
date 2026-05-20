package com.tjut.edu.vaccine.infrastructure.persistence.converter;

import com.tjut.edu.vaccine.domain.identity.entity.HospitalWindow;
import com.tjut.edu.vaccine.infrastructure.persistence.po.HospitalWindowPO;

public class HospitalWindowConverter {

    public static HospitalWindow toDomain(HospitalWindowPO po) {
        if (po == null) {
            return null;
        }
        HospitalWindow window = new HospitalWindow();
        window.setId(po.getId());
        window.setWindowCode(po.getWindowCode());
        window.setWindowName(po.getWindowName());
        window.setWindowFunctionType(po.getWindowFunctionType());
        window.setStatus(po.getStatus());
        window.setAvgHandleTime(po.getAvgHandleTime());
        window.setSortOrder(po.getSortOrder());
        window.setDoctorId(po.getDoctorId());
        window.setCreateTime(po.getCreateTime());
        window.setUpdateTime(po.getUpdateTime());
        return window;
    }

    public static HospitalWindowPO toPO(HospitalWindow window) {
        if (window == null) {
            return null;
        }
        HospitalWindowPO po = new HospitalWindowPO();
        po.setId(window.getId());
        po.setWindowCode(window.getWindowCode());
        po.setWindowName(window.getWindowName());
        po.setWindowFunctionType(window.getWindowFunctionType());
        po.setStatus(window.getStatus());
        po.setAvgHandleTime(window.getAvgHandleTime());
        po.setSortOrder(window.getSortOrder());
        po.setDoctorId(window.getDoctorId());
        return po;
    }
}
