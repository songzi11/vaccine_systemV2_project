# 流程执行与规则模块 — 研发需求文档

**文档编号:** REQ-FLOW-001
**版本:** V1.2
**状态:** 正式发布
**日期:** 2026-04-03
**上游依赖:** PRD-FLOW V2.2 / REQ-GLOBAL V1.3

---

## 目录

1. [模块定位与边界](#1-模块定位与边界)
2. [功能清单](#2-功能清单)
3. [状态与窗口映射规则](#3-状态与窗口映射规则)
4. [功能详细规格](#4-功能详细规格)
5. [队列计算规则（汇总参考）](#5-队列计算规则汇总参考)
6. [状态非法校验](#6-状态非法校验)
7. [权限控制汇总](#7-权限控制汇总)
8. [异常错误码定义](#8-异常错误码定义)

---

## 1. 模块定位与边界

### 1.1 核心定位

FLOW 是系统**流程执行与规则层**，负责：
- **状态流转执行** — 执行签到（1→6）、预检（6→7/9）、留观结束（10→2）三类状态变更
- **状态 → 窗口映射** — 根据预约当前状态，计算应去的下一窗口
- **流程指引** — 为用户端（APP）提供下一步操作提示和窗口信息
- **队列计算** — 基于预约记录，统计各窗口待处理队列

### 1.2 本模块职责范围

| 职责类别 | 包含 | 说明 |
|----------|------|------|
| 状态写入 | 签到（1→6）、预检评估（6→7/9）、留观结束（10→2） | 本模块负责的状态变更 |
| 状态写入（其他模块） | 登记（7→8）、接种（8→10）、取消（1→3） | 由 REGISTER / VACCINATE / APPOINTMENT 模块负责 |
| 规则计算 | 窗口指引、队列统计、状态校验 | 纯计算逻辑 |
| 数据写入 | signin_record、pre_check_record、observe_record、adverse_reaction | 本模块负责的业务表 |

### 1.3 与其他模块的关系

```
FLOW（本模块）                           其他执行模块
┌──────────────────┐                  ┌──────────────────┐
│ 状态→窗口映射     │  指引结果       │ REGISTER         │ ← 执行登记，写 status=8
│ 流程指引         │ ──────────────→ │ VACCINATE        │ ← 执行接种，写 status=10
│ 队列计算         │  队列数据       │ APPOINTMENT      │ ← 创建预约，取消预约     │
│                  │ ──────────────→ │                  │
│ 签到执行         │  status 1→6     │                  │
│ 预检执行         │  status 6→7/9   │                  │
│ 留观结束         │  status 10→2    │                  │
└──────────────────┘                  └──────────────────┘
         ↑                                     │
         └──── 查询 appointment 表 ────────────┘
```

**依赖说明：** 本模块的计算查询基于 `appointment` 表，依赖预约的 `status`、`current_window`、`signin_time`、`appointment_date`、`time_slot` 等字段。写入操作遵循 REQ-GLOBAL §3 状态流转约束。

### 1.4 状态机引用

> 本模块的状态码定义、状态流转规则、状态分组均引用 **REQ-GLOBAL 第2节（状态机定义）**，本模块不得自行定义或修改状态值。

状态机核心流转图（引用 REQ-GLOBAL 2.2）：

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

状态分组（引用 REQ-GLOBAL 2.3）：

| 分组 | 状态值 | 说明 |
|------|--------|------|
| 正常进行中 | 1, 6, 7, 8, 10 | 流程尚未结束，仍有后续操作 |
| 正常终态 | 2 | 流程正常完成 |
| 异常终态 | 3, 4, 9 | 流程异常终止，不可恢复 |

---

## 2. 功能清单

| 编号 | 功能名称 | API | 角色 | 子模块 | 类型 | 优先级 | PRD追溯 |
|------|----------|-----|------|--------|------|--------|---------|
| F-FLOW-001 | 获取窗口指引 | `GET /api/user/appointment/{appointmentId}/guide` | USER | 用户端 | 查询 | P0 | PRD-FLOW F-FLOW-003 |
| F-FLOW-002 | 获取排队信息 | `GET /api/user/appointment/{appointmentId}/queue` | USER | 用户端 | 查询 | P1 | PRD-FLOW F-FLOW-004 |
| F-FLOW-003 | 查询今日预约 | `GET /api/signin/today` | DOCTOR_SIGNIN | 签到 | 查询 | P0 | PRD-FLOW F-FLOW-001 |
| F-FLOW-004 | 查看待预检队列 | `GET /api/precheck/queue` | DOCTOR_PRECHECK | 预检 | 查询 | P0 | PRD-FLOW F-FLOW-005 |
| F-FLOW-005 | 查看留观队列 | `GET /api/observe/queue` | DOCTOR_OBSERVE | 留观 | 查询 | P0 | PRD-FLOW F-FLOW-008 |
| F-FLOW-006 | 执行预检评估 | `POST /api/precheck/assess` | DOCTOR_PRECHECK | 预检 | 写入 | P0 | PRD-FLOW F-FLOW-006 |
| F-FLOW-007 | 执行禁忌筛查 | `POST /api/precheck/contraindication` | DOCTOR_PRECHECK | 预检 | 计算 | P0 | PRD-FLOW F-FLOW-007 |
| F-FLOW-008 | 执行签到 | `POST /api/signin/signin` | DOCTOR_SIGNIN | 签到 | 写入 | P0 | PRD-FLOW F-FLOW-002 |
| F-FLOW-009 | 留观状态监控 | `GET /api/observe/status/{injectionId}` | DOCTOR_OBSERVE | 留观 | 查询 | P0 | PRD-FLOW F-FLOW-009 |
| F-FLOW-010 | 确认留观结束 | `POST /api/observe/finish` | DOCTOR_OBSERVE | 留观 | 写入 | P0 | PRD-FLOW F-FLOW-010 |
| F-FLOW-011 | 上报不良反应 | `POST /api/observe/adverse` | DOCTOR_OBSERVE | 留观 | 写入 | P1 | PRD-FLOW F-FLOW-011 |
| F-FLOW-012 | 处理不良反应 | `POST /api/observe/adverse/handle` | DOCTOR_OBSERVE | 留观 | 写入 | P1 | PRD-FLOW F-FLOW-012 |

> **说明：** REQ-FLOW 的功能编号与 PRD-FLOW 不完全一致（PRD 按子模块分组编号，REQ 按功能类型排序编号）。PRD 追溯列标注了对应的 PRD 原始编号。

---

## 3. 状态与窗口映射规则

### 3.1 映射定义

> 本节定义预约状态到窗口的映射关系，是窗口指引和队列计算的核心依据。

| 当前 status | 状态名称 | 下一步窗口职能 | window_function_type | 说明 |
|-------------|----------|----------------|---------------------|------|
| 1 | 已预约 | 签到窗口 | `SIGNIN` | 用户已预约，需到院签到 |
| 6 | 已签到 | 预检窗口 | `PRECHECK` | 签到完成，等待预检 |
| 7 | 预检通过 | 登记窗口 | `REGISTER` | 预检通过，等待登记 |
| 8 | 已登记 | 接种窗口 | `VACCINATE` | 登记完成，等待接种 |
| 10 | 留观中 | 留观室 | `OBSERVE` | 接种完成，留观中 |
| 2 | 已完成 | 无（终态） | — | 流程正常结束 |
| 3 | 已取消 | 无（终态） | — | 用户主动取消 |
| 4 | 已过期 | 无（终态） | — | 预约过期 |
| 9 | 预检失败 | 无（终态） | — | 预检未通过 |

### 3.2 current_window 设置规则

> 引用 REQ-GLOBAL 2.4，`current_window` 由各执行模块写入。

| 操作 | current_window 值 | 写入模块 |
|------|-------------------|----------|
| 创建预约 | `NULL` | APPOINTMENT |
| 签到完成 | 签到窗口编码（如 `SIGNIN01`） | FLOW（F-FLOW-008） |
| 预检完成 | 预检窗口编码（如 `PRECHECK01`） | FLOW（F-FLOW-006） |
| 登记完成 | 登记窗口编码（如 `REGISTER01`） | REGISTER |
| 接种完成 | `OBSERVE` | VACCINATE |
| 留观结束 | `NULL` | FLOW（F-FLOW-010） |
| 取消/过期 | `NULL` | APPOINTMENT |

### 3.3 映射计算伪代码

```java
/**
 * 根据预约状态计算下一步窗口指引
 * 本方法为纯计算，不修改任何数据
 */
public WindowGuide calculateGuide(Appointment appointment) {
    int status = appointment.getStatus();

    switch (status) {
        case 1:
            return new WindowGuide("签到", "您已成功预约，请到签到窗口办理签到", "SIGNIN");
        case 6:
            return new WindowGuide("预检", "签到成功，请到预检窗口等待预检", "PRECHECK");
        case 7:
            return new WindowGuide("登记", "预检通过，请到登记窗口排队", "REGISTER");
        case 8:
            return new WindowGuide("接种", "登记完成，请到接种窗口等待接种", "VACCINATE");
        case 10:
            return new WindowGuide("留观", "接种完成，请在留观室观察30分钟", "OBSERVE");
        case 2:
            return new WindowGuide("完成", "接种流程已完成，感谢您的配合", null);
        case 3:
            return new WindowGuide("已取消", "该预约已取消", null);
        case 4:
            return new WindowGuide("已过期", "该预约已过期", null);
        case 9:
            return new WindowGuide("预检未通过", "预检未通过，请咨询医生后重新预约", null);
        default:
            throw new BusinessException(1007, "未知的预约状态");
    }
}
```

---

## 4. 功能详细规格

### 4.1 F-FLOW-001 获取窗口指引

#### 4.1.1 功能描述

根据预约当前状态，返回下一步窗口指引信息。本接口为**纯查询接口**，不修改任何数据。

#### 4.1.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 用户已登录 | Token 有效 |
| PRE-002 | 预约记录存在 | `appointment.id = #{appointmentId}` |
| PRE-003 | 预约归属当前用户 | `appointment.user_id = #{userId}` |

#### 4.1.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| appointmentId | Long | 是 | 路径参数 | 预约ID |
| userId | Long | 是 | Token 解析 | 当前登录用户ID |

#### 4.1.4 处理流程

```
STEP 1: 参数校验
  └─ appointmentId 非空且为正整数
  → 失败返回 1003 BAD_REQUEST

STEP 2: 查询预约当前状态（只读）
  ├─ SELECT id, status, user_id, current_window FROM appointment WHERE id = #{appointmentId}
  └─ 记录不存在 → 返回 1901 APPOINT_NOT_FOUND

STEP 3: 归属校验
  └─ user_id != #{userId} → 返回 1002 FORBIDDEN

STEP 4: 根据状态匹配指引规则（纯计算，见第3.1节映射表）
  ├─ status = 1  → 指引到签到窗口
  ├─ status = 6  → 指引到预检窗口
  ├─ status = 7  → 指引到登记窗口
  ├─ status = 8  → 指引到接种窗口
  ├─ status = 10 → 指引到留观室
  ├─ status = 2  → 流程已完成（终态）
  ├─ status = 3  → 已取消（终态）
  ├─ status = 4  → 已过期（终态）
  └─ status = 9  → 预检未通过（终态）

STEP 5: 查询窗口信息（仅进行中状态需要）
  ├─ SELECT window_code, window_name, window_function_type FROM hospital_window
  │   WHERE window_function_type = #{nextWindowType} AND status = 1
  └─ 终态（2,3,4,9）不需要查询窗口信息，windowCode 返回 null

STEP 6: 组装返回结果
  └─ 返回 WindowGuide VO
```

#### 4.1.5 返回结构（WindowGuide VO）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "appointmentId": 1001,
    "status": 6,
    "statusText": "已签到",
    "nextGuide": {
      "nextAction": "预检",
      "guideMessage": "签到成功，请到预检窗口等待预检",
      "windowCode": "PRECHECK01",
      "windowName": "预检窗口1",
      "windowFunctionType": "PRECHECK"
    }
  },
  "timestamp": 1712035200000
}
```

#### 4.1.6 数据操作（SQL级）

```sql
-- STEP 2: 查询预约状态（只读）
SELECT id, status, user_id, current_window
FROM appointment
WHERE id = #{appointmentId};

-- STEP 5: 查询窗口信息（仅进行中状态时调用，只读）
SELECT window_code, window_name, window_function_type
FROM hospital_window
WHERE window_function_type = #{nextWindowType}
  AND status = 1
LIMIT 1;
```

#### 4.1.7 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 预约不存在 | 1901 | 404 | 返回"预约不存在" |
| 无权查看 | 1002 | 403 | 返回"无权操作该预约" |
| 未登录 | 1001 | 401 | 拦截器拦截 |

#### 4.1.8 并发控制

本接口为只读查询，无需并发控制。

#### 4.1.9 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `appointment.view.own` |
| 数据权限 | SQL WHERE `user_id = #{userId}` |
| API前缀 | `/api/user/*` — 仅 USER 角色可访问 |

---

### 4.2 F-FLOW-002 获取排队信息

#### 4.2.1 功能描述

根据预约当前状态对应的窗口职能，计算该窗口的排队信息。本接口为**纯查询/计算接口**，不修改任何数据。

#### 4.2.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 用户已登录 | Token 有效 |
| PRE-002 | 预约记录存在 | `appointment.id = #{appointmentId}` |
| PRE-003 | 预约归属当前用户 | `appointment.user_id = #{userId}` |
| PRE-004 | 预约处于进行中状态 | `status IN (1, 6, 7, 8, 10)` |

#### 4.2.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| appointmentId | Long | 是 | 路径参数 | 预约ID |
| userId | Long | 是 | Token 解析 | 当前登录用户ID |

#### 4.2.4 处理流程

```
STEP 1: 参数校验
  └─ appointmentId 非空且为正整数
  → 失败返回 1003 BAD_REQUEST

STEP 2: 查询预约当前状态（只读）
  ├─ SELECT id, status, user_id FROM appointment WHERE id = #{appointmentId}
  └─ 记录不存在 → 返回 1901 APPOINT_NOT_FOUND

STEP 3: 归属校验
  └─ user_id != #{userId} → 返回 1002 FORBIDDEN

STEP 4: 状态校验（必须为进行中）
  └─ status NOT IN (1, 6, 7, 8, 10) → 返回提示"当前预约不在进行中，无需排队"

STEP 5: 根据状态确定目标窗口职能（引用第3.1节映射表）
  ├─ status = 1  → windowFunctionType = SIGNIN
  ├─ status = 6  → windowFunctionType = PRECHECK
  ├─ status = 7  → windowFunctionType = REGISTER
  ├─ status = 8  → windowFunctionType = VACCINATE
  └─ status = 10 → windowFunctionType = OBSERVE

STEP 6: 计算排队人数（只读统计，见第5节队列计算规则）
  └─ currentQueue = COUNT(同窗口职能下待处理的预约数) - 1（排除当前预约自身）

STEP 7: 查询窗口配置
  └─ SELECT window_code, window_name, avg_handle_time FROM hospital_window
      WHERE window_function_type = #{windowFunctionType} AND status = 1

STEP 8: 计算预估等待时间
  └─ estimatedWaitTime = currentQueue × avg_handle_time（分钟）

STEP 9: 组装返回结果
  └─ 返回 QueueInfo VO
```

#### 4.2.5 返回结构（QueueInfo VO）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "appointmentId": 1001,
    "status": 6,
    "windowCode": "PRECHECK01",
    "windowName": "预检窗口1",
    "windowFunctionType": "PRECHECK",
    "currentQueue": 3,
    "capacity": 1,
    "estimatedWaitTime": 15
  },
  "timestamp": 1712035200000
}
```

#### 4.2.6 数据操作（SQL级）

```sql
-- STEP 2: 查询预约状态（只读）
SELECT id, status, user_id
FROM appointment
WHERE id = #{appointmentId};

-- STEP 6: 计算排队人数（只读统计）
SELECT COUNT(*) - 1 AS current_queue
FROM appointment
WHERE status = #{targetStatus}
  AND appointment_date = CURDATE()
  AND id != #{appointmentId};

-- STEP 7: 查询窗口配置（只读）
SELECT window_code, window_name, avg_handle_time
FROM hospital_window
WHERE window_function_type = #{windowFunctionType}
  AND status = 1;
```

#### 4.2.7 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 预约不存在 | 1901 | 404 | 返回"预约不存在" |
| 无权查看 | 1002 | 403 | 返回"无权操作该预约" |
| 非进行中状态 | 200 | 200 | 返回提示信息，currentQueue=0 |
| 未登录 | 1001 | 401 | 拦截器拦截 |

#### 4.2.8 并发控制

本接口为只读查询，无需并发控制。

#### 4.2.9 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `appointment.view.own` |
| 数据权限 | SQL WHERE `user_id = #{userId}` |
| API前缀 | `/api/user/*` — 仅 USER 角色可访问 |

---

### 4.3 F-FLOW-003 查询今日预约

#### 4.3.1 功能描述

签到医生查询当日预约列表，支持按签到状态筛选。本接口为**只读查询**。

#### 4.3.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 医生已登录 | Token 有效 |
| PRE-002 | 当前角色为 DOCTOR_SIGNIN | 角色校验 |

#### 4.3.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| date | Date | 否 | 查询参数 | 查询日期，默认当天 |
| filter | String | 否 | 查询参数 | 筛选值：ALL / ARRIVED / NOT_ARRIVED |

#### 4.3.4 处理流程

```
STEP 1: 参数校验
  └─ date 格式校验，filter 枚举校验
  → 失败返回 1003 BAD_REQUEST

STEP 2: 根据筛选条件构建查询
  ├─ filter=ALL → WHERE status IN (1, 6, 7, 8, 9, 10)
  ├─ filter=ARRIVED → WHERE status >= 6
  └─ filter=NOT_ARRIVED → WHERE status = 1

STEP 3: 执行查询
  └─ SELECT ... FROM appointment JOIN child_profile WHERE appointment_date = #{date}
  ORDER BY create_time ASC

STEP 4: 组装返回结果
  └─ 返回预约列表 VO
```

#### 4.3.5 返回结构

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "appointmentId": 1001,
      "appointmentNo": "APT202604100001",
      "status": 1,
      "appointmentDate": "2026-04-10",
      "timeSlot": "AM",
      "signinTime": null,
      "childName": "张小明"
    }
  ],
  "timestamp": 1712035200000
}
```

#### 4.3.6 数据操作（SQL级）

```sql
-- 基础查询（只读）
SELECT
    a.id,
    a.appointment_no,
    a.status,
    a.appointment_date,
    a.time_slot,
    a.signin_time,
    cp.name AS child_name
FROM appointment a
LEFT JOIN child_profile cp ON a.child_id = cp.id
WHERE a.appointment_date = #{date}
  AND a.status IN (1, 6, 7, 8, 9, 10)
ORDER BY a.create_time ASC;
```

#### 4.3.7 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 未登录 | 1001 | 401 | 拦截器拦截 |
| 角色无权限 | 1002 | 403 | 返回"无权访问" |
| 参数格式错误 | 1003 | 400 | 返回具体字段错误 |

#### 4.3.8 并发控制

本接口为只读查询，无需并发控制。

#### 4.3.9 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `appointment.view.today` |
| 数据权限 | 当日预约（全部），按 `appointment_date` 过滤 |
| API前缀 | `/api/signin/*` — 仅 DOCTOR_SIGNIN 角色可访问 |

---

### 4.4 F-FLOW-004 查看待预检队列

#### 4.4.1 功能描述

预检医生查询待预检的预约列表。本接口为**只读查询**。

#### 4.4.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 医生已登录 | Token 有效 |
| PRE-002 | 当前角色为 DOCTOR_PRECHECK | 角色校验 |

#### 4.4.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| date | Date | 否 | 查询参数 | 查询日期，默认当天 |

#### 4.4.4 处理流程

```
STEP 1: 参数校验
  └─ date 格式校验
  → 失败返回 1003 BAD_REQUEST

STEP 2: 查询待预检队列（status=6）
  └─ SELECT ... FROM appointment JOIN child_profile
      WHERE appointment_date = #{date} AND status = 6
      ORDER BY signin_time ASC

STEP 3: 计算等待时长
  └─ wait_duration = TIMESTAMPDIFF(MINUTE, signin_time, NOW())

STEP 4: 组装返回结果
  └─ 返回预检队列列表 VO
```

#### 4.4.5 返回结构

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "appointmentId": 1001,
      "appointmentNo": "APT202604100001",
      "signinTime": "2026-04-10 09:05:00",
      "waitDuration": 15,
      "childName": "张小明"
    }
  ],
  "timestamp": 1712035200000
}
```

#### 4.4.6 数据操作（SQL级）

```sql
SELECT
    a.id,
    a.appointment_no,
    a.signin_time,
    TIMESTAMPDIFF(MINUTE, a.signin_time, NOW()) AS wait_duration,
    cp.name AS child_name
FROM appointment a
LEFT JOIN child_profile cp ON a.child_id = cp.id
WHERE a.appointment_date = #{date}
  AND a.status = 6
ORDER BY a.signin_time ASC;
```

#### 4.4.7 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 未登录 | 1001 | 401 | 拦截器拦截 |
| 角色无权限 | 1002 | 403 | 返回"无权访问" |

#### 4.4.8 并发控制

本接口为只读查询，无需并发控制。

#### 4.4.9 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `appointment.view.queue` |
| 数据权限 | 当日待预检（全部），按 `appointment_date` 过滤 |
| API前缀 | `/api/precheck/*` — 仅 DOCTOR_PRECHECK 角色可访问 |

---

### 4.5 F-FLOW-005 查看留观队列

#### 4.5.1 功能描述

留观医生查看当前留观中的预约列表。本接口为**只读查询**。

#### 4.5.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 医生已登录 | Token 有效 |
| PRE-002 | 当前角色为 DOCTOR_OBSERVE | 角色校验 |

#### 4.5.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| date | Date | 否 | 查询参数 | 查询日期，默认当天 |

#### 4.5.4 处理流程

```
STEP 1: 参数校验
  └─ date 格式校验
  → 失败返回 1003 BAD_REQUEST

STEP 2: 查询留观队列（status=10）
  └─ SELECT ... FROM appointment JOIN child_profile JOIN vaccination_record
      WHERE appointment_date = #{date} AND status = 10
      ORDER BY injection_time ASC

STEP 3: 计算留观时长和状态
  ├─ observe_duration = TIMESTAMPDIFF(MINUTE, injection_time, NOW())
  ├─ remaining_duration = 30 - observe_duration
  └─ observe_status = observe_duration >= 30 ? 'CAN_FINISH' : 'OBSERVING'

STEP 4: 组装返回结果
  └─ 返回留观队列列表 VO
```

#### 4.5.5 返回结构

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "appointmentId": 1001,
      "appointmentNo": "APT202604100001",
      "injectionId": "INJ202604100001",
      "observeStartTime": "2026-04-10 09:45:00",
      "observeDuration": 15,
      "remainingDuration": 15,
      "observeStatus": "OBSERVING",
      "childName": "张小明"
    }
  ],
  "timestamp": 1712035200000
}
```

#### 4.5.6 数据操作（SQL级）

```sql
SELECT
    a.id,
    a.appointment_no,
    vr.injection_id,
    vr.injection_time AS observe_start_time,
    TIMESTAMPDIFF(MINUTE, vr.injection_time, NOW()) AS observe_duration,
    30 - TIMESTAMPDIFF(MINUTE, vr.injection_time, NOW()) AS remaining_duration,
    CASE
        WHEN TIMESTAMPDIFF(MINUTE, vr.injection_time, NOW()) >= 30 THEN 'CAN_FINISH'
        ELSE 'OBSERVING'
    END AS observe_status,
    cp.name AS child_name
FROM appointment a
LEFT JOIN child_profile cp ON a.child_id = cp.id
LEFT JOIN vaccination_record vr ON vr.appointment_id = a.id
WHERE a.appointment_date = #{date}
  AND a.status = 10
ORDER BY vr.injection_time ASC;
```

#### 4.5.7 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 未登录 | 1001 | 401 | 拦截器拦截 |
| 角色无权限 | 1002 | 403 | 返回"无权访问" |

#### 4.5.8 并发控制

本接口为只读查询，无需并发控制。

#### 4.5.9 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `appointment.view.queue` |
| 数据权限 | 当日留观中（全部），按 `appointment_date` 过滤 |
| API前缀 | `/api/observe/*` — 仅 DOCTOR_OBSERVE 角色可访问 |

---

### 4.6 F-FLOW-008 执行签到

> **事务引用：** REQ-GLOBAL §6 T3（签到事务）

#### 4.6.1 功能描述

签到医生核实家长身份后执行签到操作，将预约状态从"已预约"（1）变更为"已签到"（6），同时写入签到记录。

#### 4.6.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 医生已登录 | Token 有效 |
| PRE-002 | 当前角色为 DOCTOR_SIGNIN | 角色校验 |
| PRE-003 | 预约记录存在 | `appointment.id = #{appointmentId}` |
| PRE-004 | 预约状态为"已预约" | `appointment.status = 1` |
| PRE-005 | 预约日期有效 | `appointment.appointment_date <= CURDATE()` |

#### 4.6.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| appointmentId | Long | 是 | 请求体 | 预约ID |
| parentIdCard | String | 是 | 请求体 | 家长身份证号（18位） |
| doctorId | Long | 是 | Token 解析 | 当前签到医生ID |

#### 4.6.4 处理流程

```
STEP 1: 参数校验
  ├─ appointmentId 非空且为正整数
  └─ parentIdCard 非空且为18位
  → 失败返回 1003 BAD_REQUEST

STEP 2: 事务开始（T3）

STEP 3: 加行锁查询预约
  ├─ SELECT id, status, appointment_date, child_id FROM appointment
  │   WHERE id = #{appointmentId} FOR UPDATE
  └─ 记录不存在 → 返回 1901 APPOINT_NOT_FOUND

STEP 4: 状态校验
  └─ status != 1 → 返回 3001 SIGNIN_STATUS_INVALID

STEP 5: 日期校验
  └─ appointment_date > CURDATE() → 返回 3007 SIGNIN_DATE_INVALID

STEP 6: 身份证号匹配校验
  ├─ SELECT parent_id_card FROM child_profile WHERE id = #{childId}
  └─ parent_id_card != #{parentIdCard} → 返回 3002 SIGNIN_IDCARD_MISMATCH

STEP 7: 写入签到记录
  └─ INSERT INTO signin_record (appointment_id, doctor_id, window_code,
      id_card_input, id_card_match, signin_time, create_time)
      VALUES (#{appointmentId}, #{doctorId}, #{windowCode},
      #{parentIdCard}, 1, NOW(), NOW())

STEP 8: 更新预约状态
  └─ UPDATE appointment
      SET status = 6, current_window = #{windowCode}, signin_time = NOW()
      WHERE id = #{appointmentId}

STEP 9: 事务提交

STEP 10: 组装返回结果
  └─ 返回签到成功信息
```

#### 4.6.5 返回结构

```json
{
  "code": 200,
  "message": "签到成功",
  "data": {
    "appointmentId": 1001,
    "status": 6,
    "statusText": "已签到",
    "currentWindow": "SIGNIN01",
    "signinTime": "2026-04-10 09:05:00",
    "nextGuide": {
      "nextAction": "预检",
      "guideMessage": "签到成功，请到预检窗口等待预检",
      "windowFunctionType": "PRECHECK"
    }
  },
  "timestamp": 1712035200000
}
```

#### 4.6.6 数据操作（SQL级）

```sql
-- STEP 3: 加行锁查询预约
SELECT id, status, appointment_date, child_id
FROM appointment
WHERE id = #{appointmentId}
FOR UPDATE;

-- STEP 6: 查询儿童档案身份证号
SELECT parent_id_card
FROM child_profile
WHERE id = #{childId};

-- STEP 7: 写入签到记录
INSERT INTO signin_record (
    appointment_id, doctor_id, window_code,
    id_card_input, id_card_match, signin_time, create_time
) VALUES (
    #{appointmentId}, #{doctorId}, #{windowCode},
    #{parentIdCard}, 1, NOW(), NOW()
);

-- STEP 8: 更新预约状态
UPDATE appointment
SET status = 6, current_window = #{windowCode}, signin_time = NOW()
WHERE id = #{appointmentId};
```

#### 4.6.7 状态流转

| 操作 | 原状态 | 新状态 | 说明 |
|------|--------|--------|------|
| 执行签到 | 1（已预约） | 6（已签到） | 写入 signin_time 和 current_window |

#### 4.6.8 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 预约不存在 | 1901 | 404 | 返回"预约不存在" |
| 预约状态不允许签到 | 3001 | 409 | 返回"该预约已签到或状态异常，无法重复签到" |
| 预约日期未到 | 3007 | 409 | 返回"该预约未到预约日期，无法签到" |
| 身份证号不匹配 | 3002 | 400 | 返回"身份证号不匹配，请核对后重试" |
| 未登录 | 1001 | 401 | 拦截器拦截 |
| 角色无权限 | 1002 | 403 | 返回"无权执行签到操作" |

#### 4.6.9 并发控制

| 控制项 | 策略 |
|--------|------|
| 行锁 | `SELECT ... FOR UPDATE` 锁定 appointment 行，防止并发签到 |
| 事务范围 | T3：appointment + signin_record（引用 REQ-GLOBAL §6） |
| 隔离级别 | READ_COMMITTED |

#### 4.6.10 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `appointment.signin` |
| 数据权限 | 无额外限制（通过预约ID直接操作） |
| API前缀 | `/api/signin/*` — 仅 DOCTOR_SIGNIN 角色可访问 |

---

### 4.7 F-FLOW-006 执行预检评估

> **事务引用：** REQ-GLOBAL §6 T4（预检事务）

#### 4.7.1 功能描述

预检医生对儿童进行健康状况评估，记录各项指标并生成预检报告。预检通过后自动调用禁忌筛查（F-FLOW-007），根据综合结果决定状态流转：预检通过（6→7）或预检失败（6→9）。

#### 4.7.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 医生已登录 | Token 有效 |
| PRE-002 | 当前角色为 DOCTOR_PRECHECK | 角色校验 |
| PRE-003 | 预约记录存在 | `appointment.id = #{appointmentId}` |
| PRE-004 | 预约状态为"已签到" | `appointment.status = 6` |

#### 4.7.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| appointmentId | Long | 是 | 请求体 | 预约ID |
| doctorId | Long | 是 | Token 解析 | 当前预检医生ID |
| temperature | Decimal(3,1) | 是 | 请求体 | 体温（℃） |
| weight | Decimal(4,1) | 否 | 请求体 | 体重（kg） |
| height | Decimal(4,1) | 否 | 请求体 | 身高（cm） |
| healthStatus | String | 是 | 请求体 | 健康状况：GOOD / GENERAL / POOR |
| medicationStatus | String | 否 | 请求体 | 用药情况 |
| allergyHistory | String | 否 | 请求体 | 过敏史 |
| recentVaccination | String | 否 | 请求体 | 近期接种史 |
| diseaseHistory | String | 否 | 请求体 | 疾病史 |
| checkResult | String | 是 | 请求体 | 预检结果：PASS / FAIL |
| failReason | String | 条件必填 | 请求体 | 未通过原因（checkResult=FAIL 时必填） |

#### 4.7.4 处理流程

```
STEP 1: 参数校验
  ├─ appointmentId 非空且为正整数
  ├─ temperature 非空
  ├─ healthStatus 非空且为 GOOD/GENERAL/POOR
  ├─ checkResult 非空且为 PASS/FAIL
  └─ checkResult=FAIL 时 failReason 非空
  → 失败返回 1003 BAD_REQUEST

STEP 2: 事务开始（T4）

STEP 3: 加行锁查询预约
  ├─ SELECT id, status, vaccine_id, child_id FROM appointment
  │   WHERE id = #{appointmentId} FOR UPDATE
  └─ 记录不存在 → 返回 1901 APPOINT_NOT_FOUND

STEP 4: 状态校验
  └─ status != 6 → 返回 3003 PRECHECK_STATUS_INVALID

STEP 5: 写入预检记录
  └─ INSERT INTO pre_check_record (appointment_id, check_time, doctor_id,
      window_code, temperature, weight, height, health_status,
      medication_status, allergy_history, recent_vaccination,
      disease_history, check_result, fail_reason, create_time)
      VALUES (..., NOW())

STEP 6: 预检结果分支
  ├─ checkResult = PASS → 自动调用禁忌筛查（F-FLOW-007）
  │   ├─ 禁忌筛查 PASS → 预约状态更新为 7（预检通过）
  │   │   └─ UPDATE appointment SET status = 7, current_window = #{windowCode}
  │   │       WHERE id = #{appointmentId}
  │   └─ 禁忌筛查 FAIL → 预约状态更新为 9（预检失败）
  │       └─ UPDATE pre_check_record SET contraindication_check = 'FAIL',
  │           contraindication_type = #{type} WHERE appointment_id = #{appointmentId}
  │       └─ UPDATE appointment SET status = 9 WHERE id = #{appointmentId}
  └─ checkResult = FAIL → 预约状态更新为 9（预检失败）
      └─ UPDATE appointment SET status = 9 WHERE id = #{appointmentId}

STEP 6b-补充: 释放已锁定库存（防御性补偿）
  └─ 正常预检流程（status=6）中尚未锁库存，此步骤为防御性补偿。
      仅在"登记后（已锁库存）→ 发现禁忌"的异常路径中触发。

> 库存释放操作涉及 hospital_vaccine_stock 的 available_stock/locked_stock 字段，库存双字段模型和不变量参见 REQ-GLOBAL §8.1 库存双字段模型。
  └─ 检查是否存在关联的锁定库存记录
      ├─ SELECT locked_stock FROM hospital_vaccine_stock
      │   WHERE batch_id IN (
      │       SELECT batch_id FROM appointment WHERE id = #{appointmentId}
      │   ) AND locked_stock > 0
      └─ 如存在 → UPDATE hospital_vaccine_stock
          SET locked_stock = locked_stock - 1,
              available_stock = available_stock + 1
          WHERE batch_id IN (
              SELECT batch_id FROM appointment WHERE id = #{appointmentId}
          )

STEP 7: 事务提交

STEP 8: 组装返回结果
  └─ 返回预检记录信息
```

#### 4.7.5 返回结构

```json
{
  "code": 200,
  "message": "预检完成",
  "data": {
    "precheckRecordId": 5001,
    "appointmentId": 1001,
    "checkTime": "2026-04-10 09:20:00",
    "checkResult": "PASS",
    "contraindicationCheck": "PASS",
    "newStatus": 7,
    "statusText": "预检通过"
  },
  "timestamp": 1712035200000
}
```

#### 4.7.6 数据操作（SQL级）

```sql
-- STEP 3: 加行锁查询预约
SELECT id, status, vaccine_id, child_id
FROM appointment
WHERE id = #{appointmentId}
FOR UPDATE;

-- STEP 5: 写入预检记录
INSERT INTO pre_check_record (
    appointment_id, check_time, doctor_id, window_code,
    temperature, weight, height, health_status,
    medication_status, allergy_history, recent_vaccination,
    disease_history, check_result, fail_reason, create_time
) VALUES (
    #{appointmentId}, NOW(), #{doctorId}, #{windowCode},
    #{temperature}, #{weight}, #{height}, #{healthStatus},
    #{medicationStatus}, #{allergyHistory}, #{recentVaccination},
    #{diseaseHistory}, #{checkResult}, #{failReason}, NOW()
);

-- STEP 6a: 预检通过且禁忌筛查通过
UPDATE appointment
SET status = 7, current_window = #{windowCode}
WHERE id = #{appointmentId};

-- STEP 6b: 预检失败或禁忌筛查失败
UPDATE appointment SET status = 9 WHERE id = #{appointmentId};
```

#### 4.7.7 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 预约不存在 | 1901 | 404 | 返回"预约不存在" |
| 预约状态不允许预检 | 3003 | 409 | 返回"该预约状态异常，无法进行预检" |
| 必填参数缺失 | 1003 | 400 | 返回"体温和健康状况为必填项" |
| 预检失败原因缺失 | 1003 | 400 | 返回"预检未通过时，未通过原因为必填项" |
| 禁忌筛查不通过 | 3009 | 409 | 返回禁忌症类型和说明 |
| 未登录 | 1001 | 401 | 拦截器拦截 |
| 角色无权限 | 1002 | 403 | 返回"无权执行预检操作" |

#### 4.7.8 状态流转

| 操作 | 原状态 | 新状态 | 说明 |
|------|--------|--------|------|
| 预检通过 + 禁忌通过 | 6（已签到） | 7（预检通过） | 进入登记环节 |
| 预检评估失败 | 6（已签到） | 9（预检失败） | 流程终止 |
| 预检通过 + 禁忌失败 | 6（已签到） | 9（预检失败） | 流程终止 |

#### 4.7.9 并发控制

| 控制项 | 策略 |
|--------|------|
| 行锁 | `SELECT ... FOR UPDATE` 锁定 appointment 行 |
| 事务范围 | T4：appointment + pre_check_record（引用 REQ-GLOBAL §6） |
| 隔离级别 | READ_COMMITTED |

#### 4.7.10 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `precheck.assess` |
| 数据权限 | 无额外限制（通过预约ID直接操作） |
| API前缀 | `/api/precheck/*` — 仅 DOCTOR_PRECHECK 角色可访问 |

---

### 4.8 F-FLOW-007 执行禁忌筛查

#### 4.8.1 功能描述

根据预检评估数据，执行禁忌症筛查和风险评估。本接口可由 F-FLOW-006（预检评估）内部自动调用，也可独立调用。筛查范围包括：过敏史、疾病史、近期接种史、免疫程序。本接口为纯计算接口，不修改任何数据。筛查结果由调用方（F-FLOW-006）决定如何处理。

#### 4.8.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 医生已登录 | Token 有效 |
| PRE-002 | 当前角色为 DOCTOR_PRECHECK | 角色校验 |
| PRE-003 | 预约记录存在 | `appointment.id = #{appointmentId}` |

#### 4.8.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| appointmentId | Long | 是 | 请求体 | 预约ID |
| vaccineId | Long | 是 | 请求体 | 疫苗ID |
| allergyHistory | String | 是 | 请求体 | 过敏史 |
| diseaseHistory | String | 是 | 请求体 | 疾病史 |
| recentVaccination | String | 是 | 请求体 | 近期接种史 |

#### 4.8.4 处理流程

```
STEP 1: 参数校验
  ├─ appointmentId 非空且为正整数
  ├─ vaccineId 非空且为正整数
  ├─ allergyHistory 非空
  ├─ diseaseHistory 非空
  └─ recentVaccination 非空
  → 失败返回 1003 BAD_REQUEST

STEP 2: 查询预约和疫苗信息（只读）
  ├─ SELECT id, status, child_id FROM appointment WHERE id = #{appointmentId}
  ├─ SELECT id, contraindication_info FROM vaccine WHERE id = #{vaccineId}
  └─ SELECT id, birth_date FROM child_profile WHERE id = #{childId}

STEP 3: 执行四项筛查（纯计算）
  ├─ 筛查1：过敏史 — 检查 allergyHistory 是否包含该疫苗成分
  │   └─ 命中 → result=FAIL, type=ALLERGY
  ├─ 筛查2：疾病史 — 检查 diseaseHistory 是否包含该疫苗禁忌疾病
  │   └─ 命中 → result=FAIL, type=DISEASE
  ├─ 筛查3：近期接种 — 检查 recentVaccination 是否包含14天内的接种记录（依据《国家免疫规划疫苗儿童免疫程序及说明（2021年版）》最小接种间隔标准）
  │   └─ 命中 → result=FAIL, type=RECENT_VACCINATION
  └─ 筛查4：免疫程序 — 检查儿童年龄是否符合疫苗适龄范围和剂次间隔
      └─ 不符合 → result=FAIL, type=IMMUNE_SCHEDULE

STEP 4: 汇总筛查结果
  └─ 任一筛查 FAIL → 最终结果 FAIL，返回首个命中的禁忌症类型
  └─ 全部 PASS → 最终结果 PASS

STEP 5: 组装返回结果
  └─ 返回筛查结果 VO
```

#### 4.8.5 返回结构

```json
{
  "code": 200,
  "message": "筛查完成",
  "data": {
    "appointmentId": 1001,
    "screeningResult": "PASS",
    "contraindicationType": null,
    "riskLevel": null,
    "description": null
  }
}
```

筛查失败返回：

```json
{
  "code": 200,
  "message": "筛查完成",
  "data": {
    "appointmentId": 1001,
    "screeningResult": "FAIL",
    "contraindicationType": "ALLERGY",
    "riskLevel": "HIGH",
    "description": "过敏史中包含该疫苗成分，禁忌接种"
  }
}
```

#### 4.8.6 数据操作（SQL级）

```sql
-- STEP 2: 查询预约信息（只读）
SELECT id, status, child_id
FROM appointment
WHERE id = #{appointmentId};

-- 查询疫苗禁忌信息（只读）
SELECT id, contraindication_info
FROM vaccine
WHERE id = #{vaccineId};

-- 查询儿童出生日期（只读）
SELECT id, birth_date
FROM child_profile
WHERE id = #{childId};
```

> **注意：** 本接口为纯计算接口，不写入任何数据。筛查结果由调用方（F-FLOW-006）决定如何处理。

#### 4.8.7 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 预约不存在 | 1901 | 404 | 返回"预约不存在" |
| 疫苗不存在 | 1004 | 404 | 返回"疫苗不存在" |
| 必填参数缺失 | 1003 | 400 | 返回具体缺失字段 |
| 未登录 | 1001 | 401 | 拦截器拦截 |
| 角色无权限 | 1002 | 403 | 返回"无权执行禁忌筛查" |

#### 4.8.8 并发控制

本接口为只读查询+纯计算，无需并发控制。

#### 4.8.9 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `precheck.contraindication` |
| 数据权限 | 无额外限制 |
| API前缀 | `/api/precheck/*` — 仅 DOCTOR_PRECHECK 角色可访问 |

---

### 4.9 F-FLOW-009 留观状态监控

#### 4.9.1 功能描述

留观医生实时监控指定注射号的留观状态与时长，提供倒计时信息。本接口为**只读查询**。

#### 4.9.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 医生已登录 | Token 有效 |
| PRE-002 | 当前角色为 DOCTOR_OBSERVE | 角色校验 |
| PRE-003 | 接种记录存在 | `vaccination_record.injection_id = #{injectionId}` |

#### 4.9.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| injectionId | String | 是 | 路径参数 | 注射号 |
| doctorId | Long | 是 | Token 解析 | 当前留观医生ID |

#### 4.9.4 处理流程

```
STEP 1: 参数校验
  └─ injectionId 非空
  → 失败返回 1003 BAD_REQUEST

STEP 2: 查询接种记录和留观信息（只读）
  ├─ SELECT vr.injection_id, vr.injection_time, vr.appointment_id,
  │   a.status, cp.name AS child_name
  │   FROM vaccination_record vr
  │   JOIN appointment a ON vr.appointment_id = a.id
  │   JOIN child_profile cp ON a.child_id = cp.id
  │   WHERE vr.injection_id = #{injectionId}
  └─ 记录不存在 → 返回 1004 NOT_FOUND

STEP 3: 计算留观时长
  ├─ observe_duration = TIMESTAMPDIFF(MINUTE, injection_time, NOW())
  ├─ remaining_duration = MAX(0, 30 - observe_duration)
  └─ observe_status = observe_duration >= 30 ? 'CAN_FINISH' : 'OBSERVING'

STEP 4: 组装返回结果
  └─ 返回留观监控 VO
```

#### 4.9.5 返回结构

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "injectionId": "INJ202604100001",
    "childName": "张小明",
    "observeStartTime": "2026-04-10 09:45:00",
    "currentTime": "2026-04-10 10:00:30",
    "observeDuration": 15,
    "remainingDuration": 15,
    "observeStatus": "OBSERVING",
    "standardDuration": 30
  },
  "timestamp": 1712035200000
}
```

#### 4.9.6 数据操作（SQL级）

```sql
SELECT
    vr.injection_id,
    vr.injection_time,
    vr.appointment_id,
    a.status,
    cp.name AS child_name
FROM vaccination_record vr
JOIN appointment a ON vr.appointment_id = a.id
JOIN child_profile cp ON a.child_id = cp.id
WHERE vr.injection_id = #{injectionId};
```

#### 4.9.7 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 接种记录不存在 | 1004 | 404 | 返回"接种记录不存在" |
| 未登录 | 1001 | 401 | 拦截器拦截 |
| 角色无权限 | 1002 | 403 | 返回"无权访问" |

#### 4.9.8 并发控制

本接口为只读查询，无需并发控制。

#### 4.9.9 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `observe.manage` |
| 数据权限 | 无额外限制 |
| API前缀 | `/api/observe/*` — 仅 DOCTOR_OBSERVE 角色可访问 |

---

### 4.10 F-FLOW-010 确认留观结束

> **事务引用：** REQ-GLOBAL §6 T7（留观结束事务）

#### 4.10.1 功能描述

留观医生确认留观结束，将预约状态从"留观中"（10）变更为"已完成"（2），同时写入留观记录。留观结果为 ABNORMAL 时，必须先上报不良反应（F-FLOW-011）。

#### 4.10.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 医生已登录 | Token 有效 |
| PRE-002 | 当前角色为 DOCTOR_OBSERVE | 角色校验 |
| PRE-003 | 接种记录存在 | `vaccination_record.injection_id = #{injectionId}` |
| PRE-004 | 预约状态为"留观中" | `appointment.status = 10` |
| PRE-005 | 留观时长 >= 30分钟 | `NOW() - injection_time >= 30min` |

#### 4.10.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| injectionId | String | 是 | 请求体 | 注射号 |
| doctorId | Long | 是 | Token 解析 | 当前留观医生ID |
| observeResult | String | 是 | 请求体 | 留观结果：NORMAL / ABNORMAL |

#### 4.10.4 处理流程

```
STEP 1: 参数校验
  ├─ injectionId 非空
  └─ observeResult 非空且为 NORMAL/ABNORMAL
  → 失败返回 1003 BAD_REQUEST

STEP 2: 事务开始（T7）

STEP 3: 加行锁查询预约
  ├─ SELECT a.id, a.status FROM appointment a
  │   JOIN vaccination_record vr ON vr.appointment_id = a.id
  │   WHERE vr.injection_id = #{injectionId}
  │   FOR UPDATE
  └─ 记录不存在 → 返回 1004 NOT_FOUND

STEP 4: 状态校验
  └─ status != 10 → 返回 3005 OBSERVE_STATUS_INVALID

STEP 5: 留观时长校验
  ├─ observe_duration = TIMESTAMPDIFF(MINUTE, injection_time, NOW())
  └─ observe_duration < 30 → 返回 3006 OBSERVE_TIME_INSUFFICIENT

STEP 6: 留观结果校验
  ├─ observeResult = ABNORMAL
  └─ 检查是否存在不良反应记录
      ├─ SELECT COUNT(*) FROM adverse_reaction
      │   WHERE appointment_id = #{appointmentId}
      └─ COUNT = 0 → 返回 3010 ADVERSE_NOT_REPORTED

STEP 7: 更新留观记录（由接种模块在 status=10 时创建）
  └─ UPDATE observe_record
      SET end_time = NOW(),
          duration = TIMESTAMPDIFF(MINUTE, start_time, NOW()),
          observe_result = #{observeResult},
          doctor_id = #{doctorId},
          status = 1
      WHERE appointment_id = #{appointmentId}

STEP 8: 更新预约状态
  └─ UPDATE appointment
      SET status = 2, current_window = NULL
      WHERE id = #{appointmentId}

STEP 9: 事务提交

STEP 10: 组装返回结果
  └─ 返回留观结束信息
```

#### 4.10.5 返回结构

```json
{
  "code": 200,
  "message": "留观结束",
  "data": {
    "appointmentId": 1001,
    "status": 2,
    "statusText": "已完成",
    "observeEndTime": "2026-04-10 10:15:00",
    "observeDuration": 30,
    "observeResult": "NORMAL"
  },
  "timestamp": 1712035200000
}
```

#### 4.10.6 数据操作（SQL级）

```sql
-- STEP 3: 加行锁查询预约
SELECT a.id, a.status, vr.injection_time
FROM appointment a
JOIN vaccination_record vr ON vr.appointment_id = a.id
WHERE vr.injection_id = #{injectionId}
FOR UPDATE;

-- STEP 6: 检查不良反应记录
SELECT COUNT(*) FROM adverse_reaction
WHERE appointment_id = #{appointmentId};

-- STEP 7: 更新留观记录（由接种模块在 status=10 时创建初始记录）
UPDATE observe_record
SET end_time = NOW(),
    duration = TIMESTAMPDIFF(MINUTE, start_time, NOW()),
    observe_result = #{observeResult},
    doctor_id = #{doctorId},
    status = 1
WHERE appointment_id = #{appointmentId};

-- STEP 8: 更新预约状态
UPDATE appointment
SET status = 2, current_window = NULL
WHERE id = #{appointmentId};
```

#### 4.10.7 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 接种记录不存在 | 1004 | 404 | 返回"接种记录不存在" |
| 预约状态不允许留观操作 | 3005 | 409 | 返回"该预约状态异常，无法进行留观操作" |
| 留观时间不足 | 3006 | 409 | 返回当前时长和剩余时长 |
| 未上报不良反应 | 3010 | 409 | 返回"留观结果异常时，必须先上报不良反应" |
| 未登录 | 1001 | 401 | 拦截器拦截 |
| 角色无权限 | 1002 | 403 | 返回"无权执行留观操作" |

#### 4.10.8 状态流转

| 操作 | 原状态 | 新状态 | 说明 |
|------|--------|--------|------|
| 确认留观结束 | 10（留观中） | 2（已完成） | 写入留观记录，清空 current_window |

#### 4.10.9 并发控制

| 控制项 | 策略 |
|--------|------|
| 行锁 | `SELECT ... FOR UPDATE` 锁定 appointment 行 |
| 事务范围 | T7：appointment + observe_record（引用 REQ-GLOBAL §6） |
| 隔离级别 | READ_COMMITTED |

#### 4.10.10 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `observe.finish` |
| 数据权限 | 无额外限制（通过注射号操作） |
| API前缀 | `/api/observe/*` — 仅 DOCTOR_OBSERVE 角色可访问 |

---

### 4.11 F-FLOW-011 上报不良反应

#### 4.11.1 功能描述

留观医生上报儿童不良反应记录，记录反应类型、严重程度、处理措施等信息。

#### 4.11.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 医生已登录 | Token 有效 |
| PRE-002 | 当前角色为 DOCTOR_OBSERVE | 角色校验 |
| PRE-003 | 接种记录存在 | `vaccination_record.injection_id = #{injectionId}` |
| PRE-004 | 预约状态为"留观中" | `appointment.status = 10` |

#### 4.11.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| appointmentId | Long | 是 | 请求体 | 预约ID |
| injectionId | String | 是 | 请求体 | 注射号 |
| doctorId | Long | 是 | Token 解析 | 当前留观医生ID |
| reactionType | String | 是 | 请求体 | 反应类型：LOCAL_REACTION / ALLERGIC_REACTION / FEVER / OTHER |
| reactionDesc | String | 是 | 请求体 | 反应描述 |
| severity | String | 是 | 请求体 | 严重程度：MILD / MODERATE / SEVERE |
| occurTime | DateTime | 是 | 请求体 | 发生时间 |
| handleMeasure | String | 否 | 请求体 | 处理措施 |
| handleResult | String | 否 | 请求体 | 处理结果：RECOVERED / IMPROVED / UNCHANGED / REFERRED |

#### 4.11.4 处理流程

```
STEP 1: 参数校验
  ├─ appointmentId 非空且为正整数
  ├─ injectionId 非空
  ├─ reactionType 非空且为合法枚举值
  ├─ reactionDesc 非空
  ├─ severity 非空且为合法枚举值
  └─ occurTime 非空
  → 失败返回 1003 BAD_REQUEST

STEP 2: 校验预约和接种记录
  ├─ SELECT a.id, a.status FROM appointment a
  │   JOIN vaccination_record vr ON vr.appointment_id = a.id
  │   WHERE vr.injection_id = #{injectionId}
  └─ 记录不存在 → 返回 1004 NOT_FOUND

STEP 3: 状态校验
  └─ status != 10 → 返回 3005 OBSERVE_STATUS_INVALID

STEP 4: 写入不良反应记录
  └─ INSERT INTO adverse_reaction (
      appointment_id, injection_id, reaction_type, reaction_desc,
      severity, occur_time, handle_measure, handle_result,
      handle_status, create_time
  ) VALUES (..., 'PENDING', NOW())

STEP 5: 组装返回结果
  └─ 返回不良反应记录信息
```

#### 4.11.5 返回结构

```json
{
  "code": 200,
  "message": "上报成功",
  "data": {
    "adverseReactionId": 8001,
    "appointmentId": 1001,
    "reportTime": "2026-04-10 10:05:00",
    "handleStatus": "PENDING"
  },
  "timestamp": 1712035200000
}
```

#### 4.11.6 数据操作（SQL级）

```sql
-- STEP 2: 校验预约和接种记录
SELECT a.id, a.status
FROM appointment a
JOIN vaccination_record vr ON vr.appointment_id = a.id
WHERE vr.injection_id = #{injectionId};

-- STEP 4: 写入不良反应记录
INSERT INTO adverse_reaction (
    appointment_id, injection_id, reaction_type, reaction_desc,
    severity, occur_time, handle_measure, handle_result,
    handle_status, create_time
) VALUES (
    #{appointmentId}, #{injectionId}, #{reactionType}, #{reactionDesc},
    #{severity}, #{occurTime}, #{handleMeasure}, #{handleResult},
    'PENDING', NOW()
);
```

#### 4.11.7 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 接种记录不存在 | 1004 | 404 | 返回"接种记录不存在" |
| 预约状态不允许操作 | 3005 | 409 | 返回"该预约状态异常" |
| 必填参数缺失 | 1003 | 400 | 返回具体缺失字段 |
| 未登录 | 1001 | 401 | 拦截器拦截 |
| 角色无权限 | 1002 | 403 | 返回"无权上报不良反应" |

#### 4.11.8 状态流转

| 操作 | 原状态 | 新状态 | 说明 |
|------|--------|--------|------|
| 上报不良反应 | appointment.status 不变 | adverse_reaction.handle_status = PENDING | 不良反应记录创建，不影响预约状态 |

> **注意：** 不良反应上报不改变 `appointment.status`，仅创建一条新的 `adverse_reaction` 记录。预约仍处于 status=10（留观中），后续留观结束操作将检查是否已上报不良反应（3010 ADVERSE_NOT_REPORTED）。

#### 4.11.9 并发控制

本接口为单表 INSERT，无需显式加锁。不良反应记录独立创建，不存在并发冲突风险。

#### 4.11.10 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `adverse.report` |
| 数据权限 | 无额外限制 |
| API前缀 | `/api/observe/*` — 仅 DOCTOR_OBSERVE 角色可访问 |

---

### 4.12 F-FLOW-012 处理不良反应

#### 4.12.1 功能描述

留观医生处理已上报的不良反应记录，更新处理措施、处理结果等信息，将处理状态变更为 RESOLVED。

#### 4.12.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 医生已登录 | Token 有效 |
| PRE-002 | 当前角色为 DOCTOR_OBSERVE | 角色校验 |
| PRE-003 | 不良反应记录存在 | `adverse_reaction.id = #{adverseReactionId}` |
| PRE-004 | 不良反应记录为待处理状态 | `handle_status = 'PENDING'` 或 `'PROCESSING'` |

#### 4.12.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| adverseReactionId | Long | 是 | 请求体 | 不良反应记录ID |
| doctorId | Long | 是 | Token 解析 | 当前留观医生ID |
| handleMeasure | String | 是 | 请求体 | 处理措施 |
| handleResult | String | 是 | 请求体 | 处理结果：RECOVERED / IMPROVED / UNCHANGED / REFERRED |
| handleTime | DateTime | 是 | 请求体 | 处理时间 |
| isReferred | Integer | 是 | 请求体 | 是否转诊：0=否，1=是 |
| remark | String | 否 | 请求体 | 备注 |

#### 4.12.4 处理流程

```
STEP 1: 参数校验
  ├─ adverseReactionId 非空且为正整数
  ├─ handleMeasure 非空
  ├─ handleResult 非空且为合法枚举值
  ├─ handleTime 非空
  └─ isReferred 非空且为 0/1
  → 失败返回 1003 BAD_REQUEST

STEP 2: 查询不良反应记录
  ├─ SELECT id, handle_status FROM adverse_reaction
  │   WHERE id = #{adverseReactionId}
  └─ 记录不存在 → 返回 1004 NOT_FOUND

STEP 3: 状态校验
  └─ handle_status = 'RESOLVED' → 返回 1005 CONFLICT（已处理，不可重复处理）

STEP 4: 更新不良反应记录
  └─ UPDATE adverse_reaction
      SET handle_measure = #{handleMeasure},
          handle_result = #{handleResult},
          handle_time = #{handleTime},
          is_referred = #{isReferred},
          handle_status = 'RESOLVED',
          handle_doctor_id = #{doctorId},
          remark = #{remark}
      WHERE id = #{adverseReactionId}

STEP 5: 组装返回结果
  └─ 返回处理结果信息
```

#### 4.12.5 返回结构

```json
{
  "code": 200,
  "message": "处理完成",
  "data": {
    "adverseReactionId": 8001,
    "handleStatus": "RESOLVED",
    "handleResult": "RECOVERED",
    "handleTime": "2026-04-10 10:25:00"
  },
  "timestamp": 1712035200000
}
```

#### 4.12.6 数据操作（SQL级）

```sql
-- STEP 2: 查询不良反应记录
SELECT id, handle_status
FROM adverse_reaction
WHERE id = #{adverseReactionId};

-- STEP 4: 更新不良反应记录
UPDATE adverse_reaction
SET handle_measure = #{handleMeasure},
    handle_result = #{handleResult},
    handle_time = #{handleTime},
    is_referred = #{isReferred},
    handle_status = 'RESOLVED',
    handle_doctor_id = #{doctorId},
    remark = #{remark}
WHERE id = #{adverseReactionId};
```

#### 4.12.7 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 不良反应记录不存在 | 1004 | 404 | 返回"不良反应记录不存在" |
| 已处理不可重复操作 | 1005 | 409 | 返回"该不良反应已处理" |
| 必填参数缺失 | 1003 | 400 | 返回具体缺失字段 |
| 未登录 | 1001 | 401 | 拦截器拦截 |
| 角色无权限 | 1002 | 403 | 返回"无权处理不良反应" |

#### 4.12.8 状态流转

| 操作 | 原状态 | 新状态 | 说明 |
|------|--------|--------|------|
| 处理不良反应 | adverse_reaction.handle_status = PENDING/PROCESSING | RESOLVED | 更新处理措施和结果 |

> **注意：** 不良反应处理不改变 `appointment.status`，仅更新 `adverse_reaction.handle_status`。预约仍处于 status=10（留观中）或已完成 status=2。

#### 4.12.9 并发控制

单行 UPDATE 操作，MySQL 行锁自动保护。不存在高并发场景（同一不良反应记录不会同时被多名医生处理）。

#### 4.12.10 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `adverse.handle` |
| 数据权限 | 无额外限制 |
| API前缀 | `/api/observe/*` — 仅 DOCTOR_OBSERVE 角色可访问 |

---

## 5. 队列计算规则（汇总参考）

> 本节汇总所有队列计算的统计口径，供开发参考。各队列计算 SQL 已嵌入对应功能规格（§4.3 ~ §4.5）。

### 5.1 各窗口队列定义

| 队列 | 目标 status | 日期过滤 | 排序字段 | 特殊关联 |
|------|-------------|----------|----------|----------|
| 今日预约 | `IN (1,6,7,8,9,10)` | `appointment_date = #{date}` | `create_time ASC` | JOIN child_profile |
| 待签到 | `= 1` | `appointment_date = #{date}` | `create_time ASC` | — |
| 待预检 | `= 6` | `appointment_date = CURDATE()` | `signin_time ASC` | — |
| 待登记 | `= 7` | `appointment_date = CURDATE()` | `signin_time ASC` | — |
| 待接种 | `= 8` | `appointment_date = CURDATE()` | `signin_time ASC` | — |
| 留观中 | `= 10` | `appointment_date = CURDATE()` | `injection_time ASC` | JOIN vaccination_record |

### 5.2 筛选条件汇总

| 队列 | 功能编号 | 筛选值 | 附加条件 |
|------|----------|--------|----------|
| 今日预约 | F-FLOW-003 | ALL | 无 |
| 今日预约 | F-FLOW-003 | ARRIVED | `AND status >= 6` |
| 今日预约 | F-FLOW-003 | NOT_ARRIVED | `AND status = 1` |
| 待预检 | F-FLOW-004 | — | `AND status = 6` |
| 留观中 | F-FLOW-005 | — | `AND status = 10` |

---

## 6. 状态非法校验

### 6.1 校验原则

> 本模块对状态变更操作（F-FLOW-008/006/010）进行严格的状态前置校验；对查询/计算操作（F-FLOW-001~005/007/009/011/012）仅做状态提示，不阻断流程。

### 6.2 各接口的状态校验矩阵

| 接口 | 要求的状态范围 | 非法状态处理 |
|------|---------------|-------------|
| F-FLOW-001 获取窗口指引 | 全部状态（1,2,3,4,6,7,8,9,10） | 终态返回对应终态提示，不报错 |
| F-FLOW-002 获取排队信息 | 进行中（1,6,7,8,10） | 非进行中返回提示"当前无需排队" |
| F-FLOW-003 查询今日预约 | 当日预约 | 非当日返回空列表 |
| F-FLOW-004 待预检队列 | `status = 6` | 无数据返回空列表 |
| F-FLOW-005 留观队列 | `status = 10` | 无数据返回空列表 |
| F-FLOW-006 执行预检评估 | `status = 6` | 返回 3003 拒绝操作 |
| F-FLOW-007 执行禁忌筛查 | 无限制 | 不校验预约状态 |
| F-FLOW-008 执行签到 | `status = 1` | 返回 3001 拒绝操作 |
| F-FLOW-009 留观状态监控 | 无限制 | 不校验预约状态 |
| F-FLOW-010 确认留观结束 | `status = 10` | 返回 3005 拒绝操作 |
| F-FLOW-011 上报不良反应 | `status = 10` | 返回 3005 拒绝操作 |
| F-FLOW-012 处理不良反应 | 无限制 | 校验不良反应记录自身状态 |

### 6.3 状态校验伪代码

```java
/**
 * 校验预约状态是否允许签到
 * 本方法为写前校验，失败则抛异常
 */
public void validateForSignin(Appointment appointment) {
    if (appointment.getStatus() != 1) {
        throw new BusinessException(3001, "当前状态不允许签到");
    }
    if (appointment.getAppointmentDate().isAfter(LocalDate.now())) {
        throw new BusinessException(3007, "预约日期未到，无法签到");
    }
}
```

---

## 7. 权限控制汇总

### 7.1 功能权限矩阵

| 功能编号 | 权限编码 | 允许角色 | API路径 | 类型 |
|----------|----------|----------|---------|------|
| F-FLOW-001 | `appointment.view.own` | USER | `GET /api/user/appointment/{id}/guide` | 查询 |
| F-FLOW-002 | `appointment.view.own` | USER | `GET /api/user/appointment/{id}/queue` | 查询 |
| F-FLOW-003 | `appointment.view.today` | DOCTOR_SIGNIN | `GET /api/signin/today` | 查询 |
| F-FLOW-004 | `appointment.view.queue` | DOCTOR_PRECHECK | `GET /api/precheck/queue` | 查询 |
| F-FLOW-005 | `appointment.view.queue` | DOCTOR_OBSERVE | `GET /api/observe/queue` | 查询 |
| F-FLOW-006 | `precheck.assess` | DOCTOR_PRECHECK | `POST /api/precheck/assess` | 写入 |
| F-FLOW-007 | `precheck.contraindication` | DOCTOR_PRECHECK | `POST /api/precheck/contraindication` | 计算 |
| F-FLOW-008 | `appointment.signin` | DOCTOR_SIGNIN | `POST /api/signin/signin` | 写入 |
| F-FLOW-009 | `observe.manage` | DOCTOR_OBSERVE | `GET /api/observe/status/{injectionId}` | 查询 |
| F-FLOW-010 | `observe.finish` | DOCTOR_OBSERVE | `POST /api/observe/finish` | 写入 |
| F-FLOW-011 | `adverse.report` | DOCTOR_OBSERVE | `POST /api/observe/adverse` | 写入 |
| F-FLOW-012 | `adverse.handle` | DOCTOR_OBSERVE | `POST /api/observe/adverse/handle` | 写入 |

### 7.2 数据权限规则

| 角色 | 数据范围 | 实现方式 |
|------|----------|----------|
| USER | 仅自己的预约 | `WHERE user_id = #{currentUserId}` |
| DOCTOR_SIGNIN | 当日预约（全部） | `WHERE appointment_date = #{date}` |
| DOCTOR_PRECHECK | 当日待预检（全部） | `WHERE appointment_date = CURDATE() AND status = 6` |
| DOCTOR_OBSERVE | 当日留观中（全部） | `WHERE appointment_date = CURDATE() AND status = 10` |

### 7.3 模块读写操作汇总

| 接口类型 | 读/写 | 涉及表 | 操作类型 |
|----------|-------|--------|----------|
| 窗口指引 | 只读 | appointment, hospital_window | SELECT |
| 排队信息 | 只读 | appointment, hospital_window | SELECT |
| 今日预约 | 只读 | appointment, child_profile | SELECT |
| 待预检队列 | 只读 | appointment, child_profile | SELECT |
| 留观队列 | 只读 | appointment, child_profile, vaccination_record | SELECT |
| 执行签到 | 写入 | appointment, child_profile, signin_record | SELECT FOR UPDATE + INSERT + UPDATE |
| 执行预检评估 | 写入 | appointment, pre_check_record | SELECT FOR UPDATE + INSERT + UPDATE |
| 执行禁忌筛查 | 只读/计算 | appointment, vaccine, child_profile | SELECT |
| 留观状态监控 | 只读 | vaccination_record, appointment, child_profile | SELECT |
| 确认留观结束 | 写入 | appointment, vaccination_record, observe_record, adverse_reaction | SELECT FOR UPDATE + INSERT + UPDATE |
| 上报不良反应 | 写入 | appointment, vaccination_record, adverse_reaction | SELECT + INSERT |
| 处理不良反应 | 写入 | adverse_reaction | SELECT + UPDATE |

---

## 8. 异常错误码定义

> 本节汇总本模块涉及的所有异常错误码。模块专属错误码使用 **3000-3999** 段（引用 REQ-GLOBAL §4.4），系统级错误码使用 **1000-1999** 段（引用 REQ-GLOBAL §4.2），预约模块错误码使用 **2000-2999** 段（引用 REQ-GLOBAL §4.3）。系统级与模块级错误码的分层处理策略参见 REQ-GLOBAL §4.11 分层错误处理策略。

### 8.1 签到异常（3001-3002, 3007）

| 错误码 | 常量名 | 异常场景 | HTTP | 处理方式 |
|--------|--------|----------|------|----------|
| 3001 | SIGNIN_STATUS_INVALID | 预约状态不允许签到（非 status=1） | 409 | 返回"该预约已签到或状态异常，无法重复签到" |
| 3002 | SIGNIN_IDCARD_MISMATCH | 身份证号不匹配 | 400 | 清除输入，引导医生重新输入验证 |
| 3007 | SIGNIN_DATE_INVALID | 预约日期未到，无法签到 | 409 | 返回"该预约未到预约日期，无法签到" |

### 8.2 预检异常（3003, 3009）

| 错误码 | 常量名 | 异常场景 | HTTP | 处理方式 |
|--------|--------|----------|------|----------|
| 3003 | PRECHECK_STATUS_INVALID | 预约状态不允许预检（非 status=6） | 409 | 返回"该预约状态异常，无法进行预检" |
| 3009 | CONTRAINDICATION_FAILED | 禁忌筛查不通过 | 409 | 显示禁忌症类型和说明 |

### 8.3 留观异常（3005-3006, 3010）

| 错误码 | 常量名 | 异常场景 | HTTP | 处理方式 |
|--------|--------|----------|------|----------|
| 3005 | OBSERVE_STATUS_INVALID | 预约状态不允许留观操作（非 status=10） | 409 | 返回"该预约状态异常，无法进行留观操作" |
| 3006 | OBSERVE_TIME_INSUFFICIENT | 留观时间不足30分钟 | 409 | 显示当前/剩余时长，继续留观 |
| 3010 | ADVERSE_NOT_REPORTED | 留观结果异常但未上报不良反应 | 409 | 引导至不良反应上报页面 |

### 8.4 复用的系统级错误码

| 错误码 | 常量名 | 异常场景 | HTTP | 说明 |
|--------|--------|----------|------|------|
| 1001 | UNAUTHORIZED | 未登录 / Token无效 | 401 | 拦截器拦截 |
| 1002 | FORBIDDEN | 角色权限不足 | 403 | 返回"无权操作" |
| 1003 | BAD_REQUEST | 请求参数错误 | 400 | 返回具体字段错误 |
| 1004 | NOT_FOUND | 资源不存在 | 404 | 预约/接种记录/不良反应记录不存在 |
| 1005 | CONFLICT | 数据冲突 | 409 | 重复操作 |
| 1007 | INTERNAL_ERROR | 系统内部错误 | 500 | 全局异常兜底 |
| 1901 | APPOINT_NOT_FOUND | 预约记录不存在（共享码） | 404 | 跨模块查询时使用 |

### 8.5 错误码注册状态

> 本节使用的全部错误码已在 REQ-GLOBAL §4.2（系统级）、§4.4（流程模块）和 §4.10（共享业务错误码）中注册。无待注册码。

---

## 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| V1.0 | 2026-04-02 | 初始版本，包含 F-FLOW-001~005 查询/规则功能，状态映射和队列计算规则 |
| V1.1 | 2026-04-03 | 重大更新：(1) §1 模块定位从"纯查询层"扩展为"流程执行与规则层"，覆盖签到/预检/留观三类状态写入；(2) §2 功能清单扩展至 12 个功能，统一编号并标注 PRD 追溯；(3) §4 补充 7 个写入功能的完整 9-subsection 规格（F-FLOW-006/007/008/009/010/011/012），升级 3 个查询功能（F-FLOW-003/004/005）为完整规格；(4) §8 错误码从 PRD 层（40xx/50xx）替换为 REQ 层（3000-3999 + 1000-1999），新增 3007/3009/3010 三个待注册码；(5) §7 权限矩阵扩展至全部 12 个功能 |
| V1.2 | 2026-04-03 | REQ 评审修复：(1) §8 错误码 2008 APPOINT_NOT_FOUND 替换为共享码 1901（跨模块借用消除）；(2) §2 功能清单 F-FLOW-007 类型从"写入"修正为"计算"；(3) §4 补充 F-FLOW-006/008/010 三个核心写入功能的独立状态流转子段；(4) F-FLOW-006 STEP 6b 增加禁忌筛查失败时的防御性库存释放逻辑；(5) F-FLOW-010 observe_record 从"留观结束时 INSERT"改为"接种时由 VACCINATE 模块 INSERT（status=0），本功能 UPDATE 为已完成（status=1）" |

---

**文档结束**
