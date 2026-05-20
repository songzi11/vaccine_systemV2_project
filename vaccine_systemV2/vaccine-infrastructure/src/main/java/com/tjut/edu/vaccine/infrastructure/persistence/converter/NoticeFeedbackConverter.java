package com.tjut.edu.vaccine.infrastructure.persistence.converter;

import com.tjut.edu.vaccine.domain.identity.entity.NoticeFeedback;
import com.tjut.edu.vaccine.infrastructure.persistence.po.NoticeFeedbackPO;

public class NoticeFeedbackConverter {

    public static NoticeFeedback toDomain(NoticeFeedbackPO po) {
        if (po == null) {
            return null;
        }
        NoticeFeedback feedback = new NoticeFeedback();
        feedback.setId(po.getId());
        feedback.setNoticeId(po.getNoticeId());
        feedback.setUserId(po.getUserId());
        feedback.setContent(po.getContent());
        feedback.setCreateTime(po.getCreateTime());
        return feedback;
    }

    public static NoticeFeedbackPO toPO(NoticeFeedback feedback) {
        if (feedback == null) {
            return null;
        }
        NoticeFeedbackPO po = new NoticeFeedbackPO();
        po.setId(feedback.getId());
        po.setNoticeId(feedback.getNoticeId());
        po.setUserId(feedback.getUserId());
        po.setContent(feedback.getContent());
        return po;
    }
}
