package com.tjut.edu.vaccine.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("sys_user_permission")
public class SysUserPermissionPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long permissionId;
}
