package com.tjut.edu.vaccine.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ObserveStartRequest {

    @NotNull(message = "预约ID不能为空")
    private Long appointmentId;

    @NotBlank(message = "接种编号不能为空")
    private String injectionId;
}
