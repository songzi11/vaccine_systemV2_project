package com.tjut.edu.vaccine.domain.identity.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户角色关联实体
 */
@Getter
@Setter
public class UserRole implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private Long roleId;
    private LocalDateTime createTime;

    public UserRole() {
    }

    public UserRole(Long userId, Long roleId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (roleId == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        this.userId = userId;
        this.roleId = roleId;
        this.createTime = LocalDateTime.now();
    }
}
