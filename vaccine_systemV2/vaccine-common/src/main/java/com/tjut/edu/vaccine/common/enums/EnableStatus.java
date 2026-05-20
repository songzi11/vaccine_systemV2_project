package com.tjut.edu.vaccine.common.enums;

import lombok.Getter;

@Getter
public enum EnableStatus {

    ENABLED(0, "启用"),
    DISABLED(1, "禁用");

    private final int code;
    private final String description;

    EnableStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }
}
