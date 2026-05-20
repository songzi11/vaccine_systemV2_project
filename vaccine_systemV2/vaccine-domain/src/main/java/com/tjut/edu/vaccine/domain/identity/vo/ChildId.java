package com.tjut.edu.vaccine.domain.identity.vo;

import java.io.Serializable;

/**
 * 儿童ID值对象
 */
public record ChildId(Long value) implements Serializable {

    public ChildId {
        if (value == null) {
            throw new IllegalArgumentException("ChildId value must not be null");
        }
    }
}
