package com.tjut.edu.vaccine.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ScheduleToggleRequest {
    @NotNull(message = "医生ID不能为空")
    private Long doctorId;

    @NotNull(message = "窗口ID不能为空")
    private Long windowId;

    @NotNull(message = "排班日期不能为空")
    private LocalDate scheduleDate;

    @NotBlank(message = "时段不能为空")
    @Pattern(regexp = "^(AM|PM)$", message = "时段必须为AM或PM")
    private String timeSlot;

    /** 状态: 0=恢复默认, 1=请假, 2=取消 */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
