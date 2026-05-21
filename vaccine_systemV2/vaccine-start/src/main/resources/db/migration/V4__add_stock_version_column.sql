-- Add optimistic lock version column to hospital_vaccine_stock
ALTER TABLE hospital_vaccine_stock ADD COLUMN version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号';
