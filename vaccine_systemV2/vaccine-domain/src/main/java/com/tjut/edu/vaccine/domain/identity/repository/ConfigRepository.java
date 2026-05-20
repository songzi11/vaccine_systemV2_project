package com.tjut.edu.vaccine.domain.identity.repository;

import com.tjut.edu.vaccine.domain.identity.entity.SysConfig;

import java.util.List;

/**
 * 系统配置仓储接口
 */
public interface ConfigRepository {

    SysConfig findByKey(String key);

    SysConfig findById(Long id);

    List<SysConfig> findAll();

    void save(SysConfig sysConfig);

    void updateValue(String key, String value);

    void updateValueById(Long id, String value);
}
