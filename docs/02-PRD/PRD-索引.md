# PRD 索引文件

> 生成日期：2026-04-03
> 用途：AI快速定位PRD内容，颗粒度到功能/规则/异常/数据表/测试用例级别

---

## 快速参考

| 文件 | 模块 | 错误码段 | 角色数 | 功能数 | 规则数 | 异常数 | 测试用例 |
|------|------|---------|--------|--------|--------|--------|---------|
| PRD-01-用户管理.md | USER | 2001-2099 | 3 | 29 | 39 | 24 | 27 |
| PRD-02-预约管理.md | APPOINTMENT | 3001-3099 | 6 | 13 | 18 | 21 | 19 |
| PRD-03-流程管理.md | FLOW | 3101-3199 | 6 | 12 | 25 | 19 | 18 |
| PRD-04-登记管理.md | REGISTER | 4101-4199 | 2 | 17 | 32 | 25 | 22 |
| PRD-05-接种管理.md | VACCINATE | 3201-3299 | 2 | 7 | 16 | 17 | 13 |
| PRD-06-库存管理.md | STOCK | 4001-4099 | 2 | 11 | 28 | 14 | 25 |
| PRD-07-后台管理.md | ADMIN | 5001-5099 | 3 | 33 | 35 | 40 | 37 |

---

## 错误码速查表

### 用户模块 (2001-2099)
| 错误码 | 异常 | 行号 |
|--------|------|------|
| 2001 | E-USER-001 手机号已注册 | 2232 |
| 2002 | E-USER-002 手机号格式不正确 | 2253 |
| 2003 | E-USER-003 验证码不正确 | 2293 |
| 2004 | E-USER-004 密码格式不正确 | 2312 |
| 2005 | E-USER-005 两次密码不一致 | 2328 |
| 2006 | E-USER-006 用户不存在 | 2348 |
| 2007 | E-USER-007 密码不正确 | 2364 |
| 2008 | E-USER-008 用户已被冻结 | 2380 |
| 2009 | E-USER-009 登录次数过多 | 2396 |
| 2010 | E-USER-010 用户名已存在 | 2416 |
| 2011 | E-USER-011 儿童档案不存在 | 2436 |
| 2012 | E-USER-012 儿童档案数量超限 | 2456 |
| 2013 | E-USER-013 不能修改系统管理员 | 2472 |
| 2014 | E-USER-014 只能删除已注销的用户 | 2488 |
| 2015 | E-USER-015 注销时存在进行中的业务 | 2505 |
| 2016 | E-USER-016 SUPER_ADMIN保护违规 | 2521 |
| 2017 | E-USER-017 出生日期/身份证号不可修改 | 2537 |
| 2018 | E-USER-018 账户状态异常 | 2543 |
| 2019 | E-USER-019 已注销账户登录 | 2553 |
| 2051 | E-USER-101 数据库连接异常 | 2571 |
| 2052 | E-USER-102 并发冲突 | 2586 |
| 2053 | E-USER-103 参数验证失败 | 2601 |
| 2054 | E-USER-104 Session过期 | 2616 |
| 2055 | E-USER-105 依赖服务异常 | 2631 |

### 预约模块 (3001-3099)
| 错误码 | 异常 | 行号 |
|--------|------|------|
| 3001 | E-APPOINTMENT-001 用户已冻结 | 1814 |
| 3002 | E-APPOINTMENT-002 儿童档案不存在 | 1835 |
| 3003 | E-APPOINTMENT-003 疫苗未上架 | 1856 |
| 3004 | E-APPOINTMENT-004 预约日期无效 | 1877 |
| 3005 | E-APPOINTMENT-005 预约时段无效 | 1894 |
| 3006 | E-APPOINTMENT-006 时段预约已满 | 1911 |
| 3007 | E-APPOINTMENT-007 存在重复预约 | 1933 |
| 3008 | E-APPOINTMENT-008 儿童年龄不匹配 | 1959 |
| 3009 | E-APPOINTMENT-009 预约不存在 | 1980 |
| 3010 | E-APPOINTMENT-010 无权操作该预约 | 2001 |
| 3011 | E-APPOINTMENT-011 预约状态异常/重复签到 | 2018 |
| 3012 | E-APPOINTMENT-012 预约已过期 | 2038 |
| 3013 | E-APPOINTMENT-013 预约已终止 | 2055 |
| 3014 | E-APPOINTMENT-014 预约已完成 | 2075 |
| 3015 | E-APPOINTMENT-015 身份证号不匹配 | 2092 |
| 3051 | E-APPOINTMENT-106 无预约权限 | 2189 |
| 3052 | E-APPOINTMENT-101 数据库连接异常 | 2113 |
| 3053 | E-APPOINTMENT-102 并发冲突 | 2128 |
| 3054 | E-APPOINTMENT-103 参数验证失败 | 2143 |
| 3055 | E-APPOINTMENT-104 Session过期 | 2158 |
| 3056 | E-APPOINTMENT-105 依赖服务不可用 | 2173 |

### 流程模块 (3101-3199)
| 错误码 | 异常 | 行号 |
|--------|------|------|
| 3101 | E-FLOW-001 预约状态异常（签到） | 1744 |
| 3102 | E-FLOW-002 预约日期无效（签到） | 1761 |
| 3103 | E-FLOW-003 身份证号不匹配（签到） | 1778 |
| 3104 | E-FLOW-004 预约状态异常（预检） | 1797 |
| 3105 | E-FLOW-005 预检数据缺失 | 1814 |
| 3106 | E-FLOW-006 未通过原因缺失 | 1831 |
| 3107 | E-FLOW-007 禁忌筛查失败 | 1848 |
| 3108 | E-FLOW-008 预约状态异常（留观） | 1866 |
| 3109 | E-FLOW-009 留观时长不足 | 1883 |
| 3110 | E-FLOW-010 不良反应信息缺失 | 1901 |
| 3111 | E-FLOW-011 留观结果异常 | 1918 |
| 3148 | E-FLOW-014 无权限异常（留观） | 1961 |
| 3151 | E-FLOW-012 无权限异常（签到） | 1935 |
| 3152 | E-FLOW-013 无权限异常（预检） | 1948 |
| 3153 | E-FLOW-101 数据库连接异常 | 1989 |
| 3154 | E-FLOW-102 并发冲突 | 2004 |
| 3155 | E-FLOW-103 参数验证失败 | 2019 |
| 3156 | E-FLOW-104 Session过期 | 2034 |
| 3157 | E-FLOW-015 关联服务不可用 | 1974 |

