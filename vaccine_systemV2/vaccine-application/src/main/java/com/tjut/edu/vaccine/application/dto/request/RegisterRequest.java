package com.tjut.edu.vaccine.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度为6-20位")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 20, message = "真实姓名最多20个字符")
    private String realName;

    @NotBlank(message = "注册角色不能为空")
    private String roleType;  // PARENT 或 DOCTOR

    private String verifyCode; // 医生注册时必填
}
