package com.tjut.edu.vaccine.common.enums;

import lombok.Getter;

@Getter
public enum NoticeStatus {

    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    OFFLINE(2, "已下线"),
    REJECTED(3, "已拒绝");

    private final int code;
    private final String description;

    NoticeStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }
}
