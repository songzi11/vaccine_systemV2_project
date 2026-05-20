package com.tjut.edu.vaccine.adapter.web.schedule;

import com.tjut.edu.vaccine.application.dto.request.ScheduleCreateRequest;
import com.tjut.edu.vaccine.application.dto.request.ScheduleToggleRequest;
import com.tjut.edu.vaccine.application.dto.request.ScheduleUpdateRequest;
import com.tjut.edu.vaccine.application.dto.response.ScheduleDailyViewResponse;
import com.tjut.edu.vaccine.application.dto.response.ScheduleResponse;
import com.tjut.edu.vaccine.application.service.ScheduleApplicationService;
import com.tjut.edu.vaccine.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/schedule")
@RequiredArgsConstructor
@Tag(name = "排班管理")
public class ScheduleController {

    private final ScheduleApplicationService scheduleApplicationService;

    @PostMapping
    @Operation(summary = "创建排班")
    public ApiResponse<ScheduleResponse> create(@RequestBody @Valid ScheduleCreateRequest req) {
        return ApiResponse.success(scheduleApplicationService.createSchedule(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新排班")
    public ApiResponse<ScheduleResponse> update(@PathVariable Long id,
                                                 @RequestBody @Valid ScheduleUpdateRequest req) {
        return ApiResponse.success(scheduleApplicationService.updateSchedule(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除排班")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        scheduleApplicationService.deleteSchedule(id);
        return ApiResponse.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询排班详情")
    public ApiResponse<ScheduleResponse> findById(@PathVariable Long id) {
        return ApiResponse.success(scheduleApplicationService.findById(id));
    }

    @GetMapping("/date")
    @Operation(summary = "按日期查询排班")
    public ApiResponse<List<ScheduleResponse>> findByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.success(scheduleApplicationService.findByDate(date));
    }

    @GetMapping("/daily-view")
    @Operation(summary = "排班日视图（默认展示所有医生）")
    public ApiResponse<List<ScheduleDailyViewResponse>> dailyView(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.success(scheduleApplicationService.getDailyView(date));
    }

    @PostMapping("/toggle")
    @Operation(summary = "切换排班状态（请假/取消/恢复默认）")
    public ApiResponse<Void> toggleStatus(@RequestBody @Valid ScheduleToggleRequest req) {
        scheduleApplicationService.toggleScheduleStatus(req);
        return ApiResponse.success();
    }
}
