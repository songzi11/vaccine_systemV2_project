package com.tjut.edu.vaccine.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tjut.edu.vaccine.domain.identity.entity.SystemNotice;
import com.tjut.edu.vaccine.domain.identity.repository.SystemNoticeRepository;
import com.tjut.edu.vaccine.infrastructure.persistence.converter.SystemNoticeConverter;
import com.tjut.edu.vaccine.infrastructure.persistence.mapper.SystemNoticeMapper;
import com.tjut.edu.vaccine.infrastructure.persistence.po.SystemNoticePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SystemNoticeRepositoryImpl implements SystemNoticeRepository {

    private final SystemNoticeMapper systemNoticeMapper;

    @Override
    public Optional<SystemNotice> findById(Long id) {
        SystemNoticePO po = systemNoticeMapper.selectById(id);
        return Optional.ofNullable(po).map(SystemNoticeConverter::toDomain);
    }

    @Override
    public List<SystemNotice> findPublished() {
        List<SystemNoticePO> list = systemNoticeMapper.selectList(
            new LambdaQueryWrapper<SystemNoticePO>()
                .eq(SystemNoticePO::getStatus, 1)
                .orderByDesc(SystemNoticePO::getPublishTime));
        return list.stream().map(SystemNoticeConverter::toDomain).toList();
    }

    @Override
    public List<SystemNotice> findAll(int page, int size) {
        List<SystemNoticePO> list = systemNoticeMapper.selectList(
            new LambdaQueryWrapper<SystemNoticePO>()
                .orderByDesc(SystemNoticePO::getCreateTime)
                .last("LIMIT " + size + " OFFSET " + (page - 1) * size));
        return list.stream().map(SystemNoticeConverter::toDomain).toList();
    }

    @Override
    public void save(SystemNotice notice) {
        systemNoticeMapper.insert(SystemNoticeConverter.toPO(notice));
    }

    @Override
    public void update(SystemNotice notice) {
        systemNoticeMapper.updateById(SystemNoticeConverter.toPO(notice));
    }

    @Override
    public void deleteById(Long id) {
        systemNoticeMapper.deleteById(id);
    }
}
