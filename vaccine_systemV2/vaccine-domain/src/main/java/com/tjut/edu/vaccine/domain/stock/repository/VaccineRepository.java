package com.tjut.edu.vaccine.domain.stock.repository;

import com.tjut.edu.vaccine.domain.stock.entity.Vaccine;

import java.util.List;
import java.util.Optional;

/**
 * 疫苗仓储接口
 */
public interface VaccineRepository {

    Optional<Vaccine> findById(Long id);

    List<Vaccine> findAll(String keyword, String type, int page, int size);

    void save(Vaccine vaccine);

    void update(Vaccine vaccine);

    long count(String keyword, String type);
}
