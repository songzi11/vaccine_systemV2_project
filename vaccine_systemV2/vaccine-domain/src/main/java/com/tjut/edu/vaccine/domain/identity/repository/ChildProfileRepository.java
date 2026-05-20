package com.tjut.edu.vaccine.domain.identity.repository;

import com.tjut.edu.vaccine.domain.identity.aggregate.ChildProfile;

import java.util.List;

/**
 * 儿童档案仓储接口
 */
public interface ChildProfileRepository {

    ChildProfile findById(Long id);

    List<ChildProfile> findByParentId(Long parentId);

    int countByParentId(Long parentId);

    void save(ChildProfile childProfile);

    void update(ChildProfile childProfile);

    void deleteById(Long id);
}
