package com.tjut.edu.vaccine.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BatchStatus {
    NORMAL(0, "正常"), NEAR_EXPIRY(1, "临期"), EXPIRED(2, "过期"), DISPOSED(3, "已销毁");
    private final int code;
    private final String description;
    public static BatchStatus fromCode(int code) {
        for (BatchStatus s : values()) { if (s.code == code) return s; }
        throw new IllegalArgumentException("Unknown BatchStatus code: " + code);
    }
}
