package com.tjut.edu.vaccine.domain.identity.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 权限审计实体
 */
@Getter
@Setter
public class PermissionAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    /**
     * 目标类型: USER/ROLE/PERMISSION
     */
    private String targetType;
    private Long targetId;
    private String action;
    private String detail;
    private LocalDateTime createTime;

    public PermissionAudit() {
    }

    public PermissionAudit(Long userId, String targetType, Long targetId, String action, String detail) {
        if (userId == null) {
            throw new IllegalArgumentException("操作用户ID不能为空");
        }
        if (targetType == null || targetType.isBlank()) {
            throw new IllegalArgumentException("目标类型不能为空");
        }
        if (targetId == null) {
            throw new IllegalArgumentException("目标ID不能为空");
        }
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("操作类型不能为空");
        }
        this.userId = userId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.action = action;
        this.detail = detail;
        this.createTime = LocalDateTime.now();
    }
}
