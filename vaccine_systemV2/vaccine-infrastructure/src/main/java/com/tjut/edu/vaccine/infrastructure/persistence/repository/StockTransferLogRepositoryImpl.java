package com.tjut.edu.vaccine.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tjut.edu.vaccine.domain.stock.entity.StockTransferLog;
import com.tjut.edu.vaccine.domain.stock.repository.StockTransferLogRepository;
import com.tjut.edu.vaccine.infrastructure.persistence.mapper.StockTransferLogMapper;
import com.tjut.edu.vaccine.infrastructure.persistence.po.StockTransferLogPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class StockTransferLogRepositoryImpl implements StockTransferLogRepository {

    private final StockTransferLogMapper stockTransferLogMapper;

    @Override
    public void save(StockTransferLog log) {
        StockTransferLogPO po = new StockTransferLogPO();
        po.setTransferNo(log.getTransferNo());
        po.setBatchId(log.getBatchId());
        po.setFromType(log.getFromType());
        po.setFromId(log.getFromId());
        po.setToType(log.getToType());
        po.setToId(log.getToId());
        po.setQuantity(log.getQuantity());
        po.setOperatorId(log.getOperatorId());
        po.setTransferTime(log.getTransferTime());
        po.setRemark(log.getRemark());
        stockTransferLogMapper.insert(po);
    }

    @Override
    public String generateTransferNo() {
        String prefix = "TF" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        LambdaQueryWrapper<StockTransferLogPO> wrapper = new LambdaQueryWrapper<StockTransferLogPO>()
            .likeRight(StockTransferLogPO::getTransferNo, prefix)
            .orderByDesc(StockTransferLogPO::getTransferNo)
            .last("LIMIT 1");
        StockTransferLogPO last = stockTransferLogMapper.selectOne(wrapper);
        int seq = 1;
        if (last != null && last.getTransferNo().length() > prefix.length()) {
            seq = Integer.parseInt(last.getTransferNo().substring(prefix.length())) + 1;
        }
        return prefix + String.format("%04d", seq);
    }

    @Override
    public List<StockTransferLog> findAll(int page, int size) {
        LambdaQueryWrapper<StockTransferLogPO> wrapper = new LambdaQueryWrapper<StockTransferLogPO>()
            .orderByDesc(StockTransferLogPO::getCreateTime)
            .last("LIMIT " + size + " OFFSET " + (page - 1) * size);
        return stockTransferLogMapper.selectList(wrapper).stream().map(po -> {
            StockTransferLog log = new StockTransferLog();
            log.setId(po.getId());
            log.setTransferNo(po.getTransferNo());
            log.setBatchId(po.getBatchId());
            log.setFromType(po.getFromType());
            log.setFromId(po.getFromId());
            log.setToType(po.getToType());
            log.setToId(po.getToId());
            log.setQuantity(po.getQuantity());
            log.setOperatorId(po.getOperatorId());
            log.setTransferTime(po.getTransferTime());
            log.setRemark(po.getRemark());
            log.setCreateTime(po.getCreateTime());
            return log;
        }).toList();
    }
}
