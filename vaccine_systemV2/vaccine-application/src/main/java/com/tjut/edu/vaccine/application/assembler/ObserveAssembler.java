package com.tjut.edu.vaccine.application.assembler;

import com.tjut.edu.vaccine.application.dto.response.AdverseReactionResponse;
import com.tjut.edu.vaccine.application.dto.response.ObserveRecordResponse;
import com.tjut.edu.vaccine.domain.observe.aggregate.ObserveRecord;
import com.tjut.edu.vaccine.domain.observe.entity.AdverseReaction;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ObserveAssembler {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static ObserveRecordResponse toResponse(ObserveRecord record) {
        if (record == null) {
            return null;
        }
        ObserveRecordResponse response = new ObserveRecordResponse();
        response.setId(record.getId());
        response.setAppointmentId(record.getAppointmentId());
        response.setInjectionId(record.getInjectionId());
        response.setStartTime(formatDateTime(record.getStartTime()));
        response.setFinishTime(formatDateTime(record.getFinishTime()));
        response.setDuration(record.getDuration());
        response.setObserveResult(record.getObserveResult() != null ? record.getObserveResult().getDescription() : null);
        response.setDoctorId(record.getDoctorId());
        response.setCreateTime(formatDateTime(record.getCreateTime()));
        return response;
    }

    public static AdverseReactionResponse toResponse(AdverseReaction reaction) {
        if (reaction == null) {
            return null;
        }
        AdverseReactionResponse response = new AdverseReactionResponse();
        response.setId(reaction.getId());
        response.setObserveRecordId(reaction.getObserveRecordId());
        response.setAppointmentId(reaction.getAppointmentId());
        response.setReactionType(reaction.getReactionType());
        response.setDescription(reaction.getDescription());
        response.setSeverity(reaction.getSeverity() != null ? reaction.getSeverity().getDescription() : null);
        response.setReportTime(formatDateTime(reaction.getReportTime()));
        response.setHandleTime(formatDateTime(reaction.getHandleTime()));
        response.setHandleResult(reaction.getHandleResult());
        response.setHandlerId(reaction.getHandlerId());
        response.setCreateTime(formatDateTime(reaction.getCreateTime()));
        return response;
    }

    public static List<AdverseReactionResponse> toAdverseReactionList(List<AdverseReaction> reactions) {
        if (reactions == null) {
            return List.of();
        }
        return reactions.stream()
                .map(ObserveAssembler::toResponse)
                .collect(Collectors.toList());
    }

    private static String formatDateTime(java.time.LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : null;
    }
}
