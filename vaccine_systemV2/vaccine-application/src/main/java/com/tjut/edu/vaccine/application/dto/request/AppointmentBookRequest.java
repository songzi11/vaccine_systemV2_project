package com.tjut.edu.vaccine.application.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AppointmentBookRequest {

    @NotNull(message = "儿童ID不能为空")
    private Long childId;

    @NotNull(message = "疫苗ID不能为空")
    private Long vaccineId;

    @NotNull(message = "预约日期不能为空")
    @FutureOrPresent(message = "预约日期不能早于今天")
    private LocalDate appointmentDate;

    @NotBlank(message = "时段不能为空")
    @Pattern(
            regexp = "^(AM|PM|08:00-09:00|09:00-10:00|10:00-11:00|11:00-12:00|14:00-15:00|15:00-16:00|16:00-17:00)$",
            message = "时段必须为有效预约时间段"
    )
    private String timeSlot;
}
