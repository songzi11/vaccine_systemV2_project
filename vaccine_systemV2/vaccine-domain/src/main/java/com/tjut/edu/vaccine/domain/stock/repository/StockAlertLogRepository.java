package com.tjut.edu.vaccine.domain.stock.repository;

import com.tjut.edu.vaccine.domain.stock.entity.StockAlertLog;

import java.util.List;

/**
 * 库存预警仓储接口
 */
public interface StockAlertLogRepository {

    void save(StockAlertLog stockAlertLog);

    boolean existsUnhandledByBatchIdAndType(Long batchId, String type);

    List<StockAlertLog> findUnhandled(int page, int size);

    void markHandled(Long id);
}
