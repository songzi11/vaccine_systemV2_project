package com.tjut.edu.vaccine.adapter.web.precheck;

import com.tjut.edu.vaccine.application.dto.request.PreCheckAssessRequest;
import com.tjut.edu.vaccine.application.dto.request.SigninRequest;
import com.tjut.edu.vaccine.application.dto.response.PreCheckRecordResponse;
import com.tjut.edu.vaccine.application.dto.response.QueueItemResponse;
import com.tjut.edu.vaccine.application.service.AppointmentApplicationService;
import com.tjut.edu.vaccine.application.service.PreCheckApplicationService;
import com.tjut.edu.vaccine.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/precheck")
@RequiredArgsConstructor
@Tag(name = "预检管理")
public class PreCheckController {

    private final PreCheckApplicationService preCheckApplicationService;
    private final AppointmentApplicationService appointmentApplicationService;

    @PostMapping("/signin")
    @Operation(summary = "签到（确认家长到场）")
    public ApiResponse<QueueItemResponse> signin(@RequestBody @Valid SigninRequest req) {
        return ApiResponse.success(appointmentApplicationService.signin(req));
    }

    @PostMapping("/assess")
    @Operation(summary = "预检评估")
    public ApiResponse<PreCheckRecordResponse> assess(@RequestBody @Valid PreCheckAssessRequest req) {
        return ApiResponse.success(preCheckApplicationService.assess(req));
    }

    @GetMapping("/records/{appointmentId}")
    @Operation(summary = "查询预检记录")
    public ApiResponse<PreCheckRecordResponse> findByAppointmentId(@PathVariable Long appointmentId) {
        return ApiResponse.success(preCheckApplicationService.findByAppointmentId(appointmentId));
    }

    @GetMapping("/queue")
    @Operation(summary = "查询预检队列")
    public ApiResponse<List<QueueItemResponse>> findQueue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(appointmentApplicationService.findMyPreCheckQueue(
                date != null ? date : LocalDate.now()));
    }
}
