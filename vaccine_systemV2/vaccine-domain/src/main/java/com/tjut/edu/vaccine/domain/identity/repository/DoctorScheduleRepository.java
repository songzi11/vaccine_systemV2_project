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

    void save(DoctorSchedule schedule);

    void update(DoctorSchedule schedule);

    void deleteById(Long id);
}
