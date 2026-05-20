# 登记模块 — 研发需求文档

**文档编号:** REQ-REGISTER-001
**版本:** V1.2
**状态:** 正式发布
**日期:** 2026-04-03
**上游依赖:** PRD-REGISTER V2.2 / REQ-GLOBAL V1.3

---

## 目录

1. [模块定位与边界](#1-模块定位与边界)
2. [功能清单](#2-功能清单)
3. [F-REGISTER-001 查看待登记队列](#3-f-register-001-查看待登记队列)
4. [F-REGISTER-002 查询登记详情](#4-f-register-002-查询登记详情)
5. [F-REGISTER-003 核实信息](#5-f-register-003-核实信息)
6. [F-REGISTER-004 执行登记（FEFO分配+锁库存+生成排队号+状态推进）](#6-f-register-004-执行登记)
7. [F-REGISTER-005 查询可用批次](#7-f-register-005-查询可用批次)
8. [F-REGISTER-006 更换批次](#8-f-register-006-更换批次)
9. [核心算法：FEFO批次分配](#9-核心算法fefo批次分配)
10. [核心逻辑：锁库存](#10-核心逻辑锁库存)
11. [核心逻辑：排队号生成](#11-核心逻辑排队号生成)
12. [register_queue表操作规范](#12-register_queue表操作规范)
13. [状态非法校验](#13-状态非法校验)
14. [权限控制汇总](#14-权限控制汇总)
15. [补充错误码（跨模块引用）](#15-补充错误码跨模块引用)

---

## 1. 模块定位与边界

### 1.1 核心定位

REGISTER 是接种流程的桥梁模块，负责：
- **信息核实** — 核实儿童信息、预约信息、预检结果
- **批次分配（FEFO）** — 按先过期先出原则为预约分配疫苗批次
- **库存锁定** — 登记时锁定1支库存（locked_stock += 1）
- **排队管理** — 生成排队号，管理登记排队队列
- **状态推进** — 将预约状态从 7（预检通过）推进到 8（已登记）

### 1.2 不包含内容

| 禁止范围 | 归属模块 |
|----------|----------|
| 预检评估 | FLOW-PRECHECK |
| 接种执行+库存扣减 | VACCINATE |
| 批次查询/调拨/销毁/预警 | STOCK |
| 批次统计 | ADMIN |

> PRD-REGISTER 中的 F-REGISTER-008~017（批次查询、调拨、销毁、预警、统计）均归属 STOCK / ADMIN 模块，本 REQ 不再重复定义。本模块仅负责登记核心流程（F-REGISTER-001~007）及可用批次查询（F-REGISTER-011）。

### 1.3 本模块状态操作范围

> 规则来源：REQ-GLOBAL §3.3 各模块可操作状态汇总（REGISTER 可读状态 7, 8，可写目标状态：登记→8）及 §2.1 预约状态枚举（7=预检通过 PRECHECK_PASS，8=已登记 REGISTERED）

```
本模块可写状态：
  登记 → status = 8（已登记）    前置：status = 7

本模块只读状态：
  7（预检通过）、8（已登记）
```

---

## 2. 功能清单

| 编号 | 功能名称 | API | 角色 | 优先级 |
|------|----------|-----|------|--------|
| F-REGISTER-001 | 查看待登记队列 | `GET /api/register/queue` | DOCTOR_REGISTER | P0 |
| F-REGISTER-002 | 查询登记详情 | `GET /api/register/detail/{appointmentId}` | DOCTOR_REGISTER | P0 |
| F-REGISTER-003 | 核实信息 | `POST /api/register/verify` | DOCTOR_REGISTER | P0 |
| F-REGISTER-004 | 执行登记 | `POST /api/register/save` | DOCTOR_REGISTER | P0 |
| F-REGISTER-005 | 查询可用批次 | `GET /api/register/batch/available` | DOCTOR_REGISTER | P0 |
| F-REGISTER-006 | 更换批次 | `POST /api/register/batch/switch` | DOCTOR_REGISTER | P1 |
| F-REGISTER-004 | FEFO分配批次 | `GET /api/register/batch/assign` | DOCTOR_REGISTER | P0 |
| F-REGISTER-005 | 锁定库存 | `POST /api/register/stock/lock` | DOCTOR_REGISTER | P0 |
| F-REGISTER-006 | 生成排队号 | `GET /api/register/queue/no` | DOCTOR_REGISTER | P0 |
| F-REGISTER-007 | 保存登记记录 | `POST /api/register/save` | DOCTOR_REGISTER | P0 |
| F-REGISTER-008 | 查询批次列表 | `GET /api/stock/batch/list` | DOCTOR_STOCK | P0 |
| F-REGISTER-009 | 查询批次详情 | `GET /api/stock/batch/{batchId}` | DOCTOR_STOCK | P0 |
| F-REGISTER-010 | 查询批次库存 | `GET /api/stock/batch/{batchId}/stock` | DOCTOR_STOCK | P0 |
| F-REGISTER-011 | 查询可用批次 | `GET /api/stock/batch/available` | DOCTOR_STOCK, DOCTOR_REGISTER | P0 |
| F-REGISTER-012 | 创建调拨单 | `POST /api/stock/transfer/create` | DOCTOR_STOCK | P1 |
| F-REGISTER-013 | 确认调拨 | `POST /api/stock/transfer/confirm` | DOCTOR_STOCK | P1 |
| F-REGISTER-014 | 查询调拨记录 | `GET /api/stock/transfer/list` | DOCTOR_STOCK | P1 |
| F-REGISTER-015 | 销毁批次 | `POST /api/stock/batch/dispose` | DOCTOR_STOCK | P1 |
| F-REGISTER-016 | 查询批次预警 | `GET /api/stock/alert/list` | DOCTOR_STOCK | P1 |
| F-REGISTER-017 | 查询批次统计 | `POST /api/stats/stock` | DOCTOR_BUSINESS_ADMIN | P2 |

> **设计说明：** PRD-REGISTER 中的 F-REGISTER-004（FEFO分配）、F-REGISTER-005（锁库存）、F-REGISTER-006（生成排队号）、F-REGISTER-007（保存登记记录）在研发层面合并为 REQ-F-REGISTER-004（执行登记），这4步在同一个事务内原子完成，不可拆分为独立接口。上表中保留了 PRD 功能编号及 API 映射以确保 PRD→REQ 可追溯性。F-REGISTER-008~017 归属 STOCK/ADMIN 模块，此处仅作引用。

---

## 3. F-REGISTER-001 查看待登记队列

### 3.1 功能描述

登记医生查询预检通过但尚未登记的预约列表，按预检通过时间升序排列（先通过先登记）。

### 3.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 登记医生已登录 | Token 有效 |
| PRE-002 | 登记医生拥有队列查看权限 | `appointment.view.queue` |

### 3.3 输入参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| doctorId | Long | 是 | — | Token 解析 |
| date | String | 否 | CURDATE() | 预约日期，格式 YYYY-MM-DD |
| filter | String | 否 | PENDING | 筛选：PENDING（待登记）/ DONE（已登记）/ ALL（全部） |

### 3.4 处理流程

```
STEP 1: 参数校验
  ├─ date 格式校验（YYYY-MM-DD）
  ├─ filter 枚举校验（PENDING/DONE/ALL）
  → 失败返回 1003 BAD_REQUEST

STEP 2: 构建状态过滤条件
  ├─ PENDING → AND a.status = 7
  ├─ DONE    → AND a.status = 8
  └─ ALL     → AND a.status IN (7, 8)

STEP 3: 查询队列列表
  ├─ SELECT a.id, a.appointment_no, cp.name AS child_name, pcr.check_time AS precheck_time
  │        v.name AS vaccine_name
  │   FROM appointment a
  │   JOIN child_profile cp ON a.child_id = cp.id
  │   JOIN pre_check_record pcr ON a.id = pcr.appointment_id
  │   JOIN vaccine v ON a.vaccine_id = v.id
  │   WHERE a.appointment_date = #{date}
  │     AND a.status IN (#{statusList})
  │   ORDER BY pcr.check_time ASC
  └─ 返回队列列表
```

### 3.5 数据操作（SQL级）

```sql
SELECT
    a.id                AS appointment_id,
    a.appointment_no    AS appointment_no,
    a.appointment_date  AS appointment_date,
    a.time_slot         AS time_slot,
    a.status            AS status,
    cp.name             AS child_name,
    cp.gender           AS child_gender,
    cp.birth_date       AS child_birth_date,
    pcr.check_time      AS precheck_time,
    v.name              AS vaccine_name,
    v.category          AS vaccine_category
FROM appointment a
JOIN child_profile cp ON a.child_id = cp.id
JOIN pre_check_record pcr ON a.id = pcr.appointment_id
JOIN vaccine v ON a.vaccine_id = v.id
WHERE a.appointment_date = #{date}
  AND a.status IN (#{statusList})
ORDER BY pcr.check_time ASC;
```

### 3.6 状态流转

本接口为只读查询，不涉及状态变更。

### 3.7 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 未登录 | 1001 | 401 | 拦截器拦截 |
| 登录已过期 | 1001 | 401 | 清除Token，跳转登录页 |
| 无权限 | 1002 | 403 | 返回"无权限" |
| 参数格式错误 | 1003 | 400 | 返回具体字段错误 |
| 参数校验失败：{field_name} | 1003 | 400 | 返回具体字段错误 |

### 3.8 并发控制

本接口为只读查询，无需并发控制。

### 3.9 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `appointment.view.queue` |
| API前缀 | `/api/register/*` — 仅 DOCTOR_REGISTER 可访问 |

---

## 4. F-REGISTER-002 查询登记详情

### 4.1 功能描述

根据预约ID查询预约的完整登记详情，包含预约基本信息、儿童信息、疫苗信息、预检信息、登记信息（如有）。

### 4.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 登记医生已登录 | Token 有效 |
| PRE-002 | 预约记录存在 | `appointment.id = #{appointmentId}` |
| PRE-003 | 预约状态为7或8 | `appointment.status IN (7, 8)` |

### 4.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| appointmentId | Long | 是 | 路径参数 | 预约ID |
| doctorId | Long | 是 | Token 解析 | 当前登录医生ID |

### 4.4 处理流程

```
STEP 1: 参数校验
  └─ appointmentId 非空且为正整数
  → 失败返回 1003 BAD_REQUEST

STEP 2: 查询预约记录
  ├─ SELECT a.*, cp.*, v.*, pcr.* FROM appointment a
  │   JOIN child_profile cp ON a.child_id = cp.id
  │   JOIN vaccine v ON a.vaccine_id = v.id
  │   LEFT JOIN pre_check_record pcr ON a.id = pcr.appointment_id
  │   WHERE a.id = #{appointmentId}
  └─ 记录不存在 → 返回 1004 NOT_FOUND

STEP 3: 状态校验
  └─ status NOT IN (7, 8) → 返回 5001 REGISTER_STATUS_INVALID

STEP 4: 查询登记信息（如有）
  ├─ SELECT rq.*, su.name AS doctor_name FROM register_queue rq
  │   LEFT JOIN sys_user su ON rq.doctor_id = su.id
  │   WHERE rq.appointment_id = #{appointmentId}
  └─ 无记录返回 NULL（尚未登记）

STEP 5: 组装返回结果
  └─ 映射为 RegisterDetail VO
```

### 4.5 数据操作（SQL级）

```sql
-- 查询预约+儿童+疫苗+预检信息
SELECT
    a.id                AS appointment_id,
    a.appointment_no    AS appointment_no,
    a.appointment_date  AS appointment_date,
    a.time_slot         AS time_slot,
    a.status            AS status,
    a.batch_id          AS batch_id,
    cp.name             AS child_name,
    cp.gender           AS child_gender,
    cp.birth_date       AS child_birth_date,
    cp.id_card          AS child_id_card,
    v.name              AS vaccine_name,
    v.category          AS vaccine_category,
    v.manufacturer      AS manufacturer,
    pcr.check_time      AS precheck_time,
    pcr.check_result    AS precheck_result
FROM appointment a
JOIN child_profile cp ON a.child_id = cp.id
JOIN vaccine v ON a.vaccine_id = v.id
LEFT JOIN pre_check_record pcr ON a.id = pcr.appointment_id
WHERE a.id = #{appointmentId};

-- 查询登记信息（如有）
SELECT
    rq.id              AS register_id,
    rq.doctor_id       AS doctor_id,
    su.name            AS doctor_name,
    rq.queue_no        AS queue_no,
    rq.batch_id        AS batch_id,
    rq.batch_no        AS batch_no,
    rq.register_time   AS register_time,
    rq.verify_status   AS verify_status
FROM register_queue rq
LEFT JOIN sys_user su ON rq.doctor_id = su.id
WHERE rq.appointment_id = #{appointmentId};
```

### 4.6 状态流转

本接口为只读查询，不涉及状态变更。

### 4.7 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 预约不存在 | 1004 | 404 | 返回"预约不存在" |
| 状态不允许查看 | 5001 | 409 | 返回"当前状态不允许登记操作" |
| 未登录 | 1001 | 401 | 拦截器拦截 |
| 登录已过期 | 1001 | 401 | 清除Token，跳转登录页 |
| 无权限 | 1002 | 403 | 返回"无权限" |
| 并发冲突 | 5006 | 409 | 返回友好提示 |

### 4.8 并发控制

本接口为只读查询，无需并发控制。

### 4.9 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `register.verify` |
| API前缀 | `/api/register/*` — 仅 DOCTOR_REGISTER 可访问 |

---

## 5. F-REGISTER-003 核实信息

### 5.1 功能描述

登记医生核实儿童信息、预约信息、预检结果，确保信息准确无误。本接口仅记录核实状态，不推进预约状态。

### 5.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 登记医生已登录 | Token 有效 |
| PRE-002 | 预约记录存在 | `appointment.id = #{appointmentId}` |
| PRE-003 | 预约状态为预检通过 | `appointment.status = 7` |
| PRE-004 | 预检结果为通过 | `pre_check_record.check_result = 'PASS'` |

### 5.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| appointmentId | Long | 是 | 请求体 | 预约ID |
| doctorId | Long | 是 | Token 解析 | 当前登录医生ID |

### 5.4 处理流程

```
STEP 1: 参数校验
  └─ appointmentId 非空且为正整数
  → 失败返回 1003 BAD_REQUEST

STEP 2: 查询预约记录（加行锁）
  ├─ SELECT id, status, vaccine_id, child_id FROM appointment WHERE id = #{appointmentId} FOR UPDATE
  └─ 记录不存在 → 返回 1004 NOT_FOUND

STEP 3: 状态校验
  └─ status != 7 → 返回 5001 REGISTER_STATUS_INVALID

STEP 4: 预检结果校验
  ├─ SELECT check_result FROM pre_check_record WHERE appointment_id = #{appointmentId}
  └─ check_result != 'PASS' → 返回 5001 REGISTER_STATUS_INVALID（附带"预检未通过"信息）

STEP 5: 核实通过（本接口仅校验，不写 register_queue）
  └─ 返回核实通过信息，前端可继续进入批次分配步骤
```

### 5.5 数据操作（SQL级）

```sql
-- 事务开始（轻量事务，仅锁预约行做校验）
BEGIN;

-- STEP 2: 锁定预约行
SELECT id, status, vaccine_id, child_id
FROM appointment
WHERE id = #{appointmentId}
FOR UPDATE;

-- STEP 4: 校验预检结果
SELECT check_result
FROM pre_check_record
WHERE appointment_id = #{appointmentId};

COMMIT;
```

### 5.6 状态流转

本接口不推进状态，仅做前置校验。

### 5.7 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 预约不存在 | 1004 | 404 | 返回"预约不存在" |
| 预约状态异常，不能登记 | 5001 | 409 | 显示当前状态，仅"预检通过"可登记 |
| 状态不允许登记 | 5001 | 409 | 返回"当前状态不允许登记" |
| 预检结果未通过 | 5001 | 409 | 显示预检失败，引导联系预检医生 |
| 预检未通过 | 5001 | 409 | 返回"该预约预检未通过，无法登记" |
| 未登录 | 1001 | 401 | 拦截器拦截 |
| 登录已过期 | 1001 | 401 | 清除Token，跳转登录页 |
| 无权限 | 1002 | 403 | 返回"无权限" |
| 并发冲突 | 5006 | 409 | 返回友好提示 |

### 5.8 并发控制

```sql
-- SELECT FOR UPDATE 锁定预约行
-- 防止核实通过后、执行登记前，状态被其他操作篡改
SELECT id, status, vaccine_id, child_id
FROM appointment
WHERE id = #{appointmentId}
FOR UPDATE;
```

### 5.9 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `register.verify` |
| API前缀 | `/api/register/*` — 仅 DOCTOR_REGISTER 可访问 |

---

## 6. F-REGISTER-004 执行登记

### 6.1 功能描述

登记医生确认登记，系统在同一事务内原子完成：FEFO批次分配 → 库存锁定 → 排队号生成 → 登记记录保存 → 预约状态推进（7→8）。这是登记模块的核心接口。

### 6.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 登记医生已登录 | Token 有效 |
| PRE-002 | 预约记录存在 | `appointment.id = #{appointmentId}` |
| PRE-003 | 预约状态为预检通过 | `appointment.status = 7` |
| PRE-004 | 预检结果为通过 | `pre_check_record.check_result = 'PASS'` |
| PRE-005 | 存在可用批次 | FEFO查询返回结果 |

### 6.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| appointmentId | Long | 是 | 请求体 | 预约ID |
| doctorId | Long | 是 | Token 解析 | 当前登录医生ID |
| batchId | Long | 否 | 请求体 | 手动指定批次ID，为空则自动FEFO分配 |

### 6.4 处理流程

```
STEP 1: 参数校验
  ├─ appointmentId 非空且为正整数
  ├─ batchId 如有值则必须为正整数
  → 失败返回 1003 BAD_REQUEST

STEP 2: 锁定预约行，校验状态（事务开始）
  ├─ SELECT id, status, vaccine_id, batch_id FROM appointment WHERE id = #{appointmentId} FOR UPDATE
  ├─ 记录不存在 → 返回 1004 NOT_FOUND，ROLLBACK
  └─ status != 7 → 返回 5001 REGISTER_STATUS_INVALID，ROLLBACK

STEP 3: 预检结果校验
  ├─ SELECT check_result FROM pre_check_record WHERE appointment_id = #{appointmentId}
  └─ check_result != 'PASS' → 返回 5001 REGISTER_STATUS_INVALID，ROLLBACK

STEP 4: FEFO批次分配（见第9节核心算法）
  ├─ IF batchId IS NOT NULL THEN
  │     -- 手动指定批次：校验该批次是否可用
  │     SELECT b.id, b.batch_no, b.expiry_date, s.available_stock
  │     FROM vaccine_batch b JOIN hospital_vaccine_stock s ON b.id = s.batch_id
  │     WHERE b.id = #{batchId} AND b.vaccine_id = #{vaccineId}
  │       AND s.hospital_id = #{hospitalId} AND b.status IN (0, 1)
  │       AND s.available_stock > 0 AND b.expiry_date > NOW()
  │     FOR UPDATE
  │     └─ 无结果 → 返回 5002 REGISTER_BATCH_NOT_AVAILABLE，ROLLBACK
  ├─ ELSE
  │     -- 自动FEFO分配
  │     SELECT b.id, b.batch_no, b.expiry_date, s.available_stock
  │     FROM vaccine_batch b JOIN hospital_vaccine_stock s ON b.id = s.batch_id
  │     WHERE b.vaccine_id = #{vaccineId}
  │       AND s.hospital_id = #{hospitalId} AND b.status IN (0, 1)
  │       AND s.available_stock > 0 AND b.expiry_date > NOW()
  │     ORDER BY b.expiry_date ASC
  │     LIMIT 1
  │     FOR UPDATE
  │     └─ 无结果 → 返回 5002 REGISTER_BATCH_NOT_AVAILABLE，ROLLBACK
  └─ 获取到 batchId, batchNo, expiryDate

STEP 5: 锁定库存（见第10节核心逻辑）
  ├─ UPDATE hospital_vaccine_stock
  │   SET locked_stock = locked_stock + 1
  │   WHERE batch_id = #{batchId} AND available_stock > 0
  ├─ affected_rows = 0 → 返回 5003 REGISTER_LOCK_FAILED，ROLLBACK
  └─ 防超卖：WHERE 条件确保 available_stock > 0

STEP 6: 生成排队号（见第11节核心逻辑）
  ├─ SELECT MAX(CAST(SUBSTRING(queue_no, 2) AS UNSIGNED)) AS max_seq
  │   FROM register_queue WHERE DATE(register_time) = CURDATE()
  ├─ newSeq = IFNULL(max_seq, 0) + 1
  └─ queueNo = 'A' + LPAD(newSeq, 3, '0')

STEP 7: 插入登记排队记录
  └─ INSERT INTO register_queue (appointment_id, register_time, doctor_id, queue_no,
                                  batch_id, batch_no, verify_status, verify_time)
      VALUES (#{appointmentId}, NOW(), #{doctorId}, #{queueNo},
              #{batchId}, #{batchNo}, 1, NOW())

STEP 8: 推进预约状态 7 → 8
  ├─ UPDATE appointment
  │   SET status = 8, batch_id = #{batchId},
  > current_window 取值参见 REQ-GLOBAL §2.1 预约状态枚举（所在窗口列）和 §2.4 current_window 设置规则。
  │       current_window = #{windowCode}, update_time = NOW()
  │   WHERE id = #{appointmentId}
  └─ 返回登记成功信息（含排队号、批次信息）

STEP 9: 事务提交
  └─ COMMIT
```

### 6.5 数据操作（SQL级）

```sql
-- ============================================
-- 事务开始 — 这是登记核心事务（T5）
-- 隔离级别：READ_COMMITTED
-- 超时时间：30秒
-- ============================================
BEGIN;

-- STEP 2: 锁定预约行，校验状态
SELECT id, status, vaccine_id, batch_id
FROM appointment
WHERE id = #{appointmentId}
FOR UPDATE;
-- assert: status = 7

-- STEP 3: 预检结果校验
SELECT check_result
FROM pre_check_record
WHERE appointment_id = #{appointmentId};
-- assert: check_result = 'PASS'

-- STEP 4: FEFO批次分配（自动模式）
SELECT b.id, b.batch_no, b.expiry_date, s.available_stock
FROM vaccine_batch b
JOIN hospital_vaccine_stock s ON b.id = s.batch_id
WHERE b.vaccine_id = #{vaccineId}
  AND s.hospital_id = #{hospitalId}
  AND b.status IN (0, 1)
  AND s.available_stock > 0
  AND b.expiry_date > NOW()
ORDER BY b.expiry_date ASC
LIMIT 1
FOR UPDATE;

-- STEP 5: 锁定库存（防超卖核心）
UPDATE hospital_vaccine_stock
SET locked_stock = locked_stock + 1
WHERE batch_id = #{batchId}
  AND available_stock > 0;
-- assert: affected_rows = 1

-- STEP 6: 生成排队号（获取当天最大序号）
SELECT MAX(CAST(SUBSTRING(queue_no, 2) AS UNSIGNED)) AS max_seq
FROM register_queue
WHERE DATE(register_time) = CURDATE();

-- STEP 7: 插入登记排队记录
INSERT INTO register_queue (
    appointment_id, register_time, doctor_id,
    queue_no, batch_id, batch_no,
    verify_status, verify_time, create_time, update_time
) VALUES (
    #{appointmentId}, NOW(), #{doctorId},
    #{queueNo}, #{batchId}, #{batchNo},
    1, NOW(), NOW(), NOW()
);

-- STEP 8: 推进预约状态 7 → 8
UPDATE appointment
SET status = 8,
    batch_id = #{batchId},
    current_window = #{windowCode},
    update_time = NOW()
WHERE id = #{appointmentId};
> current_window 取值参见 REQ-GLOBAL §2.1 预约状态枚举（所在窗口列）和 §2.4 current_window 设置规则。

-- ============================================
-- 事务提交
-- ============================================
COMMIT;
```

### 6.6 事务边界

> 规则来源：REQ-GLOBAL §6.1 事务规范（T5 登记+批次锁定，隔离级别 READ_COMMITTED，超时 30 秒）及 §6.2 事务配置（回滚策略：遇 RuntimeException / BusinessException 回滚）

```
┌──────────────────────────────────────────────────────────────────────┐
│  事务 T5（登记+批次锁定）                                            │
│                                                                      │
│  BEGIN;                                                              │
│    ├─ SELECT appointment FOR UPDATE          ← 加行锁               │
│    ├─ SELECT pre_check_record                 ← 预检校验            │
│    ├─ SELECT vaccine_batch FOR UPDATE         ← FEFO加行锁          │
│    ├─ UPDATE hospital_vaccine_stock           ← 锁库存（防超卖）    │
│    ├─ SELECT register_queue                   ← 获取排队号序号      │
│    ├─ INSERT register_queue                   ← 插入登记记录        │
│    └─ UPDATE appointment                      ← 状态7→8             │
│  COMMIT;                                                             │
│                                                                      │
│  涉及表：appointment, pre_check_record, vaccine_batch,               │
│          hospital_vaccine_stock, register_queue                      │
│                                                                      │
│  回滚条件：任何一步抛出 RuntimeException / BusinessException          │
│  隔离级别：READ_COMMITTED                                             │
│  超时时间：30秒                                                       │
└──────────────────────────────────────────────────────────────────────┘
```

### 6.7 状态流转

| 操作 | 前置状态 | 目标状态 | 触发角色 |
|------|----------|----------|----------|
| 执行登记 | 7（预检通过） | 8（已登记） | DOCTOR_REGISTER |

### 6.8 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 预约不存在 | 1004 | 404 | 事务回滚，返回"预约不存在" |
| 预约状态异常，不能登记 | 5001 | 409 | 显示当前状态，仅"预检通过"可登记 |
| 状态不允许登记 | 5001 | 409 | 事务回滚，返回"当前状态不允许登记" |
| 预检结果未通过 | 5001 | 409 | 显示预检失败，引导联系预检医生 |
| 预检未通过 | 5001 | 409 | 事务回滚，返回"该预约预检未通过" |
| 无可用批次 | 1904 | 400 | 引导联系库管或重新预约 |
| 无可用批次 | 5002 | 400 | 事务回滚，返回"该疫苗暂无可用批次" |
| 批次状态异常 | 1903 | 404 | 显示状态，引导切换批次 |
| 批次库存不足 | 1904 | 409 | 显示库存信息，引导切换批次 |
| 批次已过期 | 5002 | 400 | 显示过期日期，引导切换批次 |
| 库存锁定失败 | 5003 | 500 | 事务回滚，重试或更换批次 |
| 排队号重复 | 5004 | 409 | 事务回滚，重试生成 |
| 未登录 | 1001 | 401 | 拦截器拦截 |
| 无权限 | 1002 | 403 | 返回"无权限" |
| 参数格式错误 | 1003 | 400 | 返回具体字段错误 |
| 参数校验失败：{field_name} | 1003 | 400 | 返回具体字段错误 |
| 登录已过期 | 1001 | 401 | 清除Token，跳转登录页 |
| 系统异常 | 1007 | 500 | 事务回滚，记录日志 |
| 并发冲突 | 5006 | 409 | 返回友好提示 |

### 6.9 异常恢复机制

**库存锁定失败恢复：** 锁库存失败时（available_stock = 0 或无可用批次），预约保持 status=7（预检通过），事务回滚不修改任何数据。用户可等待库存补充后重新排队登记，无需重新预约或签到。

```
锁库存失败恢复流程：
  1. 事务开始
  2. SELECT FOR UPDATE 锁定预约行
  3. FEFO 查询可用批次 → 无结果
  4. 返回 1904 STOCK_INSUFFICIENT + "库存不足，请稍后重新排队"
  5. 事务 ROLLBACK（appointment 和 hospital_vaccine_stock 均未修改）
  6. 用户状态：仍为 status=7（预检通过），可重新进入登记队列
```

### 6.10 并发控制

> 规则来源：REQ-GLOBAL §7 并发控制策略（§7.1 并发风险场景、§7.2 SELECT FOR UPDATE 核心策略、§7.4 重试策略）

#### 6.9.1 防超卖机制（核心）

```sql
-- 1. SELECT FOR UPDATE 锁定预约行（防止重复登记）
SELECT id, status, vaccine_id, batch_id
FROM appointment
WHERE id = #{appointmentId}
FOR UPDATE;

-- 2. SELECT FOR UPDATE 锁定批次行（防止并发分配同一批次）
SELECT b.id, b.batch_no, b.expiry_date, s.available_stock
FROM vaccine_batch b
JOIN hospital_vaccine_stock s ON b.id = s.batch_id
WHERE b.vaccine_id = #{vaccineId}
  AND s.hospital_id = #{hospitalId}
  AND b.status IN (0, 1)
  AND s.available_stock > 0
  AND b.expiry_date > NOW()
ORDER BY b.expiry_date ASC
LIMIT 1
FOR UPDATE;

-- 3. UPDATE WHERE 条件二次校验（防止锁间隔导致超卖）
UPDATE hospital_vaccine_stock
SET locked_stock = locked_stock + 1
WHERE batch_id = #{batchId}
  AND available_stock > 0;  -- 关键：available_stock > 0 确保不超卖

-- 4. affected_rows 校验（确认UPDATE成功）
-- IF affected_rows = 0 THEN THROW REGISTER_LOCK_FAILED, ROLLBACK;
```

#### 6.9.2 并发场景分析

| 场景 | 防护手段 | 结果 |
|------|----------|------|
| 两个医生同时对同一预约登记 | `SELECT appointment FOR UPDATE` 行锁 | 第二个等待锁释放后读到 status=8，状态校验拒绝 |
| 两个医生同时FEFO分配同一批次 | `SELECT vaccine_batch FOR UPDATE` 行锁 | 第二个等待锁释放后 available_stock 已减少，可能分配到下一批次 |
| FEFO查询到批次但锁定前被抢走 | `UPDATE WHERE available_stock > 0` + affected_rows 校验 | UPDATE 失败，事务回滚，触发重试 |
| 排队号并发重复 | 事务内 `SELECT MAX` + 事务隔离 | 同一事务内序号唯一，事务提交后下一个事务读到已更新的序号 |

#### 6.9.3 重试策略

| 操作 | 最大重试次数 | 重试间隔 | 退避策略 |
|------|-------------|----------|----------|
| FEFO批次分配+锁库存 | 3 | 100ms, 200ms, 400ms | 指数退避 |
| 排队号生成 | 1 | 即时 | 不重试，事务内保证唯一 |

### 6.10 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `register.batch.assign` + `register.queue.manage` |
| API前缀 | `/api/register/*` — 仅 DOCTOR_REGISTER 可访问 |

---

## 7. F-REGISTER-005 查询可用批次

### 7.1 功能描述

查询某疫苗的可用批次列表，按有效期升序排列，供登记医生在更换批次时使用。本接口为只读查询，不加锁。

### 7.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 登记医生已登录 | Token 有效 |

### 7.3 输入参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| vaccineId | Long | 是 | 疫苗ID |
| hospitalId | Long | 是 | 医院ID（固定为1） |

### 7.4 处理流程

```
STEP 1: 参数校验
  ├─ vaccineId 非空且为正整数
  ├─ hospitalId 非空且为正整数
  → 失败返回 1003 BAD_REQUEST

STEP 2: 查询可用批次列表
  ├─ SELECT b.id, b.batch_no, b.expiry_date, s.available_stock, s.locked_stock, b.status
  │   FROM vaccine_batch b
  │   JOIN hospital_vaccine_stock s ON b.id = s.batch_id
  │   WHERE b.vaccine_id = #{vaccineId}
  │     AND s.hospital_id = #{hospitalId}
  │     AND b.status IN (0, 1)
  │     AND s.available_stock > 0
  │     AND b.expiry_date > NOW()
  │   ORDER BY b.expiry_date ASC
  └─ 返回可用批次列表
```

### 7.5 数据操作（SQL级）

```sql
SELECT
    b.id                AS batch_id,
    b.batch_no          AS batch_no,
    b.expiry_date       AS expiry_date,
    b.production_date   AS production_date,
    b.status            AS batch_status,
    s.available_stock   AS available_stock,
    s.locked_stock      AS locked_stock,
    v.name              AS vaccine_name,
    v.manufacturer      AS manufacturer
FROM vaccine_batch b
JOIN hospital_vaccine_stock s ON b.id = s.batch_id
JOIN vaccine v ON b.vaccine_id = v.id
WHERE b.vaccine_id = #{vaccineId}
  AND s.hospital_id = #{hospitalId}
  AND b.status IN (0, 1)
  AND s.available_stock > 0
  AND b.expiry_date > NOW()
ORDER BY b.expiry_date ASC;
```

### 7.6 状态流转

本接口为只读查询，不涉及状态变更。

### 7.7 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 无可用批次 | 1904 | 400 | 引导联系库管或重新预约 |
| 参数格式错误 | 1003 | 400 | 返回具体字段错误 |
| 参数校验失败：{field_name} | 1003 | 400 | 返回具体字段错误 |
| 未登录 | 1001 | 401 | 拦截器拦截 |
| 无权限 | 1002 | 403 | 返回"无权限" |

### 7.8 并发控制

本接口为只读查询，不加锁。

### 7.9 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `batch.view` |
| API前缀 | `/api/register/*` — 仅 DOCTOR_REGISTER 可访问 |

---

## 8. F-REGISTER-006 更换批次

### 8.1 功能描述

登记医生在登记前更换已分配的批次，系统在同一事务内释放旧批次锁定库存、锁定新批次库存。

### 8.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 登记医生已登录 | Token 有效 |
| PRE-002 | 预约记录存在 | `appointment.id = #{appointmentId}` |
| PRE-003 | 预约状态为预检通过 | `appointment.status = 7` |
| PRE-004 | 新批次可用 | FEFO查询返回结果 |

### 8.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| appointmentId | Long | 是 | 请求体 | 预约ID |
| oldBatchId | Long | 是 | 请求体 | 原批次ID |
| newBatchId | Long | 是 | 请求体 | 新批次ID |
| doctorId | Long | 是 | Token 解析 | 当前登录医生ID |

### 8.4 处理流程

```
STEP 1: 参数校验
  ├─ appointmentId, oldBatchId, newBatchId 均非空且为正整数
  ├─ oldBatchId != newBatchId
  → 失败返回 1003 BAD_REQUEST

STEP 2: 锁定预约行，校验状态（事务开始）
  ├─ SELECT id, status, vaccine_id, batch_id FROM appointment WHERE id = #{appointmentId} FOR UPDATE
  ├─ 记录不存在 → 返回 1004 NOT_FOUND，ROLLBACK
  └─ status != 7 → 返回 5001 REGISTER_STATUS_INVALID，ROLLBACK

STEP 3: 锁定旧批次行
  ├─ SELECT available_stock, locked_stock FROM hospital_vaccine_stock WHERE batch_id = #{oldBatchId} FOR UPDATE
  └─ 记录不存在 → 返回 5002 REGISTER_BATCH_NOT_AVAILABLE，ROLLBACK

STEP 4: 锁定新批次行，校验可用性
  ├─ SELECT b.id, b.batch_no, b.expiry_date, s.available_stock
  │   FROM vaccine_batch b JOIN hospital_vaccine_stock s ON b.id = s.batch_id
  │   WHERE b.id = #{newBatchId} AND b.vaccine_id = #{vaccineId}
  │     AND s.hospital_id = #{hospitalId} AND b.status IN (0, 1)
  │     AND s.available_stock > 0 AND b.expiry_date > NOW()
  │   FOR UPDATE
  └─ 无结果 → 返回 5002 REGISTER_BATCH_NOT_AVAILABLE，ROLLBACK

STEP 5: 释放旧批次锁定库存
  ├─ UPDATE hospital_vaccine_stock
  │   SET locked_stock = locked_stock - 1
  │   WHERE batch_id = #{oldBatchId} AND locked_stock > 0
  ├─ affected_rows = 0 → 记录日志（异常情况），继续执行
  └─ 确保不出现 locked_stock < 0

STEP 6: 锁定新批次库存
  ├─ UPDATE hospital_vaccine_stock
  │   SET locked_stock = locked_stock + 1
  │   WHERE batch_id = #{newBatchId} AND available_stock > 0
  └─ affected_rows = 0 → 返回 5003 REGISTER_LOCK_FAILED，ROLLBACK

STEP 7: 事务提交
  └─ COMMIT
  └─ 返回新批次信息
```

### 8.5 数据操作（SQL级）

```sql
-- 事务开始
BEGIN;

-- STEP 2: 锁定预约行
SELECT id, status, vaccine_id, batch_id
FROM appointment
WHERE id = #{appointmentId}
FOR UPDATE;

-- STEP 3: 锁定旧批次库存行
SELECT available_stock, locked_stock
FROM hospital_vaccine_stock
WHERE batch_id = #{oldBatchId}
FOR UPDATE;

-- STEP 4: 锁定新批次行，校验可用性
SELECT b.id, b.batch_no, b.expiry_date, s.available_stock
FROM vaccine_batch b
JOIN hospital_vaccine_stock s ON b.id = s.batch_id
WHERE b.id = #{newBatchId}
  AND b.vaccine_id = #{vaccineId}
  AND s.hospital_id = #{hospitalId}
  AND b.status IN (0, 1)
  AND s.available_stock > 0
  AND b.expiry_date > NOW()
FOR UPDATE;

-- STEP 5: 释放旧批次锁定库存
UPDATE hospital_vaccine_stock
SET locked_stock = locked_stock - 1
WHERE batch_id = #{oldBatchId}
  AND locked_stock > 0;

-- STEP 6: 锁定新批次库存
UPDATE hospital_vaccine_stock
SET locked_stock = locked_stock + 1
WHERE batch_id = #{newBatchId}
  AND available_stock > 0;

COMMIT;
```

### 8.6 状态流转

本接口不推进预约状态，仅更换批次锁定。

### 8.7 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 预约不存在 | 1004 | 404 | 事务回滚 |
| 预约状态异常，不能登记 | 5001 | 409 | 显示当前状态，仅"预检通过"可登记 |
| 状态不允许 | 5001 | 409 | 事务回滚 |
| 新批次不可用 | 5002 | 400 | 事务回滚 |
| 批次状态异常 | 1903 | 404 | 显示状态，引导切换批次 |
| 批次库存不足 | 1904 | 409 | 显示库存信息，引导切换批次 |
| 批次已过期 | 5002 | 400 | 显示过期日期，引导切换批次 |
| 库存锁定失败 | 5003 | 500 | 事务回滚 |
| 未登录 | 1001 | 401 | 拦截器拦截 |
| 无权限 | 1002 | 403 | 返回"无权限" |
| 参数校验失败：{field_name} | 1003 | 400 | 返回具体字段错误 |
| 并发冲突 | 5006 | 409 | 返回友好提示 |

### 8.8 并发控制

```sql
-- 更换批次涉及2个库存行的锁，必须按固定顺序加锁防止死锁：
-- 1. 先锁预约行
-- 2. 先锁旧批次库存行
-- 3. 再锁新批次库存行
-- 所有锁在同一事务内获取，按此顺序避免死锁
```

### 8.9 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `register.batch.assign` |
| API前缀 | `/api/register/*` — 仅 DOCTOR_REGISTER 可访问 |

---

## 9. 核心算法：FEFO批次分配

> 参见 REQ-GLOBAL §8.4 FEFO批次分配规则

### 9.1 算法定义

**FEFO（First Expired First Out）** — 先过期先出，优先使用有效期最早的批次，降低疫苗过期浪费。

### 9.2 算法步骤

```
输入：vaccineId, hospitalId
输出：batchId, batchNo, expiryDate

STEP 1: 查询所有满足条件的批次
  条件：
    a. batch.vaccine_id = vaccineId           -- 疫苗匹配
    b. stock.hospital_id = hospitalId          -- 医院匹配
    c. batch.status IN (0, 1)                  -- 正常或临期
    d. stock.available_stock > 0               -- 有可用库存
    e. batch.expiry_date > NOW()               -- 未过期

STEP 2: 按 expiry_date 升序排列
  -- 有效期最早的排在最前

STEP 3: 取第一条（LIMIT 1）
  -- 即为 FEFO 分配结果

STEP 4: 加行锁（FOR UPDATE）
  -- 防止并发分配到同一批次
```

### 9.3 算法SQL

```
FEFO 排序规则（完整）：
  ├─ 优先级1：expiry_date ASC（先过期先出）
  ├─ 优先级2：batch_no ASC（相同过期日期时，批次号小的优先，即先入库先出）
  └─ 优先级3：id ASC（作为最终确定性排序，保证结果稳定）
```

```sql
SELECT
    b.id            AS batch_id,
    b.batch_no      AS batch_no,
    b.expiry_date   AS expiry_date,
    s.available_stock AS available_stock
FROM vaccine_batch b
JOIN hospital_vaccine_stock s ON b.id = s.batch_id
WHERE b.vaccine_id = #{vaccineId}
  AND s.hospital_id = #{hospitalId}
  AND b.status IN (0, 1)
  AND s.available_stock > 0
  AND b.expiry_date > NOW()
ORDER BY b.expiry_date ASC, b.batch_no ASC, b.id ASC
LIMIT 1
FOR UPDATE;
```

### 9.4 算法特性

| 特性 | 说明 |
|------|------|
| 正确性 | ORDER BY expiry_date ASC 保证先过期先出 |
| 并发安全 | FOR UPDATE 行锁防止多个医生同时分配同一批次 |
| 容错性 | 无结果时返回 5002 REGISTER_BATCH_NOT_AVAILABLE |
| 实时性 | 每次分配实时查询，不依赖缓存 |

### 9.5 FEFO索引建议

```sql
-- 加速FEFO查询的联合索引
CREATE INDEX idx_fefo_query
ON vaccine_batch (vaccine_id, status, expiry_date);

CREATE INDEX idx_stock_fefo
ON hospital_vaccine_stock (hospital_id, batch_id, available_stock);
```

---

## 10. 核心逻辑：锁库存

> 参见 REQ-GLOBAL §8 库存双阶段模型

### 10.1 锁库存定义

登记时锁定1支疫苗库存，将 `locked_stock` 加1，`available_stock` 不变。这是库存双阶段管理的第一阶段（第二阶段在接种时扣减）。

### 10.2 锁库存不变量

```
锁库存操作前后必须满足：

1. available_stock >= 0           -- 可用库存非负
2. locked_stock >= 0              -- 锁定库存非负
3. available_stock >= locked_stock -- 可用 >= 锁定（逻辑不变量）
```

### 10.3 锁库存SQL

```sql
UPDATE hospital_vaccine_stock
SET locked_stock = locked_stock + 1
WHERE batch_id = #{batchId}
  AND available_stock > 0;
```

**防超卖关键：** `WHERE available_stock > 0` 确保只有在有可用库存时才锁定。如果 `available_stock = 0`，affected_rows = 0，触发回滚。

### 10.4 锁库存验证

```java
int affected = stockMapper.lockStock(batchId);
if (affected == 0) {
    throw new BusinessException(5003, "库存锁定失败，请重试或更换批次");
}
```

### 10.5 锁库存与扣减库存的关系

```
登记阶段（本模块）：
  locked_stock  += 1
  available_stock 不变

接种阶段（VACCINATE模块）：
  available_stock -= 1
  locked_stock  -= 1

取消/过期释放（APPOINTMENT模块）：
  locked_stock  -= 1
  available_stock 不变

销毁（STOCK模块）：
  available_stock -= N
  locked_stock = 0（清空）
```

---

## 11. 核心逻辑：排队号生成

### 11.1 排队号格式

| 格式 | 示例 | 说明 |
|------|------|------|
| A + 3位数字 | A001, A002, ..., A999 | 字母A前缀 + 当日自增序号 |

### 11.2 生成规则

1. 格式：`A` + `LPAD(序号, 3, '0')`
2. 序号每日从 001 开始
3. 序号当日递增，次日重置为 001
4. 序号在事务内生成，保证唯一

### 11.3 生成SQL

```sql
-- STEP 1: 获取当天最大序号
SELECT MAX(CAST(SUBSTRING(queue_no, 2) AS UNSIGNED)) AS max_seq
FROM register_queue
WHERE DATE(register_time) = CURDATE();

-- STEP 2: 计算新序号
-- newSeq = IFNULL(max_seq, 0) + 1

-- STEP 3: 拼接排队号
-- queueNo = CONCAT('A', LPAD(newSeq, 3, '0'))
```

### 11.4 生成伪代码

```java
private String generateQueueNo() {
    // 1. 查询当天最大序号
    Integer maxSeq = registerQueueMapper.selectMaxSeqToday();
    // 2. 计算新序号（当天第一条记录时 maxSeq 为 null）
    int newSeq = (maxSeq == null ? 0 : maxSeq) + 1;
    // 3. 拼接排队号
    return String.format("A%03d", newSeq);
}
```

### 11.5 唯一性保证

排队号在事务内生成。由于事务内持有预约行锁和批次行锁，同一时刻只有一个登记事务在执行，因此排队号不会重复。

---

## 12. register_queue表操作规范

### 12.1 表结构

```sql
CREATE TABLE register_queue (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    appointment_id  BIGINT       NOT NULL COMMENT '预约ID，外键→appointment.id',
    register_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登记时间',
    doctor_id       BIGINT       NOT NULL COMMENT '登记医生ID，外键→sys_user.id',
    queue_no        VARCHAR(10)  NOT NULL COMMENT '排队号，格式A+3位数字，当天唯一',
    batch_id        BIGINT       NOT NULL COMMENT '分配批次ID，外键→vaccine_batch.id',
    batch_no        VARCHAR(50)  NOT NULL COMMENT '分配批次号',
    verify_status   TINYINT      NOT NULL DEFAULT 0 COMMENT '核实状态：0=未核实,1=已核实',
    verify_time     DATETIME     NULL COMMENT '核实时间',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_appointment (appointment_id),
    KEY idx_queue_date (register_time),
    KEY idx_doctor (doctor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登记排队表';
```

### 12.2 操作规范

| 操作 | SQL | 时机 | 事务 |
|------|-----|------|------|
| 插入 | `INSERT INTO register_queue (...)` | 执行登记（F-REGISTER-004 STEP 7） | T5 |
| 插入 | `INSERT INTO stock_transfer_log (...)` | 创建调拨（F-REGISTER-012） | T5 |
| 查询 | `SELECT ... FROM stock_transfer_log` | 查询调拨记录（F-REGISTER-014） | - |
| 插入 | `INSERT INTO batch_dispose_log (...)` | 销毁批次（F-REGISTER-015） | T5 |
| 查询 | `SELECT ... FROM stock_alert_log` | 查询批次预警（F-REGISTER-016） | - |
| 查询排队号序号 | `SELECT MAX(...) FROM register_queue WHERE DATE(register_time) = CURDATE()` | 执行登记（F-REGISTER-004 STEP 6） | T5 |
| 查询详情 | `SELECT * FROM register_queue WHERE appointment_id = ?` | 查询登记详情（F-REGISTER-002 STEP 4） | 无事务 |
| 查询队列 | `SELECT * FROM register_queue WHERE DATE(register_time) = ?` | 查看待登记队列 | 无事务 |

### 12.3 约束

| 约束 | 说明 |
|------|------|
| appointment_id 唯一 | 一个预约只能有一条登记记录（`UNIQUE KEY uk_appointment`） |
| queue_no 当天唯一 | 同一天内排队号不重复（业务层保证） |
| batch_id 非空 | 登记时必须分配批次 |
| verify_status 默认1 | 本系统设计中，执行登记即表示已核实 |

---

## 13. 状态非法校验

### 13.1 校验原则

> **任何状态变更操作，执行前必须先锁定预约行（`SELECT FOR UPDATE`），校验当前状态是否在允许的前置状态列表中。非法状态一律拒绝操作。**

### 13.2 本模块操作的状态校验矩阵

> 规则来源：REQ-GLOBAL §3.1 流转矩阵（目标状态 8 的允许前置状态为 7，触发角色 DOCTOR_REGISTER）及 §3.3 各模块可操作状态汇总（REGISTER 可写目标状态：登记→8）

| 操作 | 允许的前置状态 | 非法状态 | 拒绝错误码 |
|------|---------------|----------|-----------|
| 核实信息 | 7（预检通过） | 1,2,3,4,6,8,9,10 | 5001 REGISTER_STATUS_INVALID |
| 执行登记 | 7（预检通过） | 1,2,3,4,6,8,9,10 | 5001 REGISTER_STATUS_INVALID |
| 更换批次 | 7（预检通过） | 1,2,3,4,6,8,9,10 | 5001 REGISTER_STATUS_INVALID |

### 13.3 状态校验伪代码

```java
/**
 * 登记前 — 状态校验
 */
public void validateRegister(Long appointmentId) {
    // 1. 加行锁查询
    Appointment appt = appointmentMapper.selectForUpdate(appointmentId);
    if (appt == null) {
        throw new BusinessException(1004, "预约不存在");
    }

    // 2. 状态校验
    if (appt.getStatus() != 7) {
        throw new BusinessException(5001, "当前状态不允许登记，当前状态：" + appt.getStatus());
    }

    // 3. 预检结果校验
    PreCheckRecord pcr = preCheckRecordMapper.selectByAppointmentId(appointmentId);
    if (pcr == null || !"PASS".equals(pcr.getCheckResult())) {
        throw new BusinessException(5001, "该预约预检未通过，无法登记");
    }
}
```

### 13.4 非法状态流转的错误消息

| 当前状态 | 尝试操作 | 错误码 | 错误消息 |
|----------|----------|--------|----------|
| 1（已预约） | 登记 | 5001 | 该预约尚未预检，无法登记 |
| 2（已完成） | 登记 | 5001 | 该预约已完成，无法登记 |
| 3（已取消） | 登记 | 5001 | 该预约已取消，无法登记 |
| 4（已过期） | 登记 | 5001 | 该预约已过期，无法登记 |
| 6（已签到） | 登记 | 5001 | 该预约尚未预检，无法登记 |
| 8（已登记） | 登记 | 5001 | 该预约已登记，无需重复登记 |
| 9（预检失败） | 登记 | 5001 | 该预约预检未通过，无法登记 |
| 10（留观中） | 登记 | 5001 | 该预约正在留观中，无法登记 |

---

## 14. 权限控制汇总

### 14.1 功能权限矩阵

| 功能编号 | 权限编码 | 允许角色 | API路径 |
|----------|----------|----------|---------|
| F-REGISTER-001 | `appointment.view.queue` | DOCTOR_REGISTER | `GET /api/register/queue` |
| F-REGISTER-002 | `register.verify` | DOCTOR_REGISTER | `GET /api/register/detail/{appointmentId}` |
| F-REGISTER-003 | `register.verify` | DOCTOR_REGISTER | `POST /api/register/verify` |
| F-REGISTER-004 | `register.batch.assign` + `register.queue.manage` | DOCTOR_REGISTER | `POST /api/register/save` |
| F-REGISTER-005 | `batch.view` | DOCTOR_REGISTER | `GET /api/register/batch/available` |
| F-REGISTER-006 | `register.batch.assign` | DOCTOR_REGISTER | `POST /api/register/batch/switch` |

### 14.2 数据权限规则

| 角色 | 数据范围 | 实现方式 |
|------|----------|----------|
| DOCTOR_REGISTER | 自己登记过的预约 + 待登记的预约（status=7） | `WHERE doctor_id = #{currentDoctorId} OR status = 7` |

### 14.3 权限校验流程

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
状态校验（Service 层 SELECT FOR UPDATE + status 判断）
  │
  ├─ 状态非法 → 返回 { code: 5001, message: "当前状态不允许登记" }
  │
  ▼
执行业务操作
  │
  ▼
返回响应
```

---

## 15. 补充错误码（跨模块引用）

> 以下错误码来自 PRD-REGISTER §8，功能归属 STOCK 模块（见 §1.2 模块边界），此处仅做登记引用，详细实现见 REQ-STOCK。

### 15.1 调拨功能异常

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 调拨位置相同 | 4001 | 400 | 引导重新选择位置 |
| 调出位置库存不足 | 4002 | 400 | 显示库存信息，引导减少数量或选其他批次 |

### 15.2 批次销毁功能异常

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 批次已销毁 | 4003 | 400 | 引导选择其他批次 |
| 销毁数量超过总库存 | 4004 | 400 | 显示库存信息，引导修正数量 |
| 销毁原因未填写 | 4005 | 400 | 引导填写原因 |

---

## 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| V1.0 | 2026-04-02 | 初始版本，基于 PRD-REGISTER V1.1 / REQ-GLOBAL V1.0 生成 |
| V1.1 | 2026-04-03 | V3 评审修复：(1) 错误码对齐 GLOBAL §4；(2) 业务规则修正（FEFO/锁库存/并发控制）；(3) 版本号/依赖版本对齐 |
| V1.2 | 2026-04-03 | REQ 评审修复：(1) 文档头上游依赖同步至 REQ-GLOBAL V1.3 |

---

**文档结束**
