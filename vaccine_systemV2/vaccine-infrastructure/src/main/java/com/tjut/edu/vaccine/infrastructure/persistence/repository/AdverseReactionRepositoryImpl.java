package com.tjut.edu.vaccine.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tjut.edu.vaccine.domain.observe.entity.AdverseReaction;
import com.tjut.edu.vaccine.domain.observe.repository.AdverseReactionRepository;
import com.tjut.edu.vaccine.infrastructure.persistence.converter.AdverseReactionConverter;
import com.tjut.edu.vaccine.infrastructure.persistence.mapper.AdverseReactionMapper;
import com.tjut.edu.vaccine.infrastructure.persistence.po.AdverseReactionPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AdverseReactionRepositoryImpl implements AdverseReactionRepository {

    private final AdverseReactionMapper adverseReactionMapper;

    @Override
    public Optional<AdverseReaction> findById(Long id) {
        AdverseReactionPO po = adverseReactionMapper.selectById(id);
        return Optional.ofNullable(po).map(AdverseReactionConverter::toDomain);
    }

    @Override
    public void save(AdverseReaction reaction) {
        adverseReactionMapper.insert(AdverseReactionConverter.toPO(reaction));
    }

    @Override
    public void update(AdverseReaction reaction) {
        adverseReactionMapper.updateById(AdverseReactionConverter.toPO(reaction));
    }

    @Override
    public boolean existsByObserveRecordId(Long observeRecordId) {
        return adverseReactionMapper.selectCount(
            new LambdaQueryWrapper<AdverseReactionPO>()
                .eq(AdverseReactionPO::getObserveRecordId, observeRecordId)) > 0;
    }

    @Override
    public List<AdverseReaction> findByObserveRecordId(Long observeRecordId) {
        List<AdverseReactionPO> list = adverseReactionMapper.selectList(
            new LambdaQueryWrapper<AdverseReactionPO>()
                .eq(AdverseReactionPO::getObserveRecordId, observeRecordId)
                .orderByDesc(AdverseReactionPO::getReportTime));
        return list.stream().map(AdverseReactionConverter::toDomain).toList();
    }

    @Override
    public long count() {
        return adverseReactionMapper.selectCount(null);
    }
}
