package com.tjut.edu.vaccine.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tjut.edu.vaccine.domain.identity.entity.VerifyCode;
import com.tjut.edu.vaccine.domain.identity.repository.VerifyCodeRepository;
import com.tjut.edu.vaccine.infrastructure.persistence.converter.VerifyCodeConverter;
import com.tjut.edu.vaccine.infrastructure.persistence.mapper.VerifyCodeMapper;
import com.tjut.edu.vaccine.infrastructure.persistence.po.VerifyCodePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class VerifyCodeRepositoryImpl implements VerifyCodeRepository {

    private final VerifyCodeMapper verifyCodeMapper;

    @Override
    public Optional<VerifyCode> findById(Long id) {
        VerifyCodePO po = verifyCodeMapper.selectById(id);
        return Optional.ofNullable(po).map(VerifyCodeConverter::toDomain);
    }

    @Override
    public VerifyCode findByCode(String code) {
        VerifyCodePO po = verifyCodeMapper.selectOne(
                new LambdaQueryWrapper<VerifyCodePO>().eq(VerifyCodePO::getCode, code));
        return VerifyCodeConverter.toDomain(po);
    }

    @Override
    public List<VerifyCode> findAll() {
        List<VerifyCodePO> list = verifyCodeMapper.selectList(
                new LambdaQueryWrapper<VerifyCodePO>().orderByDesc(VerifyCodePO::getCreateTime));
        return list.stream().map(VerifyCodeConverter::toDomain).toList();
    }

    @Override
    public void save(VerifyCode verifyCode) {
        VerifyCodePO po = VerifyCodeConverter.toPO(verifyCode);
        verifyCodeMapper.insert(po);
        verifyCode.setId(po.getId());
    }

    @Override
    public void update(VerifyCode verifyCode) {
        verifyCodeMapper.updateById(VerifyCodeConverter.toPO(verifyCode));
    }
}
