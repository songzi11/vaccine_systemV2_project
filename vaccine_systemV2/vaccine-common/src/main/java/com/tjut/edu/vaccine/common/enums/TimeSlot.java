package com.tjut.edu.vaccine.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TimeSlot {
    AM("AM", "08:00-12:00"), PM("PM", "14:00-17:00");
    private final String code;
    private final String description;
    public static TimeSlot fromCode(String code) {
        for (TimeSlot t : values()) { if (t.code.equals(code)) return t; }
        throw new IllegalArgumentException("Unknown TimeSlot code: " + code);
    }
}
