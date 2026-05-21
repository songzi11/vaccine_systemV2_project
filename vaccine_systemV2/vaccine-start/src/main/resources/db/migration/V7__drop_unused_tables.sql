-- 清理未使用的冗余表
-- register_queue: 登记逻辑已在 appointment 表实现
-- sms_verification: 短信验证功能未实现，系统使用 verify_code 表
-- sys_permission_audit: 权限审计功能未实现

DROP TABLE IF EXISTS `register_queue`;
DROP TABLE IF EXISTS `sms_verification`;
DROP TABLE IF EXISTS `sys_permission_audit`;
