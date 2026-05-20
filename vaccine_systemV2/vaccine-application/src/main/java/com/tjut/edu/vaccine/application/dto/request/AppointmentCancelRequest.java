package com.tjut.edu.vaccine.application.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AppointmentCancelRequest {

    @Size(max = 500, message = "取消原因最多500个字符")
    private String reason;
}
