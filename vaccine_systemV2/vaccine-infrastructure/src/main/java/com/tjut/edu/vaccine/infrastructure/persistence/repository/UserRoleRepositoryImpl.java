package com.tjut.edu.vaccine.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tjut.edu.vaccine.domain.identity.entity.UserRole;
import com.tjut.edu.vaccine.domain.identity.repository.UserRoleRepository;
import com.tjut.edu.vaccine.infrastructure.persistence.mapper.SysUserRoleMapper;
import com.tjut.edu.vaccine.infrastructure.persistence.po.SysUserRolePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRoleRepositoryImpl implements UserRoleRepository {

    private final SysUserRoleMapper sysUserRoleMapper;

    @Override
    public List<UserRole> findByUserId(Long userId) {
        LambdaQueryWrapper<SysUserRolePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRolePO::getUserId, userId);
        List<SysUserRolePO> list = sysUserRoleMapper.selectList(wrapper);
        return list.stream().map(this::toDomain).toList();
    }

    @Override
    public List<UserRole> findByRoleId(Long roleId) {
        LambdaQueryWrapper<SysUserRolePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRolePO::getRoleId, roleId);
        List<SysUserRolePO> list = sysUserRoleMapper.selectList(wrapper);
        return list.stream().map(this::toDomain).toList();
    }

    @Override
    public void save(UserRole userRole) {
        SysUserRolePO po = new SysUserRolePO();
        po.setId(userRole.getId());
        po.setUserId(userRole.getUserId());
        po.setRoleId(userRole.getRoleId());
        if (userRole.getId() == null) {
            sysUserRoleMapper.insert(po);
        } else {
            sysUserRoleMapper.updateById(po);
        }
    }

    @Override
    public void deleteByUserIdAndRoleId(Long userId, Long roleId) {
        LambdaQueryWrapper<SysUserRolePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRolePO::getUserId, userId)
               .eq(SysUserRolePO::getRoleId, roleId);
        sysUserRoleMapper.delete(wrapper);
    }

    @Override
    public List<UserRole> findByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<SysUserRolePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SysUserRolePO::getUserId, userIds);
        List<SysUserRolePO> list = sysUserRoleMapper.selectList(wrapper);
        return list.stream().map(this::toDomain).toList();
    }

    private UserRole toDomain(SysUserRolePO po) {
        UserRole userRole = new UserRole();
        userRole.setId(po.getId());
        userRole.setUserId(po.getUserId());
        userRole.setRoleId(po.getRoleId());
        userRole.setCreateTime(po.getCreateTime());
        return userRole;
    }
}
