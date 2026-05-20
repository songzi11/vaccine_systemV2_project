package com.tjut.edu.vaccine.application.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WindowUpdateRequest {

    @Size(max = 50, message = "窗口名称最多50个字符")
    private String windowName;

    @Size(max = 30, message = "窗口职能类型最多30个字符")
    private String windowFunctionType;

    private Integer avgHandleTime;

    private Integer sortOrder;

    private Integer status;
}
