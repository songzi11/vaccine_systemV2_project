package com.tjut.edu.vaccine.application.service;

import com.tjut.edu.vaccine.application.assembler.VaccinateAssembler;
import com.tjut.edu.vaccine.application.dto.request.VaccinateExecuteRequest;
import com.tjut.edu.vaccine.application.dto.response.FEFOBatchResponse;
import com.tjut.edu.vaccine.application.dto.response.VaccinateVerifyResponse;
import com.tjut.edu.vaccine.application.dto.response.VaccinationRecordResponse;
import com.tjut.edu.vaccine.common.enums.AppointmentStatus;
import com.tjut.edu.vaccine.common.enums.BatchStatus;
import com.tjut.edu.vaccine.common.enums.ErrorCode;
import com.tjut.edu.vaccine.common.exception.BusinessException;
import com.tjut.edu.vaccine.domain.appointment.aggregate.Appointment;
import com.tjut.edu.vaccine.domain.appointment.repository.AppointmentRepository;
import com.tjut.edu.vaccine.domain.identity.aggregate.ChildProfile;
import com.tjut.edu.vaccine.domain.identity.aggregate.User;
import com.tjut.edu.vaccine.domain.identity.entity.HospitalWindow;
import com.tjut.edu.vaccine.domain.identity.repository.ChildProfileRepository;
import com.tjut.edu.vaccine.domain.identity.repository.HospitalWindowRepository;
import com.tjut.edu.vaccine.domain.observe.aggregate.ObserveRecord;
import com.tjut.edu.vaccine.domain.observe.entity.PreCheckRecord;
import com.tjut.edu.vaccine.domain.observe.repository.ObserveRecordRepository;
import com.tjut.edu.vaccine.domain.observe.repository.PreCheckRecordRepository;
import com.tjut.edu.vaccine.domain.stock.aggregate.VaccineBatch;
import com.tjut.edu.vaccine.domain.stock.repository.VaccineBatchRepository;
import com.tjut.edu.vaccine.domain.stock.repository.VaccineRepository;
import com.tjut.edu.vaccine.domain.stock.repository.VaccineStockRepository;
import com.tjut.edu.vaccine.domain.vaccinate.aggregate.VaccinationRecord;
import com.tjut.edu.vaccine.domain.vaccinate.repository.VaccinationRecordRepository;
import com.tjut.edu.vaccine.domain.port.SecurityContextPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VaccinateApplicationService {

    @Value("${vaccine.default-hospital-id:1}")
    private Long defaultHospitalId;

    private final AppointmentRepository appointmentRepository;
    private final VaccinationRecordRepository vaccinationRecordRepository;
    private final VaccineBatchRepository vaccineBatchRepository;
    private final VaccineStockRepository vaccineStockRepository;
    private final ChildProfileRepository childProfileRepository;
    private final PreCheckRecordRepository preCheckRecordRepository;
    private final ObserveRecordRepository observeRecordRepository;
    private final VaccineRepository vaccineRepository;
    private final HospitalWindowRepository hospitalWindowRepository;
    private final com.tjut.edu.vaccine.domain.identity.repository.UserRepository userRepository;
    private final SecurityContextPort securityContextPort;
    private final WindowAssignmentService windowAssignmentService;

    @Transactional
    public VaccinationRecordResponse execute(VaccinateExecuteRequest req) {
        Long doctorId = securityContextPort.getCurrentUserId();

        // 1. 查找并锁定预约
        Appointment appointment = appointmentRepository.findByIdForUpdate(req.getAppointmentId());
        if (appointment == null) {
            throw new BusinessException(ErrorCode.APPOINT_NOT_FOUND);
        }

        // 2. 验证预约状态：必须是预检通过(7)
        if (appointment.getStatus() != AppointmentStatus.PRECHECK_PASS.getCode()) {
            throw new BusinessException(ErrorCode.VACCINATE_STATUS_INVALID);
        }

        // 3. FEFO批次选择（未指定batchId时自动选择）
        VaccineBatch batch;
        if (req.getBatchId() != null) {
            batch = vaccineBatchRepository.findById(req.getBatchId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.VACCINATE_BATCH_EXPIRED));
        } else {
            batch = vaccineBatchRepository.findAvailableForFEFO(appointment.getVaccineId(), defaultHospitalId);
            if (batch == null) {
                throw new BusinessException(ErrorCode.VACCINATE_BATCH_EXPIRED);
            }
        }
        if (!batch.isAvailable()) {
            throw new BusinessException(ErrorCode.VACCINATE_BATCH_EXPIRED);
        }

        // 4. 锁定库存
        try {
            vaccineStockRepository.lockStock(batch.getId(), defaultHospitalId);
        } catch (RuntimeException e) {
            log.error("库存锁定失败: batchId={}", batch.getId(), e);
            throw new BusinessException(ErrorCode.VACCINATE_STOCK_INSUFFICIENT);
        }

        // 5. 生成接种编号
        String injectionId = vaccinationRecordRepository.generateInjectionId(LocalDate.now());

        // 6. 创建接种记录
        VaccinationRecord record = VaccinationRecord.create(
                req.getAppointmentId(), doctorId,
                req.getInjectionSite(), batch.getId(), batch.getBatchNo());
        record.setInjectionId(injectionId);
        vaccinationRecordRepository.save(record);

        // 7. 扣减库存（消耗已锁定库存，接种出库）
        try {
            vaccineStockRepository.deductStock(batch.getId(), defaultHospitalId);
        } catch (RuntimeException e) {
            log.error("库存扣减失败: batchId={}", batch.getId(), e);
            throw new BusinessException(ErrorCode.VACCINATE_DEDUCT_FAILED);
        }

        // 8. 更新预约状态：预检通过(7) -> 留观中(10)
        appointment.transitionStatus(AppointmentStatus.OBSERVING.getCode());
        appointment.assignBatch(batch.getId());
        // 自动分配到最空闲的留观窗口
        String windowCode = windowAssignmentService.assignToLeastBusyWindow("OBSERVE", LocalDate.now());
        appointment.assignToWindow(windowCode);
        appointmentRepository.updateStatus(appointment);

        Long observeDoctorId = hospitalWindowRepository.findByCode(windowCode)
                .map(HospitalWindow::getDoctorId)
                .orElse(doctorId);
        observeRecordRepository.findByAppointmentId(appointment.getId())
                .orElseGet(() -> {
                    ObserveRecord observeRecord = new ObserveRecord(
                            appointment.getId(), record.getInjectionId(), observeDoctorId);
                    observeRecord.setStartTime(record.getInjectionTime());
                    observeRecordRepository.save(observeRecord);
                    return observeRecord;
                });

        log.info("接种执行成功: appointmentId={}, injectionId={}, batchId={}, doctorId={}",
                req.getAppointmentId(), injectionId, batch.getId(), doctorId);

        return enrichRecord(record);
    }

    @Transactional(readOnly = true)
    public VaccinationRecordResponse findRecordById(Long id) {
        VaccinationRecord record = vaccinationRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.VACCINATE_RECORD_NOT_FOUND));
        return enrichRecord(record);
    }

    @Transactional(readOnly = true)
    public List<VaccinationRecordResponse> findRecordsByUserId() {
        Long userId = securityContextPort.getCurrentUserId();
        List<String> roles = securityContextPort.getCurrentRoles();
        List<VaccinationRecord> records;
        boolean userOnly = roles.contains("USER")
                && roles.stream().noneMatch(r -> r.startsWith("DOCTOR_") || r.equals("SUPER_ADMIN"));
        if (userOnly) {
            records = vaccinationRecordRepository.findByUserId(userId);
        } else {
            records = vaccinationRecordRepository.findAll();
        }
        return records.stream().map(this::enrichRecord).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VaccinationRecordResponse> findRecordsByChildId(Long childId) {
        Long userId = securityContextPort.getCurrentUserId();

        // 验证儿童归属
        ChildProfile child = childProfileRepository.findById(childId);
        if (child == null || !child.getParentId().equals(userId)) {
            throw new BusinessException(ErrorCode.VACCINATE_NO_PERMISSION);
        }

        List<VaccinationRecord> records = vaccinationRecordRepository.findByChildId(childId);
        return records.stream().map(this::enrichRecord).collect(Collectors.toList());
    }

    private VaccinationRecordResponse enrichRecord(VaccinationRecord record) {
        VaccinationRecordResponse resp = VaccinateAssembler.toResponse(record);
        // 通过预约记录获取儿童和疫苗信息
        if (record.getAppointmentId() != null) {
            Appointment appointment = appointmentRepository.findById(record.getAppointmentId()).orElse(null);
            if (appointment != null) {
                ChildProfile child = childProfileRepository.findById(appointment.getChildId());
                if (child != null) {
                    resp.setChildName(child.getName());
                    resp.setChildGender(child.getGender() != null ? child.getGender().getCode() : null);
                    resp.setChildBirthDate(child.getBirthDate() != null ? child.getBirthDate().toString() : null);
                    resp.setChildId(child.getId().value());
                }
                vaccineRepository.findById(appointment.getVaccineId())
                        .ifPresent(v -> resp.setVaccineName(v.getVaccineName()));
            }
        }
        // 医生姓名
        if (record.getDoctorId() != null) {
            User doctor = userRepository.findById(record.getDoctorId());
            if (doctor != null) {
                resp.setDoctorName(doctor.getRealName());
            }
        }
        return resp;
    }

    @Transactional(readOnly = true)
    public FEFOBatchResponse findFEFOBatch(Long vaccineId) {
        VaccineBatch batch = vaccineBatchRepository.findAvailableForFEFO(vaccineId, defaultHospitalId);
        if (batch == null) {
            throw new BusinessException(ErrorCode.VACCINATE_BATCH_EXPIRED);
        }

        int availableStock = vaccineStockRepository.sumAvailableByVaccine(vaccineId);
        return VaccinateAssembler.toFEFOBatchResponse(batch, availableStock);
    }

    @Transactional(readOnly = true)
    public VaccinateVerifyResponse verify(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINT_NOT_FOUND));

        VaccinateVerifyResponse resp = new VaccinateVerifyResponse();
        resp.setAppointmentId(appointment.getId());
        resp.setAppointmentNo(appointment.getAppointmentNo());
        resp.setAppointmentDate(appointment.getAppointmentDate() != null ? appointment.getAppointmentDate().toString() : null);
        resp.setTimeSlot(appointment.getTimeSlot());

        // 儿童信息
        ChildProfile child = childProfileRepository.findById(appointment.getChildId());
        if (child != null) {
            resp.setChildName(child.getName());
            resp.setChildGender(child.getGender() != null ? child.getGender().getCode() : null);
            resp.setChildBirthDate(child.getBirthDate() != null ? child.getBirthDate().toString() : null);
        }

        // 疫苗名称
        vaccineRepository.findById(appointment.getVaccineId())
                .ifPresent(v -> resp.setVaccineName(v.getVaccineName()));

        // 预检信息
        preCheckRecordRepository.findByAppointmentId(appointmentId)
                .ifPresent(record -> {
                    resp.setBodyTemperature(record.getBodyTemperature());
                    resp.setHealthStatus(record.getHealthStatus());
                });

        // 批次信息：优先使用已分配批次，否则自动FEFO选择
        Long batchId = appointment.getBatchId();
        if (batchId == null) {
            List<VaccineBatch> batches = vaccineBatchRepository.findAvailableBatches(appointment.getVaccineId());
            if (!batches.isEmpty()) {
                batchId = batches.get(0).getId();
            }
        }
        if (batchId != null) {
            vaccineBatchRepository.findById(batchId)
                    .ifPresent(batch -> {
                        resp.setBatchId(batch.getId());
                        resp.setBatchNo(batch.getBatchNo());
                        resp.setManufacturer(batch.getManufacturer());
                        resp.setExpiryDate(batch.getExpiryDate() != null ? batch.getExpiryDate().toString() : null);
                        vaccineStockRepository.findByBatchId(batch.getId()).ifPresent(stock -> {
                            resp.setTotalStock(stock.getTotalStock());
                            resp.setAvailableStock(stock.getAvailableStock());
                            resp.setLockedStock(stock.getLockedStock());
                        });
                    });
        }

        return resp;
    }
}
