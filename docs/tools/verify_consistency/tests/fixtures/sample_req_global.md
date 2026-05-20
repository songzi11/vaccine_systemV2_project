# REQ-GLOBAL 全局需求

## 2. 状态机定义（唯一来源）

### 2.1 预约状态枚举

| status | 状态名称 | 英文常量 | 分类 | 所在窗口 | 说明 |
|--------|----------|----------|------|----------|------|
| 1 | 已预约 | `APPOINTED` | 正常 | - | 预约成功 |
| 6 | 已签到 | `SIGNED_IN` | 正常 | SIGNIN | 签到 |
| 7 | 预检通过 | `PRECHECK_PASS` | 正常 | PRECHECK | 预检通过 |
| 8 | 已登记 | `REGISTERED` | 正常 | REGISTER | 登记 |
| 10 | 留观中 | `OBSERVING` | 正常 | OBSERVE | 留观 |
| 2 | 已完成 | `COMPLETED` | 正常 | - | 完成 |
| 3 | 已取消 | `CANCELLED` | 异常 | - | 取消 |
| 4 | 已过期 | `EXPIRED` | 异常 | - | 过期 |
| 9 | 预检失败 | `PRECHECK_FAIL` | 异常 | PRECHECK | 预检失败 |

### 3.1 流转矩阵

| 目标状态 | 允许的前置状态 | 触发操作 | 执行角色 | 所在模块 |
|----------|---------------|----------|----------|----------|
| 6（已签到） | 1（已预约） | 执行签到 | DOCTOR_SIGNIN | PRD-FLOW |
| 7（预检通过） | 6（已签到） | 预检评估通过 | DOCTOR_PRECHECK | PRD-FLOW |
| 9（预检失败） | 6（已签到） | 预检评估未通过 | DOCTOR_PRECHECK | PRD-FLOW |
| 8（已登记） | 7（预检通过） | 登记核实 | DOCTOR_REGISTER | PRD-REGISTER |
| 10（留观中） | 8（已登记） | 接种 | DOCTOR_VACCINATE | PRD-VACCINATE |
| 2（已完成） | 10（留观中） | 留观结束 | DOCTOR_OBSERVE | PRD-FLOW |
| 3（已取消） | 1（已预约） | 用户取消 | USER | PRD-APPOINTMENT |
| 4（已过期） | 1（已预约） | 系统扫描 | SYSTEM | PRD-APPOINTMENT |

## 4. 统一错误码体系

### 4.1 错误码分段规则

| 段范围 | 所属模块 | 说明 |
|--------|----------|------|
| 1000-1999 | 系统级 | 通用 |
| 2000-2999 | 预约模块 | PRD-APPOINTMENT |
| 3000-3999 | 流程模块 | PRD-FLOW |
| 4000-4999 | 库存模块 | PRD-STOCK |
| 5000-5999 | 登记模块 | PRD-REGISTER |
| 6000-6999 | 接种模块 | PRD-VACCINATE |
| 7000-7999 | 用户模块 | PRD-USER |
| 8000-8999 | 管理模块 | PRD-ADMIN |

### 4.2 系统级错误码（1000-1999）

| 错误码 | 常量名 | HTTP状态码 | 描述 | 触发场景 |
|--------|--------|-----------|------|----------|
| 1001 | UNAUTHORIZED | 401 | 未认证 | 未登录 |
| 1003 | BAD_REQUEST | 400 | 参数错误 | 缺失 |

### 4.3 预约模块错误码（2000-2999）

| 错误码 | 常量名 | HTTP状态码 | 描述 |
|--------|--------|-----------|------|
| 2001 | APPOINT_CHILD_NOT_FOUND | 404 | 儿童档案不存在 |
| 2005 | APPOINT_SLOT_FULL | 409 | 时段已满 |

### 4.8 用户模块错误码（7000-7999）

| 错误码 | 常量名 | HTTP状态码 | 描述 |
|--------|--------|-----------|------|
| 7001 | USER_NOT_FOUND | 404 | 用户不存在 |
| 7006 | SMS_SEND_FAIL | 500 | 短信发送失败 |

## 9. 权限模型

### 9.3 按模块的权限矩阵

| 权限编码 | 权限名称 | USER | SIGNIN | PRECHECK | REGISTER | VACCINATE | OBSERVE | STOCK | SCHEDULE | BIZ_ADMIN | SUPER_ADMIN |
|----------|----------|------|--------|----------|----------|-----------|---------|-------|----------|-----------|-------------|
| appointment.book | 预约接种 | Y | | | | | | | | | |
| appointment.view.own | 查看自己的预约 | Y | | | | | | | | | |
| appointment.cancel.own | 取消自己的预约 | Y | | | | | | | | | |
| appointment.signin | 用户签到 | | Y | | | | | | | | |
| precheck.assess | 预检评估 | | | Y | | | | | | | |
| vaccinate.execute | 执行接种 | | | | | Y | | | | | |
| stock.view | 查看库存 | | | | | | | Y | | | |
