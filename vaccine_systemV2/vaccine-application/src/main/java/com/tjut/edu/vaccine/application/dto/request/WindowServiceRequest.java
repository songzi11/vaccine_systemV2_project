package com.tjut.edu.vaccine.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WindowServiceRequest {

    @NotBlank(message = "业务名称不能为空")
    @Size(max = 100, message = "业务名称最多100个字符")
    private String businessName;

    private String businessDesc;

    private String businessDetail;

    private Integer estimatedTime;

    private String tips;

    private String requiredItems;
}
