package com.tjut.edu.vaccine.domain.identity.repository;

import com.tjut.edu.vaccine.domain.identity.aggregate.Role;

import java.util.List;

/**
 * 角色仓储接口
 */
public interface RoleRepository {

    Role findById(Long id);

    Role findByRoleCode(String roleCode);

    List<Role> findAll();

    void save(Role role);

    void update(Role role);

    void deleteById(Long id);

    boolean existsByRoleCode(String roleCode);
}