### 接种模块 (3201-3299)
| 错误码 | 异常 | 行号 |
|--------|------|------|
| 3201 | E-VACCINATE-001 预约状态异常 | 1412 |
| 3202 | E-VACCINATE-002 批次库存不足 | 1429 |
| 3203 | E-VACCINATE-003 批次已过期 | 1452 |
| 3204 | E-VACCINATE-004 接种部位未选择 | 1469 |
| 3205 | E-VACCINATE-005 注射号生成失败 | 1485 |
| 3206 | E-VACCINATE-006 库存扣减失败 | 1506 |
| 3207 | E-VACCINATE-007 接种记录保存失败 | 1531 |
| 3208 | E-VACCINATE-008 预约状态更新失败 | 1548 |
| 3209 | E-VACCINATE-009 无权查看接种记录 | 1565 |
| 3210 | E-VACCINATE-010 接种记录不存在 | 1582 |
| 3251 | E-VACCINATE-105 库存服务不可用 | 1665 |
| 3252 | E-VACCINATE-106 无接种权限 | 1682 |
| 3253 | E-VACCINATE-107 注射号并发冲突 | 1698 |
| 3254 | E-VACCINATE-101 数据库连接异常 | 1605 |
| 3255 | E-VACCINATE-102 并发冲突 | 1620 |
| 3256 | E-VACCINATE-103 参数验证失败 | 1635 |
| 3257 | E-VACCINATE-104 Session过期 | 1650 |

### 库存模块 (4001-4099)
| 错误码 | 异常 | 行号 |
|--------|------|------|
| 4001 | E-STOCK-001 调拨位置相同 | 1761 |
| 4002 | E-STOCK-002 调出位置库存不足 | 1777 |
| 4003 | E-STOCK-004 批次已销毁 | 1816 |
| 4004 | E-STOCK-005 销毁数量超限 | 1832 |
| 4005 | E-STOCK-006 销毁原因未填写 | 1848 |
| 4006 | E-STOCK-007 批次不存在 | 1864 |
| 4007 | E-STOCK-103 参数验证失败 | 1927 |
| 4008 | E-STOCK-104 Session过期 | 1942 |
| 4051 | E-STOCK-107 关联服务不可用 | 1964 |
| 4091 | E-STOCK-003 调拨失败 | 1799 |
| 4092 | E-STOCK-101 数据库连接异常 | 1882 |
| 4093 | E-STOCK-102 并发冲突 | 1897 |

### 登记模块 (4101-4199)
| 错误码 | 异常 | 行号 |
|--------|------|------|
| 4101 | E-REGISTER-001 预约状态异常 | 2151 |
| 4102 | E-REGISTER-002 预检结果未通过 | 2168 |
| 4103 | E-REGISTER-003 无可用批次 | 2189 |
| 4104 | E-REGISTER-004 批次状态异常 | 2216 |
| 4105 | E-REGISTER-005 批次库存不足 | 2233 |
| 4106 | E-REGISTER-006 批次已过期 | 2250 |
| 4107 | E-REGISTER-007 库存锁定失败 | 2267 |
| 4108 | E-REGISTER-008 排队号生成失败 | 2280 |
| 4109 | E-REGISTER-009 调拨位置相同 | 2307 |
| 4110 | E-REGISTER-010 调出位置库存不足 | 2323 |
| 4111 | E-REGISTER-011 调拨失败 | 2345 |
| 4112 | E-REGISTER-012 批次已销毁 | 2362 |
| 4113 | E-REGISTER-013 销毁数量超限 | 2378 |
| 4114 | E-REGISTER-014 销毁原因未填写 | 2394 |
| 4115 | E-REGISTER-015 叫号失败 | 2410 |
| 4116 | E-REGISTER-016 确认到号失败 | 2425 |
| 4117 | E-REGISTER-017 三次过号自动取消 | 2441 |
| 4151 | E-REGISTER-105 库存服务不可用 | 2520 |
| 4152 | E-REGISTER-106 并发冲突（库存分配） | 2538 |
| 4191 | E-REGISTER-101 数据库连接异常 | 2460 |
| 4192 | E-REGISTER-102 并发冲突 | 2475 |
| 4193 | E-REGISTER-103 参数验证失败 | 2490 |
| 4194 | E-REGISTER-104 Session过期 | 2505 |

### 后台管理模块 (5001-5099)
| 错误码 | 异常 | 行号 |
|--------|------|------|
| 5001 | E-ADMIN-SCHEDULE-001 医生不存在 | 449 |
| 5002 | E-ADMIN-SCHEDULE-002 窗口不存在 | 457 |
| 5003 | E-ADMIN-SCHEDULE-003 排班日期无效 | 465 |
| 5004 | E-ADMIN-SCHEDULE-004 时间区间无效 | 473 |
| 5005 | E-ADMIN-SCHEDULE-005 窗口开放时间冲突 | 481 |
| 5006 | E-ADMIN-SCHEDULE-006 排班唯一约束冲突 | 489 |
| 5007 | E-ADMIN-SCHEDULE-007 排班时间冲突 | 497 |
| 5008 | E-ADMIN-SCHEDULE-008 排班已有预约不能修改 | 505 |
| 5009 | E-ADMIN-SCHEDULE-009 排班已有预约不能删除 | 513 |
| 5010 | E-ADMIN-SCHEDULE-010 无权限异常 | 521 |
| 5011 | E-ADMIN-WINDOW-001 窗口编码已存在 | 910 |
| 5012 | E-ADMIN-WINDOW-002 窗口职能类型无效 | 918 |
| 5013 | E-ADMIN-WINDOW-003 角色编码不存在 | 926 |
| 5014 | E-ADMIN-WINDOW-004 容量无效 | 934 |
| 5015 | E-ADMIN-WINDOW-005 时间区间无效 | 942 |
| 5016 | E-ADMIN-WINDOW-006 窗口删除限制 | 950 |
| 5017 | E-ADMIN-WINDOW-007 角色与窗口职能不匹配 | 958 |
| 5018 | E-ADMIN-WINDOW-008 无权限异常 | 966 |
| 5019 | E-ADMIN-VACCINE-001 疫苗编码已存在 | 1315 |
| 5020 | E-ADMIN-VACCINE-002 适龄范围无效 | 1323 |
| 5021 | E-ADMIN-VACCINE-003 接种剂次无效 | 1331 |
| 5022 | E-ADMIN-VACCINE-004 接种间隔无效 | 1339 |
| 5023 | E-ADMIN-VACCINE-005 疫苗删除限制 | 1347 |
| 5024 | E-ADMIN-VACCINE-006 无权限异常 | 1355 |
| 5025 | E-ADMIN-NOTICE-001 公告标题为空 | 1738 |
| 5026 | E-ADMIN-NOTICE-002 公告内容为空 | 1746 |
| 5027 | E-ADMIN-NOTICE-003 生效时间晚于失效时间 | 1754 |
| 5028 | E-ADMIN-NOTICE-004 公告状态异常 | 1762 |
| 5029 | E-ADMIN-NOTICE-005 无审批权限 | 1770 |
| 5030 | E-ADMIN-STATS-001 统计类型无效 | 2041 |
| 5031 | E-ADMIN-STATS-002 统计日期范围无效 | 2049 |
| 5032 | E-ADMIN-STATS-003 无统计权限 | 2057 |
| 5051 | E-ADMIN-101 数据库连接异常 | 2071 |
| 5052 | E-ADMIN-102 并发冲突 | 2079 |
| 5053 | E-ADMIN-103 参数验证失败 | 2087 |
| 5054 | E-ADMIN-104 Session过期 | 2095 |

