package com.tjut.edu.vaccine.domain.appointment.aggregate;

import com.tjut.edu.vaccine.common.enums.AppointmentStatus;
import com.tjut.edu.vaccine.common.enums.AppointmentStatusMachine;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 预约聚合根
 */
@Getter
@Setter
public class Appointment implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String appointmentNo;
    private Long userId;
    private Long childId;
    private Long vaccineId;
    private LocalDate appointmentDate;
    private String timeSlot;
    private int status;
    private String currentWindow;
    private LocalDateTime signinTime;
    private LocalDateTime cancelTime;
    private String cancelReason;
    private Long batchId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Appointment() {
    }

    private Appointment(Long userId, Long childId, Long vaccineId,
                        LocalDate appointmentDate, String timeSlot, String appointmentNo) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (childId == null) {
            throw new IllegalArgumentException("儿童ID不能为空");
        }
        if (vaccineId == null) {
            throw new IllegalArgumentException("疫苗ID不能为空");
        }
        if (appointmentDate == null) {
            throw new IllegalArgumentException("预约日期不能为空");
        }
        if (timeSlot == null || timeSlot.isBlank()) {
            throw new IllegalArgumentException("时间段不能为空");
        }
        this.userId = userId;
        this.childId = childId;
        this.vaccineId = vaccineId;
        this.appointmentDate = appointmentDate;
        this.timeSlot = timeSlot;
        this.appointmentNo = appointmentNo;
        this.status = AppointmentStatus.APPOINTED.getCode();
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 创建预约工厂方法
     */
    public static Appointment create(Long userId, Long childId, Long vaccineId,
                                     LocalDate date, String timeSlot) {
        String appointmentNo = generateAppointmentNo(date);
        return new Appointment(userId, childId, vaccineId, date, timeSlot, appointmentNo);
    }

    /**
     * 生成预约编号 APT+yyyyMMdd+4位序号
     */
    private static String generateAppointmentNo(LocalDate date) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // 4位序号由仓储层填充，此处使用占位符
        return "APT" + dateStr + "0001";
    }

    /**
     * 设置预约编号（由仓储层调用）
     */
    public void setAppointmentNo(String appointmentNo) {
        this.appointmentNo = appointmentNo;
    }

    /**
     * 取消预约
     */
    public void cancel(String reason) {
        AppointmentStatusMachine.validateTransition(this.status, AppointmentStatus.CANCELLED.getCode());
        this.status = AppointmentStatus.CANCELLED.getCode();
        this.cancelTime = LocalDateTime.now();
        this.cancelReason = reason;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 判断是否可取消（已预约或已签到状态可取消）
     */
    public boolean isCancellable() {
        return this.status == AppointmentStatus.APPOINTED.getCode()
            || this.status == AppointmentStatus.SIGNED_IN.getCode();
    }

    /**
     * 状态流转 — 委托给 AppointmentStatusMachine 校验
     */
    public void transitionStatus(int target) {
        AppointmentStatusMachine.validateTransition(this.status, target);
        this.status = target;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 签到
     */
    public void signin(String window) {
        AppointmentStatusMachine.validateTransition(this.status, AppointmentStatus.SIGNED_IN.getCode());
        this.status = AppointmentStatus.SIGNED_IN.getCode();
        this.currentWindow = window;
        this.signinTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 设置接种批次号
     */
    public void assignBatch(Long batchId) {
        this.batchId = batchId;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 分配到指定窗口（用于流程各阶段自动分配）
     */
    public void assignToWindow(String windowCode) {
        this.currentWindow = windowCode;
        this.updateTime = LocalDateTime.now();
    }

    public boolean isAppointed() {
        return this.status == AppointmentStatus.APPOINTED.getCode();
    }

    public boolean isCompleted() {
        return this.status == AppointmentStatus.COMPLETED.getCode();
    }

    public boolean isTerminal() {
        return AppointmentStatusMachine.isTerminal(this.status);
    }
}
