package com.tjut.edu.vaccine.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tjut.edu.vaccine.domain.stock.aggregate.HospitalVaccineSummary;
import com.tjut.edu.vaccine.domain.stock.repository.HospitalVaccineSummaryRepository;
import com.tjut.edu.vaccine.infrastructure.persistence.converter.HospitalVaccineSummaryConverter;
import com.tjut.edu.vaccine.infrastructure.persistence.mapper.HospitalVaccineSummaryMapper;
import com.tjut.edu.vaccine.infrastructure.persistence.po.HospitalVaccineSummaryPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class HospitalVaccineSummaryRepositoryImpl implements HospitalVaccineSummaryRepository {

    private final HospitalVaccineSummaryMapper summaryMapper;

    @Override
    public Optional<HospitalVaccineSummary> findByVaccineId(Long vaccineId, Long hospitalId) {
        HospitalVaccineSummaryPO po = summaryMapper.selectOne(
            new LambdaQueryWrapper<HospitalVaccineSummaryPO>()
                .eq(HospitalVaccineSummaryPO::getVaccineId, vaccineId)
                .eq(HospitalVaccineSummaryPO::getHospitalId, hospitalId));
        return Optional.ofNullable(po).map(HospitalVaccineSummaryConverter::toDomain);
    }

    @Override
    public void updateWithOptimisticLock(HospitalVaccineSummary summary) {
        int rows = summaryMapper.updateWithOptimisticLock(
            summary.getId(),
            summary.getTotalStock(),
            summary.getAvailableStock(),
            summary.getVersion()
        );
        if (rows == 0) {
            throw new RuntimeException("乐观锁更新失败: vaccineId=" + summary.getVaccineId() + ", version=" + summary.getVersion());
        }
    }
}
