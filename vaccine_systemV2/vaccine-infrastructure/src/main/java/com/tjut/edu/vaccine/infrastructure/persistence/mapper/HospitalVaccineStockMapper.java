package com.tjut.edu.vaccine.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tjut.edu.vaccine.infrastructure.persistence.po.HospitalVaccineStockPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface HospitalVaccineStockMapper extends BaseMapper<HospitalVaccineStockPO> {

    /**
     * 锁定库存：预留1支给预约（可用 -1，锁定 +1）
     */
    @Update("UPDATE hospital_vaccine_stock SET " +
            "available_stock = available_stock - 1, " +
            "locked_stock = locked_stock + 1 " +
            "WHERE batch_id = #{batchId} AND available_stock >= 1")
    int lockStock(@Param("batchId") Long batchId);

    /**
     * 扣减库存（接种出库）：消耗已锁定库存
     */
    @Update("UPDATE hospital_vaccine_stock SET locked_stock = locked_stock - 1 " +
            "WHERE batch_id = #{batchId} AND locked_stock >= 1")
    int deductStock(@Param("batchId") Long batchId);

    /**
     * 释放锁定：取消预留（可用 +1，锁定 -1）
     */
    @Update("UPDATE hospital_vaccine_stock SET " +
            "available_stock = available_stock + 1, " +
            "locked_stock = locked_stock - 1 " +
            "WHERE batch_id = #{batchId} AND locked_stock > 0")
    int releaseStock(@Param("batchId") Long batchId);

    @Update("UPDATE hospital_vaccine_stock SET " +
            "available_stock = available_stock - #{quantity} " +
            "WHERE batch_id = #{batchId} AND available_stock >= #{quantity}")
    int deductStockQuantity(@Param("batchId") Long batchId, @Param("quantity") int quantity);

    @Update("UPDATE hospital_vaccine_stock SET " +
            "available_stock = available_stock + #{quantity}, " +
            "locked_stock = locked_stock - #{quantity} " +
            "WHERE batch_id = #{batchId} AND locked_stock >= #{quantity}")
    int releaseStockQuantity(@Param("batchId") Long batchId, @Param("quantity") int quantity);

    @Update("UPDATE hospital_vaccine_stock SET available_stock = available_stock + #{quantity} " +
            "WHERE batch_id = #{batchId}")
    int addStock(@Param("batchId") Long batchId, @Param("quantity") int quantity);

    /**
     * 按行ID扣减可用库存（调拨专用）
     */
    @Update("UPDATE hospital_vaccine_stock SET available_stock = available_stock - #{quantity} " +
            "WHERE id = #{id} AND available_stock >= #{quantity}")
    int deductStockById(@Param("id") Long id, @Param("quantity") int quantity);

    /**
     * 按行ID增加可用库存（调拨专用）
     */
    @Update("UPDATE hospital_vaccine_stock SET available_stock = available_stock + #{quantity} " +
            "WHERE id = #{id}")
    int addStockById(@Param("id") Long id, @Param("quantity") int quantity);

    /**
     * 按行ID清零库存（销毁专用）
     */
    @Update("UPDATE hospital_vaccine_stock SET available_stock = 0, locked_stock = 0 " +
            "WHERE batch_id = #{batchId}")
    int zeroStockByBatchId(@Param("batchId") Long batchId);

    /**
     * 可用库存 = SUM(available_stock)
     */
    @Select("SELECT COALESCE(SUM(s.available_stock), 0) FROM hospital_vaccine_stock s " +
            "JOIN vaccine_batch b ON s.batch_id = b.id " +
            "WHERE b.vaccine_id = #{vaccineId}")
    int sumAvailableByVaccine(@Param("vaccineId") Long vaccineId);

    /**
     * 总库存 = SUM(total_stock)
     */
    @Select("SELECT COALESCE(SUM(s.total_stock), 0) FROM hospital_vaccine_stock s " +
            "JOIN vaccine_batch b ON s.batch_id = b.id " +
            "WHERE b.vaccine_id = #{vaccineId}")
    int sumTotalByVaccine(@Param("vaccineId") Long vaccineId);

    @Select("SELECT s.* FROM hospital_vaccine_stock s " +
            "JOIN vaccine_batch b ON s.batch_id = b.id " +
            "WHERE b.vaccine_id = #{vaccineId} AND s.available_stock > 0")
    List<HospitalVaccineStockPO> selectAvailableByVaccine(@Param("vaccineId") Long vaccineId);
}
