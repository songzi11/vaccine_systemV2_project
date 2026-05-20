package com.tjut.edu.vaccine.domain.identity.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
public class VerifyCode implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String code;
    private int status;
    private Long createdBy;
    private Long usedBy;
    private LocalDateTime usedAt;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static final int STATUS_UNUSED = 0;
    public static final int STATUS_USED = 1;
    public static final int STATUS_REVOKED = 2;

    public boolean isUsable() {
        return this.status == STATUS_UNUSED;
    }

    public void markUsed(Long userId) {
        this.status = STATUS_USED;
        this.usedBy = userId;
        this.usedAt = LocalDateTime.now();
    }

    public void revoke() {
        this.status = STATUS_REVOKED;
    }
}
