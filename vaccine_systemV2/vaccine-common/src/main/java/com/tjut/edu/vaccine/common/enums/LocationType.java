package com.tjut.edu.vaccine.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LocationType {
    WAREHOUSE(0, "总仓"), VACCINATION_POINT(1, "接种点");
    private final int code;
    private final String description;

    public static LocationType fromCode(int code) {
        for (LocationType l : values()) { if (l.code == code) return l; }
        throw new IllegalArgumentException("Unknown LocationType code: " + code);
    }
}
