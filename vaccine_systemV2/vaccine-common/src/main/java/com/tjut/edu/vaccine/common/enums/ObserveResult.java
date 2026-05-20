package com.tjut.edu.vaccine.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ObserveResult {
    NORMAL("NORMAL", "正常"), ABNORMAL("ABNORMAL", "异常");
    private final String code;
    private final String description;

    public static ObserveResult fromCode(String code) {
        for (ObserveResult r : values()) { if (r.code.equals(code)) return r; }
        throw new IllegalArgumentException("Unknown ObserveResult code: " + code);
    }
}
