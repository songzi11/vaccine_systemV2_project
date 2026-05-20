package com.tjut.edu.vaccine.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tjut.edu.vaccine.domain.stock.entity.HospitalVaccineStock;
import com.tjut.edu.vaccine.domain.stock.repository.VaccineStockRepository;
import com.tjut.edu.vaccine.infrastructure.persistence.mapper.HospitalVaccineStockMapper;
import com.tjut.edu.vaccine.infrastructure.persistence.converter.HospitalVaccineStockConverter;
import com.tjut.edu.vaccine.infrastructure.persistence.po.HospitalVaccineStockPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class VaccineStockRepositoryImpl implements VaccineStockRepository {

    private final HospitalVaccineStockMapper stockMapper;

    @Override
    public Optional<HospitalVaccineStock> findByBatchId(Long batchId) {
        List<HospitalVaccineStockPO> list = stockMapper.selectList(
            new LambdaQueryWrapper<HospitalVaccineStockPO>()
                .eq(HospitalVaccineStockPO::getBatchId, batchId));
        if (list.isEmpty()) return Optional.empty();
        return Optional.of(HospitalVaccineStockConverter.toDomain(list.get(0)));
    }

    @Override
    public Optional<HospitalVaccineStock> findByBatchIdForUpdate(Long batchId) {
        List<HospitalVaccineStockPO> list = stockMapper.selectList(
            new LambdaQueryWrapper<HospitalVaccineStockPO>()
                .eq(HospitalVaccineStockPO::getBatchId, batchId)
                .last("FOR UPDATE"));
        if (list.isEmpty()) return Optional.empty();
        return Optional.of(HospitalVaccineStockConverter.toDomain(list.get(0)));
    }

    @Override
    public void lockStock(Long batchId) {
        int rows = stockMapper.lockStock(batchId);
        if (rows == 0) {
            throw new RuntimeException("库存锁定失败: batchId=" + batchId);
        }
    }

    @Override
    public void deductStock(Long batchId) {
        int rows = stockMapper.deductStock(batchId);
        if (rows == 0) {
            throw new RuntimeException("库存扣减失败: batchId=" + batchId);
        }
    }

    @Override
    public void deductStock(Long batchId, int quantity) {
        int rows = stockMapper.deductStockQuantity(batchId, quantity);
        if (rows == 0) {
            throw new RuntimeException("库存扣减失败: batchId=" + batchId + ", quantity=" + quantity);
        }
    }

    @Override
    public void releaseStock(Long batchId) {
        stockMapper.releaseStock(batchId);
    }

    @Override
    public void releaseStock(Long batchId, int quantity) {
        stockMapper.releaseStockQuantity(batchId, quantity);
    }

    @Override
    public void addStock(Long batchId, int quantity) {
        int rows = stockMapper.addStock(batchId, quantity);
        if (rows == 0) {
            throw new RuntimeException("库存增加失败: batchId=" + batchId + "，目标批次不存在");
        }
    }

    @Override
    public void deductStockById(Long id, int quantity) {
        int rows = stockMapper.deductStockById(id, quantity);
        if (rows == 0) {
            throw new RuntimeException("库存扣减失败: stockId=" + id + ", quantity=" + quantity);
        }
    }

    @Override
    public void addStockById(Long id, int quantity) {
        int rows = stockMapper.addStockById(id, quantity);
        if (rows == 0) {
            throw new RuntimeException("库存增加失败: stockId=" + id);
        }
    }

    @Override
    public void zeroStockByBatchId(Long batchId) {
        stockMapper.zeroStockByBatchId(batchId);
    }

    @Override
    public Optional<HospitalVaccineStock> findByLocation(Long batchId, Integer locationType, Long locationId) {
        HospitalVaccineStockPO po = stockMapper.selectOne(
            new LambdaQueryWrapper<HospitalVaccineStockPO>()
                .eq(HospitalVaccineStockPO::getBatchId, batchId)
                .eq(HospitalVaccineStockPO::getLocationType, locationType)
                .eq(HospitalVaccineStockPO::getLocationId, locationId));
        return Optional.ofNullable(po).map(HospitalVaccineStockConverter::toDomain);
    }

    @Override
    public void save(HospitalVaccineStock stock) {
        stockMapper.insert(HospitalVaccineStockConverter.toPO(stock));
    }

    @Override
    public List<HospitalVaccineStock> findAllAvailable(Long vaccineId) {
        List<HospitalVaccineStockPO> list = stockMapper.selectAvailableByVaccine(vaccineId);
        return list.stream().map(HospitalVaccineStockConverter::toDomain).toList();
    }

    @Override
    public List<HospitalVaccineStock> findAllWithStock() {
        List<HospitalVaccineStockPO> list = stockMapper.selectList(
            new LambdaQueryWrapper<HospitalVaccineStockPO>()
                .gt(HospitalVaccineStockPO::getTotalStock, 0)
                .or()
                .gt(HospitalVaccineStockPO::getAvailableStock, 0)
                .or()
                .gt(HospitalVaccineStockPO::getLockedStock, 0));
        return list.stream().map(HospitalVaccineStockConverter::toDomain).toList();
    }

    @Override
    public int sumTotalByVaccine(Long vaccineId) {
        return stockMapper.sumTotalByVaccine(vaccineId);
    }

    @Override
    public int sumAvailableByVaccine(Long vaccineId) {
        return stockMapper.sumAvailableByVaccine(vaccineId);
    }
}
