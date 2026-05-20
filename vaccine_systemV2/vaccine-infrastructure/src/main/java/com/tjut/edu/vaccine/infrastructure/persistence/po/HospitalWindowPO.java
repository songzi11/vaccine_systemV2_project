package com.tjut.edu.vaccine.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("hospital_window")
public class HospitalWindowPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String windowCode;

    private String windowName;

    private String windowFunctionType;

    private Integer status;

    private Integer avgHandleTime;

    private Integer sortOrder;

    /** 当前分配的医生ID */
    private Long doctorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
