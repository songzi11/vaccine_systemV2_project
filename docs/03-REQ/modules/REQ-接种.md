# 接种模块 — 研发需求文档

**文档编号:** REQ-VACCINATE-001
**版本:** V1.2
**状态:** 正式发布
**日期:** 2026-04-03
**上游依赖:** PRD-VACCINATE V2.2 / REQ-GLOBAL V1.3

---

## 目录

1. [模块定位与边界](#1-模块定位与边界)
2. [功能清单](#2-功能清单)
3. [F-VACCINATE-001 查看待接种队列](#3-f-vaccinate-001-查看待接种队列)
4. [F-VACCINATE-002 查询接种详情](#4-f-vaccinate-002-查询接种详情)
5. [F-VACCINATE-003 核实信息](#5-f-vaccinate-003-核实信息)
6. [F-VACCINATE-005 执行接种](#6-f-vaccinate-005-执行接种)
7. [F-VACCINATE-009 查询接种记录](#7-f-vaccinate-009-查询接种记录)
8. [F-VACCINATE-010 查询儿童接种记录](#8-f-vaccinate-010-查询儿童接种记录)
9. [F-VACCINATE-011 查询接种统计](#9-f-vaccinate-011-查询接种统计)
10. [injection_id 生成规则](#10-injection_id-生成规则)
11. [防重复接种机制](#11-防重复接种机制)
12. [状态非法校验](#12-状态非法校验)
13. [权限控制汇总](#13-权限控制汇总)

---

## 1. 模块定位与边界

### 1.1 核心定位

VACCINATE 是疫苗接种流程的核心执行模块，仅负责：
- **接种执行** — 接种医生执行疫苗接种操作
- **库存扣减** — 接种时扣减批次库存（available_stock -1, locked_stock -1）
- **接种记录** — 生成注射号、写入接种记录
- **状态推进** — 将预约状态从 8（已登记）推进到 10（留观中）

### 1.2 不包含内容

| 禁止范围 | 归属模块 |
|----------|----------|
| 签到执行 | FLOW-SIGNIN |
| 预检评估 | FLOW-PRECHECK |
| 登记核实与批次锁定 | REGISTER |
| 留观管理与留观结束 | FLOW-OBSERVE |
| 疫苗目录管理 | ADMIN |
| 儿童档案管理 | USER |
| 库存调拨/销毁 | STOCK |

### 1.3 本模块状态操作范围

```
本模块可写状态：
  接种执行 → status = 10（留观中）   前置：status = 8

本模块只读状态：
  8（已登记）— 查询待接种队列/详情时读取
  10（留观中）— 接种记录关联查询时读取
```

---

## 2. 功能清单

| 编号 | 功能名称 | API | 角色 | 优先级 |
|------|----------|-----|------|--------|
| F-VACCINATE-001 | 查看待接种队列 | `GET /api/vaccinate/queue` | DOCTOR_VACCINATE | P0 |
| F-VACCINATE-002 | 查询接种详情 | `GET /api/vaccinate/detail/{appointmentId}` | DOCTOR_VACCINATE | P0 |
| F-VACCINATE-003 | 核实信息 | `POST /api/vaccinate/verify` | DOCTOR_VACCINATE | P0 |
| F-VACCINATE-005 | 执行接种 | `POST /api/vaccinate/execute` | DOCTOR_VACCINATE | P0 |
| F-VACCINATE-009 | 查询接种记录 | `GET /api/vaccinate/record/list` | DOCTOR_VACCINATE | P0 |
| F-VACCINATE-010 | 查询儿童接种记录 | `GET /api/vaccinate/record/child/{childId}` | DOCTOR_VACCINATE | P0 |
| F-VACCINATE-011 | 查询接种统计 | `GET /api/vaccinate/statistics` | DOCTOR_BUSINESS_ADMIN | P2 |
| F-VACCINATE-004 | 选择接种部位 | 前端操作 | DOCTOR_VACCINATE | P0 |
| F-VACCINATE-006 | 生成注射号 | `GET /api/vaccinate/injection/generate` | DOCTOR_VACCINATE | P0 |
| F-VACCINATE-007 | 扣减库存 | 内部事务 | DOCTOR_VACCINATE | P0 |
| F-VACCINATE-008 | 记录接种信息 | `POST /api/vaccinate/record` | DOCTOR_VACCINATE | P0 |

> **说明：** F-VACCINATE-004（选择接种部位）为纯前端操作，无需后端 API。F-VACCINATE-006（生成注射号）、F-VACCINATE-007（扣减库存）、F-VACCINATE-008（记录接种信息）作为 F-VACCINATE-005 执行接种事务内的子步骤实现，不暴露独立 API，详见第6节。

---

## 3. F-VACCINATE-001 查看待接种队列

### 3.1 功能描述

接种医生查询已登记（status=8）但尚未接种的预约列表，按登记时间升序排列。

### 3.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 医生已登录且角色为 DOCTOR_VACCINATE | Token 有效 + 角色校验 |

### 3.3 输入参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| doctorId | Long | 是 | — | Token 解析 |
| date | String | 否 | CURDATE() | 查询日期，格式 YYYY-MM-DD |

### 3.4 处理流程

```
STEP 1: 参数校验
  ├─ date 格式校验（YYYY-MM-DD），默认当天
  → 失败返回 1003 BAD_REQUEST

STEP 2: 查询待接种队列
  └─ SELECT ... FROM appointment
      JOIN register_queue JOIN child_profile JOIN vaccine_batch
      WHERE appointment_date = #{date} AND status = 8
      ORDER BY register_time ASC

STEP 3: 组装返回结果
  └─ 映射为 VaccinateQueueItem VO 列表
```

### 3.5 数据操作（SQL级）

```sql
SELECT
    a.id                AS appointment_id,
    rq.queue_no         AS queue_no,
    cp.name             AS child_name,
    cp.gender           AS child_gender,
    cp.birth_date       AS child_birth_date,
    rq.register_time    AS register_time,
    rq.batch_no         AS batch_no,
    vb.expiry_date      AS expiry_date,
    a.status            AS status
FROM appointment a
JOIN register_queue rq ON a.id = rq.appointment_id
JOIN child_profile cp ON a.child_id = cp.id
JOIN vaccine_batch vb ON rq.batch_id = vb.id
WHERE a.appointment_date = #{date}
  AND a.status = 8
ORDER BY rq.register_time ASC;
```

### 3.6 状态流转

本接口为只读查询，不涉及状态变更。

### 3.7 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 参数格式错误 | 1003 | 400 | 返回具体字段错误信息 |
| 未登录 | 1001 | 401 | 拦截器拦截 |
| 权限不足 | 1002 | 403 | 非接种医生角色拒绝访问 |

### 3.8 并发控制

本接口为只读查询，无需并发控制。

### 3.9 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `appointment.view.queue` |
| API前缀 | `/api/vaccinate/*` — 仅 DOCTOR_VACCINATE 角色可访问 |

---

## 4. F-VACCINATE-002 查询接种详情

### 4.1 功能描述

根据预约ID查询预约的接种详情，包含预约信息、儿童信息、疫苗信息、批次信息、登记信息。

### 4.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 医生已登录 | Token 有效 |
| PRE-002 | 预约记录存在 | `appointment.id = #{appointmentId}` |

### 4.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| appointmentId | Long | 是 | 路径参数 | 预约ID |
| doctorId | Long | 是 | Token 解析 | 当前医生ID |

### 4.4 处理流程

```
STEP 1: 参数校验
  └─ appointmentId 非空且为正整数
  → 失败返回 1003 BAD_REQUEST

STEP 2: 查询预约详情（含关联信息）
  ├─ SELECT a.*, cp.*, v.*, vb.*, rq.*
  │   FROM appointment a
  │   JOIN child_profile cp ON a.child_id = cp.id
  │   JOIN vaccine_batch vb ON a.batch_id = vb.id
  │   JOIN vaccine v ON vb.vaccine_id = v.id
  │   JOIN register_queue rq ON a.id = rq.appointment_id
  │   WHERE a.id = #{appointmentId}
  └─ 记录不存在 → 返回 1004 NOT_FOUND

STEP 3: 组装返回结果
  └─ 映射为 VaccinateDetail VO
```

### 4.5 数据操作（SQL级）

```sql
SELECT
    a.id                AS appointment_id,
    a.appointment_no    AS appointment_no,
    a.appointment_date  AS appointment_date,
    a.time_slot         AS time_slot,
    a.status            AS status,
    a.batch_id          AS batch_id,
    a.child_id          AS child_id,
    cp.name             AS child_name,
    cp.gender           AS child_gender,
    cp.birth_date       AS child_birth_date,
    v.name              AS vaccine_name,
    v.category          AS vaccine_category,
    v.manufacturer      AS manufacturer,
    vb.batch_no         AS batch_no,
    vb.production_date  AS production_date,
    vb.expiry_date      AS expiry_date,
    rq.queue_no         AS queue_no,
    rq.register_time    AS register_time
FROM appointment a
JOIN child_profile cp ON a.child_id = cp.id
JOIN vaccine_batch vb ON a.batch_id = vb.id
JOIN vaccine v ON vb.vaccine_id = v.id
JOIN register_queue rq ON a.id = rq.appointment_id
WHERE a.id = #{appointmentId};
```

### 4.6 状态流转

本接口为只读查询，不涉及状态变更。

### 4.7 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 预约不存在 | 1004 | 404 | 返回"预约不存在" |
| 参数格式错误 | 1003 | 400 | 返回具体字段错误 |
| 未登录 | 1001 | 401 | 拦截器拦截 |
| 权限不足 | 1002 | 403 | 非接种医生角色拒绝访问 |

### 4.8 并发控制

本接口为只读查询，无需并发控制。

### 4.9 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `vaccinate.view` |
| API前缀 | `/api/vaccinate/*` — 仅 DOCTOR_VACCINATE 角色可访问 |

---

## 5. F-VACCINATE-003 核实信息

### 5.1 功能描述

接种医生核实儿童信息、预约信息和批次信息，确保信息准确无误后再执行接种。本接口为只读校验接口，不修改任何数据。

### 5.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 医生已登录 | Token 有效 |
| PRE-002 | 预约记录存在 | `appointment.id = #{appointmentId}` |
| PRE-003 | 预约状态为已登记 | `appointment.status = 8` |

### 5.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| appointmentId | Long | 是 | 请求体 | 预约ID |
| doctorId | Long | 是 | Token 解析 | 当前医生ID |

### 5.4 处理流程

```
STEP 1: 参数校验
  └─ appointmentId 非空且为正整数
  → 失败返回 1003 BAD_REQUEST

STEP 2: 查询预约记录
  ├─ SELECT id, status, child_id, vaccine_id, batch_id
  │   FROM appointment WHERE id = #{appointmentId}
  └─ 记录不存在 → 返回 1004 NOT_FOUND

STEP 3: 状态校验
  └─ status != 8 → 返回 6001 VACCINATE_STATUS_INVALID

STEP 4: 查询儿童信息
  └─ SELECT name, gender, birth_date FROM child_profile WHERE id = #{childId}

STEP 5: 查询疫苗信息
  └─ SELECT name, category, manufacturer FROM vaccine WHERE id = #{vaccineId}

STEP 6: 查询批次信息
  ├─ SELECT batch_no, production_date, expiry_date
  │   FROM vaccine_batch WHERE id = #{batchId}
  └─ expiry_date <= NOW() → 返回 6003 VACCINATE_BATCH_EXPIRED

STEP 7: 组装核实结果
  └─ 返回 VerifyResult VO（含所有核实项及校验结果）
```

### 5.5 数据操作（SQL级）

```sql
-- 查询预约
SELECT id, status, child_id, vaccine_id, batch_id
FROM appointment
WHERE id = #{appointmentId};

-- 查询儿童信息
SELECT name, gender, birth_date
FROM child_profile
WHERE id = #{childId};

-- 查询疫苗信息
SELECT name, category, manufacturer
FROM vaccine
WHERE id = #{vaccineId};

-- 查询批次信息
SELECT batch_no, production_date, expiry_date
FROM vaccine_batch
WHERE id = #{batchId};
```

### 5.6 状态流转

本接口为只读校验，不涉及状态变更。

### 5.7 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 预约不存在 | 1004 | 404 | 返回"预约不存在" |
| 状态不允许接种 | 6001 | 409 | 返回"当前状态不允许接种" |
| 批次已过期 | 6003 | 400 | 返回"该批次已过期" |
| 未登录 | 1001 | 401 | 拦截器拦截 |
| 权限不足 | 1002 | 403 | 非接种医生角色拒绝访问 |

### 5.8 并发控制

本接口为只读校验，无需并发控制。

### 5.9 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `vaccinate.verify` |
| API前缀 | `/api/vaccinate/*` — 仅 DOCTOR_VACCINATE 角色可访问 |

---

## 6. F-VACCINATE-005 执行接种

### 6.1 功能描述

接种医生执行疫苗接种操作。本接口是接种模块的**核心写操作**，在单个事务内原子性地完成：生成注射号 → 扣减库存 → 写入接种记录 → 更新预约状态为留观中（8 → 10）。

> 预约状态机定义参见 REQ-GLOBAL §2

> **F-VACCINATE-006（生成注射号）、F-VACCINATE-007（扣减库存）、F-VACCINATE-008（记录接种信息）均作为本事务内的子步骤实现，不暴露独立 API。**

### 6.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 医生已登录且角色为 DOCTOR_VACCINATE | Token 有效 + 角色校验 |
| PRE-002 | 预约记录存在 | `appointment.id = #{appointmentId}` |
| PRE-003 | 预约状态为已登记 | `appointment.status = 8` |
| PRE-004 | 批次库存充足 | `available_stock >= 1 AND locked_stock >= 1` |
| PRE-005 | 批次未过期 | `vaccine_batch.expiry_date > NOW()` |
| PRE-006 | 接种部位已选择 | `injection_site IN (LEFT_UPPER_ARM, RIGHT_UPPER_ARM, LEFT_BUTTOCK, RIGHT_BUTTOCK)` |

### 6.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| doctorId | Long | 是 | Token 解析 | 接种医生ID |
| appointmentId | Long | 是 | 请求体 | 预约ID |
| injectionSite | String | 是 | 请求体 | 接种部位枚举值 |

### 6.4 处理流程

```
STEP 1: 参数格式校验
  ├─ appointmentId 非空且为正整数
  ├─ injectionSite 枚举校验（LEFT_UPPER_ARM / RIGHT_UPPER_ARM / LEFT_BUTTOCK / RIGHT_BUTTOCK）
  └─ 字段非空校验
  → 失败返回 1003 BAD_REQUEST

STEP 2: 锁定预约行，校验状态（PRE-002 + PRE-003）
  ├─ SELECT id, status, batch_id, child_id, vaccine_id FROM appointment
  │   WHERE id = #{appointmentId} FOR UPDATE
  ├─ 记录不存在 → 返回 1004 NOT_FOUND
  └─ status != 8 → 返回 6001 VACCINATE_STATUS_INVALID

STEP 3: 锁定库存行，校验库存（PRE-004）
  ├─ SELECT available_stock, locked_stock FROM hospital_vaccine_stock
  │   WHERE batch_id = #{batchId} FOR UPDATE
  └─ available_stock < 1 OR locked_stock < 1 → 返回 6002 VACCINATE_STOCK_INSUFFICIENT

STEP 4: 校验批次有效期（PRE-005）
  ├─ SELECT expiry_date FROM vaccine_batch WHERE id = #{batchId}
  └─ expiry_date <= NOW() → 返回 6003 VACCINATE_BATCH_EXPIRED

STEP 5: 生成注射号（F-VACCINATE-006 子步骤）
  ├─ 格式：INJ + YYYYMMDD + 4位序号
  ├─ 查询当日最大序号：
  │   SELECT MAX(CAST(SUBSTRING(injection_id, 13, 4) AS UNSIGNED)) AS max_seq
  │   FROM vaccination_record WHERE DATE(injection_time) = CURDATE()
  ├─ 新序号 = IFNULL(max_seq, 0) + 1
  ├─ injection_id = 'INJ' + DATE_FORMAT(CURDATE(), '%Y%m%d') + LPAD(新序号, 4, '0')
  └─ 生成失败（唯一性冲突）→ 重试最多3次 → 仍失败返回 6005 VACCINATE_INJECTION_ID_FAIL

STEP 6: 扣减库存（F-VACCINATE-007 子步骤，库存模型参见 REQ-GLOBAL §8）
  ├─ UPDATE hospital_vaccine_stock
  │   SET available_stock = available_stock - 1,
  │       locked_stock = locked_stock - 1
  │   WHERE batch_id = #{batchId}
  │     AND available_stock >= 1
  │     AND locked_stock >= 1
  └─ affected_rows = 0 → 返回 6006 VACCINATE_DEDUCT_FAILED

STEP 7: 写入接种记录（F-VACCINATE-008 子步骤）
  ├─ INSERT INTO vaccination_record
  │   (appointment_id, child_id, vaccine_id, injection_id, injection_time, doctor_id,
  │    injection_site, batch_id, batch_no, create_time, update_time)
  │   VALUES (#{appointmentId}, #{childId}, #{vaccineId}, #{injectionId}, NOW(), #{doctorId},
  │           #{injectionSite}, #{batchId}, #{batchNo}, NOW(), NOW())
  └─ 插入失败 → 返回 6007 VACCINATE_RECORD_SAVE_FAILED

STEP 8: 更新预约状态
  ├─ UPDATE appointment
  │   SET status = 10,
  > current_window 取值参见 REQ-GLOBAL §2.1 预约状态枚举（所在窗口列）和 §2.4 current_window 设置规则。
  │       current_window = 'OBSERVE',
  │       update_time = NOW()
  │   WHERE id = #{appointmentId}
  └─ affected_rows = 0 → 返回 6008 VACCINATE_STATUS_UPDATE_FAILED

STEP 9: 创建留观记录
  ├─ INSERT INTO observe_record
  │   (appointment_id, injection_id, start_time, status, standard_duration, create_time)
  │   VALUES (#{appointmentId}, #{injectionId}, NOW(), 0, 30, NOW())
  └─ 参见 REQ-GLOBAL §6 T6 事务模板

STEP 10: 返回结果
  └─ 返回 VaccinateSuccess VO（injection_id, injection_site, batch_no, injection_time）
```

### 6.5 数据操作（SQL级）

> 事务定义参见 REQ-GLOBAL §6 T6（接种执行事务）

```sql
-- ========================================
-- 事务开始
-- ========================================
BEGIN;

-- STEP 2: 锁定预约行
SELECT id, status, batch_id, child_id, vaccine_id
FROM appointment
WHERE id = #{appointmentId}
FOR UPDATE;

-- STEP 3: 锁定库存行
SELECT available_stock, locked_stock
FROM hospital_vaccine_stock
WHERE batch_id = #{batchId}
FOR UPDATE;

-- STEP 4: 校验批次有效期
SELECT expiry_date
FROM vaccine_batch
WHERE id = #{batchId};

-- STEP 5: 生成注射号 — 查询当日最大序号
SELECT MAX(CAST(SUBSTRING(injection_id, 13, 4) AS UNSIGNED)) AS max_seq
FROM vaccination_record
WHERE DATE(injection_time) = CURDATE();

-- STEP 6: 扣减库存
UPDATE hospital_vaccine_stock
SET available_stock = available_stock - 1,
    locked_stock = locked_stock - 1
WHERE batch_id = #{batchId}
  AND available_stock >= 1
  AND locked_stock >= 1;

-- STEP 7: 写入接种记录
INSERT INTO vaccination_record (
    appointment_id, child_id, vaccine_id, injection_id, injection_time, doctor_id,
    injection_site, batch_id, batch_no, create_time, update_time
) VALUES (
    #{appointmentId}, #{childId}, #{vaccineId}, #{injectionId}, NOW(), #{doctorId},
    #{injectionSite}, #{batchId}, #{batchNo}, NOW(), NOW()
);

-- STEP 8: 更新预约状态
UPDATE appointment
SET status = 10,
    current_window = 'OBSERVE',
    update_time = NOW()
WHERE id = #{appointmentId};
> current_window 取值参见 REQ-GLOBAL §2.1 预约状态枚举（所在窗口列）和 §2.4 current_window 设置规则。

-- STEP 9: 创建留观记录（初始状态为"观察中"）
INSERT INTO observe_record (
    appointment_id, injection_id, start_time,
    status, standard_duration, create_time
) VALUES (
    #{appointmentId}, #{injectionId}, NOW(),
    0, 30, NOW()
);

-- ========================================
-- 事务提交
-- ========================================
COMMIT;
```

> **事务回滚：** 上述任一步骤失败（抛出 RuntimeException 或 BusinessException），整个事务自动回滚，保证数据一致性。不会出现"扣了库存但没写记录"或"写了记录但没扣库存"的不一致情况。

### 6.6 状态流转

| 操作 | 前置状态 | 目标状态 | 说明 |
|------|----------|----------|------|
| 执行接种 | 8（已登记） | 10（留观中） | 接种完成，进入留观 |

> 接种记录永久保存，不归档不删除。参见 REQ-GLOBAL §11.2。

### 6.7 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 预约不存在 | 1004 | 404 | 返回"预约不存在" |
| 状态不允许接种 | 6001 | 409 | 返回"当前状态不允许接种" |
| 批次库存不足 | 6002 | 400 | 返回"批次库存不足，可用库存：{available_stock}，锁定库存：{locked_stock}" |
| 批次已过期 | 6003 | 400 | 返回"该批次已过期，有效期：{expiry_date}" |
| 接种部位未选择 | 6004 | 400 | 返回"请选择接种部位" |
| 注射号生成失败 | 6005 | 500 | 重试最多3次，仍失败返回"注射号生成失败，请重试" |
| 库存扣减失败 | 6006 | 500 | 事务回滚，返回"库存扣减失败，请重试" |
| 接种记录保存失败 | 6007 | 500 | 事务回滚，返回"接种记录保存失败，请重试" |
| 预约状态更新失败 | 6008 | 500 | 事务回滚，返回"预约状态更新失败，请重试" |
| 参数格式错误 | 1003 | 400 | 返回具体字段错误信息 |
| 未登录 | 1001 | 401 | 拦截器拦截 |
| 权限不足 | 1002 | 403 | 非接种医生角色拒绝访问 |
| 系统异常 | 1007 | 500 | 事务回滚，记录日志，返回通用错误提示 |

### 6.8 并发控制

**预约行锁 — 防止重复接种：**

```sql
-- 事务内 SELECT FOR UPDATE 锁定预约行
-- 同一时刻只有一个线程能操作该预约，防止重复接种
SELECT id, status, batch_id, child_id, vaccine_id
FROM appointment
WHERE id = #{appointmentId}
FOR UPDATE;
```

**库存行锁 — 防止库存超卖：**

```sql
-- 事务内 SELECT FOR UPDATE 锁定库存行
-- 多个医生并发接种不同预约但使用同一批次时，串行化库存操作
SELECT available_stock, locked_stock
FROM hospital_vaccine_stock
WHERE batch_id = #{batchId}
FOR UPDATE;
```

**UPDATE 条件守卫 — 防库存为负：**

```sql
-- WHERE 条件双重保证库存不为负
UPDATE hospital_vaccine_stock
SET available_stock = available_stock - 1,
    locked_stock = locked_stock - 1
WHERE batch_id = #{batchId}
  AND available_stock >= 1
  AND locked_stock >= 1;
-- affected_rows = 0 表示库存已被其他事务扣减
```

**重试策略：**

| 操作 | 最大重试次数 | 重试间隔 | 退避策略 |
|------|-------------|----------|----------|
| 注射号生成 | 3 | 即时 | 立即重试 |
| 库存扣减 | 3 | 100ms, 200ms, 400ms | 指数退避 |

### 6.9 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token，解析出 doctorId |
| 功能权限 | `vaccinate.execute` |
| API前缀 | `/api/vaccinate/*` — 仅 DOCTOR_VACCINATE 角色可访问 |

---

## 7. F-VACCINATE-009 查询接种记录

### 7.1 功能描述

接种医生查询自己执行的接种记录列表，支持按日期范围筛选和分页查询，按接种时间倒序排列。

> **说明：** 用户端接种记录查询返回精简字段（注射号、疫苗名称、接种时间），等同于 SRD 中的 record 简表，不单独建表。

### 7.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 医生已登录 | Token 有效 |

### 7.3 输入参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| doctorId | Long | 是 | — | Token 解析 |
| startDate | String | 否 | 30天前 | 起始日期，格式 YYYY-MM-DD |
| endDate | String | 否 | 当天 | 截止日期，格式 YYYY-MM-DD |
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 10 | 每页数量，最大50 |

### 7.4 处理流程

```
STEP 1: 参数校验
  ├─ startDate、endDate 格式校验（YYYY-MM-DD）
  ├─ startDate <= endDate
  ├─ page >= 1, size ∈ [1, 50]
  → 失败返回 1003 BAD_REQUEST

STEP 2: 查询总数
  └─ SELECT COUNT(*) FROM vaccination_record vr
      JOIN appointment a ON vr.appointment_id = a.id
      WHERE vr.doctor_id = #{doctorId}
        AND DATE(vr.injection_time) >= #{startDate}
        AND DATE(vr.injection_time) <= #{endDate}

STEP 3: 查询分页数据
  └─ SELECT vr.injection_id, cp.name, v.name, vr.injection_site, vr.batch_no, vr.injection_time
      FROM vaccination_record vr
      JOIN appointment a ON vr.appointment_id = a.id
      JOIN child_profile cp ON a.child_id = cp.id
      JOIN vaccine_batch vb ON vr.batch_id = vb.id
      JOIN vaccine v ON vb.vaccine_id = v.id
      WHERE vr.doctor_id = #{doctorId}
        AND DATE(vr.injection_time) >= #{startDate}
        AND DATE(vr.injection_time) <= #{endDate}
      ORDER BY vr.injection_time DESC
      LIMIT #{offset}, #{size}

STEP 4: 组装分页响应
  └─ 返回 records + total + page + size + pages
```

### 7.5 数据操作（SQL级）

```sql
-- 查询总数
SELECT COUNT(*)
FROM vaccination_record vr
JOIN appointment a ON vr.appointment_id = a.id
WHERE vr.doctor_id = #{doctorId}
  AND DATE(vr.injection_time) >= #{startDate}
  AND DATE(vr.injection_time) <= #{endDate};

-- 查询分页数据
SELECT
    vr.id               AS record_id,
    vr.injection_id     AS injection_id,
    cp.name             AS child_name,
    v.name              AS vaccine_name,
    vr.injection_site   AS injection_site,
    vr.batch_no         AS batch_no,
    vr.injection_time   AS injection_time
FROM vaccination_record vr
JOIN appointment a ON vr.appointment_id = a.id
JOIN child_profile cp ON a.child_id = cp.id
JOIN vaccine_batch vb ON vr.batch_id = vb.id
JOIN vaccine v ON vb.vaccine_id = v.id
WHERE vr.doctor_id = #{doctorId}
  AND DATE(vr.injection_time) >= #{startDate}
  AND DATE(vr.injection_time) <= #{endDate}
ORDER BY vr.injection_time DESC
LIMIT #{offset}, #{size};
```

### 7.6 状态流转

本接口为只读查询，不涉及状态变更。

### 7.7 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 参数格式错误 | 1003 | 400 | 返回具体字段错误 |
| 接种记录不存在 | 6009 | 404 | 返回"接种记录不存在" |
| 无权查看接种记录 | 6010 | 403 | 返回"无权查看该接种记录" |
| 未登录 | 1001 | 401 | 拦截器拦截 |
| 权限不足 | 1002 | 403 | 非接种医生角色拒绝访问 |

### 7.8 并发控制

本接口为只读查询，无需并发控制。

### 7.9 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `record.view.own` |
| 数据权限 | SQL WHERE `vr.doctor_id = #{doctorId}`，确保医生只能查看自己的接种记录 |
| API前缀 | `/api/vaccinate/*` — 仅 DOCTOR_VACCINATE 角色可访问 |

---

## 8. F-VACCINATE-010 查询儿童接种记录

### 8.1 功能描述

接种医生查询指定儿童的接种历史记录，按接种时间倒序排列。

### 8.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 医生已登录 | Token 有效 |
| PRE-002 | 儿童档案存在 | `child_profile.id = #{childId}` |

### 8.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| childId | Long | 是 | 路径参数 | 儿童档案ID |
| doctorId | Long | 是 | Token 解析 | 当前医生ID |

### 8.4 处理流程

```
STEP 1: 参数校验
  └─ childId 非空且为正整数
  → 失败返回 1003 BAD_REQUEST

STEP 2: 校验儿童档案存在
  ├─ SELECT id, name FROM child_profile WHERE id = #{childId}
  └─ 不存在 → 返回 1004 NOT_FOUND

STEP 3: 查询儿童接种记录
  └─ SELECT vr.injection_id, v.name, vr.injection_site, vr.batch_no, vr.injection_time
      FROM vaccination_record vr
      JOIN appointment a ON vr.appointment_id = a.id
      JOIN vaccine_batch vb ON vr.batch_id = vb.id
      JOIN vaccine v ON vb.vaccine_id = v.id
      WHERE a.child_id = #{childId}
      ORDER BY vr.injection_time DESC

STEP 4: 返回结果
  └─ 返回儿童接种记录列表
```

### 8.5 数据操作（SQL级）

```sql
-- 校验儿童档案
SELECT id, name
FROM child_profile
WHERE id = #{childId};

-- 查询儿童接种记录
SELECT
    vr.injection_id     AS injection_id,
    v.name              AS vaccine_name,
    vr.injection_site   AS injection_site,
    vr.batch_no         AS batch_no,
    vr.injection_time   AS injection_time
FROM vaccination_record vr
JOIN appointment a ON vr.appointment_id = a.id
JOIN vaccine_batch vb ON vr.batch_id = vb.id
JOIN vaccine v ON vb.vaccine_id = v.id
WHERE a.child_id = #{childId}
ORDER BY vr.injection_time DESC;
```

### 8.6 状态流转

本接口为只读查询，不涉及状态变更。

### 8.7 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 儿童档案不存在 | 1004 | 404 | 返回"儿童档案不存在" |
| 未登录 | 1001 | 401 | 拦截器拦截 |
| 权限不足 | 1002 | 403 | 非接种医生角色拒绝访问 |

### 8.8 并发控制

本接口为只读查询，无需并发控制。

### 8.9 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `record.view.child` |
| API前缀 | `/api/vaccinate/*` — 仅 DOCTOR_VACCINATE 角色可访问 |

---

## 9. F-VACCINATE-011 查询接种统计

### 9.1 功能描述

业务管理医生查询接种统计数据，支持按医生、日期范围、疫苗维度筛选，返回总接种数及各维度分组统计。

### 9.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 管理医生已登录 | Token 有效 + 角色校验 |

### 9.3 输入参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| doctorId | Long | 否 | NULL | 不填则统计全部医生 |
| startDate | String | 否 | 30天前 | 起始日期 |
| endDate | String | 否 | 当天 | 截止日期 |
| vaccineId | Long | 否 | NULL | 不填则统计全部疫苗 |

### 9.4 处理流程

```
STEP 1: 参数校验
  ├─ startDate、endDate 格式校验（YYYY-MM-DD）
  ├─ startDate <= endDate
  → 失败返回 1003 BAD_REQUEST

STEP 2: 查询总接种数
  └─ SELECT COUNT(*) FROM vaccination_record WHERE ... 条件

STEP 3: 查询按医生分组统计
  └─ SELECT doctor_id, COUNT(*) FROM vaccination_record WHERE ...
      GROUP BY doctor_id

STEP 4: 查询按疫苗分组统计
  └─ SELECT vb.vaccine_id, v.name, COUNT(*) FROM vaccination_record vr
      JOIN vaccine_batch vb ON vr.batch_id = vb.id
      JOIN vaccine v ON vb.vaccine_id = v.id
      WHERE ... GROUP BY vb.vaccine_id

STEP 5: 查询按接种部位分组统计
  └─ SELECT injection_site, COUNT(*) FROM vaccination_record
      WHERE ... GROUP BY injection_site

STEP 6: 查询按日期分组统计（最近7天）
  └─ SELECT DATE(injection_time), COUNT(*) FROM vaccination_record
      WHERE ... GROUP BY DATE(injection_time)

STEP 7: 组装返回结果
  └─ 映射为 VaccinateStatistics VO
```

### 9.5 数据操作（SQL级）

```sql
-- 总接种数
SELECT COUNT(*) AS total_count
FROM vaccination_record
WHERE DATE(injection_time) >= #{startDate}
  AND DATE(injection_time) <= #{endDate}
  AND (#{doctorId} IS NULL OR doctor_id = #{doctorId})
  AND (#{vaccineId} IS NULL OR batch_id IN (
      SELECT id FROM vaccine_batch WHERE vaccine_id = #{vaccineId}
  ));

-- 按医生分组统计
SELECT vr.doctor_id, su.name AS doctor_name, COUNT(*) AS count
FROM vaccination_record vr
LEFT JOIN sys_user su ON vr.doctor_id = su.id
WHERE DATE(vr.injection_time) >= #{startDate}
  AND DATE(vr.injection_time) <= #{endDate}
  AND (#{doctorId} IS NULL OR vr.doctor_id = #{doctorId})
  AND (#{vaccineId} IS NULL OR vr.batch_id IN (
      SELECT id FROM vaccine_batch WHERE vaccine_id = #{vaccineId}
  ))
GROUP BY vr.doctor_id, su.name;

-- 按疫苗分组统计
SELECT vb.vaccine_id, v.name AS vaccine_name, COUNT(*) AS count
FROM vaccination_record vr
JOIN vaccine_batch vb ON vr.batch_id = vb.id
JOIN vaccine v ON vb.vaccine_id = v.id
WHERE DATE(vr.injection_time) >= #{startDate}
  AND DATE(vr.injection_time) <= #{endDate}
  AND (#{doctorId} IS NULL OR vr.doctor_id = #{doctorId})
  AND (#{vaccineId} IS NULL OR vb.vaccine_id = #{vaccineId})
GROUP BY vb.vaccine_id, v.name;

-- 按接种部位分组统计
SELECT injection_site, COUNT(*) AS count
FROM vaccination_record
WHERE DATE(injection_time) >= #{startDate}
  AND DATE(injection_time) <= #{endDate}
  AND (#{doctorId} IS NULL OR doctor_id = #{doctorId})
  AND (#{vaccineId} IS NULL OR batch_id IN (
      SELECT id FROM vaccine_batch WHERE vaccine_id = #{vaccineId}
  ))
GROUP BY injection_site;

-- 按日期分组统计（最近7天）
SELECT DATE(vr.injection_time) AS inject_date, COUNT(*) AS count
FROM vaccination_record vr
WHERE DATE(vr.injection_time) >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
  AND DATE(vr.injection_time) <= CURDATE()
  AND (#{doctorId} IS NULL OR vr.doctor_id = #{doctorId})
  AND (#{vaccineId} IS NULL OR vr.batch_id IN (
      SELECT id FROM vaccine_batch WHERE vaccine_id = #{vaccineId}
  ))
GROUP BY DATE(vr.injection_time)
ORDER BY inject_date ASC;
```

### 9.6 状态流转

本接口为只读查询，不涉及状态变更。

### 9.7 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 参数格式错误 | 1003 | 400 | 返回具体字段错误 |
| 未登录 | 1001 | 401 | 拦截器拦截 |
| 权限不足 | 1002 | 403 | 非管理医生角色拒绝访问 |

### 9.8 并发控制

本接口为只读查询，无需并发控制。

### 9.9 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `stats.view` |
| API前缀 | `/api/vaccinate/*` — DOCTOR_VACCINATE 和 DOCTOR_BUSINESS_ADMIN 角色均可访问（本端点仅 DOCTOR_BUSINESS_ADMIN 有 `stats.view` 权限） |

---

## 10. injection_id 生成规则

### 10.1 格式定义

> **injection_id = `INJ` + `YYYYMMDD` + `4位序号`**

| 组成部分 | 长度 | 说明 |
|----------|------|------|
| 前缀 | 3 | 固定值 `INJ` |
| 日期 | 8 | 接种日期，格式 YYYYMMDD |
| 序号 | 4 | 当日自增序号，从 0001 开始 |

**示例：** `INJ202604020001` = 2026年4月2日第1个注射号

### 10.2 生成逻辑

```java
/**
 * 生成注射号（事务内调用）
 * 位置：F-VACCINATE-005 的 STEP 5
 */
public String generateInjectionId() {
    // 1. 查询当日最大序号
    Integer maxSeq = vaccinationRecordMapper.selectMaxSeqOfToday();
    // SQL: SELECT MAX(CAST(SUBSTRING(injection_id, 13, 4) AS UNSIGNED))
    //      FROM vaccination_record WHERE DATE(injection_time) = CURDATE()

    // 2. 计算新序号
    int newSeq = (maxSeq == null ? 0 : maxSeq) + 1;

    // 3. 拼接注射号
    String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    String seqPart = String.format("%04d", newSeq);

    return "INJ" + datePart + seqPart;
}
```

### 10.3 唯一性保障

| 保障层 | 机制 | 说明 |
|--------|------|------|
| 事务隔离 | 整体在事务内执行 | injection_id 生成与 INSERT 在同一事务 |
| 行锁保护 | 预约行 FOR UPDATE | 同一预约不会被并发接种 |
| 数据库约束 | `vaccination_record.injection_id` UNIQUE 索引 | 最终兜底，重复时抛异常 |
| 重试机制 | 最多重试3次 | 序号冲突时重新生成 |

### 10.4 序号重置规则

- 序号每天重置，从 0001 开始
- 不跨天延续
- 重置方式：查询条件使用 `DATE(injection_time) = CURDATE()`，天然按天隔离

---

## 11. 防重复接种机制

### 11.1 防重复接种策略

> **本模块通过三层机制防止重复接种：**

```
第一层：状态校验（业务逻辑）
  └─ 接种前校验 appointment.status = 8
  └─ 首次接种后 status 变为 10，后续请求因 status != 8 被拒绝

第二层：行级锁（并发控制）
  └─ SELECT FOR UPDATE 锁定预约行
  └─ 并发请求串行化，同一时刻只有一个线程能操作同一预约

第三层：数据库约束（最终兜底）
  └─ vaccination_record.appointment_id 可设置唯一索引
  └─ 即使前两层失效，数据库唯一约束也能阻止重复写入
```

### 11.2 防库存错误策略

```
第一层：库存校验（业务逻辑）
  └─ SELECT FOR UPDATE 锁定库存行后检查
     available_stock >= 1 AND locked_stock >= 1

第二层：UPDATE 条件守卫（数据库层）
  └─ UPDATE ... WHERE available_stock >= 1 AND locked_stock >= 1
  └─ affected_rows = 0 表示库存已被其他事务扣减

第三层：事务回滚（一致性保障）
  └─ 库存扣减失败时整个事务回滚
  └─ 不会出现"扣了库存但没写记录"或"写了记录但没扣库存"的情况
```

### 11.3 完整防并发流程

```
接种请求到达
    │
    ▼
BEGIN; ← 开启事务
    │
    ▼
SELECT ... FROM appointment WHERE id = ? FOR UPDATE;
    │
    ├─ 记录不存在 → ROLLBACK; 返回 1004
    ├─ status != 8 → ROLLBACK; 返回 6001  ← 防重复接种（第一层）
    │
    ▼
SELECT ... FROM hospital_vaccine_stock WHERE batch_id = ? FOR UPDATE;
    │
    ├─ available_stock < 1 OR locked_stock < 1
    │   → ROLLBACK; 返回 6002
    │
    ▼
UPDATE hospital_vaccine_stock
  SET available_stock = available_stock - 1,
      locked_stock = locked_stock - 1
  WHERE batch_id = ?
    AND available_stock >= 1
    AND locked_stock >= 1;
    │
    ├─ affected_rows = 0
    │   → ROLLBACK; 返回 6006  ← 防库存错误（第二层）
    │
    ▼
INSERT INTO vaccination_record (...);
    │
    ├─ 唯一索引冲突
    │   → ROLLBACK; 返回 6007  ← 防重复接种（第三层兜底）
    │
    ▼
UPDATE appointment SET status = 10 ...;
    │
    ▼
COMMIT; ← 提交事务
```

---

## 12. 状态非法校验

### 12.1 校验原则

> **任何状态变更操作，执行前必须先锁定预约行（`SELECT FOR UPDATE`），校验当前状态是否在允许的前置状态列表中。非法状态一律拒绝操作。**

### 12.2 本模块操作的状态校验矩阵

| 操作 | 允许的前置状态 | 非法状态 | 拒绝错误码 |
|------|---------------|----------|-----------|
| 执行接种 | 8（已登记） | 1,2,3,4,6,7,9,10 | 6001 VACCINATE_STATUS_INVALID |

### 12.3 状态校验伪代码

```java
/**
 * 执行接种 — 状态校验
 */
public Appointment validateVaccinate(Long appointmentId) {
    // 1. 加行锁查询
    Appointment appt = appointmentMapper.selectForUpdate(appointmentId);
    if (appt == null) {
        throw new BusinessException(1004, "预约不存在");
    }

    // 2. 状态校验
    if (appt.getStatus() != 8) {
        String statusText = StatusCodeMapping.getText(appt.getStatus());
        throw new BusinessException(6001,
            "当前状态不允许接种，当前状态：" + statusText);
    }

    return appt;
}
```

### 12.4 非法状态流转的错误消息

| 当前状态 | 尝试操作 | 错误码 | 错误消息 |
|----------|----------|--------|----------|
| 1（已预约） | 接种 | 6001 | 当前状态不允许接种，当前状态：已预约 |
| 2（已完成） | 接种 | 6001 | 当前状态不允许接种，当前状态：已完成 |
| 3（已取消） | 接种 | 6001 | 当前状态不允许接种，当前状态：已取消 |
| 4（已过期） | 接种 | 6001 | 当前状态不允许接种，当前状态：已过期 |
| 6（已签到） | 接种 | 6001 | 当前状态不允许接种，当前状态：已签到 |
| 7（预检通过） | 接种 | 6001 | 当前状态不允许接种，当前状态：预检通过 |
| 9（预检失败） | 接种 | 6001 | 当前状态不允许接种，当前状态：预检失败 |
| 10（留观中） | 接种 | 6001 | 当前状态不允许接种，当前状态：留观中 |

---

## 13. 权限控制汇总

### 13.1 功能权限矩阵

| 功能编号 | 权限编码 | 允许角色 | API路径 |
|----------|----------|----------|---------|
| F-VACCINATE-001 | `appointment.view.queue` | DOCTOR_VACCINATE | `GET /api/vaccinate/queue` |
| F-VACCINATE-002 | `vaccinate.view` | DOCTOR_VACCINATE | `GET /api/vaccinate/detail/{appointmentId}` |
| F-VACCINATE-003 | `vaccinate.verify` | DOCTOR_VACCINATE | `POST /api/vaccinate/verify` |
| F-VACCINATE-005 | `vaccinate.execute` | DOCTOR_VACCINATE | `POST /api/vaccinate/execute` |
| F-VACCINATE-009 | `record.view.own` | DOCTOR_VACCINATE | `GET /api/vaccinate/record/list` |
| F-VACCINATE-010 | `record.view.child` | DOCTOR_VACCINATE | `GET /api/vaccinate/record/child/{childId}` |
| F-VACCINATE-011 | `stats.view` | DOCTOR_BUSINESS_ADMIN | `GET /api/vaccinate/statistics` |

### 13.2 数据权限规则

| 角色 | 数据范围 | 实现方式 |
|------|----------|----------|
| DOCTOR_VACCINATE | 仅自己执行的接种记录 | `WHERE doctor_id = #{currentDoctorId}` |
| DOCTOR_BUSINESS_ADMIN | 全部接种统计数据 | 无限制 |

### 13.3 权限校验流程

```
请求到达
  │
  ▼
解析 Token → 获取 doctorId
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
  ├─ 数据越权 → 返回 { code: 6010, message: "无权查看该接种记录" }
  │
  ▼
执行业务操作
  │
  ▼
返回响应
```

---

## 14. 错误码定义

### 14.1 本模块错误码段（6000-6099）

| 错误码 | 常量名 | 说明 |
|--------|--------|------|
| 6001 | VACCINATE_STATUS_INVALID | 当前状态不允许接种 |
| 6002 | VACCINATE_STOCK_INSUFFICIENT | 批次库存不足 |
| 6003 | VACCINATE_BATCH_EXPIRED | 批次已过期 |
| 6004 | VACCINATE_SITE_NOT_SELECTED | 接种部位未选择 |
| 6005 | VACCINATE_INJECTION_ID_FAIL | 注射号生成失败 |
| 6006 | VACCINATE_DEDUCT_FAILED | 库存扣减失败 |
| 6007 | VACCINATE_RECORD_SAVE_FAILED | 接种记录保存失败 |
| 6008 | VACCINATE_STATUS_UPDATE_FAILED | 预约状态更新失败 |
| 6009 | VACCINATE_RECORD_NOT_FOUND | 接种记录不存在 |
| 6010 | VACCINATE_RECORD_NO_PERMISSION | 无权查看接种记录 |

### 14.2 共享错误码段（1900-1999）

本模块引用的跨模块共享错误码，定义于全局共享段：

| 错误码 | 常量名 | 说明 | 原属模块 |
|--------|--------|------|----------|
| 1900-1999 | — | 共享错误码段（跨模块复用） | 全局共享 |

> 共享错误码段 1900-1999 用于跨模块复用的通用错误场景，避免模块间错误码段借用。详见 REQ-GLOBAL 错误码总表。

---

## 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| V1.0 | 2026-04-02 | 初始版本，基于 PRD-VACCINATE V1.1 / REQ-GLOBAL V1.0 生成 |
| V1.1 | 2026-04-03 | V3 评审修复：(1) 错误码对齐 GLOBAL §4；(2) 业务规则修正（防重复接种/库存扣减）；(3) 版本号/依赖版本对齐 |
| V1.2 | 2026-04-03 | REQ 评审修复：(1) §6.4 伪代码新增 STEP 9 创建留观记录；(2) §6.7 异常表删除 6009/6010；(3) §6.4 STEP 6 增加 GLOBAL §8 库存模型引用；(4) 文档头上游依赖同步至 REQ-GLOBAL V1.3 |

---

**文档结束**
