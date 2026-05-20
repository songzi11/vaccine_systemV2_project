package com.tjut.edu.vaccine.application.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class AuthResponse {

    private String token;
    private Long userId;
    private String phone;
    private String realName;
    private List<String> roles;
}
