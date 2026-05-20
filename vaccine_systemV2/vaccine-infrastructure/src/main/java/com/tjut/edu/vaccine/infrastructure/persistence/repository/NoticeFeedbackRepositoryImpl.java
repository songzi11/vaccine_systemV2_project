package com.tjut.edu.vaccine.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tjut.edu.vaccine.domain.identity.entity.NoticeFeedback;
import com.tjut.edu.vaccine.domain.identity.repository.NoticeFeedbackRepository;
import com.tjut.edu.vaccine.infrastructure.persistence.converter.NoticeFeedbackConverter;
import com.tjut.edu.vaccine.infrastructure.persistence.mapper.NoticeFeedbackMapper;
import com.tjut.edu.vaccine.infrastructure.persistence.po.NoticeFeedbackPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class NoticeFeedbackRepositoryImpl implements NoticeFeedbackRepository {

    private final NoticeFeedbackMapper noticeFeedbackMapper;

    @Override
    public List<NoticeFeedback> findByNoticeId(Long noticeId) {
        List<NoticeFeedbackPO> list = noticeFeedbackMapper.selectList(
            new LambdaQueryWrapper<NoticeFeedbackPO>()
                .eq(NoticeFeedbackPO::getNoticeId, noticeId)
                .orderByDesc(NoticeFeedbackPO::getCreateTime));
        return list.stream().map(NoticeFeedbackConverter::toDomain).toList();
    }

    @Override
    public void save(NoticeFeedback feedback) {
        noticeFeedbackMapper.insert(NoticeFeedbackConverter.toPO(feedback));
    }
}
