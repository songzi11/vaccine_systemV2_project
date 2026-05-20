package com.tjut.edu.vaccine.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tjut.edu.vaccine.domain.identity.aggregate.User;
import com.tjut.edu.vaccine.domain.identity.repository.UserRepository;
import com.tjut.edu.vaccine.domain.identity.vo.UserId;
import com.tjut.edu.vaccine.infrastructure.persistence.converter.UserConverter;
import com.tjut.edu.vaccine.infrastructure.persistence.mapper.SysUserMapper;
import com.tjut.edu.vaccine.infrastructure.persistence.po.SysUserPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final SysUserMapper sysUserMapper;

    @Override
    public User findById(Long id) {
        SysUserPO po = sysUserMapper.selectById(id);
        return UserConverter.toDomain(po);
    }

    @Override
    public List<User> findAll() {
        List<SysUserPO> list = sysUserMapper.selectList(
            new LambdaQueryWrapper<SysUserPO>().orderByDesc(SysUserPO::getCreateTime));
        return list.stream().map(UserConverter::toDomain).toList();
    }

    @Override
    public List<User> findByKeyword(String keyword) {
        LambdaQueryWrapper<SysUserPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .like(SysUserPO::getPhone, keyword)
                .or().like(SysUserPO::getRealName, keyword)
                .or().like(SysUserPO::getUsername, keyword));
        wrapper.orderByDesc(SysUserPO::getCreateTime);
        List<SysUserPO> list = sysUserMapper.selectList(wrapper);
        return list.stream().map(UserConverter::toDomain).toList();
    }

    @Override
    public User findByPhone(String phone) {
        LambdaQueryWrapper<SysUserPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserPO::getPhone, phone);
        SysUserPO po = sysUserMapper.selectOne(wrapper);
        return UserConverter.toDomain(po);
    }

    @Override
    public User findByUsername(String username) {
        LambdaQueryWrapper<SysUserPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserPO::getUsername, username);
        SysUserPO po = sysUserMapper.selectOne(wrapper);
        return UserConverter.toDomain(po);
    }

    @Override
    public void save(User user) {
        SysUserPO po = UserConverter.toPO(user);
        if (po.getId() == null) {
            sysUserMapper.insert(po);
            user.setId(new UserId(po.getId()));
        } else {
            sysUserMapper.updateById(po);
        }
    }

    @Override
    public void updateStatus(User user) {
        SysUserPO po = UserConverter.toPO(user);
        sysUserMapper.updateById(po);
    }

    @Override
    public void incrementNoShowCount(Long userId) {
        SysUserPO po = sysUserMapper.selectById(userId);
        if (po != null) {
            po.setNoShowCount(po.getNoShowCount() != null ? po.getNoShowCount() + 1 : 1);
            sysUserMapper.updateById(po);
        }
    }

    @Override
    public void freezeForNoShow(Long userId, int days) {
        User user = findById(userId);
        if (user != null) {
            user.freezeForNoShow(days);
            SysUserPO po = UserConverter.toPO(user);
            sysUserMapper.updateById(po);
        }
    }
}
