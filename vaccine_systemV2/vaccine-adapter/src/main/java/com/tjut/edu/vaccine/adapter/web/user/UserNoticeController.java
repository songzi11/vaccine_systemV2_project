package com.tjut.edu.vaccine.adapter.web.user;

import com.tjut.edu.vaccine.application.dto.response.NoticeResponse;
import com.tjut.edu.vaccine.application.service.AdminApplicationService;
import com.tjut.edu.vaccine.common.response.ApiResponse;
import com.tjut.edu.vaccine.infrastructure.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user/notices")
@RequiredArgsConstructor
@Tag(name = "公告查看")
public class UserNoticeController {

    private final AdminApplicationService adminApplicationService;

    @GetMapping
    @Operation(summary = "查看已发布公告")
    public ApiResponse<List<NoticeResponse>> findPublishedNotices() {
        return ApiResponse.success(adminApplicationService.findPublishedNotices(UserContext.getRoles()));
    }
}
