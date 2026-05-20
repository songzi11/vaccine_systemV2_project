package com.tjut.edu.vaccine.domain.identity.repository;

import com.tjut.edu.vaccine.domain.identity.entity.WindowServiceConfig;

import java.util.Optional;

public interface WindowServiceConfigRepository {

    Optional<WindowServiceConfig> findByWindowCode(String windowCode);

    void save(WindowServiceConfig config);

    void update(WindowServiceConfig config);
}
