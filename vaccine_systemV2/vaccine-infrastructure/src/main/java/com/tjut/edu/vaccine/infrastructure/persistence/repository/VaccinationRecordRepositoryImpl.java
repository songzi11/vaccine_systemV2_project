package com.tjut.edu.vaccine.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tjut.edu.vaccine.domain.vaccinate.aggregate.VaccinationRecord;
import com.tjut.edu.vaccine.domain.vaccinate.repository.VaccinationRecordRepository;
import com.tjut.edu.vaccine.infrastructure.persistence.converter.VaccinationRecordConverter;
import com.tjut.edu.vaccine.infrastructure.persistence.mapper.VaccinationRecordMapper;
import com.tjut.edu.vaccine.infrastructure.persistence.po.VaccinationRecordPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class VaccinationRecordRepositoryImpl implements VaccinationRecordRepository {

    private final VaccinationRecordMapper vaccinationRecordMapper;

    @Override
    public Optional<VaccinationRecord> findById(Long id) {
        VaccinationRecordPO po = vaccinationRecordMapper.selectById(id);
        return Optional.ofNullable(po).map(VaccinationRecordConverter::toDomain);
    }

    @Override
    public Optional<VaccinationRecord> findByAppointmentId(Long appointmentId) {
        VaccinationRecordPO po = vaccinationRecordMapper.selectOne(
            new LambdaQueryWrapper<VaccinationRecordPO>()
                .eq(VaccinationRecordPO::getAppointmentId, appointmentId));
        return Optional.ofNullable(po).map(VaccinationRecordConverter::toDomain);
    }

    @Override
    public Optional<VaccinationRecord> findByInjectionId(String injectionId) {
        VaccinationRecordPO po = vaccinationRecordMapper.selectOne(
            new LambdaQueryWrapper<VaccinationRecordPO>()
                .eq(VaccinationRecordPO::getInjectionId, injectionId));
        return Optional.ofNullable(po).map(VaccinationRecordConverter::toDomain);
    }

    @Override
    public void save(VaccinationRecord record) {
        vaccinationRecordMapper.insert(VaccinationRecordConverter.toPO(record));
    }

    @Override
    public String generateInjectionId(LocalDate date) {
        String prefix = "INJ" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        LambdaQueryWrapper<VaccinationRecordPO> wrapper = new LambdaQueryWrapper<VaccinationRecordPO>()
            .likeRight(VaccinationRecordPO::getInjectionId, prefix)
            .orderByDesc(VaccinationRecordPO::getInjectionId)
            .last("LIMIT 1");
        VaccinationRecordPO last = vaccinationRecordMapper.selectOne(wrapper);
        int seq = 1;
        if (last != null && last.getInjectionId().length() > prefix.length()) {
            seq = Integer.parseInt(last.getInjectionId().substring(prefix.length())) + 1;
        }
        return prefix + String.format("%04d", seq);
    }

    @Override
    public List<VaccinationRecord> findByDoctorId(Long doctorId, int page, int size) {
        List<VaccinationRecordPO> list = vaccinationRecordMapper.selectList(
            new LambdaQueryWrapper<VaccinationRecordPO>()
                .eq(VaccinationRecordPO::getDoctorId, doctorId)
                .orderByDesc(VaccinationRecordPO::getInjectionTime)
                .last("LIMIT " + size + " OFFSET " + (page - 1) * size));
        return list.stream().map(VaccinationRecordConverter::toDomain).toList();
    }

    @Override
    public List<VaccinationRecord> findByChildId(Long childId) {
        List<VaccinationRecordPO> list = vaccinationRecordMapper.selectByChildId(childId);
        return list.stream().map(VaccinationRecordConverter::toDomain).toList();
    }

    @Override
    public List<VaccinationRecord> findByUserId(Long userId) {
        List<VaccinationRecordPO> list = vaccinationRecordMapper.selectByUserId(userId);
        return list.stream().map(VaccinationRecordConverter::toDomain).toList();
    }

    @Override
    public List<VaccinationRecord> findAll() {
        List<VaccinationRecordPO> list = vaccinationRecordMapper.selectList(
            new LambdaQueryWrapper<VaccinationRecordPO>()
                .orderByDesc(VaccinationRecordPO::getInjectionTime));
        return list.stream().map(VaccinationRecordConverter::toDomain).toList();
    }
}
