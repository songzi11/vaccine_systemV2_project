package com.tjut.edu.vaccine.domain.identity.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 短信验证码实体
 */
@Getter
@Setter
public class SmsVerification implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String phone;
    /**
     * 类型: REGISTER/RESET_PASSWORD
     */
    private String type;
    private String code;
    private LocalDateTime expireTime;
    /**
     * 是否已使用: 0=未使用, 1=已使用
     */
    private int used;
    private LocalDateTime createTime;

    public SmsVerification() {
    }

    public SmsVerification(String phone, String type, String code, LocalDateTime expireTime) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("验证类型不能为空");
        }
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("验证码不能为空");
        }
        if (expireTime == null) {
            throw new IllegalArgumentException("过期时间不能为空");
        }
        this.phone = phone;
        this.type = type;
        this.code = code;
        this.expireTime = expireTime;
        this.used = 0;
        this.createTime = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expireTime);
    }

    public boolean isUsed() {
        return this.used == 1;
    }

    public void markUsed() {
        this.used = 1;
    }
}
