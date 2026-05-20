package com.tjut.edu.vaccine.application.service;

import com.tjut.edu.vaccine.application.assembler.ObserveAssembler;
import com.tjut.edu.vaccine.application.dto.request.AdverseReactionHandleRequest;
import com.tjut.edu.vaccine.application.dto.request.AdverseReactionRequest;
import com.tjut.edu.vaccine.application.dto.request.ObserveFinishRequest;
import com.tjut.edu.vaccine.application.dto.request.ObserveStartRequest;
import com.tjut.edu.vaccine.application.dto.response.AdverseReactionResponse;
import com.tjut.edu.vaccine.application.dto.response.ObserveRecordResponse;
import com.tjut.edu.vaccine.common.enums.AppointmentStatus;
import com.tjut.edu.vaccine.common.enums.ErrorCode;
import com.tjut.edu.vaccine.common.enums.ObserveResult;
import com.tjut.edu.vaccine.common.enums.Severity;
import com.tjut.edu.vaccine.common.exception.BusinessException;
import com.tjut.edu.vaccine.domain.appointment.aggregate.Appointment;
import com.tjut.edu.vaccine.domain.appointment.repository.AppointmentRepository;
import com.tjut.edu.vaccine.domain.observe.aggregate.ObserveRecord;
import com.tjut.edu.vaccine.domain.observe.entity.AdverseReaction;
import com.tjut.edu.vaccine.domain.observe.repository.AdverseReactionRepository;
import com.tjut.edu.vaccine.domain.observe.repository.ObserveRecordRepository;
import com.tjut.edu.vaccine.domain.port.SecurityContextPort;
import com.tjut.edu.vaccine.domain.vaccinate.aggregate.VaccinationRecord;
import com.tjut.edu.vaccine.domain.vaccinate.repository.VaccinationRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ObserveApplicationService {

    private static final int MIN_OBSERVE_MINUTES = 30;

    private final AppointmentRepository appointmentRepository;
    private final ObserveRecordRepository observeRecordRepository;
    private final AdverseReactionRepository adverseReactionRepository;
    private final VaccinationRecordRepository vaccinationRecordRepository;
    private final SecurityContextPort securityContextPort;

    @Transactional
    public ObserveRecordResponse start(ObserveStartRequest req) {
        Long doctorId = securityContextPort.getCurrentUserId();

        // 1. 查找并锁定预约
        Appointment appointment = appointmentRepository.findByIdForUpdate(req.getAppointmentId());
        if (appointment == null) {
            throw new BusinessException(ErrorCode.APPOINT_NOT_FOUND);
        }

        // 2. 验证预约状态：必须是留观中(10)
        if (appointment.getStatus() != AppointmentStatus.OBSERVING.getCode()) {
            throw new BusinessException(ErrorCode.OBSERVE_STATUS_INVALID);
        }

        ObserveRecord existingRecord = observeRecordRepository.findByAppointmentId(req.getAppointmentId()).orElse(null);
        if (existingRecord != null) {
            return ObserveAssembler.toResponse(existingRecord);
        }

        // 3. 创建留观记录
        ObserveRecord record = new ObserveRecord(
                req.getAppointmentId(), req.getInjectionId(), doctorId);
        observeRecordRepository.save(record);

        log.info("留观开始: appointmentId={}, injectionId={}, doctorId={}",
                req.getAppointmentId(), req.getInjectionId(), doctorId);

        return ObserveAssembler.toResponse(record);
    }

    @Transactional
    public ObserveRecordResponse finish(Long id, ObserveFinishRequest req) {
        // 1. 查找并锁定预约
        Appointment appointment = appointmentRepository.findByIdForUpdate(id);
        if (appointment == null) {
            throw new BusinessException(ErrorCode.APPOINT_NOT_FOUND);
        }

        // 2. 查找留观记录，兼容已进入留观但历史上未生成记录的数据
        Optional<ObserveRecord> existingRecord = observeRecordRepository.findByAppointmentId(id);

        // 3. 已完成的留观允许幂等返回，同时修正仍停留在留观中的预约状态
        if (existingRecord.isPresent() && existingRecord.get().isFinished()) {
            completeAppointmentIfObserving(appointment);
            return ObserveAssembler.toResponse(existingRecord.get());
        }

        if (appointment.getStatus() != AppointmentStatus.OBSERVING.getCode()) {
            throw new BusinessException(ErrorCode.OBSERVE_STATUS_INVALID);
        }

        ObserveRecord record = existingRecord.orElseGet(() -> createObserveRecordFromVaccination(id));

        // 4. 完成留观（服务端自动计算实际经过时长）
        record.finish();

        // 6. 如果异常，检查是否已上报不良反应
        if (record.getObserveResult() == ObserveResult.ABNORMAL) {
            boolean hasReport = adverseReactionRepository.existsByObserveRecordId(record.getId());
            if (!hasReport) {
                throw new BusinessException(ErrorCode.ADVERSE_NOT_REPORTED);
            }
        }

        // 7. 更新留观记录
        observeRecordRepository.update(record);

        // 8. 更新预约状态：留观中(10) -> 已完成(2)
        completeAppointmentIfObserving(appointment);

        log.info("留观完成: appointmentId={}, duration={}min, result={}",
                id, req.getDurationMinutes(), record.getObserveResult().getCode());

        return ObserveAssembler.toResponse(record);
    }

    @Transactional
    public AdverseReactionResponse reportAdverseReaction(AdverseReactionRequest req) {
        // 1. 查找或创建留观记录
        Long observeRecordId = req.getObserveRecordId();
        if (observeRecordId == null) {
            // 根据预约ID查找留观记录
            observeRecordId = findOrCreateObserveRecord(req.getAppointmentId()).getId();
        }
        ObserveRecord record = observeRecordRepository.findById(observeRecordId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINT_NOT_FOUND));

        // 2. 标记留观异常
        record.markAbnormal();
        observeRecordRepository.update(record);

        // 3. 创建不良反应记录
        Severity severity = Severity.fromCode(req.getSeverity());
        AdverseReaction reaction = new AdverseReaction(
                observeRecordId, req.getAppointmentId(),
                req.getReactionType(), req.getDescription(), severity);
        adverseReactionRepository.save(reaction);

        log.info("不良反应上报: appointmentId={}, observeRecordId={}, severity={}",
                req.getAppointmentId(), observeRecordId, severity.getCode());

        return ObserveAssembler.toResponse(reaction);
    }

    @Transactional
    public AdverseReactionResponse handleAdverseReaction(Long id, AdverseReactionHandleRequest req) {
        Long handlerId = securityContextPort.getCurrentUserId();

        // 1. 查找不良反应记录
        AdverseReaction reaction = adverseReactionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINT_NOT_FOUND));

        // 2. 处理
        reaction.handle(req.getHandleResult(), handlerId);
        adverseReactionRepository.update(reaction);

        log.info("不良反应处理: reactionId={}, handlerId={}", id, handlerId);

        return ObserveAssembler.toResponse(reaction);
    }

    @Transactional(readOnly = true)
    public List<AdverseReactionResponse> findAdverseReactions(Long observeRecordId) {
        List<AdverseReaction> reactions = adverseReactionRepository.findByObserveRecordId(observeRecordId);
        return ObserveAssembler.toAdverseReactionList(reactions);
    }

    private ObserveRecord findOrCreateObserveRecord(Long appointmentId) {
        return observeRecordRepository.findByAppointmentId(appointmentId)
                .orElseGet(() -> createObserveRecordFromVaccination(appointmentId));
    }

    private ObserveRecord createObserveRecordFromVaccination(Long appointmentId) {
        VaccinationRecord vaccinationRecord = vaccinationRecordRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VACCINATE_RECORD_NOT_FOUND));
        ObserveRecord observeRecord = new ObserveRecord(
                appointmentId, vaccinationRecord.getInjectionId(), securityContextPort.getCurrentUserId());
        observeRecord.setStartTime(vaccinationRecord.getInjectionTime());
        observeRecordRepository.save(observeRecord);
        log.info("补建留观记录: appointmentId={}, injectionId={}",
                appointmentId, vaccinationRecord.getInjectionId());
        return observeRecord;
    }

    private void completeAppointmentIfObserving(Appointment appointment) {
        if (appointment.getStatus() == AppointmentStatus.OBSERVING.getCode()) {
            appointment.transitionStatus(AppointmentStatus.COMPLETED.getCode());
            appointmentRepository.updateStatus(appointment);
        }
    }
}
