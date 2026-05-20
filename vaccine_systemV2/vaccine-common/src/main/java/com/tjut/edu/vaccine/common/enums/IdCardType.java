package com.tjut.edu.vaccine.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IdCardType {
    ID_CARD(1, "身份证"), PASSPORT(2, "护照"), OTHER(3, "其他");
    private final int code;
    private final String description;
    public static IdCardType fromCode(int code) {
        for (IdCardType t : values()) { if (t.code == code) return t; }
        throw new IllegalArgumentException("Unknown IdCardType code: " + code);
    }
}
