package com.tjut.edu.vaccine.adapter.web.admin;

import com.tjut.edu.vaccine.application.dto.request.WindowCreateRequest;
import com.tjut.edu.vaccine.application.dto.request.WindowServiceRequest;
import com.tjut.edu.vaccine.application.dto.request.WindowUpdateRequest;
import com.tjut.edu.vaccine.application.dto.response.WindowResponse;
import com.tjut.edu.vaccine.application.dto.response.WindowServiceResponse;
import com.tjut.edu.vaccine.application.service.AdminApplicationService;
import com.tjut.edu.vaccine.application.service.ScheduleApplicationService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/windows")
@RequiredArgsConstructor
@Tag(name = "窗口管理")
public class WindowAdminController {

    private final ScheduleApplicationService scheduleApplicationService;
    private final AdminApplicationService adminApplicationService;

    @PostMapping
    @Operation(summary = "创建窗口")
    public ApiResponse<WindowResponse> createWindow(@RequestBody @Valid WindowCreateRequest req) {
        return ApiResponse.success(scheduleApplicationService.createWindow(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新窗口")
    public ApiResponse<WindowResponse> updateWindow(@PathVariable Long id,
                                                     @RequestBody @Valid WindowUpdateRequest req) {
        return ApiResponse.success(scheduleApplicationService.updateWindow(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除窗口")
    public ApiResponse<Void> deleteWindow(@PathVariable Long id) {
        scheduleApplicationService.deleteWindow(id);
        return ApiResponse.success();
    }

    @GetMapping
    @Operation(summary = "查询所有窗口")
    public ApiResponse<List<WindowResponse>> findAllWindows() {
        return ApiResponse.success(scheduleApplicationService.findAllWindows());
    }

    @GetMapping("/type")
    @Operation(summary = "按职能类型查询窗口")
    public ApiResponse<List<WindowResponse>> findByType(@RequestParam String functionType) {
        return ApiResponse.success(scheduleApplicationService.findWindowsByType(functionType));
    }

    @PostMapping("/{windowCode}/service")
    @Operation(summary = "保存窗口服务配置")
    public ApiResponse<WindowServiceResponse> saveWindowService(@PathVariable String windowCode,
                                                                @RequestBody @Valid WindowServiceRequest req) {
        return ApiResponse.success(scheduleApplicationService.saveWindowService(windowCode, req));
    }

    @GetMapping("/service/{windowCode}")
    @Operation(summary = "查询窗口服务配置")
    public ApiResponse<WindowServiceResponse> getWindowService(@PathVariable String windowCode) {
        return ApiResponse.success(scheduleApplicationService.getWindowService(windowCode));
    }

    @PostMapping("/{windowId}/assign-doctor")
    @Operation(summary = "分配医生到窗口")
    public ApiResponse<Void> assignDoctorToWindow(@PathVariable Long windowId,
                                                    @RequestBody Map<String, Long> body) {
        adminApplicationService.assignDoctorToWindow(body.get("doctorId"), windowId);
        return ApiResponse.success();
    }

    @PostMapping("/{windowId}/remove-doctor")
    @Operation(summary = "从窗口移除医生（设为后勤）")
    public ApiResponse<Void> removeDoctorFromWindow(@PathVariable Long windowId) {
        adminApplicationService.removeDoctorFromWindow(windowId);
        return ApiResponse.success();
    }
}
