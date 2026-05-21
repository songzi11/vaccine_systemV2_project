package com.tjut.edu.vaccine.infrastructure.persistence.converter;

import com.tjut.edu.vaccine.domain.identity.entity.SystemNotice;
import com.tjut.edu.vaccine.infrastructure.persistence.po.SystemNoticePO;

public class SystemNoticeConverter {

    public static SystemNotice toDomain(SystemNoticePO po) {
        if (po == null) {
            return null;
        }
        SystemNotice notice = new SystemNotice();
        notice.setId(po.getId());
        notice.setTitle(po.getTitle());
        notice.setContent(po.getContent());
        notice.setNoticeType(po.getNoticeType());
        notice.setStatus(po.getStatus());
        notice.setTargetUserId(po.getTargetUserId());
        notice.setAuthorId(po.getAuthorId());
        notice.setAuditUserId(po.getAuditUserId());
        notice.setAuditTime(po.getAuditTime());
        notice.setAuditReason(po.getAuditReason());
        notice.setPublishTime(po.getPublishTime());
        notice.setStartTime(po.getStartTime() != null ? po.getStartTime().toLocalDate() : null);
        notice.setEndTime(po.getEndTime() != null ? po.getEndTime().toLocalDate() : null);
        notice.setCreateTime(po.getCreateTime());
        notice.setUpdateTime(po.getUpdateTime());
        return notice;
    }

    public static SystemNoticePO toPO(SystemNotice notice) {
        if (notice == null) {
            return null;
        }
        SystemNoticePO po = new SystemNoticePO();
        po.setId(notice.getId());
        po.setTitle(notice.getTitle());
        po.setContent(notice.getContent());
        po.setNoticeType(notice.getNoticeType());
        po.setStatus(notice.getStatus());
        po.setTargetUserId(notice.getTargetUserId());
        po.setAuthorId(notice.getAuthorId());
        po.setAuditUserId(notice.getAuditUserId());
        po.setAuditTime(notice.getAuditTime());
        po.setAuditReason(notice.getAuditReason());
        po.setPublishTime(notice.getPublishTime());
        po.setStartTime(notice.getStartTime() != null ? notice.getStartTime().atStartOfDay() : null);
        po.setEndTime(notice.getEndTime() != null ? notice.getEndTime().atStartOfDay() : null);
        return po;
    }
}
