package com.tjut.edu.vaccine.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InjectionSite {
    LEFT_UPPER_ARM("LEFT_UPPER_ARM", "左上臂"),
    RIGHT_UPPER_ARM("RIGHT_UPPER_ARM", "右上臂"),
    LEFT_BUTTOCK("LEFT_BUTTOCK", "左臀"),
    RIGHT_BUTTOCK("RIGHT_BUTTOCK", "右臀");
    private final String code;
    private final String description;
    public static InjectionSite fromCode(String code) {
        for (InjectionSite s : values()) { if (s.code.equals(code)) return s; }
        throw new IllegalArgumentException("Unknown InjectionSite code: " + code);
    }
}
