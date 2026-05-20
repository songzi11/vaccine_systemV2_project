package com.tjut.edu.vaccine.adapter.web.admin;

import com.tjut.edu.vaccine.application.service.ConfigApplicationService;
import com.tjut.edu.vaccine.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/configs")
@RequiredArgsConstructor
@Tag(name = "系统配置（管理员）")
public class ConfigController {

    private final ConfigApplicationService configApplicationService;

    @GetMapping
    @Operation(summary = "查询所有配置")
    public ApiResponse<List<Map<String, Object>>> findAllConfigs() {
        return ApiResponse.success(configApplicationService.findAllConfigs());
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新配置值")
    public ApiResponse<Map<String, Object>> updateConfig(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String configValue = body.get("configValue");
        return ApiResponse.success(configApplicationService.updateConfig(id, configValue));
    }
}
