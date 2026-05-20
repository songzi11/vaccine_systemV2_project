# 疫苗管理系统 — 全局研发需求文档

**文档编号:** REQ-GLOBAL-001
**版本:** V1.3
**状态:** 正式发布
**日期:** 2026-04-03
**上游依赖:** SRD V2.1 / PRD-APPOINTMENT V2.2 / PRD-FLOW V2.2 / PRD-REGISTER V2.2 / PRD-VACCINATE V2.2 / PRD-STOCK V2.2 / PRD-USER V2.2 / PRD-ADMIN V2.2

---

## 目录

1. [核心设计原则](#1-核心设计原则)
2. [状态机定义（唯一来源）](#2-状态机定义唯一来源)
3. [状态流转约束](#3-状态流转约束)
4. [统一错误码体系](#4-统一错误码体系)
5. [统一返回结构](#5-统一返回结构)
6. [事务规范](#6-事务规范)
7. [并发控制策略](#7-并发控制策略)
8. [库存模型](#8-库存模型)
9. [权限模型](#9-权限模型)
10. [时间规则](#10-时间规则)
11. [数据生命周期管理](#11-数据生命周期管理)

---

## 1. 核心设计原则

| 原则 | 说明 |
|------|------|
| **预约主控模型** | 系统以 `appointment` 表为核心，所有业务环节围绕预约记录展开，预约状态驱动整个流程流转 |
| **状态机驱动** | 预约状态是流程的唯一控制变量，任何环节的操作必须校验当前状态是否允许该操作 |
| **窗口化作业** | 每个状态对应一个职能窗口，状态流转即窗口切换 |
| **FEFO库存** | 批次分配严格遵循先过期先出原则 |
| **库存双阶段** | 登记时锁定（locked_stock+1），接种时扣减（available_stock-1, locked_stock-1） |

---

## 2. 状态机定义（唯一来源）

### 2.1 预约状态枚举

> **本节是全系统唯一的状态码权威定义。任何模块不得自行定义或修改状态值。**

| status | 状态名称 | 英文常量 | 分类 | 所在窗口 | 说明 |
|--------|----------|----------|------|----------|------|
| 1 | 已预约 | `APPOINTED` | 正常 | - | 用户提交预约成功，等待到院 |
| 6 | 已签到 | `SIGNED_IN` | 正常 | SIGNIN | 签到医生确认身份后签到 |
| 7 | 预检通过 | `PRECHECK_PASS` | 正常 | PRECHECK | 预检评估通过 |
| 8 | 已登记 | `REGISTERED` | 正常 | REGISTER | 登记完成，批次已锁定 |
| 10 | 留观中 | `OBSERVING` | 正常 | OBSERVE | 接种完成，进入留观 |
| 2 | 已完成 | `COMPLETED` | 正常 | - | 留观结束，流程闭环 |
| 3 | 已取消 | `CANCELLED` | 异常 | - | 用户主动取消或系统取消 |
| 4 | 已过期 | `EXPIRED` | 异常 | - | 预约时间已过且未签到 |
| 9 | 预检失败 | `PRECHECK_FAIL` | 异常 | PRECHECK | 预检评估未通过 |

### 2.2 状态机流转图

```
                         ┌─────────────────────────────────────┐
                         │           正常主流程                 │
                         │                                     │
    ┌────────┐    ┌────────┐    ┌────────┐    ┌────────┐      │
    │  1     │───>│  6     │───>│  7     │───>│  8     │      │
    │ 已预约 │    │ 已签到 │    │预检通过│    │ 已登记 │      │
    └────┬───┘    └────────┘    └────────┘    └────┬───┘      │
         │                                         │          │
         │                                         ▼          │
         │                                   ┌────────┐      │
         │                                   │  10    │      │
         │                                   │ 留观中 │      │
         │                                   └────┬───┘      │
         │                                        │           │
         │                                        ▼           │
         │                                   ┌────────┐      │
         │                                   │   2    │      │
         │                                   │ 已完成 │      │
         │                                   └────────┘      │
         │                                                       │
         │              ┌───────────────────────────────────────┘
         │              │
    ┌────┴───┐    ┌────────┐    ┌────────┐
    │   3    │    │   4    │    │   9    │
    │ 已取消 │    │ 已过期 │    │预检失败│
    └────────┘    └────────┘    └────────┘
      异常终态      异常终态      异常终态
```

### 2.3 状态分组

| 分组 | 状态值 | 说明 |
|------|--------|------|
| 正常进行中 | 1, 6, 7, 8, 10 | 流程尚未结束，仍有后续操作 |
| 正常终态 | 2 | 流程正常完成 |
| 异常终态 | 3, 4, 9 | 流程异常终止，不可恢复 |

> **禁忌后重新预约：** 状态 9（预检失败）为终态，不可逆。用户需重新创建预约（F-APPOINTMENT-001），系统将按新预约的预约时间重新走完整流程（签到→预检→...）。无需特殊操作。

### 2.4 current_window 设置规则

> 预约表的 `current_window` 字段记录当前所在窗口，驱动窗口指引。

| 操作 | 执行后 current_window | 说明 |
|------|----------------------|------|
| 创建预约 | `NULL` | 尚未到院 |
| 签到完成 | 签到窗口编码（如 `SIGNIN01`） | |
| 预检完成 | 预检窗口编码（如 `PRECHECK01`） | |
| 登记完成 | 登记窗口编码（如 `REGISTER01`） | |
| 接种完成 | `OBSERVE` | 固定值，指引到留观室 |
| 留观结束 | `NULL` | 流程完成 |
| 取消/过期 | `NULL` | 流程终止 |

---

## 3. 状态流转约束

> **核心规则：任何状态变更操作必须先校验当前状态是否在允许的前置状态列表中，否则拒绝操作。**

### 3.1 流转矩阵

| 目标状态 | 允许的前置状态 | 触发操作 | 执行角色 | 所在模块 |
|----------|---------------|----------|----------|----------|
| 6（已签到） | 1（已预约） | 执行签到 | DOCTOR_SIGNIN | PRD-FLOW |
| 7（预检通过） | 6（已签到） | 预检评估通过 | DOCTOR_PRECHECK | PRD-FLOW |
| 9（预检失败） | 6（已签到） | 预检评估未通过 | DOCTOR_PRECHECK | PRD-FLOW |
| 8（已登记） | 7（预检通过） | 登记核实+批次锁定 | DOCTOR_REGISTER | PRD-REGISTER |
| 10（留观中） | 8（已登记） | 执行接种+库存扣减 | DOCTOR_VACCINATE | PRD-VACCINATE |
| 2（已完成） | 10（留观中） | 确认留观结束 | DOCTOR_OBSERVE | PRD-FLOW |
| 3（已取消） | 1（已预约） | 用户主动取消 | USER | PRD-APPOINTMENT |
| 4（已过期） | 1（已预约） | 系统定时扫描 | SYSTEM | PRD-APPOINTMENT |

### 3.2 终态规则

| 终态 | 可否继续操作 | 说明 |
|------|-------------|------|
| 2（已完成） | 不可 | 流程正常结束 |
| 3（已取消） | 不可 | 需重新预约 |
| 4（已过期） | 不可 | 需重新预约 |
| 9（预检失败） | 不可 | 需咨询医生后重新预约 |

### 3.3 各模块可操作状态汇总

| 模块 | 可读状态 | 可写（变更）目标状态 |
|------|----------|---------------------|
| APPOINTMENT（预约） | 1, 2, 3, 4, 6, 7, 8, 9, 10 | 创建→1，取消→3，过期→4 |
| FLOW-SIGNIN（签到） | 1, 6 | 签到→6 |
| FLOW-PRECHECK（预检） | 6, 7, 9 | 预检通过→7，预检失败→9 |
| REGISTER（登记） | 7, 8 | 登记→8 |
| VACCINATE（接种） | 8, 10 | 接种→10 |
| FLOW-OBSERVE（留观） | 10, 2 | 留观结束→2 |

### 3.4 状态校验伪代码

```java
public void transitionStatus(Long appointmentId, int targetStatus, String operatorRole) {
    // 1. 加行锁查询当前状态
    Appointment appt = appointmentMapper.selectForUpdate(appointmentId);
    int currentStatus = appt.getStatus();

    // 2. 校验流转合法性
    List<Integer> allowedFrom = STATUS_TRANSITION_MAP.get(targetStatus);
    if (!allowedFrom.contains(currentStatus)) {
        throw new BusinessException(
            ErrorCode.STATUS_TRANSITION_FORBIDDEN,
            "当前状态(" + currentStatus + ")不允许变更为(" + targetStatus + ")"
        );
    }

    // 3. 校验操作角色
    List<String> allowedRoles = STATUS_ROLE_MAP.get(targetStatus);
    if (!allowedRoles.contains(operatorRole)) {
        throw new BusinessException(ErrorCode.FORBIDDEN, "无权执行此操作");
    }

    // 4. 执行状态变更
    appointmentMapper.updateStatus(appointmentId, targetStatus);
}
```

---

## 4. 统一错误码体系

### 4.1 错误码分段规则

| 段范围 | 所属模块 | 说明 |
|--------|----------|------|
| 1000-1999 | 系统级 | 认证、权限、参数、通用错误 |
| 2000-2999 | 预约模块 | PRD-APPOINTMENT |
| 3000-3999 | 流程模块 | PRD-FLOW（签到/预检/留观） |
| 4000-4999 | 库存模块 | PRD-STOCK |
| 5000-5999 | 登记模块 | PRD-REGISTER |
| 6000-6999 | 接种模块 | PRD-VACCINATE |
| 7000-7999 | 用户模块 | PRD-USER |
| 8000-8999 | 管理模块 | PRD-ADMIN |
| 1900-1999 | 共享业务错误码 | 跨模块复用（预约/疫苗/批次/库存等通用查询） |

### 4.2 系统级错误码（1000-1999）

| 错误码 | 常量名 | HTTP状态码 | 描述 | 触发场景 |
|--------|--------|-----------|------|----------|
| 1001 | UNAUTHORIZED | 401 | 未认证或Token无效 | 未登录、Token过期 |
| 1002 | FORBIDDEN | 403 | 无权限访问 | 角色权限不足 |
| 1003 | BAD_REQUEST | 400 | 请求参数错误 | 参数缺失或格式错误 |
| 1004 | NOT_FOUND | 404 | 资源不存在 | 查询的记录不存在 |
| 1005 | CONFLICT | 409 | 数据冲突 | 重复操作、并发冲突 |
| 1006 | TOO_MANY_REQUESTS | 429 | 请求频率超限 | 短时间内大量请求 |
| 1007 | INTERNAL_ERROR | 500 | 系统内部错误 | 未预期的服务端异常 |
| 1010 | STATUS_TRANSITION_FORBIDDEN | 409 | 状态流转不允许 | 当前状态不允许该操作 |
| 1011 | USER_FROZEN | 403 | 用户已被冻结 | 冻结期内操作 |
| 1012 | NO_SHOW_FROZEN | 403 | 爽约冻结中 | 爽约3次冻结7天 |

### 4.3 预约模块错误码（2000-2999）

| 错误码 | 常量名 | HTTP状态码 | 描述 |
|--------|--------|-----------|------|
| 2001 | APPOINT_CHILD_NOT_FOUND | 404 | 儿童档案不存在 |
| 2002 | APPOINT_CHILD_NOT_OWN | 403 | 儿童档案不属于当前用户 |
| 2003 | APPOINT_VACCINE_OFF_SHELF | 400 | 疫苗未上架 |
| 2004 | APPOINT_DATE_INVALID | 400 | 预约日期无效（过去日期） |
| 2005 | APPOINT_SLOT_FULL | 409 | 预约时段已满 |
| 2006 | APPOINT_DUPLICATE | 409 | 同日同疫苗重复预约 |
| 2007 | APPOINT_CANCEL_FORBIDDEN | 409 | 当前状态不允许取消 |
| 2008 | APPOINT_NOT_FOUND | 404 | 预约记录不存在 |
| 2009 | APPOINT_EXPIRED | 409 | 预约已过期 |
| 2010 | APPOINT_HAS_ACTIVE | 409 | 存在进行中的预约 |

### 4.4 流程模块错误码（3000-3999）

| 错误码 | 常量名 | HTTP状态码 | 描述 |
|--------|--------|-----------|------|
| 3001 | SIGNIN_STATUS_INVALID | 409 | 当前状态不允许签到（非status=1） |
| 3002 | SIGNIN_IDCARD_MISMATCH | 400 | 身份证号不匹配 |
| 3003 | PRECHECK_STATUS_INVALID | 409 | 当前状态不允许预检（非status=6） |
| 3004 | PRECHECK_TEMP_HIGH | 400 | 体温异常，建议暂缓接种 |
| 3005 | OBSERVE_STATUS_INVALID | 409 | 当前状态不允许结束留观（非status=10） |
| 3006 | OBSERVE_TIME_INSUFFICIENT | 400 | 留观时间不足30分钟 |
| 3007 | SIGNIN_DATE_INVALID | 409 | 预约日期未到，无法签到 |
| 3009 | CONTRAINDICATION_FAILED | 409 | 禁忌筛查不通过 |
| 3010 | ADVERSE_NOT_REPORTED | 409 | 留观结果异常但未上报不良反应 |

### 4.5 库存模块错误码（4000-4999）

| 错误码 | 常量名 | HTTP状态码 | 描述 |
|--------|--------|-----------|------|
| 4001 | STOCK_TRANSFER_SAME_LOCATION | 400 | 调拨位置相同 |
| 4002 | STOCK_TRANSFER_INSUFFICIENT | 400 | 调出位置库存不足 |
| 4003 | STOCK_BATCH_DISPOSED | 400 | 批次已销毁 |
| 4004 | STOCK_DISPOSE_EXCEED | 400 | 销毁数量超过总库存 |
| 4005 | STOCK_DISPOSE_REASON_EMPTY | 400 | 销毁原因未填写 |
| 4006 | STOCK_BATCH_NOT_FOUND | 404 | 批次不存在 |
| 4007 | STOCK_PARAM_INVALID | 400 | 库存模块参数错误 |
| 4008 | STOCK_SESSION_EXPIRED | 401 | Session过期 |
| 4009 | STOCK_TRANSFER_FAILED | 500 | 调拨执行失败 |
| 4010 | STOCK_DB_ERROR | 500 | 数据库连接异常 |
| 4011 | STOCK_CONCURRENCY_CONFLICT | 409 | 库存并发冲突 |

### 4.6 登记模块错误码（5000-5999）

| 错误码 | 常量名 | HTTP状态码 | 描述 |
|--------|--------|-----------|------|
| 5001 | REGISTER_STATUS_INVALID | 409 | 当前状态不允许登记（非status=7） |
| 5002 | REGISTER_BATCH_NOT_AVAILABLE | 400 | 无可用批次（库存为0或全部过期） |
| 5003 | REGISTER_LOCK_FAILED | 500 | 库存锁定失败 |
| 5004 | REGISTER_QUEUE_DUPLICATE | 409 | 排队号重复 |
| 5005 | DB_CONNECTION_FAILED | 500 | 数据库连接失败 |
| 5006 | CONCURRENCY_CONFLICT | 409 | 并发冲突 |

### 4.7 接种模块错误码（6000-6999）

| 错误码 | 常量名 | HTTP状态码 | 描述 |
|--------|--------|-----------|------|
| 6001 | VACCINATE_STATUS_INVALID | 409 | 当前状态不允许接种（非status=8） |
| 6002 | VACCINATE_STOCK_INSUFFICIENT | 400 | 批次库存不足 |
| 6003 | VACCINATE_BATCH_EXPIRED | 400 | 批次已过期 |
| 6004 | VACCINATE_SITE_EMPTY | 400 | 接种部位未选择 |
| 6005 | VACCINATE_INJECTION_ID_FAIL | 500 | 注射号生成失败 |
| 6006 | VACCINATE_DEDUCT_FAILED | 500 | 库存扣减失败 |
| 6007 | VACCINATE_RECORD_SAVE_FAILED | 500 | 接种记录保存失败 |
| 6008 | VACCINATE_STATUS_UPDATE_FAILED | 500 | 预约状态更新失败 |
| 6009 | VACCINATE_RECORD_NOT_FOUND | 404 | 接种记录不存在 |
| 6010 | VACCINATE_NO_PERMISSION | 403 | 无权查看该接种记录 |

### 4.8 用户模块错误码（7000-7999）

| 错误码 | 常量名 | HTTP状态码 | 描述 |
|--------|--------|-----------|------|
| 7001 | USER_PHONE_DUPLICATE | 409 | 手机号已注册 |
| 7002 | USER_LOGIN_FAILED | 401 | 用户名或密码错误 |
| 7003 | USER_NOT_FOUND | 404 | 用户不存在 |
| 7004 | USER_ALREADY_FROZEN | 409 | 用户已处于冻结状态 |
| 7005 | CHILD_INFO_INCOMPLETE | 400 | 儿童档案信息不完整 |
| 7006 | CHILD_NOT_FOUND | 404 | 儿童档案不存在 |
| 7007 | SMS_CODE_INVALID | 400 | 验证码无效或已过期 |
| 7008 | LOGIN_LOCKED | 403 | 登录失败次数过多，账号已锁定 |
| 7009 | USER_FROZEN_LOGIN | 403 | 用户已冻结，无法登录 |
| 7010 | OLD_PASSWORD_ERROR | 400 | 旧密码错误 |
| 7011 | CHILD_COUNT_EXCEEDED | 400 | 儿童档案数量已达上限（5个） |
| 7012 | CHILD_HAS_APPOINTMENT | 409 | 该儿童存在未完成的预约，无法删除 |
| 7013 | ONLY_DEACTIVATE_SUSPENDED | 400 | 只能注销已停用的用户 |
| 7014 | USER_HAS_ACTIVE_APPOINTMENT | 409 | 用户存在进行中的预约，无法执行此操作 |

### 4.9 管理模块错误码（8000-8999）

| 错误码 | 常量名 | HTTP状态码 | 描述 |
|--------|--------|-----------|------|
| 8001 | SCHEDULE_CONFLICT | 409 | 排班时间冲突 |
| 8002 | SCHEDULE_NOT_FOUND | 404 | 排班记录不存在 |
| 8003 | WINDOW_CODE_DUPLICATE | 409 | 窗口编码已存在 |
| 8004 | WINDOW_IN_USE | 409 | 窗口存在关联数据，无法删除 |
| 8005 | NOTICE_NOT_FOUND | 404 | 公告不存在 |
| 8006 | VACCINE_NOT_FOUND | 404 | 疫苗不存在 |
| 8007 | ROLE_ASSIGN_FAILED | 500 | 角色分配失败 |
| 8008 | ROLE_CODE_DUPLICATE | 409 | 角色编码已存在 |
| 8009 | ROLE_NOT_FOUND | 404 | 角色不存在 |
| 8010 | ROLE_SYSTEM_PROTECTED | 409 | 系统内置角色不可删除 |
| 8011 | ROLE_IN_USE | 409 | 角色已绑定用户，无法删除 |
| 8012 | PERMISSION_NOT_FOUND | 404 | 权限不存在 |
| 8013 | CONFIG_KEY_NOT_FOUND | 404 | 配置项不存在 |
| 8014 | CONFIG_VALUE_INVALID | 400 | 配置值格式不正确 |
| 8015 | CANNOT_MODIFY_SYSADMIN | 403 | 不能修改系统管理员 |

### 4.10 共享业务错误码（1900-1999）

> 本段存放跨模块高频复用的业务错误码。当多个模块需要查询同一实体时，使用本段共享码而非各自模块段。

| 错误码 | 常量名 | HTTP状态码 | 描述 | 典型使用模块 |
|--------|--------|-----------|------|------------|
| 1901 | APPOINT_NOT_FOUND | 404 | 预约记录不存在（跨模块查询） | USER/REGISTER/FLOW/VACCINATE |
| 1902 | VACCINE_NOT_FOUND | 404 | 疫苗信息不存在 | FLOW/REGISTER |
| 1903 | BATCH_NOT_FOUND | 404 | 疫苗批次不存在 | REGISTER |
| 1904 | STOCK_INSUFFICIENT | 409 | 库存不足 | REGISTER/APPOINTMENT/STOCK |
| 1905 | RECORD_NOT_FOUND | 404 | 通用记录不存在 | FLOW/VACCINATE |

### 4.11 分层错误处理策略

本系统采用两级错误处理机制，系统码与模块专用码存在有意设计的语义重叠：

**第一级：系统拦截器（1000-1999）**

系统级错误码由框架拦截器和中间件统一抛出，适用于所有模块：

| 系统码 | 常量名 | 触发层 | 适用范围 |
|--------|--------|--------|---------|
| 1001 | UNAUTHORIZED | 认证拦截器 | 全局 |
| 1003 | BAD_REQUEST | 参数校验拦截器 | 全局 |
| 1005 | CONFLICT | 并发控制拦截器 | 全局 |

**第二级：模块业务层（2000-8999）**

模块专用码在业务逻辑层抛出，提供更精确的上下文信息：

| 模块码 | 常量名 | 对应系统码 | 模块 | 补充语义 |
|--------|--------|-----------|------|---------|
| 4007 | STOCK_PARAM_INVALID | 1003 | STOCK | 标注具体的 {field_name} 参数 |
| 4008 | STOCK_SESSION_EXPIRED | 1001 | STOCK | 标注库存操作特有的会话失效场景 |
| 5006 | REGISTER_CONCURRENCY_CONFLICT | 1005 | REGISTER | 标注登记操作特有的并发冲突 |

**处理规则：**

1. 模块业务逻辑优先抛出模块专用码（提供精确上下文）
2. 未被模块业务逻辑捕获的异常由系统拦截器兜底，抛出对应系统码
3. 前端根据模块专用码展示精确提示；若收到系统码则展示通用提示
4. 模块专用码与系统码的语义重叠是有意设计，不构成冲突

---

## 5. 统一返回结构

### 5.1 成功响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { },
  "timestamp": 1712035200000
}
```

### 5.2 业务异常响应

```json
{
  "code": 2001,
  "message": "儿童档案不存在",
  "data": null,
  "timestamp": 1712035200000
}
```

### 5.3 系统异常响应

```json
{
  "code": 1007,
  "message": "系统繁忙，请稍后重试",
  "data": null,
  "timestamp": 1712035200000
}
```

### 5.4 分页响应

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "records": [],
    "total": 100,
    "page": 1,
    "size": 10,
    "pages": 10
  },
  "timestamp": 1712035200000
}
```

### 5.5 字段规范

| 字段 | 类型 | 必返回 | 说明 |
|------|------|--------|------|
| `code` | int | 是 | 业务状态码。200=成功，其他=失败。**不使用HTTP状态码作为业务码** |
| `message` | string | 是 | 人类可读的提示信息 |
| `data` | object/null | 是 | 业务数据。失败时为 `null` |
| `timestamp` | long | 是 | 服务器响应时间戳（毫秒） |

### 5.6 HTTP状态码与业务码的关系

| 场景 | HTTP状态码 | 业务code | 说明 |
|------|-----------|----------|------|
| 正常业务成功 | 200 | 200 | 业务正常返回 |
| 业务规则校验失败 | 200 | 非200 | 前端根据 `code` 字段处理 |
| 未认证 | 401 | 1001 | Token无效或缺失 |
| 认证过期 | 401 | 1001 | 需重新登录 |
| 参数格式错误（框架层） | 400 | 1003 | `@Valid` 校验不通过 |
| 服务端未预期异常 | 500 | 1007 | 全局异常兜底 |

---

## 6. 事务规范

### 6.1 必须使用事务的操作

| 序号 | 操作 | 涉及表 | 事务范围 | 说明 |
|------|------|--------|----------|------|
| T1 | 预约创建 | appointment | 单表 | 含时段容量校验 |
| T2 | 取消预约 | appointment + hospital_vaccine_stock | 跨表 | 需释放锁定库存 |
| T3 | 签到 | appointment + pre_check_record（无，签到不写预检表） | 单表 | 状态+窗口+时间 |
| T4 | 预检 | appointment + pre_check_record | 跨表 | 状态+预检记录 |
| T5 | 登记+批次锁定 | appointment + register_queue + hospital_vaccine_stock | 跨表 | **最高并发热点** |
| T6 | 接种执行 | appointment + vaccination_record + hospital_vaccine_stock | 跨表 | **库存扣减核心事务** |
| T7 | 留观结束 | appointment + observe_record | 跨表 | 状态+留观记录 |
| T8 | 库存调拨 | hospital_vaccine_stock（2行） + stock_transfer_log | 跨表 | 同一事务内扣增 |
| T9 | 批次销毁 | vaccine_batch + hospital_vaccine_stock + batch_dispose_log | 跨表 | 状态+库存+日志 |
| T10 | 角色权限分配 | sys_user_role + sys_role_permission | 跨表 | 级联更新 |

### 6.2 事务配置

| 配置项 | 值 | 说明 |
|--------|-----|------|
| 隔离级别 | `READ_COMMITTED` | MySQL默认，兼顾性能与一致性 |
| 超时时间 | 30秒 | 单事务最长执行时间 |
| 回滚策略 | 遇 `RuntimeException` / `BusinessException` 回滚 | 编译期异常不回滚 |

### 6.3 事务伪代码示例（接种执行）

```java
@Transactional(rollbackFor = Exception.class, timeout = 30)
public VaccinateResult executeVaccinate(Long appointmentId, String injectionSite, Long doctorId) {
    // 1. SELECT FOR UPDATE 锁定预约行
    Appointment appt = appointmentMapper.selectForUpdate(appointmentId);
    assertStatus(appt.getStatus(), 8);  // 必须是已登记

    // 2. SELECT FOR UPDATE 锁定库存行
    VaccineStock stock = stockMapper.selectForUpdate(appt.getBatchId());
    if (stock.getAvailableStock() < 1 || stock.getLockedStock() < 1) {
        throw new BusinessException(6002, "批次库存不足");
    }

    // 3. 生成注射号
    String injectionId = generateInjectionId();

    // 4. 保存接种记录
    vaccinationRecordMapper.insert(...);

    // 5. 扣减库存（available_stock -1, locked_stock -1）
    stockMapper.deductStock(appt.getBatchId());

    // 6. 更新预约状态为10
    appointmentMapper.updateStatus(appointmentId, 10, "OBSERVE");

    return result;
}
```

---

## 7. 并发控制策略

### 7.1 并发风险场景

| 场景 | 风险 | 后果 |
|------|------|------|
| 多个登记医生同时FEFO分配同一批次 | 批次库存超卖 | locked_stock > actual_stock |
| 登记和接种同时操作同一批次 | 库存数据不一致 | available_stock 或 locked_stock 为负 |
| 多个用户同时预约同一时段 | 时段容量超限 | 实际到院人数超过窗口容量 |
| 爽约次数并发累加 | 计数不准确 | 冻结判定延迟 |

### 7.2 核心策略：SELECT FOR UPDATE

> **所有涉及库存变更和状态变更的操作，必须在事务内使用 `SELECT ... FOR UPDATE` 加行锁。**

#### 7.2.1 登记阶段（FEFO分配+库存锁定）

```sql
-- 事务开始
BEGIN;

-- 1. 锁定预约行，校验状态
SELECT id, status, batch_id FROM appointment
WHERE id = #{appointmentId}
FOR UPDATE;

-- 2. FEFO查询可用批次（加锁，防止并发分配到同一批次）
SELECT b.id, b.batch_no, b.expiry_date, s.available_stock
FROM vaccine_batch b
JOIN hospital_vaccine_stock s ON b.id = s.batch_id
WHERE b.vaccine_id = #{vaccineId}
  AND s.hospital_id = #{hospitalId}
  AND b.status IN (0, 1)       -- 正常或临期状态均可分配
  AND s.available_stock > 0
  AND b.expiry_date > NOW()
ORDER BY b.expiry_date ASC
LIMIT 1
FOR UPDATE;

-- 3. 锁定库存（locked_stock +1）
UPDATE hospital_vaccine_stock
SET locked_stock = locked_stock + 1
WHERE batch_id = #{batchId}
  AND available_stock > 0;

-- 4. 更新预约状态+记录登记信息
UPDATE appointment SET status = 8, batch_id = #{batchId}, current_window = #{windowCode}
WHERE id = #{appointmentId};

INSERT INTO register_queue (appointment_id, batch_id, queue_no, register_time)
VALUES (#{appointmentId}, #{batchId}, #{queueNo}, NOW());

-- 事务提交
COMMIT;
```

#### 7.2.2 接种阶段（库存扣减）

```sql
-- 事务开始
BEGIN;

-- 1. 锁定预约行
SELECT id, status, batch_id FROM appointment
WHERE id = #{appointmentId}
FOR UPDATE;

-- 2. 锁定库存行
SELECT available_stock, locked_stock
FROM hospital_vaccine_stock
WHERE batch_id = #{batchId}
FOR UPDATE;

-- 3. 扣减库存（available_stock -1, locked_stock -1）
UPDATE hospital_vaccine_stock
SET available_stock = available_stock - 1,
    locked_stock = locked_stock - 1
WHERE batch_id = #{batchId}
  AND available_stock >= 1
  AND locked_stock >= 1;

-- 4. 保存接种记录
INSERT INTO vaccination_record (...) VALUES (...);

-- 5. 更新预约状态
UPDATE appointment SET status = 10, current_window = 'OBSERVE'
WHERE id = #{appointmentId};

-- 事务提交
COMMIT;
```

#### 7.2.3 预约创建（时段容量控制）

```sql
-- 事务开始
BEGIN;

-- 1. 检查时段剩余容量（加锁）
SELECT COUNT(*) FROM appointment
WHERE vaccine_id = #{vaccineId}
  AND appointment_date = #{date}
  AND time_slot = #{timeSlot}
  AND status IN (1, 6, 7, 8, 10)
FOR UPDATE;

-- 2. 容量不足则拒绝
-- IF count >= max_capacity THEN ROLLBACK;

-- 3. 创建预约
INSERT INTO appointment (...) VALUES (...);

-- 事务提交
COMMIT;
```

### 7.3 乐观锁补充

库存汇总表 `hospital_vaccine_summary` 使用乐观锁，避免汇总更新的长事务：

```sql
UPDATE hospital_vaccine_summary
SET total_stock = #{totalStock},
    available_stock = #{availableStock},
    version = version + 1
WHERE id = #{id}
  AND version = #{currentVersion};
```

### 7.4 重试策略

| 操作 | 最大重试次数 | 重试间隔 | 退避策略 |
|------|-------------|----------|----------|
| FEFO批次分配 | 3 | 100ms, 200ms, 400ms | 指数退避 |
| 库存扣减 | 3 | 100ms, 200ms, 400ms | 指数退避 |
| 注射号生成 | 3 | 即时 | 立即重试 |
| 预约创建 | 1 | 即时 | 不重试，直接提示用户 |
| T8 调拨事务执行 | 2 | 200ms, 500ms | 固定间隔重试，失败后引导用户手动重试 |
| T9 批次销毁事务 | 2 | 200ms, 500ms | 固定间隔重试，失败后进入异常状态等待补偿任务处理 |

---

## 8. 库存模型

### 8.1 库存双字段模型

> `hospital_vaccine_stock` 表使用 `available_stock` 和 `locked_stock` 两个字段分离管理库存。

```
                    总库存概念
    ┌─────────────────────────────────┐
    │       total_stock               │
    │  ┌─────────────┐ ┌───────────┐ │
    │  │ available   │ │  locked   │ │
    │  │   _stock    │ │  _stock   │ │
    │  │  (可预约)   │ │ (已登记)  │ │
    │  └─────────────┘ └───────────┘ │
    └─────────────────────────────────┘
```

### 8.2 库存变更操作矩阵

| 操作 | available_stock | locked_stock | 触发时机 | 执行角色 |
|------|----------------|-------------|----------|----------|
| **登记锁定** | 不变 | **+1** | 登记完成 | DOCTOR_REGISTER |
| **接种扣减** | **-1** | **-1** | 接种完成 | DOCTOR_VACCINATE |
| **取消释放** | 不变 | **-1** | 取消预约 | USER / SYSTEM |
| **调拨调出** | **-N** | 不变 | 调拨确认 | DOCTOR_STOCK |
| **调拨调入** | **+N** | 不变 | 调拨确认 | DOCTOR_STOCK |
| **批次销毁** | **-N** | **归零** | 销毁确认 | DOCTOR_STOCK |

### 8.3 库存不变量

> **以下约束在任何时刻都必须成立：**

```
1. available_stock >= 0
2. locked_stock >= 0
3. total_stock = available_stock + locked_stock（逻辑不变量）
4. available_stock >= locked_stock（登记时只加locked，接种时同时减两者）
5. 预约时段有效预约数 <= 时段容量上限
```

### 8.4 FEFO批次分配规则

**原则：** 优先使用有效期最早的批次（First Expired First Out）。

```sql
SELECT b.id AS batch_id, b.batch_no, b.expiry_date, s.available_stock
FROM vaccine_batch b
JOIN hospital_vaccine_stock s ON b.id = s.batch_id
WHERE b.vaccine_id = #{vaccineId}
  AND s.hospital_id = #{hospitalId}
  AND b.status IN (0, 1)     -- 正常或临期状态均可分配
  AND s.available_stock > 0  -- 有可用库存
  AND b.expiry_date > NOW()  -- 未过期
ORDER BY b.expiry_date ASC   -- 按有效期升序（先过期先出）
LIMIT 1
FOR UPDATE;                   -- 行锁防并发
```

### 8.5 库存预警规则

| 预警类型 | 触发条件 | 动作 |
|----------|----------|------|
| LOW_STOCK | `available_stock / (available_stock + locked_stock) < 20%` | 自动同步供应商补货 |
| EXPIRY_SOON | `expiry_date - warning_days < NOW()` 且 `status = 0` | 标记 `status = 1`（临期），通知库存医生 |
| EXPIRED | `expiry_date < NOW()` 且 `status IN (0, 1)` | 标记 `status = 2`（过期），不可使用 |

---

## 9. 权限模型

### 9.1 三层权限架构

```
用户(User) ──N:N──> 角色(Role) ──N:N──> 权限(Permission)
     │                    │                    │
     │                    │                    │
  sys_user           sys_role           sys_permission
  sys_user_role      sys_role_permission
```

### 9.2 角色分组

| 分组 | 角色编码 | 角色名称 | 说明 |
|------|----------|----------|------|
| **用户** | USER | 用户 | 家长APP端，预约/儿童档案/记录查看 |
| **医生** | DOCTOR_SIGNIN | 签到医生 | 签到窗口操作 |
| | DOCTOR_PRECHECK | 预检医生 | 预检窗口操作 |
| | DOCTOR_REGISTER | 登记医生 | 登记窗口操作 |
| | DOCTOR_VACCINATE | 接种医生 | 接种窗口操作 |
| | DOCTOR_OBSERVE | 留观医生 | 留观室操作 |
| | DOCTOR_STOCK | 库存管理医生 | 库存窗口操作 |
| | DOCTOR_SCHEDULE | 排班医生 | 排班管理 |
| **管理** | DOCTOR_BUSINESS_ADMIN | 业务管理医生 | 医生权限分配、窗口配置、排班管理 |
| | SUPER_ADMIN | 系统管理员 | 系统最高权限 |

### 9.3 按模块的权限矩阵

| 权限编码 | 权限名称 | USER | SIGNIN | PRECHECK | REGISTER | VACCINATE | OBSERVE | STOCK | SCHEDULE | BIZ_ADMIN | SUPER_ADMIN |
|----------|----------|------|--------|----------|----------|-----------|---------|-------|----------|-----------|-------------|
| appointment.book | 预约接种 | Y | | | | | | | | | |
| appointment.cancel.own | 取消自己的预约 | Y | | | | | | | | | |
| appointment.view.own | 查看自己的预约 | Y | | | | | | | | | |
| appointment.signin | 用户签到 | | Y | | | | | | | | |
| appointment.confirm | 预约确认 | | Y | | | | | | | | |
| appointment.view.today | 查看今日预约 | | Y | | | | | | | | |
| appointment.view.queue | 查看待处理队列 | | | Y | Y | Y | Y | | | | |
| appointment.view.register | 查看待登记队列 | | | | Y | | | | | | |
| appointment.view.vaccinate | 查看待接种队列 | | | | | Y | | | | | |
| appointment.view.observe | 查看留观队列 | | | | | | Y | | | | |
| precheck.assess | 预检评估 | | | Y | | | | | | | |
| precheck.contraindication | 禁忌筛查 | | | Y | | | | | | | |
| precheck.result.view | 查看预检结果 | | | Y | | | | | | | |
| register.verify | 登记核实 | | | | Y | | | | | | |
| register.batch.assign | 批次分配 | | | | Y | | | | | | |
| register.queue.manage | 排队管理 | | | | Y | | | | | | |
| register.view | 查看登记详情 | | | | Y | | | | | | |
| register.save | 保存登记记录 | | | | Y | | | | | | |
| stock.lock | 锁定批次库存 | | | | Y | | | | | | |
| child.view.own | 查看自己的儿童档案 | Y | | | | | | | | | |
| child.add.own | 添加自己的儿童档案 | Y | | | | | | | | | |
| child.edit.own | 修改自己的儿童档案 | Y | | | | | | | | | |
| child.delete.own | 删除自己的儿童档案 | Y | | | | | | | | | |
| vaccinate.execute | 执行接种 | | | | | Y | | | | | |
| vaccinate.record | 记录接种信息 | | | | | Y | | | | | |
| record.view.own | 查看自己的接种记录 | | | | | Y | | | | | |
| record.view.child | 查看儿童接种记录 | | | | | Y | | | | | |
| vaccinate.view | 查看接种详情 | | | | | Y | | | | | |
| vaccinate.verify | 核实儿童/预约/批次信息 | | | | | Y | | | | | |
| vaccinate.site.select | 选择接种部位 | | | | | Y | | | | | |
| vaccinate.id.generate | 生成注射号 | | | | | Y | | | | | |
| stock.deduct | 扣减库存 | | | | | Y | | | | | |
| observe.manage | 留观管理 | | | | | | Y | | | | |
| observe.finish | 留观结束确认 | | | | | | Y | | | | |
| adverse.report | 不良反应上报 | | | | | | Y | | | | |
| adverse.handle | 不良反应处理 | | | | | | Y | | | | |
| stock.view | 查看库存 | | | | | | | Y | | | |
| stock.transfer | 库存调拨 | | | | | | | Y | | | |
| stock.disposal | 批次销毁 | | | | | | | Y | | | |
| batch.manage | 批次管理 | | | | | | | Y | | | |
| batch.view | 查看批次列表和详情 | | | | | | | Y | | | |
| stock.transfer.create | 创建调拨单 | | | | | | | Y | | | |
| stock.transfer.confirm | 确认调拨 | | | | | | | Y | | | |
| stock.transfer.view | 查看调拨记录 | | | | | | | Y | | | |
| batch.disposal | 批次销毁 | | | | | | | Y | | | |
| stock.alert.view | 查看库存预警 | | | | | | | Y | | Y | |
| doctor.schedule.view | 查看排班 | | | | | | | | Y | Y | |
| doctor.schedule.create | 创建排班 | | | | | | | | Y | Y | |
| doctor.schedule.edit | 修改排班 | | | | | | | | Y | Y | |
| doctor.schedule.delete | 删除排班 | | | | | | | | | Y | |
| doctor.assign.role | 分配医生角色 | | | | | | | | | Y | |
| doctor.assign.permission | 分配医生权限 | | | | | | | | | Y | |
| window.manage | 窗口管理 | | | | | | | | | Y | |
| window.service.manage | 窗口服务管理 | | | | | | | | | Y | |
| user.manage | 用户管理 | | | | | | | | | | Y |
| notice.manage | 公告管理 | | | | | | | | | | Y |
| notice.audit | 公告审批 | | | | | | | | | | Y |
| stats.view | 统计分析 | | | | | | | | | | Y |
| all.data.view | 全局数据查看 | | | | | | | | | | Y |

### 9.4 API路径与角色映射

| API前缀 | 允许角色 |
|---------|----------|
| `/api/user/*` | USER |
| `/api/signin/*` | DOCTOR_SIGNIN |
| `/api/precheck/*` | DOCTOR_PRECHECK |
| `/api/register/*` | DOCTOR_REGISTER |
| `/api/vaccinate/*` | DOCTOR_VACCINATE, DOCTOR_BUSINESS_ADMIN |
| `/api/observe/*` | DOCTOR_OBSERVE |
| `/api/stock/*` | DOCTOR_STOCK |
| `/api/schedule/*` | DOCTOR_SCHEDULE, DOCTOR_BUSINESS_ADMIN |
| `/api/business/*` | DOCTOR_BUSINESS_ADMIN |
| `/api/admin/*` | SUPER_ADMIN |
| `/api/public/*` | 无需认证 |

### 9.5 数据权限隔离

| 角色类型 | 数据范围 | 实现方式 |
|----------|---------|----------|
| USER | 仅自己的预约、儿童档案、接种记录 | `WHERE user_id = #{currentUserId}` |
| DOCTOR_VACCINATE | 仅自己执行的接种记录 | `WHERE doctor_id = #{currentDoctorId}` |
| DOCTOR_STOCK | 所有批次和库存 | 无限制 |
| DOCTOR_BUSINESS_ADMIN | 所有统计数据 | 无限制 |
| SUPER_ADMIN | 全局所有数据 | 无限制 |

### 9.6 权限校验流程

```
请求到达
  │
  ▼
解析Token → 获取userId
  │
  ▼
查询 sys_user_role → 获取 roleIds
  │
  ▼
查询 sys_role_permission → 获取 permissionCodes
  │
  ▼
@RequirePermission("vaccinate.execute") 注解校验
  │
  ├─ 通过 → 进入Controller
  └─ 拒绝 → 返回 { code: 1002, message: "无权限" }
```

---

## 10. 时间规则

### 10.1 预约时段定义

| 时段编码 | 时间范围 | 说明 |
|----------|----------|------|
| AM | 08:00 - 12:00 | 上午场 |
| PM | 14:00 - 17:00 | 下午场 |

### 10.2 预约过期规则

| 规则 | 说明 |
|------|------|
| 过期判定 | `appointment_date` 已过当天 17:00 且状态仍为 `1（已预约）` |
| 过期处理 | 系统定时任务扫描，将过期预约状态更新为 `4（已过期）` |
| 扫描频率 | 每小时执行一次 |
| 过期SQL | `UPDATE appointment SET status = 4 WHERE appointment_date < CURDATE() AND status = 1` |

### 10.3 爽约规则

| 规则 | 说明 |
|------|------|
| 爽约定义 | 预约当天或之后取消预约（`cancel_time >= appointment_date`） |
| 正常取消 | 预约日期之前取消（`cancel_time < appointment_date`），不计入爽约 |
| 爽约计数 | 每次爽约 `no_show_count += 1`，累加不重置 |
| 冻结触发 | `no_show_count >= 3` 时，冻结用户预约权限 |
| 冻结时长 | 7天 |
| 冻结实现 | 设置 `freeze_end_time = NOW() + INTERVAL 7 DAY` |
| 冻结校验 | 每次预约前检查 `freeze_end_time > NOW()`，若仍冻结则拒绝 |
| 解冻机制 | 冻结到期后自动解冻（下次请求时判断） |

### 10.4 留观时间规则

| 规则 | 说明 |
|------|------|
| 标准留观时长 | 30分钟 |
| 提前结束 | 不可提前结束（`observe_duration < 30min` 时拒绝确认） |
| 留观开始时间 | 接种完成时间（接种记录的 `injection_time`） |
| 留观结束时间 | 留观医生确认时间（`observe_record.finish_time`） |
| 留观时长计算 | `finish_time - injection_time` |
| observe_record.status | 0=观察中（接种时创建），1=已完成（留观结束时更新） |

### 10.5 批次时间规则

| 规则 | 说明 |
|------|------|
| FEFO可用条件 | `expiry_date > NOW()` 且 `status IN (0, 1)` |
| 临期标记 | `expiry_date - warning_days < NOW()` → `status = 1` |
| 过期标记 | `expiry_date < NOW()` → `status = 2` |
| 过期扫描 | 每小时执行一次，批量更新过期批次状态 |

### 10.6 关键时间参数汇总

| 参数 | 值 | 说明 |
|------|-----|------|
| 预约可提前天数 | 由排班配置决定 | 系统不硬编码，查看排班表 |
| 签到截止时间 | 预约日 17:00 | 超过则视为过期 |
| 预约过期扫描间隔 | 1小时 | 定时任务执行频率 |
| 留观最短时长 | 30分钟 | 低于此值不可结束留观 |
| 爽约冻结时长 | 7天 | 累计3次爽约后冻结 |
| 批次过期扫描间隔 | 1小时 | 定时任务执行频率 |
| 事务超时 | 30秒 | 单事务最长执行时间 |
| 库存预警阈值 | 20% | 剩余率低于此值触发预警 |

---

## §11 数据生命周期管理

### 11.1 实体生命周期阶段

系统中的核心实体遵循以下生命周期阶段：

| 阶段 | 说明 | 对应状态 |
|------|------|---------|
| 创建 | 实体首次写入数据库 | status 初始值 |
| 活跃 | 实体正常使用中 | 非终态状态 |
| 终结 | 实体到达终态，不再变更 | 终态状态（2/3/4/9） |
| 归档 | 终结实体定期归档，从主表迁移至历史表 | — |
| 保留 | 归档数据按合规要求保留一定期限 | — |

### 11.2 数据保留策略

| 实体 | 保留期限 | 归档策略 | 删除策略 |
|------|---------|---------|---------|
| 预约记录 | 就诊后 2 年 | 终结后 90 天归档至 appointment_history | 保留期满后逻辑删除 |
| 留观记录 | 就诊后 2 年 | 终结后 90 天归档至 observe_record_history | 保留期满后逻辑删除 |
| 接种记录 | 永久 | 不归档（法律要求永久保存） | 不删除 |
| 儿童档案 | 永久 | 不归档（含完整接种历史） | 不删除 |
| 库存操作日志 | 1 年 | 创建后 1 年归档至 stock_log_history | 保留期满后物理删除 |
| 调拨记录 | 3 年 | 完成后 1 年归档 | 保留期满后逻辑删除 |
| 销毁记录 | 3 年 | 完成后 1 年归档 | 保留期满后逻辑删除 |
| 操作审计日志 | 1 年 | 创建后 180 天归档 | 保留期满后物理删除 |
| 系统日志 | 180 天 | 创建后 90 天归档 | 保留期满后物理删除 |

### 11.3 归档机制

- **归档任务：** 由定时任务调度（每日凌晨 2:00），扫描满足归档条件的记录
- **归档方式：** INSERT INTO {table}_history SELECT ... FROM {table} WHERE ...; DELETE FROM {table} WHERE ...;
- **归档一致性：** 归档操作在同一数据库事务内完成，确保数据不丢失
- **归档可追溯：** 归档操作记录在审计日志中（操作类型=ARCHIVE）

### 11.4 注销与冻结的数据处理

用户账户注销（F-USER-029）和冻结（F-USER-013）不删除关联数据，仅标记用户状态：

| 用户状态 | 预约记录 | 接种记录 | 儿童档案 | 库存影响 |
|---------|---------|---------|---------|---------|
| 冻结（status=1） | 保留，可查看 | 保留，可查看 | 保留，只读 | 无影响 |
| 注销（status=2） | 保留，历史可查 | 保留，永久保存 | 软删除（status=1） | 无影响 |

> 依据：《儿童预防接种信息报告管理工作规范》要求，接种记录和儿童档案不得物理删除。

---

## 附录A：状态流转完整SQL模板

### A.1 签到（1 → 6）

```sql
BEGIN;
SELECT id, status FROM appointment WHERE id = #{id} FOR UPDATE;
-- assert status = 1
UPDATE appointment
SET status = 6,
    current_window = #{windowCode},
    sign_in_time = NOW()
WHERE id = #{id};
COMMIT;
```

### A.2 预检（6 → 7 或 6 → 9）

```sql
BEGIN;
SELECT id, status FROM appointment WHERE id = #{id} FOR UPDATE;
-- assert status = 6
INSERT INTO pre_check_record (appointment_id, check_time, check_result, ...) VALUES (...);
UPDATE appointment
SET status = #{result},  -- 7=通过, 9=失败
    current_window = #{windowCode}
WHERE id = #{id};
COMMIT;
```

### A.3 登记（7 → 8）

```sql
BEGIN;
SELECT id, status FROM appointment WHERE id = #{id} FOR UPDATE;
-- assert status = 7

SELECT b.id, s.available_stock
FROM vaccine_batch b JOIN hospital_vaccine_stock s ON b.id = s.batch_id
WHERE b.vaccine_id = #{vaccineId} AND b.status = 0
  AND s.available_stock > 0 AND b.expiry_date > NOW()
ORDER BY b.expiry_date ASC LIMIT 1 FOR UPDATE;

UPDATE hospital_vaccine_stock SET locked_stock = locked_stock + 1 WHERE batch_id = #{batchId} AND available_stock > 0;

INSERT INTO register_queue (appointment_id, batch_id, queue_no, register_time) VALUES (...);
UPDATE appointment SET status = 8, batch_id = #{batchId}, current_window = #{windowCode} WHERE id = #{id};
COMMIT;
```

### A.4 接种（8 → 10）

```sql
BEGIN;
SELECT id, status, batch_id FROM appointment WHERE id = #{id} FOR UPDATE;
-- assert status = 8

SELECT available_stock, locked_stock FROM hospital_vaccine_stock WHERE batch_id = #{batchId} FOR UPDATE;
-- assert available_stock >= 1 AND locked_stock >= 1

INSERT INTO vaccination_record (appointment_id, injection_id, injection_time, doctor_id, injection_site, batch_id, batch_no) VALUES (...);
UPDATE hospital_vaccine_stock SET available_stock = available_stock - 1, locked_stock = locked_stock - 1 WHERE batch_id = #{batchId};
UPDATE appointment SET status = 10, current_window = 'OBSERVE' WHERE id = #{id};
COMMIT;
```

### A.5 留观结束（10 → 2）

```sql
BEGIN;
SELECT id, status FROM appointment WHERE id = #{id} FOR UPDATE;
-- assert status = 10
-- assert (NOW() - injection_time) >= 30分钟

-- observe_record 已在接种时创建（status=0，观察中），此处更新为已完成
UPDATE observe_record
SET end_time = NOW(),
    duration = TIMESTAMPDIFF(MINUTE, start_time, NOW()),
    observe_result = #{observeResult},
    doctor_id = #{doctorId},
    status = 1
WHERE appointment_id = #{id};
UPDATE appointment SET status = 2, current_window = NULL WHERE id = #{id};
COMMIT;
```

### A.6 取消预约（1 → 3）

```sql
BEGIN;
SELECT id, status, batch_id FROM appointment WHERE id = #{id} FOR UPDATE;
-- assert status = 1

-- 释放锁定库存（如有，正常取消时batch_id为NULL，无需释放）
-- 若为已登记后取消的特殊情况（预留）：
-- UPDATE hospital_vaccine_stock SET locked_stock = locked_stock - 1 WHERE batch_id = #{batchId} AND locked_stock > 0;

UPDATE appointment SET status = 3, cancel_time = NOW(), cancel_reason = #{reason} WHERE id = #{id};

-- 爽约判定
-- IF cancel_time >= appointment_date THEN UPDATE sys_user SET no_show_count = no_show_count + 1 WHERE id = #{userId};
-- IF no_show_count >= 3 THEN UPDATE sys_user SET freeze_end_time = NOW() + INTERVAL 7 DAY WHERE id = #{userId};

COMMIT;
```

---

## 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| V1.0 | 2026-04-02 | 初始版本，基于SRD V2.1及全部PRD生成 |
| V1.1 | 2026-04-03 | 修复跨模块错误码冲突：(1) §4.4 新增 3007/3009/3010 三个流程模块错误码；(2) §4.5 移除混入的非库存模块代码（4031-4039→§4.4、4041→§4.7 已有6009、4000→重复1003、4012→重复1001、4013→§4.9 新增8015、4014→§4.8 新增7013） |
| V1.2 | 2026-04-03 | REQ 评审修复：(1) §4 新增 §4.10 共享业务错误码段（1900-1999），定义 5 个跨模块复用码（1901-1905）；(2) §6 T7 留观结束事务模板更新：observe_record 从 INSERT 改为 UPDATE（接种时创建，留观结束时更新为已完成） |
| V1.3 | 2026-04-03 | V2 评审修复：(1) §8.4 FEFO 规则扩展：`status = 0` 改为 `status IN (0, 1)`，临期批次可参与分配；(2) §7.2.1 登记阶段 FEFO 查询同步更新；(3) §10.4 补充 observe_record.status 字段说明；(4) 上游依赖版本号对齐 V2.2 |

---

**文档结束**
