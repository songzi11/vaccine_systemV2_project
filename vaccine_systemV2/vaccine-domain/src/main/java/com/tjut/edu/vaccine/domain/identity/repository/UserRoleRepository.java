package com.tjut.edu.vaccine.domain.identity.repository;

import com.tjut.edu.vaccine.domain.identity.entity.UserRole;

import java.util.List;

/**
 * 用户角色关联仓储接口
 */
public interface UserRoleRepository {

    List<UserRole> findByUserId(Long userId);

    List<UserRole> findByRoleId(Long roleId);

    void save(UserRole userRole);

    void deleteByUserIdAndRoleId(Long userId, Long roleId);

    List<UserRole> findByUserIds(List<Long> userIds);
}