---

## 数据表速查表

| 数据表 | 所属文件 | 行号 | SRD来源 |
|--------|---------|------|---------|
| sys_user | PRD-01-用户管理.md | 1946 | SRD 8.3 |
| sys_role | PRD-01-用户管理.md | 1965 | SRD 8.3 |
| sys_permission | PRD-01-用户管理.md | 1977 | SRD 8.3 |
| sys_user_role | PRD-01-用户管理.md | 1988 | SRD 8.3 |
| sys_role_permission | PRD-01-用户管理.md | 1997 | SRD 8.3 |
| sys_user_permission | PRD-01-用户管理.md | 2006 | PRD扩展 |
| child_profile | PRD-01-用户管理.md | 2015 | SRD 8.3 |
| sms_verification | PRD-01-用户管理.md | 2031 | PRD扩展 |
| sys_permission_audit | PRD-01-用户管理.md | 2042 | PRD扩展 |
| appointment | PRD-02-预约管理.md | 1495 | SRD 8.3 |
| pre_check_record | PRD-02-预约管理.md | 1523 | SRD 8.3 |
| register_queue | PRD-02-预约管理.md | 1543 | SRD 8.3 |
| vaccination_record | PRD-02-预约管理.md | 1556 | SRD 8.3 |
| observe_record | PRD-02-预约管理.md | 1575 | SRD 8.3 |
| signin_record | PRD-03-流程管理.md | 1472 | PRD扩展 |
| adverse_reaction | PRD-03-流程管理.md | 1525 | PRD扩展 |
| register_queue | PRD-04-登记管理.md | 1833 | SRD 8.3 |
| vaccine_batch | PRD-04-登记管理.md | 1852 | SRD 8.3 |
| hospital_vaccine_stock | PRD-04-登记管理.md | 1868 | SRD 7.1 |
| stock_transfer_log | PRD-04-登记管理.md | 1880 | SRD 7.5 |
| batch_dispose_log | PRD-04-登记管理.md | 1896 | PRD扩展 |
| stock_alert_log | PRD-04-登记管理.md | 1909 | SRD 7.4 |
| hospital_vaccine_stock | PRD-06-库存管理.md | 1474 | SRD 7.1 |
| hospital_vaccine_summary | PRD-06-库存管理.md | 1485 | SRD 7.1 |
| stock_transfer_log | PRD-06-库存管理.md | 1498 | SRD 7.5 |
| batch_dispose_log | PRD-06-库存管理.md | 1515 | PRD扩展 |
| stock_lock_record | PRD-06-库存管理.md | 1528 | PRD扩展 |
| supplier_sync_log | PRD-06-库存管理.md | 1539 | PRD扩展 |
| doctor_schedule | PRD-07-后台管理.md | 409 | SRD 8.3 |
| hospital_info | PRD-07-后台管理.md | 837 | SRD 8.3 |
| hospital_window | PRD-07-后台管理.md | 849 | SRD 8.3 |
| window_service_desc | PRD-07-后台管理.md | 867 | PRD扩展 |
| vaccine | PRD-07-后台管理.md | 1267 | SRD 8.3 |
| notice | PRD-07-后台管理.md | 1679 | SRD 8.3 |
| notice_feedback | PRD-07-后台管理.md | 1699 | PRD扩展 |
| sys_config | PRD-07-后台管理.md | 2282 | SRD 8.3 |

---

## API速查表

### 用户端API (/api/user/*)
| 方法 | 路径 | 功能 | 文件 | 行号 |
|------|------|------|------|------|
| POST | /api/user/register | 用户注册 | PRD-01 | 158 |
| POST | /api/user/login | 用户登录 | PRD-01 | 192 |
| POST | /api/user/logout | 用户登出 | PRD-01 | 228 |
| POST | /api/user/changePassword | 修改密码 | PRD-01 | 246 |
| POST | /api/user/resetPassword | 重置密码 | PRD-01 | 264 |
| GET | /api/user/profile | 查询用户信息 | PRD-01 | 284 |
| POST | /api/user/profile | 修改用户信息 | PRD-01 | 302 |
| DELETE | /api/user/account | 注销账户 | PRD-01 | 722 |
| GET | /api/user/children | 查询儿童档案列表 | PRD-01 | 513 |
| GET | /api/user/child/{childId} | 查询儿童档案详情 | PRD-01 | 537 |
| POST | /api/user/child | 创建儿童档案 | PRD-01 | 558 |
| PUT | /api/user/child/{childId} | 修改儿童档案 | PRD-01 | 578 |
| DELETE | /api/user/child/{childId} | 删除儿童档案 | PRD-01 | 594 |
| GET | /api/user/notice/list | 查询公告列表 | PRD-01 | 613 |
| GET | /api/user/notice/{noticeId} | 查询公告详情 | PRD-01 | 636 |
| POST | /api/user/notice/{noticeId}/feedback | 提交公告反馈 | PRD-01 | 654 |
| GET | /api/user/vaccine/list | 查询疫苗目录 | PRD-01 | 676 |
| GET | /api/user/vaccine/{vaccineId} | 查询疫苗详情 | PRD-01 | 700 |
| POST | /api/user/appointment | 创建预约 | PRD-02 | 99 |
| GET | /api/user/appointment/{id} | 查询预约详情 | PRD-02 | 124 |
| GET | /api/user/appointments | 查询预约列表 | PRD-02 | 150 |
| PUT | /api/user/appointment/{id}/cancel | 取消预约 | PRD-02 | 173 |
| GET | /api/user/appointment/{id}/guide | 获取窗口指引 | PRD-02 | 207 |
| GET | /api/user/appointment/{id}/queue | 获取排队信息 | PRD-02 | 229 |

