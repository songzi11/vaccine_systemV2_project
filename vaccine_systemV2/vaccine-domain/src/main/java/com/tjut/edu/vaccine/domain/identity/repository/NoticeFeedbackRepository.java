package com.tjut.edu.vaccine.domain.identity.repository;

import com.tjut.edu.vaccine.domain.identity.entity.NoticeFeedback;

import java.util.List;

public interface NoticeFeedbackRepository {

    List<NoticeFeedback> findByNoticeId(Long noticeId);

    void save(NoticeFeedback feedback);
}
