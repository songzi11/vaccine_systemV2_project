package com.tjut.edu.vaccine.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {
    NORMAL(0, "正常"), DISABLED(1, "已禁用"), CANCELLED(2, "已注销"), FROZEN(3, "已冻结");
    private final int code;
    private final String description;
    public static UserStatus fromCode(int code) {
        for (UserStatus s : values()) { if (s.code == code) return s; }
        throw new IllegalArgumentException("Unknown UserStatus code: " + code);
    }
}
