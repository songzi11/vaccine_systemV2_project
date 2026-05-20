package com.tjut.edu.vaccine.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tjut.edu.vaccine.domain.identity.entity.RolePermission;
import com.tjut.edu.vaccine.domain.identity.repository.RolePermissionRepository;
import com.tjut.edu.vaccine.infrastructure.persistence.mapper.SysRolePermissionMapper;
import com.tjut.edu.vaccine.infrastructure.persistence.po.SysRolePermissionPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RolePermissionRepositoryImpl implements RolePermissionRepository {

    private final SysRolePermissionMapper sysRolePermissionMapper;

    @Override
    public List<RolePermission> findByRoleId(Long roleId) {
        LambdaQueryWrapper<SysRolePermissionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePermissionPO::getRoleId, roleId);
        List<SysRolePermissionPO> list = sysRolePermissionMapper.selectList(wrapper);
        return list.stream().map(this::toDomain).toList();
    }

    @Override
    public List<RolePermission> findByPermissionId(Long permissionId) {
        LambdaQueryWrapper<SysRolePermissionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePermissionPO::getPermissionId, permissionId);
        List<SysRolePermissionPO> list = sysRolePermissionMapper.selectList(wrapper);
        return list.stream().map(this::toDomain).toList();
    }

    @Override
    public void save(RolePermission rolePermission) {
        SysRolePermissionPO po = new SysRolePermissionPO();
        po.setId(rolePermission.getId());
        po.setRoleId(rolePermission.getRoleId());
        po.setPermissionId(rolePermission.getPermissionId());
        if (rolePermission.getId() == null) {
            sysRolePermissionMapper.insert(po);
        } else {
            sysRolePermissionMapper.updateById(po);
        }
    }

    @Override
    public void deleteByRoleId(Long roleId) {
        LambdaQueryWrapper<SysRolePermissionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePermissionPO::getRoleId, roleId);
        sysRolePermissionMapper.delete(wrapper);
    }

    private RolePermission toDomain(SysRolePermissionPO po) {
        RolePermission rp = new RolePermission();
        rp.setId(po.getId());
        rp.setRoleId(po.getRoleId());
        rp.setPermissionId(po.getPermissionId());
        rp.setCreateTime(po.getCreateTime());
        return rp;
    }
}
