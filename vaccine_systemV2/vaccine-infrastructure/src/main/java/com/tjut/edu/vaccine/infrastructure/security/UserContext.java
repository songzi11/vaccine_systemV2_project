package com.tjut.edu.vaccine.infrastructure.security;

import java.util.List;

public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<List<String>> ROLES = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static void setRoles(List<String> roles) {
        ROLES.set(roles);
    }

    public static List<String> getRoles() {
        return ROLES.get();
    }

    public static void clear() {
        USER_ID.remove();
        ROLES.remove();
    }
}
