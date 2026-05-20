package com.tjut.edu.vaccine.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tjut.edu.vaccine.domain.identity.entity.SysConfig;
import com.tjut.edu.vaccine.domain.identity.repository.ConfigRepository;
import com.tjut.edu.vaccine.infrastructure.persistence.mapper.SysConfigMapper;
import com.tjut.edu.vaccine.infrastructure.persistence.po.SysConfigPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ConfigRepositoryImpl implements ConfigRepository {

    private final SysConfigMapper sysConfigMapper;

    @Override
    public SysConfig findByKey(String key) {
        LambdaQueryWrapper<SysConfigPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfigPO::getConfigKey, key);
        SysConfigPO po = sysConfigMapper.selectOne(wrapper);
        return toDomain(po);
    }

    @Override
    public List<SysConfig> findAll() {
        List<SysConfigPO> poList = sysConfigMapper.selectList(null);
        return poList.stream().map(this::toDomain).toList();
    }

    @Override
    public void save(SysConfig sysConfig) {
        SysConfigPO po = toPO(sysConfig);
        sysConfigMapper.insert(po);
    }

    @Override
    public void updateValue(String key, String value) {
        LambdaUpdateWrapper<SysConfigPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysConfigPO::getConfigKey, key)
               .set(SysConfigPO::getConfigValue, value);
        sysConfigMapper.update(null, wrapper);
    }

    @Override
    public SysConfig findById(Long id) {
        SysConfigPO po = sysConfigMapper.selectById(id);
        return toDomain(po);
    }

    @Override
    public void updateValueById(Long id, String value) {
        LambdaUpdateWrapper<SysConfigPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysConfigPO::getId, id)
               .set(SysConfigPO::getConfigValue, value);
        sysConfigMapper.update(null, wrapper);
    }

    private SysConfig toDomain(SysConfigPO po) {
        if (po == null) {
            return null;
        }
        SysConfig config = new SysConfig();
        config.setId(po.getId());
        config.setConfigKey(po.getConfigKey());
        config.setConfigValue(po.getConfigValue());
        config.setConfigDesc(po.getConfigDesc());
        config.setValueType(po.getValueType());
        config.setUpdateTime(po.getUpdateTime());
        return config;
    }

    private SysConfigPO toPO(SysConfig config) {
        SysConfigPO po = new SysConfigPO();
        po.setConfigKey(config.getConfigKey());
        po.setConfigValue(config.getConfigValue());
        po.setConfigDesc(config.getConfigDesc());
        po.setValueType(config.getValueType());
        return po;
    }
}
