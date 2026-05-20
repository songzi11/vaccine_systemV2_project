package com.tjut.edu.vaccine.adapter.web.appointment;

import com.tjut.edu.vaccine.application.dto.request.AppointmentBookRequest;
import com.tjut.edu.vaccine.application.dto.request.AppointmentCancelRequest;
import com.tjut.edu.vaccine.application.dto.response.AppointmentDetailResponse;
import com.tjut.edu.vaccine.application.dto.response.AppointmentResponse;
import com.tjut.edu.vaccine.application.service.AppointmentApplicationService;
import com.tjut.edu.vaccine.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user/appointments")
@RequiredArgsConstructor
@Tag(name = "预约管理")
public class AppointmentController {

    private final AppointmentApplicationService appointmentApplicationService;

    @PostMapping
    @Operation(summary = "预约接种")
    public ApiResponse<AppointmentResponse> book(@RequestBody @Valid AppointmentBookRequest req) {
        return ApiResponse.success(appointmentApplicationService.book(req));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消预约")
    public ApiResponse<Void> cancel(@PathVariable Long id,
                                    @RequestBody @Valid AppointmentCancelRequest req) {
        appointmentApplicationService.cancel(id, req);
        return ApiResponse.success();
    }

    @GetMapping
    @Operation(summary = "查询当前用户预约列表")
    public ApiResponse<List<AppointmentResponse>> findByUserId(
            @RequestParam(required = false) Integer status) {
        return ApiResponse.success(appointmentApplicationService.findByUserId(status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询预约详情")
    public ApiResponse<AppointmentDetailResponse> findById(@PathVariable Long id) {
        return ApiResponse.success(appointmentApplicationService.findById(id));
    }

    @GetMapping("/by-date")
    @Operation(summary = "按日期查询预约列表（工作人员）")
    public ApiResponse<List<AppointmentDetailResponse>> findByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.success(appointmentApplicationService.findByDate(date));
    }

    @GetMapping("/slot-availability")
    @Operation(summary = "查询时段剩余名额")
    public ApiResponse<Map<String, Integer>> getSlotAvailability(
            @RequestParam Long vaccineId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.success(appointmentApplicationService.getSlotAvailability(vaccineId, date));
    }

    @GetMapping("/{id}/guide")
    @Operation(summary = "获取预约流程指引")
    public ApiResponse<AppointmentDetailResponse> findGuide(@PathVariable Long id) {
        return ApiResponse.success(appointmentApplicationService.findGuide(id));
    }

    @GetMapping("/{id}/queue")
    @Operation(summary = "获取预约排队信息")
    public ApiResponse<com.tjut.edu.vaccine.application.dto.response.QueueItemResponse> findQueue(@PathVariable Long id) {
        return ApiResponse.success(appointmentApplicationService.findAppointmentQueue(id));
    }

    @GetMapping("/my-stats")
    @Operation(summary = "获取今日概览统计")
    public ApiResponse<Map<String, Integer>> getMyTodayStats() {
        return ApiResponse.success(appointmentApplicationService.getMyTodayStats());
    }
}
