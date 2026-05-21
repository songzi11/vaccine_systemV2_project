package com.tjut.edu.vaccine.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("hospital_vaccine_stock")
public class HospitalVaccineStockPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long hospitalId;

    private Long batchId;

    private Integer locationType;

    private Long locationId;

    private Integer totalStock;

    private Integer availableStock;

    private Integer lockedStock;

    @Version
    private Integer version;
}
