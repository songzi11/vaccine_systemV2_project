package com.tjut.edu.vaccine.infrastructure.persistence.converter;

import com.tjut.edu.vaccine.common.enums.Gender;
import com.tjut.edu.vaccine.common.enums.IdCardType;
import com.tjut.edu.vaccine.common.enums.UserStatus;
import com.tjut.edu.vaccine.domain.identity.aggregate.User;
import com.tjut.edu.vaccine.domain.identity.vo.UserId;
import com.tjut.edu.vaccine.infrastructure.persistence.po.SysUserPO;

public class UserConverter {

    public static User toDomain(SysUserPO po) {
        if (po == null) {
            return null;
        }
        User user = new User();
        user.setId(new UserId(po.getId()));
        user.setUsername(po.getUsername());
        user.setPhone(po.getPhone());
        user.setPassword(po.getPassword());
        user.setRealName(po.getRealName());
        user.setGender(po.getGender() != null ? Gender.fromCode(po.getGender()) : null);
        user.setIdCardType(po.getIdCardType() != null ? IdCardType.fromCode(po.getIdCardType()) : null);
        user.setIdCardNo(po.getIdCardNo());
        user.setStatus(po.getStatus() != null ? UserStatus.fromCode(po.getStatus()) : null);
        user.setNoShowCount(po.getNoShowCount() != null ? po.getNoShowCount() : 0);
        user.setFreezeStartTime(po.getFreezeStartTime());
        user.setFreezeEndTime(po.getFreezeEndTime());
        user.setCreateTime(po.getCreateTime());
        user.setUpdateTime(po.getUpdateTime());
        return user;
    }

    public static SysUserPO toPO(User user) {
        if (user == null) {
            return null;
        }
        SysUserPO po = new SysUserPO();
        if (user.getId() != null) {
            po.setId(user.getId().value());
        }
        po.setUsername(user.getUsername());
        po.setPhone(user.getPhone());
        po.setPassword(user.getPassword());
        po.setRealName(user.getRealName());
        po.setGender(user.getGender() != null ? user.getGender().getCode() : null);
        po.setIdCardType(user.getIdCardType() != null ? user.getIdCardType().getCode() : null);
        po.setIdCardNo(user.getIdCardNo());
        po.setStatus(user.getStatus() != null ? user.getStatus().getCode() : null);
        po.setNoShowCount(user.getNoShowCount());
        po.setFreezeStartTime(user.getFreezeStartTime());
        po.setFreezeEndTime(user.getFreezeEndTime());
        po.setCreateTime(user.getCreateTime());
        po.setUpdateTime(user.getUpdateTime());
        return po;
    }
}
