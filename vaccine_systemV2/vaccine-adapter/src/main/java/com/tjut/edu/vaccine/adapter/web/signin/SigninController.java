package com.tjut.edu.vaccine.adapter.web.signin;

import com.tjut.edu.vaccine.application.dto.request.SigninRequest;
import com.tjut.edu.vaccine.application.dto.response.QueueItemResponse;
import com.tjut.edu.vaccine.application.service.AppointmentApplicationService;
import com.tjut.edu.vaccine.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/signin")
@RequiredArgsConstructor
@Tag(name = "签到管理")
public class SigninController {

    private final AppointmentApplicationService appointmentApplicationService;

    @PostMapping("/execute")
    @Operation(summary = "执行签到")
    public ApiResponse<QueueItemResponse> signin(@RequestBody @Valid SigninRequest req) {
        return ApiResponse.success(appointmentApplicationService.signin(req));
    }
}
