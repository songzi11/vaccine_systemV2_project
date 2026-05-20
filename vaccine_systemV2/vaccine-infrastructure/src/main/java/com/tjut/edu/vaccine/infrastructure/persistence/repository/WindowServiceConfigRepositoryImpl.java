package com.tjut.edu.vaccine.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tjut.edu.vaccine.domain.identity.entity.WindowServiceConfig;
import com.tjut.edu.vaccine.domain.identity.repository.WindowServiceConfigRepository;
import com.tjut.edu.vaccine.infrastructure.persistence.converter.WindowServiceConfigConverter;
import com.tjut.edu.vaccine.infrastructure.persistence.mapper.WindowServiceConfigMapper;
import com.tjut.edu.vaccine.infrastructure.persistence.po.WindowServiceConfigPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WindowServiceConfigRepositoryImpl implements WindowServiceConfigRepository {

    private final WindowServiceConfigMapper windowServiceConfigMapper;

    @Override
    public Optional<WindowServiceConfig> findByWindowCode(String windowCode) {
        WindowServiceConfigPO po = windowServiceConfigMapper.selectOne(
            new LambdaQueryWrapper<WindowServiceConfigPO>()
                .eq(WindowServiceConfigPO::getWindowCode, windowCode));
        return Optional.ofNullable(po).map(WindowServiceConfigConverter::toDomain);
    }

    @Override
    public void save(WindowServiceConfig config) {
        windowServiceConfigMapper.insert(WindowServiceConfigConverter.toPO(config));
    }

    @Override
    public void update(WindowServiceConfig config) {
        windowServiceConfigMapper.updateById(WindowServiceConfigConverter.toPO(config));
    }
}
