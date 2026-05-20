package com.tjut.edu.vaccine.domain.identity.repository;

import com.tjut.edu.vaccine.domain.identity.entity.DoctorSchedule;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DoctorScheduleRepository {

    Optional<DoctorSchedule> findById(Long id);

    List<DoctorSchedule> findByDate(LocalDate date);

    List<DoctorSchedule> findByDoctorId(Long doctorId, LocalDate startDate, LocalDate endDate);

    boolean existsConflict(Long doctorId, Long windowId, LocalDate scheduleDate, String timeSlot);

    /**
     * 检查同一医生在指定日期时段是否已有排班（不限窗口，防止同一时段排到不同窗口）
     */
    boolean existsDoctorTimeConflict(Long doctorId, LocalDate scheduleDate, String timeSlot, Long excludeScheduleId);

    void save(DoctorSchedule schedule);

    void update(DoctorSchedule schedule);

    void deleteById(Long id);
}
