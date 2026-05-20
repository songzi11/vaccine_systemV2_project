package com.tjut.edu.vaccine.domain.identity.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公告反馈实体
 */
@Getter
@Setter
public class NoticeFeedback implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long noticeId;
    private Long userId;
    private String content;
    private LocalDateTime createTime;

    public NoticeFeedback() {
    }

    public NoticeFeedback(Long noticeId, Long userId, String content) {
        if (noticeId == null) {
            throw new IllegalArgumentException("公告ID不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("反馈内容不能为空");
        }
        this.noticeId = noticeId;
        this.userId = userId;
        this.content = content;
        this.createTime = LocalDateTime.now();
    }
}
