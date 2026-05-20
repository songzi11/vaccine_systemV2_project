package com.tjut.edu.vaccine.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AlertType {
    LOW_STOCK("LOW_STOCK", "库存不足"), EXPIRY_SOON("EXPIRY_SOON", "即将过期"), EXPIRED("EXPIRED", "已过期");
    private final String code;
    private final String description;

    public static AlertType fromCode(String code) {
        for (AlertType a : values()) { if (a.code.equals(code)) return a; }
        throw new IllegalArgumentException("Unknown AlertType code: " + code);
    }
}
