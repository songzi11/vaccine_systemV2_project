package com.tjut.edu.vaccine.adapter.web.admin;

import com.tjut.edu.vaccine.application.dto.request.RoleCreateRequest;
import com.tjut.edu.vaccine.application.dto.request.RoleUpdateRequest;
import com.tjut.edu.vaccine.application.dto.response.RoleResponse;
import com.tjut.edu.vaccine.application.service.AdminApplicationService;
import com.tjut.edu.vaccine.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/roles")
@RequiredArgsConstructor
@Tag(name = "角色管理")
public class RoleAdminController {

    private final AdminApplicationService adminApplicationService;

    @PostMapping
    @Operation(summary = "创建角色")
    public ApiResponse<RoleResponse> createRole(@RequestBody @Valid RoleCreateRequest req) {
        return ApiResponse.success(adminApplicationService.createRole(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新角色")
    public ApiResponse<RoleResponse> updateRole(@PathVariable Long id,
                                                 @RequestBody @Valid RoleUpdateRequest req) {
        return ApiResponse.success(adminApplicationService.updateRole(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色")
    public ApiResponse<Void> deleteRole(@PathVariable Long id) {
        adminApplicationService.deleteRole(id);
        return ApiResponse.success();
    }

    @GetMapping
    @Operation(summary = "查询所有角色")
    public ApiResponse<List<RoleResponse>> findAllRoles() {
        return ApiResponse.success(adminApplicationService.findAllRoles());
    }

    @GetMapping("/{id}/permissions")
    @Operation(summary = "获取角色权限列表")
    public ApiResponse<List<String>> getRolePermissions(@PathVariable Long id) {
        return ApiResponse.success(adminApplicationService.getRolePermissions(id));
    }
}
