-- 新增 sys_user.freeze_reason 字段（管理员冻结原因）
ALTER TABLE sys_user ADD COLUMN freeze_reason VARCHAR(255) DEFAULT NULL COMMENT '冻结原因' AFTER freeze_end_time;
