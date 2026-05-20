package com.tjut.edu.vaccine.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdverseReactionHandleRequest {

    @NotBlank(message = "处理结果不能为空")
    @Size(max = 500, message = "处理结果最多500个字符")
    private String handleResult;
}
