package com.tjut.edu.vaccine.common.enums;

import com.tjut.edu.vaccine.common.exception.BusinessException;
import java.util.List;
import java.util.Map;

/**
 * 预约状态机（工具类，非枚举）
 * <p>注：位于 enums 包以便与 AppointmentStatus 保持内聚，
 * 如项目规模扩大建议迁移至独立的 statemachine 包。</p>
 */
public final class AppointmentStatusMachine {

    private AppointmentStatusMachine() {}

    public static final List<Integer> IN_PROGRESS = List.of(
        AppointmentStatus.APPOINTED.getCode(),
        AppointmentStatus.SIGNED_IN.getCode(),
        AppointmentStatus.PRECHECK_PASS.getCode(),
        AppointmentStatus.OBSERVING.getCode()
    );
    public static final List<Integer> NORMAL_TERMINAL = List.of(AppointmentStatus.COMPLETED.getCode());
    public static final List<Integer> ABNORMAL_TERMINAL = List.of(
        AppointmentStatus.CANCELLED.getCode(),
        AppointmentStatus.EXPIRED.getCode(),
        AppointmentStatus.PRECHECK_FAIL.getCode()
    );
    /** 从枚举的 terminal 字段自动派生，单一数据源 */
    public static final List<Integer> ALL_TERMINAL = List.of(
        java.util.Arrays.stream(AppointmentStatus.values())
            .filter(AppointmentStatus::isTerminal)
            .map(AppointmentStatus::getCode)
            .toArray(Integer[]::new)
    );

    private static final Map<Integer, List<Integer>> TRANSITIONS = Map.of(
        AppointmentStatus.APPOINTED.getCode(), List.of(
            AppointmentStatus.CANCELLED.getCode(),
            AppointmentStatus.EXPIRED.getCode(),
            AppointmentStatus.SIGNED_IN.getCode()
        ),
        AppointmentStatus.SIGNED_IN.getCode(), List.of(
            AppointmentStatus.CANCELLED.getCode(),
            AppointmentStatus.PRECHECK_PASS.getCode(),
            AppointmentStatus.PRECHECK_FAIL.getCode()
        ),
        AppointmentStatus.PRECHECK_PASS.getCode(), List.of(AppointmentStatus.OBSERVING.getCode()),
        AppointmentStatus.OBSERVING.getCode(), List.of(AppointmentStatus.COMPLETED.getCode())
    );

    public static boolean isTerminal(int status) {
        return ALL_TERMINAL.contains(status);
    }

    public static boolean isInProgress(int status) {
        return IN_PROGRESS.contains(status);
    }

    public static void validateTransition(int current, int target) {
        List<Integer> allowed = TRANSITIONS.get(current);
        if (allowed == null || !allowed.contains(target)) {
            throw new BusinessException(ErrorCode.STATUS_TRANSITION_FORBIDDEN.getCode(),
                "不允许从状态 " + current + " 转换到 " + target);
        }
    }
}
