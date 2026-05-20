package com.tjut.edu.vaccine.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WindowCreateRequest {

    @NotBlank(message = "窗口编码不能为空")
    @Size(max = 30, message = "窗口编码最多30个字符")
    private String windowCode;

    @NotBlank(message = "窗口名称不能为空")
    @Size(max = 50, message = "窗口名称最多50个字符")
    private String windowName;

    @NotBlank(message = "窗口职能类型不能为空")
    private String windowFunctionType;

    private Integer avgHandleTime;

    private Integer sortOrder;
}