### 管理端API
| 方法 | 路径 | 功能 | 文件 | 行号 |
|------|------|------|------|------|
| GET | /api/admin/user/list | 查询用户列表 | PRD-01 | 328 |
| GET | /api/admin/user/{userId} | 查询用户详情 | PRD-01 | 344 |
| POST | /api/admin/user | 创建用户 | PRD-01 | 364 |
| PUT | /api/admin/user/{userId} | 修改用户 | PRD-01 | 381 |
| POST | /api/admin/user/{userId}/freeze | 冻结用户 | PRD-01 | 397 |
| POST | /api/admin/user/{userId}/unfreeze | 解冻用户 | PRD-01 | 412 |
| DELETE | /api/admin/user/{userId} | 删除用户 | PRD-01 | 427 |
| GET | /api/admin/user/{userId}/roles | 查询用户角色 | PRD-01 | 477 |
| GET | /api/admin/user/{userId}/permissions | 查询用户权限 | PRD-01 | 494 |
| POST | /api/business/user/{userId}/assignRole | 分配角色 | PRD-01 | 443 |
| POST | /api/business/user/{userId}/assignPermission | 分配权限 | PRD-01 | 459 |
| GET | /api/signin/today | 查询今日预约 | PRD-02 | 241 |
| POST | /api/signin/signin | 执行签到 | PRD-02 | 249 |
| GET | /api/precheck/queue | 查看待预检队列 | PRD-02 | 257 |
| GET | /api/register/queue | 查看待登记队列 | PRD-02 | 265 |
| GET | /api/vaccinate/queue | 查看待接种队列 | PRD-02 | 273 |
| GET | /api/observe/queue | 查看留观队列 | PRD-02 | 281 |
| POST | /api/admin/appointment/expire | 预约过期处理 | PRD-02 | 308 |

### 业务操作API
| 方法 | 路径 | 功能 | 文件 | 行号 |
|------|------|------|------|------|
| POST | /api/signin/signin | 执行签到 | PRD-03 | 140 |
| GET | /api/signin/today | 查询今日预约 | PRD-03 | 115 |
| GET | /api/precheck/queue | 查看待预检队列 | PRD-03 | 237 |
| POST | /api/precheck/assess | 执行预检评估 | PRD-03 | 268 |
| POST | /api/precheck/contraindication | 执行禁忌筛查 | PRD-03 | 305 |
| GET | /api/observe/queue | 查看留观队列 | PRD-03 | 338 |
| GET | /api/observe/status/{injectionId} | 留观状态监控 | PRD-03 | 364 |
| POST | /api/observe/finish | 确认留观结束 | PRD-03 | 393 |
| POST | /api/observe/adverse | 上报不良反应 | PRD-03 | 429 |
| POST | /api/observe/adverse/handle | 处理不良反应 | PRD-03 | 463 |
| GET | /api/register/queue | 查看待登记队列 | PRD-04 | 145 |
| GET | /api/register/detail/{appointmentId} | 查询登记详情 | PRD-04 | 177 |
| POST | /api/register/verify | 核实信息 | PRD-04 | 198 |
| POST | /api/register/batch/assign | FEFO分配批次 | PRD-04 | 226 |
| POST | /api/register/stock/lock | 锁定库存 | PRD-04 | 265 |
| POST | /api/register/queue/no | 生成排队号 | PRD-04 | 299 |
| POST | /api/register/save | 保存登记记录 | PRD-04 | 337 |
| GET | /api/stock/batch/list | 查询批次列表 | PRD-04 | 373 |
| GET | /api/stock/batch/{batchId} | 查询批次详情 | PRD-04 | 381 |
| GET | /api/stock/batch/{batchId}/stock | 查询批次库存 | PRD-04 | 389 |
| GET | /api/stock/batch/available | 查询可用批次 | PRD-04 | 409 |
| POST | /api/stock/transfer/create | 创建调拨单 | PRD-04 | 430 |
| POST | /api/stock/transfer/confirm | 确认调拨 | PRD-04 | 438 |
| GET | /api/stock/transfer/list | 查询调拨记录 | PRD-04 | 446 |
| POST | /api/stock/batch/dispose | 销毁批次 | PRD-04 | 454 |
| GET | /api/stock/alert/list | 查询批次预警 | PRD-04 | 462 |
| GET | /api/vaccinate/queue | 查看待接种队列 | PRD-05 | 120 |
| GET | /api/vaccinate/detail/{appointmentId} | 查询接种详情 | PRD-05 | 151 |
| POST | /api/vaccinate/verify | 核实信息 | PRD-05 | 173 |
| POST | /api/vaccinate/execute | 执行接种 | PRD-05 | 236 |
| GET | /api/vaccinate/record/list | 查询接种记录 | PRD-05 | 300 |
| GET | /api/vaccinate/record/child/{childId} | 查询儿童接种记录 | PRD-05 | 337 |
| GET | /api/stock/summary | 查询库存汇总 | PRD-06 | 129 |
| GET | /api/stock/batch/{batchId}/stock | 查询批次库存 | PRD-06 | 160 |
| GET | /api/stock/batch/list | 查询批次列表 | PRD-06 | 189 |
| GET | /api/stock/batch/{batchId} | 查询批次详情 | PRD-06 | 219 |
| POST | /api/stock/transfer/create | 创建调拨单 | PRD-06 | 241 |
| POST | /api/stock/transfer/confirm | 确认调拨 | PRD-06 | 261 |
| GET | /api/stock/transfer/list | 查询调拨记录 | PRD-06 | 323 |
| POST | /api/stock/batch/dispose | 销毁批次 | PRD-06 | 342 |
| GET | /api/stock/alert/list | 查询批次预警 | PRD-06 | 425 |
| POST | /api/stock/alert/{alertId}/handle | 标记预警已处理 | PRD-06 | 451 |
| GET | /api/stock/statistics | 查询库存统计 | PRD-06 | 475 |
| POST | /api/schedule | 创建排班 | PRD-07 | 160 |
| PUT | /api/schedule/{id} | 修改排班 | PRD-07 | 188 |
| GET | /api/schedule | 查询排班 | PRD-07 | 213 |
| DELETE | /api/schedule/{id} | 删除排班 | PRD-07 | 226 |
| POST | /api/schedule/conflict | 排班冲突检测 | PRD-07 | 250 |
| POST | /api/business/window | 新增窗口 | PRD-07 | 559 |
| PUT | /api/business/window/{id} | 修改窗口 | PRD-07 | 596 |
| DELETE | /api/business/window/{id} | 删除窗口 | PRD-07 | 610 |
| GET | /api/business/window | 查询窗口 | PRD-07 | 633 |
| POST | /api/business/window/service | 配置窗口服务 | PRD-07 | 654 |
| POST | /api/business/vaccine | 新增疫苗 | PRD-07 | 1008 |
| PUT | /api/business/vaccine/{id} | 修改疫苗 | PRD-07 | 1043 |
| DELETE | /api/business/vaccine/{id} | 删除疫苗 | PRD-07 | 1057 |
| GET | /api/business/vaccine | 查询疫苗 | PRD-07 | 1080 |
| PUT | /api/business/vaccine/{id}/status | 疫苗上下架 | PRD-07 | 1096 |
| POST | /api/business/notice | 发布公告 | PRD-07 | 1390 |
| PUT | /api/admin/notice/{id}/approve | 审批公告 | PRD-07 | 1414 |
| GET | /api/business/notice | 查询公告 | PRD-07 | 1432 |
| DELETE | /api/admin/notice/{id} | 删除公告 | PRD-07 | 1446 |
| GET | /api/admin/notice/{id}/feedback | 查看公告反馈 | PRD-07 | 1463 |
| POST | /api/admin/stats/vaccination | 接种统计 | PRD-07 | 1810 |
| POST | /api/admin/stats/stock | 库存统计 | PRD-07 | 1838 |
| POST | /api/admin/stats/efficiency | 效率统计 | PRD-07 | 1866 |
| POST | /api/admin/stats/anomaly | 异常统计 | PRD-07 | 1888 |

