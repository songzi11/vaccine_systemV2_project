package com.tjut.edu.vaccine.adapter.web.stock;

import com.tjut.edu.vaccine.application.dto.request.BatchCreateRequest;
import com.tjut.edu.vaccine.application.dto.request.BatchDisposeRequest;
import com.tjut.edu.vaccine.application.dto.request.StockTransferRequest;
import com.tjut.edu.vaccine.application.dto.response.StockAlertResponse;
import com.tjut.edu.vaccine.application.dto.response.StockTransferResponse;
import com.tjut.edu.vaccine.application.dto.response.StockSummaryResponse;
import com.tjut.edu.vaccine.application.dto.response.VaccineBatchResponse;
import com.tjut.edu.vaccine.application.service.StockApplicationService;
import com.tjut.edu.vaccine.common.response.ApiResponse;
import com.tjut.edu.vaccine.domain.stock.entity.StockTransferLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
@Tag(name = "库存管理")
public class StockController {

    private final StockApplicationService stockApplicationService;

    @GetMapping("/hospital")
    @Operation(summary = "查看医院库存总览")
    public ApiResponse<StockSummaryResponse> findHospitalStock() {
        return ApiResponse.success(stockApplicationService.findStockSummary());
    }

    @GetMapping("/batches")
    @Operation(summary = "查看批次列表")
    public ApiResponse<List<VaccineBatchResponse>> findBatches(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long vaccineId,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(stockApplicationService.findBatches(status, vaccineId, keyword));
    }

    @GetMapping("/batches/{id}")
    @Operation(summary = "查看批次详情")
    public ApiResponse<VaccineBatchResponse> findBatchDetail(@PathVariable Long id) {
        return ApiResponse.success(stockApplicationService.findBatchDetail(id));
    }

    @PostMapping("/batches")
    @Operation(summary = "新建批次入库")
    public ApiResponse<VaccineBatchResponse> createBatch(@RequestBody @Valid BatchCreateRequest req) {
        return ApiResponse.success(stockApplicationService.createBatch(req));
    }

    @PostMapping("/transfer")
    @Operation(summary = "库存调拨")
    public ApiResponse<Void> transfer(@RequestBody @Valid StockTransferRequest req) {
        stockApplicationService.transfer(req);
        return ApiResponse.success();
    }

    @GetMapping("/transfer/records")
    @Operation(summary = "查看调拨记录")
    public ApiResponse<List<StockTransferResponse>> findTransferRecords(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        int p = page != null ? page : 1;
        int s = size != null ? size : 100;
        return ApiResponse.success(stockApplicationService.findTransferRecordsEnriched(p, s));
    }

    @PostMapping("/batches/{id}/dispose")
    @Operation(summary = "批次销毁")
    public ApiResponse<Void> dispose(@PathVariable Long id,
                                     @RequestBody @Valid BatchDisposeRequest req) {
        stockApplicationService.dispose(id, req);
        return ApiResponse.success();
    }

    @GetMapping("/alerts")
    @Operation(summary = "查看库存预警")
    public ApiResponse<List<StockAlertResponse>> findAlerts() {
        return ApiResponse.success(stockApplicationService.findAlerts());
    }

    @PutMapping("/alerts/{id}/handle")
    @Operation(summary = "标记预警已处理")
    public ApiResponse<Void> handleAlert(@PathVariable Long id) {
        stockApplicationService.handleAlert(id);
        return ApiResponse.success();
    }
}
