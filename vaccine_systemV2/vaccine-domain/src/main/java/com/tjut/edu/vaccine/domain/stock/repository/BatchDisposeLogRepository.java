package com.tjut.edu.vaccine.domain.stock.repository;

import com.tjut.edu.vaccine.domain.stock.entity.BatchDisposeLog;

public interface BatchDisposeLogRepository {

    void save(BatchDisposeLog log);

    String generateDisposeNo();
}
