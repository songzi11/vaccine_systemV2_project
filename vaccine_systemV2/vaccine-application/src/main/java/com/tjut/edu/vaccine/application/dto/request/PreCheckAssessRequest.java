package com.tjut.edu.vaccine.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PreCheckAssessRequest {

    @NotNull(message = "预约ID不能为空")
    private Long appointmentId;

    @NotNull(message = "体温不能为空")
    private BigDecimal bodyTemperature;

    private BigDecimal weight;

    private BigDecimal height;

    @Size(max = 200, message = "健康状态最多200个字符")
    private String healthStatus;

    @Size(max = 500, message = "过敏史最多500个字符")
    private String allergyHistory;

    @Size(max = 500, message = "近期用药最多500个字符")
    private String medicationRecent;

    @Size(max = 500, message = "疾病史最多500个字符")
    private String diseaseHistory;

    @Size(max = 500, message = "近期接种史最多500个字符")
    private String vaccinationRecent;

    @NotBlank(message = "预检结果不能为空")
    private String result;

    @Size(max = 500, message = "失败原因最多500个字符")
    private String failReason;
}
