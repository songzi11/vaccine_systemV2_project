package com.tjut.edu.vaccine.domain.identity.aggregate;

import com.tjut.edu.vaccine.common.enums.Gender;
import com.tjut.edu.vaccine.common.enums.IdCardType;
import com.tjut.edu.vaccine.common.enums.UserStatus;
import com.tjut.edu.vaccine.domain.identity.vo.UserId;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户聚合根
 */
@Getter
@Setter
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private UserId id;
    private String username;
    private String phone;
    private String password;
    private String realName;
    private Gender gender;
    private IdCardType idCardType;
    private String idCardNo;
    private UserStatus status;
    private int noShowCount;
    private LocalDateTime freezeStartTime;
    private LocalDateTime freezeEndTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public User() {
    }

    private User(String phone, String password, String realName) {
        this.phone = phone;
        this.password = password;
        this.realName = realName;
        this.status = UserStatus.NORMAL;
        this.noShowCount = 0;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 用户注册工厂方法
     */
    public static User register(String phone, String encodedPassword, String realName) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        if (encodedPassword == null || encodedPassword.isBlank()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (realName == null || realName.isBlank()) {
            throw new IllegalArgumentException("真实姓名不能为空");
        }
        return new User(phone, encodedPassword, realName);
    }

    /**
     * 判断是否允许登录
     */
    public boolean isLoginAllowed() {
        return status != null && status == UserStatus.NORMAL;
    }

    /**
     * 判断是否允许预约
     */
    public boolean isAppointmentAllowed() {
        if (status != UserStatus.NORMAL) {
            return false;
        }
        if (freezeEndTime != null && freezeEndTime.isAfter(LocalDateTime.now())) {
            return false;
        }
        return true;
    }

    /**
     * 冻结用户
     */
    public void freeze(String reason) {
        if (status == UserStatus.FROZEN) {
            throw new IllegalStateException("用户已处于冻结状态");
        }
        this.status = UserStatus.FROZEN;
        this.freezeStartTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 解冻用户
     */
    public void unfreeze() {
        if (status != UserStatus.FROZEN) {
            throw new IllegalStateException("用户不在冻结状态");
        }
        this.status = UserStatus.NORMAL;
        this.freezeStartTime = null;
        this.freezeEndTime = null;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 增加爽约次数
     */
    public void incrementNoShowCount() {
        this.noShowCount++;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 因爽约冻结用户
     */
    public void freezeForNoShow(int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("冻结天数必须大于0");
        }
        this.freezeStartTime = LocalDateTime.now();
        this.freezeEndTime = LocalDateTime.now().plusDays(days);
        this.status = UserStatus.FROZEN;
        this.updateTime = LocalDateTime.now();
    }
}
