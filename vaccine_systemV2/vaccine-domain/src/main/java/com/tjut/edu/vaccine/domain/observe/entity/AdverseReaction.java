package com.tjut.edu.vaccine.domain.observe.entity;

import com.tjut.edu.vaccine.common.enums.Severity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 不良反应实体
 */
@Getter
@Setter
public class AdverseReaction implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long observeRecordId;
    private Long appointmentId;
    private String reactionType;
    private String description;
    private Severity severity;
    private LocalDateTime reportTime;
    private LocalDateTime handleTime;
    private String handleResult;
    private Long handlerId;
    private LocalDateTime createTime;

    public AdverseReaction() {
    }

    public AdverseReaction(Long observeRecordId, Long appointmentId,
                           String reactionType, String description, Severity severity) {
        if (observeRecordId == null) {
            throw new IllegalArgumentException("留观记录ID不能为空");
        }
        if (appointmentId == null) {
            throw new IllegalArgumentException("预约ID不能为空");
        }
        if (reactionType == null || reactionType.isBlank()) {
            throw new IllegalArgumentException("反应类型不能为空");
        }
        if (severity == null) {
            throw new IllegalArgumentException("严重程度不能为空");
        }
        this.observeRecordId = observeRecordId;
        this.appointmentId = appointmentId;
        this.reactionType = reactionType;
        this.description = description;
        this.severity = severity;
        this.reportTime = LocalDateTime.now();
        this.createTime = LocalDateTime.now();
    }

    public void handle(String handleResult, Long handlerId) {
        if (handleResult == null || handleResult.isBlank()) {
            throw new IllegalArgumentException("处理结果不能为空");
        }
        this.handleResult = handleResult;
        this.handlerId = handlerId;
        this.handleTime = LocalDateTime.now();
    }

    public boolean isHandled() {
        return this.handleTime != null;
    }
}
