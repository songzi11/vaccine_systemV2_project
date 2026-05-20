package com.tjut.edu.vaccine.common.constants;

public final class PermissionCode {

    private PermissionCode() {}

    // 预约
    public static final String APPOINTMENT_BOOK = "appointment.book";
    public static final String APPOINTMENT_CANCEL_OWN = "appointment.cancel.own";
    public static final String APPOINTMENT_VIEW_OWN = "appointment.view.own";
    // 流程
    public static final String APPOINTMENT_SIGNIN = "appointment.signin";
    public static final String APPOINTMENT_CONFIRM = "appointment.confirm";
    public static final String APPOINTMENT_VIEW_TODAY = "appointment.view.today";
    public static final String APPOINTMENT_VIEW_QUEUE = "appointment.view.queue";
    public static final String APPOINTMENT_VIEW_VACCINATE = "appointment.view.vaccinate";
    public static final String APPOINTMENT_VIEW_OBSERVE = "appointment.view.observe";
    // 预检
    public static final String PRECHECK_ASSESS = "precheck.assess";
    public static final String PRECHECK_CONTRAINDICATION = "precheck.contraindication";
    public static final String PRECHECK_RESULT_VIEW = "precheck.result.view";
    // 用户
    public static final String CHILD_VIEW_OWN = "child.view.own";
    public static final String CHILD_ADD_OWN = "child.add.own";
    public static final String CHILD_EDIT_OWN = "child.edit.own";
    public static final String CHILD_DELETE_OWN = "child.delete.own";
    // 接种
    public static final String VACCINATE_EXECUTE = "vaccinate.execute";
    public static final String VACCINATE_RECORD = "vaccinate.record";
    public static final String VACCINATE_VERIFY = "vaccinate.verify";
    public static final String VACCINATE_SITE_SELECT = "vaccinate.site.select";
    public static final String VACCINATE_ID_GENERATE = "vaccinate.id.generate";
    public static final String VACCINATE_VIEW = "vaccinate.view";
    public static final String RECORD_VIEW_OWN = "record.view.own";
    public static final String RECORD_VIEW_CHILD = "record.view.child";
    // 库存
    public static final String STOCK_VIEW = "stock.view";
    public static final String STOCK_TRANSFER = "stock.transfer";
    public static final String STOCK_DISPOSAL = "stock.disposal";
    public static final String STOCK_LOCK = "stock.lock";
    public static final String STOCK_DEDUCT = "stock.deduct";
    public static final String BATCH_MANAGE = "batch.manage";
    public static final String BATCH_VIEW = "batch.view";
    public static final String STOCK_TRANSFER_CREATE = "stock.transfer.create";
    public static final String STOCK_TRANSFER_CONFIRM = "stock.transfer.confirm";
    public static final String STOCK_TRANSFER_VIEW = "stock.transfer.view";
    public static final String STOCK_ALERT_VIEW = "stock.alert.view";
    // 留观
    public static final String OBSERVE_MANAGE = "observe.manage";
    public static final String OBSERVE_FINISH = "observe.finish";
    public static final String ADVERSE_REPORT = "adverse.report";
    public static final String ADVERSE_HANDLE = "adverse.handle";
    // 排班
    public static final String DOCTOR_SCHEDULE_VIEW = "doctor.schedule.view";
    public static final String DOCTOR_SCHEDULE_CREATE = "doctor.schedule.create";
    public static final String DOCTOR_SCHEDULE_EDIT = "doctor.schedule.edit";
    public static final String DOCTOR_SCHEDULE_DELETE = "doctor.schedule.delete";
    // 管理
    public static final String DOCTOR_ASSIGN_ROLE = "doctor.assign.role";
    public static final String DOCTOR_ASSIGN_PERMISSION = "doctor.assign.permission";
    public static final String WINDOW_MANAGE = "window.manage";
    public static final String WINDOW_SERVICE_MANAGE = "window.service.manage";
    public static final String USER_MANAGE = "user.manage";
    public static final String NOTICE_MANAGE = "notice.manage";
    public static final String NOTICE_AUDIT = "notice.audit";
    // 通用
    public static final String NOTICE_VIEW = "notice.view";
    public static final String NOTICE_FEEDBACK = "notice.feedback";
    public static final String VACCINE_CATALOG_VIEW = "vaccine.catalog.view";
    public static final String STATS_VIEW = "stats.view";
    public static final String ALL_DATA_VIEW = "all.data.view";
}
