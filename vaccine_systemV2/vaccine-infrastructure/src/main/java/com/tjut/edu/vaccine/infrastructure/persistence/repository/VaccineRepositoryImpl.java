package com.tjut.edu.vaccine.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tjut.edu.vaccine.domain.stock.entity.Vaccine;
import com.tjut.edu.vaccine.domain.stock.repository.VaccineRepository;
import com.tjut.edu.vaccine.infrastructure.persistence.converter.VaccineConverter;
import com.tjut.edu.vaccine.infrastructure.persistence.mapper.VaccineMapper;
import com.tjut.edu.vaccine.infrastructure.persistence.po.VaccinePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class VaccineRepositoryImpl implements VaccineRepository {

    private final VaccineMapper vaccineMapper;

    @Override
    public Optional<Vaccine> findById(Long id) {
        VaccinePO po = vaccineMapper.selectById(id);
        return Optional.ofNullable(po).map(VaccineConverter::toDomain);
    }

    @Override
    public List<Vaccine> findAll(String keyword, String type, int page, int size) {
        LambdaQueryWrapper<VaccinePO> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(VaccinePO::getVaccineName, keyword);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq(VaccinePO::getVaccineType, type);
        }
        wrapper.orderByAsc(VaccinePO::getId);
        wrapper.last("LIMIT " + size + " OFFSET " + (page - 1) * size);
        List<VaccinePO> list = vaccineMapper.selectList(wrapper);
        return list.stream().map(VaccineConverter::toDomain).toList();
    }

    @Override
    public void save(Vaccine vaccine) {
        vaccineMapper.insert(VaccineConverter.toPO(vaccine));
    }

    @Override
    public void update(Vaccine vaccine) {
        vaccineMapper.updateById(VaccineConverter.toPO(vaccine));
    }

    @Override
    public long count(String keyword, String type) {
        LambdaQueryWrapper<VaccinePO> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(VaccinePO::getVaccineName, keyword);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq(VaccinePO::getVaccineType, type);
        }
        return vaccineMapper.selectCount(wrapper);
    }
}
