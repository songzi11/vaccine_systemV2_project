package com.tjut.edu.vaccine.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tjut.edu.vaccine.common.enums.AlertType;
import com.tjut.edu.vaccine.domain.stock.entity.StockAlertLog;
import com.tjut.edu.vaccine.domain.stock.repository.StockAlertLogRepository;
import com.tjut.edu.vaccine.infrastructure.persistence.mapper.StockAlertLogMapper;
import com.tjut.edu.vaccine.infrastructure.persistence.po.StockAlertLogPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class StockAlertLogRepositoryImpl implements StockAlertLogRepository {

    private final StockAlertLogMapper stockAlertLogMapper;

    @Override
    public void save(StockAlertLog alertLog) {
        stockAlertLogMapper.insert(toPO(alertLog));
    }

    @Override
    public boolean existsUnhandledByBatchIdAndType(Long batchId, String type) {
        return stockAlertLogMapper.selectCount(
            new LambdaQueryWrapper<StockAlertLogPO>()
                .eq(StockAlertLogPO::getBatchId, batchId)
                .eq(StockAlertLogPO::getAlertType, type)
                .eq(StockAlertLogPO::getIsHandled, 0)) > 0;
    }

    @Override
    public List<StockAlertLog> findUnhandled(int page, int size) {
        List<StockAlertLogPO> list = stockAlertLogMapper.selectList(
            new LambdaQueryWrapper<StockAlertLogPO>()
                .eq(StockAlertLogPO::getIsHandled, 0)
                .orderByDesc(StockAlertLogPO::getCreateTime)
                .last("LIMIT " + size + " OFFSET " + (page - 1) * size));
        return list.stream().map(this::toDomain).toList();
    }

    @Override
    public void markHandled(Long id) {
        StockAlertLogPO po = new StockAlertLogPO();
        po.setId(id);
        po.setIsHandled(1);
        stockAlertLogMapper.updateById(po);
    }

    private StockAlertLog toDomain(StockAlertLogPO po) {
        StockAlertLog log = new StockAlertLog();
        log.setId(po.getId());
        log.setAlertType(po.getAlertType() != null ? AlertType.fromCode(po.getAlertType()) : null);
        log.setVaccineId(po.getVaccineId());
        log.setBatchId(po.getBatchId());
        log.setAlertValue(po.getAlertValue());
        log.setExpiryDate(po.getExpiryDate());
        log.setHandled(po.getIsHandled() != null && po.getIsHandled() == 1);
        log.setCreateTime(po.getCreateTime());
        return log;
    }

    private StockAlertLogPO toPO(StockAlertLog log) {
        StockAlertLogPO po = new StockAlertLogPO();
        po.setId(log.getId());
        po.setAlertType(log.getAlertType() != null ? log.getAlertType().getCode() : null);
        po.setVaccineId(log.getVaccineId());
        po.setBatchId(log.getBatchId());
        po.setAlertValue(log.getAlertValue());
        po.setExpiryDate(log.getExpiryDate());
        po.setIsHandled(log.isHandled() ? 1 : 0);
        po.setCreateTime(log.getCreateTime());
        return po;
    }
}
