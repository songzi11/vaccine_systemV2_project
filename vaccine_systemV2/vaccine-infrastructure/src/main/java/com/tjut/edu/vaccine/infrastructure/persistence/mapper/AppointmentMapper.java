package com.tjut.edu.vaccine.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tjut.edu.vaccine.infrastructure.persistence.po.AppointmentPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface AppointmentMapper extends BaseMapper<AppointmentPO> {

    @Select("SELECT * FROM appointment WHERE id = #{id} FOR UPDATE")
    AppointmentPO selectByIdForUpdate(Long id);

    @Select("SELECT COUNT(*) FROM appointment WHERE vaccine_id = #{vaccineId} AND appointment_date = #{date} AND time_slot = #{slot} AND status NOT IN (3,4) FOR UPDATE")
    int countBySlotForUpdate(@Param("vaccineId") Long vaccineId,
                             @Param("date") LocalDate date,
                             @Param("slot") String slot);

    @Select("SELECT time_slot AS timeSlot, COUNT(*) AS cnt FROM appointment WHERE vaccine_id = #{vaccineId} AND appointment_date = #{date} AND status NOT IN (3,4) GROUP BY time_slot")
    List<Map<String, Object>> countGroupBySlot(@Param("vaccineId") Long vaccineId,
                                               @Param("date") LocalDate date);

    @Select("SELECT status, COUNT(*) AS cnt FROM appointment WHERE appointment_date = #{date} GROUP BY status")
    List<Map<String, Object>> countGroupByStatus(@Param("date") LocalDate date);

    @Select("SELECT GET_LOCK(#{lockName}, #{timeout})")
    Integer getLock(@Param("lockName") String lockName, @Param("timeout") int timeout);

    @Select("SELECT RELEASE_LOCK(#{lockName})")
    Integer releaseLock(@Param("lockName") String lockName);
}
