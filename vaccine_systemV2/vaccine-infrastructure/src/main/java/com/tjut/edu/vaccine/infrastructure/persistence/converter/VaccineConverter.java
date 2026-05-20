package com.tjut.edu.vaccine.infrastructure.persistence.converter;

import com.tjut.edu.vaccine.domain.stock.entity.Vaccine;
import com.tjut.edu.vaccine.infrastructure.persistence.po.VaccinePO;

public class VaccineConverter {

    public static Vaccine toDomain(VaccinePO po) {
        if (po == null) {
            return null;
        }
        Vaccine vaccine = new Vaccine();
        vaccine.setId(po.getId());
        vaccine.setVaccineName(po.getVaccineName());
        vaccine.setVaccineType(po.getVaccineType());
        vaccine.setManufacturer(po.getManufacturer());
        vaccine.setDescription(po.getDescription());
        vaccine.setIsOnShelf(po.getIsOnShelf());
        vaccine.setCreateTime(po.getCreateTime());
        vaccine.setUpdateTime(po.getUpdateTime());
        return vaccine;
    }

    public static VaccinePO toPO(Vaccine vaccine) {
        if (vaccine == null) {
            return null;
        }
        VaccinePO po = new VaccinePO();
        po.setId(vaccine.getId());
        po.setVaccineName(vaccine.getVaccineName());
        po.setVaccineType(vaccine.getVaccineType());
        po.setManufacturer(vaccine.getManufacturer());
        po.setDescription(vaccine.getDescription());
        po.setIsOnShelf(vaccine.getIsOnShelf());
        po.setCreateTime(vaccine.getCreateTime());
        po.setUpdateTime(vaccine.getUpdateTime());
        return po;
    }
}
