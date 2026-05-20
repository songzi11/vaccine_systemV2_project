package com.tjut.edu.vaccine.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tjut.edu.vaccine.infrastructure.persistence.po.VaccineBatchPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VaccineBatchMapper extends BaseMapper<VaccineBatchPO> {

    @Select("SELECT * FROM vaccine_batch WHERE id = #{id} FOR UPDATE")
    VaccineBatchPO selectByIdForUpdate(@Param("id") Long id);

    @Select("SELECT b.* FROM vaccine_batch b " +
            "JOIN hospital_vaccine_stock s ON b.id = s.batch_id " +
            "WHERE b.vaccine_id = #{vaccineId} " +
            "AND s.hospital_id = #{hospitalId} " +
            "AND b.status = 0 " +
            "AND s.available_stock > 0 " +
            "AND b.expiry_date > CURDATE() " +
            "ORDER BY b.expiry_date ASC LIMIT 1 FOR UPDATE")
    VaccineBatchPO selectAvailableForFEFO(@Param("vaccineId") Long vaccineId,
                                          @Param("hospitalId") Long hospitalId);
}
