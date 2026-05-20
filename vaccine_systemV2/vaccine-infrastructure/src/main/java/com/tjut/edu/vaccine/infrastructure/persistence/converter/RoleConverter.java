package com.tjut.edu.vaccine.infrastructure.persistence.converter;

import com.tjut.edu.vaccine.domain.identity.aggregate.Role;
import com.tjut.edu.vaccine.domain.identity.vo.RoleId;
import com.tjut.edu.vaccine.infrastructure.persistence.po.SysRolePO;

public class RoleConverter {

    public static Role toDomain(SysRolePO po) {
        if (po == null) {
            return null;
        }
        Role role = new Role();
        role.setId(new RoleId(po.getId()));
        role.setRoleCode(po.getRoleCode());
        role.setRoleName(po.getRoleName());
        role.setRoleGroup(po.getRoleGroup());
        role.setDescription(po.getDescription());
        role.setStatus(po.getStatus());
        role.setIsSystem(po.getIsSystem());
        role.setCreateTime(po.getCreateTime());
        role.setUpdateTime(po.getUpdateTime());
        return role;
    }

    public static SysRolePO toPO(Role role) {
        if (role == null) {
            return null;
        }
        SysRolePO po = new SysRolePO();
        if (role.getId() != null) {
            po.setId(role.getId().value());
        }
        po.setRoleCode(role.getRoleCode());
        po.setRoleName(role.getRoleName());
        po.setRoleGroup(role.getRoleGroup());
        po.setDescription(role.getDescription());
        po.setStatus(role.getStatus());
        po.setIsSystem(role.getIsSystem());
        po.setCreateTime(role.getCreateTime());
        po.setUpdateTime(role.getUpdateTime());
        return po;
    }
}
