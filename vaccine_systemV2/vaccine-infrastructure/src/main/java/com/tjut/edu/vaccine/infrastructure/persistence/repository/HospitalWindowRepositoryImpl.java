package com.tjut.edu.vaccine.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tjut.edu.vaccine.domain.identity.entity.HospitalWindow;
import com.tjut.edu.vaccine.domain.identity.repository.HospitalWindowRepository;
import com.tjut.edu.vaccine.infrastructure.persistence.converter.HospitalWindowConverter;
import com.tjut.edu.vaccine.infrastructure.persistence.mapper.HospitalWindowMapper;
import com.tjut.edu.vaccine.infrastructure.persistence.po.HospitalWindowPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class HospitalWindowRepositoryImpl implements HospitalWindowRepository {

    private final HospitalWindowMapper hospitalWindowMapper;

    @Override
    public Optional<HospitalWindow> findById(Long id) {
        HospitalWindowPO po = hospitalWindowMapper.selectById(id);
        return Optional.ofNullable(po).map(HospitalWindowConverter::toDomain);
    }

    @Override
    public Optional<HospitalWindow> findByCode(String windowCode) {
        HospitalWindowPO po = hospitalWindowMapper.selectOne(
            new LambdaQueryWrapper<HospitalWindowPO>()
                .eq(HospitalWindowPO::getWindowCode, windowCode));
        return Optional.ofNullable(po).map(HospitalWindowConverter::toDomain);
    }

    @Override
    public Optional<HospitalWindow> findByDoctorId(Long doctorId) {
        HospitalWindowPO po = hospitalWindowMapper.selectOne(
            new LambdaQueryWrapper<HospitalWindowPO>()
                .eq(HospitalWindowPO::getDoctorId, doctorId)
                .eq(HospitalWindowPO::getStatus, 0)
                .last("LIMIT 1"));
        return Optional.ofNullable(po).map(HospitalWindowConverter::toDomain);
    }

    @Override
    public List<HospitalWindow> findAll() {
        List<HospitalWindowPO> list = hospitalWindowMapper.selectList(
            new LambdaQueryWrapper<HospitalWindowPO>()
                .orderByAsc(HospitalWindowPO::getSortOrder));
        return list.stream().map(HospitalWindowConverter::toDomain).toList();
    }

    @Override
    public List<HospitalWindow> findByFunctionType(String functionType) {
        List<HospitalWindowPO> list = hospitalWindowMapper.selectList(
            new LambdaQueryWrapper<HospitalWindowPO>()
                .eq(HospitalWindowPO::getWindowFunctionType, functionType)
                .orderByAsc(HospitalWindowPO::getSortOrder));
        return list.stream().map(HospitalWindowConverter::toDomain).toList();
    }

    @Override
    public boolean existsByCode(String windowCode) {
        Long count = hospitalWindowMapper.selectCount(
            new LambdaQueryWrapper<HospitalWindowPO>()
                .eq(HospitalWindowPO::getWindowCode, windowCode));
        return count != null && count > 0;
    }

    @Override
    public void save(HospitalWindow window) {
        hospitalWindowMapper.insert(HospitalWindowConverter.toPO(window));
    }

    @Override
    public void update(HospitalWindow window) {
        hospitalWindowMapper.updateById(HospitalWindowConverter.toPO(window));
    }

    @Override
    public void deleteById(Long id) {
        hospitalWindowMapper.deleteById(id);
    }
}
