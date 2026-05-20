package com.tjut.edu.vaccine.application.service;

import com.tjut.edu.vaccine.application.assembler.PreCheckAssembler;
import com.tjut.edu.vaccine.application.dto.request.PreCheckAssessRequest;
import com.tjut.edu.vaccine.application.dto.response.PreCheckRecordResponse;
import com.tjut.edu.vaccine.common.enums.AppointmentStatus;
import com.tjut.edu.vaccine.common.enums.CheckResult;
import com.tjut.edu.vaccine.common.enums.ErrorCode;
import com.tjut.edu.vaccine.common.exception.BusinessException;
import com.tjut.edu.vaccine.domain.appointment.aggregate.Appointment;
import com.tjut.edu.vaccine.domain.appointment.repository.AppointmentRepository;
import com.tjut.edu.vaccine.domain.observe.entity.PreCheckRecord;
import com.tjut.edu.vaccine.domain.observe.repository.PreCheckRecordRepository;
import com.tjut.edu.vaccine.domain.port.SecurityContextPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreCheckApplicationService {

    private final AppointmentRepository appointmentRepository;
    private final PreCheckRecordRepository preCheckRecordRepository;
    private final SecurityContextPort securityContextPort;
    private final WindowAssignmentService windowAssignmentService;

    @Transactional
    public PreCheckRecordResponse assess(PreCheckAssessRequest req) {
        Long doctorId = securityContextPort.getCurrentUserId();

        // 1. 查找并锁定预约
        Appointment appointment = appointmentRepository.findByIdForUpdate(req.getAppointmentId());
        if (appointment == null) {
            throw new BusinessException(ErrorCode.APPOINT_NOT_FOUND);
        }

        // 2. 验证预约状态：必须是已签到(6)
        if (appointment.getStatus() != AppointmentStatus.SIGNED_IN.getCode()) {
            throw new BusinessException(ErrorCode.PRECHECK_STATUS_INVALID);
        }

        // 3. 创建预检记录
        PreCheckRecord record = new PreCheckRecord(
                req.getAppointmentId(), doctorId,
                req.getBodyTemperature(), req.getWeight(), req.getHeight(),
                req.getHealthStatus(), req.getAllergyHistory(), req.getMedicationRecent(),
                req.getDiseaseHistory(), req.getVaccinationRecent());

        // 4. 根据结果处理
        CheckResult result = CheckResult.fromCode(req.getResult());
        if (result == CheckResult.PASS) {
            record.pass();
            appointment.transitionStatus(AppointmentStatus.PRECHECK_PASS.getCode());
            // 自动分配到最空闲的接种窗口
            String windowCode = windowAssignmentService.assignToLeastBusyWindow("VACCINATE", LocalDate.now());
            appointment.assignToWindow(windowCode);
        } else {
            record.fail(req.getFailReason());
            appointment.transitionStatus(AppointmentStatus.PRECHECK_FAIL.getCode());
        }

        // 5. 保存
        preCheckRecordRepository.save(record);
        appointmentRepository.updateStatus(appointment);

        log.info("预检完成: appointmentId={}, doctorId={}, result={}",
                req.getAppointmentId(), doctorId, result.getCode());

        return PreCheckAssembler.toResponse(record);
    }

    @Transactional(readOnly = true)
    public PreCheckRecordResponse findByAppointmentId(Long appointmentId) {
        PreCheckRecord record = preCheckRecordRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRECHECK_APPOINTMENT_EXPIRED));
        return PreCheckAssembler.toResponse(record);
    }
}
