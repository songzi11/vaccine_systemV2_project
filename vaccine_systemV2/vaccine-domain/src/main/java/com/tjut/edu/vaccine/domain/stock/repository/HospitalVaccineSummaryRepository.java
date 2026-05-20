package com.tjut.edu.vaccine.domain.stock.repository;

import com.tjut.edu.vaccine.domain.stock.aggregate.HospitalVaccineSummary;

import java.util.Optional;

/**
 * 医院疫苗库存汇总仓储接口
 */
public interface HospitalVaccineSummaryRepository {

    Optional<HospitalVaccineSummary> findByVaccineId(Long vaccineId, Long hospitalId);

    void updateWithOptimisticLock(HospitalVaccineSummary summary);
}
