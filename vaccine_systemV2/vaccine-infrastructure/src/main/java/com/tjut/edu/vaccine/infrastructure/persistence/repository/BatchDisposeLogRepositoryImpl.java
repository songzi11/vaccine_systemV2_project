package com.tjut.edu.vaccine.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tjut.edu.vaccine.domain.stock.entity.BatchDisposeLog;
import com.tjut.edu.vaccine.domain.stock.repository.BatchDisposeLogRepository;
import com.tjut.edu.vaccine.infrastructure.persistence.mapper.BatchDisposeLogMapper;
import com.tjut.edu.vaccine.infrastructure.persistence.po.BatchDisposeLogPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Repository
@RequiredArgsConstructor
public class BatchDisposeLogRepositoryImpl implements BatchDisposeLogRepository {

    private final BatchDisposeLogMapper batchDisposeLogMapper;

    @Override
    public void save(BatchDisposeLog log) {
        BatchDisposeLogPO po = new BatchDisposeLogPO();
        po.setDisposeNo(log.getDisposeNo());
        po.setBatchId(log.getBatchId());
        po.setDisposeQuantity(log.getDisposeQuantity());
        po.setDisposeReason(log.getDisposeReason());
        po.setOperatorId(log.getOperatorId());
        po.setDisposeTime(log.getDisposeTime());
        po.setRemark(log.getRemark());
        batchDisposeLogMapper.insert(po);
    }

    @Override
    public String generateDisposeNo() {
        String prefix = "BD" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        LambdaQueryWrapper<BatchDisposeLogPO> wrapper = new LambdaQueryWrapper<BatchDisposeLogPO>()
            .likeRight(BatchDisposeLogPO::getDisposeNo, prefix)
            .orderByDesc(BatchDisposeLogPO::getDisposeNo)
            .last("LIMIT 1");
        BatchDisposeLogPO last = batchDisposeLogMapper.selectOne(wrapper);
        int seq = 1;
        if (last != null && last.getDisposeNo().length() > prefix.length()) {
            seq = Integer.parseInt(last.getDisposeNo().substring(prefix.length())) + 1;
        }
        return prefix + String.format("%04d", seq);
    }
}
