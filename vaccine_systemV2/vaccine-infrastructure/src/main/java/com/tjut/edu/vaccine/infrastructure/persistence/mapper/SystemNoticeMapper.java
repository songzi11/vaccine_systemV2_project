package com.tjut.edu.vaccine.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tjut.edu.vaccine.infrastructure.persistence.po.SystemNoticePO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SystemNoticeMapper extends BaseMapper<SystemNoticePO> {
}
