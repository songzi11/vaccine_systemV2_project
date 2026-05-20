package com.tjut.edu.vaccine.domain.identity.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色权限关联实体
 */
@Getter
@Setter
public class RolePermission implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long roleId;
    private Long permissionId;
    private LocalDateTime createTime;

    public RolePermission() {
    }

    public RolePermission(Long roleId, Long permissionId) {
        if (roleId == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        if (permissionId == null) {
            throw new IllegalArgumentException("权限ID不能为空");
        }
        this.roleId = roleId;
        this.permissionId = permissionId;
        this.createTime = LocalDateTime.now();
    }
}