---

## 各文件详细索引

### PRD-01-用户管理.md
| 行号 | 类型 | 内容 |
|------|------|------|
| 1 | 章节 | 用户模块产品需求文档 |
| 24 | 章节 | 1. 模块概述 |
| 26 | 章节 | 1.1 模块定位 |
| 33 | 章节 | 1.2 模块边界 |
| 43 | 章节 | 1.3 涉及角色 |
| 51 | 章节 | 1.4 核心业务价值 |
| 59 | 章节 | 1.5 业务场景 |
| 105 | 章节 | 2. 功能列表 |
| 111 | 功能 | F-USER-001 用户注册 |
| 112 | 功能 | F-USER-002 用户登录 |
| 113 | 功能 | F-USER-003 用户登出 |
| 114 | 功能 | F-USER-004 修改密码 |
| 115 | 功能 | F-USER-005 重置密码 |
| 116 | 功能 | F-USER-006 查询用户信息 |
| 117 | 功能 | F-USER-007 修改用户信息 |
| 118 | 功能 | F-USER-008 查询用户列表 |
| 119 | 功能 | F-USER-009 查询用户详情 |
| 120 | 功能 | F-USER-010 创建用户 |
| 121 | 功能 | F-USER-011 修改用户 |
| 122 | 功能 | F-USER-012 冻结用户 |
| 123 | 功能 | F-USER-013 解冻用户 |
| 124 | 功能 | F-USER-014 删除用户 |
| 125 | 功能 | F-USER-015 分配用户角色 |
| 126 | 功能 | F-USER-016 分配用户权限 |
| 127 | 功能 | F-USER-017 查询用户角色 |
| 128 | 功能 | F-USER-018 查询用户权限 |
| 129 | 功能 | F-USER-019 查询儿童档案列表 |
| 130 | 功能 | F-USER-020 查询儿童档案详情 |
| 131 | 功能 | F-USER-021 创建儿童档案 |
| 132 | 功能 | F-USER-022 修改儿童档案 |
| 133 | 功能 | F-USER-023 删除儿童档案 |
| 134 | 功能 | F-USER-024 查询公告列表 |
| 135 | 功能 | F-USER-025 查询公告详情 |
| 136 | 功能 | F-USER-026 提交公告反馈 |
| 137 | 功能 | F-USER-027 查询疫苗目录列表 |
| 138 | 功能 | F-USER-028 查询疫苗详情 |
| 139 | 功能 | F-USER-029 注销账户 |
| 730 | 章节 | 3. 页面设计 |
| 734 | 章节 | 3.1 用户端页面 |
| 802 | 章节 | 3.1.2 用户登录页面 |
| 849 | 章节 | 3.1.3 个人中心页面 |
| 916 | 章节 | 3.1.4 我的儿童页面 |
| 964 | 章节 | 3.1.5 添加/编辑儿童档案页面 |
| 1029 | 章节 | 3.2 管理端页面 |
| 1300 | 章节 | 4. 用户操作流程 |
| 1302 | 流程 | 4.1 用户注册流程 |
| 1383 | 流程 | 4.2 添加儿童档案流程 |
| 1455 | 流程 | 4.3 冻结用户流程 |
| 1520 | 流程 | 4.4 分配角色流程 |
| 1585 | 章节 | 5. 业务规则 |
| 1612 | 规则 | 规则1 手机号唯一性校验 |
| 1621 | 规则 | 规则2 手机号格式校验 |
| 1629 | 规则 | 规则3 验证码校验 |
| 1639 | 规则 | 规则4 密码格式校验 |
| 1647 | 规则 | 规则5 密码一致性校验 |
| 1652 | 规则 | 规则6 姓名非空校验 |
| 1657 | 规则 | 规则7 用户名自动生成规则 |
| 1661 | 规则 | 规则8 用户初始状态规则 |
| 1665 | 规则 | 规则9 验证码标记已使用 |
| 1674 | 规则 | 规则10 用户存在性校验 |
| 1683 | 规则 | 规则11 密码校验 |
| 1688 | 规则 | 规则12 用户状态校验（冻结/注销） |
| 1695 | 规则 | 规则13 登录次数限制 |
| 1700 | 规则 | 规则14 Token生成规则 |
| 1720 | 规则 | 规则15 用户名唯一性校验 |
| 1729 | 规则 | 规则16 用户状态流转规则 |
| 1739 | 规则 | 规则17 冻结用户规则 |
| 1744 | 规则 | 规则18 冻结状态更新规则 |
| 1751 | 规则 | 规则19 解冻用户规则 |
| 1758 | 规则 | 规则20 删除用户规则 |
| 1763 | 规则 | 规则21 删除级联规则 |
| 1780 | 规则 | 规则22 冻结机制说明（双重标记） |
| 1787 | 规则 | 规则23 冻结解除方式 |
| 1793 | 规则 | 规则24 SUPER_ADMIN保护规则 |
| 1802 | 规则 | 规则25 注销级联处理规则 |
| 1812 | 规则 | 规则26 儿童档案所有权校验 |
| 1817 | 规则 | 规则27 儿童姓名非空校验 |
| 1822 | 规则 | 规则28 性别必填校验 |
| 1827 | 规则 | 规则29 出生日期必填校验 |
| 1832 | 规则 | 规则30 身份证号格式校验 |
| 1837 | 规则 | 规则31 儿童档案数量限制（最多5个） |
| 1848 | 规则 | 规则32 角色分配规则 |
| 1853 | 规则 | 规则33 角色更新规则 |
| 1864 | 规则 | 规则34 权限分配规则 |
| 1875 | 规则 | 规则35 权限查询规则 |
| 1895 | 规则 | 规则36 旧密码校验 |
| 1900 | 规则 | 规则37 新密码格式校验 |
| 1908 | 规则 | 规则38 密码加密规则（BCrypt） |
| 1912 | 规则 | 规则39 重置密码验证码校验 |
| 1924 | 流程 | 用户状态流转图 |
| 1946 | 数据表 | sys_user 系统用户表 |
| 1965 | 数据表 | sys_role 系统角色表 |
| 1977 | 数据表 | sys_permission 系统权限表 |
| 1988 | 数据表 | sys_user_role 用户角色关联表 |
| 1997 | 数据表 | sys_role_permission 角色权限关联表 |
| 2006 | 数据表 | sys_user_permission 用户额外权限关联表 |
| 2015 | 数据表 | child_profile 儿童档案表 |
| 2031 | 数据表 | sms_verification 短信验证码表 |
| 2042 | 数据表 | sys_permission_audit 权限审计日志表 |
| 2060 | 数据表 | sys_user.status 用户状态枚举 |
| 2068 | 数据表 | sys_user.gender 性别枚举 |
| 2076 | 数据表 | sys_role.role_type 角色类型枚举 |
| 2155 | 权限 | 功能权限映射表 |
| 2197 | 权限 | 操作权限说明 |
| 2232 | 异常 | E-USER-001 ~ E-USER-019 业务异常 |
| 2571 | 异常 | E-USER-101 ~ E-USER-105 系统异常 |
| 2683 | API | 附录A：API接口映射 |
| 2735 | 测试用例 | TC-USER-001 ~ TC-USER-019 用户测试 |
| 2757 | 测试用例 | TC-CHILD-001 ~ TC-CHILD-006 儿童档案测试 |
| 2768 | 测试用例 | TC-ADMIN-001 ~ TC-ADMIN-010 管理测试 |

