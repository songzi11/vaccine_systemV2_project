SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;


-- Records of adverse_reaction
INSERT INTO `adverse_reaction` VALUES (1, 1, 8, 'FEVER', '接种后体温38.2℃，持续约4小时后自行退热', 'MILD', '2026-04-05 19:45:01', '2026-04-05 20:45:01', '物理降温后体温恢复正常，无需用药', 51, '2026-04-05 19:45:01');
INSERT INTO `adverse_reaction` VALUES (2, 4, 14, 'LOCAL_REACTION', '接种部位红肿直径约3cm，伴有轻微疼痛', 'MILD', '2026-03-29 16:45:01', '2026-03-30 16:45:01', '冷敷处理后红肿消退', 51, '2026-03-29 16:45:01');
INSERT INTO `adverse_reaction` VALUES (3, 5, 15, 'ALLERGIC_REACTION', '接种后出现全身皮疹，伴轻度瘙痒', 'MODERATE', '2026-03-26 16:45:01', '2026-03-27 16:45:01', '口服氯雷他定后皮疹消退，观察24小时无复发', 51, '2026-03-26 16:45:01');

-- Records of appointment
INSERT INTO `appointment` VALUES (8, 'APT202604050001', 46, 18, 1, '2026-04-05', 'AM', 2, 'SIGNIN_01', '2026-04-05 18:17:34', NULL, NULL, 87, '2026-04-02 00:00:00', '2026-04-05 16:17:34');
INSERT INTO `appointment` VALUES (9, 'APT202604040002', 46, 18, 2, '2026-04-04', 'PM', 2, 'SIGNIN_01', '2026-04-04 20:17:34', NULL, NULL, 88, '2026-04-01 16:17:34', '2026-04-04 20:17:34');
INSERT INTO `appointment` VALUES (10, 'APT202604020001', 47, 20, 3, '2026-04-02', 'AM', 2, 'SIGNIN_02', '2026-04-02 16:17:34', NULL, NULL, 89, '2026-03-31 16:17:34', '2026-04-02 16:17:34');
INSERT INTO `appointment` VALUES (11, 'APT202603310001', 46, 19, 4, '2026-04-03', 'PM', 3, NULL, NULL, '2026-04-02 16:17:34', '孩子发烧，暂时不能接种', NULL, '2026-03-31 16:17:34', '2026-04-02 16:17:34');
INSERT INTO `appointment` VALUES (12, 'APT202604050002', 47, 21, 5, '2026-04-05', 'PM', 1, NULL, NULL, NULL, NULL, NULL, '2026-04-04 00:00:00', '2026-04-05 16:17:34');
INSERT INTO `appointment` VALUES (13, 'APT202604050003', 46, 18, 6, '2026-04-05', 'AM', 6, 'SIGNIN_01', '2026-04-05 17:17:34', NULL, NULL, NULL, '2026-04-03 00:00:00', '2026-04-05 16:17:34');
INSERT INTO `appointment` VALUES (14, 'APT202603290001', 47, 21, 1, '2026-03-29', 'AM', 2, 'SIGNIN_01', '2026-03-29 16:17:34', NULL, NULL, 87, '2026-03-27 16:17:34', '2026-03-29 16:17:34');
INSERT INTO `appointment` VALUES (15, 'APT202603260001', 46, 18, 4, '2026-03-26', 'AM', 2, 'SIGNIN_02', '2026-03-26 16:17:34', NULL, NULL, 90, '2026-03-24 16:17:34', '2026-03-26 16:17:34');
INSERT INTO `appointment` VALUES (16, 'APT202603210001', 47, 20, 2, '2026-03-21', 'PM', 2, 'SIGNIN_01', '2026-03-21 16:17:34', NULL, NULL, 88, '2026-03-19 16:17:34', '2026-03-21 16:17:34');
INSERT INTO `appointment` VALUES (17, CONCAT('APT', DATE_FORMAT(CURDATE(), '%Y%m%d'), '9001'), 46, 14, 1, CURDATE(), 'AM', 7, '05', NOW(), NULL, NULL, 87, NOW(), NOW());

-- Records of batch_dispose_log

-- Records of child_profile
INSERT INTO `child_profile` VALUES (13, 46, '120101199001011234', '小亮', 1, '2026-04-01', 1, '1', '天津市', '汉', '无', '无', '2026-04-05 08:40:31', '2026-04-05 15:18:35');
INSERT INTO `child_profile` VALUES (14, 46, '120101199001011234', '小红', 2, '2024-03-10', 1, '120101202403100025', '天津市', '汉', '无', '鸡蛋过敏', '2026-04-05 08:40:31', '2026-04-05 08:40:31');
INSERT INTO `child_profile` VALUES (15, 47, '120101199501015678', '小刚', 1, '2022-11-20', 1, '120101202211200033', '天津市', '汉', '无', '无', '2026-04-05 08:40:31', '2026-04-05 08:40:31');
INSERT INTO `child_profile` VALUES (16, 46, NULL, '小名', 1, '2026-04-01', 1, '12121', NULL, NULL, NULL, NULL, '2026-04-05 15:19:12', '2026-04-05 15:19:12');
INSERT INTO `child_profile` VALUES (21, 46, NULL, '张小明', 1, '2023-06-15', 1, NULL, '天津', '汉族', NULL, NULL, '2024-01-10 10:00:00', '2026-04-05 16:17:34');
INSERT INTO `child_profile` VALUES (22, 46, NULL, '张小红', 2, '2024-03-20', 1, NULL, '天津', '汉族', NULL, NULL, '2024-04-05 14:00:00', '2026-04-05 16:17:34');
INSERT INTO `child_profile` VALUES (23, 47, NULL, '李小宝', 1, '2024-01-10', 1, NULL, '北京', '汉族', NULL, NULL, '2024-02-20 09:00:00', '2026-04-05 16:17:34');
INSERT INTO `child_profile` VALUES (24, 47, NULL, '李小花', 2, '2023-11-05', 1, NULL, '北京', '汉族', NULL, NULL, '2024-01-15 11:00:00', '2026-04-05 16:17:34');

-- Records of doctor_schedule

