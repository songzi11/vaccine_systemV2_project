package com.tjut.edu.vaccine.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tjut.edu.vaccine.domain.identity.entity.Permission;
import com.tjut.edu.vaccine.domain.identity.repository.PermissionRepository;
import com.tjut.edu.vaccine.infrastructure.persistence.converter.PermissionConverter;
import com.tjut.edu.vaccine.infrastructure.persistence.mapper.SysPermissionMapper;
import com.tjut.edu.vaccine.infrastructure.persistence.po.SysPermissionPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PermissionRepositoryImpl implements PermissionRepository {

    private final SysPermissionMapper sysPermissionMapper;

    @Override
    public Permission findById(Long id) {
        SysPermissionPO po = sysPermissionMapper.selectById(id);
        return PermissionConverter.toDomain(po);
    }

    @Override
    public Permission findByPermissionCode(String permissionCode) {
        LambdaQueryWrapper<SysPermissionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPermissionPO::getPermissionCode, permissionCode);
        SysPermissionPO po = sysPermissionMapper.selectOne(wrapper);
        return PermissionConverter.toDomain(po);
    }

    @Override
    public List<Permission> findAll() {
        List<SysPermissionPO> poList = sysPermissionMapper.selectList(null);
        return poList.stream().map(PermissionConverter::toDomain).toList();
    }

    @Override
    public List<Permission> findByRoleId(Long roleId) {
        LambdaQueryWrapper<SysPermissionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.exists("SELECT 1 FROM sys_role_permission rp WHERE rp.permission_id = sys_permission.id AND rp.role_id = {0}", roleId);
        List<SysPermissionPO> poList = sysPermissionMapper.selectList(wrapper);
        return poList.stream().map(PermissionConverter::toDomain).toList();
    }

    @Override
    public List<Permission> findByUserId(Long userId) {
        LambdaQueryWrapper<SysPermissionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.exists("SELECT 1 FROM sys_role_permission rp " +
                "JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
                "WHERE ur.user_id = {0} AND rp.permission_id = sys_permission.id", userId);
        List<SysPermissionPO> poList = sysPermissionMapper.selectList(wrapper);
        return poList.stream().map(PermissionConverter::toDomain).toList();
    }

    @Override
    public List<Permission> findByModule(String module) {
        LambdaQueryWrapper<SysPermissionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPermissionPO::getModule, module);
        List<SysPermissionPO> poList = sysPermissionMapper.selectList(wrapper);
        return poList.stream().map(PermissionConverter::toDomain).toList();
    }

    @Override
    public List<Permission> findByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        String ids = roleIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        LambdaQueryWrapper<SysPermissionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.exists("SELECT 1 FROM sys_role_permission rp WHERE rp.permission_id = sys_permission.id AND rp.role_id IN (" + ids + ")");
        List<SysPermissionPO> poList = sysPermissionMapper.selectList(wrapper);
        return poList.stream().map(PermissionConverter::toDomain).toList();
    }

    @Override
    public void save(Permission permission) {
        SysPermissionPO po = new SysPermissionPO();
        po.setPermissionCode(permission.getPermissionCode());
        po.setPermissionName(permission.getPermissionName());
        po.setModule(permission.getModule());
        po.setDescription(permission.getDescription());
        sysPermissionMapper.insert(po);
        permission.setId(po.getId());
    }
}
