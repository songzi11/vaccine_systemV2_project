package com.tjut.edu.vaccine.application.service;

import com.tjut.edu.vaccine.application.assembler.AppointmentAssembler;
import com.tjut.edu.vaccine.application.dto.request.AppointmentBookRequest;
import com.tjut.edu.vaccine.application.dto.request.AppointmentCancelRequest;
import com.tjut.edu.vaccine.application.dto.request.SigninRequest;
import com.tjut.edu.vaccine.application.dto.response.AppointmentDetailResponse;
import com.tjut.edu.vaccine.application.dto.response.AppointmentResponse;
import com.tjut.edu.vaccine.application.dto.response.QueueItemResponse;
import com.tjut.edu.vaccine.common.enums.AppointmentStatus;
import com.tjut.edu.vaccine.common.enums.ErrorCode;
import com.tjut.edu.vaccine.common.exception.BusinessException;
import com.tjut.edu.vaccine.domain.appointment.aggregate.Appointment;
import com.tjut.edu.vaccine.domain.appointment.repository.AppointmentRepository;
import com.tjut.edu.vaccine.domain.identity.aggregate.ChildProfile;
import com.tjut.edu.vaccine.domain.identity.aggregate.User;
import com.tjut.edu.vaccine.domain.identity.entity.HospitalWindow;
import com.tjut.edu.vaccine.domain.identity.entity.SysConfig;
import com.tjut.edu.vaccine.domain.identity.repository.ChildProfileRepository;
import com.tjut.edu.vaccine.domain.identity.repository.ConfigRepository;
import com.tjut.edu.vaccine.domain.identity.repository.HospitalWindowRepository;
import com.tjut.edu.vaccine.domain.identity.repository.UserRepository;
import com.tjut.edu.vaccine.domain.observe.repository.AdverseReactionRepository;
import com.tjut.edu.vaccine.domain.observe.repository.ObserveRecordRepository;
import com.tjut.edu.vaccine.domain.stock.entity.Vaccine;
import com.tjut.edu.vaccine.domain.stock.repository.VaccineRepository;
import com.tjut.edu.vaccine.domain.stock.repository.VaccineStockRepository;
import com.tjut.edu.vaccine.domain.vaccinate.repository.VaccinationRecordRepository;
import com.tjut.edu.vaccine.domain.port.SecurityContextPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentApplicationService {

    private static final String CONFIG_MAX_CAPACITY = "appointment.max_capacity";
    private static final String CONFIG_ADVANCE_DAYS = "appointment.advance_days";
    private static final int DEFAULT_MAX_CAPACITY = 50;
    private static final int DEFAULT_ADVANCE_DAYS = 7;
    private static final List<String> APPOINTMENT_TIME_SLOTS = List.of(
            "08:00-09:00", "09:00-10:00", "10:00-11:00", "11:00-12:00",
            "14:00-15:00", "15:00-16:00", "16:00-17:00");

    private final AppointmentRepository appointmentRepository;
    private final ChildProfileRepository childProfileRepository;
    private final UserRepository userRepository;
    private final VaccineRepository vaccineRepository;
    private final VaccineStockRepository vaccineStockRepository;
    private final ConfigRepository configRepository;
    private final VaccinationRecordRepository vaccinationRecordRepository;
    private final ObserveRecordRepository observeRecordRepository;
    private final AdverseReactionRepository adverseReactionRepository;
    private final SecurityContextPort securityContextPort;
    private final WindowAssignmentService windowAssignmentService;
    private final HospitalWindowRepository hospitalWindowRepository;

    @Transactional
    public AppointmentResponse book(AppointmentBookRequest req) {
        Long userId = securityContextPort.getCurrentUserId();

        // 1. 验证用户预约资格
        User user = userRepository.findById(userId);
        if (user == null || !user.isAppointmentAllowed()) {
            throw new BusinessException(ErrorCode.USER_FROZEN_LOGIN);
        }

        // 2. 验证儿童归属
        ChildProfile child = childProfileRepository.findById(req.getChildId());
        if (child == null) {
            throw new BusinessException(ErrorCode.APPOINT_CHILD_NOT_FOUND);
        }
        if (!child.getParentId().equals(userId)) {
            throw new BusinessException(ErrorCode.APPOINT_CHILD_NOT_OWN);
        }

        // 3. 验证疫苗状态
        Vaccine vaccine = vaccineRepository.findById(req.getVaccineId())
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINT_VACCINE_OFF_SHELF));
        if (!vaccine.isOnShelf()) {
            throw new BusinessException(ErrorCode.APPOINT_VACCINE_OFF_SHELF);
        }

        // 4. 验证预约日期
        LocalDate today = LocalDate.now();
        int advanceDays = getConfigInt(CONFIG_ADVANCE_DAYS, DEFAULT_ADVANCE_DAYS);
        if (req.getAppointmentDate().isBefore(today) ||
                req.getAppointmentDate().isAfter(today.plusDays(advanceDays))) {
            throw new BusinessException(ErrorCode.APPOINT_DATE_INVALID);
        }

        // 5. 验证时段容量
        int currentCount = appointmentRepository.countBySlotForUpdate(
                req.getVaccineId(), req.getAppointmentDate(), req.getTimeSlot());
        int maxCapacity = getConfigInt(CONFIG_MAX_CAPACITY, DEFAULT_MAX_CAPACITY);
        if (currentCount >= maxCapacity) {
            throw new BusinessException(ErrorCode.APPOINT_SLOT_FULL);
        }

        // 6. 验证重复预约
        if (appointmentRepository.existsDuplicate(
                req.getChildId(), req.getVaccineId(), req.getAppointmentDate())) {
            throw new BusinessException(ErrorCode.APPOINT_DUPLICATE);
        }

        // 7. 创建预约
        Appointment appointment = Appointment.create(
                userId, req.getChildId(), req.getVaccineId(),
                req.getAppointmentDate(), req.getTimeSlot());

        // 8. 生成预约编号并保存
        String appointmentNo = appointmentRepository.generateAppointmentNo(req.getAppointmentDate());
        appointment.setAppointmentNo(appointmentNo);
        appointmentRepository.save(appointment);

        log.info("预约创建成功: userId={}, appointmentNo={}, childId={}, vaccineId={}",
                userId, appointmentNo, req.getChildId(), req.getVaccineId());

        return AppointmentAssembler.toResponse(appointment);
    }

    @Transactional
    public void cancel(Long appointmentId, AppointmentCancelRequest req) {
        Long userId = securityContextPort.getCurrentUserId();

        // 1. 查找预约
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINT_NOT_FOUND));

        // 2. 验证所有权
        if (!appointment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.APPOINT_NOT_OWN);
        }

        // 3. 验证可取消状态
        if (!appointment.isCancellable()) {
            throw new BusinessException(ErrorCode.APPOINT_CANCEL_FORBIDDEN);
        }

        // 4. 执行取消
        appointment.cancel(req.getReason());
        appointmentRepository.updateStatus(appointment);

        // 5. 释放已锁定的库存（如果已分配批次）
        if (appointment.getBatchId() != null) {
            vaccineStockRepository.releaseStock(appointment.getBatchId());
            log.info("预约取消释放库存: appointmentId={}, batchId={}", appointmentId, appointment.getBatchId());
        }

        log.info("预约取消成功: userId={}, appointmentId={}, reason={}",
                userId, appointmentId, req.getReason());
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> findByUserId(Integer status) {
        Long userId = securityContextPort.getCurrentUserId();
        List<Appointment> appointments = appointmentRepository.findByUserId(userId, status, 1, 100);
        return appointments.stream().map(this::enrichAppointmentResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AppointmentDetailResponse findById(Long appointmentId) {
        Long userId = securityContextPort.getCurrentUserId();

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINT_NOT_FOUND));

        if (!appointment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.APPOINT_NOT_OWN);
        }

        return enrichDetailResponse(AppointmentAssembler.toDetailResponse(appointment));
    }

    @Transactional(readOnly = true)
    public List<AppointmentDetailResponse> findByDate(LocalDate date) {
        List<Appointment> appointments = appointmentRepository.findByDate(date);
        return AppointmentAssembler.toDetailResponseList(appointments);
    }

    @Transactional(readOnly = true)
    public Map<String, Integer> getSlotAvailability(Long vaccineId, LocalDate date) {
        int maxCapacity = getConfigInt(CONFIG_MAX_CAPACITY, DEFAULT_MAX_CAPACITY);
        Map<String, Integer> countMap = appointmentRepository.countGroupBySlot(vaccineId, date);
        Map<String, Integer> result = new HashMap<>();
        for (String slot : APPOINTMENT_TIME_SLOTS) {
            int count = countMap.getOrDefault(slot, 0);
            result.put(slot, Math.max(0, maxCapacity - count));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public AppointmentDetailResponse findGuide(Long appointmentId) {
        Long userId = securityContextPort.getCurrentUserId();
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINT_NOT_FOUND));
        if (!appointment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.APPOINT_NOT_OWN);
        }
        return enrichDetailResponse(AppointmentAssembler.toDetailResponse(appointment));
    }

    @Transactional(readOnly = true)
    public QueueItemResponse findAppointmentQueue(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINT_NOT_FOUND));
        QueueItemResponse r = toQueueItem(appointment);
        // Calculate queue position
        int status = appointment.getStatus();
        List<Integer> statuses;
        if (status == 1) {
            statuses = List.of(1); // APPOINTED
        } else if (status == 6) {
            statuses = List.of(6); // SIGNED_IN
        } else if (status == 7) {
            statuses = List.of(7); // PRECHECK_PASS
        } else {
            statuses = List.of(status);
        }
        List<Appointment> queue = appointmentRepository.findByStatuses(statuses, appointment.getAppointmentDate());
        int position = 0;
        for (Appointment a : queue) {
            position++;
            if (a.getId().equals(appointmentId)) break;
        }
        r.setCurrentQueue(position);
        r.setEstimatedWaitMinutes(position * 10);
        return r;
    }

    // ==================== 队列查询 ====================

    @Transactional(readOnly = true)
    public List<QueueItemResponse> findQueueByStatus(List<Integer> statuses, LocalDate date) {
        List<Appointment> appointments = appointmentRepository.findByStatuses(statuses, date);
        return appointments.stream().map(this::toQueueItem).collect(Collectors.toList());
    }

    /**
     * 按当前医生窗口过滤的队列查询（预检/登记/接种用）
     */
    @Transactional(readOnly = true)
    public List<QueueItemResponse> findMyQueue(List<Integer> statuses, LocalDate date) {
        Long doctorId = securityContextPort.getCurrentUserId();
        String windowCode;
        try {
            windowCode = windowAssignmentService.getDoctorWindowCode(doctorId);
        } catch (BusinessException e) {
            log.warn("医生未分配窗口，返回空队列: doctorId={}", doctorId);
            return List.of();
        }
        List<Appointment> appointments = appointmentRepository.findByStatuses(statuses, date);
        return appointments.stream()
                .filter(a -> windowCode.equals(a.getCurrentWindow()))
                .map(this::toQueueItem)
                .collect(Collectors.toList());
    }

    /**
     * 预检工作台队列：未签到预约尚未分配窗口，需要对预检医生可见；已签到预约仅展示当前窗口。
     */
    @Transactional(readOnly = true)
    public List<QueueItemResponse> findMyPreCheckQueue(LocalDate date) {
        Long doctorId = securityContextPort.getCurrentUserId();
        String windowCode = null;
        try {
            HospitalWindow window = windowAssignmentService.getDoctorWindow(doctorId);
            if ("PRECHECK".equals(window.getWindowFunctionType())) {
                windowCode = window.getWindowCode();
            } else {
                log.warn("医生当前窗口不是预检窗口，仅展示未签到预检队列: doctorId={}, windowType={}",
                        doctorId, window.getWindowFunctionType());
            }
        } catch (BusinessException e) {
            log.warn("医生未分配窗口，仅展示未签到预检队列: doctorId={}", doctorId);
        }

        String currentWindowCode = windowCode;
        List<Appointment> appointments = appointmentRepository.findByStatuses(
                List.of(AppointmentStatus.APPOINTED.getCode(), AppointmentStatus.SIGNED_IN.getCode()), date);
        return appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.APPOINTED.getCode()
                        || (currentWindowCode != null && currentWindowCode.equals(a.getCurrentWindow())))
                .map(this::toQueueItem)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<QueueItemResponse> findObserveQueue(LocalDate date) {
        List<Appointment> appointments = appointmentRepository.findByStatuses(
                List.of(AppointmentStatus.OBSERVING.getCode()), date);

        return appointments.stream().map(a -> {
            QueueItemResponse r = toQueueItem(a);

            // 查找接种记录
            vaccinationRecordRepository.findByAppointmentId(a.getId())
                    .ifPresent(record -> {
                        r.setInjectionId(record.getId());
                        r.setInjectionNo(record.getInjectionId());
                        r.setInjectionTime(formatDateTime(record.getInjectionTime()));
                        if (record.getInjectionTime() != null) {
                            long elapsed = Duration.between(record.getInjectionTime(), LocalDateTime.now()).getSeconds();
                            r.setElapsedSeconds(elapsed);
                        }
                    });

            // 检查不良反应
            observeRecordRepository.findByAppointmentId(a.getId())
                    .ifPresent(observeRecord ->
                            r.setHasAdverseReaction(adverseReactionRepository.existsByObserveRecordId(observeRecord.getId())));

            return r;
        }).collect(Collectors.toList());
    }

    /**
     * 按当前医生窗口过滤的留观队列查询
     */
    @Transactional(readOnly = true)
    public List<QueueItemResponse> findMyObserveQueue(LocalDate date) {
        Long doctorId = securityContextPort.getCurrentUserId();
        String windowCode;
        try {
            windowCode = windowAssignmentService.getDoctorWindowCode(doctorId);
        } catch (BusinessException e) {
            log.warn("医生未分配窗口，返回空队列: doctorId={}", doctorId);
            return List.of();
        }
        List<Appointment> appointments = appointmentRepository.findByStatuses(
                List.of(AppointmentStatus.OBSERVING.getCode()), date);

        return appointments.stream()
                .filter(a -> windowCode.equals(a.getCurrentWindow()))
                .map(a -> {
                    QueueItemResponse r = toQueueItem(a);

                    vaccinationRecordRepository.findByAppointmentId(a.getId())
                            .ifPresent(record -> {
                                r.setInjectionId(record.getId());
                                r.setInjectionNo(record.getInjectionId());
                                r.setInjectionTime(formatDateTime(record.getInjectionTime()));
                                if (record.getInjectionTime() != null) {
                                    long elapsed = Duration.between(record.getInjectionTime(), LocalDateTime.now()).getSeconds();
                                    r.setElapsedSeconds(elapsed);
                                }
                            });

                    observeRecordRepository.findByAppointmentId(a.getId())
                            .ifPresent(observeRecord ->
                                    r.setHasAdverseReaction(adverseReactionRepository.existsByObserveRecordId(observeRecord.getId())));

                    return r;
                }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public QueueItemResponse findObserveDetail(Long injectionId) {
        return vaccinationRecordRepository.findById(injectionId)
                .map(record -> {
                    QueueItemResponse r = new QueueItemResponse();
                    r.setInjectionId(record.getId());
                    r.setInjectionNo(record.getInjectionId());
                    r.setInjectionTime(formatDateTime(record.getInjectionTime()));
                    if (record.getInjectionTime() != null) {
                        long elapsed = Duration.between(record.getInjectionTime(), LocalDateTime.now()).getSeconds();
                        r.setElapsedSeconds(elapsed);
                    }

                    // 查找预约信息
                    appointmentRepository.findById(record.getAppointmentId())
                            .ifPresent(appointment -> {
                                r.setAppointmentId(appointment.getId());
                                r.setChildId(appointment.getChildId());
                                r.setVaccineId(appointment.getVaccineId());
                                ChildProfile child = childProfileRepository.findById(appointment.getChildId());
                                if (child != null) {
                                    r.setChildName(child.getName());
                                }
                                vaccineRepository.findById(appointment.getVaccineId())
                                        .ifPresent(v -> r.setVaccineName(v.getVaccineName()));
                                if (record.getBatchNo() != null) {
                                    r.setQueueNo(record.getBatchNo());
                                }

                                // 检查不良反应
                                observeRecordRepository.findByAppointmentId(appointment.getId())
                                        .ifPresent(observeRecord ->
                                                r.setHasAdverseReaction(adverseReactionRepository.existsByObserveRecordId(observeRecord.getId())));
                            });

                    return r;
                }).orElse(null);
    }

    @Transactional
    public QueueItemResponse signin(SigninRequest req) {
        Appointment appointment = appointmentRepository.findByIdForUpdate(req.getAppointmentId());
        if (appointment == null) {
            throw new BusinessException(ErrorCode.APPOINT_NOT_FOUND);
        }
        if (!appointment.getAppointmentDate().equals(LocalDate.now())) {
            throw new BusinessException(ErrorCode.SIGNIN_STATUS_INVALID);
        }
        String windowCode = resolvePreCheckWindowForSignin();
        appointment.signin(windowCode);
        appointmentRepository.updateStatus(appointment);

        log.info("签到成功: appointmentId={}, window={}", req.getAppointmentId(), windowCode);
        return toQueueItem(appointment);
    }

    private String resolvePreCheckWindowForSignin() {
        Long doctorId = securityContextPort.getCurrentUserId();
        try {
            HospitalWindow window = windowAssignmentService.getDoctorWindow(doctorId);
            if ("PRECHECK".equals(window.getWindowFunctionType())) {
                return window.getWindowCode();
            }
        } catch (BusinessException ignored) {
            // 没有绑定预检窗口的签到医生继续使用自动分配。
        }
        return windowAssignmentService.assignToLeastBusyWindow("PRECHECK", LocalDate.now());
    }

    private QueueItemResponse toQueueItem(Appointment a) {
        QueueItemResponse r = new QueueItemResponse();
        r.setId(a.getId());
        r.setAppointmentId(a.getId());
        r.setChildId(a.getChildId());
        r.setVaccineId(a.getVaccineId());
        r.setQueueNo(a.getAppointmentNo());
        r.setAppointmentDate(a.getAppointmentDate() != null ? a.getAppointmentDate().toString() : null);
        r.setTimeSlot(a.getTimeSlot());
        r.setStatus(a.getStatus());
        r.setCurrentWindow(a.getCurrentWindow());
        r.setSigninTime(formatDateTime(a.getSigninTime()));
        r.setCreateTime(formatDateTime(a.getCreateTime()));
        r.setPrecheckTime(formatDateTime(a.getUpdateTime()));
        r.setRegisterTime(formatDateTime(a.getUpdateTime()));

        ChildProfile child = childProfileRepository.findById(a.getChildId());
        if (child != null) {
            r.setChildName(child.getName());
        }
        vaccineRepository.findById(a.getVaccineId())
                .ifPresent(vaccine -> r.setVaccineName(vaccine.getVaccineName()));

        return r;
    }

    private AppointmentResponse enrichAppointmentResponse(Appointment a) {
        AppointmentResponse resp = AppointmentAssembler.toResponse(a);
        ChildProfile child = childProfileRepository.findById(a.getChildId());
        if (child != null) {
            resp.setChildName(child.getName());
        }
        vaccineRepository.findById(a.getVaccineId())
                .ifPresent(v -> resp.setVaccineName(v.getVaccineName()));
        // 查询窗口名称
        if (a.getCurrentWindow() != null) {
            hospitalWindowRepository.findByCode(a.getCurrentWindow())
                    .ifPresent(window -> resp.setWindowName(window.getWindowName()));
        } else if (a.getStatus()== 1) {
            resolveSigninWindowForDisplay(resp);
        }
        return resp;
    }

    private AppointmentDetailResponse enrichDetailResponse(AppointmentDetailResponse resp) {
        if (resp.getChildId() != null) {
            ChildProfile child = childProfileRepository.findById(resp.getChildId());
            if (child != null) {
                resp.setChildName(child.getName());
                resp.setChildGender(child.getGender() != null ? child.getGender().getCode() : null);
                resp.setChildBirthDate(child.getBirthDate() != null ? child.getBirthDate().toString() : null);
            }
        }
        if (resp.getVaccineId() != null) {
            vaccineRepository.findById(resp.getVaccineId())
                    .ifPresent(v -> {
                        resp.setVaccineName(v.getVaccineName());
                        resp.setVaccineCategory(v.getVaccineType());
                        resp.setManufacturer(v.getManufacturer());
                    });
        }
        // 通过 currentWindow 查询窗口名称和对应医生
        if (resp.getCurrentWindow() != null) {
            hospitalWindowRepository.findByCode(resp.getCurrentWindow())
                    .ifPresent(window -> {
                        resp.setWindowName(window.getWindowName());
                        resp.setWindowFunctionType(window.getWindowFunctionType());
                        if (window.getDoctorId() != null) {
                            try {
                                var doctor = userRepository.findById(window.getDoctorId());
                                if (doctor != null) {
                                    resp.setDoctorName(doctor.getRealName());
                                }
                            } catch (Exception ignored) {}
                        }
                    });
        } else if (resp.getStatus() == 1) {
            resolveSigninWindowForDetailDisplay(resp);
        } else if (resp.getStatus() == 7) {
            resolveVaccinateWindowForDetailDisplay(resp);
        }
        return resp;
    }

    @Transactional(readOnly = true)
    public Map<String, Integer> getMyTodayStats() {
        LocalDate today = LocalDate.now();
        Map<Integer, Integer> statusCounts = appointmentRepository.countGroupByStatus(today);
        Map<String, Integer> result = new HashMap<>();
        result.put("appointed", statusCounts.getOrDefault(1, 0));
        result.put("signedIn", statusCounts.getOrDefault(6, 0));
        result.put("precheckPass", statusCounts.getOrDefault(7, 0));
        result.put("observing", statusCounts.getOrDefault(10, 0));
        result.put("completed", statusCounts.getOrDefault(2, 0));
        result.put("cancelled", statusCounts.getOrDefault(3, 0));
        return result;
    }

    /**
     * 待签到状态：查找一个启用的签到窗口供家长展示（仅展示用途，不执行分配）
     */
    private void resolveSigninWindowForDisplay(AppointmentResponse resp) {
        hospitalWindowRepository.findByFunctionType("PRECHECK").stream()
                .filter(HospitalWindow::isEnabled)
                .findFirst()
                .ifPresent(window -> resp.setWindowName(window.getWindowName()));
    }

    private void resolveSigninWindowForDetailDisplay(AppointmentDetailResponse resp) {
        hospitalWindowRepository.findByFunctionType("PRECHECK").stream()
                .filter(HospitalWindow::isEnabled)
                .findFirst()
                .ifPresent(window -> {
                    resp.setWindowName(window.getWindowName());
                    resp.setWindowFunctionType("PRECHECK");
                    if (window.getDoctorId() != null) {
                        try {
                            var doctor = userRepository.findById(window.getDoctorId());
                            if (doctor != null) {
                                resp.setDoctorName(doctor.getRealName());
                            }
                        } catch (Exception ignored) {}
                    }
                });
    }

    /**
     * 预检通过状态：查找一个启用的接种窗口供家长展示
     */
    private void resolveVaccinateWindowForDetailDisplay(AppointmentDetailResponse resp) {
        hospitalWindowRepository.findByFunctionType("VACCINATE").stream()
                .filter(HospitalWindow::isEnabled)
                .findFirst()
                .ifPresent(window -> {
                    resp.setWindowName(window.getWindowName());
                    resp.setWindowFunctionType("VACCINATE");
                    if (window.getDoctorId() != null) {
                        try {
                            var doctor = userRepository.findById(window.getDoctorId());
                            if (doctor != null) {
                                resp.setDoctorName(doctor.getRealName());
                            }
                        } catch (Exception ignored) {}
                    }
                });
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
    }

    private int getConfigInt(String key, int defaultValue) {
        SysConfig config = configRepository.findByKey(key);
        if (config == null || config.getConfigValue() == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(config.getConfigValue());
        } catch (NumberFormatException e) {
            log.warn("配置项 {} 的值不是有效整数: {}", key, config.getConfigValue());
            return defaultValue;
        }
    }
}
