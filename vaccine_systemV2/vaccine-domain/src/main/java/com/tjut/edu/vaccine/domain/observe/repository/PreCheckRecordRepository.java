package com.tjut.edu.vaccine.domain.observe.repository;

import com.tjut.edu.vaccine.domain.observe.entity.PreCheckRecord;

import java.util.Optional;

/**
 * 预检记录仓储接口
 */
public interface PreCheckRecordRepository {

    void save(PreCheckRecord preCheckRecord);

    Optional<PreCheckRecord> findByAppointmentId(Long appointmentId);
}
