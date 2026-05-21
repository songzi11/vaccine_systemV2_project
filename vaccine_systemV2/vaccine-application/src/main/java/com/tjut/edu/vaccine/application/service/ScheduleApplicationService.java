package com.tjut.edu.vaccine.application.service;

import com.tjut.edu.vaccine.application.assembler.ScheduleAssembler;
import com.tjut.edu.vaccine.application.dto.request.ScheduleCreateRequest;
import com.tjut.edu.vaccine.application.dto.request.ScheduleToggleRequest;
import com.tjut.edu.vaccine.application.dto.request.ScheduleUpdateRequest;
import com.tjut.edu.vaccine.application.dto.request.WindowCreateRequest;
import com.tjut.edu.vaccine.application.dto.request.WindowServiceRequest;
import com.tjut.edu.vaccine.application.dto.request.WindowUpdateRequest;
import com.tjut.edu.vaccine.application.dto.response.ScheduleDailyViewResponse;
import com.tjut.edu.vaccine.application.dto.response.ScheduleResponse;
import com.tjut.edu.vaccine.application.dto.response.WindowResponse;
import com.tjut.edu.vaccine.application.dto.response.WindowServiceResponse;
import com.tjut.edu.vaccine.common.enums.EnableStatus;
import com.tjut.edu.vaccine.common.enums.ErrorCode;
import com.tjut.edu.vaccine.common.exception.BusinessException;
import com.tjut.edu.vaccine.domain.identity.aggregate.Role;
import com.tjut.edu.vaccine.domain.identity.aggregate.User;
import com.tjut.edu.vaccine.domain.identity.entity.DoctorSchedule;
import com.tjut.edu.vaccine.domain.identity.entity.HospitalWindow;
import com.tjut.edu.vaccine.domain.identity.entity.UserRole;
import com.tjut.edu.vaccine.domain.identity.entity.WindowServiceConfig;
import com.tjut.edu.vaccine.domain.appointment.repository.AppointmentRepository;
import com.tjut.edu.vaccine.domain.identity.repository.DoctorScheduleRepository;
import com.tjut.edu.vaccine.domain.identity.repository.HospitalWindowRepository;
import com.tjut.edu.vaccine.domain.identity.repository.RoleRepository;
import com.tjut.edu.vaccine.domain.identity.repository.UserRepository;
import com.tjut.edu.vaccine.domain.identity.repository.UserRoleRepository;
import com.tjut.edu.vaccine.domain.identity.repository.WindowServiceConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleApplicationService {

    private final DoctorScheduleRepository doctorScheduleRepository;
    private final HospitalWindowRepository hospitalWindowRepository;
    private final WindowServiceConfigRepository windowServiceConfigRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final AppointmentRepository appointmentRepository;

    /** 医生角色 → 窗口功能类型 映射（不含库管） */
    private static final Map<String, String> ROLE_TO_WINDOW_TYPE = Map.of(
            "DOCTOR_SIGNIN", "SIGNIN",
            "DOCTOR_PRECHECK", "PRECHECK",
            "DOCTOR_REGISTER", "REGISTER",
            "DOCTOR_VACCINATE", "VACCINATE",
            "DOCTOR_OBSERVE", "OBSERVE"
    );

    /** 窗口功能类型 → 医生角色 映射（反向，不含库管） */
    private static final Map<String, String> WINDOW_TYPE_TO_ROLE = Map.of(
            "SIGNIN", "DOCTOR_SIGNIN",
            "PRECHECK", "DOCTOR_PRECHECK",
            "REGISTER", "DOCTOR_REGISTER",
            "VACCINATE", "DOCTOR_VACCINATE",
            "OBSERVE", "DOCTOR_OBSERVE"
    );

    /** 角色显示名称 */
    private static final Map<String, String> ROLE_DISPLAY_NAME = Map.of(
            "DOCTOR_SIGNIN", "签到医生",
            "DOCTOR_PRECHECK", "预检医生",
            "DOCTOR_REGISTER", "登记医生",
            "DOCTOR_VACCINATE", "接种医生",
            "DOCTOR_OBSERVE", "留观医生",
            "DOCTOR_STOCK", "库存管理"
    );

    // ========== 排班管理 ==========

    @Transactional
    public ScheduleResponse createSchedule(ScheduleCreateRequest req) {
        // 1. 验证同一医生同一时段不能排到不同窗口
        if (doctorScheduleRepository.existsDoctorTimeConflict(
                req.getDoctorId(), req.getScheduleDate(), req.getTimeSlot(), null)) {
            throw new BusinessException(ErrorCode.SCHEDULE_CONFLICT);
        }
        // 2. 验证同一窗口同一时段不重复排班
        if (doctorScheduleRepository.existsConflict(
                req.getDoctorId(), req.getWindowId(), req.getScheduleDate(), req.getTimeSlot())) {
            throw new BusinessException(ErrorCode.SCHEDULE_CONFLICT);
        }

        // 2. 创建排班
        int maxCapacity = req.getMaxCapacity() != null ? req.getMaxCapacity() : 50;
        DoctorSchedule schedule = new DoctorSchedule(
                req.getDoctorId(), req.getWindowId(), req.getScheduleDate(), req.getTimeSlot(), maxCapacity);
        doctorScheduleRepository.save(schedule);

        log.info("排班创建成功: doctorId={}, windowId={}, date={}, slot={}",
                req.getDoctorId(), req.getWindowId(), req.getScheduleDate(), req.getTimeSlot());

        return enrichScheduleResponse(ScheduleAssembler.toScheduleResponse(schedule));
    }

    @Transactional
    public ScheduleResponse updateSchedule(Long id, ScheduleUpdateRequest req) {
        DoctorSchedule schedule = doctorScheduleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (req.getStatus() != null) {
            schedule.setStatus(req.getStatus());
        }
        if (req.getMaxCapacity() != null) {
            schedule.setMaxCapacity(req.getMaxCapacity());
        }
        if (req.getTimeSlot() != null) {
            // 修改时段时需要重新校验冲突
            if (doctorScheduleRepository.existsDoctorTimeConflict(
                    schedule.getDoctorId(), schedule.getScheduleDate(), req.getTimeSlot(), id)) {
                throw new BusinessException(ErrorCode.SCHEDULE_CONFLICT);
            }
            schedule.setTimeSlot(req.getTimeSlot());
        }
        doctorScheduleRepository.update(schedule);

        return enrichScheduleResponse(ScheduleAssembler.toScheduleResponse(schedule));
    }

    @Transactional
    public void deleteSchedule(Long id) {
        DoctorSchedule schedule = doctorScheduleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));

        // 检查该排班对应窗口在排班日期是否有未完成的预约
        HospitalWindow window = hospitalWindowRepository.findById(schedule.getWindowId()).orElse(null);
        if (window != null) {
            List<Integer> activeStatuses = List.of(1, 6, 7, 10); // APPOINTED, SIGNED_IN, PRECHECK_PASS, OBSERVING
            int activeCount = appointmentRepository.countByWindowAndStatus(
                    window.getWindowCode(), activeStatuses, schedule.getScheduleDate());
            if (activeCount > 0) {
                throw new BusinessException(ErrorCode.SCHEDULE_CONFLICT);
            }
        }

        doctorScheduleRepository.deleteById(id);
        log.info("排班删除成功: scheduleId={}", id);
    }

    @Transactional(readOnly = true)
    public ScheduleResponse findById(Long id) {
        DoctorSchedule schedule = doctorScheduleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));
        return enrichScheduleResponse(ScheduleAssembler.toScheduleResponse(schedule));
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> findByDate(LocalDate date) {
        List<DoctorSchedule> schedules = doctorScheduleRepository.findByDate(date);
        return ScheduleAssembler.toScheduleResponseList(schedules).stream()
                .map(this::enrichScheduleResponse).toList();
    }

    /**
     * 获取排班日视图：通过窗口的 doctorId 直接关联医生与窗口，库管为固定角色
     */
    @Transactional(readOnly = true)
    public List<ScheduleDailyViewResponse> getDailyView(LocalDate date) {
        // 1. 获取所有用户
        List<User> allUsers = userRepository.findAll();
        List<Long> userIds = allUsers.stream().map(u -> u.getId().value()).toList();

        // 2. 查找库管人员（DOCTOR_STOCK 角色为固定角色，不走窗口分配）
        Set<Long> stockManagerIds = new HashSet<>();
        if (!userIds.isEmpty()) {
            List<UserRole> allUserRoles = userRoleRepository.findByUserIds(userIds);
            for (UserRole ur : allUserRoles) {
                Role role = roleRepository.findById(ur.getRoleId());
                if (role != null && "DOCTOR_STOCK".equals(role.getRoleCode())) {
                    stockManagerIds.add(ur.getUserId());
                }
            }
        }

        // 3. 获取所有启用窗口，构建 doctorId → window 映射
        List<HospitalWindow> allWindows = hospitalWindowRepository.findAll();
        Map<Long, HospitalWindow> doctorWindowMap = new HashMap<>();
        for (HospitalWindow w : allWindows) {
            if (w.isEnabled() && w.getDoctorId() != null) {
                doctorWindowMap.put(w.getDoctorId(), w);
            }
        }

        // 4. 获取当日已有排班记录
        List<DoctorSchedule> existingSchedules = doctorScheduleRepository.findByDate(date);
        Map<String, DoctorSchedule> scheduleMap = new HashMap<>();
        for (DoctorSchedule s : existingSchedules) {
            String key = s.getDoctorId() + "_" + s.getWindowId() + "_" + s.getTimeSlot();
            scheduleMap.put(key, s);
        }

        // 5. 构建日视图
        List<ScheduleDailyViewResponse> result = new ArrayList<>();
        for (User user : allUsers) {
            Long userId = user.getId().value();

            // 库管为固定角色
            if (stockManagerIds.contains(userId)) {
                ScheduleDailyViewResponse resp = new ScheduleDailyViewResponse();
                resp.setDoctorId(userId);
                resp.setDoctorName(user.getRealName());
                resp.setRoleName("库存管理");
                resp.setAmStatus(0);
                resp.setPmStatus(0);
                result.add(resp);
                continue;
            }

            HospitalWindow window = doctorWindowMap.get(userId);
            ScheduleDailyViewResponse resp = new ScheduleDailyViewResponse();
            resp.setDoctorId(userId);
            resp.setDoctorName(user.getRealName());

            if (window != null) {
                String functionType = window.getWindowFunctionType();
                String roleCode = WINDOW_TYPE_TO_ROLE.get(functionType);
                resp.setRoleName(roleCode != null
                        ? ROLE_DISPLAY_NAME.getOrDefault(roleCode, roleCode)
                        : (functionType != null ? functionType : "其他"));
                resp.setWindowId(window.getId());
                resp.setWindowName(window.getWindowName());
                resp.setWindowCode(window.getWindowCode());
                String amKey = userId + "_" + window.getId() + "_AM";
                DoctorSchedule amSchedule = scheduleMap.get(amKey);
                resp.setAmStatus(amSchedule != null ? amSchedule.getStatus() : 0);
                resp.setAmScheduleId(amSchedule != null ? amSchedule.getId() : null);
                String pmKey = userId + "_" + window.getId() + "_PM";
                DoctorSchedule pmSchedule = scheduleMap.get(pmKey);
                resp.setPmStatus(pmSchedule != null ? pmSchedule.getStatus() : 0);
                resp.setPmScheduleId(pmSchedule != null ? pmSchedule.getId() : null);
            } else {
                resp.setRoleName("后勤");
                resp.setAmStatus(0);
                resp.setPmStatus(0);
            }

            result.add(resp);
        }

        // 后勤排在最下面
        result.sort((a, b) -> {
            boolean aLogistics = "后勤".equals(a.getRoleName());
            boolean bLogistics = "后勤".equals(b.getRoleName());
            if (aLogistics != bLogistics) return aLogistics ? 1 : -1;
            return 0;
        });
        return result;
    }

    /**
     * 切换排班状态：0=恢复默认(删除记录)，1=请假，2=取消
     */
    @Transactional
    public void toggleScheduleStatus(ScheduleToggleRequest req) {
        List<DoctorSchedule> daySchedules = doctorScheduleRepository.findByDate(req.getScheduleDate());
        DoctorSchedule existing = daySchedules.stream()
                .filter(s -> s.getDoctorId().equals(req.getDoctorId())
                        && s.getWindowId().equals(req.getWindowId())
                        && s.getTimeSlot().equals(req.getTimeSlot()))
                .findFirst().orElse(null);

        if (req.getStatus() == 0) {
            // 恢复默认：删除覆盖记录
            if (existing != null) {
                doctorScheduleRepository.deleteById(existing.getId());
                log.info("排班恢复默认: doctorId={}, windowId={}, date={}, slot={}",
                        req.getDoctorId(), req.getWindowId(), req.getScheduleDate(), req.getTimeSlot());
            }
        } else {
            if (existing != null) {
                existing.setStatus(req.getStatus());
                doctorScheduleRepository.update(existing);
            } else {
                DoctorSchedule schedule = new DoctorSchedule(
                        req.getDoctorId(), req.getWindowId(), req.getScheduleDate(),
                        req.getTimeSlot(), 50);
                schedule.setStatus(req.getStatus());
                doctorScheduleRepository.save(schedule);
            }
            log.info("排班状态更新: doctorId={}, windowId={}, date={}, slot={}, status={}",
                    req.getDoctorId(), req.getWindowId(), req.getScheduleDate(), req.getTimeSlot(), req.getStatus());
        }
    }

    private ScheduleResponse enrichScheduleResponse(ScheduleResponse resp) {
        if (resp.getDoctorId() != null) {
            try {
                var user = userRepository.findById(resp.getDoctorId());
                if (user != null) resp.setDoctorName(user.getRealName());
            } catch (Exception ignored) {}
        }
        if (resp.getWindowId() != null) {
            try {
                hospitalWindowRepository.findById(resp.getWindowId())
                        .ifPresent(w -> resp.setWindowName(w.getWindowName()));
            } catch (Exception ignored) {}
        }
        return resp;
    }

    // ========== 窗口管理 ==========

    @Transactional
    public WindowResponse createWindow(WindowCreateRequest req) {
        // 1. 验证编码唯一
        if (hospitalWindowRepository.existsByCode(req.getWindowCode())) {
            throw new BusinessException(ErrorCode.WINDOW_CODE_DUPLICATE);
        }

        // 2. 创建窗口
        int avgHandleTime = req.getAvgHandleTime() != null ? req.getAvgHandleTime() : 5;
        int sortOrder = req.getSortOrder() != null ? req.getSortOrder() : 0;
        HospitalWindow window = new HospitalWindow(
                req.getWindowCode(), req.getWindowName(), req.getWindowFunctionType(),
                avgHandleTime, sortOrder);
        hospitalWindowRepository.save(window);

        log.info("窗口创建成功: windowCode={}", req.getWindowCode());

        return ScheduleAssembler.toWindowResponse(window);
    }

    @Transactional
    public WindowResponse updateWindow(Long id, WindowUpdateRequest req) {
        HospitalWindow window = hospitalWindowRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));

        window.update(
                req.getWindowName(), req.getWindowFunctionType(),
                req.getAvgHandleTime() != null ? req.getAvgHandleTime() : window.getAvgHandleTime(),
                req.getSortOrder() != null ? req.getSortOrder() : window.getSortOrder());
        if (req.getStatus() != null) {
            if (req.getStatus() == EnableStatus.ENABLED.getCode()) {
                window.enable();
            } else {
                window.disable();
            }
        }
        hospitalWindowRepository.update(window);

        return ScheduleAssembler.toWindowResponse(window);
    }

    @Transactional
    public void deleteWindow(Long id) {
        hospitalWindowRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));
        hospitalWindowRepository.deleteById(id);
        log.info("窗口删除成功: windowId={}", id);
    }

    @Transactional(readOnly = true)
    public List<WindowResponse> findAllWindows() {
        List<HospitalWindow> windows = hospitalWindowRepository.findAll();
        List<WindowResponse> responses = ScheduleAssembler.toWindowResponseList(windows);

        // 通过窗口上的 doctorId 直接查找医生姓名
        for (WindowResponse resp : responses) {
            if (resp.getDoctorId() != null) {
                resp.setDoctorName(getDoctorName(resp.getDoctorId()));
            }
        }
        return responses;
    }

    private String getDoctorName(Long doctorId) {
        try {
            var user = userRepository.findById(doctorId);
            return user != null ? user.getRealName() : null;
        } catch (Exception e) { return null; }
    }

    @Transactional(readOnly = true)
    public List<WindowResponse> findWindowsByType(String functionType) {
        List<HospitalWindow> windows = hospitalWindowRepository.findByFunctionType(functionType);
        return ScheduleAssembler.toWindowResponseList(windows);
    }

    // ========== 窗口服务配置 ==========

    @Transactional
    public WindowServiceResponse saveWindowService(String windowCode, WindowServiceRequest req) {
        // 验证窗口存在
        hospitalWindowRepository.findByCode(windowCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));

        var existing = windowServiceConfigRepository.findByWindowCode(windowCode);
        if (existing.isPresent()) {
            WindowServiceConfig config = existing.get();
            config.update(req.getBusinessName(), req.getBusinessDesc(), req.getBusinessDetail(),
                    req.getEstimatedTime(), req.getTips(), req.getRequiredItems());
            windowServiceConfigRepository.update(config);
            log.info("窗口服务配置更新: windowCode={}", windowCode);
            return toWindowServiceResponse(config);
        } else {
            WindowServiceConfig config = new WindowServiceConfig(windowCode, req.getBusinessName());
            config.setBusinessDesc(req.getBusinessDesc());
            config.setBusinessDetail(req.getBusinessDetail());
            config.setEstimatedTime(req.getEstimatedTime());
            config.setTips(req.getTips());
            config.setRequiredItems(req.getRequiredItems());
            windowServiceConfigRepository.save(config);
            log.info("窗口服务配置创建: windowCode={}", windowCode);
            return toWindowServiceResponse(config);
        }
    }

    @Transactional(readOnly = true)
    public WindowServiceResponse getWindowService(String windowCode) {
        return windowServiceConfigRepository.findByWindowCode(windowCode)
                .map(this::toWindowServiceResponse)
                .orElse(null);
    }

    private WindowServiceResponse toWindowServiceResponse(WindowServiceConfig config) {
        WindowServiceResponse resp = new WindowServiceResponse();
        resp.setId(config.getId());
        resp.setWindowCode(config.getWindowCode());
        resp.setBusinessName(config.getBusinessName());
        resp.setBusinessDesc(config.getBusinessDesc());
        resp.setBusinessDetail(config.getBusinessDetail());
        resp.setEstimatedTime(config.getEstimatedTime());
        resp.setTips(config.getTips());
        resp.setRequiredItems(config.getRequiredItems());
        if (config.getCreateTime() != null) {
            resp.setCreateTime(config.getCreateTime().toString());
        }
        return resp;
    }
}
