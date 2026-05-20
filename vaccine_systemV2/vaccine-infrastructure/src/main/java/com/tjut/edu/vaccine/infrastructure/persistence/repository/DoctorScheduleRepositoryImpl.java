package com.tjut.edu.vaccine.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tjut.edu.vaccine.domain.identity.entity.DoctorSchedule;
import com.tjut.edu.vaccine.domain.identity.repository.DoctorScheduleRepository;
import com.tjut.edu.vaccine.infrastructure.persistence.converter.DoctorScheduleConverter;
import com.tjut.edu.vaccine.infrastructure.persistence.mapper.DoctorScheduleMapper;
import com.tjut.edu.vaccine.infrastructure.persistence.po.DoctorSchedulePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DoctorScheduleRepositoryImpl implements DoctorScheduleRepository {

    private final DoctorScheduleMapper doctorScheduleMapper;

    @Override
    public Optional<DoctorSchedule> findById(Long id) {
        DoctorSchedulePO po = doctorScheduleMapper.selectById(id);
        return Optional.ofNullable(po).map(DoctorScheduleConverter::toDomain);
    }

    @Override
    public List<DoctorSchedule> findByDate(LocalDate date) {
        List<DoctorSchedulePO> list = doctorScheduleMapper.selectList(
            new LambdaQueryWrapper<DoctorSchedulePO>()
                .eq(DoctorSchedulePO::getScheduleDate, date)
                .orderByAsc(DoctorSchedulePO::getTimeSlot));
        return list.stream().map(DoctorScheduleConverter::toDomain).toList();
    }

    @Override
    public List<DoctorSchedule> findByDoctorId(Long doctorId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<DoctorSchedulePO> wrapper = new LambdaQueryWrapper<DoctorSchedulePO>()
            .eq(DoctorSchedulePO::getDoctorId, doctorId)
            .ge(DoctorSchedulePO::getScheduleDate, startDate)
            .le(DoctorSchedulePO::getScheduleDate, endDate)
            .orderByAsc(DoctorSchedulePO::getScheduleDate);
        List<DoctorSchedulePO> list = doctorScheduleMapper.selectList(wrapper);
        return list.stream().map(DoctorScheduleConverter::toDomain).toList();
    }

    @Override
    public boolean existsConflict(Long doctorId, Long windowId, LocalDate scheduleDate, String timeSlot) {
        Long count = doctorScheduleMapper.selectCount(
            new LambdaQueryWrapper<DoctorSchedulePO>()
                .eq(DoctorSchedulePO::getDoctorId, doctorId)
                .eq(DoctorSchedulePO::getWindowId, windowId)
                .eq(DoctorSchedulePO::getScheduleDate, scheduleDate)
                .eq(DoctorSchedulePO::getTimeSlot, timeSlot));
        return count != null && count > 0;
    }

    @Override
    public void save(DoctorSchedule schedule) {
        doctorScheduleMapper.insert(DoctorScheduleConverter.toPO(schedule));
    }

    @Override
    public void update(DoctorSchedule schedule) {
        doctorScheduleMapper.updateById(DoctorScheduleConverter.toPO(schedule));
    }

    @Override
    public void deleteById(Long id) {
        doctorScheduleMapper.deleteById(id);
    }
}
