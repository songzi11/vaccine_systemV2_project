package com.tjut.edu.vaccine.infrastructure.persistence.converter;

import com.tjut.edu.vaccine.domain.identity.entity.Permission;
import com.tjut.edu.vaccine.infrastructure.persistence.po.SysPermissionPO;

public class PermissionConverter {

    public static Permission toDomain(SysPermissionPO po) {
        if (po == null) {
            return null;
        }
        Permission permission = new Permission();
        permission.setId(po.getId());
        permission.setPermissionCode(po.getPermissionCode());
        permission.setPermissionName(po.getPermissionName());
        permission.setModule(po.getModule());
        permission.setDescription(po.getDescription());
        permission.setCreateTime(po.getCreateTime());
        return permission;
    }
}
