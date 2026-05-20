package com.tjut.edu.vaccine.domain.stock.event;

/**
 * 库存预警触发事件
 */
public record StockAlertTriggeredEvent(
        Long alertId,
        String alertType,
        Long vaccineId,
        Long batchId
) {
}
