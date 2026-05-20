package com.tjut.edu.vaccine.adapter.web.admin;

import com.tjut.edu.vaccine.application.service.StatsApplicationService;
import com.tjut.edu.vaccine.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/stats")
@RequiredArgsConstructor
@Tag(name = "统计看板（管理员）")
public class StatsController {

    private final StatsApplicationService statsApplicationService;

    @GetMapping("/vaccination")
    @Operation(summary = "接种统计")
    public ApiResponse<Map<String, Object>> getVaccinationStats() {
        return ApiResponse.success(statsApplicationService.getVaccinationStats());
    }

    @GetMapping("/stock")
    @Operation(summary = "库存统计")
    public ApiResponse<Map<String, Object>> getStockStats() {
        return ApiResponse.success(statsApplicationService.getStockStats());
    }

    @GetMapping("/efficiency")
    @Operation(summary = "效率统计")
    public ApiResponse<Map<String, Object>> getEfficiencyStats() {
        return ApiResponse.success(statsApplicationService.getEfficiencyStats());
    }

    @GetMapping("/anomaly")
    @Operation(summary = "异常统计")
    public ApiResponse<Map<String, Object>> getAnomalyStats() {
        return ApiResponse.success(statsApplicationService.getAnomalyStats());
    }
}
