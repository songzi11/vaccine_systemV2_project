package com.tjut.edu.vaccine.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tjut.edu.vaccine.domain.observe.aggregate.ObserveRecord;
import com.tjut.edu.vaccine.domain.observe.repository.ObserveRecordRepository;
import com.tjut.edu.vaccine.infrastructure.persistence.converter.ObserveRecordConverter;
import com.tjut.edu.vaccine.infrastructure.persistence.mapper.ObserveRecordMapper;
import com.tjut.edu.vaccine.infrastructure.persistence.po.ObserveRecordPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ObserveRecordRepositoryImpl implements ObserveRecordRepository {

    private final ObserveRecordMapper observeRecordMapper;

    @Override
    public Optional<ObserveRecord> findById(Long id) {
        ObserveRecordPO po = observeRecordMapper.selectById(id);
        return Optional.ofNullable(po).map(ObserveRecordConverter::toDomain);
    }

    @Override
    public Optional<ObserveRecord> findByAppointmentId(Long appointmentId) {
        ObserveRecordPO po = observeRecordMapper.selectOne(
            new LambdaQueryWrapper<ObserveRecordPO>()
                .eq(ObserveRecordPO::getAppointmentId, appointmentId));
        return Optional.ofNullable(po).map(ObserveRecordConverter::toDomain);
    }

    @Override
    public Optional<ObserveRecord> findByInjectionId(String injectionId) {
        ObserveRecordPO po = observeRecordMapper.selectOne(
            new LambdaQueryWrapper<ObserveRecordPO>()
                .eq(ObserveRecordPO::getInjectionId, injectionId));
        return Optional.ofNullable(po).map(ObserveRecordConverter::toDomain);
    }

    @Override
    public void save(ObserveRecord record) {
        ObserveRecordPO po = ObserveRecordConverter.toPO(record);
        observeRecordMapper.insert(po);
        record.setId(po.getId());
    }

    @Override
    public void update(ObserveRecord record) {
        observeRecordMapper.updateById(ObserveRecordConverter.toPO(record));
    }
}
