package com.tjut.edu.vaccine.domain.stock.repository;

import com.tjut.edu.vaccine.domain.stock.aggregate.VaccineBatch;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 疫苗批次仓储接口
 */
public interface VaccineBatchRepository {

    Optional<VaccineBatch> findById(Long id);

    VaccineBatch findByIdForUpdate(Long id);

    /**
     * 按FEFO策略查找可用批次(先过期先出)
     */
    VaccineBatch findAvailableForFEFO(Long vaccineId, Long hospitalId);

    List<VaccineBatch> findAvailableBatches(Long vaccineId);

    List<VaccineBatch> findAllNormal();

    List<VaccineBatch> findByFilter(String status, Long vaccineId, String keyword);

    void save(VaccineBatch vaccineBatch);

    void updateStatus(VaccineBatch vaccineBatch);

    void markNearExpiry(List<Long> ids);

    void markExpired(List<Long> ids);

    List<VaccineBatch> findNearExpiry(LocalDate warningDate);

    List<VaccineBatch> findExpired(LocalDate today);
}
