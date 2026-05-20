package com.tjut.edu.vaccine.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Severity {
    MILD("MILD", "轻度"), MODERATE("MODERATE", "中度"), SEVERE("SEVERE", "重度");
    private final String code;
    private final String description;

    public static Severity fromCode(String code) {
        for (Severity s : values()) { if (s.code.equals(code)) return s; }
        throw new IllegalArgumentException("Unknown Severity code: " + code);
    }
}
