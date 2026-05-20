package com.tjut.edu.vaccine.adapter.web.auth;

import com.tjut.edu.vaccine.application.dto.request.LoginRequest;
import com.tjut.edu.vaccine.application.dto.request.RegisterRequest;
import com.tjut.edu.vaccine.application.dto.request.SmsRequest;
import com.tjut.edu.vaccine.application.dto.response.AuthResponse;
import com.tjut.edu.vaccine.application.dto.response.UserInfoResponse;
import com.tjut.edu.vaccine.application.service.AuthApplicationService;
import com.tjut.edu.vaccine.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "认证管理")
public class AuthController {

    private final AuthApplicationService authApplicationService;

    @PostMapping("/public/auth/register")
    @Operation(summary = "用户注册")
    public ApiResponse<AuthResponse> register(@RequestBody @Valid RegisterRequest req) {
        return ApiResponse.success(authApplicationService.register(req));
    }

    @PostMapping("/public/auth/login")
    @Operation(summary = "用户登录")
    public ApiResponse<AuthResponse> login(@RequestBody @Valid LoginRequest req) {
        return ApiResponse.success(authApplicationService.login(req));
    }

    @PostMapping("/public/auth/sms-code")
    @Operation(summary = "发送短信验证码")
    public ApiResponse<String> sendSmsCode(@RequestBody @Valid SmsRequest req) {
        String code = authApplicationService.sendSmsCode(req);
        return ApiResponse.success(code);
    }

    @GetMapping("/user/auth/me")
    @Operation(summary = "获取当前用户信息")
    public ApiResponse<UserInfoResponse> getCurrentUser() {
        return ApiResponse.success(authApplicationService.getCurrentUser());
    }

    @PutMapping("/user/password")
    @Operation(summary = "修改密码")
    public ApiResponse<Void> changePassword(@RequestBody Map<String, String> body) {
        authApplicationService.changePassword(body.get("oldPassword"), body.get("newPassword"));
        return ApiResponse.success(null);
    }

    @PostMapping("/public/auth/sms-login")
    @Operation(summary = "短信验证码登录")
    public ApiResponse<AuthResponse> smsLogin(@RequestBody Map<String, String> body) {
        return ApiResponse.success(authApplicationService.smsLogin(body.get("phone"), body.get("smsCode")));
    }

    @PostMapping("/user/logout")
    @Operation(summary = "用户登出")
    public ApiResponse<Void> logout() {
        authApplicationService.logout();
        return ApiResponse.success();
    }

    @PostMapping("/user/password/reset")
    @Operation(summary = "重置密码（短信验证码方式）")
    public ApiResponse<Void> resetPassword(@RequestBody Map<String, String> body) {
        authApplicationService.resetPassword(body.get("phone"), body.get("smsCode"), body.get("newPassword"));
        return ApiResponse.success();
    }

    @PutMapping("/user/profile")
    @Operation(summary = "更新用户信息")
    public ApiResponse<UserInfoResponse> updateProfile(@RequestBody Map<String, String> body) {
        return ApiResponse.success(authApplicationService.updateProfile(body));
    }
}