-- Records of hospital_vaccine_stock
INSERT INTO `hospital_vaccine_stock` VALUES (1, 1, 16, 0, 1, 100, 99, 0);
INSERT INTO `hospital_vaccine_stock` VALUES (2, 1, 17, 0, 1, 100, 99, 1);
INSERT INTO `hospital_vaccine_stock` VALUES (3, 1, 18, 0, 1, 100, 99, 1);
INSERT INTO `hospital_vaccine_stock` VALUES (4, 1, 19, 0, 1, 100, 99, 1);
INSERT INTO `hospital_vaccine_stock` VALUES (5, 1, 20, 0, 1, 100, 99, 1);
INSERT INTO `hospital_vaccine_stock` VALUES (6, 1, 21, 0, 1, 100, 99, 1);
INSERT INTO `hospital_vaccine_stock` VALUES (7, 1, 22, 0, 1, 100, 99, 1);
INSERT INTO `hospital_vaccine_stock` VALUES (8, 1, 23, 0, 1, 100, 99, 1);
INSERT INTO `hospital_vaccine_stock` VALUES (9, 1, 24, 0, 1, 100, 99, 1);
INSERT INTO `hospital_vaccine_stock` VALUES (10, 1, 25, 0, 1, 100, 99, 1);
INSERT INTO `hospital_vaccine_stock` VALUES (11, 1, 26, 0, 1, 100, 99, 1);
INSERT INTO `hospital_vaccine_stock` VALUES (12, 1, 27, 0, 1, 100, 99, 1);
INSERT INTO `hospital_vaccine_stock` VALUES (13, 1, 28, 0, 1, 100, 99, 1);
INSERT INTO `hospital_vaccine_stock` VALUES (14, 1, 29, 0, 1, 100, 99, 1);
INSERT INTO `hospital_vaccine_stock` VALUES (15, 1, 30, 0, 1, 100, 99, 1);
INSERT INTO `hospital_vaccine_stock` VALUES (16, 1, 31, 0, 1, 100, 99, 1);
INSERT INTO `hospital_vaccine_stock` VALUES (17, 1, 32, 0, 1, 100, 99, 1);
INSERT INTO `hospital_vaccine_stock` VALUES (35, 1, 42, 0, 1, 3, 3, 0);
INSERT INTO `hospital_vaccine_stock` VALUES (56, 1, 38, 0, 1, 100, 100, 0);
INSERT INTO `hospital_vaccine_stock` VALUES (104, 1, 87, 0, 1, 100, 100, 0);
INSERT INTO `hospital_vaccine_stock` VALUES (105, 1, 88, 0, 1, 100, 100, 0);
INSERT INTO `hospital_vaccine_stock` VALUES (106, 1, 89, 0, 1, 100, 100, 0);
INSERT INTO `hospital_vaccine_stock` VALUES (107, 1, 90, 0, 1, 100, 100, 0);
INSERT INTO `hospital_vaccine_stock` VALUES (108, 1, 91, 0, 1, 100, 100, 0);
INSERT INTO `hospital_vaccine_stock` VALUES (109, 1, 92, 0, 1, 100, 100, 0);
INSERT INTO `hospital_vaccine_stock` VALUES (110, 1, 93, 0, 1, 100, 100, 0);
INSERT INTO `hospital_vaccine_stock` VALUES (111, 1, 94, 0, 1, 100, 100, 0);

-- Records of hospital_vaccine_summary
INSERT INTO `hospital_vaccine_summary` VALUES (36, 1, 1, 100, 100, 20, 0, '2026-04-05 16:17:34');
INSERT INTO `hospital_vaccine_summary` VALUES (37, 1, 2, 100, 100, 20, 0, '2026-04-05 16:17:34');
INSERT INTO `hospital_vaccine_summary` VALUES (38, 1, 3, 100, 100, 20, 0, '2026-04-05 16:17:34');
INSERT INTO `hospital_vaccine_summary` VALUES (39, 1, 4, 100, 100, 20, 0, '2026-04-05 16:17:34');
INSERT INTO `hospital_vaccine_summary` VALUES (40, 1, 5, 100, 100, 20, 0, '2026-04-05 16:17:34');
INSERT INTO `hospital_vaccine_summary` VALUES (41, 1, 6, 100, 100, 20, 0, '2026-04-05 16:17:34');
INSERT INTO `hospital_vaccine_summary` VALUES (42, 1, 7, 100, 100, 20, 0, '2026-04-05 16:17:34');
INSERT INTO `hospital_vaccine_summary` VALUES (43, 1, 8, 100, 100, 20, 0, '2026-04-05 16:17:34');
INSERT INTO `hospital_vaccine_summary` VALUES (44, 1, 9, 100, 100, 20, 0, '2026-04-05 16:17:34');
INSERT INTO `hospital_vaccine_summary` VALUES (45, 1, 10, 100, 100, 20, 0, '2026-04-05 16:17:34');
INSERT INTO `hospital_vaccine_summary` VALUES (46, 1, 11, 100, 100, 20, 0, '2026-04-05 16:17:34');
INSERT INTO `hospital_vaccine_summary` VALUES (47, 1, 12, 100, 100, 20, 0, '2026-04-05 16:17:34');
INSERT INTO `hospital_vaccine_summary` VALUES (48, 1, 13, 100, 100, 20, 0, '2026-04-05 16:17:34');
INSERT INTO `hospital_vaccine_summary` VALUES (49, 1, 14, 100, 100, 20, 0, '2026-04-05 16:17:34');
INSERT INTO `hospital_vaccine_summary` VALUES (50, 1, 15, 100, 100, 20, 0, '2026-04-05 16:17:34');
INSERT INTO `hospital_vaccine_summary` VALUES (51, 1, 16, 100, 100, 20, 0, '2026-04-05 16:17:34');
INSERT INTO `hospital_vaccine_summary` VALUES (52, 1, 17, 100, 100, 20, 0, '2026-04-05 16:17:34');
INSERT INTO `hospital_vaccine_summary` VALUES (53, 1, 18, 100, 100, 20, 0, '2026-04-05 16:17:34');
INSERT INTO `hospital_vaccine_summary` VALUES (54, 1, 19, 100, 100, 20, 0, '2026-04-05 16:17:34');
INSERT INTO `hospital_vaccine_summary` VALUES (55, 1, 20, 100, 100, 20, 0, '2026-04-05 16:17:34');

-- Records of hospital_window
INSERT INTO `hospital_window` VALUES (9, '01', '签到窗口', 'SIGNIN', 0, 50, 1, 1, 57, '2026-04-05 11:45:06', '2026-04-05 11:59:42');
INSERT INTO `hospital_window` VALUES (10, '02', '预检窗口01', 'PRECHECK', 0, 50, 5, 2, 59, '2026-04-05 11:45:51', '2026-04-05 12:34:05');
INSERT INTO `hospital_window` VALUES (11, '03', '预检窗口02', 'PRECHECK', 0, 50, 5, 2, 59, '2026-04-05 11:46:21', '2026-04-05 12:02:48');
INSERT INTO `hospital_window` VALUES (12, '04', '登记窗口01', 'REGISTER', 0, 50, 1, 3, 49, '2026-04-05 12:04:38', '2026-04-05 12:34:05');
INSERT INTO `hospital_window` VALUES (13, '05', '接种窗口01', 'VACCINATE', 0, 50, 5, 4, 50, '2026-04-05 12:08:20', '2026-04-05 12:34:05');
INSERT INTO `hospital_window` VALUES (14, '06', '接种窗口02', 'VACCINATE', 0, 50, 5, 4, 50, '2026-04-05 12:08:52', '2026-04-05 12:34:05');
INSERT INTO `hospital_window` VALUES (15, '07', '留观医生', 'OBSERVE', 0, 1, 5, 5, 51, '2026-04-05 12:09:41', '2026-04-05 12:34:05');

