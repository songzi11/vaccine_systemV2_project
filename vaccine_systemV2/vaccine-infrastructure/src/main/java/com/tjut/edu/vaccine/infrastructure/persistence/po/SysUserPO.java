package com.tjut.edu.vaccine.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUserPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String phone;

    private String password;

    private String realName;

    private Integer gender;

    private Integer idCardType;

    private String idCardNo;

    private Integer status;

    private Integer noShowCount;

    private LocalDateTime freezeStartTime;

    private LocalDateTime freezeEndTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
