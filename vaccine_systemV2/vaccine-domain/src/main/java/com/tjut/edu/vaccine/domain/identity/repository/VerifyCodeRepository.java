package com.tjut.edu.vaccine.domain.identity.repository;

import com.tjut.edu.vaccine.domain.identity.entity.VerifyCode;

import java.util.List;
import java.util.Optional;

public interface VerifyCodeRepository {

    Optional<VerifyCode> findById(Long id);

    VerifyCode findByCode(String code);

    List<VerifyCode> findAll();

    void save(VerifyCode verifyCode);

    void update(VerifyCode verifyCode);
}
