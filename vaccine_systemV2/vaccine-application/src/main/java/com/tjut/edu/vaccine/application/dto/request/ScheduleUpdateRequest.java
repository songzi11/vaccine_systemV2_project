package com.tjut.edu.vaccine.application.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ScheduleUpdateRequest {

    private Integer status;

    private Integer maxCapacity;

    @Pattern(regexp = "^(AM|PM)$", message = "时段必须为AM或PM")
    private String timeSlot;
}
