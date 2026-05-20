package com.tjut.edu.vaccine.adapter.web.vaccinate;

import com.tjut.edu.vaccine.application.dto.request.VaccinateExecuteRequest;
import com.tjut.edu.vaccine.application.dto.response.FEFOBatchResponse;
import com.tjut.edu.vaccine.application.dto.response.QueueItemResponse;
import com.tjut.edu.vaccine.application.dto.response.VaccinateVerifyResponse;
import com.tjut.edu.vaccine.application.dto.response.VaccinationRecordResponse;
import com.tjut.edu.vaccine.application.service.AppointmentApplicationService;
import com.tjut.edu.vaccine.application.service.VaccinateApplicationService;
import com.tjut.edu.vaccine.common.enums.AppointmentStatus;
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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "接种管理")
public class VaccinateController {

    private final VaccinateApplicationService vaccinateApplicationService;
    private final AppointmentApplicationService appointmentApplicationService;

    @PostMapping("/vaccinate/execute")
    @Operation(summary = "执行接种")
    public ApiResponse<VaccinationRecordResponse> execute(@RequestBody @Valid VaccinateExecuteRequest req) {
        return ApiResponse.success(vaccinateApplicationService.execute(req));
    }

    @GetMapping("/user/vaccination-records")
    @Operation(summary = "查询当前用户接种记录")
    public ApiResponse<List<VaccinationRecordResponse>> findRecordsByUserId() {
        return ApiResponse.success(vaccinateApplicationService.findRecordsByUserId());
    }

    @GetMapping("/user/vaccination-records/{id}")
    @Operation(summary = "查询接种记录详情")
    public ApiResponse<VaccinationRecordResponse> findRecordById(@PathVariable Long id) {
        return ApiResponse.success(vaccinateApplicationService.findRecordById(id));
    }

    @GetMapping("/user/children/{childId}/vaccination-records")
    @Operation(summary = "查询儿童接种记录")
    public ApiResponse<List<VaccinationRecordResponse>> findRecordsByChildId(@PathVariable Long childId) {
        return ApiResponse.success(vaccinateApplicationService.findRecordsByChildId(childId));
    }

    @GetMapping("/vaccinate/fefo-batch/{vaccineId}")
    @Operation(summary = "FEFO策略推荐批次（工作人员）")
    public ApiResponse<FEFOBatchResponse> findFEFOBatch(@PathVariable Long vaccineId) {
        return ApiResponse.success(vaccinateApplicationService.findFEFOBatch(vaccineId));
    }

    @GetMapping("/vaccinate/queue")
    @Operation(summary = "查询接种队列")
    public ApiResponse<List<QueueItemResponse>> findVaccinateQueue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String keyword) {
        LocalDate queryDate = date != null ? date : java.time.LocalDate.now();
        List<QueueItemResponse> list = appointmentApplicationService.findQueueByStatus(
                List.of(AppointmentStatus.PRECHECK_PASS.getCode()), queryDate);
        list.forEach(item -> item.setQueueStatus(0)); // 待叫号
        return ApiResponse.success(list);
    }

    @GetMapping("/vaccinate/{appointmentId}/verify")
    @Operation(summary = "接种前验证")
    public ApiResponse<VaccinateVerifyResponse> verify(@PathVariable Long appointmentId) {
        return ApiResponse.success(vaccinateApplicationService.verify(appointmentId));
    }
}
