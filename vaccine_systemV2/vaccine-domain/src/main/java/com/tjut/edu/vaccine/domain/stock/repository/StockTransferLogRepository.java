package com.tjut.edu.vaccine.domain.stock.repository;

import com.tjut.edu.vaccine.domain.stock.entity.StockTransferLog;

import java.util.List;

public interface StockTransferLogRepository {

    void save(StockTransferLog log);

    String generateTransferNo();

    List<StockTransferLog> findAll(int page, int size);
}