-- Records of notice_feedback

-- Records of observe_record
INSERT INTO `observe_record` VALUES (1, 8, 'INJ202604050001', '2026-04-05 19:17:34', '2026-04-05 19:17:34', 30, 'NORMAL', 51, '2026-04-05 16:17:34');
INSERT INTO `observe_record` VALUES (2, 9, 'INJ202604040002', '2026-04-04 22:17:34', '2026-04-04 23:17:34', 30, 'NORMAL', 51, '2026-04-04 23:17:34');
INSERT INTO `observe_record` VALUES (3, 10, 'INJ202604020001', '2026-04-02 16:17:34', '2026-04-02 16:17:34', 30, 'NORMAL', 51, '2026-04-02 16:17:34');
INSERT INTO `observe_record` VALUES (4, 14, 'INJ202603290001', '2026-03-29 16:17:34', '2026-03-29 16:17:34', 30, 'NORMAL', 51, '2026-03-29 16:17:34');
INSERT INTO `observe_record` VALUES (5, 15, 'INJ202603260001', '2026-03-26 16:17:34', '2026-03-26 16:17:34', 30, 'NORMAL', 51, '2026-03-26 16:17:34');
INSERT INTO `observe_record` VALUES (6, 16, 'INJ202603210001', '2026-03-21 16:17:34', '2026-03-21 16:17:34', 30, 'NORMAL', 51, '2026-03-21 16:17:34');

-- Records of pre_check_record
INSERT INTO `pre_check_record` VALUES (2, 8, '2026-04-05 18:17:34', 36.5, 10.5, 75.0, 'GOOD', '无', '无', NULL, NULL, 'PASS', NULL, 48, '2026-04-05 16:17:34');
INSERT INTO `pre_check_record` VALUES (3, 9, '2026-04-04 20:17:34', 36.8, 10.5, 76.0, 'GOOD', '无', '无', NULL, NULL, 'PASS', NULL, 48, '2026-04-04 20:17:34');
INSERT INTO `pre_check_record` VALUES (4, 10, '2026-04-02 16:17:34', 36.6, 12.0, 88.0, 'GOOD', '无', '无', NULL, NULL, 'PASS', NULL, 48, '2026-04-02 16:17:34');
INSERT INTO `pre_check_record` VALUES (5, 14, '2026-03-29 16:17:34', 36.7, 11.0, 82.0, 'GOOD', '无', '无', NULL, NULL, 'PASS', NULL, 48, '2026-03-29 16:17:34');
INSERT INTO `pre_check_record` VALUES (6, 15, '2026-03-26 16:17:34', 36.4, 10.8, 76.0, 'GOOD', '无', '无', NULL, NULL, 'PASS', NULL, 48, '2026-03-26 16:17:34');
INSERT INTO `pre_check_record` VALUES (7, 16, '2026-03-21 16:17:34', 36.5, 12.0, 89.0, 'GOOD', '无', '无', NULL, NULL, 'PASS', NULL, 48, '2026-03-21 16:17:34');

-- Records of register_queue
INSERT INTO `register_queue` VALUES (2, 8, '2026-04-05 18:17:34', 49, 'A001', 87, 'BATCH-20250101', 1, '2026-04-05 18:17:34', '2026-04-05 16:17:34', '2026-04-05 16:17:34');
INSERT INTO `register_queue` VALUES (3, 9, '2026-04-04 21:17:34', 49, 'A002', 88, 'BATCH-20250201', 1, '2026-04-04 21:17:34', '2026-04-04 21:17:34', '2026-04-05 16:17:34');
INSERT INTO `register_queue` VALUES (4, 10, '2026-04-02 16:17:34', 49, 'A003', 89, 'BATCH-20250301', 1, '2026-04-02 16:17:34', '2026-04-02 16:17:34', '2026-04-05 16:17:34');
INSERT INTO `register_queue` VALUES (5, 14, '2026-03-29 16:17:34', 49, 'A004', 87, 'BATCH-20250101', 1, '2026-03-29 16:17:34', '2026-03-29 16:17:34', '2026-04-05 16:17:34');
INSERT INTO `register_queue` VALUES (6, 15, '2026-03-26 16:17:34', 49, 'A005', 90, 'BATCH-20250401', 1, '2026-03-26 16:17:34', '2026-03-26 16:17:34', '2026-04-05 16:17:34');
INSERT INTO `register_queue` VALUES (7, 16, '2026-03-21 16:17:34', 49, 'A006', 88, 'BATCH-20250201', 1, '2026-03-21 16:17:34', '2026-03-21 16:17:34', '2026-04-05 16:17:34');

-- Records of sms_verification

-- Records of stock_alert_log
INSERT INTO `stock_alert_log` VALUES (1, 'EXPIRY_SOON', 5, 39, 30.00, '2026-04-15', 0, '2026-04-05 08:44:22');
INSERT INTO `stock_alert_log` VALUES (2, 'EXPIRY_SOON', 7, 40, 30.00, '2026-04-20', 0, '2026-04-05 08:44:22');
INSERT INTO `stock_alert_log` VALUES (3, 'EXPIRY_SOON', 8, 41, 30.00, '2026-04-10', 0, '2026-04-05 08:44:22');
INSERT INTO `stock_alert_log` VALUES (4, 'EXPIRED', 9, 42, 0.00, '2025-12-01', 0, '2026-04-05 08:44:22');
INSERT INTO `stock_alert_log` VALUES (5, 'LOW_STOCK', 8, 41, 5.00, NULL, 0, '2026-04-05 08:44:22');

-- Records of stock_transfer_log

-- Records of sys_config
INSERT INTO `sys_config` VALUES (1, 'appointment.max_capacity', '50', '每时段最大预约数', 'INT', '2026-04-04 21:06:32');
INSERT INTO `sys_config` VALUES (2, 'appointment.advance_days', '30', '可提前预约天数', 'INT', '2026-04-05 15:56:16');
INSERT INTO `sys_config` VALUES (3, 'no_show.freeze_threshold', '3', '爽约冻结阈值（次）', 'INT', '2026-04-04 21:06:32');
INSERT INTO `sys_config` VALUES (4, 'no_show.freeze_days', '7', '爽约冻结天数', 'INT', '2026-04-04 21:06:32');
INSERT INTO `sys_config` VALUES (5, 'observe.min_duration', '30', '最短留观时长（分钟）', 'INT', '2026-04-04 21:06:32');
INSERT INTO `sys_config` VALUES (6, 'stock.expiry_warning_days', '30', '批次临期预警天数', 'INT', '2026-04-04 21:06:32');
INSERT INTO `sys_config` VALUES (7, 'stock.low_threshold_percent', '20', '库存预警阈值百分比', 'INT', '2026-04-04 21:06:32');
INSERT INTO `sys_config` VALUES (8, 'schedule.cron_expire', '0 30 0 * * ?', '预约过期扫描 Cron 表达式', 'STRING', '2026-04-04 21:06:32');
INSERT INTO `sys_config` VALUES (9, 'batch.expiry_scan_interval', '3600', '批次过期扫描间隔（分钟）', 'INT', '2026-04-05 15:56:49');

