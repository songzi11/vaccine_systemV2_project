package com.tjut.edu.vaccine.application.assembler;

import com.tjut.edu.vaccine.application.dto.response.AuthResponse;
import com.tjut.edu.vaccine.application.dto.response.UserInfoResponse;
import com.tjut.edu.vaccine.domain.identity.aggregate.User;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class AuthAssembler {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static AuthResponse toAuthResponse(User user, String token, List<String> roles) {
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUserId(user.getId().value());
        response.setPhone(user.getPhone());
        response.setRealName(user.getRealName());
        response.setRoles(roles);
        return response;
    }

    public static UserInfoResponse toUserInfoResponse(User user, List<String> roles) {
        UserInfoResponse response = new UserInfoResponse();
        response.setUserId(user.getId().value());
        response.setPhone(user.getPhone());
        response.setRealName(user.getRealName());
        response.setGender(user.getGender() != null ? user.getGender().getDescription() : null);
        response.setIdCardType(user.getIdCardType() != null ? user.getIdCardType().getDescription() : null);
        response.setIdCardNo(user.getIdCardNo());
        response.setStatus(user.getStatus() != null ? user.getStatus().getDescription() : null);
        response.setCreateTime(user.getCreateTime() != null ? user.getCreateTime().format(DATE_FORMATTER) : null);
        response.setRoles(roles);
        return response;
    }
}
