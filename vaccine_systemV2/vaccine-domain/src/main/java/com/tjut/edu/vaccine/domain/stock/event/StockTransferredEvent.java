package com.tjut.edu.vaccine.domain.stock.event;

/**
 * 库存调拨事件
 */
public record StockTransferredEvent(
        String transferNo,
        Long batchId,
        int quantity,
        Long operatorId
) {
}
