package com.tjut.edu.vaccine.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tjut.edu.vaccine.infrastructure.persistence.po.VaccinationRecordPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VaccinationRecordMapper extends BaseMapper<VaccinationRecordPO> {

    @Select("SELECT vr.* FROM vaccination_record vr " +
            "JOIN appointment a ON vr.appointment_id = a.id " +
            "WHERE a.child_id = #{childId} " +
            "ORDER BY vr.injection_time DESC")
    List<VaccinationRecordPO> selectByChildId(@Param("childId") Long childId);

    @Select("SELECT vr.* FROM vaccination_record vr " +
            "JOIN appointment a ON vr.appointment_id = a.id " +
            "WHERE a.user_id = #{userId} " +
            "ORDER BY vr.injection_time DESC")
    List<VaccinationRecordPO> selectByUserId(@Param("userId") Long userId);
}
