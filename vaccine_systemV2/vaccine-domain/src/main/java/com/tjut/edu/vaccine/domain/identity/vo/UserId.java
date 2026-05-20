package com.tjut.edu.vaccine.domain.identity.vo;

import java.io.Serializable;

/**
 * 用户ID值对象
 */
public record UserId(Long value) implements Serializable {

    public UserId {
        if (value == null) {
            throw new IllegalArgumentException("UserId value must not be null");
        }
        if (value <= 0) {
            throw new IllegalArgumentException("UserId value must be greater than 0");
        }
    }
}
