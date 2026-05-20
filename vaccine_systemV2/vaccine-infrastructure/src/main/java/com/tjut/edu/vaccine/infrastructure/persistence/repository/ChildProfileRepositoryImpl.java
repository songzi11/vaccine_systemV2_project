package com.tjut.edu.vaccine.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tjut.edu.vaccine.domain.identity.aggregate.ChildProfile;
import com.tjut.edu.vaccine.domain.identity.repository.ChildProfileRepository;
import com.tjut.edu.vaccine.domain.identity.vo.ChildId;
import com.tjut.edu.vaccine.infrastructure.persistence.converter.ChildProfileConverter;
import com.tjut.edu.vaccine.infrastructure.persistence.mapper.ChildProfileMapper;
import com.tjut.edu.vaccine.infrastructure.persistence.po.ChildProfilePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChildProfileRepositoryImpl implements ChildProfileRepository {

    private final ChildProfileMapper childProfileMapper;

    @Override
    public ChildProfile findById(Long id) {
        ChildProfilePO po = childProfileMapper.selectById(id);
        return ChildProfileConverter.toDomain(po);
    }

    @Override
    public List<ChildProfile> findByParentId(Long parentId) {
        LambdaQueryWrapper<ChildProfilePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChildProfilePO::getParentId, parentId);
        List<ChildProfilePO> poList = childProfileMapper.selectList(wrapper);
        return poList.stream().map(ChildProfileConverter::toDomain).toList();
    }

    @Override
    public int countByParentId(Long parentId) {
        LambdaQueryWrapper<ChildProfilePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChildProfilePO::getParentId, parentId);
        return Math.toIntExact(childProfileMapper.selectCount(wrapper));
    }

    @Override
    public void save(ChildProfile child) {
        ChildProfilePO po = ChildProfileConverter.toPO(child);
        if (po.getId() == null) {
            childProfileMapper.insert(po);
            child.setId(new ChildId(po.getId()));
        } else {
            childProfileMapper.updateById(po);
        }
    }

    @Override
    public void update(ChildProfile child) {
        ChildProfilePO po = ChildProfileConverter.toPO(child);
        childProfileMapper.updateById(po);
    }

    @Override
    public void deleteById(Long id) {
        childProfileMapper.deleteById(id);
    }
}
