package com.tjut.edu.vaccine.domain.stock.repository;

import com.tjut.edu.vaccine.domain.stock.entity.HospitalVaccineStock;

import java.util.List;
import java.util.Optional;

/**
 * 医院疫苗库存仓储接口
 */
public interface VaccineStockRepository {

    Optional<HospitalVaccineStock> findByBatchId(Long batchId);

    Optional<HospitalVaccineStock> findByBatchIdForUpdate(Long batchId);

    void lockStock(Long batchId, Long hospitalId);

    void deductStock(Long batchId, Long hospitalId);

    void deductStock(Long batchId, int quantity);

    void releaseStock(Long batchId, Long hospitalId);

    void releaseStock(Long batchId, int quantity);

    void addStock(Long batchId, int quantity);

    void deductStockById(Long id, int quantity);

    void addStockById(Long id, int quantity);

    void zeroStockByBatchId(Long batchId);

    Optional<HospitalVaccineStock> findByLocation(Long batchId, Integer locationType, Long locationId);

    void save(HospitalVaccineStock stock);

    List<HospitalVaccineStock> findAllAvailable(Long vaccineId);

    List<HospitalVaccineStock> findAllWithStock();

    int sumTotalByVaccine(Long vaccineId);

    int sumAvailableByVaccine(Long vaccineId);
}
