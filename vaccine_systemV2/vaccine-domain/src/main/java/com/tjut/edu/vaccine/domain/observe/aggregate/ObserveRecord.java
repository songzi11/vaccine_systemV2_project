package com.tjut.edu.vaccine.domain.observe.aggregate;

import com.tjut.edu.vaccine.common.enums.ObserveResult;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 留观记录聚合根
 */
@Getter
@Setter
public class ObserveRecord implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final int MIN_OBSERVE_MINUTES = 30;

    private Long id;
    private Long appointmentId;
    private String injectionId;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
    /**
     * 留观时长(分钟)
     */
    private int duration;
    private ObserveResult observeResult;
    private Long doctorId;
    private LocalDateTime createTime;

    public ObserveRecord() {
    }

    public ObserveRecord(Long appointmentId, String injectionId, Long doctorId) {
        if (appointmentId == null) {
            throw new IllegalArgumentException("预约ID不能为空");
        }
        if (injectionId == null || injectionId.isBlank()) {
            throw new IllegalArgumentException("接种编号不能为空");
        }
        this.appointmentId = appointmentId;
        this.injectionId = injectionId;
        this.doctorId = doctorId;
        this.startTime = LocalDateTime.now();
        this.observeResult = ObserveResult.NORMAL;
        this.createTime = LocalDateTime.now();
    }

    /**
     * 完成留观（服务端自动计算实际时长）
     */
    public void finish() {
        if (this.finishTime != null) {
            throw new IllegalStateException("留观已结束，不可重复完成");
        }
        LocalDateTime now = LocalDateTime.now();
        long actualMinutes = Duration.between(this.startTime, now).toMinutes();
        if (actualMinutes <= 0) {
            actualMinutes = 1;
        }
        if (actualMinutes < MIN_OBSERVE_MINUTES) {
            throw new IllegalStateException("留观时间不足" + MIN_OBSERVE_MINUTES + "分钟，当前仅" + actualMinutes + "分钟");
        }
        this.finishTime = now;
        this.duration = (int) actualMinutes;
    }

    public boolean isFinished() {
        return this.finishTime != null;
    }

    /**
     * 系统自动完成留观（定时任务调用，已确认超过30分钟）
     */
    public void autoFinish() {
        if (this.finishTime != null) {
            return; // 幂等：已完成则跳过
        }
        LocalDateTime now = LocalDateTime.now();
        long actualMinutes = Duration.between(this.startTime, now).toMinutes();
        if (actualMinutes <= 0) {
            actualMinutes = 1;
        }
        this.finishTime = now;
        this.duration = (int) actualMinutes;
    }

    public void markAbnormal() {
        if (isFinished()) {
            throw new IllegalStateException("留观已结束，不可再标记异常");
        }
        if (this.observeResult == ObserveResult.ABNORMAL) {
            throw new IllegalStateException("该记录已标记为异常");
        }
        this.observeResult = ObserveResult.ABNORMAL;
    }
}
