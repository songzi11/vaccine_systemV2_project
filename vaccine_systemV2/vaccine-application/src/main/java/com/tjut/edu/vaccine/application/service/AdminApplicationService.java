package com.tjut.edu.vaccine.application.service;

import com.tjut.edu.vaccine.application.assembler.AdminAssembler;
import com.tjut.edu.vaccine.application.assembler.VerifyCodeAssembler;
import com.tjut.edu.vaccine.application.dto.request.NoticeCreateRequest;
import com.tjut.edu.vaccine.application.dto.request.NoticeUpdateRequest;
import com.tjut.edu.vaccine.application.dto.request.RoleCreateRequest;
import com.tjut.edu.vaccine.application.dto.request.RoleUpdateRequest;
import com.tjut.edu.vaccine.application.dto.response.NoticeResponse;
import com.tjut.edu.vaccine.application.dto.response.RoleResponse;
import com.tjut.edu.vaccine.application.dto.response.UserResponse;
import com.tjut.edu.vaccine.application.dto.response.VerifyCodeResponse;
import com.tjut.edu.vaccine.common.enums.EnableStatus;
import com.tjut.edu.vaccine.common.enums.ErrorCode;
import com.tjut.edu.vaccine.common.enums.NoticeStatus;
import com.tjut.edu.vaccine.common.exception.BusinessException;
import com.tjut.edu.vaccine.domain.identity.aggregate.Role;
import com.tjut.edu.vaccine.domain.identity.aggregate.User;
import com.tjut.edu.vaccine.domain.identity.entity.Permission;
import com.tjut.edu.vaccine.domain.identity.entity.RolePermission;
import com.tjut.edu.vaccine.domain.identity.entity.SystemNotice;
import com.tjut.edu.vaccine.domain.identity.entity.UserRole;
import com.tjut.edu.vaccine.domain.identity.entity.HospitalWindow;
import com.tjut.edu.vaccine.domain.identity.entity.VerifyCode;
import com.tjut.edu.vaccine.domain.identity.repository.HospitalWindowRepository;
import com.tjut.edu.vaccine.domain.identity.repository.PermissionRepository;
import com.tjut.edu.vaccine.domain.identity.repository.RolePermissionRepository;
import com.tjut.edu.vaccine.domain.identity.repository.RoleRepository;
import com.tjut.edu.vaccine.domain.identity.repository.SystemNoticeRepository;
import com.tjut.edu.vaccine.domain.identity.repository.UserRepository;
import com.tjut.edu.vaccine.domain.identity.repository.UserRoleRepository;
import com.tjut.edu.vaccine.domain.identity.repository.VerifyCodeRepository;
import com.tjut.edu.vaccine.domain.port.SecurityContextPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminApplicationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final HospitalWindowRepository hospitalWindowRepository;
    private final SystemNoticeRepository systemNoticeRepository;
    private final VerifyCodeRepository verifyCodeRepository;
    private final SecurityContextPort securityContextPort;

    // ========== 用户管理 ==========

    @Transactional(readOnly = true)
    public List<UserResponse> findAllUsers(String keyword) {
        List<User> users = (keyword != null && !keyword.isBlank())
                ? userRepository.findByKeyword(keyword)
                : userRepository.findAll();
        List<UserResponse> responses = AdminAssembler.toUserResponseList(users);

        // 填充每个用户的角色编码
        List<Long> userIds = users.stream().map(u -> u.getId().value()).toList();
        if (!userIds.isEmpty()) {
            List<UserRole> allUserRoles = userRoleRepository.findByUserIds(userIds);
            Map<Long, List<String>> userRolesMap = new HashMap<>();
            for (UserRole ur : allUserRoles) {
                Role role = roleRepository.findById(ur.getRoleId());
                if (role != null) {
                    userRolesMap.computeIfAbsent(ur.getUserId(), k -> new ArrayList<>())
                            .add(role.getRoleCode());
                }
            }
            for (UserResponse resp : responses) {
                resp.setRoleCodes(userRolesMap.getOrDefault(resp.getUserId(), List.of()));
            }
        }
        return responses;
    }

    @Transactional
    public void freezeUser(Long userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        user.freeze("管理员冻结");
        userRepository.updateStatus(user);
        log.info("用户冻结成功: userId={}", userId);
    }

    @Transactional
    public void unfreezeUser(Long userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        user.unfreeze();
        userRepository.updateStatus(user);
        log.info("用户解冻成功: userId={}", userId);
    }

    // ========== 角色管理 ==========

    @Transactional
    public RoleResponse createRole(RoleCreateRequest req) {
        if (roleRepository.existsByRoleCode(req.getRoleCode())) {
            throw new BusinessException(ErrorCode.ROLE_CODE_DUPLICATE);
        }
        Role role = new Role(req.getRoleCode(), req.getRoleName(), req.getRoleGroup(),
                req.getDescription());
        roleRepository.save(role);

        // 建立角色-权限关联
        if (req.getPermissions() != null && !req.getPermissions().isEmpty()) {
            bindPermissions(role.getId().value(), req.getPermissions());
        }

        log.info("角色创建成功: roleCode={}", req.getRoleCode());
        RoleResponse resp = AdminAssembler.toRoleResponse(role);
        resp.setPermissions(req.getPermissions());
        resp.setPermissionCount(req.getPermissions() != null ? req.getPermissions().size() : 0);
        return resp;
    }

    @Transactional
    public RoleResponse updateRole(Long id, RoleUpdateRequest req) {
        Role role = roleRepository.findById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND);
        }
        if (req.getRoleName() != null || req.getRoleGroup() != null || req.getDescription() != null) {
            role.update(
                    req.getRoleName() != null ? req.getRoleName() : role.getRoleName(),
                    req.getRoleGroup() != null ? req.getRoleGroup() : role.getRoleGroup(),
                    req.getDescription() != null ? req.getDescription() : role.getDescription());
        }
        if (req.getStatus() != null) {
            if (req.getStatus() == EnableStatus.ENABLED.getCode()) {
                role.enable();
            } else {
                role.disable();
            }
        }
        roleRepository.update(role);

        // 更新角色-权限关联
        if (req.getPermissions() != null) {
            rolePermissionRepository.deleteByRoleId(id);
            if (!req.getPermissions().isEmpty()) {
                bindPermissions(id, req.getPermissions());
            }
        }

        RoleResponse resp = AdminAssembler.toRoleResponse(role);
        List<String> perms = getPermissionCodesByRoleId(id);
        resp.setPermissions(perms);
        resp.setPermissionCount(perms.size());
        resp.setUserCount(countUsersByRoleId(id));
        return resp;
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> findAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return AdminAssembler.toRoleResponseList(roles).stream()
                .map(resp -> {
                    Long roleId = resp.getId();
                    List<String> perms = getPermissionCodesByRoleId(roleId);
                    resp.setPermissions(perms);
                    resp.setPermissionCount(perms.size());
                    resp.setUserCount(countUsersByRoleId(roleId));
                    return resp;
                }).toList();
    }

    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND);
        }
        if (role.isSystemBuiltIn()) {
            throw new BusinessException(ErrorCode.ROLE_SYSTEM_PROTECTED);
        }
        // 清除关联的权限
        rolePermissionRepository.deleteByRoleId(id);
        roleRepository.deleteById(id);
        log.info("角色删除成功: roleId={}", id);
    }

    @Transactional(readOnly = true)
    public List<String> getRolePermissions(Long roleId) {
        return getPermissionCodesByRoleId(roleId);
    }

    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        List<UserRole> existing = userRoleRepository.findByUserId(userId);
        Set<Long> existingRoleIds = existing.stream()
                .map(UserRole::getRoleId).collect(Collectors.toSet());
        Set<Long> newRoleIds = new HashSet<>(roleIds);

        // 删除不再需要的角色
        for (UserRole ur : existing) {
            if (!newRoleIds.contains(ur.getRoleId())) {
                userRoleRepository.deleteByUserIdAndRoleId(userId, ur.getRoleId());
            }
        }
        // 添加新角色
        for (Long roleId : newRoleIds) {
            if (!existingRoleIds.contains(roleId)) {
                userRoleRepository.save(new UserRole(userId, roleId));
            }
        }
        log.info("用户角色分配成功: userId={}, roleIds={}", userId, roleIds);
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> getUserRoles(Long userId) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        return userRoles.stream()
                .map(ur -> {
                    Role role = roleRepository.findById(ur.getRoleId());
                    return AdminAssembler.toRoleResponse(role);
                })
                .toList();
    }

    // ========== 角色管理辅助方法 ==========

    /** 所有 DOCTOR_* 角色编码（窗口分配用，不含库管） */
    private static final Set<String> DOCTOR_ROLE_CODES = Set.of(
            "DOCTOR_SIGNIN",
            "DOCTOR_PRECHECK",
            "DOCTOR_REGISTER",
            "DOCTOR_VACCINATE", "DOCTOR_OBSERVE"
    );

    /** 窗口功能类型 → 对应医生角色编码 */
    private static final Map<String, String> WINDOW_TYPE_TO_ROLE = Map.of(
            "SIGNIN", "DOCTOR_SIGNIN",
            "PRECHECK", "DOCTOR_PRECHECK",
            "REGISTER", "DOCTOR_REGISTER",
            "VACCINATE", "DOCTOR_VACCINATE",
            "OBSERVE", "DOCTOR_OBSERVE"
    );

    /**
     * 安排医生到窗口：直接在窗口上设置 doctorId，同时赋予对应角色权限
     * 如果目标窗口已被其他医生占据，自动将原医生降为后勤
     * @param userId 医生用户ID
     * @param windowId 窗口ID，传 null 表示仅移除该医生的所有窗口岗位
     */
    @Transactional
    public void assignDoctorToWindow(Long userId, Long windowId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 1. 先将该医生从所有窗口上移除（清除 window.doctorId 指向该医生的记录）
        List<HospitalWindow> allWindows = hospitalWindowRepository.findAll();
        for (HospitalWindow w : allWindows) {
            if (userId.equals(w.getDoctorId())) {
                w.setDoctorId(null);
                hospitalWindowRepository.update(w);
            }
        }

        // 2. 删除该医生所有旧的 DOCTOR_* 角色
        removeDoctorRoles(userId);

        if (windowId == null) {
            log.info("医生设为后勤: userId={}", userId);
            return;
        }

        // 3. 查找目标窗口
        HospitalWindow window = hospitalWindowRepository.findById(windowId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));

        // 4. 如果窗口已有其他医生，先清除原医生的角色
        Long oldDoctorId = window.getDoctorId();
        if (oldDoctorId != null && !oldDoctorId.equals(userId)) {
            removeDoctorRoles(oldDoctorId);
            log.info("原医生降为后勤: userId={}, window={}", oldDoctorId, window.getWindowName());
        }

        // 5. 通过窗口功能类型匹配角色
        String windowType = window.getWindowFunctionType();
        String roleCode = WINDOW_TYPE_TO_ROLE.get(windowType);
        if (roleCode == null) {
            log.warn("窗口功能类型无对应角色: windowType={}, windowId={}", windowType, windowId);
            return;
        }

        Role newRole = roleRepository.findByRoleCode(roleCode);
        if (newRole == null) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND);
        }

        // 6. 分配新角色 + 设置窗口 doctorId
        userRoleRepository.save(new UserRole(userId, newRole.getId().value()));
        window.setDoctorId(userId);
        hospitalWindowRepository.update(window);
        log.info("医生安排到窗口: userId={}, window={}, role={}", userId, window.getWindowName(), roleCode);
    }

    /**
     * 从窗口移除医生：清除窗口的 doctorId，移除该医生的角色
     */
    @Transactional
    public void removeDoctorFromWindow(Long windowId) {
        HospitalWindow window = hospitalWindowRepository.findById(windowId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));
        Long doctorId = window.getDoctorId();
        if (doctorId == null) {
            return;
        }
        removeDoctorRoles(doctorId);
        window.setDoctorId(null);
        hospitalWindowRepository.update(window);
        log.info("从窗口移除医生: windowId={}, doctorId={}", windowId, doctorId);
    }

    /** 移除医生的所有 DOCTOR_* 角色 */
    private void removeDoctorRoles(Long userId) {
        List<UserRole> existing = userRoleRepository.findByUserId(userId);
        for (UserRole ur : existing) {
            Role role = roleRepository.findById(ur.getRoleId());
            if (role != null && DOCTOR_ROLE_CODES.contains(role.getRoleCode())) {
                userRoleRepository.deleteByUserIdAndRoleId(userId, ur.getRoleId());
            }
        }
    }

    private void bindPermissions(Long roleId, List<String> permissionCodes) {
        for (String code : permissionCodes) {
            Permission perm = permissionRepository.findByPermissionCode(code);
            if (perm != null) {
                rolePermissionRepository.save(new RolePermission(roleId, perm.getId()));
            }
        }
    }

    private List<String> getPermissionCodesByRoleId(Long roleId) {
        List<Permission> perms = permissionRepository.findByRoleId(roleId);
        return perms.stream().map(Permission::getPermissionCode).toList();
    }

    private int countUsersByRoleId(Long roleId) {
        List<UserRole> userRoles = userRoleRepository.findByRoleId(roleId);
        return userRoles.size();
    }

    // ========== 公告管理 ==========

    @Transactional
    public NoticeResponse createNotice(NoticeCreateRequest req) {
        Long authorId = securityContextPort.getCurrentUserId();
        SystemNotice notice = new SystemNotice(req.getTitle(), req.getContent(), req.getNoticeType(), authorId);
        notice.setStartTime(req.getStartTime());
        notice.setEndTime(req.getEndTime());
        systemNoticeRepository.save(notice);
        log.info("公告创建成功: title={}", req.getTitle());
        return AdminAssembler.toNoticeResponse(notice);
    }

    @Transactional
    public NoticeResponse updateNotice(Long id, NoticeUpdateRequest req) {
        SystemNotice notice = systemNoticeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));

        if (req.getTitle() != null) {
            notice.setTitle(req.getTitle());
        }
        if (req.getContent() != null) {
            notice.setContent(req.getContent());
        }
        if (req.getNoticeType() != null) {
            notice.setNoticeType(req.getNoticeType());
        }
        if (req.getStatus() != null) {
            if (req.getStatus() == NoticeStatus.PUBLISHED.getCode()) {
                Long auditUserId = securityContextPort.getCurrentUserId();
                notice.publish(auditUserId, req.getStartTime(), req.getEndTime());
            } else if (req.getStatus() == NoticeStatus.OFFLINE.getCode()) {
                notice.offline();
            } else if (req.getStatus() == NoticeStatus.REJECTED.getCode()) {
                Long auditUserId = securityContextPort.getCurrentUserId();
                notice.reject(auditUserId, "管理员拒绝");
            }
        }
        if (req.getStartTime() != null) {
            notice.setStartTime(req.getStartTime());
        }
        if (req.getEndTime() != null) {
            notice.setEndTime(req.getEndTime());
        }
        systemNoticeRepository.update(notice);
        return AdminAssembler.toNoticeResponse(notice);
    }

    @Transactional
    public void deleteNotice(Long id) {
        systemNoticeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));
        systemNoticeRepository.deleteById(id);
        log.info("公告删除成功: noticeId={}", id);
    }

    @Transactional(readOnly = true)
    public List<NoticeResponse> findAllNotices() {
        List<SystemNotice> notices = systemNoticeRepository.findAll(1, 100);
        return AdminAssembler.toNoticeResponseList(notices).stream()
                .map(this::enrichNoticeResponse).toList();
    }

    private NoticeResponse enrichNoticeResponse(NoticeResponse resp) {
        if (resp.getAuthorId() != null) {
            try {
                var user = userRepository.findById(resp.getAuthorId());
                if (user != null) resp.setPublisherName(user.getRealName());
            } catch (Exception ignored) {}
        }
        return resp;
    }

    @Transactional(readOnly = true)
    public List<NoticeResponse> findPublishedNotices(List<String> roles) {
        List<SystemNotice> notices = systemNoticeRepository.findPublished();
        // 家长只能看系统公告，医生和管理员可以看所有公告
        boolean isUserOnly = roles != null && roles.contains("USER")
                && roles.stream().noneMatch(r -> r.startsWith("DOCTOR_") || r.equals("SUPER_ADMIN"));
        if (isUserOnly) {
            notices = notices.stream()
                    .filter(n -> "SYSTEM".equals(n.getNoticeType()))
                    .toList();
        }
        return AdminAssembler.toNoticeResponseList(notices);
    }

    // ========== 验证码管理 ==========

    @Transactional
    public VerifyCodeResponse generateVerifyCode() {
        Long adminId = securityContextPort.getCurrentUserId();
        String code = String.format("%06d", new java.util.Random().nextInt(1000000));
        int retries = 0;
        while (verifyCodeRepository.findByCode(code) != null && retries < 10) {
            code = String.format("%06d", new java.util.Random().nextInt(1000000));
            retries++;
        }
        VerifyCode vc = new VerifyCode();
        vc.setCode(code);
        vc.setStatus(VerifyCode.STATUS_UNUSED);
        vc.setCreatedBy(adminId);
        verifyCodeRepository.save(vc);
        VerifyCodeResponse resp = VerifyCodeAssembler.toResponse(vc);
        resp.setCreatorName(getUserName(adminId));
        return resp;
    }

    @Transactional(readOnly = true)
    public List<VerifyCodeResponse> findAllVerifyCodes() {
        return verifyCodeRepository.findAll().stream().map(vc -> {
            VerifyCodeResponse resp = VerifyCodeAssembler.toResponse(vc);
            resp.setCreatorName(getUserName(vc.getCreatedBy()));
            if (vc.getUsedBy() != null) {
                resp.setUsedByName(getUserName(vc.getUsedBy()));
            }
            return resp;
        }).toList();
    }

    @Transactional
    public void revokeVerifyCode(Long id) {
        VerifyCode vc = verifyCodeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        if (vc.getStatus() != VerifyCode.STATUS_UNUSED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        vc.revoke();
        verifyCodeRepository.update(vc);
    }

    private String getUserName(Long userId) {
        if (userId == null) return null;
        try {
            User user = userRepository.findById(userId);
            return user != null ? user.getRealName() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
