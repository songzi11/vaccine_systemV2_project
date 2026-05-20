package com.tjut.edu.vaccine.adapter.web.admin;

import com.tjut.edu.vaccine.application.dto.response.VerifyCodeResponse;
import com.tjut.edu.vaccine.application.service.AdminApplicationService;
import com.tjut.edu.vaccine.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/verify-codes")
@RequiredArgsConstructor
@Tag(name = "验证码管理")
public class VerifyCodeAdminController {

    private final AdminApplicationService adminApplicationService;

    @PostMapping
    @Operation(summary = "生成验证码")
    public ApiResponse<VerifyCodeResponse> generateCode() {
        return ApiResponse.success(adminApplicationService.generateVerifyCode());
    }

    @GetMapping
    @Operation(summary = "查询所有验证码")
    public ApiResponse<List<VerifyCodeResponse>> findAllCodes() {
        return ApiResponse.success(adminApplicationService.findAllVerifyCodes());
    }

    @PutMapping("/{id}/revoke")
    @Operation(summary = "撤销验证码")
    public ApiResponse<Void> revokeCode(@PathVariable Long id) {
        adminApplicationService.revokeVerifyCode(id);
        return ApiResponse.success();
    }
}
