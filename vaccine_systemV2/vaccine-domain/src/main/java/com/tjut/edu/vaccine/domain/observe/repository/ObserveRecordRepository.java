package com.tjut.edu.vaccine.domain.observe.repository;

import com.tjut.edu.vaccine.domain.observe.aggregate.ObserveRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 留观记录仓储接口
 */
public interface ObserveRecordRepository {

    Optional<ObserveRecord> findById(Long id);

    Optional<ObserveRecord> findByAppointmentId(Long appointmentId);

    Optional<ObserveRecord> findByAppointmentIdForUpdate(Long appointmentId);

    Optional<ObserveRecord> findByInjectionId(String injectionId);

    void save(ObserveRecord observeRecord);

    void update(ObserveRecord observeRecord);

    /**
     * 查找未完成且开始时间早于指定时间的留观记录
     */
    List<ObserveRecord> findUnfinishedBefore(LocalDateTime threshold);
}
