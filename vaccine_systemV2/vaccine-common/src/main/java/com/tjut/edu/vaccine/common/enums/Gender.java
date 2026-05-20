package com.tjut.edu.vaccine.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Gender {
    UNKNOWN(0, "未知"), MALE(1, "男"), FEMALE(2, "女");
    private final int code;
    private final String description;
    public static Gender fromCode(int code) {
        for (Gender g : values()) { if (g.code == code) return g; }
        throw new IllegalArgumentException("Unknown Gender code: " + code);
    }
}
