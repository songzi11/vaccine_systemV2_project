package com.tjut.edu.vaccine.domain.identity.repository;

import com.tjut.edu.vaccine.domain.identity.entity.HospitalWindow;

import java.util.List;
import java.util.Optional;

public interface HospitalWindowRepository {

    Optional<HospitalWindow> findById(Long id);

    Optional<HospitalWindow> findByCode(String windowCode);

    Optional<HospitalWindow> findByDoctorId(Long doctorId);

    List<HospitalWindow> findAll();

    List<HospitalWindow> findByFunctionType(String functionType);

    boolean existsByCode(String windowCode);

    void save(HospitalWindow window);

    void update(HospitalWindow window);

    void deleteById(Long id);
}
