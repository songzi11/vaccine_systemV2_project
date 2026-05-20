package com.tjut.edu.vaccine.domain.identity.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 窗口服务配置实体
 */
@Getter
@Setter
public class WindowServiceConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String windowCode;
    private String businessName;
    private String businessDesc;
    private String businessDetail;
    private Integer estimatedTime;
    private String tips;
    private String requiredItems;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public WindowServiceConfig() {
    }

    public WindowServiceConfig(String windowCode, String businessName) {
        if (windowCode == null || windowCode.isBlank()) {
            throw new IllegalArgumentException("窗口编码不能为空");
        }
        if (businessName == null || businessName.isBlank()) {
            throw new IllegalArgumentException("业务名称不能为空");
        }
        this.windowCode = windowCode;
        this.businessName = businessName;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    public void update(String businessName, String businessDesc, String businessDetail,
                       Integer estimatedTime, String tips, String requiredItems) {
        if (businessName != null && !businessName.isBlank()) {
            this.businessName = businessName;
        }
        this.businessDesc = businessDesc;
        this.businessDetail = businessDetail;
        if (estimatedTime != null) {
            this.estimatedTime = estimatedTime;
        }
        this.tips = tips;
        this.requiredItems = requiredItems;
        this.updateTime = LocalDateTime.now();
    }
}
