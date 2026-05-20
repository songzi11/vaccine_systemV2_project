package com.tjut.edu.vaccine.domain.identity.aggregate;

import com.tjut.edu.vaccine.domain.identity.vo.RoleId;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色聚合根
 */
@Getter
@Setter
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    private RoleId id;
    private String roleCode;
    private String roleName;
    private String roleGroup;
    private String description;
    /**
     * 状态: 0=启用, 1=禁用
     */
    private int status;
    /**
     * 是否系统内置: 0=自定义, 1=系统内置
     */
    private int isSystem;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Role() {
    }

    public Role(String roleCode, String roleName, String roleGroup, String description) {
        if (roleCode == null || roleCode.isBlank()) {
            throw new IllegalArgumentException("角色编码不能为空");
        }
        if (roleName == null || roleName.isBlank()) {
            throw new IllegalArgumentException("角色名称不能为空");
        }
        this.roleCode = roleCode;
        this.roleName = roleName;
        this.roleGroup = roleGroup;
        this.description = description;
        this.status = 0;
        this.isSystem = 0;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    public void update(String roleName, String roleGroup, String description) {
        if (roleName != null && !roleName.isBlank()) {
            this.roleName = roleName;
        }
        this.roleGroup = roleGroup;
        this.description = description;
        this.updateTime = LocalDateTime.now();
    }

    public void enable() {
        this.status = 0;
        this.updateTime = LocalDateTime.now();
    }

    public void disable() {
        this.status = 1;
        this.updateTime = LocalDateTime.now();
    }

    public boolean isEnabled() {
        return this.status == 0;
    }

    public boolean isSystemBuiltIn() {
        return this.isSystem == 1;
    }
}
