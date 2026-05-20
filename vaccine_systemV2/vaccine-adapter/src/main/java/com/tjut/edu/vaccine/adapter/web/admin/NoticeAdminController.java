package com.tjut.edu.vaccine.adapter.web.admin;

import com.tjut.edu.vaccine.application.dto.request.NoticeCreateRequest;
import com.tjut.edu.vaccine.application.dto.request.NoticeUpdateRequest;
import com.tjut.edu.vaccine.application.dto.response.NoticeResponse;
import com.tjut.edu.vaccine.application.service.AdminApplicationService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/notices")
@RequiredArgsConstructor
@Tag(name = "公告管理（管理员）")
public class NoticeAdminController {

    private final AdminApplicationService adminApplicationService;

    @PostMapping
    @Operation(summary = "创建公告")
    public ApiResponse<NoticeResponse> createNotice(@RequestBody @Valid NoticeCreateRequest req) {
        return ApiResponse.success(adminApplicationService.createNotice(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新公告")
    public ApiResponse<NoticeResponse> updateNotice(@PathVariable Long id,
                                                     @RequestBody @Valid NoticeUpdateRequest req) {
        return ApiResponse.success(adminApplicationService.updateNotice(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除公告")
    public ApiResponse<Void> deleteNotice(@PathVariable Long id) {
        adminApplicationService.deleteNotice(id);
        return ApiResponse.success();
    }

    @GetMapping
    @Operation(summary = "查询所有公告")
    public ApiResponse<List<NoticeResponse>> findAllNotices() {
        return ApiResponse.success(adminApplicationService.findAllNotices());
    }
}
