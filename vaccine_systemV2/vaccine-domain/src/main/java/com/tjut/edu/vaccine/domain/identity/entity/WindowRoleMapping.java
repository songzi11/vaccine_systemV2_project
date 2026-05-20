package com.tjut.edu.vaccine.domain.identity.entity;

import java.util.Map;
import java.util.Set;

/**
 * 窗口功能类型与医生角色的映射规则（领域知识）
 */
public final class WindowRoleMapping {

    private WindowRoleMapping() {}

    /** 所有 DOCTOR_* 角色编码（窗口分配用，不含库管） */
    public static final Set<String> DOCTOR_ROLE_CODES = Set.of(
            "DOCTOR_SIGNIN",
            "DOCTOR_PRECHECK",
            "DOCTOR_REGISTER",
            "DOCTOR_VACCINATE",
            "DOCTOR_OBSERVE"
    );

    /** 窗口功能类型 → 对应医生角色编码 */
    public static final Map<String, String> WINDOW_TYPE_TO_ROLE = Map.of(
            "SIGNIN", "DOCTOR_SIGNIN",
            "PRECHECK", "DOCTOR_PRECHECK",
            "REGISTER", "DOCTOR_REGISTER",
            "VACCINATE", "DOCTOR_VACCINATE",
            "OBSERVE", "DOCTOR_OBSERVE"
    );
}
