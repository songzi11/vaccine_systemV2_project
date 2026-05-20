package com.tjut.edu.vaccine.infrastructure.persistence.converter;

import com.tjut.edu.vaccine.domain.stock.entity.HospitalVaccineStock;
import com.tjut.edu.vaccine.infrastructure.persistence.po.HospitalVaccineStockPO;

public class HospitalVaccineStockConverter {

    public static HospitalVaccineStock toDomain(HospitalVaccineStockPO po) {
        if (po == null) {
            return null;
        }
        HospitalVaccineStock stock = new HospitalVaccineStock();
        stock.setId(po.getId());
        stock.setHospitalId(po.getHospitalId());
        stock.setBatchId(po.getBatchId());
        stock.setLocationType(po.getLocationType());
        stock.setLocationId(po.getLocationId());
        int availableStock = po.getAvailableStock() != null ? po.getAvailableStock() : 0;
        int lockedStock = po.getLockedStock() != null ? po.getLockedStock() : 0;
        int totalStock = po.getTotalStock() != null && po.getTotalStock() > 0
                ? po.getTotalStock()
                : availableStock + lockedStock;
        stock.setTotalStock(totalStock);
        stock.setAvailableStock(availableStock);
        stock.setLockedStock(lockedStock);
        return stock;
    }

    public static HospitalVaccineStockPO toPO(HospitalVaccineStock stock) {
        if (stock == null) {
            return null;
        }
        HospitalVaccineStockPO po = new HospitalVaccineStockPO();
        po.setId(stock.getId());
        po.setHospitalId(stock.getHospitalId());
        po.setBatchId(stock.getBatchId());
        po.setLocationType(stock.getLocationType());
        po.setLocationId(stock.getLocationId());
        po.setTotalStock(stock.getTotalStock());
        po.setAvailableStock(stock.getAvailableStock());
        po.setLockedStock(stock.getLockedStock());
        return po;
    }
}
