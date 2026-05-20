package com.tjut.edu.vaccine.infrastructure.persistence.converter;

import com.tjut.edu.vaccine.common.enums.BatchStatus;
import com.tjut.edu.vaccine.domain.stock.aggregate.VaccineBatch;
import com.tjut.edu.vaccine.infrastructure.persistence.po.VaccineBatchPO;

public class VaccineBatchConverter {

    public static VaccineBatch toDomain(VaccineBatchPO po) {
        if (po == null) {
            return null;
        }
        VaccineBatch batch = new VaccineBatch();
        batch.setId(po.getId());
        batch.setBatchNo(po.getBatchNo());
        batch.setVaccineId(po.getVaccineId());
        batch.setManufacturer(po.getManufacturer());
        batch.setProductionDate(po.getProductionDate());
        batch.setExpiryDate(po.getExpiryDate());
        batch.setWarningDays(po.getWarningDays());
        batch.setStatus(po.getStatus() != null ? BatchStatus.fromCode(po.getStatus()) : null);
        batch.setCreateTime(po.getCreateTime());
        batch.setUpdateTime(po.getUpdateTime());
        return batch;
    }

    public static VaccineBatchPO toPO(VaccineBatch batch) {
        if (batch == null) {
            return null;
        }
        VaccineBatchPO po = new VaccineBatchPO();
        po.setId(batch.getId());
        po.setBatchNo(batch.getBatchNo());
        po.setVaccineId(batch.getVaccineId());
        po.setManufacturer(batch.getManufacturer());
        po.setProductionDate(batch.getProductionDate());
        po.setExpiryDate(batch.getExpiryDate());
        po.setWarningDays(batch.getWarningDays());
        po.setStatus(batch.getStatus() != null ? batch.getStatus().getCode() : null);
        po.setCreateTime(batch.getCreateTime());
        po.setUpdateTime(batch.getUpdateTime());
        return po;
    }
}
