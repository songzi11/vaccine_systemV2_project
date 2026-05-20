package com.tjut.edu.vaccine.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.tjut.edu.vaccine.common.enums.BatchStatus;
import com.tjut.edu.vaccine.domain.stock.aggregate.VaccineBatch;
import com.tjut.edu.vaccine.domain.stock.repository.VaccineBatchRepository;
import com.tjut.edu.vaccine.infrastructure.persistence.converter.VaccineBatchConverter;
import com.tjut.edu.vaccine.infrastructure.persistence.mapper.VaccineBatchMapper;
import com.tjut.edu.vaccine.infrastructure.persistence.po.VaccineBatchPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class VaccineBatchRepositoryImpl implements VaccineBatchRepository {

    private final VaccineBatchMapper vaccineBatchMapper;

    @Override
    public Optional<VaccineBatch> findById(Long id) {
        VaccineBatchPO po = vaccineBatchMapper.selectById(id);
        return Optional.ofNullable(po).map(VaccineBatchConverter::toDomain);
    }

    @Override
    public VaccineBatch findByIdForUpdate(Long id) {
        VaccineBatchPO po = vaccineBatchMapper.selectByIdForUpdate(id);
        return po != null ? VaccineBatchConverter.toDomain(po) : null;
    }

    @Override
    public VaccineBatch findAvailableForFEFO(Long vaccineId, Long hospitalId) {
        VaccineBatchPO po = vaccineBatchMapper.selectAvailableForFEFO(vaccineId, hospitalId);
        return po != null ? VaccineBatchConverter.toDomain(po) : null;
    }

    @Override
    public List<VaccineBatch> findAvailableBatches(Long vaccineId) {
        List<VaccineBatchPO> list = vaccineBatchMapper.selectList(
            new LambdaQueryWrapper<VaccineBatchPO>()
                .eq(VaccineBatchPO::getVaccineId, vaccineId)
                .eq(VaccineBatchPO::getStatus, BatchStatus.NORMAL.getCode())
                .gt(VaccineBatchPO::getExpiryDate, java.time.LocalDate.now())
                .orderByAsc(VaccineBatchPO::getExpiryDate));
        return list.stream().map(VaccineBatchConverter::toDomain).toList();
    }

    @Override
    public List<VaccineBatch> findAllNormal() {
        List<VaccineBatchPO> list = vaccineBatchMapper.selectList(
            new LambdaQueryWrapper<VaccineBatchPO>()
                .eq(VaccineBatchPO::getStatus, BatchStatus.NORMAL.getCode())
                .gt(VaccineBatchPO::getExpiryDate, java.time.LocalDate.now())
                .orderByAsc(VaccineBatchPO::getExpiryDate));
        return list.stream().map(VaccineBatchConverter::toDomain).toList();
    }

    @Override
    public List<VaccineBatch> findByFilter(String status, Long vaccineId, String keyword) {
        LambdaQueryWrapper<VaccineBatchPO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(status)) {
            try {
                BatchStatus batchStatus = BatchStatus.valueOf(status);
                wrapper.eq(VaccineBatchPO::getStatus, batchStatus.getCode());
            } catch (IllegalArgumentException e) {
                // 尝试按数字code匹配
                try {
                    int code = Integer.parseInt(status);
                    wrapper.eq(VaccineBatchPO::getStatus, code);
                } catch (NumberFormatException ignored) {
                    // 无效的status参数，忽略过滤条件
                }
            }
        }
        if (vaccineId != null) {
            wrapper.eq(VaccineBatchPO::getVaccineId, vaccineId);
        }
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.like(VaccineBatchPO::getBatchNo, keyword);
        }
        wrapper.orderByAsc(VaccineBatchPO::getExpiryDate);
        List<VaccineBatchPO> list = vaccineBatchMapper.selectList(wrapper);
        return list.stream().map(VaccineBatchConverter::toDomain).toList();
    }

    @Override
    public void save(VaccineBatch batch) {
        VaccineBatchPO po = VaccineBatchConverter.toPO(batch);
        vaccineBatchMapper.insert(po);
        batch.setId(po.getId());
    }

    @Override
    public void updateStatus(VaccineBatch batch) {
        VaccineBatchPO po = new VaccineBatchPO();
        po.setId(batch.getId());
        po.setStatus(batch.getStatus().getCode());
        vaccineBatchMapper.updateById(po);
    }

    @Override
    public void markNearExpiry(List<Long> ids) {
        ids.forEach(id -> {
            VaccineBatchPO po = new VaccineBatchPO();
            po.setId(id);
            po.setStatus(BatchStatus.NEAR_EXPIRY.getCode());
            vaccineBatchMapper.updateById(po);
        });
    }

    @Override
    public void markExpired(List<Long> ids) {
        ids.forEach(id -> {
            VaccineBatchPO po = new VaccineBatchPO();
            po.setId(id);
            po.setStatus(BatchStatus.EXPIRED.getCode());
            vaccineBatchMapper.updateById(po);
        });
    }

    @Override
    public List<VaccineBatch> findNearExpiry(LocalDate warningDate) {
        List<VaccineBatchPO> list = vaccineBatchMapper.selectList(
            new LambdaQueryWrapper<VaccineBatchPO>()
                .eq(VaccineBatchPO::getStatus, BatchStatus.NORMAL.getCode())
                .le(VaccineBatchPO::getExpiryDate, warningDate)
                .gt(VaccineBatchPO::getExpiryDate, LocalDate.now()));
        return list.stream().map(VaccineBatchConverter::toDomain).toList();
    }

    @Override
    public List<VaccineBatch> findExpired(LocalDate today) {
        List<VaccineBatchPO> list = vaccineBatchMapper.selectList(
            new LambdaQueryWrapper<VaccineBatchPO>()
                .in(VaccineBatchPO::getStatus, List.of(BatchStatus.NORMAL.getCode(), BatchStatus.NEAR_EXPIRY.getCode()))
                .lt(VaccineBatchPO::getExpiryDate, today));
        return list.stream().map(VaccineBatchConverter::toDomain).toList();
    }
}
