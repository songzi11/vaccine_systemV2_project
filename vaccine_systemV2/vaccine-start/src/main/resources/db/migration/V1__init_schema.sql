SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- Table structure for adverse_reaction
DROP TABLE IF EXISTS `adverse_reaction`;
CREATE TABLE `adverse_reaction`  (
CREATE TABLE `adverse_reaction`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '不良反应ID',
  `observe_record_id` bigint NOT NULL COMMENT '留观记录ID（→ observe_record.id）',
  `appointment_id` bigint NOT NULL COMMENT '预约ID（→ appointment.id）',
  `reaction_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '反应类型（发热/皮疹/红肿等）',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '详细描述',
  `severity` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '严重程度（MILD/MODERATE/SEVERE）',
  `report_time` datetime NOT NULL COMMENT '上报时间',
  `handle_time` datetime NULL DEFAULT NULL COMMENT '处理时间',
  `handle_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '处理结果',
  `handler_id` bigint NULL DEFAULT NULL COMMENT '处理医生ID（→ sys_user.id）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_observe_record_id`(`observe_record_id` ASC) USING BTREE,
  INDEX `idx_appointment_id`(`appointment_id` ASC) USING BTREE,
  INDEX `idx_severity`(`severity` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '不良反应表' ROW_FORMAT = Dynamic;

-- Table structure for appointment
DROP TABLE IF EXISTS `appointment`;
CREATE TABLE `appointment`  (
CREATE TABLE `appointment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '预约ID',
  `appointment_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '预约编号（APT+YYYYMMDD+4seq）',
  `user_id` bigint NOT NULL COMMENT '家长用户ID（→ sys_user.id）',
  `child_id` bigint NOT NULL COMMENT '儿童档案ID（→ child_profile.id）',
  `vaccine_id` bigint NOT NULL COMMENT '疫苗ID（→ vaccine.id）',
  `appointment_date` date NOT NULL COMMENT '预约日期',
  `time_slot` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '预约时段',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '预约状态（1已预约/2已完成/3已取消/4已过期/6已签到/7预检通过/8已登记/9预检失败/10留观中）',
  `current_window` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '当前所在窗口编码',
  `signin_time` datetime NULL DEFAULT NULL COMMENT '签到时间',
  `cancel_time` datetime NULL DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '取消原因',
  `batch_id` bigint NULL DEFAULT NULL COMMENT '分配的批次ID（登记时写入，→ vaccine_batch.id）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_appointment_no`(`appointment_no` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_child_id`(`child_id` ASC) USING BTREE,
  INDEX `idx_vaccine_id`(`vaccine_id` ASC) USING BTREE,
  INDEX `idx_appointment_date`(`appointment_date` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_user_id_status`(`user_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_date_slot_status`(`vaccine_id` ASC, `appointment_date` ASC, `time_slot` ASC, `status` ASC) USING BTREE,
  INDEX `idx_batch_id`(`batch_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '预约表' ROW_FORMAT = Dynamic;

-- Table structure for batch_dispose_log
DROP TABLE IF EXISTS `batch_dispose_log`;
CREATE TABLE `batch_dispose_log`  (
CREATE TABLE `batch_dispose_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `dispose_no` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '销毁单号（BD+YYYYMMDD+4seq）',
  `batch_id` bigint NOT NULL COMMENT '批次ID（→ vaccine_batch.id）',
  `dispose_quantity` int NOT NULL COMMENT '销毁数量',
  `dispose_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '销毁原因',
  `operator_id` bigint NOT NULL COMMENT '操作员ID（→ sys_user.id）',
  `dispose_time` datetime NOT NULL COMMENT '销毁执行时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '销毁备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dispose_no`(`dispose_no` ASC) USING BTREE,
  INDEX `idx_batch_id`(`batch_id` ASC) USING BTREE,
  INDEX `idx_operator_id`(`operator_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '批次销毁日志表' ROW_FORMAT = Dynamic;

-- Table structure for child_profile
DROP TABLE IF EXISTS `child_profile`;
CREATE TABLE `child_profile`  (
CREATE TABLE `child_profile`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '儿童档案ID',
  `parent_id` bigint NOT NULL COMMENT '家长ID（→ sys_user.id）',
  `parent_id_card` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '家长证件号（同步自User）',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '儿童姓名',
  `gender` tinyint NOT NULL COMMENT '性别（1男/2女）',
  `birth_date` date NOT NULL COMMENT '出生日期',
  `id_card_type` tinyint NOT NULL DEFAULT 1 COMMENT '证件类型',
  `id_card_no` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '证件号码',
  `native_place` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '籍贯',
  `nation` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '民族',
  `medical_history` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '疾病史',
  `allergy_history` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '过敏史',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_parent_id_card`(`parent_id_card` ASC) USING BTREE,
  INDEX `idx_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '儿童档案表' ROW_FORMAT = Dynamic;

-- Table structure for doctor_schedule
DROP TABLE IF EXISTS `doctor_schedule`;
CREATE TABLE `doctor_schedule`  (
CREATE TABLE `doctor_schedule`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '排班ID',
  `doctor_id` bigint NOT NULL COMMENT '医生ID',
  `window_id` bigint NOT NULL COMMENT '窗口ID',
  `schedule_date` date NOT NULL COMMENT '排班日期',
  `time_slot` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '排班时段（AM/PM）',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0正常/1请假/2取消）',
  `max_capacity` int NOT NULL DEFAULT 50 COMMENT '最大接待量',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_doctor_window_date_slot`(`doctor_id` ASC, `window_id` ASC, `schedule_date` ASC, `time_slot` ASC) USING BTREE,
  INDEX `idx_doctor_id`(`doctor_id` ASC) USING BTREE,
  INDEX `idx_schedule_date`(`schedule_date` ASC) USING BTREE,
  INDEX `idx_window_id`(`window_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '医生排班表' ROW_FORMAT = Dynamic;

-- Table structure for hospital_vaccine_stock
DROP TABLE IF EXISTS `hospital_vaccine_stock`;
CREATE TABLE `hospital_vaccine_stock`  (
CREATE TABLE `hospital_vaccine_stock`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '库存ID',
  `hospital_id` bigint NOT NULL COMMENT '医院ID',
  `batch_id` bigint NOT NULL COMMENT '批次ID（→ vaccine_batch.id）',
  `location_type` tinyint NOT NULL DEFAULT 0 COMMENT '位置类型：0-仓库，1-接种窗口',
  `location_id` bigint NULL DEFAULT NULL COMMENT '位置ID（仓库ID或窗口ID）',
  `total_stock` int NOT NULL DEFAULT 0 COMMENT '总库存（入库总数，接种后不变）',
  `available_stock` int NOT NULL DEFAULT 0 COMMENT '可用库存',
  `locked_stock` int NOT NULL DEFAULT 0 COMMENT '锁定库存',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_hospital_batch`(`hospital_id` ASC, `batch_id` ASC, `location_type` ASC, `location_id` ASC) USING BTREE,
  INDEX `idx_hospital_id`(`hospital_id` ASC) USING BTREE,
  INDEX `idx_batch_id`(`batch_id` ASC) USING BTREE,
  INDEX `idx_fefo_selection`(`hospital_id` ASC, `available_stock` ASC, `locked_stock` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 119 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '医院疫苗库存表' ROW_FORMAT = Dynamic;

-- Table structure for hospital_vaccine_summary
DROP TABLE IF EXISTS `hospital_vaccine_summary`;
CREATE TABLE `hospital_vaccine_summary`  (
CREATE TABLE `hospital_vaccine_summary`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '汇总ID',
  `hospital_id` bigint NOT NULL COMMENT '医院ID',
  `vaccine_id` bigint NOT NULL COMMENT '疫苗ID（→ vaccine.id）',
  `total_stock` int NOT NULL DEFAULT 0 COMMENT '总库存',
  `available_stock` int NOT NULL DEFAULT 0 COMMENT '可用库存',
  `warning_threshold` int NOT NULL DEFAULT 20 COMMENT '预警阈值（百分比）',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_hospital_vaccine`(`hospital_id` ASC, `vaccine_id` ASC) USING BTREE,
  INDEX `idx_vaccine_id`(`vaccine_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 67 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '医院疫苗汇总表' ROW_FORMAT = Dynamic;

-- Table structure for hospital_window
DROP TABLE IF EXISTS `hospital_window`;
CREATE TABLE `hospital_window`  (
CREATE TABLE `hospital_window`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '窗口ID',
  `window_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '窗口编码（SIGNIN_01, PRECHECK_01）',
  `window_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '窗口名称',
  `window_function_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '窗口职能类型（SIGNIN/PRECHECK/REGISTER/VACCINATE/OBSERVE）',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0启用/1禁用）',
  `capacity` int NOT NULL DEFAULT 1 COMMENT '窗口容量',
  `avg_handle_time` int NOT NULL DEFAULT 5 COMMENT '平均处理时长（分钟）',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序序号',
  `doctor_id` bigint NULL DEFAULT NULL COMMENT '当前分配的医生ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_window_code`(`window_code` ASC) USING BTREE,
  INDEX `idx_function_type`(`window_function_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '接种窗口表' ROW_FORMAT = Dynamic;

-- Table structure for notice_feedback
DROP TABLE IF EXISTS `notice_feedback`;
CREATE TABLE `notice_feedback`  (
CREATE TABLE `notice_feedback`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
  `notice_id` bigint NOT NULL COMMENT '公告ID',
  `user_id` bigint NOT NULL COMMENT '反馈用户ID',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '反馈内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_notice_id`(`notice_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '公告反馈表' ROW_FORMAT = Dynamic;

-- Table structure for observe_record
DROP TABLE IF EXISTS `observe_record`;
CREATE TABLE `observe_record`  (
CREATE TABLE `observe_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '留观记录ID',
  `appointment_id` bigint NOT NULL COMMENT '预约ID（→ appointment.id）',
  `injection_id` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '注射号',
  `start_time` datetime NOT NULL COMMENT '留观开始时间（=接种时间）',
  `finish_time` datetime NULL DEFAULT NULL COMMENT '留观结束时间',
  `duration` int NULL DEFAULT NULL COMMENT '留观时长（分钟）',
  `observe_result` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NORMAL' COMMENT '留观结果（NORMAL/ABNORMAL）',
  `doctor_id` bigint NOT NULL COMMENT '留观医生ID（→ sys_user.id）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_appointment_id`(`appointment_id` ASC) USING BTREE,
  INDEX `idx_injection_id`(`injection_id` ASC) USING BTREE,
  INDEX `idx_doctor_id`(`doctor_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '留观记录表' ROW_FORMAT = Dynamic;

-- Table structure for pre_check_record
DROP TABLE IF EXISTS `pre_check_record`;
CREATE TABLE `pre_check_record`  (
CREATE TABLE `pre_check_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '预检记录ID',
  `appointment_id` bigint NOT NULL COMMENT '预约ID（一对一，→ appointment.id）',
  `check_time` datetime NOT NULL COMMENT '预检时间',
  `body_temperature` decimal(4, 1) NULL DEFAULT NULL COMMENT '体温（如37.2）',
  `weight` decimal(5, 1) NULL DEFAULT NULL COMMENT '体重（kg）',
  `height` decimal(5, 1) NULL DEFAULT NULL COMMENT '身高（cm）',
  `health_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '健康状况评估',
  `allergy_history` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '过敏史',
  `medication_recent` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '近期用药情况',
  `disease_history` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '疾病史',
  `vaccination_recent` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '近期接种史',
  `check_result` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '预检结果（PASS/FAIL）',
  `fail_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '预检失败原因',
  `doctor_id` bigint NOT NULL COMMENT '预检医生ID（→ sys_user.id）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_appointment_id`(`appointment_id` ASC) USING BTREE,
  INDEX `idx_doctor_id`(`doctor_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '预检记录表' ROW_FORMAT = Dynamic;

-- Table structure for register_queue
DROP TABLE IF EXISTS `register_queue`;
CREATE TABLE `register_queue`  (
CREATE TABLE `register_queue`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '登记记录ID',
  `appointment_id` bigint NOT NULL COMMENT '预约ID（一对一，→ appointment.id）',
  `register_time` datetime NOT NULL COMMENT '登记时间',
  `doctor_id` bigint NOT NULL COMMENT '登记医生ID（→ sys_user.id）',
  `queue_no` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '排队号（A+3位日序号）',
  `batch_id` bigint NOT NULL COMMENT '分配的批次ID（→ vaccine_batch.id）',
  `batch_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '批次号（冗余）',
  `verify_status` tinyint NOT NULL DEFAULT 0 COMMENT '核实状态（0未核实/1已核实）',
  `verify_time` datetime NULL DEFAULT NULL COMMENT '核实时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_appointment_id`(`appointment_id` ASC) USING BTREE,
  INDEX `idx_batch_id`(`batch_id` ASC) USING BTREE,
  INDEX `idx_doctor_id`(`doctor_id` ASC) USING BTREE,
  INDEX `idx_register_time`(`register_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '登记排队表' ROW_FORMAT = Dynamic;

-- Table structure for sms_verification
DROP TABLE IF EXISTS `sms_verification`;
CREATE TABLE `sms_verification`  (
CREATE TABLE `sms_verification`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '验证码ID',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用途（REGISTER/RESET_PASSWORD）',
  `code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '验证码（6位数字）',
  `expire_time` datetime NOT NULL COMMENT '过期时间（5分钟）',
  `used` tinyint NOT NULL DEFAULT 0 COMMENT '是否已使用（0否/1是）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_phone_type`(`phone` ASC, `type` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '短信验证码表' ROW_FORMAT = Dynamic;

-- Table structure for stock_alert_log
DROP TABLE IF EXISTS `stock_alert_log`;
CREATE TABLE `stock_alert_log`  (
CREATE TABLE `stock_alert_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '预警ID',
  `alert_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '预警类型（LOW_STOCK/EXPIRY_SOON/EXPIRED）',
  `vaccine_id` bigint NULL DEFAULT NULL COMMENT '疫苗ID（→ vaccine.id）',
  `batch_id` bigint NULL DEFAULT NULL COMMENT '批次ID（→ vaccine_batch.id）',
  `alert_value` decimal(10, 2) NULL DEFAULT NULL COMMENT '预警值',
  `expiry_date` date NULL DEFAULT NULL COMMENT '有效期',
  `is_handled` tinyint NOT NULL DEFAULT 0 COMMENT '是否已处理（0否/1是）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_alert_type`(`alert_type` ASC) USING BTREE,
  INDEX `idx_vaccine_id`(`vaccine_id` ASC) USING BTREE,
  INDEX `idx_batch_id`(`batch_id` ASC) USING BTREE,
  INDEX `idx_is_handled`(`is_handled` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '库存预警日志表' ROW_FORMAT = Dynamic;

-- Table structure for stock_transfer_log
DROP TABLE IF EXISTS `stock_transfer_log`;
CREATE TABLE `stock_transfer_log`  (
CREATE TABLE `stock_transfer_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `transfer_no` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调拨单号（TF+YYYYMMDD+4seq）',
  `batch_id` bigint NOT NULL COMMENT '批次ID（→ vaccine_batch.id）',
  `from_type` tinyint NOT NULL COMMENT '调出类型（0总仓/1接种点）',
  `from_id` bigint NOT NULL COMMENT '调出位置ID',
  `to_type` tinyint NOT NULL COMMENT '调入类型（0总仓/1接种点）',
  `to_id` bigint NOT NULL COMMENT '调入位置ID',
  `quantity` int NOT NULL COMMENT '调拨数量',
  `operator_id` bigint NOT NULL COMMENT '操作员ID（→ sys_user.id）',
  `transfer_time` datetime NOT NULL COMMENT '调拨执行时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '调拨备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_transfer_no`(`transfer_no` ASC) USING BTREE,
  INDEX `idx_batch_id`(`batch_id` ASC) USING BTREE,
  INDEX `idx_operator_id`(`operator_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '库存调拨日志表' ROW_FORMAT = Dynamic;

-- Table structure for sys_config
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
CREATE TABLE `sys_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置键',
  `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置值',
  `config_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '配置描述',
  `value_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'STRING' COMMENT '值类型（INT/STRING/BOOLEAN）',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统配置表' ROW_FORMAT = Dynamic;

-- Table structure for sys_permission
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`  (
CREATE TABLE `sys_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `permission_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限编码',
  `permission_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限名称',
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属模块',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '权限描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_permission_code`(`permission_code` ASC) USING BTREE,
  INDEX `idx_module`(`module` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 62 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '权限表' ROW_FORMAT = Dynamic;

-- Table structure for sys_permission_audit
DROP TABLE IF EXISTS `sys_permission_audit`;
CREATE TABLE `sys_permission_audit`  (
CREATE TABLE `sys_permission_audit`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '审计日志ID',
  `user_id` bigint NOT NULL COMMENT '操作人ID',
  `target_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作对象类型（USER/ROLE/PERMISSION）',
  `target_id` bigint NOT NULL COMMENT '操作对象ID',
  `action` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型',
  `detail` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作详情',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_target`(`target_type` ASC, `target_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '权限审计日志表' ROW_FORMAT = Dynamic;

-- Table structure for sys_role
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色编码',
  `role_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `role_group` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色分组（USER/DOCTOR/ADMIN）',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色描述',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0启用/1禁用）',
  `is_system` tinyint NOT NULL DEFAULT 0 COMMENT '是否系统内置（0自定义/1内置）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_code`(`role_code` ASC) USING BTREE,
  INDEX `idx_role_group`(`role_group` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- Table structure for sys_role_permission
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`  (
CREATE TABLE `sys_role_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_permission`(`role_id` ASC, `permission_id` ASC) USING BTREE,
  INDEX `idx_permission_id`(`permission_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 83 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色权限关联表' ROW_FORMAT = Dynamic;

-- Table structure for sys_user
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（BCrypt加密）',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `gender` tinyint NOT NULL DEFAULT 0 COMMENT '性别（0未知/1男/2女）',
  `id_card_type` tinyint NOT NULL DEFAULT 1 COMMENT '证件类型（1身份证/2护照/3其他）',
  `id_card_no` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '证件号码',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0正常/1已禁用/2已注销）',
  `no_show_count` int NOT NULL DEFAULT 0 COMMENT '爽约次数',
  `freeze_start_time` datetime NULL DEFAULT NULL COMMENT '爽约冻结开始时间',
  `freeze_end_time` datetime NULL DEFAULT NULL COMMENT '爽约冻结结束时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 68 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- Table structure for sys_user_permission
DROP TABLE IF EXISTS `sys_user_permission`;
CREATE TABLE `sys_user_permission`  (
CREATE TABLE `sys_user_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_permission`(`user_id` ASC, `permission_id` ASC) USING BTREE,
  INDEX `idx_permission_id`(`permission_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户直接权限表' ROW_FORMAT = Dynamic;

-- Table structure for sys_user_role
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
CREATE TABLE `sys_user_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id` ASC, `role_id` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 80 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户角色关联表' ROW_FORMAT = Dynamic;

-- Table structure for system_notice
DROP TABLE IF EXISTS `system_notice`;
CREATE TABLE `system_notice`  (
CREATE TABLE `system_notice`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告内容',
  `notice_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SYSTEM' COMMENT '公告类型（SYSTEM=系统公告/INTERNAL=内部公告）',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0待审核/1已发布/2已下架/3已拒绝）',
  `author_id` bigint NOT NULL COMMENT '发布人ID',
  `audit_user_id` bigint NULL DEFAULT NULL COMMENT '审核人ID',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `audit_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核意见',
  `publish_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `start_time` datetime NULL DEFAULT NULL COMMENT '展示开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '展示结束时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_author_id`(`author_id` ASC) USING BTREE,
  INDEX `idx_publish_time`(`publish_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统公告表' ROW_FORMAT = Dynamic;

-- Table structure for vaccination_record
DROP TABLE IF EXISTS `vaccination_record`;
CREATE TABLE `vaccination_record`  (
CREATE TABLE `vaccination_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '接种记录ID',
  `appointment_id` bigint NOT NULL COMMENT '预约ID（一对一，→ appointment.id）',
  `injection_id` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '注射号（INJ+YYYYMMDD+4seq）',
  `injection_time` datetime NOT NULL COMMENT '接种执行时间',
  `doctor_id` bigint NOT NULL COMMENT '接种医生ID（→ sys_user.id）',
  `injection_site` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接种部位（LEFT_UPPER_ARM/RIGHT_UPPER_ARM/LEFT_BUTTOCK/RIGHT_BUTTOCK）',
  `batch_id` bigint NOT NULL COMMENT '批次ID（→ vaccine_batch.id）',
  `batch_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '批次号（冗余）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_appointment_id`(`appointment_id` ASC) USING BTREE,
  UNIQUE INDEX `uk_injection_id`(`injection_id` ASC) USING BTREE,
  INDEX `idx_doctor_id`(`doctor_id` ASC) USING BTREE,
  INDEX `idx_batch_id`(`batch_id` ASC) USING BTREE,
  INDEX `idx_injection_time`(`injection_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '接种记录表' ROW_FORMAT = Dynamic;

-- Table structure for vaccine
DROP TABLE IF EXISTS `vaccine`;
CREATE TABLE `vaccine`  (
CREATE TABLE `vaccine`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '疫苗ID',
  `vaccine_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '疫苗名称',
  `vaccine_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '疫苗类型（CLASS_I一类/CLASS_II二类）',
  `manufacturer` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '生产厂家',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '疫苗说明',
  `is_on_shelf` tinyint NOT NULL DEFAULT 1 COMMENT '是否上架（0下架/1上架）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_vaccine_type`(`vaccine_type` ASC) USING BTREE,
  INDEX `idx_is_on_shelf`(`is_on_shelf` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '疫苗目录表' ROW_FORMAT = Dynamic;

-- Table structure for vaccine_batch
DROP TABLE IF EXISTS `vaccine_batch`;
CREATE TABLE `vaccine_batch`  (
CREATE TABLE `vaccine_batch`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '批次ID',
  `batch_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '批次号',
  `vaccine_id` bigint NOT NULL COMMENT '疫苗ID（→ vaccine.id）',
  `manufacturer` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '生产厂家',
  `production_date` date NULL DEFAULT NULL COMMENT '生产日期',
  `expiry_date` date NOT NULL COMMENT '有效期',
  `warning_days` int NOT NULL DEFAULT 30 COMMENT '临期预警天数',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0正常/1临期/2过期/3已销毁）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_batch_no`(`batch_no` ASC) USING BTREE,
  INDEX `idx_vaccine_id`(`vaccine_id` ASC) USING BTREE,
  INDEX `idx_expiry_date`(`expiry_date` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 95 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '疫苗批次表' ROW_FORMAT = Dynamic;

-- Table structure for verify_code
DROP TABLE IF EXISTS `verify_code`;
CREATE TABLE `verify_code`  (
CREATE TABLE `verify_code`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '验证码ID',
  `code` varchar(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '6位验证码',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0未使用 1已使用 2已撤销',
  `created_by` bigint NOT NULL COMMENT '创建人ID',
  `used_by` bigint NULL DEFAULT NULL COMMENT '使用者ID',
  `used_at` datetime NULL DEFAULT NULL COMMENT '使用时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '医生注册验证码' ROW_FORMAT = Dynamic;

-- Table structure for window_service_config
DROP TABLE IF EXISTS `window_service_config`;
CREATE TABLE `window_service_config`  (
CREATE TABLE `window_service_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `window_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '窗口编码',
  `business_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务名称',
  `business_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '业务描述',
  `business_detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '业务详情说明',
  `estimated_time` int NULL DEFAULT NULL COMMENT '预估办理时间（分钟）',
  `tips` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '温馨提示',
  `required_items` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '需携带物品',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_window_code`(`window_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '窗口服务配置表' ROW_FORMAT = Dynamic;
SET FOREIGN_KEY_CHECKS = 1;
