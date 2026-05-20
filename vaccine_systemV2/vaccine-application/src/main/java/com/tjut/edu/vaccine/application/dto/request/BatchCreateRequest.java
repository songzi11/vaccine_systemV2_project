package com.tjut.edu.vaccine.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BatchCreateRequest {

    @NotNull(message = "疫苗ID不能为空")
    private Long vaccineId;

    @NotBlank(message = "批次号不能为空")
    @Size(max = 50, message = "批次号最多50个字符")
    private String batchNo;

    @Size(max = 100, message = "生产厂家最多100个字符")
    private String manufacturer;

    private LocalDate productionDate;

    @NotNull(message = "有效期不能为空")
    private LocalDate expiryDate;

    private Integer warningDays;

    @NotNull(message = "入库总数不能为空")
    private Integer quantity;
}