-- Records of sys_permission
INSERT INTO `sys_permission` VALUES (1, 'appointment.book', '预约接种', '预约', '用户预约疫苗接种', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (2, 'appointment.cancel.own', '取消自己的预约', '预约', '用户取消自己的预约', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (3, 'appointment.view.own', '查看自己的预约', '预约', '用户查看自己的预约列表', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (4, 'appointment.signin', '用户签到', '流程', '用户到院签到', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (5, 'appointment.confirm', '预约确认', '流程', '确认预约信息', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (6, 'appointment.view.today', '查看今日预约', '流程', '查看今日待签到预约', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (7, 'appointment.view.queue', '查看待预检队列', '流程', '查看待预检队列', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (8, 'appointment.view.register', '查看待登记队列', '流程', '查看待登记队列', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (9, 'appointment.view.vaccinate', '查看待接种队列', '流程', '查看待接种队列', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (10, 'appointment.view.observe', '查看留观队列', '流程', '查看当前留观队列', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (11, 'precheck.assess', '预检评估', '流程', '执行预检评估', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (12, 'precheck.contraindication', '禁忌筛查', '流程', '执行禁忌症筛查', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (13, 'precheck.result.view', '查看预检结果', '流程', '查看预检结果详情', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (14, 'register.verify', '登记核实', '登记', '核实登记信息', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (15, 'register.batch.assign', '批次分配', '登记', 'FEFO分配疫苗批次', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (16, 'register.queue.manage', '排队管理', '登记', '管理登记排队', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (17, 'register.view', '查看登记详情', '登记', '查看登记详细信息', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (18, 'register.save', '保存登记记录', '登记', '保存登记记录', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (19, 'vaccinate.execute', '执行接种', '接种', '执行疫苗接种', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (20, 'vaccinate.record', '记录接种信息', '接种', '记录接种详细信息', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (21, 'vaccinate.verify', '核实信息', '接种', '核实接种信息', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (22, 'vaccinate.site.select', '选择接种部位', '接种', '选择接种部位', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (23, 'vaccinate.id.generate', '生成注射号', '接种', '生成注射号', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (24, 'vaccinate.view', '查看接种详情', '接种', '查看接种详情', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (25, 'record.view.own', '查看自己的接种记录', '接种', '查看自己的接种记录', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (26, 'record.view.child', '查看儿童接种记录', '接种', '查看指定儿童的接种记录', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (27, 'stock.view', '查看库存', '库存', '查看疫苗库存信息', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (28, 'stock.transfer', '库存调拨', '库存', '调拨疫苗库存', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (29, 'stock.disposal', '批次销毁', '库存', '销毁过期或损坏批次', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (30, 'stock.lock', '锁定批次库存', '库存', '登记时锁定库存', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (31, 'stock.deduct', '扣减批次库存', '库存', '接种时扣减库存', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (32, 'stock.transfer.create', '创建调拨单', '库存', '创建库存调拨单', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (33, 'stock.transfer.confirm', '确认调拨', '库存', '确认库存调拨', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (34, 'stock.transfer.view', '查看调拨记录', '库存', '查看调拨记录', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (35, 'stock.alert.view', '查看库存预警', '库存', '查看库存预警信息', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (36, 'batch.manage', '批次管理', '库存', '批次信息管理', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (37, 'batch.view', '查看批次列表', '库存', '查看批次列表和详情', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (38, 'child.view.own', '查看儿童档案', '用户', '查看自己的儿童档案', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (39, 'child.add.own', '添加儿童档案', '用户', '添加儿童档案', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (40, 'child.edit.own', '修改儿童档案', '用户', '修改自己的儿童档案', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (41, 'child.delete.own', '删除儿童档案', '用户', '删除自己的儿童档案', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (42, 'observe.manage', '留观管理', '留观', '管理留观记录', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (43, 'observe.finish', '确认留观结束', '留观', '确认留观结束', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (44, 'adverse.report', '上报不良反应', '留观', '上报不良反应', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (45, 'adverse.handle', '处理不良反应', '留观', '处理不良反应记录', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (46, 'doctor.schedule.view', '查看排班', '排班', '查看医生排班信息', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (47, 'doctor.schedule.create', '创建排班', '排班', '创建排班记录', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (48, 'doctor.schedule.edit', '修改排班', '排班', '修改排班信息', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (49, 'doctor.schedule.delete', '删除排班', '排班', '删除排班记录', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (50, 'doctor.assign.role', '分配医生角色', '管理', '给医生分配角色', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (51, 'doctor.assign.permission', '分配医生权限', '管理', '给医生分配额外权限', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (52, 'window.manage', '窗口管理', '管理', '窗口增删改查', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (53, 'window.service.manage', '窗口服务管理', '管理', '窗口可办理业务说明管理', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (54, 'user.manage', '用户管理', '管理', '用户增删改查、冻结', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (55, 'notice.manage', '公告管理', '管理', '公告增删改查', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (56, 'notice.audit', '公告审批', '管理', '审批医生提交的公告', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (57, 'notice.view', '查看公告', '通用', '系统公告', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (58, 'notice.feedback', '提交公告反馈', '通用', '提交公告意见反馈', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (59, 'vaccine.catalog.view', '查看疫苗目录', '通用', '查看可预约的疫苗列表', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (60, 'stats.view', '统计分析', '管理', '查看各类统计数据', '2026-04-04 21:06:32');
INSERT INTO `sys_permission` VALUES (61, 'all.data.view', '全局数据查看', '管理', '查看系统所有数据', '2026-04-04 21:06:32');

-- Records of sys_permission_audit

-- Records of sys_role
INSERT INTO `sys_role` VALUES (1, 'USER', '用户', 'USER', '普通用户，可预约、管理儿童档案、查看记录', 0, 1, '2026-04-04 21:06:32', '2026-04-04 21:06:32');
INSERT INTO `sys_role` VALUES (2, 'DOCTOR_SIGNIN', '签到医生', 'DOCTOR', '负责用户签到和预约确认', 0, 1, '2026-04-04 21:06:32', '2026-04-04 21:06:32');
INSERT INTO `sys_role` VALUES (3, 'DOCTOR_PRECHECK', '预检医生', 'DOCTOR', '负责预检评估和禁忌筛查', 0, 1, '2026-04-04 21:06:32', '2026-04-04 21:06:32');
INSERT INTO `sys_role` VALUES (4, 'DOCTOR_REGISTER', '登记医生', 'DOCTOR', '负责登记核实和批次分配', 0, 1, '2026-04-04 21:06:32', '2026-04-04 21:06:32');
INSERT INTO `sys_role` VALUES (5, 'DOCTOR_VACCINATE', '接种医生', 'DOCTOR', '负责执行疫苗接种和记录', 0, 1, '2026-04-04 21:06:32', '2026-04-04 21:06:32');
INSERT INTO `sys_role` VALUES (6, 'DOCTOR_OBSERVE', '留观医生', 'DOCTOR', '负责留观管理和不良反应处理', 0, 1, '2026-04-04 21:06:32', '2026-04-04 21:06:32');
INSERT INTO `sys_role` VALUES (7, 'DOCTOR_STOCK', '库存管理医生', 'DOCTOR', '负责库存查询、调拨、销毁', 0, 1, '2026-04-04 21:06:32', '2026-04-04 21:06:32');
INSERT INTO `sys_role` VALUES (9, 'DOCTOR_BUSINESS_ADMIN', '业务主管', 'ADMIN', '负责排班管理、窗口管理、疫苗管理和公告管理', 0, 1, '2026-04-04 21:06:32', '2026-04-05 08:27:05');
INSERT INTO `sys_role` VALUES (10, 'SUPER_ADMIN', '系统管理员', 'ADMIN', '系统级最高权限，不受其他限制', 0, 1, '2026-04-04 21:06:32', '2026-04-04 21:06:32');

-- Records of sys_role_permission
INSERT INTO `sys_role_permission` VALUES (1, 1, 1, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (2, 1, 2, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (3, 1, 3, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (4, 1, 39, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (5, 1, 40, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (6, 1, 38, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (7, 1, 58, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (8, 1, 57, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (9, 1, 25, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (10, 1, 59, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (16, 2, 5, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (17, 2, 4, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (18, 2, 6, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (19, 2, 57, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (23, 3, 7, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (24, 3, 57, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (25, 3, 11, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (26, 3, 12, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (27, 3, 13, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (30, 4, 8, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (31, 4, 57, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (32, 4, 15, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (33, 4, 16, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (34, 4, 14, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (37, 5, 9, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (38, 5, 57, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (39, 5, 26, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (40, 5, 25, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (41, 5, 19, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (42, 5, 20, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (82, 5, 21, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (83, 5, 22, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (84, 5, 23, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (85, 5, 24, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (86, 5, 31, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (44, 6, 45, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (45, 6, 44, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (46, 6, 10, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (47, 6, 57, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (48, 6, 43, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (49, 6, 42, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (51, 7, 36, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (52, 7, 57, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (53, 7, 29, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (54, 7, 28, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (55, 7, 27, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (56, 7, 59, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (65, 9, 51, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (66, 9, 50, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (67, 9, 46, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (68, 9, 57, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (69, 9, 59, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (70, 9, 52, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (71, 9, 53, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (72, 10, 61, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (73, 10, 56, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (74, 10, 55, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (75, 10, 57, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (76, 10, 60, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (77, 10, 54, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (78, 10, 59, '2026-04-04 21:06:32');
INSERT INTO `sys_role_permission` VALUES (79, 9, 47, '2026-04-05 08:27:05');
INSERT INTO `sys_role_permission` VALUES (80, 9, 49, '2026-04-05 08:27:05');
INSERT INTO `sys_role_permission` VALUES (81, 9, 48, '2026-04-05 08:27:05');

-- Records of sys_user
INSERT INTO `sys_user` VALUES (46, '13800000001', '13800000001', '$2b$10$uqOJRzH.Sfir2e/Xa9LJdOWhqIhtVUvKCvoeUEVQrA.7XasIhaALm', '张三', 1, 1, NULL, 0, 0, NULL, NULL, '2026-04-05 08:40:31', '2026-04-05 12:24:47');
INSERT INTO `sys_user` VALUES (47, '13800000002', '13800000002', '$2b$10$uqOJRzH.Sfir2e/Xa9LJdOWhqIhtVUvKCvoeUEVQrA.7XasIhaALm', '李四', 2, 1, NULL, 0, 0, NULL, NULL, '2026-04-05 08:40:31', '2026-04-05 12:24:49');
INSERT INTO `sys_user` VALUES (48, '13900000001', '13900000001', '$2b$10$uqOJRzH.Sfir2e/Xa9LJdOWhqIhtVUvKCvoeUEVQrA.7XasIhaALm', '王医生', 1, 1, NULL, 0, 0, NULL, NULL, '2026-04-05 08:40:31', '2026-04-05 12:23:46');
INSERT INTO `sys_user` VALUES (49, '13900000002', '13900000002', '$2b$10$uqOJRzH.Sfir2e/Xa9LJdOWhqIhtVUvKCvoeUEVQrA.7XasIhaALm', '赵医生', 1, 1, NULL, 0, 0, NULL, NULL, '2026-04-05 08:40:31', '2026-04-05 12:23:51');
INSERT INTO `sys_user` VALUES (50, '13900000003', '13900000003', '$2b$10$uqOJRzH.Sfir2e/Xa9LJdOWhqIhtVUvKCvoeUEVQrA.7XasIhaALm', '钱医生', 1, 1, NULL, 0, 0, NULL, NULL, '2026-04-05 08:40:31', '2026-04-05 12:23:56');
INSERT INTO `sys_user` VALUES (51, '13900000004', '13900000004', '$2b$10$uqOJRzH.Sfir2e/Xa9LJdOWhqIhtVUvKCvoeUEVQrA.7XasIhaALm', '孙医生', 2, 1, NULL, 0, 0, NULL, NULL, '2026-04-05 08:40:31', '2026-04-05 12:23:59');
INSERT INTO `sys_user` VALUES (52, '13900000005', '13900000005', '$2b$10$uqOJRzH.Sfir2e/Xa9LJdOWhqIhtVUvKCvoeUEVQrA.7XasIhaALm', '周医生', 1, 1, NULL, 0, 0, NULL, NULL, '2026-04-05 08:40:31', '2026-04-05 12:24:02');
INSERT INTO `sys_user` VALUES (53, '13900000006', '13900000006', '$2b$10$uqOJRzH.Sfir2e/Xa9LJdOWhqIhtVUvKCvoeUEVQrA.7XasIhaALm', '吴医生', 1, 1, NULL, 0, 0, NULL, NULL, '2026-04-05 08:40:31', '2026-04-05 12:24:06');
INSERT INTO `sys_user` VALUES (54, '13900000007', '13900000007', '$2b$10$uqOJRzH.Sfir2e/Xa9LJdOWhqIhtVUvKCvoeUEVQrA.7XasIhaALm', '郑医生', 1, 1, NULL, 0, 0, NULL, NULL, '2026-04-05 08:40:31', '2026-04-05 12:24:10');
INSERT INTO `sys_user` VALUES (55, '13900000008', '13900000008', '$2b$10$uqOJRzH.Sfir2e/Xa9LJdOWhqIhtVUvKCvoeUEVQrA.7XasIhaALm', '冯医生', 1, 1, NULL, 0, 0, NULL, NULL, '2026-04-05 08:40:31', '2026-04-05 12:24:16');
INSERT INTO `sys_user` VALUES (56, '13700000001', '13700000001', '$2b$10$uqOJRzH.Sfir2e/Xa9LJdOWhqIhtVUvKCvoeUEVQrA.7XasIhaALm', '管理员Admin', 1, 1, NULL, 0, 0, NULL, NULL, '2026-04-05 08:40:31', '2026-04-05 08:40:31');
INSERT INTO `sys_user` VALUES (57, '13600000001', '13600000001', '$2b$10$uqOJRzH.Sfir2e/Xa9LJdOWhqIhtVUvKCvoeUEVQrA.7XasIhaALm', '陈医生', 1, 1, NULL, 0, 0, NULL, NULL, '2026-04-05 10:34:27', '2026-04-05 12:24:19');
INSERT INTO `sys_user` VALUES (58, '13600000002', '13600000002', '$2b$10$uqOJRzH.Sfir2e/Xa9LJdOWhqIhtVUvKCvoeUEVQrA.7XasIhaALm', '林医生', 2, 1, NULL, 0, 0, NULL, NULL, '2026-04-05 10:34:27', '2026-04-05 12:24:22');
INSERT INTO `sys_user` VALUES (59, '13600000003', '13600000003', '$2b$10$uqOJRzH.Sfir2e/Xa9LJdOWhqIhtVUvKCvoeUEVQrA.7XasIhaALm', '黄医生', 1, 1, NULL, 0, 0, NULL, NULL, '2026-04-05 10:34:27', '2026-04-05 12:24:24');
INSERT INTO `sys_user` VALUES (60, '13600000004', '13600000004', '$2b$10$uqOJRzH.Sfir2e/Xa9LJdOWhqIhtVUvKCvoeUEVQrA.7XasIhaALm', '杨医生', 2, 1, NULL, 0, 0, NULL, NULL, '2026-04-05 10:34:27', '2026-04-05 12:24:25');
INSERT INTO `sys_user` VALUES (61, '13600000005', '13600000005', '$2b$10$uqOJRzH.Sfir2e/Xa9LJdOWhqIhtVUvKCvoeUEVQrA.7XasIhaALm', '许医生', 1, 1, NULL, 0, 0, NULL, NULL, '2026-04-05 10:34:27', '2026-04-05 12:24:28');
INSERT INTO `sys_user` VALUES (62, '13600000006', '13600000006', '$2b$10$uqOJRzH.Sfir2e/Xa9LJdOWhqIhtVUvKCvoeUEVQrA.7XasIhaALm', '何医生', 2, 1, NULL, 0, 0, NULL, NULL, '2026-04-05 10:34:27', '2026-04-05 12:24:30');
INSERT INTO `sys_user` VALUES (63, '13600000007', '13600000007', '$2b$10$uqOJRzH.Sfir2e/Xa9LJdOWhqIhtVUvKCvoeUEVQrA.7XasIhaALm', '曹医生', 1, 1, NULL, 0, 0, NULL, NULL, '2026-04-05 10:34:27', '2026-04-05 12:24:31');
INSERT INTO `sys_user` VALUES (64, '13600000008', '13600000008', '$2b$10$uqOJRzH.Sfir2e/Xa9LJdOWhqIhtVUvKCvoeUEVQrA.7XasIhaALm', '徐医生', 2, 1, NULL, 0, 0, NULL, NULL, '2026-04-05 10:34:27', '2026-04-05 12:24:33');
INSERT INTO `sys_user` VALUES (65, '13600000009', '13600000009', '$2b$10$uqOJRzH.Sfir2e/Xa9LJdOWhqIhtVUvKCvoeUEVQrA.7XasIhaALm', '谢医生', 1, 1, NULL, 0, 0, NULL, NULL, '2026-04-05 10:34:27', '2026-04-05 12:24:34');
INSERT INTO `sys_user` VALUES (66, '13600000010', '13600000010', '$2b$10$uqOJRzH.Sfir2e/Xa9LJdOWhqIhtVUvKCvoeUEVQrA.7XasIhaALm', '马医生', 2, 1, NULL, 0, 0, NULL, NULL, '2026-04-05 10:34:27', '2026-04-05 12:24:36');
INSERT INTO `sys_user` VALUES (67, '13600000011', '13600000011', '$2b$10$uqOJRzH.Sfir2e/Xa9LJdOWhqIhtVUvKCvoeUEVQrA.7XasIhaALm', '罗医生', 1, 1, NULL, 0, 0, NULL, NULL, '2026-04-05 10:34:27', '2026-04-05 12:24:39');

-- Records of sys_user_permission

-- Records of sys_user_role
INSERT INTO `sys_user_role` VALUES (45, 46, 1, '2026-04-05 08:40:31');
INSERT INTO `sys_user_role` VALUES (46, 47, 1, '2026-04-05 08:40:31');
INSERT INTO `sys_user_role` VALUES (49, 49, 4, '2026-04-05 08:40:31');
INSERT INTO `sys_user_role` VALUES (50, 50, 5, '2026-04-05 08:40:31');
INSERT INTO `sys_user_role` VALUES (51, 51, 6, '2026-04-05 08:40:31');
INSERT INTO `sys_user_role` VALUES (52, 52, 7, '2026-04-05 08:40:31');
INSERT INTO `sys_user_role` VALUES (53, 55, 9, '2026-04-05 08:40:31');
INSERT INTO `sys_user_role` VALUES (54, 56, 10, '2026-04-05 08:40:31');
INSERT INTO `sys_user_role` VALUES (60, 62, 4, '2026-04-05 10:34:27');
INSERT INTO `sys_user_role` VALUES (64, 66, 6, '2026-04-05 10:34:27');
INSERT INTO `sys_user_role` VALUES (65, 67, 7, '2026-04-05 10:34:27');
INSERT INTO `sys_user_role` VALUES (73, 57, 2, '2026-04-05 11:45:14');
INSERT INTO `sys_user_role` VALUES (75, 59, 3, '2026-04-05 12:02:48');
INSERT INTO `sys_user_role` VALUES (76, 61, 4, '2026-04-05 12:04:48');
INSERT INTO `sys_user_role` VALUES (77, 63, 5, '2026-04-05 12:08:28');
INSERT INTO `sys_user_role` VALUES (78, 64, 5, '2026-04-05 12:08:59');
INSERT INTO `sys_user_role` VALUES (79, 65, 6, '2026-04-05 12:10:11');

-- Records of system_notice
INSERT INTO `system_notice` VALUES (5, '系统测试', '12121', 'SYSTEM', 2, 55, 55, '2026-04-05 09:03:30', NULL, '2026-04-05 09:03:30', NULL, NULL, '2026-04-05 09:00:19', '2026-04-05 09:33:54');
INSERT INTO `system_notice` VALUES (6, '内部测试', '123', 'INTERNAL', 1, 55, 55, '2026-04-05 09:33:09', NULL, '2026-04-05 09:33:09', '2026-04-01 00:00:00', '2026-04-30 00:00:00', '2026-04-05 09:33:01', '2026-04-05 09:33:09');
INSERT INTO `system_notice` VALUES (7, '系统测试', '123', 'SYSTEM', 1, 55, 55, '2026-04-05 09:33:48', NULL, '2026-04-05 09:33:48', '2026-04-01 00:00:00', '2026-04-30 00:00:00', '2026-04-05 09:33:41', '2026-04-05 09:33:48');
INSERT INTO `system_notice` VALUES (14, '【测试】春季疫苗接种通知', '春季疫苗接种工作即将开始请及时预约接种。', 'SYSTEM', 0, 55, NULL, NULL, NULL, NULL, '2026-04-01 00:00:00', '2026-06-30 00:00:00', '2026-04-05 09:51:06', '2026-04-05 09:52:10');
INSERT INTO `system_notice` VALUES (15, '【测试】节假日接种安排', '五一期间正常接种请提前预约。', 'SYSTEM', 1, 55, 56, '2026-04-05 09:52:10', NULL, '2026-04-05 09:52:10', '2026-04-20 00:00:00', '2026-05-10 00:00:00', '2026-04-05 09:51:38', '2026-04-05 09:52:10');
INSERT INTO `system_notice` VALUES (16, '【测试】内部排班调整通知', '下周一至周三接种窗口1由王医生值班。', 'INTERNAL', 1, 55, 56, '2026-04-05 09:52:10', NULL, '2026-04-05 09:52:10', '2026-04-01 00:00:00', '2026-12-31 00:00:00', '2026-04-05 09:51:48', '2026-04-05 09:52:10');
INSERT INTO `system_notice` VALUES (17, '【测试】疫苗到货通知', '四价流感疫苗200支请及时领取。', 'SYSTEM', 3, 55, 56, '2026-04-05 09:52:10', 'rejected', NULL, '2026-04-01 00:00:00', '2026-06-30 00:00:00', '2026-04-05 09:52:00', '2026-04-05 09:52:10');
INSERT INTO `system_notice` VALUES (18, '【测试】过期公告示例', '此公告有效期已过用于测试过期状态。', 'SYSTEM', 1, 55, 56, '2026-04-05 09:52:10', NULL, '2026-04-05 09:52:10', '2026-01-01 00:00:00', '2026-03-31 00:00:00', '2026-04-05 09:52:00', '2026-04-05 09:52:10');
INSERT INTO `system_notice` VALUES (19, '【测试】已下架有效期内公告', '有效期内被下架可重新上架。', 'SYSTEM', 2, 55, 56, '2026-04-05 09:52:10', NULL, '2026-04-05 09:52:10', '2026-04-01 00:00:00', '2026-12-31 00:00:00', '2026-04-05 09:52:00', '2026-04-05 09:52:10');
INSERT INTO `system_notice` VALUES (20, '【测试】系统升级维护公告', '本周六凌晨2点至6点升级维护。', 'SYSTEM', 1, 55, 56, '2026-04-05 09:52:10', NULL, '2026-04-05 09:52:10', '2026-04-01 00:00:00', '2027-04-01 00:00:00', '2026-04-05 09:52:00', '2026-04-05 09:52:10');

-- Records of vaccination_record
INSERT INTO `vaccination_record` VALUES (3, 8, 'INJ202604050001', '2026-04-05 19:17:34', 50, 'LEFT_UPPER_ARM', 87, 'BATCH-20250101', '2026-04-05 16:17:34', '2026-04-05 16:17:34');
INSERT INTO `vaccination_record` VALUES (4, 9, 'INJ202604040002', '2026-04-04 22:17:34', 50, 'RIGHT_UPPER_ARM', 88, 'BATCH-20250201', '2026-04-04 22:17:34', '2026-04-04 22:17:34');
INSERT INTO `vaccination_record` VALUES (5, 10, 'INJ202604020001', '2026-04-02 16:17:34', 50, 'LEFT_UPPER_ARM', 89, 'BATCH-20250301', '2026-04-02 16:17:34', '2026-04-02 16:17:34');
INSERT INTO `vaccination_record` VALUES (6, 14, 'INJ202603290001', '2026-03-29 16:17:34', 50, 'LEFT_UPPER_ARM', 87, 'BATCH-20250101', '2026-03-29 16:17:34', '2026-03-29 16:17:34');
INSERT INTO `vaccination_record` VALUES (7, 15, 'INJ202603260001', '2026-03-26 16:17:34', 50, 'LEFT_UPPER_ARM', 90, 'BATCH-20250401', '2026-03-26 16:17:34', '2026-03-26 16:17:34');
INSERT INTO `vaccination_record` VALUES (8, 16, 'INJ202603210001', '2026-03-21 16:17:34', 50, 'RIGHT_UPPER_ARM', 88, 'BATCH-20250201', '2026-03-21 16:17:34', '2026-03-21 16:17:34');

-- Records of vaccine
INSERT INTO `vaccine` VALUES (1, '乙型肝炎疫苗', 'CLASS_I', '深圳康泰生物', '预防乙型肝炎病毒感染，国家免疫规划疫苗', 1, '2026-04-04 21:06:32', '2026-04-04 21:06:32');
INSERT INTO `vaccine` VALUES (2, '卡介苗', 'CLASS_I', '成都生物制品', '预防结核病，国家免疫规划疫苗', 1, '2026-04-04 21:06:32', '2026-04-04 21:06:32');
INSERT INTO `vaccine` VALUES (3, '脊髓灰质炎疫苗', 'CLASS_I', '北京科兴生物', '预防脊髓灰质炎，国家免疫规划疫苗', 1, '2026-04-04 21:06:32', '2026-04-04 21:06:32');
INSERT INTO `vaccine` VALUES (4, '百白破联合疫苗', 'CLASS_I', '武汉生物制品', '预防百日咳、白喉、破伤风，国家免疫规划疫苗', 1, '2026-04-04 21:06:32', '2026-04-04 21:06:32');
INSERT INTO `vaccine` VALUES (5, '麻疹风疹联合疫苗', 'CLASS_I', '上海生物制品', '预防麻疹和风疹，国家免疫规划疫苗', 1, '2026-04-04 21:06:32', '2026-04-04 21:06:32');
INSERT INTO `vaccine` VALUES (6, '流行性感冒疫苗', 'CLASS_II', '华兰生物疫苗', '预防流行性感冒，自费自愿接种', 1, '2026-04-04 21:06:32', '2026-04-04 21:06:32');
INSERT INTO `vaccine` VALUES (7, '水痘减毒活疫苗', 'CLASS_II', '长春百克生物', '预防水痘-带状疱疹病毒感染，自费自愿接种', 1, '2026-04-04 21:06:32', '2026-04-04 21:06:32');
INSERT INTO `vaccine` VALUES (8, '轮状病毒疫苗', 'CLASS_II', '兰州生物制品', '预防轮状病毒引起的腹泻，自费自愿接种', 1, '2026-04-04 21:06:32', '2026-04-04 21:06:32');
INSERT INTO `vaccine` VALUES (9, '乙肝疫苗', 'CLASS_I', '深圳康泰生物', '乙型肝炎疫苗，新生儿出生后24小时内接种', 1, '2026-04-04 22:40:25', '2026-04-04 22:40:25');
INSERT INTO `vaccine` VALUES (10, '卡介苗', 'CLASS_I', '成都生物制品研究所', '预防结核病，出生后即可接种', 1, '2026-04-04 22:40:25', '2026-04-04 22:40:25');
INSERT INTO `vaccine` VALUES (11, '脊灰灭活疫苗', 'CLASS_I', '北京科兴生物', '预防脊髓灰质炎', 1, '2026-04-04 22:40:25', '2026-04-04 22:40:25');
INSERT INTO `vaccine` VALUES (12, '百白破疫苗', 'CLASS_I', '武汉生物制品研究所', '预防百日咳、白喉、破伤风', 1, '2026-04-04 22:40:25', '2026-04-04 22:40:25');
INSERT INTO `vaccine` VALUES (13, '麻腮风疫苗', 'CLASS_I', '上海生物制品研究所', '预防麻疹、流行性腮腺炎、风疹', 1, '2026-04-04 22:40:25', '2026-04-04 22:40:25');
INSERT INTO `vaccine` VALUES (14, 'A群流脑疫苗', 'CLASS_I', '武汉生物制品研究所', '预防A群脑膜炎球菌引起的流行性脑脊髓膜炎', 1, '2026-04-04 22:40:25', '2026-04-04 22:40:25');
INSERT INTO `vaccine` VALUES (15, '乙脑减毒疫苗', 'CLASS_I', '成都生物制品研究所', '预防流行性乙型脑炎', 1, '2026-04-04 22:40:25', '2026-04-04 22:40:25');
INSERT INTO `vaccine` VALUES (16, '甲肝减毒疫苗', 'CLASS_I', '长春生物制品研究所', '预防甲型肝炎', 1, '2026-04-04 22:40:25', '2026-04-04 22:40:25');
INSERT INTO `vaccine` VALUES (17, '水痘疫苗', 'CLASS_II', '长春百克生物', '预防水痘-带状疱疹病毒感染', 1, '2026-04-04 22:40:25', '2026-04-04 22:40:25');
INSERT INTO `vaccine` VALUES (18, 'Hib疫苗', 'CLASS_II', '兰州生物制品研究所', '预防b型流感嗜血杆菌引起的侵袭性疾病', 1, '2026-04-04 22:40:25', '2026-04-04 22:40:25');
INSERT INTO `vaccine` VALUES (19, '手足口病疫苗', 'CLASS_II', '中国医学科学院医学生物学研究所', '预防肠道病毒71型感染相关手足口病', 1, '2026-04-04 22:40:25', '2026-04-04 22:40:25');
INSERT INTO `vaccine` VALUES (20, '轮状病毒疫苗', 'CLASS_II', '兰州生物制品研究所', '预防婴幼儿轮状病毒性胃肠炎', 1, '2026-04-04 22:40:25', '2026-04-04 22:40:25');

-- Records of vaccine_batch
INSERT INTO `vaccine_batch` VALUES (38, 'BATCH-2026-FLU-001', 6, '华兰生物疫苗', '2026-01-10', '2027-01-10', 90, 0, '2026-04-05 08:40:31', '2026-04-05 08:40:31');
INSERT INTO `vaccine_batch` VALUES (42, 'BATCH-2024-RAB-001', 9, '宁波荣安生物', '2023-12-01', '2025-12-01', 30, 2, '2026-04-05 08:40:31', '2026-04-05 08:40:31');
INSERT INTO `vaccine_batch` VALUES (87, 'BATCH-20250101', 1, '深圳康泰生物', '2025-01-01', '2026-07-01', 30, 0, '2026-04-05 16:17:34', '2026-04-05 16:17:34');
INSERT INTO `vaccine_batch` VALUES (88, 'BATCH-20250201', 2, '成都生物制品', '2025-02-01', '2026-08-01', 30, 0, '2026-04-05 16:17:34', '2026-04-05 16:17:34');
INSERT INTO `vaccine_batch` VALUES (89, 'BATCH-20250301', 3, '北京科兴生物', '2025-03-01', '2026-09-01', 30, 0, '2026-04-05 16:17:34', '2026-04-05 16:17:34');
INSERT INTO `vaccine_batch` VALUES (90, 'BATCH-20250401', 4, '武汉生物制品', '2025-04-01', '2026-10-01', 30, 0, '2026-04-05 16:17:34', '2026-04-05 16:17:34');
INSERT INTO `vaccine_batch` VALUES (91, 'BATCH-20250501', 5, '上海生物制品', '2025-05-01', '2026-11-01', 30, 0, '2026-04-05 16:17:34', '2026-04-05 16:17:34');
INSERT INTO `vaccine_batch` VALUES (92, 'BATCH-20250601', 6, '华兰生物疫苗', '2025-06-01', '2026-12-01', 30, 0, '2026-04-05 16:17:34', '2026-04-05 16:17:34');
INSERT INTO `vaccine_batch` VALUES (93, 'BATCH-20250701', 7, '长春百克生物', '2025-07-01', '2027-01-01', 30, 0, '2026-04-05 16:17:34', '2026-04-05 16:17:34');
INSERT INTO `vaccine_batch` VALUES (94, 'BATCH-20250801', 8, '兰州生物制品', '2025-08-01', '2027-02-01', 30, 0, '2026-04-05 16:17:34', '2026-04-05 16:17:34');

-- Records of verify_code

-- Records of window_service_config

SET FOREIGN_KEY_CHECKS = 1;
