package com.tjut.edu.vaccine.application.service;

import com.tjut.edu.vaccine.common.enums.AppointmentStatus;
import com.tjut.edu.vaccine.common.enums.ErrorCode;
import com.tjut.edu.vaccine.common.exception.BusinessException;
import com.tjut.edu.vaccine.domain.appointment.repository.AppointmentRepository;
import com.tjut.edu.vaccine.domain.identity.entity.DoctorSchedule;
import com.tjut.edu.vaccine.domain.identity.entity.HospitalWindow;
import com.tjut.edu.vaccine.domain.identity.repository.DoctorScheduleRepository;
import com.tjut.edu.vaccine.domain.identity.repository.HospitalWindowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WindowAssignmentService {

    private final HospitalWindowRepository hospitalWindowRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final AppointmentRepository appointmentRepository;

    /**
     * 窗口功能类型 → 该窗口对应的排队状态
     */
    private static final Map<String, List<Integer>> WINDOW_TYPE_TO_QUEUE_STATUSES = Map.of(
            "SIGNIN", List.of(AppointmentStatus.APPOINTED.getCode()),
            "PRECHECK", List.of(AppointmentStatus.SIGNED_IN.getCode()),
            "REGISTER", List.of(AppointmentStatus.PRECHECK_PASS.getCode()),
            "VACCINATE", List.of(AppointmentStatus.PRECHECK_PASS.getCode()),
            "OBSERVE", List.of(AppointmentStatus.OBSERVING.getCode())
    );

    /**
     * 分配患者到最空闲的窗口
     *
     * @param functionType 窗口功能类型 (PRECHECK/REGISTER/VACCINATE/OBSERVE)
     * @param date         预约日期
     * @return 分配的窗口编码
     */
    public String assignToLeastBusyWindow(String functionType, LocalDate date) {
        List<HospitalWindow> windows = hospitalWindowRepository.findByFunctionType(functionType);
        if (windows.isEmpty()) {
            throw new BusinessException(ErrorCode.WINDOW_NOT_AVAILABLE);
        }

        // 过滤：启用 + 有医生 + 医生未请假
        String currentSlot = getCurrentTimeSlot();
        List<HospitalWindow> available = windows.stream()
                .filter(HospitalWindow::isEnabled)
                .filter(w -> w.getDoctorId() != null)
                .filter(w -> !isDoctorOnLeave(w.getDoctorId(), date, currentSlot))
                .toList();

        if (available.isEmpty()) {
            throw new BusinessException(ErrorCode.WINDOW_NOT_AVAILABLE);
        }

        // 统计每个窗口的排队数
        List<Integer> queueStatuses = WINDOW_TYPE_TO_QUEUE_STATUSES.getOrDefault(functionType, List.of());

        HospitalWindow best = null;
        int minCount = Integer.MAX_VALUE;

        for (HospitalWindow w : available) {
            int count = appointmentRepository.countByWindowAndStatus(w.getWindowCode(), queueStatuses, date);
            if (best == null || count < minCount || (count == minCount && w.getSortOrder() < best.getSortOrder())) {
                minCount = count;
                best = w;
            }
        }

        log.info("窗口自动分配: functionType={}, assignedWindow={}, queueSize={}",
                functionType, best.getWindowCode(), minCount);
        return best.getWindowCode();
    }

    /**
     * 获取医生当前分配的窗口
     */
    public HospitalWindow getDoctorWindow(Long doctorId) {
        return hospitalWindowRepository.findByDoctorId(doctorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WINDOW_NOT_ASSIGNED));
    }

    /**
     * 获取医生当前窗口编码
     */
    public String getDoctorWindowCode(Long doctorId) {
        return getDoctorWindow(doctorId).getWindowCode();
    }

    private String getCurrentTimeSlot() {
        int hour = LocalTime.now().getHour();
        return hour < 13 ? "AM" : "PM";
    }

    private boolean isDoctorOnLeave(Long doctorId, LocalDate date, String slot) {
        List<DoctorSchedule> schedules = doctorScheduleRepository.findByDoctorId(doctorId, date, date);
        for (DoctorSchedule s : schedules) {
            if (s.getTimeSlot().equals(slot) && !s.isNormal()) {
                return true;
            }
        }
        return false;
    }
}
