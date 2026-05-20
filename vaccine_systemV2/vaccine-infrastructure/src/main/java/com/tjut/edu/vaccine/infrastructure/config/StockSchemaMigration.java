package com.tjut.edu.vaccine.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockSchemaMigration implements InitializingBean {

    private static final String STOCK_TABLE = "hospital_vaccine_stock";
    private static final String TOTAL_STOCK_COLUMN = "total_stock";

    private final DataSource dataSource;

    @Override
    public void afterPropertiesSet() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            if (!hasColumn(connection)) {
                addTotalStockColumn(connection);
            }
            backfillTotalStock(connection);
        }
    }

    private boolean hasColumn(Connection connection) throws Exception {
        String sql = "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, STOCK_TABLE);
            statement.setString(2, TOTAL_STOCK_COLUMN);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    private void addTotalStockColumn(Connection connection) throws Exception {
        String sql = "ALTER TABLE hospital_vaccine_stock " +
                "ADD COLUMN total_stock INT NOT NULL DEFAULT 0 COMMENT '总库存（入库总数，接种后不变）' " +
                "AFTER location_id";
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
            log.info("已为 hospital_vaccine_stock 添加 total_stock 字段");
        }
    }

    private void backfillTotalStock(Connection connection) throws Exception {
        String sql = "UPDATE hospital_vaccine_stock " +
                "SET total_stock = available_stock + locked_stock " +
                "WHERE total_stock = 0 AND (available_stock > 0 OR locked_stock > 0)";
        try (Statement statement = connection.createStatement()) {
            int rows = statement.executeUpdate(sql);
            if (rows > 0) {
                log.info("已回填 hospital_vaccine_stock.total_stock: rows={}", rows);
            }
        }
    }
}
