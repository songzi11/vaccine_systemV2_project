package com.tjut.edu.vaccine.infrastructure.persistence.converter;

import com.tjut.edu.vaccine.common.enums.Severity;
import com.tjut.edu.vaccine.domain.observe.entity.AdverseReaction;
import com.tjut.edu.vaccine.infrastructure.persistence.po.AdverseReactionPO;

public class AdverseReactionConverter {

    public static AdverseReaction toDomain(AdverseReactionPO po) {
        if (po == null) {
            return null;
        }
        AdverseReaction reaction = new AdverseReaction();
        reaction.setId(po.getId());
        reaction.setObserveRecordId(po.getObserveRecordId());
        reaction.setAppointmentId(po.getAppointmentId());
        reaction.setReactionType(po.getReactionType());
        reaction.setDescription(po.getDescription());
        reaction.setSeverity(po.getSeverity() != null ? Severity.fromCode(po.getSeverity()) : null);
        reaction.setReportTime(po.getReportTime());
        reaction.setHandleTime(po.getHandleTime());
        reaction.setHandleResult(po.getHandleResult());
        reaction.setHandlerId(po.getHandlerId());
        reaction.setCreateTime(po.getCreateTime());
        return reaction;
    }

    public static AdverseReactionPO toPO(AdverseReaction reaction) {
        if (reaction == null) {
            return null;
        }
        AdverseReactionPO po = new AdverseReactionPO();
        po.setId(reaction.getId());
        po.setObserveRecordId(reaction.getObserveRecordId());
        po.setAppointmentId(reaction.getAppointmentId());
        po.setReactionType(reaction.getReactionType());
        po.setDescription(reaction.getDescription());
        po.setSeverity(reaction.getSeverity() != null ? reaction.getSeverity().getCode() : null);
        po.setReportTime(reaction.getReportTime());
        po.setHandleTime(reaction.getHandleTime());
        po.setHandleResult(reaction.getHandleResult());
        po.setHandlerId(reaction.getHandlerId());
        po.setCreateTime(reaction.getCreateTime());
        return po;
    }
}
