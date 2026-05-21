package com.tjut.edu.vaccine.application.service;

import com.tjut.edu.vaccine.application.assembler.StockAssembler;
import com.tjut.edu.vaccine.application.dto.request.BatchCreateRequest;
import com.tjut.edu.vaccine.application.dto.request.BatchDisposeRequest;
import com.tjut.edu.vaccine.application.dto.request.StockTransferRequest;
import com.tjut.edu.vaccine.application.dto.response.HospitalStockResponse;
import com.tjut.edu.vaccine.application.dto.response.StockAlertResponse;
import com.tjut.edu.vaccine.application.dto.response.StockSummaryResponse;
import com.tjut.edu.vaccine.application.dto.response.StockTransferResponse;
import com.tjut.edu.vaccine.application.dto.response.VaccineBatchResponse;
import com.tjut.edu.vaccine.common.enums.AlertType;
import com.tjut.edu.vaccine.common.enums.BatchStatus;
import com.tjut.edu.vaccine.common.enums.ErrorCode;
import com.tjut.edu.vaccine.common.exception.BusinessException;
import com.tjut.edu.vaccine.domain.stock.aggregate.VaccineBatch;
import com.tjut.edu.vaccine.domain.stock.entity.BatchDisposeLog;
import com.tjut.edu.vaccine.domain.stock.entity.HospitalVaccineStock;
import com.tjut.edu.vaccine.domain.stock.entity.StockAlertLog;
import com.tjut.edu.vaccine.domain.stock.entity.StockTransferLog;
import com.tjut.edu.vaccine.domain.identity.aggregate.User;
import com.tjut.edu.vaccine.domain.stock.repository.BatchDisposeLogRepository;
import com.tjut.edu.vaccine.domain.stock.repository.StockAlertLogRepository;
import com.tjut.edu.vaccine.domain.stock.repository.StockTransferLogRepository;
import com.tjut.edu.vaccine.domain.stock.repository.VaccineBatchRepository;
import com.tjut.edu.vaccine.domain.stock.repository.VaccineRepository;
import com.tjut.edu.vaccine.domain.stock.repository.VaccineStockRepository;
import com.tjut.edu.vaccine.domain.identity.repository.UserRepository;
import com.tjut.edu.vaccine.domain.port.SecurityContextPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockApplicationService {

    @Value("${vaccine.default-hospital-id:1}")
    private Long defaultHospitalId;

    private final VaccineRepository vaccineRepository;
    private final VaccineBatchRepository vaccineBatchRepository;
    private final VaccineStockRepository vaccineStockRepository;
    private final StockAlertLogRepository stockAlertLogRepository;
    private final StockTransferLogRepository stockTransferLogRepository;
    private final BatchDisposeLogRepository batchDisposeLogRepository;
    private final UserRepository userRepository;
    private final SecurityContextPort securityContextPort;

    @Transactional(readOnly = true)
    public List<HospitalStockResponse> findHospitalStock() {
        List<HospitalVaccineStock> stocks = vaccineStockRepository.findAllWithStock();
        List<HospitalStockResponse> responses = new ArrayList<>();
        for (HospitalVaccineStock stock : stocks) {
            Optional<VaccineBatch> batchOpt = vaccineBatchRepository.findById(stock.getBatchId());
            if (batchOpt.isEmpty()) {
                continue;
            }
            VaccineBatch batch = batchOpt.get();
            Optional<com.tjut.edu.vaccine.domain.stock.entity.Vaccine> vaccineOpt = vaccineRepository.findById(batch.getVaccineId());
            String vaccineName = vaccineOpt.map(v -> v.getVaccineName()).orElse(null);
            String vaccineType = vaccineOpt.map(v -> v.getVaccineType()).orElse(null);
            String status = batch.getStatus() != null ? batch.getStatus().getDescription() : null;
            responses.add(StockAssembler.toStockResponse(stock, batch.getVaccineId(), vaccineName, vaccineType, batch.getBatchNo(), status));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public StockSummaryResponse findStockSummary() {
        List<HospitalStockResponse> stocks = findHospitalStock();

        // 按疫苗ID聚合
        Map<Long, List<HospitalStockResponse>> grouped = stocks.stream()
                .filter(s -> s.getVaccineId() != null)
                .collect(Collectors.groupingBy(HospitalStockResponse::getVaccineId));

        List<HospitalStockResponse> vaccines = new ArrayList<>();
        for (Map.Entry<Long, List<HospitalStockResponse>> entry : grouped.entrySet()) {
            List<HospitalStockResponse> items = entry.getValue();
            HospitalStockResponse aggregated = new HospitalStockResponse();
            aggregated.setVaccineId(entry.getKey());
            aggregated.setVaccineName(items.get(0).getVaccineName());
            aggregated.setVaccineType(items.get(0).getVaccineType());
            int total = items.stream().mapToInt(s -> s.getTotalStock() != null ? s.getTotalStock() : 0).sum();
            int totalAvailable = items.stream().mapToInt(s -> s.getAvailableStock() != null ? s.getAvailableStock() : 0).sum();
            int totalLocked = items.stream().mapToInt(s -> s.getLockedStock() != null ? s.getLockedStock() : 0).sum();
            aggregated.setAvailableStock(totalAvailable);
            aggregated.setLockedStock(totalLocked);
            aggregated.setTotalStock(total);
            vaccines.add(aggregated);
        }

        // 统计预警和过期
        List<StockAlertLog> alerts = stockAlertLogRepository.findUnhandled(1, 1000);
        int alertCount = (int) alerts.stream().filter(a -> !a.isHandled()).count();
        int expiredCount = (int) alerts.stream()
                .filter(a -> a.getAlertType() == AlertType.EXPIRED)
                .count();

        StockSummaryResponse summary = new StockSummaryResponse();
        summary.setTotalVaccines(vaccines.size());
        summary.setAlertCount(alertCount);
        summary.setExpiredCount(expiredCount);
        summary.setTodayTransfers(0);
        summary.setVaccines(vaccines);
        return summary;
    }

    @Transactional(readOnly = true)
    public List<VaccineBatchResponse> findBatches(String status, Long vaccineId, String keyword) {
        List<VaccineBatch> batches;
        if (status != null || vaccineId != null || (keyword != null && !keyword.isEmpty())) {
            batches = vaccineBatchRepository.findByFilter(status, vaccineId, keyword);
        } else {
            batches = vaccineBatchRepository.findAllNormal();
        }
        return batches.stream().map(this::enrichBatchResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VaccineBatchResponse findBatchDetail(Long batchId) {
        VaccineBatch batch = vaccineBatchRepository.findById(batchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STOCK_BATCH_NOT_FOUND));
        return enrichBatchResponse(batch);
    }

    private VaccineBatchResponse enrichBatchResponse(VaccineBatch batch) {
        VaccineBatchResponse resp = StockAssembler.toBatchResponse(batch);
        // 补充疫苗名称和类型
        vaccineRepository.findById(batch.getVaccineId()).ifPresent(v -> {
            resp.setVaccineName(v.getVaccineName());
            resp.setVaccineType(v.getVaccineType());
        });
        // 补充库存信息
        vaccineStockRepository.findByBatchId(batch.getId()).ifPresent(stock -> {
            resp.setAvailableStock(stock.getAvailableStock());
            resp.setLockedStock(stock.getLockedStock());
            resp.setTotalStock(stock.getTotalStock());
        });
        return resp;
    }

    @Transactional
    public VaccineBatchResponse createBatch(BatchCreateRequest req) {
        // 1. 验证疫苗存在
        vaccineRepository.findById(req.getVaccineId())
                .orElseThrow(() -> new BusinessException(ErrorCode.VACCINE_NOT_FOUND));

        // 2. 创建批次
        VaccineBatch batch = new VaccineBatch();
        batch.setBatchNo(req.getBatchNo());
        batch.setVaccineId(req.getVaccineId());
        batch.setManufacturer(req.getManufacturer());
        batch.setProductionDate(req.getProductionDate());
        batch.setExpiryDate(req.getExpiryDate());
        batch.setWarningDays(req.getWarningDays() != null ? req.getWarningDays() : 30);
        vaccineBatchRepository.save(batch);

        // 3. 创建库存记录
        HospitalVaccineStock stock = new HospitalVaccineStock(defaultHospitalId, batch.getId(), req.getQuantity());
        vaccineStockRepository.save(stock);

        log.info("批次入库成功: batchNo={}, vaccineId={}, quantity={}",
                req.getBatchNo(), req.getVaccineId(), req.getQuantity());

        return StockAssembler.toBatchResponse(batch);
    }

    @Transactional
    public void transfer(StockTransferRequest req) {
        Long operatorId = securityContextPort.getCurrentUserId();

        // 1. 验证批次
        VaccineBatch batch = vaccineBatchRepository.findByIdForUpdate(req.getBatchId());
        if (batch == null) {
            throw new BusinessException(ErrorCode.STOCK_BATCH_NOT_FOUND);
        }
        if (batch.isExpired()) {
            throw new BusinessException(ErrorCode.STOCK_BATCH_DISPOSED);
        }

        // 2. 验证来源!=目标
        if (req.getFromType().equals(req.getToType()) && req.getFromId().equals(req.getToId())) {
            throw new BusinessException(ErrorCode.STOCK_TRANSFER_SAME_LOCATION);
        }

        // 3. 查找来源库存并扣减
        HospitalVaccineStock fromStock = vaccineStockRepository
                .findByLocation(req.getBatchId(), req.getFromType(), req.getFromId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STOCK_TRANSFER_INSUFFICIENT));

        if (fromStock.getAvailableStock() < req.getQuantity()) {
            throw new BusinessException(ErrorCode.STOCK_TRANSFER_INSUFFICIENT);
        }

        // 4. 查找或创建目标库存
        HospitalVaccineStock toStock = vaccineStockRepository
                .findByLocation(req.getBatchId(), req.getToType(), req.getToId())
                .orElse(null);

        if (toStock == null) {
            HospitalVaccineStock newStock = new HospitalVaccineStock(
                    defaultHospitalId, req.getBatchId(), req.getToType(), req.getToId(), 0);
            vaccineStockRepository.save(newStock);
            toStock = vaccineStockRepository
                    .findByLocation(req.getBatchId(), req.getToType(), req.getToId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.STOCK_TRANSFER_FAILED));
        }

        // 5. 来源扣减 + 目标增加（按行ID精确操作）
        try {
            vaccineStockRepository.deductStockById(fromStock.getId(), req.getQuantity());
            vaccineStockRepository.addStockById(toStock.getId(), req.getQuantity());
        } catch (RuntimeException e) {
            log.error("库存调拨失败: batchId={}, quantity={}", req.getBatchId(), req.getQuantity(), e);
            throw new BusinessException(ErrorCode.STOCK_TRANSFER_FAILED);
        }

        // 6. 生成调拨编号并记录日志
        String transferNo = stockTransferLogRepository.generateTransferNo();
        StockTransferLog transferLog = new StockTransferLog(
                transferNo, req.getBatchId(), req.getFromType(), req.getFromId(),
                req.getToType(), req.getToId(), req.getQuantity(), operatorId, req.getRemark());
        stockTransferLogRepository.save(transferLog);

        log.info("库存调拨成功: transferNo={}, batchId={}, from={}/{}, to={}/{}, quantity={}",
                transferNo, req.getBatchId(), req.getFromType(), req.getFromId(),
                req.getToType(), req.getToId(), req.getQuantity());
    }

    @Transactional
    public void dispose(Long batchId, BatchDisposeRequest req) {
        Long operatorId = securityContextPort.getCurrentUserId();

        // 1. 验证批次
        VaccineBatch batch = vaccineBatchRepository.findByIdForUpdate(batchId);
        if (batch == null) {
            throw new BusinessException(ErrorCode.STOCK_BATCH_NOT_FOUND);
        }
        if (batch.getStatus() == BatchStatus.DISPOSED) {
            throw new BusinessException(ErrorCode.STOCK_BATCH_DISPOSED);
        }

        // 2. 生成销毁编号并记录日志
        String disposeNo = batchDisposeLogRepository.generateDisposeNo();
        BatchDisposeLog disposeLog = new BatchDisposeLog(
                disposeNo, batchId, req.getQuantity(), req.getReason(), operatorId, req.getRemark());
        batchDisposeLogRepository.save(disposeLog);

        // 3. 更新批次状态
        batch.dispose();
        vaccineBatchRepository.updateStatus(batch);

        // 4. 清零该批次的所有库存记录
        vaccineStockRepository.zeroStockByBatchId(batchId);

        log.info("批次销毁成功: disposeNo={}, batchId={}, quantity={}", disposeNo, batchId, req.getQuantity());
    }

    @Transactional(readOnly = true)
    public List<StockAlertResponse> findAlerts() {
        List<StockAlertLog> alerts = stockAlertLogRepository.findUnhandled(1, 100);
        return alerts.stream().map(alert -> {
            StockAlertResponse resp = StockAssembler.toAlertResponse(alert);
            // 补充疫苗名称
            vaccineRepository.findById(alert.getVaccineId())
                    .ifPresent(v -> resp.setVaccineName(v.getVaccineName()));
            // 补充批次号
            if (alert.getBatchId() != null) {
                vaccineBatchRepository.findById(alert.getBatchId())
                        .ifPresent(batch -> resp.setBatchNo(batch.getBatchNo()));
            }
            // Build detail text
            String detail = resp.getAlertType() + " - " + resp.getVaccineName();
            if (resp.getBatchNo() != null) {
                detail += " (批次: " + resp.getBatchNo() + ")";
            }
            if (resp.getExpiryDate() != null) {
                detail += ", 到期日: " + resp.getExpiryDate();
            }
            resp.setDetail(detail);
            return resp;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void handleAlert(Long alertId) {
        stockAlertLogRepository.markHandled(alertId);
    }

    @Transactional(readOnly = true)
    public List<StockTransferLog> findTransferRecords(int page, int size) {
        return stockTransferLogRepository.findAll(page, size);
    }

    @Transactional(readOnly = true)
    public List<StockTransferResponse> findTransferRecordsEnriched(int page, int size) {
        List<StockTransferLog> logs = stockTransferLogRepository.findAll(page, size);
        return logs.stream().map(log -> {
            StockTransferResponse resp = new StockTransferResponse();
            resp.setId(log.getId());
            resp.setTransferNo(log.getTransferNo());
            resp.setBatchId(log.getBatchId());
            resp.setFromType(log.getFromType());
            resp.setFromId(log.getFromId());
            resp.setToType(log.getToType());
            resp.setToId(log.getToId());
            resp.setQuantity(log.getQuantity());
            resp.setOperatorId(log.getOperatorId());
            resp.setTransferTime(log.getTransferTime());
            resp.setRemark(log.getRemark());
            resp.setCreateTime(log.getCreateTime());
            vaccineBatchRepository.findById(log.getBatchId()).ifPresent(batch -> {
                resp.setBatchNo(batch.getBatchNo());
            });
            if (log.getOperatorId() != null) {
                User operator = userRepository.findById(log.getOperatorId());
                if (operator != null) {
                    resp.setOperatorName(operator.getRealName());
                }
            }
            resp.setFromLocationName(log.getFromType() == 0 ? "仓库" : "医院-" + log.getFromId());
            resp.setToLocationName(log.getToType() == 0 ? "仓库" : "医院-" + log.getToId());
            return resp;
        }).collect(Collectors.toList());
    }
}
