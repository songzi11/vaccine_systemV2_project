package com.tjut.edu.vaccine.common.constants;

import java.util.List;

public final class ApiRoleMapping {

    private ApiRoleMapping() {}

    public static final String PUBLIC_PREFIX = "/api/v1/public";
    public static final String USER_PREFIX = "/api/v1/user";
    public static final String SIGNIN_PREFIX = "/api/v1/signin";
    public static final String PRECHECK_PREFIX = "/api/v1/precheck";
    public static final String VACCINATE_PREFIX = "/api/v1/vaccinate";
    public static final String OBSERVE_PREFIX = "/api/v1/observe";
    public static final String STOCK_PREFIX = "/api/v1/stock";
    public static final String SCHEDULE_PREFIX = "/api/v1/schedule";
    public static final String BUSINESS_PREFIX = "/api/v1/business";
    public static final String ADMIN_PREFIX = "/api/v1/admin";
    public static final String ADMIN_WINDOWS_PREFIX = "/api/v1/admin/windows";
    public static final String ADMIN_VACCINES_PREFIX = "/api/v1/admin/vaccines";
    public static final String ADMIN_NOTICES_PREFIX = "/api/v1/admin/notices";
    public static final String ADMIN_STATS_PREFIX = "/api/v1/admin/stats";

    public static List<String> getAllowedRoles(String pathPrefix) {
        return switch (pathPrefix) {
            case PUBLIC_PREFIX -> List.of();
            case USER_PREFIX -> List.of("USER");
            case SIGNIN_PREFIX -> List.of("DOCTOR_SIGNIN", "DOCTOR_PRECHECK");
            case PRECHECK_PREFIX -> List.of("DOCTOR_PRECHECK");
            case VACCINATE_PREFIX -> List.of("DOCTOR_VACCINATE");
            case OBSERVE_PREFIX -> List.of("DOCTOR_OBSERVE");
            case STOCK_PREFIX -> List.of("DOCTOR_STOCK");
            case SCHEDULE_PREFIX -> List.of("DOCTOR_BUSINESS_ADMIN");
            case BUSINESS_PREFIX -> List.of("DOCTOR_BUSINESS_ADMIN");
            case ADMIN_WINDOWS_PREFIX, ADMIN_VACCINES_PREFIX, ADMIN_NOTICES_PREFIX, ADMIN_STATS_PREFIX ->
                    List.of("SUPER_ADMIN", "DOCTOR_BUSINESS_ADMIN");
            case ADMIN_PREFIX -> List.of("SUPER_ADMIN");
            default -> List.of();
        };
    }

    public static boolean requiresAuth(String pathPrefix) {
        return !PUBLIC_PREFIX.equals(pathPrefix);
    }
}
