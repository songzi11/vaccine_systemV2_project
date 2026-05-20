package com.tjut.edu.vaccine.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VaccinateExecuteRequest {

    @NotNull(message = "预约ID不能为空")
    private Long appointmentId;

    @NotBlank(message = "接种部位不能为空")
    @Pattern(regexp = "^(LEFT_UPPER_ARM|RIGHT_UPPER_ARM|LEFT_BUTTOCK|RIGHT_BUTTOCK)$",
             message = "接种部位无效")
    private String injectionSite;

    /** 批次ID，不传时后端自动通过FEFO策略选择 */
    private Long batchId;
}
