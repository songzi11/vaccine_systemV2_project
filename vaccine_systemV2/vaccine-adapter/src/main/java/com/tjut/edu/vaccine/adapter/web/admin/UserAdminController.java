package com.tjut.edu.vaccine.adapter.web.admin;

import com.tjut.edu.vaccine.application.dto.response.RoleResponse;
import com.tjut.edu.vaccine.application.dto.response.UserResponse;
import com.tjut.edu.vaccine.application.service.AdminApplicationService;
import com.tjut.edu.vaccine.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Tag(name = "用户管理")
public class UserAdminController {

    private final AdminApplicationService adminApplicationService;

    @GetMapping
    @Operation(summary = "查询用户列表")
    public ApiResponse<List<UserResponse>> findAllUsers(
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(adminApplicationService.findAllUsers(keyword));
    }

    @PutMapping("/{id}/freeze")
    @Operation(summary = "冻结用户")
    public ApiResponse<Void> freezeUser(@PathVariable Long id) {
        adminApplicationService.freezeUser(id);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/unfreeze")
    @Operation(summary = "解冻用户")
    public ApiResponse<Void> unfreezeUser(@PathVariable Long id) {
        adminApplicationService.unfreezeUser(id);
        return ApiResponse.success();
    }

    @PostMapping("/{userId}/assign-roles")
    @Operation(summary = "分配用户角色")
    public ApiResponse<Void> assignRoles(@PathVariable Long userId,
                                          @RequestBody Map<String, List<Long>> body) {
        adminApplicationService.assignRoles(userId, body.get("roleIds"));
        return ApiResponse.success();
    }

    @GetMapping("/{userId}/roles")
    @Operation(summary = "获取用户角色")
    public ApiResponse<List<RoleResponse>> getUserRoles(@PathVariable Long userId) {
        return ApiResponse.success(adminApplicationService.getUserRoles(userId));
    }

    @PostMapping("/{userId}/assign-window")
    @Operation(summary = "安排医生到窗口")
    public ApiResponse<Void> assignDoctorToWindow(@PathVariable Long userId,
                                                    @RequestBody Map<String, Long> body) {
        adminApplicationService.assignDoctorToWindow(userId, body.get("windowId"));
        return ApiResponse.success();
    }
}
