package com.tjut.edu.vaccine.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sms_verification")
public class SmsVerificationPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String phone;

    private String type;

    private String code;

    private LocalDateTime expireTime;

    private Integer used;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
