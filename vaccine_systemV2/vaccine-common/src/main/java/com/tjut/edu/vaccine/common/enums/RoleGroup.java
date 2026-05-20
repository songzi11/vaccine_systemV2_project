package com.tjut.edu.vaccine.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoleGroup {
    USER("USER", "用户"), DOCTOR("DOCTOR", "医生"), ADMIN("ADMIN", "管理员");
    private final String code;
    private final String description;
    public static RoleGroup fromCode(String code) {
        for (RoleGroup g : values()) { if (g.code.equals(code)) return g; }
        throw new IllegalArgumentException("Unknown RoleGroup code: " + code);
    }
}
