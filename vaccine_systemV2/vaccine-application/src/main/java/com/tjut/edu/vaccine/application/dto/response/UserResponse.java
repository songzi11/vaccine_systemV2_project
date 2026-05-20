package com.tjut.edu.vaccine.application.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class UserResponse {

    private Long userId;
    private String phone;
    private String realName;
    private String gender;
    private String status;
    private List<String> roleCodes;
    private String createTime;
}
