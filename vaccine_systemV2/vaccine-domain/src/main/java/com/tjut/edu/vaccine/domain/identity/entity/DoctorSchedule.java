package com.tjut.edu.vaccine.domain.identity.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 医生排班实体
 */
@Getter
@Setter
public class DoctorSchedule implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long doctorId;
    private Long windowId;
    private LocalDate scheduleDate;
    /**
     * 时间段: AM/PM
     */
    private String timeSlot;
    /**
     * 状态: 0=正常, 1=请假, 2=取消
     */
    private int status;
    private int maxCapacity;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public DoctorSchedule() {
    }

    public DoctorSchedule(Long doctorId, Long windowId, LocalDate scheduleDate,
                          String timeSlot, int maxCapacity) {
        if (doctorId == null) {
            throw new IllegalArgumentException("医生ID不能为空");
        }
        if (scheduleDate == null) {
            throw new IllegalArgumentException("排班日期不能为空");
        }
        if (timeSlot == null || timeSlot.isBlank()) {
            throw new IllegalArgumentException("时间段不能为空");
        }
        this.doctorId = doctorId;
        this.windowId = windowId;
        this.scheduleDate = scheduleDate;
        this.timeSlot = timeSlot;
        this.status = 0;
        this.maxCapacity = maxCapacity;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    public void markLeave() {
        this.status = 1;
        this.updateTime = LocalDateTime.now();
    }

    public void cancel() {
        this.status = 2;
        this.updateTime = LocalDateTime.now();
    }

    public boolean isNormal() {
        return this.status == 0;
    }
}
