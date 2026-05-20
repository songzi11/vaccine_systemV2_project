package com.tjut.edu.vaccine.application.assembler;

import com.tjut.edu.vaccine.application.dto.response.NoticeResponse;
import com.tjut.edu.vaccine.application.dto.response.RoleResponse;
import com.tjut.edu.vaccine.application.dto.response.UserResponse;
import com.tjut.edu.vaccine.domain.identity.aggregate.Role;
import com.tjut.edu.vaccine.domain.identity.aggregate.User;
import com.tjut.edu.vaccine.domain.identity.entity.SystemNotice;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AdminAssembler {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String[] USER_STATUS = {"正常", "停用", "注销", "冻结"};
    private static final String[] NOTICE_STATUS = {"待审核", "已发布", "已下架", "已拒绝"};

    public static UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }
        UserResponse response = new UserResponse();
        response.setUserId(user.getId().value());
        response.setPhone(user.getPhone());
        response.setRealName(user.getRealName());
        response.setGender(user.getGender() != null ? user.getGender().getDescription() : null);
        response.setStatus(user.getStatus() != null ? user.getStatus().getDescription() : null);
        response.setCreateTime(formatDateTime(user.getCreateTime()));
        return response;
    }

    public static List<UserResponse> toUserResponseList(List<User> users) {
        if (users == null) {
            return List.of();
        }
        return users.stream().map(AdminAssembler::toUserResponse).collect(Collectors.toList());
    }

    public static RoleResponse toRoleResponse(Role role) {
        if (role == null) {
            return null;
        }
        RoleResponse response = new RoleResponse();
        response.setId(role.getId().value());
        response.setRoleCode(role.getRoleCode());
        response.setRoleName(role.getRoleName());
        response.setRoleGroup(role.getRoleGroup());
        response.setDescription(role.getDescription());
        response.setStatus(role.isEnabled() ? "启用" : "禁用");
        response.setIsSystem(role.isSystemBuiltIn());
        response.setCreateTime(formatDateTime(role.getCreateTime()));
        return response;
    }

    public static List<RoleResponse> toRoleResponseList(List<Role> roles) {
        if (roles == null) {
            return List.of();
        }
        return roles.stream().map(AdminAssembler::toRoleResponse).collect(Collectors.toList());
    }

    public static NoticeResponse toNoticeResponse(SystemNotice notice) {
        if (notice == null) {
            return null;
        }
        NoticeResponse response = new NoticeResponse();
        response.setId(notice.getId());
        response.setTitle(notice.getTitle());
        response.setContent(notice.getContent());
        response.setNoticeType(notice.getNoticeType());
        response.setStatus(notice.getStatus() >= 0 && notice.getStatus() < NOTICE_STATUS.length
                ? NOTICE_STATUS[notice.getStatus()] : String.valueOf(notice.getStatus()));
        response.setStatusCode(notice.getStatus());
        response.setAuthorId(notice.getAuthorId());
        response.setStartTime(notice.getStartTime());
        response.setEndTime(notice.getEndTime());
        response.setPublishTime(formatDateTime(notice.getPublishTime()));
        response.setCreateTime(formatDateTime(notice.getCreateTime()));
        response.setAuditReason(notice.getAuditReason());
        return response;
    }

    public static List<NoticeResponse> toNoticeResponseList(List<SystemNotice> notices) {
        if (notices == null) {
            return List.of();
        }
        return notices.stream().map(AdminAssembler::toNoticeResponse).collect(Collectors.toList());
    }

    private static String formatDateTime(java.time.LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : null;
    }
}
