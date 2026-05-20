package com.tjut.edu.vaccine.application.service;

import com.tjut.edu.vaccine.application.assembler.ChildProfileAssembler;
import com.tjut.edu.vaccine.application.dto.request.ChildCreateRequest;
import com.tjut.edu.vaccine.application.dto.request.ChildUpdateRequest;
import com.tjut.edu.vaccine.application.dto.response.ChildResponse;
import com.tjut.edu.vaccine.common.enums.ErrorCode;
import com.tjut.edu.vaccine.common.enums.Gender;
import com.tjut.edu.vaccine.common.enums.IdCardType;
import com.tjut.edu.vaccine.common.exception.BusinessException;
import com.tjut.edu.vaccine.domain.appointment.repository.AppointmentRepository;
import com.tjut.edu.vaccine.domain.identity.aggregate.ChildProfile;
import com.tjut.edu.vaccine.domain.identity.repository.ChildProfileRepository;
import com.tjut.edu.vaccine.domain.port.SecurityContextPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChildProfileApplicationService {

    private final ChildProfileRepository childProfileRepository;
    private final AppointmentRepository appointmentRepository;
    private final SecurityContextPort securityContextPort;

    @Transactional
    public ChildResponse create(ChildCreateRequest req) {
        Long parentId = securityContextPort.getCurrentUserId();

        // 1. 验证儿童数量限制
        int currentCount = childProfileRepository.countByParentId(parentId);
        ChildProfile.validateChildCount(currentCount);

        // 2. 创建儿童档案
        Gender gender = Gender.fromCode(req.getGender());
        IdCardType idCardType = req.getIdCardType() != null ? IdCardType.fromCode(req.getIdCardType()) : null;
        ChildProfile child = ChildProfile.create(
                parentId, req.getName(), gender, req.getBirthDate(), req.getIdCardNo());

        if (idCardType != null) {
            child.setIdCardType(idCardType);
        }
        child.setNativePlace(req.getNativePlace());
        child.setNation(req.getNation());
        child.setMedicalHistory(req.getMedicalHistory());
        child.setAllergyHistory(req.getAllergyHistory());

        childProfileRepository.save(child);
        log.info("儿童档案创建成功: parentId={}, childId={}", parentId, child.getId().value());

        return ChildProfileAssembler.toResponse(child);
    }

    @Transactional
    public ChildResponse update(Long childId, ChildUpdateRequest req) {
        Long parentId = securityContextPort.getCurrentUserId();

        // 1. 查找并验证所有权
        ChildProfile child = childProfileRepository.findById(childId);
        if (child == null) {
            throw new BusinessException(ErrorCode.CHILD_NOT_FOUND);
        }
        if (!child.getParentId().equals(parentId)) {
            throw new BusinessException(ErrorCode.CHILD_NOT_OWN);
        }

        // 2. 更新字段
        Gender gender = req.getGender() != null ? Gender.fromCode(req.getGender()) : null;
        IdCardType idCardType = req.getIdCardType() != null ? IdCardType.fromCode(req.getIdCardType()) : null;
        child.update(req.getName(), gender, req.getBirthDate(), idCardType,
                req.getIdCardNo(), req.getNativePlace(), req.getNation(),
                req.getMedicalHistory(), req.getAllergyHistory());

        childProfileRepository.update(child);
        log.info("儿童档案更新成功: childId={}", childId);

        return ChildProfileAssembler.toResponse(child);
    }

    @Transactional(readOnly = true)
    public ChildResponse findById(Long childId) {
        ChildProfile child = childProfileRepository.findById(childId);
        if (child == null) {
            throw new BusinessException(ErrorCode.CHILD_NOT_FOUND);
        }
        Long parentId = securityContextPort.getCurrentUserId();
        if (!child.getParentId().equals(parentId)) {
            throw new BusinessException(ErrorCode.CHILD_NOT_OWN);
        }
        return ChildProfileAssembler.toResponse(child);
    }

    @Transactional(readOnly = true)
    public List<ChildResponse> findByParentId() {
        Long parentId = securityContextPort.getCurrentUserId();
        log.info("查询儿童列表: parentId={}", parentId);
        List<ChildProfile> children = childProfileRepository.findByParentId(parentId);
        log.info("查询儿童列表结果: parentId={}, count={}", parentId, children.size());
        return ChildProfileAssembler.toResponseList(children);
    }

    @Transactional
    public void deleteById(Long childId) {
        Long parentId = securityContextPort.getCurrentUserId();

        // 1. 查找并验证所有权
        ChildProfile child = childProfileRepository.findById(childId);
        if (child == null) {
            throw new BusinessException(ErrorCode.CHILD_NOT_FOUND);
        }
        if (!child.getParentId().equals(parentId)) {
            throw new BusinessException(ErrorCode.CHILD_NOT_OWN);
        }

        // 2. 检查是否存在未完成的预约
        List<?> inProgress = appointmentRepository.findInProgressByUserAndChild(parentId, childId);
        if (!inProgress.isEmpty()) {
            throw new BusinessException(ErrorCode.CHILD_HAS_APPOINTMENT);
        }

        childProfileRepository.deleteById(childId);
        log.info("儿童档案删除成功: childId={}", childId);
    }
}
