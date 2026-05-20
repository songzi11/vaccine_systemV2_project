package com.tjut.edu.vaccine.domain.identity.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 权限实体
 */
@Getter
@Setter
public class Permission implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String permissionCode;
    private String permissionName;
    private String module;
    private String description;
    private LocalDateTime createTime;

    public Permission() {
        this.createTime = LocalDateTime.now();
    }

    public Permission(String permissionCode, String permissionName, String module, String description) {
        if (permissionCode == null || permissionCode.isBlank()) {
            throw new IllegalArgumentException("权限编码不能为空");
        }
        if (permissionName == null || permissionName.isBlank()) {
            throw new IllegalArgumentException("权限名称不能为空");
        }
        this.permissionCode = permissionCode;
        this.permissionName = permissionName;
        this.module = module;
        this.description = description;
        this.createTime = LocalDateTime.now();
    }
}
