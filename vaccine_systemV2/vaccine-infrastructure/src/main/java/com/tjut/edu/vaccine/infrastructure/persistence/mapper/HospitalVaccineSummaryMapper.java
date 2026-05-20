package com.tjut.edu.vaccine.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tjut.edu.vaccine.infrastructure.persistence.po.HospitalVaccineSummaryPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface HospitalVaccineSummaryMapper extends BaseMapper<HospitalVaccineSummaryPO> {

    @Update("UPDATE hospital_vaccine_summary SET total_stock = #{totalStock}, " +
            "available_stock = #{availableStock}, version = version + 1, update_time = NOW() " +
            "WHERE id = #{id} AND version = #{version}")
    int updateWithOptimisticLock(@Param("id") Long id,
                                 @Param("totalStock") int totalStock,
                                 @Param("availableStock") int availableStock,
                                 @Param("version") int version);
}
