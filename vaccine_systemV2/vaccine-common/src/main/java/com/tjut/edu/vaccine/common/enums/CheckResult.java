package com.tjut.edu.vaccine.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CheckResult {
    PASS("PASS", "通过"), FAIL("FAIL", "不通过");
    private final String code;
    private final String description;

    public static CheckResult fromCode(String code) {
        for (CheckResult r : values()) { if (r.code.equals(code)) return r; }
        throw new IllegalArgumentException("Unknown CheckResult code: " + code);
    }
}
