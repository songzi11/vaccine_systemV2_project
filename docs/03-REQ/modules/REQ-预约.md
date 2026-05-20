# 预约模块 — 研发需求文档

**文档编号:** REQ-APPOINTMENT-001
**版本:** V1.2
**状态:** 正式发布
**日期:** 2026-04-03
**上游依赖:** PRD-APPOINTMENT V2.2 / REQ-GLOBAL V1.3

---

## 目录

1. [模块定位与边界](#1-模块定位与边界)
2. [功能清单](#2-功能清单)
3. [F-APPOINTMENT-001 创建预约](#3-f-appointment-001-创建预约)
4. [F-APPOINTMENT-002 查询预约详情](#4-f-appointment-002-查询预约详情)
5. [F-APPOINTMENT-003 查询预约列表](#5-f-appointment-003-查询预约列表)
6. [F-APPOINTMENT-004 取消预约](#6-f-appointment-004-取消预约)
7. [F-APPOINTMENT-005 获取窗口指引](#7-f-appointment-005-获取窗口指引)
8. [F-APPOINTMENT-013 过期处理](#8-f-appointment-013-过期处理)
9. [预约唯一性约束](#9-预约唯一性约束)
10. [爽约次数与冻结逻辑](#10-爽约次数与冻结逻辑)
11. [状态非法校验](#11-状态非法校验)
12. [权限控制汇总](#12-权限控制汇总)

---

## 1. 模块定位与边界

### 1.1 核心定位

APPOINTMENT 是系统主控模块，仅负责：
- **生命周期管理** — 预约记录的创建、查询、取消、过期
- **状态流转控制** — 本模块仅变更 status=1（创建）→ status=3（取消）、status=1→status=4（过期）
- **预约业务** — 时段容量控制、爽约冻结、窗口指引

### 1.2 不包含内容

| 禁止范围 | 归属模块 |
|----------|----------|
| 排队执行逻辑（签到、预检、留观） | FLOW |
| 库存操作（锁定、扣减、释放） | STOCK / REGISTER / VACCINATE |
| 接种执行 | VACCINATE |
| 登记核实与批次分配 | REGISTER |

### 1.3 本模块状态操作范围

```
本模块可写状态：
  创建 → status = 1（已预约）
  取消 → status = 3（已取消）    前置：status = 1
  过期 → status = 4（已过期）    前置：status = 1

本模块只读状态：
  6（已签到）、7（预检通过）、8（已登记）、10（留观中）、2（已完成）、9（预检失败）
```

---

## 2. 功能清单

| 编号 | 功能名称 | API | 角色 | 优先级 |
|------|----------|-----|------|--------|
| F-APPOINTMENT-001 | 创建预约 | `POST /api/user/appointment` | USER | P0 |
| F-APPOINTMENT-002 | 查询预约详情 | `GET /api/user/appointment/{appointmentId}` | USER | P0 |
| F-APPOINTMENT-003 | 查询预约列表 | `GET /api/user/appointment` | USER | P0 |
| F-APPOINTMENT-004 | 取消预约 | `DELETE /api/user/appointment/{appointmentId}` | USER | P1 |
| F-APPOINTMENT-005 | 获取窗口指引 | `GET /api/user/appointment/{appointmentId}/guide` | USER | P0 |
| F-APPOINTMENT-013 | 过期处理 | 内部定时任务 | SYSTEM | P1 |
| F-APPOINTMENT-006 | 获取排队信息 | `GET /api/signin/queue` | USER | P0 |
| F-APPOINTMENT-007 | 查询今日预约 | `GET /api/signin/today` | SIGNIN_DOCTOR | P0 |
| F-APPOINTMENT-008 | 执行签到 | `POST /api/signin/signin` | SIGNIN_DOCTOR | P0 |
| F-APPOINTMENT-009 | 查看待预检队列 | `GET /api/precheck/queue` | PRECHECK_DOCTOR | P0 |
| F-APPOINTMENT-010 | 查看待登记队列 | `GET /api/register/queue` | DOCTOR_REGISTER | P0 |
| F-APPOINTMENT-011 | 查看待接种队列 | `GET /api/vaccinate/queue` | DOCTOR_VACCINATE | P0 |
| F-APPOINTMENT-012 | 查看留观队列 | `GET /api/observe/queue` | OBSERVE_DOCTOR | P0 |

> **跨模块引用说明：** F-006~012 属于 FLOW/REGISTER/VACCINATE 模块的功能（详见 REQ-流程.md §2），本模块不负责其实现。此处列出仅为标注与预约模块的数据关联（如队列查询基于预约记录），实现与详细规格以各归属模块文档为准。

---

## 3. F-APPOINTMENT-001 创建预约

### 3.1 功能描述

用户通过 APP 提交疫苗接种预约申请，系统进行规则校验后生成预约记录，初始状态为 `status=1（已预约）`。

### 3.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 用户已登录且未被冻结 | `sys_user.status = 0 AND (freeze_end_time IS NULL OR freeze_end_time < NOW())` |
| PRE-002 | 儿童档案存在且属于当前用户 | `child_profile.id = ? AND child_profile.parent_id = ?` |
| PRE-003 | 疫苗存在且处于上架状态 | `vaccine.id = ? AND vaccine.status = 1` |
| PRE-004 | 预约日期为未来日期 | `appointment_date > CURDATE()` |
| PRE-005 | 预约时段有效 | `time_slot IN ('AM', 'PM')` |
| PRE-006 | 该时段预约人数未达上限 | `COUNT(status=1) < max_capacity` |
| PRE-007 | 不存在同日同疫苗的未完成预约 | 预约唯一性约束（见第9节） |

### 3.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| userId | Long | 是 | Token 解析 | 当前登录用户ID |
| childId | Long | 是 | 请求体 | 儿童档案ID |
| vaccineId | Long | 是 | 请求体 | 疫苗ID |
| appointmentDate | String | 是 | 请求体 | 预约日期，格式 YYYY-MM-DD |
| timeSlot | String | 是 | 请求体 | 预约时段，枚举 AM/PM |

### 3.4 处理流程

```
STEP 1: 参数格式校验
  ├─ appointmentDate 格式校验（YYYY-MM-DD）
  ├─ timeSlot 枚举校验（AM/PM）
  └─ 字段非空校验
  → 失败返回 1003 BAD_REQUEST

STEP 2: 用户状态校验（PRE-001）
  ├─ SELECT status, freeze_end_time, no_show_count FROM sys_user WHERE id = #{userId}
  ├─ 用户不存在 → 返回 1004 NOT_FOUND
  ├─ status != 0 → 返回 1012 NO_SHOW_FROZEN
  └─ freeze_end_time IS NOT NULL AND freeze_end_time > NOW() → 返回 1012 NO_SHOW_FROZEN

STEP 3: 儿童档案归属校验（PRE-002）
  ├─ SELECT id FROM child_profile WHERE id = #{childId} AND parent_id = #{userId}
  └─ 不存在 → 返回 2001 APPOINT_CHILD_NOT_FOUND

STEP 4: 疫苗上架状态校验（PRE-003）
  ├─ SELECT id, status FROM vaccine WHERE id = #{vaccineId}
  └─ status != 1 → 返回 2003 APPOINT_VACCINE_OFF_SHELF

STEP 5: 预约日期有效性校验（PRE-004）
  └─ appointmentDate <= CURDATE() → 返回 2004 APPOINT_DATE_INVALID

STEP 6: 时段容量校验（PRE-006，需加锁）
  ├─ SELECT COUNT(*) FROM appointment WHERE appointment_date = #{date} AND time_slot = #{slot} AND status IN (1,6,7,8,10) FOR UPDATE
  └─ count >= max_capacity → 返回 2005 APPOINT_SLOT_FULL

STEP 7: 重复预约校验（PRE-007，预约唯一性约束）
  ├─ SELECT COUNT(*) FROM appointment WHERE user_id = #{userId} AND child_id = #{childId} AND vaccine_id = #{vaccineId} AND appointment_date >= CURDATE() AND status IN (1,6,7,8,10)
  └─ count > 0 → 返回 2006 APPOINT_DUPLICATE

STEP 8: 生成预约号
  └─ 格式：APT + YYYYMMDD + 4位序号（当日自增）

STEP 9: 插入预约记录
  └─ INSERT INTO appointment (user_id, child_id, vaccine_id, appointment_no, appointment_date, time_slot, status) VALUES (...)

STEP 10: 返回结果
  └─ 返回预约ID、预约号、预约详情
```

### 3.5 数据操作（SQL级）

```sql
-- 事务开始
BEGIN;

-- STEP 6: 时段容量校验（加锁）
SELECT COUNT(*) AS cnt
FROM appointment
WHERE appointment_date = #{appointmentDate}
  AND time_slot = #{timeSlot}
  AND status IN (1, 6, 7, 8, 10)
FOR UPDATE;

-- STEP 7: 重复预约校验
SELECT COUNT(*) AS cnt
FROM appointment
WHERE user_id = #{userId}
  AND child_id = #{childId}
  AND vaccine_id = #{vaccineId}
  AND appointment_date >= CURDATE()
  AND status IN (1, 6, 7, 8, 10);

-- STEP 9: 插入预约记录
INSERT INTO appointment (
    user_id, child_id, vaccine_id,
    appointment_no, appointment_date, time_slot,
    status, current_window, create_time, update_time
) VALUES (
    #{userId}, #{childId}, #{vaccineId},
    #{appointmentNo}, #{appointmentDate}, #{timeSlot},
    1, NULL, NOW(), NOW()
);

COMMIT;
```

### 3.6 状态流转

| 操作 | 前置状态 | 目标状态 | 说明 |
|------|----------|----------|------|
| 创建预约 | 无 | 1（已预约） | 新记录初始状态 |

> 预约记录生命周期管理参见 REQ-GLOBAL §11。

### 3.7 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 用户未登录 | 1001 | 401 | 拦截器拦截，返回登录提示 |
| 登录已过期 | 1001 | 401 | 清除Token，跳转登录页 |
| 用户已冻结 | 1012 | 403 | 返回解冻时间 `freeze_end_time` |
| 儿童档案不存在 | 2001 | 404 | 返回"儿童档案不存在" |
| 疫苗未上架 | 2003 | 400 | 返回"该疫苗暂未上架" |
| 预约日期无效 | 2004 | 400 | 返回"预约日期必须是未来日期" |
| 时段已满 | 2005 | 409 | 返回各时段剩余名额 |
| 重复预约 | 2006 | 409 | 返回已有预约日期 |
| 参数格式错误 | 1003 | 400 | 返回具体字段错误信息 |
| 系统异常 | 1007 | 500 | 记录日志，返回通用错误提示 |

### 3.8 并发控制

**时段容量并发控制：**

```sql
-- 事务内使用 SELECT FOR UPDATE 对时段已有预约行加锁
-- 防止多个用户同时预约同一时段导致容量超限
SELECT COUNT(*) AS cnt
FROM appointment
WHERE appointment_date = #{appointmentDate}
  AND time_slot = #{timeSlot}
  AND status IN (1, 6, 7, 8, 10)
FOR UPDATE;
```

**重试策略：** 预约创建不重试，直接提示用户重新选择时段。

### 3.9 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token，解析出 userId |
| 功能权限 | `appointment.book` |
| 数据权限 | 自动限定 `parent_id = #{userId}`（通过儿童档案关联） |
| API前缀 | `/api/user/*` — 仅 USER 角色可访问 |

---

## 4. F-APPOINTMENT-002 查询预约详情

### 4.1 功能描述

根据预约ID查询单个预约的完整详细信息，包含预约基本信息、儿童信息、疫苗信息。

### 4.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 用户已登录 | Token 有效 |
| PRE-002 | 预约记录存在 | `appointment.id = #{appointmentId}` |
| PRE-003 | 用户有权查看该预约 | `appointment.user_id = #{userId}` |

### 4.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| appointmentId | Long | 是 | 路径参数 | 预约ID |
| userId | Long | 是 | Token 解析 | 当前登录用户ID |

### 4.4 处理流程

```
STEP 1: 参数校验
  └─ appointmentId 非空且为正整数
  → 失败返回 1003 BAD_REQUEST

STEP 2: 查询预约记录（含数据权限）
  ├─ SELECT a.*, cp.name AS child_name, cp.gender AS child_gender, cp.birth_date AS child_birth_date,
  │        v.name AS vaccine_name, v.category AS vaccine_category, v.manufacturer AS manufacturer
  │   FROM appointment a
  │   LEFT JOIN child_profile cp ON a.child_id = cp.id
  │   LEFT JOIN vaccine v ON a.vaccine_id = v.id
  │   WHERE a.id = #{appointmentId} AND a.user_id = #{userId}
  └─ 记录不存在 → 返回 2008 APPOINT_NOT_FOUND

STEP 3: 组装返回结果
  └─ 映射为 AppointmentDetail VO（字段见下方）

STEP 4: 返回结果
  └─ 返回预约详情对象
```

### 4.5 数据操作（SQL级）

```sql
SELECT
    a.id                AS appointment_id,
    a.appointment_no    AS appointment_no,
    a.appointment_date  AS appointment_date,
    a.time_slot         AS time_slot,
    a.status            AS status,
    a.current_window    AS current_window,
    a.signin_time       AS signin_time,
    a.cancel_time       AS cancel_time,
    a.cancel_reason     AS cancel_reason,
    a.create_time       AS create_time,
    a.update_time       AS update_time,
    a.child_id          AS child_id,
    cp.name             AS child_name,
    cp.gender           AS child_gender,
    cp.birth_date       AS child_birth_date,
    a.vaccine_id        AS vaccine_id,
    v.name              AS vaccine_name,
    v.category          AS vaccine_category,
    v.manufacturer      AS manufacturer
FROM appointment a
LEFT JOIN child_profile cp ON a.child_id = cp.id
LEFT JOIN vaccine v ON a.vaccine_id = v.id
WHERE a.id = #{appointmentId}
  AND a.user_id = #{userId};
```

### 4.6 状态流转

本接口为只读查询，不涉及状态变更。

### 4.7 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 预约不存在 | 2008 | 404 | 返回"预约不存在" |
| 无权查看 | 1002 | 403 | SQL层 WHERE 过滤，查不到数据统一返回 2008 |
| 未登录 | 1001 | 401 | 拦截器拦截 |

### 4.8 并发控制

本接口为只读查询，无需并发控制。

### 4.9 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `appointment.view.own` |
| 数据权限 | SQL WHERE `a.user_id = #{userId}`，确保用户只能查看自己的预约 |
| API前缀 | `/api/user/*` — 仅 USER 角色可访问 |

---

## 5. F-APPOINTMENT-003 查询预约列表

### 5.1 功能描述

查询当前用户的预约记录列表，支持按状态筛选和分页查询，按预约时间倒序排列。

### 5.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 用户已登录 | Token 有效 |

### 5.3 输入参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| userId | Long | 是 | — | Token 解析 |
| status | String | 否 | ALL | 状态筛选：ALL/IN_PROGRESS/COMPLETED/CANCELLED |
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 10 | 每页数量，最大50 |

### 5.4 处理流程

```
STEP 1: 参数校验
  ├─ status 枚举校验（ALL/IN_PROGRESS/COMPLETED/CANCELLED）
  ├─ page >= 1, size ∈ [1, 50]
  → 失败返回 1003 BAD_REQUEST

STEP 2: 构建状态过滤条件
  ├─ ALL         → 不追加 status 条件
  ├─ IN_PROGRESS → AND a.status IN (1, 6, 7, 8, 10)
  ├─ COMPLETED   → AND a.status = 2
  └─ CANCELLED   → AND a.status IN (3, 4, 9)

STEP 3: 查询总数
  └─ SELECT COUNT(*) FROM appointment a WHERE a.user_id = #{userId} [AND status条件]

STEP 4: 查询分页数据
  └─ SELECT a.*, cp.name AS child_name, v.name AS vaccine_name, v.category AS vaccine_category
      FROM appointment a
      LEFT JOIN child_profile cp ON a.child_id = cp.id
      LEFT JOIN vaccine v ON a.vaccine_id = v.id
      WHERE a.user_id = #{userId} [AND status条件]
      ORDER BY a.create_time DESC
      LIMIT #{offset}, #{size}

STEP 5: 组装分页响应
  └─ 返回 records + total + page + size + pages
```

### 5.5 数据操作（SQL级）

```sql
-- 查询总数
SELECT COUNT(*)
FROM appointment
WHERE user_id = #{userId}
  AND status IN (#{statusList});   -- 根据 status 参数动态拼接

-- 查询分页数据
SELECT
    a.id,
    a.appointment_no,
    a.appointment_date,
    a.time_slot,
    a.status,
    a.current_window,
    a.create_time,
    a.update_time,
    a.cancel_time,
    a.cancel_reason,
    a.child_id,
    cp.name         AS child_name,
    a.vaccine_id,
    v.name          AS vaccine_name,
    v.category      AS vaccine_category
FROM appointment a
LEFT JOIN child_profile cp ON a.child_id = cp.id
LEFT JOIN vaccine v ON a.vaccine_id = v.id
WHERE a.user_id = #{userId}
  AND a.status IN (#{statusList})   -- 动态
ORDER BY a.create_time DESC
LIMIT #{offset}, #{size};
```

### 5.6 状态流转

本接口为只读查询，不涉及状态变更。

### 5.7 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 参数格式错误 | 1003 | 400 | 返回具体字段错误 |
| 未登录 | 1001 | 401 | 拦截器拦截 |

### 5.8 并发控制

本接口为只读查询，无需并发控制。

### 5.9 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `appointment.view.own` |
| 数据权限 | SQL WHERE `a.user_id = #{userId}` |
| API前缀 | `/api/user/*` — 仅 USER 角色可访问 |

---

## 6. F-APPOINTMENT-004 取消预约

### 6.1 功能描述

用户主动取消预约，系统将预约状态变更为 `status=3（已取消）`，并根据取消时间判定是否计入爽约。

### 6.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 用户已登录 | Token 有效 |
| PRE-002 | 预约记录存在 | `appointment.id = #{appointmentId}` |
| PRE-003 | 预约归属当前用户 | `appointment.user_id = #{userId}` |
| PRE-004 | 预约状态为已预约 | `appointment.status = 1` |

### 6.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| appointmentId | Long | 是 | 路径参数 | 预约ID |
| userId | Long | 是 | Token 解析 | 当前登录用户ID |
| cancelReason | String | 否 | 请求体 | 取消原因，最长200字 |

### 6.4 处理流程

```
STEP 1: 参数校验
  └─ appointmentId 非空且为正整数
  → 失败返回 1003 BAD_REQUEST

STEP 2: 查询预约记录（加行锁）
  ├─ SELECT id, user_id, status, appointment_date, batch_id FROM appointment WHERE id = #{appointmentId} FOR UPDATE
  └─ 记录不存在 → 返回 2008 APPOINT_NOT_FOUND

STEP 3: 归属校验
  └─ user_id != #{userId} → 返回 1002 FORBIDDEN

STEP 4: 状态校验（状态非法校验）
  └─ status != 1 → 返回 2007 APPOINT_CANCEL_FORBIDDEN
     （状态非法校验详细规则见第11节）

STEP 5: 执行取消
  └─ UPDATE appointment SET status = 3, cancel_time = NOW(), cancel_reason = #{cancelReason}, update_time = NOW() WHERE id = #{appointmentId}

STEP 6: 爽约判定
  ├─ IF NOW() >= #{appointmentDate} THEN
  │     UPDATE sys_user SET no_show_count = no_show_count + 1 WHERE id = #{userId}
  │     → 查询更新后的 no_show_count
  │     IF no_show_count >= 3 AND (freeze_end_time IS NULL OR freeze_end_time <= NOW()) THEN
  │       UPDATE sys_user SET freeze_start_time = NOW(), freeze_end_time = DATE_ADD(NOW(), INTERVAL 7 DAY) WHERE id = #{userId}
  │     END IF
  └─ ELSE
        → 正常取消，不计入爽约

STEP 7: 返回结果
  └─ 返回取消确认信息（含爽约状态、剩余爽约次数）
```

### 6.5 数据操作（SQL级）

```sql
-- 事务开始
BEGIN;

-- STEP 2: 查询并加锁
SELECT id, user_id, status, appointment_date, batch_id
FROM appointment
WHERE id = #{appointmentId}
FOR UPDATE;

-- STEP 5: 更新预约状态
UPDATE appointment
SET status = 3,
    cancel_time = NOW(),
    cancel_reason = #{cancelReason},
    update_time = NOW()
WHERE id = #{appointmentId};

-- STEP 6: 爽约判定（仅在取消时间 >= 预约日期时执行）
-- 6a: 爽约次数 +1
UPDATE sys_user
SET no_show_count = no_show_count + 1
WHERE id = #{userId};

-- 6b: 冻结判定（no_show_count >= 3 且当前未冻结时执行）
UPDATE sys_user
SET freeze_start_time = NOW(),
    freeze_end_time = DATE_ADD(NOW(), INTERVAL 7 DAY)
WHERE id = #{userId}
  AND no_show_count >= 3
  AND (freeze_end_time IS NULL OR freeze_end_time <= NOW());

COMMIT;
```

### 6.6 状态流转

| 操作 | 前置状态 | 目标状态 | 触发角色 |
|------|----------|----------|----------|
| 取消预约 | 1（已预约） | 3（已取消） | USER |

### 6.7 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 预约不存在 | 2008 | 404 | 返回"预约不存在" |
| 无权操作 | 1002 | 403 | 返回"无权操作该预约" |
| 预约已取消 | 1004 | 410 | 显示取消原因，引导重新预约 |
| 预约已完成 | 1005 | 409 | 显示完成时间，引导查看接种记录 |
| 状态不允许取消 | 2007 | 409 | 返回"当前状态不允许取消" |
| 未登录 | 1001 | 401 | 拦截器拦截 |
| 系统异常 | 1007 | 500 | 事务回滚，记录日志 |

### 6.8 并发控制

```sql
-- SELECT FOR UPDATE 加行锁
-- 防止用户和过期任务同时操作同一条预约
SELECT id, user_id, status, appointment_date
FROM appointment
WHERE id = #{appointmentId}
FOR UPDATE;
```

**爽约计数并发控制：** `no_show_count` 更新使用行级锁（`SELECT FOR UPDATE` 锁定 sys_user 行），防止并发取消导致计数不准确。

### 6.9 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `appointment.cancel.own` |
| 数据权限 | SQL WHERE `user_id = #{userId}` 归属校验 |
| API前缀 | `/api/user/*` — 仅 USER 角色可访问 |

---

## 7. F-APPOINTMENT-005 获取窗口指引

### 7.1 功能描述

根据预约当前状态，返回下一步窗口指引信息，包括窗口编码、窗口名称、指引消息。本接口为纯查询接口，不修改任何数据。

### 7.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 用户已登录 | Token 有效 |
| PRE-002 | 预约记录存在 | `appointment.id = #{appointmentId}` |
| PRE-003 | 预约归属当前用户 | `appointment.user_id = #{userId}` |

### 7.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| appointmentId | Long | 是 | 路径参数 | 预约ID |
| userId | Long | 是 | Token 解析 | 当前登录用户ID |

### 7.4 处理流程

```
STEP 1: 参数校验
  └─ appointmentId 非空且为正整数
  → 失败返回 1003 BAD_REQUEST

STEP 2: 查询预约当前状态
  ├─ SELECT id, status, user_id FROM appointment WHERE id = #{appointmentId}
  └─ 记录不存在 → 返回 2008 APPOINT_NOT_FOUND

STEP 3: 归属校验
  └─ user_id != #{userId} → 返回 1002 FORBIDDEN

STEP 4: 匹配指引规则
  ├─ status = 1  → 指引到签到窗口
  ├─ status = 6  → 指引到预检窗口
  ├─ status = 7  → 指引到登记窗口
  ├─ status = 8  → 指引到接种窗口
  ├─ status = 10 → 指引到留观室
  ├─ status = 2  → 流程已完成
  ├─ status = 3  → 已取消
  ├─ status = 4  → 已过期
  └─ status = 9  → 预检未通过

STEP 5: 查询窗口信息（仅进行中状态）
  ├─ SELECT window_code, window_name, window_function_type FROM hospital_window
  │   WHERE window_function_type = #{nextWindowType} AND status = 1
  └─ 终态不需要查询窗口信息

STEP 6: 组装返回结果
  └─ 返回 WindowGuide VO
```

### 7.5 数据操作（SQL级）

```sql
-- STEP 2: 查询预约状态
SELECT id, status, user_id
FROM appointment
WHERE id = #{appointmentId};

-- STEP 5: 查询窗口信息（进行中状态时调用）
SELECT window_code, window_name, window_function_type
FROM hospital_window
WHERE window_function_type = #{nextWindowType}
  AND status = 1
LIMIT 1;
```

### 7.6 状态流转

本接口为只读查询，不涉及状态变更。

### 7.7 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 预约不存在 | 2008 | 404 | 返回"预约不存在" |
| 无权查看 | 1002 | 403 | 返回"无权操作该预约" |
| 未登录 | 1001 | 401 | 拦截器拦截 |

### 7.8 并发控制

本接口为只读查询，无需并发控制。

### 7.9 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `appointment.view.own` |
| 数据权限 | SQL WHERE `user_id = #{userId}` |
| API前缀 | `/api/user/*` — 仅 USER 角色可访问 |

---

## 8. F-APPOINTMENT-013 过期处理

### 8.1 功能描述

定时扫描所有预约日期已过且状态仍为 `status=1（已预约）` 的预约记录，批量将状态更新为 `status=4（已过期）`。过期计入爽约次数。

> 状态机定义参见 REQ-GLOBAL §2

### 8.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 定时任务触发或管理端手动触发 | 系统调度 / `POST /api/admin/appointment/expire` |

### 8.3 输入参数

本功能由系统内部触发，无外部输入参数。

管理端手动触发接口：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| operatorId | Long | 是 | 操作人ID（Token解析） |

### 8.4 处理流程

```
STEP 1: 扫描过期预约
  └─ SELECT id, appointment_no, user_id, appointment_date FROM appointment
      WHERE appointment_date < CURDATE() AND status = 1
      ORDER BY appointment_date ASC

STEP 2: 批量更新状态
  └─ UPDATE appointment SET status = 4, update_time = NOW()
      WHERE appointment_date < CURDATE() AND status = 1

STEP 3: 爽约计数（按用户维度累加）
  └─ 对 STEP 1 扫描到的每条过期预约，按 user_id 分组后逐条累加（每条过期预约计一次爽约）：
        UPDATE sys_user SET no_show_count = no_show_count + 1
        WHERE id = #{userId}

STEP 4: 冻结判定
  └─ 对每个受影响用户检查：
        IF no_show_count >= 3 AND (freeze_end_time IS NULL OR freeze_end_time <= NOW()) THEN
          UPDATE sys_user SET freeze_start_time = NOW(), freeze_end_time = DATE_ADD(NOW(), INTERVAL 7 DAY) WHERE id = #{userId}
        END IF

STEP 5: 记录过期日志
  └─ 对每条过期预约记录操作日志（appointment_no, 原状态1, 目标状态4, 操作时间）

STEP 6: 返回处理结果
  └─ 返回本次处理的记录数
```

### 8.5 数据操作（SQL级）

```sql
-- STEP 1+2: 批量更新过期预约
UPDATE appointment
SET status = 4,
    update_time = NOW()
WHERE appointment_date < CURDATE()
  AND status = 1;

-- 查询本次影响行数（用于日志记录）
SELECT ROW_COUNT() AS affected_rows;

-- STEP 3: 爽约计数（每个用户每条过期预约计一次爽约）
-- 注意：需在应用层按 user_id 分组后，逐条过期预约执行以下 SQL
UPDATE sys_user
SET no_show_count = no_show_count + 1
WHERE id = #{userId};

-- STEP 4: 冻结判定
UPDATE sys_user
SET freeze_start_time = NOW(),
    freeze_end_time = DATE_ADD(NOW(), INTERVAL 7 DAY)
WHERE id = #{userId}
  AND no_show_count >= 3
  AND (freeze_end_time IS NULL OR freeze_end_time <= NOW());
```

### 8.6 状态流转

| 操作 | 前置状态 | 目标状态 | 触发角色 |
|------|----------|----------|----------|
| 过期处理 | 1（已预约） | 4（已过期） | SYSTEM |

### 8.7 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 数据库异常 | 1007 | 500 | 记录日志，下次定时任务重试 |
| 手动触发无权限 | 1002 | 403 | 仅 SUPER_ADMIN 可手动触发 |

### 8.8 并发控制

```sql
-- 批量 UPDATE 使用 WHERE status = 1 条件作为乐观锁
-- 如果取消预约和过期处理并发：
--   取消已将 status 改为 3，过期 UPDATE 的 WHERE status = 1 不会命中
--   过期已将 status 改为 4，取消的 FOR UPDATE 读到 status=4 后状态校验拒绝
-- 两者互不冲突
```

### 8.9 权限控制

| 控制项 | 规则 |
|--------|------|
| 定时任务 | 内部系统身份，无需Token |
| 手动触发 | `SUPER_ADMIN` 角色，API前缀 `/api/admin/*` |

### 8.10 调度配置

| 配置项 | 值 | 说明 |
|--------|-----|------|
| cron 表达式 | `0 30 0 * * ?` | 每日凌晨 00:30 执行 |
| 事务隔离 | READ_COMMITTED | MySQL默认 |
| 批次大小 | 一次全量 UPDATE | 数据量不大，无需分批 |

---

## 9. 预约唯一性约束

### 9.1 约束定义

> **同一用户 + 同一儿童 + 同一疫苗，在当前日期及之后，只允许存在一条进行中的预约。**

进行中状态：`status IN (1, 6, 7, 8, 10)`

### 9.2 约束目的

防止用户为同一儿童重复预约同一疫苗，导致资源浪费。

### 9.3 校验SQL

```sql
SELECT COUNT(*) AS cnt
FROM appointment
WHERE user_id = #{userId}
  AND child_id = #{childId}
  AND vaccine_id = #{vaccineId}
  AND appointment_date >= CURDATE()
  AND status IN (1, 6, 7, 8, 10);
```

### 9.4 校验时机

- 创建预约（F-APPOINTMENT-001）的 STEP 7
- 在事务内、INSERT 之前执行

### 9.5 违反约束时的处理

| 错误码 | HTTP | 错误消息 |
|--------|------|----------|
| 2006 | 409 | `您已有该疫苗的未完成预约，预约日期为 {appointment_date}，请完成后再预约` |

### 9.6 数据库层面约束（可选）

```sql
-- 建议在 appointment 表上建立联合索引以加速唯一性校验
CREATE INDEX idx_appoint_unique_check
ON appointment (user_id, child_id, vaccine_id, appointment_date, status);
```

> 注意：由于 status 是多值条件（IN 1,6,7,8,10），数据库唯一索引无法直接表达此约束，需在业务层保证。

---

## 10. 爽约次数与冻结逻辑

> 冻结触发条件参见 REQ-GLOBAL §10.3

### 10.1 爽约定义

| 场景 | 是否爽约 | 说明 |
|------|----------|------|
| 预约日期之前取消 | 否 | `cancel_time < appointment_date` |
| 预约当天或之后取消 | **是** | `cancel_time >= appointment_date` |
| 系统过期（未签到） | **是** | 预约过期计入爽约次数，与预约当天取消同等处理 |

### 10.2 爽约计数规则

```sql
-- 爽约时执行
UPDATE sys_user
SET no_show_count = no_show_count + 1
WHERE id = #{userId};
```

| 规则 | 说明 |
|------|------|
| 计数方式 | 累加，不重置 |
| 重置条件 | 无（`no_show_count` 一旦增加永不清零） |
| 冻结解除后 | `no_show_count` 保持不变，继续累计 |

### 10.3 冻结触发与解除

#### 冻结触发条件

`no_show_count >= 3`

#### 冻结执行SQL

```sql
UPDATE sys_user
SET freeze_start_time = NOW(),
    freeze_end_time = DATE_ADD(NOW(), INTERVAL 7 DAY)
WHERE id = #{userId}
  AND no_show_count >= 3
  AND (freeze_end_time IS NULL OR freeze_end_time <= NOW());
```

#### 冻结参数

| 参数 | 值 | 说明 |
|------|-----|------|
| 冻结时长 | 7天 | 从触发时刻起算 |
| 冻结字段 | `freeze_start_time`, `freeze_end_time` | 记录在 `sys_user` 表 |
| 冻结范围 | 仅禁止创建新预约 | 不影响已有预约的查看和取消 |

#### 冻结校验（创建预约时）

```sql
-- 在 F-APPOINTMENT-001 的 STEP 2 中执行
SELECT status, freeze_end_time, no_show_count
FROM sys_user
WHERE id = #{userId};

-- 校验逻辑
IF freeze_end_time IS NOT NULL AND freeze_end_time > NOW() THEN
    -- 用户处于冻结期，拒绝创建预约
    RETURN 1012 NO_SHOW_FROZEN
END IF;
```

#### 解冻机制

| 方式 | 说明 |
|------|------|
| 自动解冻 | 冻结到期后，下次请求时 `freeze_end_time < NOW()` 自然通过校验 |
| 主动清空 | 不需要定时任务清空，通过条件判断自动解冻 |

### 10.4 爽约完整流程图

```
用户取消预约 / 系统过期
    │
    ▼
取消: cancel_time >= appointment_date ?   过期: 系统自动处理
    │                                      │
    ├─ 否 → 正常取消，不计爽约              └─ 是（过期即爽约）
    │
    └─ 是 → no_show_count += 1
              │
              ▼
         no_show_count >= 3 ?
              │
              ├─ 否 → 处理完成
              │
              └─ 是 → 设置冻结7天
                        │
                        ▼
                   冻结到期？
                        │
                        ├─ 否 → 冻结中，禁止创建预约（返回 1012）
                        │
                        └─ 是 → 自动解冻，可正常预约
```

---

## 11. 状态非法校验

### 11.1 校验原则

> **任何状态变更操作，执行前必须先锁定预约行（`SELECT FOR UPDATE`），校验当前状态是否在允许的前置状态列表中。非法状态一律拒绝操作。**

### 11.2 本模块操作的状态校验矩阵

| 操作 | 允许的前置状态 | 非法状态 | 拒绝错误码 |
|------|---------------|----------|-----------|
| 创建预约 | 无（新记录） | — | — |
| 取消预约 | 1（已预约） | 2,3,4,6,7,8,9,10 | 2007 APPOINT_CANCEL_FORBIDDEN |
| 过期处理 | 1（已预约） | 2,3,4,6,7,8,9,10 | 自动跳过（WHERE status=1） |

### 11.3 状态校验伪代码

```java
/**
 * 取消预约 — 状态校验
 */
public void validateCancel(Long appointmentId, Long userId) {
    // 1. 加行锁查询
    Appointment appt = appointmentMapper.selectForUpdate(appointmentId);
    if (appt == null) {
        throw new BusinessException(2008, "预约不存在");
    }

    // 2. 归属校验
    if (!appt.getUserId().equals(userId)) {
        throw new BusinessException(1002, "无权操作该预约");
    }

    // 3. 状态校验
    if (appt.getStatus() != 1) {
        throw new BusinessException(2007, "当前状态不允许取消");
    }
}
```

### 11.4 非法状态流转的错误消息

| 当前状态 | 尝试操作 | 错误码 | 错误消息 |
|----------|----------|--------|----------|
| 2（已完成） | 取消 | 2007 | 该预约已完成，无法取消 |
| 3（已取消） | 取消 | 2007 | 该预约已取消 |
| 4（已过期） | 取消 | 2007 | 该预约已过期，无法取消 |
| 6（已签到） | 取消 | 2007 | 该预约已签到，无法取消 |
| 7（预检通过） | 取消 | 2007 | 该预约已在预检流程中，无法取消 |
| 8（已登记） | 取消 | 2007 | 该预约已登记，无法取消 |
| 9（预检失败） | 取消 | 2007 | 该预约预检未通过，无法取消 |
| 10（留观中） | 取消 | 2007 | 该预约正在留观中，无法取消 |

---

## 12. 权限控制汇总

### 12.1 功能权限矩阵

| 功能编号 | 权限编码 | 允许角色 | API路径 |
|----------|----------|----------|---------|
| F-APPOINTMENT-001 | `appointment.book` | USER | `POST /api/user/appointment` |
| F-APPOINTMENT-002 | `appointment.view.own` | USER | `GET /api/user/appointment/{id}` |
| F-APPOINTMENT-003 | `appointment.view.own` | USER | `GET /api/user/appointment` |
| F-APPOINTMENT-004 | `appointment.cancel.own` | USER | `DELETE /api/user/appointment/{id}` |
| F-APPOINTMENT-005 | `appointment.view.own` | USER | `GET /api/user/appointment/{id}/guide` |
| F-APPOINTMENT-013 | 系统内部 | SYSTEM / SUPER_ADMIN | 内部定时任务 / `POST /api/admin/appointment/expire` |

### 12.2 数据权限规则

| 角色 | 数据范围 | 实现方式 |
|------|----------|----------|
| USER | 仅自己的预约 | `WHERE user_id = #{currentUserId}` |
| SUPER_ADMIN | 全部（仅过期处理） | 无限制 |

### 12.3 权限校验流程

```
请求到达
  │
  ▼
解析 Token → 获取 userId
  │
  ├─ Token 无效 → 返回 { code: 1001, message: "未登录或登录已过期" }
  │
  ▼
查询 sys_user_role → 获取 roleIds
  │
  ▼
查询 sys_role_permission → 获取 permissionCodes
  │
  ├─ 权限不足 → 返回 { code: 1002, message: "无权限访问" }
  │
  ▼
进入 Controller → Service 层业务逻辑
  │
  ▼
数据权限校验（Service 层 SQL WHERE 条件）
  │
  ├─ 数据越权 → 返回 { code: 2008, message: "预约不存在" }
  │
  ▼
执行业务操作
  │
  ▼
返回响应
```

---

## 13. 错误码定义

### 13.1 本模块错误码段（2000-2099）

| 错误码 | 常量名 | 说明 |
|--------|--------|------|
| 2001 | APPOINT_CHILD_NOT_FOUND | 儿童档案不存在 |
| 2003 | APPOINT_VACCINE_OFF_SHELF | 疫苗未上架 |
| 2004 | APPOINT_DATE_INVALID | 预约日期无效 |
| 2005 | APPOINT_SLOT_FULL | 时段已满 |
| 2006 | APPOINT_DUPLICATE | 重复预约 |
| 2007 | APPOINT_CANCEL_FORBIDDEN | 当前状态不允许取消 |
| 2008 | APPOINT_NOT_FOUND | 预约不存在 |

### 13.2 共享错误码段（1900-1999）

本模块引用的跨模块共享错误码，定义于全局共享段：

| 错误码 | 常量名 | 说明 | 原属模块 |
|--------|--------|------|----------|
| 1900-1999 | — | 共享错误码段（跨模块复用） | 全局共享 |

> 共享错误码段 1900-1999 用于跨模块复用的通用错误场景，避免模块间错误码段借用。详见 REQ-GLOBAL 错误码总表。

---

## 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| V1.0 | 2026-04-02 | 初始版本，基于 PRD-APPOINTMENT V1.1 / REQ-GLOBAL V1.0 生成 |
| V1.1 | 2026-04-03 | 修复过期爽约矛盾：系统过期改为计入爽约次数，对齐 PRD-APPOINTMENT V1.1 |
| V1.2 | 2026-04-03 | 评审修复：(1) §3.7 错误码 4010 替换为 1001（消除跨段借用）；(2) §8.4 爽约计数措辞修正（消除"去重"歧义）；(3) §2 功能清单 F-006~012 添加跨模块引用说明（消除与 §1.2 排除范围的矛盾） |

---

**文档结束**
