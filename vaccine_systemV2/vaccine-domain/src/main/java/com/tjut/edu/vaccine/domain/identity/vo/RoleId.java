package com.tjut.edu.vaccine.domain.identity.vo;

import java.io.Serializable;

/**
 * 角色ID值对象
 */
public record RoleId(Long value) implements Serializable {

    public RoleId {
        if (value == null) {
            throw new IllegalArgumentException("RoleId value must not be null");
        }
    }
}
