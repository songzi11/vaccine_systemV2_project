package com.tjut.edu.vaccine.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class StockTransferRequest {

    @NotNull(message = "批次ID不能为空")
    private Long batchId;

    @NotNull(message = "来源类型不能为空")
    private Integer fromType;

    @NotNull(message = "来源ID不能为空")
    private Long fromId;

    @NotNull(message = "目标类型不能为空")
    private Integer toType;

    @NotNull(message = "目标ID不能为空")
    private Long toId;

    @NotNull(message = "调拨数量不能为空")
    @Positive(message = "调拨数量必须大于0")
    private Integer quantity;

    private String remark;
}