### PRD-02-预约管理.md
| 行号 | 类型 | 内容 |
|------|------|------|
| 1 | 章节 | 预约模块产品需求文档 |
| 24 | 章节 | 1. 模块概述 |
| 68 | 功能 | F-APPOINTMENT-001 创建预约 |
| 69 | 功能 | F-APPOINTMENT-002 查询预约详情 |
| 70 | 功能 | F-APPOINTMENT-003 查询预约列表 |
| 71 | 功能 | F-APPOINTMENT-004 取消预约 |
| 72 | 功能 | F-APPOINTMENT-005 获取窗口指引 |
| 73 | 功能 | F-APPOINTMENT-006 获取排队信息 |
| 74 | 功能 | F-APPOINTMENT-007 查询今日预约 |
| 75 | 功能 | F-APPOINTMENT-008 执行签到 |
| 76 | 功能 | F-APPOINTMENT-009 查看待预检队列 |
| 77 | 功能 | F-APPOINTMENT-010 查看待登记队列 |
| 78 | 功能 | F-APPOINTMENT-011 查看待接种队列 |
| 79 | 功能 | F-APPOINTMENT-012 查看留观队列 |
| 80 | 功能 | F-APPOINTMENT-013 预约过期自动处理 |
| 312 | 章节 | 3. 页面设计 |
| 884 | 流程 | 4.1 创建预约流程 |
| 945 | 流程 | 4.2 查询预约并获取指引 |
| 1001 | 流程 | 4.3 取消预约流程 |
| 1057 | 流程 | 4.4 签到医生执行签到 |
| 1130 | 流程 | 4.5 查看队列（预检/登记/接种/留观） |
| 1192 | 规则 | 规则1~8 预约创建规则 |
| 1262 | 规则 | 规则9~14 预约取消/爽约规则 |
| 1330 | 规则 | 规则15~18 签到规则 |
| 1385 | 章节 | 5.4 窗口指引规则 |
| 1444 | 流程 | 预约状态流转图 |
| 1495 | 数据表 | appointment 预约表 |
| 1593 | 数据表 | appointment.status 预约状态枚举 |
| 1729 | 权限 | 功能权限映射 |
| 1814 | 异常 | E-APPOINTMENT-001~015 业务异常 |
| 2113 | 异常 | E-APPOINTMENT-101~106 系统异常 |
| 2296 | 测试用例 | TC-APPOINTMENT-001~009 创建预约测试 |
| 2310 | 测试用例 | TC-APPOINTMENT-101~105 取消预约测试 |
| 2322 | 测试用例 | TC-APPOINTMENT-201~205 签到测试 |

