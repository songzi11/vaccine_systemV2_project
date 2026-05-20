package com.tjut.edu.vaccine.adapter.web.observe;

import com.tjut.edu.vaccine.application.dto.request.AdverseReactionHandleRequest;
import com.tjut.edu.vaccine.application.dto.request.AdverseReactionRequest;
import com.tjut.edu.vaccine.application.dto.request.ObserveFinishRequest;
import com.tjut.edu.vaccine.application.dto.request.ObserveStartRequest;
import com.tjut.edu.vaccine.application.dto.response.AdverseReactionResponse;
import com.tjut.edu.vaccine.application.dto.response.ObserveRecordResponse;
import com.tjut.edu.vaccine.application.dto.response.QueueItemResponse;
import com.tjut.edu.vaccine.application.service.AppointmentApplicationService;
import com.tjut.edu.vaccine.application.service.ObserveApplicationService;
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

@RestController
@RequestMapping("/api/v1/observe")
@RequiredArgsConstructor
@Tag(name = "留观管理")
public class ObserveController {

    private final ObserveApplicationService observeApplicationService;
    private final AppointmentApplicationService appointmentApplicationService;

    @PostMapping("/start")
    @Operation(summary = "开始留观")
    public ApiResponse<ObserveRecordResponse> start(@RequestBody @Valid ObserveStartRequest req) {
        return ApiResponse.success(observeApplicationService.start(req));
    }

    @PutMapping("/{id}/finish")
    @Operation(summary = "完成留观")
    public ApiResponse<ObserveRecordResponse> finish(@PathVariable Long id,
                                                     @RequestBody @Valid ObserveFinishRequest req) {
        return ApiResponse.success(observeApplicationService.finish(id, req));
    }

    @PostMapping("/adverse-reaction")
    @Operation(summary = "上报不良反应")
    public ApiResponse<AdverseReactionResponse> reportAdverseReaction(
            @RequestBody @Valid AdverseReactionRequest req) {
        return ApiResponse.success(observeApplicationService.reportAdverseReaction(req));
    }

    @PutMapping("/adverse-reaction/{id}/handle")
    @Operation(summary = "处理不良反应")
    public ApiResponse<AdverseReactionResponse> handleAdverseReaction(
            @PathVariable Long id,
            @RequestBody @Valid AdverseReactionHandleRequest req) {
        return ApiResponse.success(observeApplicationService.handleAdverseReaction(id, req));
    }

    @GetMapping("/adverse-reaction/{observeRecordId}")
    @Operation(summary = "查询不良反应记录")
    public ApiResponse<List<AdverseReactionResponse>> findAdverseReactions(
            @PathVariable Long observeRecordId) {
        return ApiResponse.success(observeApplicationService.findAdverseReactions(observeRecordId));
    }

    @GetMapping("/queue")
    @Operation(summary = "查询留观队列")
    public ApiResponse<List<QueueItemResponse>> findObserveQueue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.success(appointmentApplicationService.findMyObserveQueue(date != null ? date : java.time.LocalDate.now()));
    }

    @GetMapping("/{injectionId}")
    @Operation(summary = "查询留观详情")
    public ApiResponse<QueueItemResponse> findObserveDetail(@PathVariable Long injectionId) {
        return ApiResponse.success(appointmentApplicationService.findObserveDetail(injectionId));
    }
}
