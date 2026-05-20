package com.tjut.edu.vaccine.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AppointmentStatus {

    APPOINTED(1, "已预约", false),
    COMPLETED(2, "已完成", true),
    CANCELLED(3, "已取消", true),
    EXPIRED(4, "已过期", true),
    SIGNED_IN(6, "已签到", false),
    PRECHECK_PASS(7, "预检通过", false),
    PRECHECK_FAIL(9, "预检失败", true),
    OBSERVING(10, "留观中", false);

    private final int code;
    private final String description;
    private final boolean terminal;

    public static AppointmentStatus fromCode(int code) {
        for (AppointmentStatus s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("Unknown AppointmentStatus code: " + code);
    }
}
