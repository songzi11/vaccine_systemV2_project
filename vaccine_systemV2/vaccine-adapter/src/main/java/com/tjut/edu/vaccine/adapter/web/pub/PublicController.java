package com.tjut.edu.vaccine.adapter.web.pub;

import com.tjut.edu.vaccine.application.dto.response.VaccinePublicResponse;
import com.tjut.edu.vaccine.application.service.VaccinePublicService;
import com.tjut.edu.vaccine.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
@Tag(name = "公开接口")
public class PublicController {

    private final VaccinePublicService vaccinePublicService;

    @GetMapping("/vaccines")
    @Operation(summary = "查询上架疫苗列表（公开）")
    public ApiResponse<List<VaccinePublicResponse>> findVaccines(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long id) {
        return ApiResponse.success(vaccinePublicService.findOnShelf(category, keyword, id));
    }
}
