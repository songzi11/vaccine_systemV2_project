package com.tjut.edu.vaccine.application.service;

import com.tjut.edu.vaccine.application.dto.response.VaccinePublicResponse;
import com.tjut.edu.vaccine.domain.stock.entity.Vaccine;
import com.tjut.edu.vaccine.domain.stock.repository.VaccineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VaccinePublicService {

    private final VaccineRepository vaccineRepository;

    @Transactional(readOnly = true)
    public List<VaccinePublicResponse> findOnShelf(String category, String keyword, Long id) {
        // If id is provided, try to find a single vaccine by id
        if (id != null) {
            return vaccineRepository.findById(id)
                    .filter(Vaccine::isOnShelf)
                    .map(v -> List.of(toResponse(v)))
                    .orElse(List.of());
        }

        // 查询所有上架疫苗（page=1, size=100 获取全部）
        List<Vaccine> vaccines = vaccineRepository.findAll(null, category, 1, 100);

        // Filter by keyword if provided
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase();
            vaccines = vaccines.stream()
                    .filter(v -> v.getVaccineName() != null && v.getVaccineName().toLowerCase().contains(kw))
                    .collect(Collectors.toList());
        }

        return vaccines.stream()
                .filter(Vaccine::isOnShelf)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private VaccinePublicResponse toResponse(Vaccine v) {
        VaccinePublicResponse r = new VaccinePublicResponse();
        r.setId(v.getId());
        r.setVaccineName(v.getVaccineName());
        r.setCategory(v.getVaccineType()); // vaccineType → category
        r.setManufacturer(v.getManufacturer());
        r.setDescription(v.getDescription());
        r.setIsOnShelf(v.getIsOnShelf());
        return r;
    }
}
