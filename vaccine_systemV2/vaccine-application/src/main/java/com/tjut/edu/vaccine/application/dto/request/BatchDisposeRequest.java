package com.tjut.edu.vaccine.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BatchDisposeRequest {

    @NotNull(message = "销毁数量不能为空")
    @Positive(message = "销毁数量必须大于0")
    private Integer quantity;

    @NotBlank(message = "销毁原因不能为空")
    @Size(max = 500, message = "销毁原因最多500个字符")
    private String reason;

    @Size(max = 500, message = "备注最多500个字符")
    private String remark;
}
