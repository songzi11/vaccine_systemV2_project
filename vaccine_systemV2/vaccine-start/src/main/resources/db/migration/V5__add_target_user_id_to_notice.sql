-- 新增 system_notice.target_user_id 字段（个人通知目标用户，NULL=广播）
ALTER TABLE system_notice ADD COLUMN target_user_id BIGINT DEFAULT NULL COMMENT '目标用户ID（NULL表示广播公告）' AFTER author_id;
CREATE INDEX idx_target_user_id ON system_notice(target_user_id);

-- 更新爽约冻结天数配置为30天（一个月）
UPDATE sys_config SET config_value = '30' WHERE config_key = 'no_show.freeze_days';
