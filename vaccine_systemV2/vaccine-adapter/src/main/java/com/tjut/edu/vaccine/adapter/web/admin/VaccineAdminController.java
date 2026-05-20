package com.tjut.edu.vaccine.adapter.web.admin;

import com.tjut.edu.vaccine.application.dto.request.VaccineCreateRequest;
import com.tjut.edu.vaccine.application.dto.request.VaccineUpdateRequest;
import com.tjut.edu.vaccine.application.dto.response.VaccinePublicResponse;
import com.tjut.edu.vaccine.application.service.VaccineAdminService;
import com.tjut.edu.vaccine.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/vaccines")
@RequiredArgsConstructor
@Tag(name = "疫苗管理（管理员）")
public class VaccineAdminController {

    private final VaccineAdminService vaccineAdminService;

    @GetMapping
    @Operation(summary = "查询疫苗列表（支持id参数查单个）")
    public ApiResponse<List<VaccinePublicResponse>> listVaccines(
            @RequestParam(required = false) Long id) {
        return ApiResponse.success(vaccineAdminService.listVaccines(id));
    }

    @PostMapping
    @Operation(summary = "创建疫苗")
    public ApiResponse<VaccinePublicResponse> createVaccine(
            @RequestBody VaccineCreateRequest req) {
        return ApiResponse.success(vaccineAdminService.createVaccine(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新疫苗")
    public ApiResponse<VaccinePublicResponse> updateVaccine(
            @PathVariable Long id,
            @RequestBody VaccineUpdateRequest req) {
        return ApiResponse.success(vaccineAdminService.updateVaccine(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除疫苗")
    public ApiResponse<Void> deleteVaccine(@PathVariable Long id) {
        vaccineAdminService.deleteVaccine(id);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/shelf-status")
    @Operation(summary = "切换疫苗上下架状态")
    public ApiResponse<VaccinePublicResponse> toggleShelfStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        return ApiResponse.success(vaccineAdminService.updateShelfStatus(id, status));
    }
}
