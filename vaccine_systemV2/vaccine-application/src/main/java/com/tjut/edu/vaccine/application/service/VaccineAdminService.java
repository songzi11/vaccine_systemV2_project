package com.tjut.edu.vaccine.application.service;

import com.tjut.edu.vaccine.application.dto.request.VaccineCreateRequest;
import com.tjut.edu.vaccine.application.dto.request.VaccineUpdateRequest;
import com.tjut.edu.vaccine.application.dto.response.VaccinePublicResponse;
import com.tjut.edu.vaccine.common.enums.ErrorCode;
import com.tjut.edu.vaccine.common.exception.BusinessException;
import com.tjut.edu.vaccine.domain.stock.entity.Vaccine;
import com.tjut.edu.vaccine.domain.stock.repository.VaccineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VaccineAdminService {

    private final VaccineRepository vaccineRepository;

    @Transactional(readOnly = true)
    public List<VaccinePublicResponse> listVaccines(Long id) {
        if (id != null) {
            return vaccineRepository.findById(id)
                    .map(v -> Collections.singletonList(toResponse(v)))
                    .orElse(Collections.emptyList());
        }
        List<Vaccine> vaccines = vaccineRepository.findAll(null, null, 1, 200);
        return vaccines.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public VaccinePublicResponse createVaccine(VaccineCreateRequest req) {
        Vaccine vaccine = new Vaccine(
                req.getVaccineName(),
                req.getCategory() != null ? req.getCategory() : Vaccine.TYPE_CLASS_I,
                req.getManufacturer(),
                req.getDescription()
        );
        if (req.getShelfStatus() != null && req.getShelfStatus() == 0) {
            vaccine.offShelf();
        }
        vaccineRepository.save(vaccine);
        log.info("疫苗创建成功: name={}", req.getVaccineName());

        VaccinePublicResponse resp = toResponse(vaccine);
        resp.setVaccineCode(req.getVaccineCode());
        resp.setSpecification(req.getSpecification());
        resp.setMinAgeMonth(req.getMinAge());
        resp.setMaxAgeMonth(req.getMaxAge());
        resp.setDoses(req.getDoses());
        resp.setIntervalDays(req.getIntervalDays());
        resp.setPrice(req.getPrice());
        return resp;
    }

    @Transactional
    public VaccinePublicResponse updateVaccine(Long id, VaccineUpdateRequest req) {
        Vaccine vaccine = vaccineRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.VACCINE_NOT_FOUND));

        vaccine.update(
                req.getVaccineName(),
                req.getCategory() != null ? req.getCategory() : vaccine.getVaccineType(),
                req.getManufacturer(),
                req.getDescription()
        );
        vaccineRepository.update(vaccine);
        log.info("疫苗更新成功: id={}", id);
        return toResponse(vaccine);
    }

    @Transactional
    public void deleteVaccine(Long id) {
        Vaccine vaccine = vaccineRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.VACCINE_NOT_FOUND));
        vaccineRepository.update(vaccine);
        log.info("疫苗删除成功: id={}", id);
    }

    @Transactional
    public VaccinePublicResponse updateShelfStatus(Long id, Integer status) {
        Vaccine vaccine = vaccineRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.VACCINE_NOT_FOUND));

        if (status != null && status == 1) {
            vaccine.onShelf();
        } else {
            vaccine.offShelf();
        }
        vaccineRepository.update(vaccine);
        log.info("疫苗上下架状态更新: id={}, status={}", id, status);
        return toResponse(vaccine);
    }

    private VaccinePublicResponse toResponse(Vaccine v) {
        VaccinePublicResponse r = new VaccinePublicResponse();
        r.setId(v.getId());
        r.setVaccineName(v.getVaccineName());
        r.setCategory(v.getVaccineType());
        r.setManufacturer(v.getManufacturer());
        r.setDescription(v.getDescription());
        r.setIsOnShelf(v.getIsOnShelf());
        return r;
    }
}
