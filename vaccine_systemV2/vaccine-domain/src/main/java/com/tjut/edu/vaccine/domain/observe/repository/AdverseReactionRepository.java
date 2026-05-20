package com.tjut.edu.vaccine.domain.observe.repository;

import com.tjut.edu.vaccine.domain.observe.entity.AdverseReaction;

import java.util.List;
import java.util.Optional;

/**
 * 不良反应仓储接口
 */
public interface AdverseReactionRepository {

    Optional<AdverseReaction> findById(Long id);

    void save(AdverseReaction adverseReaction);

    void update(AdverseReaction adverseReaction);

    boolean existsByObserveRecordId(Long observeRecordId);

    List<AdverseReaction> findByObserveRecordId(Long observeRecordId);

    long count();
}
