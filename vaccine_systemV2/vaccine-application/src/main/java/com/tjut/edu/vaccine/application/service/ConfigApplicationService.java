package com.tjut.edu.vaccine.application.service;

import com.tjut.edu.vaccine.common.enums.ErrorCode;
import com.tjut.edu.vaccine.common.exception.BusinessException;
import com.tjut.edu.vaccine.domain.identity.entity.SysConfig;
import com.tjut.edu.vaccine.domain.identity.repository.ConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigApplicationService {

    private final ConfigRepository configRepository;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> findAllConfigs() {
        List<SysConfig> configs = configRepository.findAll();
        return configs.stream().map(this::toMap).toList();
    }

    @Transactional
    public Map<String, Object> updateConfig(Long id, String configValue) {
        SysConfig config = configRepository.findById(id);
        if (config == null) {
            throw new BusinessException(ErrorCode.CONFIG_KEY_NOT_FOUND);
        }
        configRepository.updateValueById(id, configValue);
        log.info("配置更新成功: id={}, key={}", id, config.getConfigKey());
        config.setConfigValue(configValue);
        return toMap(config);
    }

    private Map<String, Object> toMap(SysConfig config) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", config.getId());
        map.put("configKey", config.getConfigKey());
        map.put("configValue", config.getConfigValue());
        map.put("configDesc", config.getConfigDesc());
        map.put("valueType", config.getValueType());
        map.put("updateTime", config.getUpdateTime());
        return map;
    }
}
