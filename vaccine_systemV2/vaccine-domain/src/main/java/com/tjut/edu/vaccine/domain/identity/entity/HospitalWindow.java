package com.tjut.edu.vaccine.domain.identity.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 医院窗口实体
 */
@Getter
@Setter
public class HospitalWindow implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String windowCode;
    private String windowName;
    private String windowFunctionType;
    /**
     * 状态: 0=正常, 1=停用
     */
    private int status;
    /**
     * 平均处理时间(分钟)
     */
    private int avgHandleTime;
    private int sortOrder;
    /** 当前分配的医生ID，null 表示未分配 */
    private Long doctorId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public HospitalWindow() {
    }

    public HospitalWindow(String windowCode, String windowName, String windowFunctionType,
                          int avgHandleTime, int sortOrder) {
        if (windowCode == null || windowCode.isBlank()) {
            throw new IllegalArgumentException("窗口编码不能为空");
        }
        if (windowName == null || windowName.isBlank()) {
            throw new IllegalArgumentException("窗口名称不能为空");
        }
        this.windowCode = windowCode;
        this.windowName = windowName;
        this.windowFunctionType = windowFunctionType;
        this.status = 0;
        this.avgHandleTime = avgHandleTime;
        this.sortOrder = sortOrder;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    public void update(String windowName, String windowFunctionType,
                       int avgHandleTime, int sortOrder) {
        this.windowName = windowName;
        this.windowFunctionType = windowFunctionType;
        this.avgHandleTime = avgHandleTime;
        this.sortOrder = sortOrder;
        this.updateTime = LocalDateTime.now();
    }

    public void enable() {
        this.status = 0;
        this.updateTime = LocalDateTime.now();
    }

    public void disable() {
        this.status = 1;
        this.updateTime = LocalDateTime.now();
    }

    public boolean isEnabled() {
        return this.status == 0;
    }
}
