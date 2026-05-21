package com.tjut.edu.vaccine.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tjut.edu.vaccine.common.enums.AppointmentStatus;
import com.tjut.edu.vaccine.common.enums.AppointmentStatusMachine;
import com.tjut.edu.vaccine.domain.appointment.aggregate.Appointment;
import com.tjut.edu.vaccine.domain.appointment.repository.AppointmentRepository;
import com.tjut.edu.vaccine.infrastructure.persistence.converter.AppointmentConverter;
import com.tjut.edu.vaccine.infrastructure.persistence.mapper.AppointmentMapper;
import com.tjut.edu.vaccine.infrastructure.persistence.po.AppointmentPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AppointmentRepositoryImpl implements AppointmentRepository {

    private final AppointmentMapper appointmentMapper;

    @Override
    public Optional<Appointment> findById(Long id) {
        AppointmentPO po = appointmentMapper.selectById(id);
        return Optional.ofNullable(po).map(AppointmentConverter::toDomain);
    }

    @Override
    public Appointment findByIdForUpdate(Long id) {
        AppointmentPO po = appointmentMapper.selectByIdForUpdate(id);
        if (po == null) return null;
        return AppointmentConverter.toDomain(po);
    }

    @Override
    public void save(Appointment appointment) {
        appointmentMapper.insert(AppointmentConverter.toPO(appointment));
    }

    @Override
    public void updateStatus(Appointment appointment) {
        AppointmentPO po = new AppointmentPO();
        po.setId(appointment.getId());
        po.setStatus(appointment.getStatus());
        po.setCurrentWindow(appointment.getCurrentWindow());
        po.setSigninTime(appointment.getSigninTime());
        po.setCancelTime(appointment.getCancelTime());
        po.setCancelReason(appointment.getCancelReason());
        po.setBatchId(appointment.getBatchId());
        appointmentMapper.updateById(po);
    }

    @Override
    public int countBySlotForUpdate(Long vaccineId, LocalDate date, String slot) {
        return appointmentMapper.countBySlotForUpdate(vaccineId, date, slot);
    }

    @Override
    public Map<String, Integer> countGroupBySlot(Long vaccineId, LocalDate date) {
        List<Map<String, Object>> rows = appointmentMapper.countGroupBySlot(vaccineId, date);
        Map<String, Integer> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String slot = (String) row.get("timeSlot");
            Number cnt = (Number) row.get("cnt");
            if (slot != null) {
                result.put(slot, cnt.intValue());
            }
        }
        return result;
    }

    @Override
    public int countByWindowAndStatus(String windowCode, List<Integer> statuses, LocalDate date) {
        Long count = appointmentMapper.selectCount(
            new LambdaQueryWrapper<AppointmentPO>()
                .eq(AppointmentPO::getCurrentWindow, windowCode)
                .eq(AppointmentPO::getAppointmentDate, date)
                .in(AppointmentPO::getStatus, statuses));
        return count != null ? count.intValue() : 0;
    }

    @Override
    public List<Appointment> findInProgressByUserAndChild(Long userId, Long childId) {
        List<AppointmentPO> list = appointmentMapper.selectList(
            new LambdaQueryWrapper<AppointmentPO>()
                .eq(AppointmentPO::getUserId, userId)
                .eq(AppointmentPO::getChildId, childId)
                .in(AppointmentPO::getStatus, AppointmentStatusMachine.IN_PROGRESS)
        );
        return list.stream().map(AppointmentConverter::toDomain).toList();
    }

    @Override
    public List<Appointment> findExpired(LocalDate date) {
        List<AppointmentPO> list = appointmentMapper.selectList(
            new LambdaQueryWrapper<AppointmentPO>()
                .eq(AppointmentPO::getStatus, AppointmentStatus.APPOINTED.getCode())
                .le(AppointmentPO::getAppointmentDate, date)
        );
        return list.stream().map(AppointmentConverter::toDomain).toList();
    }

    @Override
    public List<Appointment> findByUserId(Long userId, Integer status, int page, int size) {
        LambdaQueryWrapper<AppointmentPO> wrapper = new LambdaQueryWrapper<AppointmentPO>()
            .eq(AppointmentPO::getUserId, userId)
            .orderByDesc(AppointmentPO::getCreateTime);
        if (status != null) {
            wrapper.eq(AppointmentPO::getStatus, status);
        }
        wrapper.last("LIMIT " + size + " OFFSET " + (page - 1) * size);
        List<AppointmentPO> list = appointmentMapper.selectList(wrapper);
        return list.stream().map(AppointmentConverter::toDomain).toList();
    }

    @Override
    public List<Appointment> findByDate(LocalDate date) {
        List<AppointmentPO> list = appointmentMapper.selectList(
            new LambdaQueryWrapper<AppointmentPO>()
                .eq(AppointmentPO::getAppointmentDate, date)
                .in(AppointmentPO::getStatus, List.of(1, 6, 7, 10))
                .orderByAsc(AppointmentPO::getTimeSlot)
        );
        return list.stream().map(AppointmentConverter::toDomain).toList();
    }

    @Override
    public boolean existsDuplicate(Long childId, Long vaccineId, LocalDate date) {
        Long count = appointmentMapper.selectCount(
            new LambdaQueryWrapper<AppointmentPO>()
                .eq(AppointmentPO::getChildId, childId)
                .eq(AppointmentPO::getVaccineId, vaccineId)
                .eq(AppointmentPO::getAppointmentDate, date)
                .notIn(AppointmentPO::getStatus, List.of(3, 4))
        );
        return count != null && count > 0;
    }

    @Override
    public List<Appointment> findByStatuses(List<Integer> statuses, LocalDate date) {
        List<AppointmentPO> list = appointmentMapper.selectList(
            new LambdaQueryWrapper<AppointmentPO>()
                .eq(AppointmentPO::getAppointmentDate, date)
                .in(AppointmentPO::getStatus, statuses)
                .orderByAsc(AppointmentPO::getCreateTime)
        );
        return list.stream().map(AppointmentConverter::toDomain).toList();
    }

    @Override
    public Map<Integer, Integer> countGroupByStatus(LocalDate date) {
        List<Map<String, Object>> rows = appointmentMapper.countGroupByStatus(date);
        Map<Integer, Integer> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Number status = (Number) row.get("status");
            Number cnt = (Number) row.get("cnt");
            if (status != null) {
                result.put(status.intValue(), cnt.intValue());
            }
        }
        return result;
    }

    @Override
    public String generateAppointmentNo(LocalDate date) {
        String prefix = "APT" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        LambdaQueryWrapper<AppointmentPO> wrapper = new LambdaQueryWrapper<AppointmentPO>()
            .likeRight(AppointmentPO::getAppointmentNo, prefix)
            .orderByDesc(AppointmentPO::getAppointmentNo)
            .last("LIMIT 1 FOR UPDATE");
        AppointmentPO last = appointmentMapper.selectOne(wrapper);
        int seq = 1;
        if (last != null && last.getAppointmentNo().length() > prefix.length()) {
            seq = Integer.parseInt(last.getAppointmentNo().substring(prefix.length())) + 1;
        }
        return prefix + String.format("%04d", seq);
    }
}
