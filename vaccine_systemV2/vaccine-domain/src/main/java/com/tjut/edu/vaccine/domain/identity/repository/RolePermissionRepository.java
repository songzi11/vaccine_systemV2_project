package com.tjut.edu.vaccine.domain.identity.repository;

import com.tjut.edu.vaccine.domain.identity.entity.RolePermission;

import java.util.List;

/**
 * 角色权限关联仓储接口
 */
public interface RolePermissionRepository {

    List<RolePermission> findByRoleId(Long roleId);

    List<RolePermission> findByPermissionId(Long permissionId);

    void save(RolePermission rolePermission);

    void deleteByRoleId(Long roleId);
}
