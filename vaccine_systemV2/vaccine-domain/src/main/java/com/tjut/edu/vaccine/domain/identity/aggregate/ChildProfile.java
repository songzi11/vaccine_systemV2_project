package com.tjut.edu.vaccine.domain.identity.aggregate;

import com.tjut.edu.vaccine.common.enums.Gender;
import com.tjut.edu.vaccine.common.enums.IdCardType;
import com.tjut.edu.vaccine.domain.identity.vo.ChildId;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 儿童档案聚合根
 */
@Getter
@Setter
public class ChildProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int MAX_CHILD_COUNT = 5;

    private ChildId id;
    private Long parentId;
    private String parentIdCard;
    private String name;
    private Gender gender;
    private LocalDate birthDate;
    private IdCardType idCardType;
    private String idCardNo;
    private String nativePlace;
    private String nation;
    private String medicalHistory;
    private String allergyHistory;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public ChildProfile() {
    }

    private ChildProfile(Long parentId, String name, Gender gender, LocalDate birthDate, String idCardNo) {
        this.parentId = parentId;
        this.name = name;
        this.gender = gender;
        this.birthDate = birthDate;
        this.idCardNo = idCardNo;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 创建儿童档案工厂方法
     */
    public static ChildProfile create(Long parentId, String name, Gender gender, LocalDate birthDate, String idCardNo) {
        if (parentId == null) {
            throw new IllegalArgumentException("家长ID不能为空");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("儿童姓名不能为空");
        }
        if (gender == null) {
            throw new IllegalArgumentException("性别不能为空");
        }
        if (birthDate == null) {
            throw new IllegalArgumentException("出生日期不能为空");
        }
        return new ChildProfile(parentId, name, gender, birthDate, idCardNo);
    }

    /**
     * 校验儿童数量上限
     */
    public static void validateChildCount(int currentCount) {
        if (currentCount >= MAX_CHILD_COUNT) {
            throw new IllegalStateException("每位家长最多可添加" + MAX_CHILD_COUNT + "个儿童档案");
        }
    }

    /**
     * 更新儿童档案信息
     */
    public void update(String name, Gender gender, LocalDate birthDate, IdCardType idCardType,
                       String idCardNo, String nativePlace, String nation,
                       String medicalHistory, String allergyHistory) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (gender != null) {
            this.gender = gender;
        }
        if (birthDate != null) {
            this.birthDate = birthDate;
        }
        if (idCardType != null) {
            this.idCardType = idCardType;
        }
        if (idCardNo != null) {
            this.idCardNo = idCardNo;
        }
        this.nativePlace = nativePlace;
        this.nation = nation;
        this.medicalHistory = medicalHistory;
        this.allergyHistory = allergyHistory;
        this.updateTime = LocalDateTime.now();
    }
}
