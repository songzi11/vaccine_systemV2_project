package com.tjut.edu.vaccine.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ===== 系统级 (1000-1999) =====
    UNAUTHORIZED(1001, "未认证或Token无效"),
    FORBIDDEN(1002, "无权限访问"),
    BAD_REQUEST(1003, "请求参数错误"),
    NOT_FOUND(1004, "资源不存在"),
    CONFLICT(1005, "数据冲突"),
    TOO_MANY_REQUESTS(1006, "请求频率超限"),
    INTERNAL_ERROR(1007, "系统内部错误"),
    STATUS_TRANSITION_FORBIDDEN(1010, "状态流转不允许"),
    USER_FROZEN(1011, "用户已被冻结"),
    NO_SHOW_FROZEN(1012, "爽约冻结中"),

    // ===== 预约模块 (2000-2999) =====
    APPOINT_CHILD_NOT_FOUND(2001, "儿童档案不存在"),
    APPOINT_CHILD_NOT_OWN(2002, "儿童档案不属于当前用户"),
    APPOINT_VACCINE_OFF_SHELF(2003, "疫苗未上架"),
    APPOINT_DATE_INVALID(2004, "预约日期无效"),
    APPOINT_SLOT_FULL(2005, "预约时段已满"),
    APPOINT_DUPLICATE(2006, "同一儿童同日同疫苗重复预约"),
    APPOINT_CANCEL_FORBIDDEN(2007, "当前状态不允许取消"),
    APPOINT_NOT_OWN(2010, "预约记录不属于当前用户"),
    APPOINT_NOT_FOUND(2008, "预约记录不存在"),
    APPOINT_EXPIRED(2009, "预约已过期"),

    // ===== 流程模块 (3000-3999) =====
    SIGNIN_STATUS_INVALID(3001, "当前状态不允许签到"),
    SIGNIN_IDCARD_MISMATCH(3002, "身份证号不匹配"),
    PRECHECK_STATUS_INVALID(3003, "当前状态不允许预检"),
    PRECHECK_TEMP_HIGH(3004, "体温异常"),
    OBSERVE_STATUS_INVALID(3005, "当前状态不允许留观操作"),
    OBSERVE_TIME_INSUFFICIENT(3006, "留观时间不足30分钟"),
    SIGNIN_DATE_INVALID(3007, "非当日预约不可签到"),
    PRECHECK_APPOINTMENT_EXPIRED(3008, "预约已过期"),
    ADVERSE_NOT_REPORTED(3009, "留观异常但未上报不良反应"),
    CONTRAINDICATION_FAILED(3010, "禁忌筛查不通过"),
    WINDOW_NOT_AVAILABLE(3011, "当前无可用窗口，请稍后再试"),
    WINDOW_NOT_ASSIGNED(3012, "当前医生未分配窗口"),

    // ===== 库存模块 (4000-4999) =====
    STOCK_TRANSFER_SAME_LOCATION(4001, "调拨位置相同"),
    STOCK_TRANSFER_INSUFFICIENT(4002, "调出位置库存不足"),
    STOCK_BATCH_DISPOSED(4003, "批次已销毁"),
    STOCK_DISPOSE_EXCEED(4004, "销毁数量超过总库存"),
    STOCK_DISPOSE_REASON_EMPTY(4005, "销毁原因未填写"),
    STOCK_BATCH_NOT_FOUND(4006, "批次不存在"),
    STOCK_PARAM_INVALID(4007, "库存模块参数错误"),
    STOCK_TRANSFER_FAILED(4009, "调拨执行失败"),
    STOCK_CONCURRENCY_CONFLICT(4011, "库存并发冲突"),

    // ===== 接种模块 (6000-6999) =====
    VACCINATE_STATUS_INVALID(6001, "当前状态不允许接种"),
    VACCINATE_STOCK_INSUFFICIENT(6002, "批次库存不足"),
    VACCINATE_BATCH_EXPIRED(6003, "批次已过期"),
    VACCINATE_SITE_EMPTY(6004, "接种部位未选择"),
    VACCINATE_INJECTION_ID_FAIL(6005, "注射号生成失败"),
    VACCINATE_DEDUCT_FAILED(6006, "库存扣减失败"),
    VACCINATE_RECORD_SAVE_FAILED(6007, "接种记录保存失败"),
    VACCINATE_STATUS_UPDATE_FAILED(6008, "预约状态更新失败"),
    VACCINATE_RECORD_NOT_FOUND(6009, "接种记录不存在"),
    VACCINATE_NO_PERMISSION(6010, "无权查看该接种记录"),

    // ===== 用户模块 (7000-7999) =====
    USER_PHONE_DUPLICATE(7001, "手机号已注册"),
    USER_LOGIN_FAILED(7002, "用户名或密码错误"),
    USER_NOT_FOUND(7003, "用户不存在"),
    USER_ALREADY_FROZEN(7004, "用户已处于冻结状态"),
    CHILD_INFO_INCOMPLETE(7005, "儿童档案信息不完整"),
    CHILD_NOT_FOUND(7006, "儿童档案不存在"),
    CHILD_NOT_OWN(7014, "儿童档案不属于当前用户"),
    SMS_CODE_INVALID(7007, "验证码无效或已过期"),
    LOGIN_LOCKED(7008, "登录失败次数过多，账号已锁定"),
    USER_FROZEN_LOGIN(7009, "用户已冻结，无法登录"),
    OLD_PASSWORD_ERROR(7010, "旧密码错误"),
    CHILD_COUNT_EXCEEDED(7011, "儿童档案数量已达上限"),
    CHILD_HAS_APPOINTMENT(7012, "该儿童存在未完成的预约"),
    CANNOT_MODIFY_SYSADMIN(7013, "不能修改系统管理员"),
    ONLY_DEACTIVATE_SUSPENDED(7014, "只能注销已停用的用户"),

    // ===== 管理模块 (8000-8999) =====
    SCHEDULE_CONFLICT(8001, "排班时间冲突"),
    SCHEDULE_NOT_FOUND(8002, "排班记录不存在"),
    WINDOW_CODE_DUPLICATE(8003, "窗口编码已存在"),
    WINDOW_IN_USE(8004, "窗口存在关联数据"),
    NOTICE_NOT_FOUND(8005, "公告不存在"),
    VACCINE_NOT_FOUND(8006, "疫苗不存在"),
    ROLE_ASSIGN_FAILED(8007, "角色分配失败"),
    ROLE_CODE_DUPLICATE(8008, "角色编码已存在"),
    ROLE_NOT_FOUND(8009, "角色不存在"),
    ROLE_SYSTEM_PROTECTED(8010, "系统内置角色不可删除"),
    ROLE_IN_USE(8011, "角色已绑定用户"),
    PERMISSION_NOT_FOUND(8012, "权限不存在"),
    CONFIG_KEY_NOT_FOUND(8013, "配置项不存在"),
    CONFIG_VALUE_INVALID(8014, "配置值格式不正确"),
    VERIFY_CODE_INVALID(8015, "注册验证码无效"),
    VERIFY_CODE_USED(8016, "注册验证码已使用"),
    VERIFY_CODE_REVOKED(8017, "注册验证码已撤销");

    private final int code;
    private final String message;

    public static ErrorCode fromCode(int code) {
        for (ErrorCode e : values()) { if (e.code == code) return e; }
        throw new IllegalArgumentException("Unknown ErrorCode code: " + code);
    }
}
