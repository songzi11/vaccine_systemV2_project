package com.tjut.edu.vaccine.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdverseReactionRequest {

    private Long observeRecordId;

    @NotNull(message = "预约ID不能为空")
    private Long appointmentId;

    @NotBlank(message = "反应类型不能为空")
    @Size(max = 100, message = "反应类型最多100个字符")
    private String reactionType;

    @Size(max = 500, message = "描述最多500个字符")
    private String description;

    @NotBlank(message = "严重程度不能为空")
    @Pattern(regexp = "^(MILD|MODERATE|SEVERE)$", message = "严重程度必须为MILD、MODERATE或SEVERE")
    private String severity;
}
