package com.tjut.edu.vaccine.domain.identity.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统配置实体
 */
@Getter
@Setter
public class SysConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String configKey;
    private String configValue;
    private String configDesc;
    private String valueType;
    private LocalDateTime updateTime;

    public SysConfig() {
    }

    public SysConfig(String configKey, String configValue, String configDesc, String valueType) {
        if (configKey == null || configKey.isBlank()) {
            throw new IllegalArgumentException("配置键不能为空");
        }
        this.configKey = configKey;
        this.configValue = configValue;
        this.configDesc = configDesc;
        this.valueType = valueType;
        this.updateTime = LocalDateTime.now();
    }

    public void updateValue(String value) {
        this.configValue = value;
        this.updateTime = LocalDateTime.now();
    }
}
