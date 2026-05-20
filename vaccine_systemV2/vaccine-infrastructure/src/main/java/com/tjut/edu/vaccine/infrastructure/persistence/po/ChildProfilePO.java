package com.tjut.edu.vaccine.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("child_profile")
public class ChildProfilePO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long parentId;

    private String parentIdCard;

    private String name;

    private Integer gender;

    private LocalDate birthDate;

    private Integer idCardType;

    private String idCardNo;

    private String nativePlace;

    private String nation;

    private String medicalHistory;

    private String allergyHistory;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
