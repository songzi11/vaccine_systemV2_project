package com.tjut.edu.vaccine.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("window_service_config")
public class WindowServiceConfigPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String windowCode;
    private String businessName;
    private String businessDesc;
    private String businessDetail;
    private Integer estimatedTime;
    private String tips;
    private String requiredItems;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
