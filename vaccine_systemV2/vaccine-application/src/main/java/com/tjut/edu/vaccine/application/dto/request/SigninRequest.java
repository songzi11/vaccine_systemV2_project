package com.tjut.edu.vaccine.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SigninRequest {

    @NotNull(message = "预约ID不能为空")
    private Long appointmentId;

    private String windowCode;
    private String idCard;
}
