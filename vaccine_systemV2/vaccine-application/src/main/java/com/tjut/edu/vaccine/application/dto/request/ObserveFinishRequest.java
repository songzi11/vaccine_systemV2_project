package com.tjut.edu.vaccine.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ObserveFinishRequest {

    @NotNull(message = "留观时长不能为空")
    @Min(value = 1, message = "留观时长必须大于0")
    private Integer durationMinutes;
}
