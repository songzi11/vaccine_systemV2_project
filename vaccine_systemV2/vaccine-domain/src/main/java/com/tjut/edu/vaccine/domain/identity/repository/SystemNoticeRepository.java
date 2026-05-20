package com.tjut.edu.vaccine.domain.identity.repository;

import com.tjut.edu.vaccine.domain.identity.entity.SystemNotice;

import java.util.List;
import java.util.Optional;

public interface SystemNoticeRepository {

    Optional<SystemNotice> findById(Long id);

    List<SystemNotice> findPublished();

    List<SystemNotice> findAll(int page, int size);

    void save(SystemNotice notice);

    void update(SystemNotice notice);

    void deleteById(Long id);
}
