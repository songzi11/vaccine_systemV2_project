package com.tjut.edu.vaccine.domain.stock.event;

/**
 * 批次处置事件
 */
public record BatchDisposedEvent(
        Long batchId,
        int quantity,
        String reason,
        Long operatorId
) {
}
