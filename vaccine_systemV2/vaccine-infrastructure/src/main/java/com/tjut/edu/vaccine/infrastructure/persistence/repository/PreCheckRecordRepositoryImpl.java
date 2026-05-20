package com.tjut.edu.vaccine.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tjut.edu.vaccine.domain.observe.entity.PreCheckRecord;
import com.tjut.edu.vaccine.domain.observe.repository.PreCheckRecordRepository;
import com.tjut.edu.vaccine.infrastructure.persistence.converter.PreCheckRecordConverter;
import com.tjut.edu.vaccine.infrastructure.persistence.mapper.PreCheckRecordMapper;
import com.tjut.edu.vaccine.infrastructure.persistence.po.PreCheckRecordPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PreCheckRecordRepositoryImpl implements PreCheckRecordRepository {

    private final PreCheckRecordMapper preCheckRecordMapper;

    @Override
    public void save(PreCheckRecord preCheckRecord) {
        PreCheckRecordPO po = PreCheckRecordConverter.toPO(preCheckRecord);
        if (po.getId() == null) {
            preCheckRecordMapper.insert(po);
            preCheckRecord.setId(po.getId());
        } else {
            preCheckRecordMapper.updateById(po);
        }
    }

    @Override
    public Optional<PreCheckRecord> findByAppointmentId(Long appointmentId) {
        LambdaQueryWrapper<PreCheckRecordPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PreCheckRecordPO::getAppointmentId, appointmentId);
        PreCheckRecordPO po = preCheckRecordMapper.selectOne(wrapper);
        return Optional.ofNullable(po).map(PreCheckRecordConverter::toDomain);
    }
}
