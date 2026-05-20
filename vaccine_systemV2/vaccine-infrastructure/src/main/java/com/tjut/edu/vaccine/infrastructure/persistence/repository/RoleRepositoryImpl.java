package com.tjut.edu.vaccine.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tjut.edu.vaccine.domain.identity.aggregate.Role;
import com.tjut.edu.vaccine.domain.identity.repository.RoleRepository;
import com.tjut.edu.vaccine.domain.identity.vo.RoleId;
import com.tjut.edu.vaccine.infrastructure.persistence.converter.RoleConverter;
import com.tjut.edu.vaccine.infrastructure.persistence.mapper.SysRoleMapper;
import com.tjut.edu.vaccine.infrastructure.persistence.po.SysRolePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RoleRepositoryImpl implements RoleRepository {

    private final SysRoleMapper sysRoleMapper;

    @Override
    public Role findById(Long id) {
        SysRolePO po = sysRoleMapper.selectById(id);
        return RoleConverter.toDomain(po);
    }

    @Override
    public Role findByRoleCode(String roleCode) {
        LambdaQueryWrapper<SysRolePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePO::getRoleCode, roleCode);
        SysRolePO po = sysRoleMapper.selectOne(wrapper);
        return RoleConverter.toDomain(po);
    }

    @Override
    public List<Role> findAll() {
        List<SysRolePO> poList = sysRoleMapper.selectList(null);
        return poList.stream().map(RoleConverter::toDomain).toList();
    }

    @Override
    public void save(Role role) {
        SysRolePO po = RoleConverter.toPO(role);
        if (po.getId() == null) {
            sysRoleMapper.insert(po);
            role.setId(new RoleId(po.getId()));
        } else {
            sysRoleMapper.updateById(po);
        }
    }

    @Override
    public void update(Role role) {
        SysRolePO po = RoleConverter.toPO(role);
        sysRoleMapper.updateById(po);
    }

    @Override
    public void deleteById(Long id) {
        sysRoleMapper.deleteById(id);
    }

    @Override
    public boolean existsByRoleCode(String roleCode) {
        LambdaQueryWrapper<SysRolePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePO::getRoleCode, roleCode);
        return sysRoleMapper.selectCount(wrapper) > 0;
    }
}
