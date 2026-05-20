package com.tjut.edu.vaccine.infrastructure.persistence.converter;

import com.tjut.edu.vaccine.domain.stock.aggregate.HospitalVaccineSummary;
import com.tjut.edu.vaccine.infrastructure.persistence.po.HospitalVaccineSummaryPO;

public class HospitalVaccineSummaryConverter {

    public static HospitalVaccineSummary toDomain(HospitalVaccineSummaryPO po) {
        if (po == null) {
            return null;
        }
        HospitalVaccineSummary summary = new HospitalVaccineSummary();
        summary.setId(po.getId());
        summary.setHospitalId(po.getHospitalId());
        summary.setVaccineId(po.getVaccineId());
        summary.setTotalStock(po.getTotalStock());
        summary.setAvailableStock(po.getAvailableStock());
        summary.setWarningThreshold(po.getWarningThreshold());
        summary.setVersion(po.getVersion());
        summary.setUpdateTime(po.getUpdateTime());
        return summary;
    }

    public static HospitalVaccineSummaryPO toPO(HospitalVaccineSummary summary) {
        if (summary == null) {
            return null;
        }
        HospitalVaccineSummaryPO po = new HospitalVaccineSummaryPO();
        po.setId(summary.getId());
        po.setHospitalId(summary.getHospitalId());
        po.setVaccineId(summary.getVaccineId());
        po.setTotalStock(summary.getTotalStock());
        po.setAvailableStock(summary.getAvailableStock());
        po.setWarningThreshold(summary.getWarningThreshold());
        po.setVersion(summary.getVersion());
        po.setUpdateTime(summary.getUpdateTime());
        return po;
    }
}