### PRD-03-流程管理.md
| 行号 | 类型 | 内容 |
|------|------|------|
| 1 | 章节 | 接种流程模块产品需求文档 |
| 24 | 章节 | 1. 模块概述 |
| 81 | 功能 | F-FLOW-001~012 功能清单 |
| 94 | 章节 | 2.2 签到管理子模块功能详情 |
| 120 | 流程 | F-FLOW-002 CAS乐观锁签到 |
| 218 | 章节 | 2.3 预检管理子模块功能详情 |
| 283 | 流程 | 预检状态流转 status=6→7/9 |
| 319 | 章节 | 2.4 留观管理子模块功能详情 |
| 405 | 流程 | 留观结束状态流转 status=10→2 |
| 473 | 章节 | 3. 页面设计 |
| 973 | 流程 | 4.1 签到流程 |
| 1043 | 流程 | 4.2 预检流程 |
| 1119 | 流程 | 4.3 留观流程 |
| 1207 | 规则 | 规则1~6 签到业务规则 |
| 1249 | 规则 | 规则7~12 预检业务规则 |
| 1292 | 规则 | 规则13~16 禁忌筛查规则 |
| 1316 | 规则 | 规则16.5 预检未通过库存释放 |
| 1323 | 规则 | 规则17~25 留观业务规则 |
| 1382 | 流程 | 完整状态流转图 |
| 1429 | 规则 | 窗口指引映射表（SRD 4.6） |
| 1439 | 规则 | CAS并发控制规则 |
| 1472 | 数据表 | signin_record 签到记录表 |
| 1487 | 数据表 | pre_check_record 预检记录表 |
| 1509 | 数据表 | observe_record 留观记录表 |
| 1525 | 数据表 | adverse_reaction 不良反应记录表 |
| 1547 | 数据表 | appointment.status 预约状态 |
| 1744 | 异常 | E-FLOW-001~003 签到异常 |
| 1797 | 异常 | E-FLOW-004~007 预检异常 |
| 1866 | 异常 | E-FLOW-008~011 留观异常 |
| 1935 | 异常 | E-FLOW-012~015 权限/服务异常 |
| 1989 | 异常 | E-FLOW-101~104 系统异常 |
| 2207 | 测试用例 | TC-FLOW-001~005 签到测试 |
| 2217 | 测试用例 | TC-FLOW-101~107 预检测试 |
| 2229 | 测试用例 | TC-FLOW-201~207 留观测试 |

### PRD-04-登记管理.md
| 行号 | 类型 | 内容 |
|------|------|------|
| 1 | 章节 | 登记批次模块产品需求文档 |
| 107 | 功能 | F-REGISTER-001~017 功能清单 |
| 127 | 功能 | F-REGISTER-001 查看待登记队列 |
| 162 | 功能 | F-REGISTER-002 查询登记详情 |
| 185 | 功能 | F-REGISTER-003 核实信息 |
| 209 | 功能 | F-REGISTER-004 FEFO分配批次 |
| 253 | 功能 | F-REGISTER-005 锁定库存 |
| 286 | 功能 | F-REGISTER-006 生成排队号 |
| 322 | 功能 | F-REGISTER-007 保存登记记录 |
| 369 | 功能 | F-REGISTER-008~017 批次管理功能 |
| 472 | 章节 | 3. 页面设计 |
| 1016 | 流程 | 4.1 执行登记流程 |
| 1129 | 流程 | 4.2 批次调拨流程 |
| 1220 | 流程 | 4.3 批次销毁流程 |
| 1294 | 流程 | 4.4 查看批次预警 |
| 1358 | 规则 | 规则1~10 登记业务规则 |
| 1397 | 规则 | 规则3b 登记时库存不足处理 |
| 1436 | 规则 | 规则7 库存锁定规则 |
| 1454 | 规则 | 规则8 排队号生成规则 |
| 1469 | 规则 | 规则9 更换批次库存释放 |
| 1481 | 规则 | 规则10a 库存锁定释放机制 |
| 1508 | 规则 | 规则11~15 批次业务规则 |
| 1550 | 规则 | 规则16~20 调拨规则 |
| 1593 | 规则 | 规则21~25 销毁规则 |
| 1629 | 规则 | 规则26~28 预警规则 |
| 1664 | 规则 | 规则29~32a 叫号/超时/过号 |
| 1770 | 流程 | 登记状态流转 |
| 1833 | 数据表 | register_queue 登记排队表 |
| 1852 | 数据表 | vaccine_batch 批次表 |
| 1868 | 数据表 | hospital_vaccine_stock 医院库存表 |
| 1880 | 数据表 | stock_transfer_log 调拨日志表 |
| 1896 | 数据表 | batch_dispose_log 销毁日志表 |
| 1909 | 数据表 | stock_alert_log 预警日志表 |
| 2151 | 异常 | E-REGISTER-001~017 业务异常 |
| 2460 | 异常 | E-REGISTER-101~108 系统异常 |
| 2655 | 测试用例 | TC-REGISTER-001~009 登记测试 |
| 2669 | 测试用例 | TC-BATCH-001~004 批次测试 |
| 2678 | 测试用例 | TC-TRANSFER-001~004 调拨测试 |
| 2687 | 测试用例 | TC-DISPOSE-001~004 销毁测试 |

### PRD-05-接种管理.md
| 行号 | 类型 | 内容 |
|------|------|------|
| 1 | 章节 | 接种模块产品需求文档 |
| 90 | 功能 | F-VACCINATE-001~007 功能清单 |
| 102 | 功能 | F-VACCINATE-001 查看待接种队列 |
| 136 | 功能 | F-VACCINATE-002 查询接种详情 |
| 159 | 功能 | F-VACCINATE-003 核实信息 |
| 184 | 功能 | F-VACCINATE-004 选择接种部位 |
| 208 | 功能 | F-VACCINATE-005 执行接种 |
| 242 | 流程 | 步骤1：生成注射号 |
| 250 | 流程 | 步骤2：扣减库存 |
| 259 | 流程 | 步骤3：保存接种记录 |
| 265 | 流程 | 步骤4：更新预约状态 |
| 272 | 流程 | 事务控制与并发控制 |
| 352 | 章节 | 3. 页面设计 |
| 1009 | 规则 | 规则1~10 接种业务规则 |
| 1087 | 规则 | 规则11~13 接种部位选择规则 |
| 1104 | 流程 | 接种状态流转 |
| 1126 | 规则 | 规则14~16 异常处理规则 |
| 1153 | 数据表 | vaccination_record 接种记录表 |
| 1170 | 数据表 | record 接种记录视图 |
| 1268 | 数据表 | 跨模块引用表汇总 |
| 1335 | 权限 | 功能权限映射 |
| 1412 | 异常 | E-VACCINATE-001~010 业务异常 |
| 1605 | 异常 | E-VACCINATE-101~107 系统异常 |
| 1800 | 测试用例 | TC-VACCINATE-001~009 接种流程测试 |
| 1814 | 测试用例 | TC-VACCINATE-101~104 查询测试 |
| 1823 | 测试用例 | TC-VACCINATE-201~202 系统异常测试 |

