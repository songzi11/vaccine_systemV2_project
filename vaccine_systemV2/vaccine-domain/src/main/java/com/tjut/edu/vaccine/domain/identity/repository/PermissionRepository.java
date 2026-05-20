package com.tjut.edu.vaccine.domain.identity.repository;

import com.tjut.edu.vaccine.domain.identity.entity.Permission;

import java.util.List;

/**
 * 权限仓储接口
 */
public interface PermissionRepository {

    Permission findById(Long id);

    List<Permission> findAll();

    Permission findByPermissionCode(String permissionCode);

    List<Permission> findByModule(String module);

    List<Permission> findByRoleId(Long roleId);

    List<Permission> findByUserId(Long userId);

    void save(Permission permission);

    List<Permission> findByRoleIds(List<Long> roleIds);
}
