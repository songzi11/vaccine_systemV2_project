package com.tjut.edu.vaccine.domain.appointment.repository;

import com.tjut.edu.vaccine.domain.appointment.aggregate.Appointment;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 预约仓储接口
 */
public interface AppointmentRepository {

    Optional<Appointment> findById(Long id);

    Appointment findByIdForUpdate(Long id);

    void save(Appointment appointment);

    void updateStatus(Appointment appointment);

    int countBySlotForUpdate(Long vaccineId, LocalDate date, String slot);

    Map<String, Integer> countGroupBySlot(Long vaccineId, LocalDate date);

    int countByWindowAndStatus(String windowCode, List<Integer> statuses, LocalDate date);

    List<Appointment> findInProgressByUserAndChild(Long userId, Long childId);

    List<Appointment> findExpired(LocalDate date);

    List<Appointment> findByUserId(Long userId, Integer status, int page, int size);

    String generateAppointmentNo(LocalDate date);

    List<Appointment> findByDate(LocalDate date);

    boolean existsDuplicate(Long childId, Long vaccineId, LocalDate date);

    List<Appointment> findByStatuses(List<Integer> statuses, LocalDate date);

    Map<Integer, Integer> countGroupByStatus(LocalDate date);

    boolean acquireSlotLock(String lockName, int timeoutSeconds);

    void releaseSlotLock(String lockName);

    int countExpiredByUserInMonth(Long userId, LocalDate startOfMonth, LocalDate today);
}
