package com.tjut.edu.vaccine.application.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class UserInfoResponse {

    private Long userId;
    private String phone;
    private String realName;
    private String gender;
    private String idCardType;
    private String idCardNo;
    private String status;
    private String createTime;
    private List<String> roles;
    private String windowCode;
    private String windowName;
    private String windowFunctionType;
}
