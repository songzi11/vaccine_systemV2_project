package com.tjut.edu.vaccine.adapter.web.user;

import com.tjut.edu.vaccine.application.dto.request.ChildCreateRequest;
import com.tjut.edu.vaccine.application.dto.request.ChildUpdateRequest;
import com.tjut.edu.vaccine.application.dto.response.ChildResponse;
import com.tjut.edu.vaccine.application.service.ChildProfileApplicationService;
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
@RequestMapping("/api/v1/user/children")
@RequiredArgsConstructor
@Tag(name = "儿童档案管理")
public class ChildProfileController {

    private final ChildProfileApplicationService childProfileApplicationService;

    @PostMapping
    @Operation(summary = "添加儿童档案")
    public ApiResponse<ChildResponse> create(@RequestBody @Valid ChildCreateRequest req) {
        return ApiResponse.success(childProfileApplicationService.create(req));
    }

    @PutMapping("/{childId}")
    @Operation(summary = "更新儿童档案")
    public ApiResponse<ChildResponse> update(@PathVariable Long childId,
                                             @RequestBody @Valid ChildUpdateRequest req) {
        return ApiResponse.success(childProfileApplicationService.update(childId, req));
    }

    @GetMapping("/{childId}")
    @Operation(summary = "查询儿童档案详情")
    public ApiResponse<ChildResponse> findById(@PathVariable Long childId) {
        return ApiResponse.success(childProfileApplicationService.findById(childId));
    }

    @GetMapping
    @Operation(summary = "查询当前用户所有儿童档案")
    public ApiResponse<List<ChildResponse>> findByParentId() {
        return ApiResponse.success(childProfileApplicationService.findByParentId());
    }

    @DeleteMapping("/{childId}")
    @Operation(summary = "删除儿童档案")
    public ApiResponse<Void> deleteById(@PathVariable Long childId) {
        childProfileApplicationService.deleteById(childId);
        return ApiResponse.success();
    }
}