### PRD-06-库存管理.md
| 行号 | 类型 | 内容 |
|------|------|------|
| 1 | 章节 | 库存管理模块产品需求文档 |
| 95 | 功能 | F-STOCK-001~011 功能清单 |
| 109 | 功能 | F-STOCK-001 查询库存汇总 |
| 144 | 功能 | F-STOCK-002 查询批次库存 |
| 168 | 功能 | F-STOCK-003 查询批次列表 |
| 206 | 功能 | F-STOCK-004 查询批次详情 |
| 223 | 功能 | F-STOCK-005 创建调拨单 |
| 249 | 功能 | F-STOCK-006 确认调拨 |
| 304 | 功能 | F-STOCK-007 查询调拨记录 |
| 327 | 功能 | F-STOCK-008 销毁批次 |
| 407 | 功能 | F-STOCK-009 查询批次预警 |
| 436 | 功能 | F-STOCK-010 标记预警已处理 |
| 455 | 功能 | F-STOCK-011 查询库存统计 |
| 481 | 章节 | 3. 页面设计 |
| 1134 | 规则 | 规则0 FEFO批次分配规则 |
| 1156 | 规则 | 规则1~2 库存查询规则 |
| 1186 | 规则 | 规则3~7 调拨规则 |
| 1229 | 规则 | 规则8~12 销毁规则 |
| 1269 | 规则 | 规则13~16 预警规则 |
| 1323 | 规则 | 规则17~18 批次状态流转 |
| 1348 | 规则 | 规则19 库存汇总自动更新 |
| 1379 | 规则 | 规则20~22 库存操作规则（锁定/扣减/释放） |
| 1413 | 规则 | 规则23~25 并发控制规则 |
| 1438 | 规则 | 规则26 库存预警通知 |
| 1449 | 规则 | 规则27 库存锁定超时释放 |
| 1474 | 数据表 | hospital_vaccine_stock 医院库存表 |
| 1485 | 数据表 | hospital_vaccine_summary 库存汇总表 |
| 1498 | 数据表 | stock_transfer_log 调拨日志表 |
| 1515 | 数据表 | batch_dispose_log 销毁日志表 |
| 1528 | 数据表 | stock_lock_record 库存锁定记录表 |
| 1539 | 数据表 | supplier_sync_log 供应商同步日志表 |
| 1554 | 数据表 | stock_alert_log 预警日志表 |
| 1761 | 异常 | E-STOCK-001~007 业务异常 |
| 1882 | 异常 | E-STOCK-101~107 系统异常 |
| 2126 | 测试用例 | TC-STOCK-001~004 库存查询测试 |
| 2135 | 测试用例 | TC-TRANSFER-001~005 调拨测试 |
| 2145 | 测试用例 | TC-DISPOSE-001~005 销毁测试 |
| 2155 | 测试用例 | TC-ALERT-001~004 预警测试 |
| 2164 | 测试用例 | TC-CONCURRENCY-001~003 并发测试 |
| 2172 | 测试用例 | TC-STOCK-OP-001~004 库存操作测试 |

### PRD-07-后台管理.md
| 行号 | 类型 | 内容 |
|------|------|------|
| 1 | 章节 | 后台管理模块产品需求文档 |
| 100 | 功能 | F-ADMIN-001~024 核心功能清单 |
| 124 | 功能 | F-ADMIN-101~108 角色权限功能 |
| 132 | 功能 | F-ADMIN-201~202 系统配置功能 |
| 137 | 章节 | 3. 医生排班子模块（SCHEDULE） |
| 145 | 功能 | F-ADMIN-001~005 排班功能 |
| 371 | 规则 | 规则1~9 排班业务规则 |
| 409 | 数据表 | doctor_schedule 排班表 |
| 449 | 异常 | E-ADMIN-SCHEDULE-001~010 排班异常 |
| 531 | 章节 | 4. 窗口管理子模块（WINDOW） |
| 539 | 功能 | F-ADMIN-006~010 窗口功能 |
| 800 | 规则 | 规则10~16 窗口业务规则 |
| 837 | 数据表 | hospital_info 医院信息表 |
| 849 | 数据表 | hospital_window 窗口配置表 |
| 867 | 数据表 | window_service_desc 窗口服务说明表 |
| 910 | 异常 | E-ADMIN-WINDOW-001~008 窗口异常 |
| 976 | 章节 | 5. 疫苗管理子模块（VACCINE） |
| 984 | 功能 | F-ADMIN-011~015 疫苗功能 |
| 1235 | 规则 | 规则17~23 疫苗业务规则 |
| 1267 | 数据表 | vaccine 疫苗信息表 |
| 1315 | 异常 | E-ADMIN-VACCINE-001~006 疫苗异常 |
| 1365 | 章节 | 6. 公告管理子模块（NOTICE） |
| 1373 | 功能 | F-ADMIN-016~020 公告功能 |
| 1637 | 规则 | 规则24~31 公告业务规则 |
| 1679 | 数据表 | notice 公告表 |
| 1699 | 数据表 | notice_feedback 公告意见表 |
| 1738 | 异常 | E-ADMIN-NOTICE-001~005 公告异常 |
| 1782 | 章节 | 7. 统计分析子模块（STATS） |
| 1788 | 功能 | F-ADMIN-021~024 统计功能 |
| 1968 | 规则 | 规则33~35 统计业务规则 |
| 2041 | 异常 | E-ADMIN-STATS-001~003 统计异常 |
| 2069 | 异常 | E-ADMIN-101~104 通用系统异常 |
| 2291 | 版本 | 版本历史 |

---

**文档结束**
