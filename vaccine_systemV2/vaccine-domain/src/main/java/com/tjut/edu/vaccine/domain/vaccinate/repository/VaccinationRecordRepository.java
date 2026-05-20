package com.tjut.edu.vaccine.domain.vaccinate.repository;

import com.tjut.edu.vaccine.domain.vaccinate.aggregate.VaccinationRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 接种记录仓储接口
 */
public interface VaccinationRecordRepository {

    Optional<VaccinationRecord> findById(Long id);

    Optional<VaccinationRecord> findByAppointmentId(Long appointmentId);

    Optional<VaccinationRecord> findByInjectionId(String injectionId);

    void save(VaccinationRecord vaccinationRecord);

    String generateInjectionId(LocalDate date);

    List<VaccinationRecord> findByDoctorId(Long doctorId, int page, int size);

    List<VaccinationRecord> findByChildId(Long childId);

    List<VaccinationRecord> findByUserId(Long userId);

    List<VaccinationRecord> findAll();
}
