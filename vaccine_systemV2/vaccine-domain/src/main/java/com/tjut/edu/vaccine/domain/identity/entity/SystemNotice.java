package com.tjut.edu.vaccine.domain.identity.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 系统公告实体
 */
@Getter
@Setter
public class SystemNotice implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String content;
    /**
     * 公告类型: SYSTEM=系统公告, INTERNAL=内部公告
     */
    private String noticeType;
    /**
     * 状态: 0=待审核, 1=已发布, 2=已下线, 3=已驳回
     */
    private int status;
    private Long authorId;
    private Long auditUserId;
    private LocalDateTime auditTime;
    private String auditReason;
    private LocalDateTime publishTime;
    private LocalDate startTime;
    private LocalDate endTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public SystemNotice() {
    }

    public SystemNotice(String title, String content, String noticeType, Long authorId) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("公告标题不能为空");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("公告内容不能为空");
        }
        if (noticeType == null || noticeType.isBlank()) {
            throw new IllegalArgumentException("公告类型不能为空");
        }
        this.title = title;
        this.content = content;
        this.noticeType = noticeType;
        this.status = 0;
        this.authorId = authorId;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    public void publish(Long auditUserId, LocalDate startTime, LocalDate endTime) {
        this.status = 1;
        this.auditUserId = auditUserId;
        this.auditTime = LocalDateTime.now();
        this.publishTime = LocalDateTime.now();
        this.startTime = startTime;
        this.endTime = endTime;
        this.updateTime = LocalDateTime.now();
    }

    public void reject(Long auditUserId, String reason) {
        this.status = 3;
        this.auditUserId = auditUserId;
        this.auditTime = LocalDateTime.now();
        this.auditReason = reason;
        this.updateTime = LocalDateTime.now();
    }

    public void offline() {
        this.status = 2;
        this.updateTime = LocalDateTime.now();
    }

    public boolean isPublished() {
        return this.status == 1;
    }

    public boolean isPending() {
        return this.status == 0;
    }
}
