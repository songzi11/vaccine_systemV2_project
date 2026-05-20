package com.tjut.edu.vaccine.infrastructure.persistence.converter;

import com.tjut.edu.vaccine.domain.identity.entity.WindowServiceConfig;
import com.tjut.edu.vaccine.infrastructure.persistence.po.WindowServiceConfigPO;

public class WindowServiceConfigConverter {

    public static WindowServiceConfig toDomain(WindowServiceConfigPO po) {
        if (po == null) { return null; }
        WindowServiceConfig config = new WindowServiceConfig();
        config.setId(po.getId());
        config.setWindowCode(po.getWindowCode());
        config.setBusinessName(po.getBusinessName());
        config.setBusinessDesc(po.getBusinessDesc());
        config.setBusinessDetail(po.getBusinessDetail());
        config.setEstimatedTime(po.getEstimatedTime());
        config.setTips(po.getTips());
        config.setRequiredItems(po.getRequiredItems());
        config.setCreateTime(po.getCreateTime());
        config.setUpdateTime(po.getUpdateTime());
        return config;
    }

    public static WindowServiceConfigPO toPO(WindowServiceConfig config) {
        if (config == null) { return null; }
        WindowServiceConfigPO po = new WindowServiceConfigPO();
        po.setId(config.getId());
        po.setWindowCode(config.getWindowCode());
        po.setBusinessName(config.getBusinessName());
        po.setBusinessDesc(config.getBusinessDesc());
        po.setBusinessDetail(config.getBusinessDetail());
        po.setEstimatedTime(config.getEstimatedTime());
        po.setTips(config.getTips());
        po.setRequiredItems(config.getRequiredItems());
        return po;
    }
}
