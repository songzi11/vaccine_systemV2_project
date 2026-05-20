# 库存管理模块 — 研发需求文档

**文档编号:** REQ-STOCK-001
**版本:** V1.2
**状态:** 正式发布
**日期:** 2026-04-03
**上游依赖:** PRD-STOCK V2.2 / REQ-GLOBAL V1.3

---

## 目录

1. [模块定位与边界](#1-模块定位与边界)
2. [功能清单](#2-功能清单)
3. [批次表结构设计](#3-批次表结构设计)
4. [库存查询逻辑](#4-库存查询逻辑)
5. [调拨流程](#5-调拨流程)
6. [库存预警规则](#6-库存预警规则)
7. [数据一致性说明](#7-数据一致性说明)
8. [权限控制汇总](#8-权限控制汇总)

---

## 1. 模块定位与边界

### 1.1 核心定位

STOCK 是库存资源层，仅负责：
- **库存查询** — 按疫苗维度/批次维度查询库存汇总与明细
- **批次管理** — 批次列表查询、批次详情查看、批次状态维护
- **调拨** — 医院内部不同位置之间的库存调拨
- **预警** — 低库存/临期/过期批次的预警检测与处理

### 1.2 不包含内容（明确禁止）

| 禁止范围 | 归属模块 | 说明 |
|----------|----------|------|
| **锁库存**（locked_stock += 1） | REGISTER | 登记时锁定库存 |
| **扣库存**（available_stock -= 1, locked_stock -= 1） | VACCINATE | 接种时扣减库存 |
| **释放锁定库存**（locked_stock -= 1） | APPOINTMENT | 取消/过期预约时释放 |
| 疫苗目录管理 | ADMIN | 疫苗增删改查 |
| 库存统计分析（管理后台） | ADMIN | POST /api/stats/stock |

### 1.3 本模块状态操作范围

```
本模块操作的不是预约状态，而是批次状态：

批次状态（vaccine_batch.status）：
  0 = 正常
  1 = 临期（自动标记）
  2 = 过期（自动标记）
  3 = 已销毁（本模块操作）

本模块可写状态：
  正常(0) → 已销毁(3)    销毁操作
  临期(1) → 已销毁(3)    销毁操作
  过期(2) → 已销毁(3)    销毁操作
  正常(0) → 临期(1)      系统自动扫描
  临期(1) → 过期(2)      系统自动扫描

本模块不操作预约状态（appointment.status）
```

---

## 2. 功能清单

| 编号 | 功能名称 | API | 角色 | 优先级 |
|------|----------|-----|------|--------|
| F-STOCK-001 | 查询库存汇总 | `GET /api/stock/summary` | DOCTOR_STOCK | P0 |
| F-STOCK-002 | 查询批次库存 | `GET /api/stock/batch/{batchId}/stock` | DOCTOR_STOCK | P0 |
| F-STOCK-003 | 查询批次列表 | `GET /api/stock/batch/list` | DOCTOR_STOCK | P0 |
| F-STOCK-004 | 查询批次详情 | `GET /api/stock/batch/{batchId}` | DOCTOR_STOCK | P0 |
| F-STOCK-005 | 创建并执行调拨 | `POST /api/stock/transfer/create` | DOCTOR_STOCK | P1 |
| F-STOCK-006 | 查询调拨记录 | `GET /api/stock/transfer/list` | DOCTOR_STOCK | P1 |
| F-STOCK-007 | 销毁批次 | `POST /api/stock/batch/dispose` | DOCTOR_STOCK | P1 |
| F-STOCK-008 | 查询批次预警 | `GET /api/stock/alert/list` | DOCTOR_STOCK | P1 |
| F-STOCK-009 | 标记预警已处理 | `POST /api/stock/alert/{alertId}/handle` | DOCTOR_STOCK | P2 |
| F-STOCK-010 | 查询库存统计 | `GET /api/stock/statistics` | DOCTOR_BUSINESS_ADMIN | P2 |
| F-STOCK-011 | 确认调拨 | `POST /api/stock/transfer/confirm` | DOCTOR_STOCK | P1 | 已合并至 F-STOCK-005，见 §5.2 |

---

> **设计说明：** PRD-STOCK 中的 F-STOCK-005（创建调拨单）和 F-STOCK-006（确认调拨）在研发层面合并为 F-STOCK-005（创建并执行调拨），创建即执行，无待确认状态，避免引入额外的事务一致性问题。

---

## 3. 批次表结构设计

### 3.1 vaccine_batch — 疫苗批次表

```sql
CREATE TABLE vaccine_batch (
    id                BIGINT       AUTO_INCREMENT PRIMARY KEY,
    batch_no          VARCHAR(50)  NOT NULL COMMENT '批次号，唯一标识',
    vaccine_id        BIGINT       NOT NULL COMMENT '疫苗ID，外键→vaccine.id',
    manufacturer      VARCHAR(100) NOT NULL COMMENT '生产厂家',
    production_date   DATE         NOT NULL COMMENT '生产日期',
    expiry_date       DATE         NOT NULL COMMENT '有效期至',
    warning_days      INT          NOT NULL DEFAULT 30 COMMENT '临期预警天数',
    status            TINYINT      NOT NULL DEFAULT 0 COMMENT '批次状态：0=正常,1=临期,2=过期,3=已销毁',
    create_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_batch_no (batch_no),
    KEY idx_vaccine_status_expiry (vaccine_id, status, expiry_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='疫苗批次表';
```

### 3.2 hospital_vaccine_stock — 医院库存表（按批次）

```sql
CREATE TABLE hospital_vaccine_stock (
    id                BIGINT       AUTO_INCREMENT PRIMARY KEY,
    hospital_id       BIGINT       NOT NULL DEFAULT 1 COMMENT '医院ID，外键→hospital_info.id，固定为1',
    batch_id          BIGINT       NOT NULL COMMENT '批次ID，外键→vaccine_batch.id',
    available_stock   INT          NOT NULL DEFAULT 0 COMMENT '可用库存（未锁定）',
    locked_stock      INT          NOT NULL DEFAULT 0 COMMENT '锁定库存（已登记未接种）',
    create_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_hospital_batch (hospital_id, batch_id),
    KEY idx_batch (batch_id),
    KEY idx_available (available_stock)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医院库存表（按批次）';
```

### 3.3 hospital_vaccine_summary — 医院疫苗库存汇总表

```sql
CREATE TABLE hospital_vaccine_summary (
    id                  BIGINT       AUTO_INCREMENT PRIMARY KEY,
    hospital_id         BIGINT       NOT NULL DEFAULT 1 COMMENT '医院ID',
    vaccine_id          BIGINT       NOT NULL COMMENT '疫苗ID，外键→vaccine.id',
    total_stock         INT          NOT NULL DEFAULT 0 COMMENT '总库存（所有批次合计）',
    available_stock     INT          NOT NULL DEFAULT 0 COMMENT '可用库存（所有批次合计）',
    warning_threshold   INT          NOT NULL DEFAULT 20 COMMENT '库存预警阈值（百分比）',
    version             INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    create_time         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_hospital_vaccine (hospital_id, vaccine_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医院疫苗库存汇总表';
```

### 3.4 stock_transfer_log — 库存调拨日志表

```sql
CREATE TABLE stock_transfer_log (
    id                BIGINT       AUTO_INCREMENT PRIMARY KEY,
    transfer_no       VARCHAR(50)  NOT NULL COMMENT '调拨号，唯一，格式：TF+日期+序号',
    batch_id          BIGINT       NOT NULL COMMENT '批次ID，外键→vaccine_batch.id',
    from_type         TINYINT      NOT NULL COMMENT '调出类型：0=总仓,1=接种点',
    from_id           BIGINT       NOT NULL COMMENT '调出位置ID',
    to_type           TINYINT      NOT NULL COMMENT '调入类型：0=总仓,1=接种点',
    to_id             BIGINT       NOT NULL COMMENT '调入位置ID',
    quantity          INT          NOT NULL COMMENT '调拨数量',
    operator_id       BIGINT       NOT NULL COMMENT '操作员ID，外键→sys_user.id',
    transfer_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '调拨时间',
    remark            VARCHAR(500) NULL COMMENT '备注',
    create_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_transfer_no (transfer_no),
    KEY idx_batch (batch_id),
    KEY idx_operator (operator_id),
    KEY idx_transfer_time (transfer_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存调拨日志表';
```

### 3.5 batch_dispose_log — 批次销毁日志表

```sql
CREATE TABLE batch_dispose_log (
    id                BIGINT       AUTO_INCREMENT PRIMARY KEY,
    dispose_no        VARCHAR(50)  NOT NULL COMMENT '销毁号，唯一，格式：DS+日期+序号',
    batch_id          BIGINT       NOT NULL COMMENT '批次ID，外键→vaccine_batch.id',
    dispose_quantity  INT          NOT NULL COMMENT '销毁数量',
    dispose_reason    VARCHAR(200) NOT NULL COMMENT '销毁原因',
    operator_id       BIGINT       NOT NULL COMMENT '操作员ID，外键→sys_user.id',
    dispose_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '销毁时间',
    remark            VARCHAR(500) NULL COMMENT '备注',
    create_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_dispose_no (dispose_no),
    KEY idx_batch (batch_id),
    KEY idx_operator (operator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='批次销毁日志表';
```

### 3.6 stock_alert_log — 库存预警日志表

```sql
CREATE TABLE stock_alert_log (
    id                    BIGINT       AUTO_INCREMENT PRIMARY KEY,
    alert_type            VARCHAR(20)  NOT NULL COMMENT '预警类型：LOW_STOCK/EXPIRY_SOON/EXPIRED',
    vaccine_id            BIGINT       NOT NULL COMMENT '疫苗ID，外键→vaccine.id',
    batch_id              BIGINT       NULL COMMENT '批次ID，外键→vaccine_batch.id',
    batch_no              VARCHAR(50)  NULL COMMENT '批次号',
    alert_value           DECIMAL(10,2) NOT NULL COMMENT '预警值（剩余率或剩余天数）',
    expiry_date           DATE         NULL COMMENT '有效期',
    alert_time            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '预警时间',
    synced_to_supplier    TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已同步供应商：0=否,1=是',
    is_handled            TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已处理：0=未处理,1=已处理',
    handled_by            BIGINT       NULL COMMENT '处理人ID，外键→sys_user.id',
    handled_time          DATETIME     NULL COMMENT '处理时间',
    handle_remark         VARCHAR(500) NULL COMMENT '处理备注',
    create_time           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_alert_type (alert_type),
    KEY idx_vaccine (vaccine_id),
    KEY idx_batch (batch_id),
    KEY idx_handled (is_handled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存预警日志表';
```

### 3.7 supplier_sync_log — 供应商同步日志表（预留）

```sql
CREATE TABLE supplier_sync_log (
    id                BIGINT       AUTO_INCREMENT PRIMARY KEY,
    batch_id          BIGINT       NOT NULL COMMENT '批次ID',
    sync_type         VARCHAR(20)  NOT NULL COMMENT '同步类型：LOW_STOCK/EXPIRY_SOON',
    sync_status       TINYINT      NOT NULL COMMENT '同步状态：0=失败,1=成功',
    request_body      TEXT         NULL COMMENT '请求报文',
    response_body     TEXT         NULL COMMENT '响应报文',
    error_msg         VARCHAR(500) NULL COMMENT '错误信息',
    sync_time         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '同步时间',
    KEY idx_batch (batch_id),
    KEY idx_sync_time (sync_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商同步日志表（预留）';
```

### 3.8 表间关系

```
vaccine (疫苗信息)
  │ 1:N
  ▼
vaccine_batch (批次)
  │ 1:1
  ▼
hospital_vaccine_stock (库存)
  │ 聚合
  ▼
hospital_vaccine_summary (汇总)

vaccine_batch (批次)
  │ 1:N
  ├──► stock_transfer_log (调拨日志)
  ├──► batch_dispose_log (销毁日志)
  ├──► stock_alert_log (预警日志)
  └──► supplier_sync_log (供应商同步日志)
```

---

## 4. 库存查询逻辑

### 4.1 F-STOCK-001 查询库存汇总

#### 4.1.1 功能描述

按疫苗维度查询医院库存汇总信息，数据来源为 `hospital_vaccine_summary` 汇总表。

#### 4.1.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 库存医生已登录 | Token 有效 |
| PRE-002 | 拥有库存查看权限 | `stock.view` |

#### 4.1.3 输入参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| vaccineId | Long | 否 | NULL | 不传则查询所有疫苗 |
| hospitalId | Long | 是 | 1 | 固定为1 |

#### 4.1.4 处理流程

```
STEP 1: 参数校验
  ├─ hospitalId 非空且为正整数
  ├─ vaccineId 如有值则必须为正整数
  → 失败返回 1003 BAD_REQUEST

STEP 2: 查询库存汇总
  ├─ SELECT vs.id, vs.vaccine_id, v.vaccine_name, v.category,
  │        vs.total_stock, vs.available_stock,
  │        (vs.available_stock / vs.total_stock) * 100 AS remaining_ratio,
  │        ((vs.total_stock - vs.available_stock) / vs.total_stock) * 100 AS usage_ratio
  │   FROM hospital_vaccine_summary vs
  │   JOIN vaccine v ON vs.vaccine_id = v.id
  │   WHERE vs.hospital_id = #{hospitalId}
  │     AND (#{vaccineId} IS NULL OR vs.vaccine_id = #{vaccineId})
  │   ORDER BY remaining_ratio ASC
  └─ 返回库存汇总列表（低库存在前）

STEP 3: 补充预警标识
  └─ 对每条记录，判断 remaining_ratio < warning_threshold 则标记 hasAlert=true
```

#### 4.1.5 数据操作（SQL级）

```sql
SELECT
    vs.id                AS summary_id,
    vs.vaccine_id        AS vaccine_id,
    v.name               AS vaccine_name,
    v.category           AS vaccine_category,
    vs.total_stock       AS total_stock,
    vs.available_stock   AS available_stock,
    vs.total_stock - vs.available_stock AS locked_stock,
    CASE WHEN vs.total_stock > 0
         THEN ROUND((vs.available_stock / vs.total_stock) * 100, 2)
         ELSE 100.00 END AS remaining_ratio,
    CASE WHEN vs.total_stock > 0
         THEN ROUND(((vs.total_stock - vs.available_stock) / vs.total_stock) * 100, 2)
         ELSE 0.00 END AS usage_ratio,
    vs.warning_threshold AS warning_threshold
FROM hospital_vaccine_summary vs
JOIN vaccine v ON vs.vaccine_id = v.id
WHERE vs.hospital_id = #{hospitalId}
  AND (#{vaccineId} IS NULL OR vs.vaccine_id = #{vaccineId})
ORDER BY remaining_ratio ASC;
```

#### 4.1.6 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 未登录 | 1001 | 401 | 拦截器拦截 |
| 无权限 | 1002 | 403 | 返回"无权限" |
| 参数格式错误 | 1003 | 400 | 返回具体字段错误 |
| 参数校验失败：{field_name} | 4007 | 400 | 返回具体字段错误 |
| 登录已过期 | 4008 | 401 | 清除Token，跳转登录页 |

#### 4.1.7 并发控制

本接口为只读查询，无需并发控制。

#### 4.1.8 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `stock.view` |
| API前缀 | `/api/stock/*` — 仅 DOCTOR_STOCK 可访问 |

---

### 4.2 F-STOCK-002 查询批次库存

#### 4.2.1 功能描述

根据批次ID查询该批次的库存信息，数据来源为 `hospital_vaccine_stock`。

#### 4.2.2 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| batchId | Long | 是 | 路径参数 | 批次ID |

#### 4.2.3 处理流程

```
STEP 1: 参数校验
  └─ batchId 非空且为正整数
  → 失败返回 1003 BAD_REQUEST

STEP 2: 查询批次库存
  ├─ SELECT s.available_stock, s.locked_stock,
  │        b.batch_no, v.name AS vaccine_name
  │   FROM hospital_vaccine_stock s
  │   JOIN vaccine_batch b ON s.batch_id = b.id
  │   JOIN vaccine v ON b.vaccine_id = v.id
  │   WHERE s.batch_id = #{batchId} AND s.hospital_id = #{hospitalId}
  └─ 记录不存在 → 返回 4006 STOCK_BATCH_NOT_FOUND

STEP 3: 计算派生字段
  ├─ total_stock = available_stock + locked_stock
  └─ remaining_ratio = (available_stock / total_stock) * 100%
```

#### 4.2.4 数据操作（SQL级）

```sql
SELECT
    s.batch_id           AS batch_id,
    b.batch_no           AS batch_no,
    v.name               AS vaccine_name,
    v.category           AS vaccine_category,
    s.available_stock    AS available_stock,
    s.locked_stock       AS locked_stock,
    (s.available_stock + s.locked_stock) AS total_stock,
    CASE WHEN (s.available_stock + s.locked_stock) > 0
         THEN ROUND((s.available_stock / (s.available_stock + s.locked_stock)) * 100, 2)
         ELSE 100.00 END AS remaining_ratio
FROM hospital_vaccine_stock s
JOIN vaccine_batch b ON s.batch_id = b.id
JOIN vaccine v ON b.vaccine_id = v.id
WHERE s.batch_id = #{batchId}
  AND s.hospital_id = #{hospitalId};
```

#### 4.2.5 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 批次不存在 | 4006 | 404 | 返回"批次不存在" |
| 未登录 | 1001 | 401 | 拦截器拦截 |
| 无权限 | 1002 | 403 | 返回"无权限" |
| 参数校验失败：{field_name} | 4007 | 400 | 返回具体字段错误 |
| 登录已过期 | 4008 | 401 | 清除Token，跳转登录页 |
| 数据库连接失败 | 4010 | 500 | 返回友好提示 |

#### 4.2.6 并发控制

> 本接口为只读查询，采用快照读取（SELECT 不加锁），不参与库存事务。查询结果反映查询时刻的一致性快照。

---

### 4.3 F-STOCK-003 查询批次列表

#### 4.3.1 功能描述

查询疫苗批次列表，支持按疫苗ID、批次状态筛选，支持分页，按有效期升序排列。

#### 4.3.2 输入参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| vaccineId | Long | 否 | NULL | 疫苗ID |
| status | String | 否 | ALL | ALL/NORMAL/EXPIRY_SOON/EXPIRED/DISPOSED |
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页数量 |

#### 4.3.3 处理流程

```
STEP 1: 参数校验
  ├─ status 枚举校验（ALL/NORMAL/EXPIRY_SOON/EXPIRED/DISPOSED）
  ├─ page >= 1, 1 <= size <= 100
  → 失败返回 1003 BAD_REQUEST

STEP 2: 构建状态过滤条件
  ├─ ALL       → 无额外条件
  ├─ NORMAL    → AND b.status = 0
  ├─ EXPIRY_SOON → AND b.status = 1
  ├─ EXPIRED   → AND b.status = 2
  └─ DISPOSED  → AND b.status = 3

STEP 3: 查询批次列表（分页）
  ├─ SELECT b.id, b.batch_no, v.vaccine_name, v.category,
  │        b.production_date, b.expiry_date,
  │        s.available_stock, s.locked_stock, b.status
  │   FROM vaccine_batch b
  │   JOIN vaccine v ON b.vaccine_id = v.id
  │   JOIN hospital_vaccine_stock s ON b.id = s.batch_id
  │   WHERE s.hospital_id = #{hospitalId}
  │     AND (#{vaccineId} IS NULL OR b.vaccine_id = #{vaccineId})
  │     AND (#{statusFilter} 条件)
  │   ORDER BY b.expiry_date ASC
  │   LIMIT #{size} OFFSET #{offset}
  └─ 返回批次列表 + 分页信息
```

#### 4.3.4 数据操作（SQL级）

```sql
-- 查询列表
SELECT
    b.id                AS batch_id,
    b.batch_no          AS batch_no,
    b.vaccine_id        AS vaccine_id,
    v.name              AS vaccine_name,
    v.category          AS vaccine_category,
    b.manufacturer      AS manufacturer,
    b.production_date   AS production_date,
    b.expiry_date       AS expiry_date,
    b.status            AS batch_status,
    s.available_stock   AS available_stock,
    s.locked_stock      AS locked_stock,
    (s.available_stock + s.locked_stock) AS total_stock
FROM vaccine_batch b
JOIN vaccine v ON b.vaccine_id = v.id
JOIN hospital_vaccine_stock s ON b.id = s.batch_id
WHERE s.hospital_id = #{hospitalId}
  AND (#{vaccineId} IS NULL OR b.vaccine_id = #{vaccineId})
  AND (#{statusFilter})
ORDER BY b.expiry_date ASC
LIMIT #{size} OFFSET #{offset};

-- 查询总数
SELECT COUNT(*)
FROM vaccine_batch b
JOIN hospital_vaccine_stock s ON b.id = s.batch_id
WHERE s.hospital_id = #{hospitalId}
  AND (#{vaccineId} IS NULL OR b.vaccine_id = #{vaccineId})
  AND (#{statusFilter});
```

#### 4.3.5 并发控制

> 本接口为只读查询，采用快照读取（SELECT 不加锁），不参与库存事务。查询结果反映查询时刻的一致性快照。

#### 4.3.6 排序与分页

- 默认按有效期升序排列（即将过期的批次排在前面，FEFO 原则）
- 支持分页查询，默认每页 10 条，最大 100 条
- 分页参数：`page`（页码，从 1 开始）、`size`（每页条数）

---

### 4.4 F-STOCK-004 查询批次详情

#### 4.4.1 功能描述

根据批次ID查询批次的完整详情，包含批次基本信息、疫苗信息、库存信息、预警信息。

#### 4.4.2 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| batchId | Long | 是 | 路径参数 | 批次ID |

#### 4.4.3 数据操作（SQL级）

```sql
-- 查询批次详情
SELECT
    b.id                AS batch_id,
    b.batch_no          AS batch_no,
    b.vaccine_id        AS vaccine_id,
    v.name              AS vaccine_name,
    v.category          AS vaccine_category,
    b.manufacturer      AS manufacturer,
    b.production_date   AS production_date,
    b.expiry_date       AS expiry_date,
    b.warning_days      AS warning_days,
    b.status            AS batch_status,
    s.available_stock   AS available_stock,
    s.locked_stock      AS locked_stock,
    (s.available_stock + s.locked_stock) AS total_stock,
    CASE WHEN (s.available_stock + s.locked_stock) > 0
         THEN ROUND((s.available_stock / (s.available_stock + s.locked_stock)) * 100, 2)
         ELSE 100.00 END AS remaining_ratio
FROM vaccine_batch b
JOIN vaccine v ON b.vaccine_id = v.id
JOIN hospital_vaccine_stock s ON b.id = s.batch_id
WHERE b.id = #{batchId}
  AND s.hospital_id = #{hospitalId};

-- 查询预警信息（如有）
SELECT
    id AS alert_id,
    alert_type,
    alert_value,
    expiry_date,
    alert_time,
    is_handled,
    handled_by,
    handled_time
FROM stock_alert_log
WHERE batch_id = #{batchId}
  AND is_handled = 0
ORDER BY alert_time DESC;
```

#### 4.4.4 并发控制

> 本接口为只读查询，采用快照读取（SELECT 不加锁），不参与库存事务。查询结果反映查询时刻的一致性快照。返回结果中包含批次当前状态值（batch_status），状态含义参见本文 §6.3 批次状态流转图。

---

## 5. 调拨流程

### 5.1 F-STOCK-005 创建并执行调拨

#### 5.1.1 功能描述

库存医生创建调拨单并立即执行调拨。系统在同一事务内完成：校验 → 扣减调出位置库存 → 增加调入位置库存 → 记录调拨日志 → 更新库存汇总。创建即执行，无待确认状态。

#### 5.1.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 库存医生已登录 | Token 有效 |
| PRE-002 | 拥有调拨权限 | `stock.transfer` |
| PRE-003 | 批次存在 | `vaccine_batch.id = #{batchId}` |
| PRE-004 | 批次未销毁 | `vaccine_batch.status != 3` |
| PRE-005 | 调出位置库存充足 | `available_stock >= quantity` |

#### 5.1.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| batchId | Long | 是 | 请求体 | 批次ID |
| fromType | Integer | 是 | 请求体 | 调出类型：0=总仓,1=接种点 |
| fromId | Long | 是 | 请求体 | 调出位置ID |
| toType | Integer | 是 | 请求体 | 调入类型：0=总仓,1=接种点 |
| toId | Long | 是 | 请求体 | 调入位置ID |
| quantity | Integer | 是 | 请求体 | 调拨数量 |
| remark | String | 否 | 请求体 | 备注 |
| doctorId | Long | 是 | Token 解析 | 操作员ID |

#### 5.1.4 处理流程

```
STEP 1: 参数校验
  ├─ batchId, fromId, toId 非空且为正整数
  ├─ quantity 非空且为正整数
  ├─ fromType, toType 枚举校验（0=总仓,1=接种点）
  → 失败返回 1003 BAD_REQUEST

STEP 2: 调出调入位置不能相同校验
  ├─ IF from_type == to_type AND from_id == to_id
  └─ → 返回 4001 STOCK_TRANSFER_SAME_LOCATION

STEP 3: 锁定批次行，校验状态（事务开始）
  ├─ SELECT id, status, vaccine_id FROM vaccine_batch WHERE id = #{batchId} FOR UPDATE
  ├─ 记录不存在 → 返回 4006 STOCK_BATCH_NOT_FOUND，ROLLBACK
  └─ status = 3 → 返回 4003 STOCK_BATCH_DISPOSED，ROLLBACK

STEP 4: 锁定调出位置库存行，校验库存充足
  ├─ SELECT available_stock FROM hospital_vaccine_stock
  │   WHERE batch_id = #{batchId} AND hospital_id = #{hospitalId}
  │   FOR UPDATE
  └─ available_stock < quantity → 返回 4002 STOCK_TRANSFER_INSUFFICIENT，ROLLBACK

STEP 5: 扣减调出位置库存
  ├─ UPDATE hospital_vaccine_stock
  │   SET available_stock = available_stock - #{quantity}
  │   WHERE batch_id = #{batchId}
  │     AND available_stock >= #{quantity}
  ├─ affected_rows = 0 → 返回 4009 STOCK_TRANSFER_FAILED，ROLLBACK
  └─ WHERE available_stock >= #{quantity} 防并发超扣

STEP 6: 增加调入位置库存
  ├─ INSERT INTO hospital_vaccine_stock
  │   (hospital_id, batch_id, available_stock, locked_stock)
  │   VALUES (#{hospitalId}, #{batchId}, #{quantity}, 0)
  │   ON DUPLICATE KEY UPDATE
  │   available_stock = available_stock + #{quantity}
  └─ 使用 hospital_id + batch_id 联合唯一键判断

STEP 7: 生成调拨号并记录调拨日志
  ├─ transferNo = 'TF' + YYYYMMDD + 4位序号
  └─ INSERT INTO stock_transfer_log (...) VALUES (...)

STEP 8: 更新库存汇总表（乐观锁）
  └─ UPDATE hospital_vaccine_summary
      SET total_stock = #{newTotal}, available_stock = #{newAvailable},
          version = version + 1
      WHERE id = #{summaryId} AND version = #{currentVersion}
  └─ affected_rows = 0 → 记录日志，后续定时任务补偿

STEP 9: 事务提交
  └─ COMMIT
  └─ 返回调拨成功信息（含调拨号）
```

#### 5.1.5 数据操作（SQL级）

```sql
-- ============================================
-- 事务 T8（库存调拨）
-- 隔离级别：READ_COMMITTED
-- 超时时间：30秒
-- ============================================
BEGIN;

-- STEP 3: 锁定批次行
SELECT id, status, vaccine_id
FROM vaccine_batch
WHERE id = #{batchId}
FOR UPDATE;
-- assert: status != 3

-- STEP 4: 锁定调出位置库存行
SELECT available_stock
FROM hospital_vaccine_stock
WHERE batch_id = #{batchId}
  AND hospital_id = #{hospitalId}
FOR UPDATE;
-- assert: available_stock >= #{quantity}

-- STEP 5: 扣减调出位置库存
UPDATE hospital_vaccine_stock
SET available_stock = available_stock - #{quantity},
    update_time = NOW()
WHERE batch_id = #{batchId}
  AND hospital_id = #{hospitalId}
  AND available_stock >= #{quantity};
-- assert: affected_rows = 1

-- STEP 6: 增加调入位置库存
INSERT INTO hospital_vaccine_stock (hospital_id, batch_id, available_stock, locked_stock, create_time, update_time)
VALUES (#{hospitalId}, #{batchId}, #{quantity}, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    available_stock = available_stock + #{quantity},
    update_time = NOW();

-- STEP 7: 生成调拨号并记录日志
-- transferNo 生成伪代码：'TF' + DateFormat(NOW(), 'yyyyMMdd') + LPAD(SEQ, 4, '0')
INSERT INTO stock_transfer_log (
    transfer_no, batch_id, from_type, from_id, to_type, to_id,
    quantity, operator_id, transfer_time, remark, create_time
) VALUES (
    #{transferNo}, #{batchId}, #{fromType}, #{fromId}, #{toType}, #{toId},
    #{quantity}, #{doctorId}, NOW(), #{remark}, NOW()
);

-- STEP 8: 更新库存汇总（乐观锁）
UPDATE hospital_vaccine_summary
SET total_stock = (
      SELECT COALESCE(SUM(available_stock + locked_stock), 0)
      FROM hospital_vaccine_stock
      WHERE hospital_id = #{hospitalId} AND batch_id IN (
          SELECT id FROM vaccine_batch WHERE vaccine_id = #{vaccineId}
      )
  ),
  available_stock = (
      SELECT COALESCE(SUM(available_stock), 0)
      FROM hospital_vaccine_stock
      WHERE hospital_id = #{hospitalId} AND batch_id IN (
          SELECT id FROM vaccine_batch WHERE vaccine_id = #{vaccineId}
      )
  ),
  version = version + 1,
  update_time = NOW()
WHERE hospital_id = #{hospitalId}
  AND vaccine_id = #{vaccineId};

-- ============================================
-- 事务提交
-- ============================================
-- 调拨事务补偿机制：
-- 事务 T8 涵盖出库+入库两张表 UPDATE，使用数据库事务保证原子性。
-- ├─ 正常完成：两张表同时更新成功，事务提交。
-- ├─ 出库成功但入库失败：事务回滚，出库操作自动撤销，数据保持一致。
-- └─ 补偿查询：SELECT * FROM stock_transfer_log WHERE status = 'PENDING'
--     可用于定时任务扫描未完成调拨并重试（当前实现为同步事务，此为预留扩展点）。
COMMIT;
```

#### 5.1.6 事务边界

```
┌──────────────────────────────────────────────────────────────────────┐
│  事务 T8（库存调拨）                                                 │
│                                                                      │
│  BEGIN;                                                              │
│    ├─ SELECT vaccine_batch FOR UPDATE       ← 锁批次行              │
│    ├─ SELECT hospital_vaccine_stock FOR UPDATE ← 锁库存行            │
│    ├─ UPDATE hospital_vaccine_stock (扣减)  ← 扣调出库存             │
│    ├─ INSERT/UPDATE hospital_vaccine_stock  ← 加调入库存             │
│    ├─ INSERT stock_transfer_log             ← 记录调拨日志           │
│    └─ UPDATE hospital_vaccine_summary       ← 更新汇总（乐观锁）     │
│  COMMIT;                                                             │
│                                                                      │
│  涉及表：vaccine_batch, hospital_vaccine_stock(×2),                  │
│          stock_transfer_log, hospital_vaccine_summary                 │
│                                                                      │
│  回滚条件：任何一步抛出 RuntimeException / BusinessException          │
│  隔离级别：READ_COMMITTED                                             │
│  超时时间：30秒                                                       │
└──────────────────────────────────────────────────────────────────────┘
```

#### 5.1.7 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 调出调入位置相同 | 4001 | 400 | 返回"调出位置和调入位置不能相同" |
| 调出位置库存不足 | 4002 | 400 | 事务回滚，返回库存不足详情 |
| 批次已销毁 | 4003 | 400 | 事务回滚，返回"该批次已销毁" |
| 批次不存在 | 4006 | 404 | 事务回滚，返回"批次不存在" |
| 参数错误 | 4007 | 400 | 返回具体字段错误 |
| 登录已过期 | 4008 | 401 | 清除Token，跳转登录页 |
| 调拨执行失败 | 4009 | 500 | 事务回滚，引导重试 |
| 数据库异常 | 4010 | 500 | 事务回滚，记录日志 |
| 并发冲突 | 4011 | 409 | 事务回滚，引导重试 |
| 调拨操作失败 | 4009 | 500 | 显示错误，引导重试，记录日志 |
| 数据库连接失败 | 4010 | 500 | 返回友好提示 |
| 并发冲突（通用） | 4011 | 409 | 返回友好提示 |
| 未登录 | 1001 | 401 | 拦截器拦截 |
| 无权限 | 1002 | 403 | 返回"无权限" |

#### 5.1.8 并发控制

```sql
-- 调拨涉及库存行的读写，使用 SELECT FOR UPDATE 加行锁：
-- 1. 先锁批次行（防止销毁并发）
SELECT id, status FROM vaccine_batch WHERE id = #{batchId} FOR UPDATE;

-- 2. 再锁库存行（防止并发调拨超扣）
SELECT available_stock FROM hospital_vaccine_stock
WHERE batch_id = #{batchId} FOR UPDATE;

-- 3. UPDATE WHERE 二次校验（防锁间隔超扣）
UPDATE hospital_vaccine_stock
SET available_stock = available_stock - #{quantity}
WHERE batch_id = #{batchId} AND available_stock >= #{quantity};
```

> 乐观锁冲突时，按 REQ-GLOBAL §7.4 重试策略执行：最多重试 2 次，间隔 200ms、500ms（固定间隔）。重试耗尽后返回 4011 STOCK_CONFLICT（HTTP 409），引导用户手动重试。

#### 5.1.9 调拨号生成规则

| 格式 | 示例 | 说明 |
|------|------|------|
| TF + 8位日期 + 4位序号 | TF202604020001 | 唯一标识 |

```java
private String generateTransferNo() {
    String datePart = new SimpleDateFormat("yyyyMMdd").format(new Date());
    Integer maxSeq = transferLogMapper.selectMaxSeqByDate(datePart);
    int newSeq = (maxSeq == null ? 0 : maxSeq) + 1;
    return String.format("TF%s%04d", datePart, newSeq);
}
```

---

### 5.2 F-STOCK-006 查询调拨记录

#### 5.2.1 功能描述

查询调拨历史记录，支持按批次ID、日期范围筛选，支持分页。

#### 5.2.2 输入参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| batchId | Long | 否 | NULL | 批次ID |
| startDate | String | 否 | 30天前 | 开始日期 YYYY-MM-DD |
| endDate | String | 否 | 当天 | 结束日期 YYYY-MM-DD |
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页数量 |

#### 5.2.3 数据操作（SQL级）

```sql
SELECT
    stl.id               AS transfer_id,
    stl.transfer_no      AS transfer_no,
    stl.batch_id         AS batch_id,
    vb.batch_no          AS batch_no,
    stl.from_type        AS from_type,
    stl.from_id          AS from_id,
    stl.to_type          AS to_type,
    stl.to_id            AS to_id,
    stl.quantity         AS quantity,
    stl.operator_id      AS operator_id,
    su.name              AS operator_name,
    stl.transfer_time    AS transfer_time,
    stl.remark           AS remark
FROM stock_transfer_log stl
JOIN vaccine_batch vb ON stl.batch_id = vb.id
LEFT JOIN sys_user su ON stl.operator_id = su.id
WHERE 1 = 1
  AND (#{batchId} IS NULL OR stl.batch_id = #{batchId})
  AND stl.transfer_time >= #{startDate}
  AND stl.transfer_time <= CONCAT(#{endDate}, ' 23:59:59')
ORDER BY stl.transfer_time DESC
LIMIT #{size} OFFSET #{offset};
```

#### 5.2.4 处理流程

```
STEP 1: 参数校验
  ├─ startDate 格式校验，endDate 格式校验
  └─ startDate > endDate → 返回 1003 BAD_REQUEST

STEP 2: 构建查询条件
  ├─ filter=ALL → 无额外条件
  ├─ filter=PENDING → AND status = 'PENDING'
  ├─ filter=COMPLETED → AND status = 'COMPLETED'
  └─ filter=FAILED → AND status = 'FAILED'

STEP 3: 执行查询
  └─ SELECT ... FROM stock_transfer_log
      WHERE transfer_date BETWEEN #{startDate} AND #{endDate}
      [AND status = #{filter}]
      ORDER BY create_time DESC
      LIMIT #{pageSize} OFFSET #{offset}

STEP 4: 组装返回结果
  └─ 返回调拨记录列表 VO
```

#### 5.2.5 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 参数格式错误 | 1003 | 400 | 返回具体字段错误 |
| 未登录 | 1001 | 401 | 拦截器拦截 |
| 角色无权限 | 1002 | 403 | 返回"无权访问" |

#### 5.2.6 并发控制

本接口为只读查询，无需并发控制。列表查询使用 LIMIT 分页，避免全表扫描。

#### 5.2.7 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `stock.transfer.view` |
| 数据权限 | 按调拨日期范围过滤 |
| API前缀 | `/api/stock/*` — 仅 DOCTOR_STOCK 角色可访问 |

### 5.3 F-STOCK-007 销毁批次

> **事务引用：** REQ-GLOBAL §6 T9（批次销毁事务）

#### 5.3.1 功能描述

对过期或质量异常的疫苗批次执行销毁操作，扣减库存并记录销毁日志。销毁操作不可逆。

#### 5.3.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 管理员已登录 | Token 有效 |
| PRE-002 | 当前角色为 DOCTOR_STOCK | 角色校验 |
| PRE-003 | 批次记录存在 | `vaccine_batch.id = #{batchId}` |
| PRE-004 | 批次状态允许销毁 | `hospital_vaccine_stock.available_stock >= 0` |

#### 5.3.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| batchId | Long | 是 | 请求体 | 批次ID |
| disposeReason | String | 是 | 请求体 | 销毁原因：EXPIRED/QUALITY_ISSUE/OTHER |
| disposeQuantity | Integer | 是 | 请求体 | 销毁数量 |
| operatorId | Long | 是 | Token 解析 | 操作人ID |
| remark | String | 否 | 请求体 | 备注信息 |

#### 5.3.4 处理流程

```
STEP 1: 参数校验
  ├─ batchId 非空且为正整数
  ├─ quantity 非空且为正整数
  ├─ disposeReason 非空且非空字符串
  → 失败返回 1003 BAD_REQUEST / 4005 STOCK_DISPOSE_REASON_EMPTY

STEP 2: 锁定批次行，校验状态（事务开始）
  ├─ SELECT id, status, vaccine_id FROM vaccine_batch WHERE id = #{batchId} FOR UPDATE
  ├─ 记录不存在 → 返回 4006 STOCK_BATCH_NOT_FOUND，ROLLBACK
  └─ status = 3 → 返回 4003 STOCK_BATCH_DISPOSED，ROLLBACK

STEP 3: 锁定库存行，校验销毁数量
  ├─ SELECT available_stock, locked_stock FROM hospital_vaccine_stock
  │   WHERE batch_id = #{batchId} FOR UPDATE
  └─ quantity > (available_stock + locked_stock) → 返回 4004 STOCK_DISPOSE_EXCEED，ROLLBACK

STEP 4: 判断全量销毁 or 部分销毁
  ├─ IF quantity >= (available_stock + locked_stock) THEN
  │   └─ 全量销毁：
  │       ├─ UPDATE vaccine_batch SET status = 3, update_time = NOW() WHERE id = #{batchId}
  │       └─ UPDATE hospital_vaccine_stock
  │           SET available_stock = 0, locked_stock = 0, update_time = NOW()
  │           WHERE batch_id = #{batchId}
  ├─ ELSE
  │   └─ 部分销毁：
  │       ├─ 不修改 vaccine_batch.status
  │       └─ UPDATE hospital_vaccine_stock
  │           SET available_stock = available_stock - #{quantity},
  │               update_time = NOW()
  │           WHERE batch_id = #{batchId} AND available_stock >= #{quantity}
  │           （不修改 locked_stock）
  └─ affected_rows = 0 → 返回 4010 STOCK_DB_ERROR，ROLLBACK

STEP 5: 记录销毁日志
  └─ INSERT INTO batch_dispose_log (...) VALUES (...)

STEP 6: 更新库存汇总
  └─ UPDATE hospital_vaccine_summary (同7.3.2模板)

STEP 7: 事务提交
  └─ COMMIT
```

#### 5.3.5 数据操作（SQL级）

```sql
-- ============================================
-- 事务 T9（批次销毁）
-- 隔离级别：READ_COMMITTED
-- 超时时间：30秒
-- ============================================
BEGIN;

-- STEP 2: 锁定批次行
SELECT id, status, vaccine_id
FROM vaccine_batch
WHERE id = #{batchId}
FOR UPDATE;
-- assert: status IN (0, 1, 2)

-- STEP 3: 锁定库存行
SELECT available_stock, locked_stock
FROM hospital_vaccine_stock
WHERE batch_id = #{batchId}
FOR UPDATE;
-- assert: quantity <= (available_stock + locked_stock)

-- STEP 4: 判断全量销毁 or 部分销毁
-- 全量销毁：销毁数量 >= 库存总量
IF #{quantity} >= (available_stock + locked_stock) THEN
  UPDATE vaccine_batch
  SET status = 3, update_time = NOW()
  WHERE id = #{batchId};
  UPDATE hospital_vaccine_stock
  SET available_stock = 0, locked_stock = 0, update_time = NOW()
  WHERE batch_id = #{batchId};
ELSE
  -- 部分销毁：仅扣减 available_stock，不修改批次状态和 locked_stock
  UPDATE hospital_vaccine_stock
  SET available_stock = available_stock - #{quantity},
      update_time = NOW()
  WHERE batch_id = #{batchId}
    AND available_stock >= #{quantity};
END IF;

-- STEP 5: 记录销毁日志
INSERT INTO batch_dispose_log (
    dispose_no, batch_id, dispose_quantity, dispose_reason,
    operator_id, dispose_time, remark, create_time
) VALUES (
    #{disposeNo}, #{batchId}, #{quantity}, #{disposeReason},
    #{doctorId}, NOW(), #{remark}, NOW()
);

-- STEP 6: 更新库存汇总
UPDATE hospital_vaccine_summary
SET total_stock = (
      SELECT COALESCE(SUM(available_stock + locked_stock), 0)
      FROM hospital_vaccine_stock
      WHERE hospital_id = #{hospitalId} AND batch_id IN (
          SELECT id FROM vaccine_batch WHERE vaccine_id = #{vaccineId}
      )
  ),
  available_stock = (
      SELECT COALESCE(SUM(available_stock), 0)
      FROM hospital_vaccine_stock
      WHERE hospital_id = #{hospitalId} AND batch_id IN (
          SELECT id FROM vaccine_batch WHERE vaccine_id = #{vaccineId}
      )
  ),
  version = version + 1,
  update_time = NOW()
WHERE hospital_id = #{hospitalId}
  AND vaccine_id = #{vaccineId};

COMMIT;
```

#### 5.3.6 销毁号生成规则

| 格式 | 示例 | 说明 |
|------|------|------|
| DS + 8位日期 + 4位序号 | DS202604020001 | 唯一标识 |

#### 5.3.7 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 未登录 | 1001 | 401 | 拦截器拦截 |
| 无权限 | 1002 | 403 | 返回"无权限" |
| 参数格式错误 | 1003 | 400 | 返回具体字段错误 |
| 批次已销毁 | 4003 | 400 | 事务回滚，返回"该批次已销毁" |
| 销毁数量超限 | 4004 | 400 | 事务回滚，返回"销毁数量超过库存" |
| 销毁原因为空 | 4005 | 400 | 返回"请填写销毁原因" |
| 批次不存在 | 4006 | 404 | 事务回滚，返回"批次不存在" |
| 参数校验失败：{field_name} | 4007 | 400 | 返回具体字段错误 |
| 登录已过期 | 4008 | 401 | 清除Token，跳转登录页 |
| 调拨操作失败 | 4009 | 500 | 显示错误，引导重试，记录日志 |
| 数据库连接失败 | 4010 | 500 | 返回友好提示 |
| 数据库异常 | 4010 | 500 | 事务回滚，记录日志 |
| 并发冲突 | 4011 | 409 | 返回友好提示 |

#### 5.3.8 并发控制

| 控制项 | 规则 |
|--------|------|
| 事务引用 | REQ-GLOBAL §6 T9（批次销毁事务） |
| 锁定方式 | `SELECT ... FOR UPDATE` 分别锁定 vaccine_batch 和 hospital_vaccine_stock 行 |
| 隔离级别 | READ_COMMITTED |
| 超时时间 | 30秒 |
| 锁顺序 | 先锁 vaccine_batch，再锁 hospital_vaccine_stock（与调拨事务 T8 一致，防止死锁） |

> 乐观锁冲突时，按 REQ-GLOBAL §7.4 重试策略执行：最多重试 2 次，间隔 200ms、500ms（固定间隔）。重试耗尽后批次进入异常状态（非正常完成/非回滚），返回 4011 STOCK_CONFLICT（HTTP 409），由补偿任务（REQ-GLOBAL §7.3.3，每 5 分钟扫描）自动处理。

#### 5.3.9 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `stock.batch.dispose` |
| 角色要求 | DOCTOR_STOCK |
| 数据权限 | 本院库存数据 |
| API前缀 | `/api/stock/*` — 仅 DOCTOR_STOCK 角色可访问 |

#### 5.3.10 状态流转说明

销毁操作仅影响库存数据和批次状态，**不影响预约状态**：

| 对象 | 影响 | 说明 |
|------|------|------|
| `vaccine_batch.status` | 条件变更 | 全量销毁时 0/1/2 → 3（已销毁）；部分销毁时不修改 |
| `hospital_vaccine_stock.available_stock` | 扣减 | 减少 disposeQuantity（全量时归零） |
| `hospital_vaccine_stock.locked_stock` | 条件清零 | 全量销毁时清零；部分销毁时不修改 |
| `appointment.status` | 不影响 | 预约状态由 REGISTER/VACCINATE 模块管理 |
| `hospital_vaccine_summary` | 更新 | 同步更新汇总数据 |

> 库存操作日志和调拨/销毁记录的归档策略参见 REQ-GLOBAL §11。

---

## 5.5 F-STOCK-010 查询库存统计

### 5.5.1 功能描述

按疫苗、批次、时间段统计库存使用量，支持汇总和明细两种视图。本接口为**只读查询**。

### 5.5.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 管理员已登录 | Token 有效 |
| PRE-002 | 当前角色为 DOCTOR_BUSINESS_ADMIN | 角色校验 |

### 5.5.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| startDate | Date | 否 | 查询参数 | 统计起始日期，默认本月1日 |
| endDate | Date | 否 | 查询参数 | 统计结束日期，默认当天 |
| vaccineId | Long | 否 | 查询参数 | 疫苗ID筛选，不传则统计全部 |
| groupBy | String | 否 | 查询参数 | 分组方式：VACCINE/BATCH/TIME_PERIOD，默认 VACCINE |

### 5.5.4 处理流程

```
STEP 1: 参数校验
  ├─ startDate/endDate 格式校验
  ├─ startDate > endDate → 返回 1003 BAD_REQUEST
  └─ groupBy 枚举校验（VACCINE/BATCH/TIME_PERIOD）
  → 失败返回 1003 BAD_REQUEST

STEP 2: 构建统计查询
  ├─ groupBy=VACCINE → GROUP BY v.id, v.name
  │   └─ 统计：SUM(used_quantity), SUM(available_stock), SUM(locked_stock), COUNT(batch_no)
  ├─ groupBy=BATCH → GROUP BY vb.batch_no, vb.expiry_date
  │   └─ 统计：used_quantity, available_stock, locked_stock, dispose_quantity
  └─ groupBy=TIME_PERIOD → GROUP BY DATE(hvs.update_time)
      └─ 统计：每日入库量、每日使用量、每日销毁量

STEP 3: 执行查询
  └─ SELECT [统计字段] FROM hospital_vaccine_stock hvs
      JOIN vaccine_batch vb ON hvs.batch_id = vb.id
      JOIN vaccine v ON vb.vaccine_id = v.id
      WHERE 1=1
        [AND vb.vaccine_id = #{vaccineId}]
        [AND hvs.update_time BETWEEN #{startDate} AND #{endDate}+1]
      GROUP BY [分组字段]
      ORDER BY [排序字段]

STEP 4: 组装返回结果
  └─ 返回统计列表 VO
```

### 5.5.5 数据操作（SQL级）

```sql
-- 按疫苗汇总统计（默认）
SELECT v.id AS vaccine_id, v.name AS vaccine_name,
    SUM(hvs.used_quantity) AS total_used,
    SUM(hvs.available_stock) AS total_available,
    SUM(hvs.locked_stock) AS total_locked,
    COUNT(hvs.batch_id) AS batch_count
FROM hospital_vaccine_stock hvs
JOIN vaccine_batch vb ON hvs.batch_id = vb.id
JOIN vaccine v ON vb.vaccine_id = v.id
WHERE 1=1
  [AND vb.vaccine_id = #{vaccineId}]
GROUP BY v.id, v.name
ORDER BY total_used DESC;
```

### 5.5.6 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 参数格式错误 | 1003 | 400 | 返回具体字段错误 |
| 未登录 | 1001 | 401 | 拦截器拦截 |
| 角色无权限 | 1002 | 403 | 返回"无权访问" |

### 5.5.7 并发控制

本接口为只读统计查询，无需并发控制。使用 READ_COMMITTED 隔离级别，统计查询添加时间范围限制避免全表扫描。

### 5.5.8 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `stats.view` |
| 数据权限 | 本院全部库存数据 |
| API前缀 | `/api/stock/*` — 仅 DOCTOR_BUSINESS_ADMIN 角色可访问 |

---

## 6. 库存预警规则

### 6.1 预警类型定义

| 预警类型 | alert_type | 触发条件 | 自动动作 |
|----------|-----------|----------|----------|
| 低库存预警 | `LOW_STOCK` | `available_stock / (available_stock + locked_stock) * 100 < 20%` | 记录预警日志，同步供应商，INSERT INTO supplier_sync_log |
| 临期预警 | `EXPIRY_SOON` | `expiry_date - warning_days < NOW()` 且 `status = 0` | 标记 `status = 1`，记录预警日志 |
| 过期预警 | `EXPIRED` | `expiry_date < NOW()` 且 `status IN (0, 1)` | 标记 `status = 2`，记录预警日志 |

### 6.2 预警扫描定时任务

#### 6.2.1 临期预警扫描

```sql
-- 每小时执行一次
-- 扫描正常批次中即将临期的，标记为临期
UPDATE vaccine_batch
SET status = 1, update_time = NOW()
WHERE expiry_date - INTERVAL warning_days DAY < NOW()
  AND status = 0;

-- 记录临期预警日志
INSERT INTO stock_alert_log (alert_type, vaccine_id, batch_id, batch_no, alert_value, expiry_date, alert_time, synced_to_supplier)
SELECT 'EXPIRY_SOON', vb.vaccine_id, vb.id, vb.batch_no,
       DATEDIFF(vb.expiry_date, NOW()), vb.expiry_date, NOW(), 0
FROM vaccine_batch vb
WHERE vb.expiry_date - INTERVAL vb.warning_days DAY < NOW()
  AND vb.status = 1
  AND NOT EXISTS (
      SELECT 1 FROM stock_alert_log sal
      WHERE sal.batch_id = vb.id AND sal.alert_type = 'EXPIRY_SOON'
        AND sal.alert_time = CURDATE()
  );
```

#### 6.2.2 过期预警扫描

```sql
-- 每小时执行一次
-- 扫描正常/临期批次中已过期的，标记为过期
UPDATE vaccine_batch
SET status = 2, update_time = NOW()
WHERE expiry_date < NOW()
  AND status IN (0, 1);

-- 记录过期预警日志
INSERT INTO stock_alert_log (alert_type, vaccine_id, batch_id, batch_no, alert_value, expiry_date, alert_time, synced_to_supplier)
SELECT 'EXPIRED', vb.vaccine_id, vb.id, vb.batch_no,
       -DATEDIFF(NOW(), vb.expiry_date), vb.expiry_date, NOW(), 0
FROM vaccine_batch vb
WHERE vb.expiry_date < NOW()
  AND vb.status = 2
  AND NOT EXISTS (
      SELECT 1 FROM stock_alert_log sal
      WHERE sal.batch_id = vb.id AND sal.alert_type = 'EXPIRED'
        AND sal.alert_time = CURDATE()
  );
```

#### 6.2.3 低库存预警扫描

```sql
-- 每小时执行一次
-- 扫描剩余率低于阈值的批次
INSERT INTO stock_alert_log (alert_type, vaccine_id, batch_id, batch_no, alert_value, alert_time, synced_to_supplier)
SELECT 'LOW_STOCK', vb.vaccine_id, hvs.batch_id, vb.batch_no,
       ROUND((hvs.available_stock / (hvs.available_stock + hvs.locked_stock)) * 100, 2),
       NOW(), vb.expiry_date, 0
FROM hospital_vaccine_stock hvs
JOIN vaccine_batch vb ON hvs.batch_id = vb.id
JOIN hospital_vaccine_summary hvs_sum ON vb.vaccine_id = hvs_sum.vaccine_id
WHERE hvs.hospital_id = #{hospitalId}
  AND vb.status IN (0, 1)
  AND (hvs.available_stock + hvs.locked_stock) > 0
  AND (hvs.available_stock / (hvs.available_stock + hvs.locked_stock)) * 100 < hvs_sum.warning_threshold
  AND NOT EXISTS (
      SELECT 1 FROM stock_alert_log sal
      WHERE sal.batch_id = hvs.batch_id AND sal.alert_type = 'LOW_STOCK'
        AND sal.alert_time = CURDATE()
  );
```

### 6.3 批次状态流转图

```
  ┌──────────┐     定时扫描      ┌──────────┐     定时扫描      ┌──────────┐
  │  0       │ ──────────────>  │  1       │ ──────────────>  │  2       │
  │  正常    │   expiry -       │  临期    │   expiry <      │  过期    │
  │          │   warning_days   │          │   NOW()         │          │
  │          │   < NOW()        │          │                 │          │
  └────┬─────┘                  └────┬─────┘                  └────┬─────┘
       │                             │                             │
       │        销毁操作             │        销毁操作             │
       └──────────┬──────────────────┘────────────────────────────┘
                  │
                  ▼
            ┌──────────┐
            │  3       │
            │  已销毁  │  ← 终态，不可逆
            └──────────┘
```

### 6.4 F-STOCK-008 查询批次预警

#### 6.4.1 输入参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| alertType | String | 否 | ALL | ALL/LOW_STOCK/EXPIRY_SOON/EXPIRED |
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页数量 |

#### 6.4.2 数据操作（SQL级）

```sql
SELECT
    sal.id               AS alert_id,
    sal.alert_type       AS alert_type,
    sal.vaccine_id       AS vaccine_id,
    v.name               AS vaccine_name,
    sal.batch_id         AS batch_id,
    sal.batch_no         AS batch_no,
    sal.alert_value      AS alert_value,
    sal.expiry_date      AS expiry_date,
    sal.alert_time       AS alert_time,
    sal.synced_to_supplier AS synced_to_supplier,
    sal.is_handled       AS is_handled,
    su.name              AS handled_by_name,
    sal.handled_time     AS handled_time,
    sal.handle_remark    AS handle_remark
FROM stock_alert_log sal
JOIN vaccine v ON sal.vaccine_id = v.id
LEFT JOIN sys_user su ON sal.handled_by = su.id
WHERE (#{alertType} = 'ALL' OR sal.alert_type = #{alertType})
ORDER BY sal.alert_time DESC
LIMIT #{size} OFFSET #{offset};
```

#### 6.4.3 功能描述

查询满足预警条件的疫苗批次列表，支持按预警类型筛选。本接口为**只读查询**。

#### 6.4.4 处理流程

```
STEP 1: 参数校验
  ├─ alertType 枚举校验（EXPIRING/EXPIRED/LOW_STOCK/ALL）
  └─ 日期格式校验
  → 失败返回 1003 BAD_REQUEST

STEP 2: 构建预警查询条件（引用第6节预警规则）
  ├─ alertType=EXPIRING → expiry_date BETWEEN CURDATE() AND CURDATE() + INTERVAL 30 DAY
  ├─ alertType=EXPIRED → expiry_date < CURDATE() AND available_stock > 0
  ├─ alertType=LOW_STOCK → available_stock <= alert_threshold
  └─ alertType=ALL → 以上三种条件 UNION

STEP 3: 执行查询
  └─ SELECT vb.batch_no, vb.vaccine_name, vb.expiry_date,
      hvs.available_stock, hvs.locked_stock,
      CASE WHEN vb.expiry_date < CURDATE() THEN 'EXPIRED'
           WHEN vb.expiry_date <= CURDATE() + INTERVAL 30 DAY THEN 'EXPIRING'
           WHEN hvs.available_stock <= vb.alert_threshold THEN 'LOW_STOCK'
           END AS alert_type
      FROM vaccine_batch vb
      JOIN hospital_vaccine_stock hvs ON vb.id = hvs.batch_id
      WHERE [预警条件]
      ORDER BY CASE alert_type
          WHEN 'EXPIRED' THEN 1
          WHEN 'EXPIRING' THEN 2
          WHEN 'LOW_STOCK' THEN 3
          END, vb.expiry_date ASC

STEP 4: 组装返回结果
  └─ 返回预警批次列表 VO
```

#### 6.4.5 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 参数格式错误 | 1003 | 400 | 返回具体字段错误 |
| 未登录 | 1001 | 401 | 拦截器拦截 |
| 角色无权限 | 1002 | 403 | 返回"无权访问" |

#### 6.4.6 并发控制

本接口为只读查询，无需并发控制。

#### 6.4.7 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `stock.alert.view` |
| 数据权限 | 本院全部批次 |
| API前缀 | `/api/stock/*` — 仅 DOCTOR_STOCK 角色可访问 |

### 6.5 F-STOCK-009 标记预警已处理

#### 6.5.1 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| alertId | Long | 是 | 路径参数 | 预警ID |
| doctorId | Long | 是 | Token 解析 | 处理人ID |
| handleRemark | String | 否 | 请求体 | 处理备注 |

#### 6.5.2 数据操作（SQL级）

```sql
UPDATE stock_alert_log
SET is_handled    = 1,
    handled_by    = #{doctorId},
    handled_time  = NOW(),
    handle_remark = #{handleRemark}
WHERE id = #{alertId}
  AND is_handled = 0;
```

#### 6.5.3 功能描述

标记指定预警日志为已处理状态，记录处理措施和备注信息。

#### 6.5.4 处理流程

```
STEP 1: 参数校验
  ├─ alertId 非空且为正整数
  └─ handleMeasure 非空
  → 失败返回 1003 BAD_REQUEST

STEP 2: 查询预警日志
  ├─ SELECT id, is_handled, alert_type FROM stock_alert_log
  │   WHERE id = #{alertId}
  └─ 记录不存在 → 返回 1004 NOT_FOUND

STEP 3: 状态校验
  └─ is_handled = 1 → 返回 1005 CONFLICT（已处理，不可重复操作）

STEP 4: 更新预警日志
  └─ UPDATE stock_alert_log
      SET is_handled = 1, handle_measure = #{handleMeasure},
          handle_time = NOW(), handle_by = #{doctorId}
      WHERE id = #{alertId} AND is_handled = 0

STEP 5: 组装返回结果
  └─ 返回处理成功信息
```

#### 6.5.5 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 预警日志不存在 | 1004 | 404 | 返回"预警记录不存在" |
| 已处理不可重复操作 | 1005 | 409 | 返回"该预警已处理" |
| 参数格式错误 | 1003 | 400 | 返回具体字段错误 |
| 未登录 | 1001 | 401 | 拦截器拦截 |
| 角色无权限 | 1002 | 403 | 返回"无权访问" |

#### 6.5.6 并发控制

单行 UPDATE 操作使用 `AND is_handled = 0` 作为乐观锁条件，防止重复处理。MySQL 行锁自动保护。

#### 6.5.7 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `stock.alert.handle` |
| 数据权限 | 本院全部预警记录 |
| API前缀 | `/api/stock/*` — 仅 DOCTOR_STOCK 角色可访问 |

---

## 7. 数据一致性说明

### 7.1 库存不变量

> **以下约束在任何时刻都必须成立：**

```
1. available_stock >= 0
2. locked_stock >= 0
3. total_stock = available_stock + locked_stock（逻辑不变量）
4. available_stock >= locked_stock（登记时只加locked，接种时同时减两者）
```

### 7.2 库存变更操作矩阵

| 操作 | available_stock | locked_stock | 触发模块 | 事务编号 |
|------|----------------|-------------|----------|----------|
| **登记锁定** | 不变 | **+1** | REGISTER | T5 |
| **接种扣减** | **-1** | **-1** | VACCINATE | T6 |
| **取消释放** | 不变 | **-1** | APPOINTMENT | T2 |
| **调拨调出** | **-N** | 不变 | STOCK | T8 |
| **调拨调入** | **+N** | 不变 | STOCK | T8 |
| **批次销毁** | **-N** | **归零** | STOCK | T9 |

### 7.3 库存汇总表一致性策略

`hospital_vaccine_summary` 作为聚合表，采用**最终一致性**策略：

| 策略 | 说明 |
|------|------|
| **乐观锁** | `version` 字段防止并发更新覆盖 |
| **实时更新** | 调拨（T8）和销毁（T9）事务内同步更新 |
| **异步补偿** | 登记锁定（T5）和接种扣减（T6）操作后，通过汇总更新保证一致性 |

#### 7.3.1 汇总更新时机

| 触发场景 | 更新方式 | 说明 |
|----------|----------|------|
| 调拨完成（T8） | 事务内同步更新 | 调拨事务内 UPDATE summary |
| 批次销毁（T9） | 事务内同步更新 | 销毁事务内 UPDATE summary |
| 登记锁定（T5） | 事务内同步更新 | 锁库存后 UPDATE summary |
| 接种扣减（T6） | 事务内同步更新 | 扣库存后 UPDATE summary |
| 取消释放（T2） | 事务内同步更新 | 释放锁库存后 UPDATE summary |

#### 7.3.2 汇总更新SQL（统一模板）

```sql
-- 在库存变更事务内，最后一步更新汇总
UPDATE hospital_vaccine_summary
SET total_stock = (
      SELECT COALESCE(SUM(available_stock + locked_stock), 0)
      FROM hospital_vaccine_stock
      WHERE hospital_id = #{hospitalId} AND batch_id IN (
          SELECT id FROM vaccine_batch WHERE vaccine_id = #{vaccineId}
      )
  ),
  available_stock = (
      SELECT COALESCE(SUM(available_stock), 0)
      FROM hospital_vaccine_stock
      WHERE hospital_id = #{hospitalId} AND batch_id IN (
          SELECT id FROM vaccine_batch WHERE vaccine_id = #{vaccineId}
      )
  ),
  version = version + 1,
  update_time = NOW()
WHERE hospital_id = #{hospitalId}
  AND vaccine_id = #{vaccineId}
  AND version = #{currentVersion};
```

> **乐观锁冲突处理：** 当 `affected_rows = 0` 时，表示版本号已被其他事务修改。记录日志，由定时补偿任务重新计算。

#### 7.3.3 汇总一致性补偿任务

```sql
-- 定时任务（每5分钟执行），全量重算汇总表
-- 仅修正 version 不一致或数据漂移的记录
UPDATE hospital_vaccine_summary hvs
SET
  hvs.total_stock = (
      SELECT COALESCE(SUM(hs.available_stock + hs.locked_stock), 0)
      FROM hospital_vaccine_stock hs
      JOIN vaccine_batch vb ON hs.batch_id = vb.id
      WHERE hs.hospital_id = hvs.hospital_id AND vb.vaccine_id = hvs.vaccine_id
  ),
  hvs.available_stock = (
      SELECT COALESCE(SUM(hs.available_stock), 0)
      FROM hospital_vaccine_stock hs
      JOIN vaccine_batch vb ON hs.batch_id = vb.id
      WHERE hs.hospital_id = hvs.hospital_id AND vb.vaccine_id = hvs.vaccine_id
  ),
  hvs.version = hvs.version + 1,
  hvs.update_time = NOW();
```

### 7.4 销毁操作一致性（T9）

#### 7.4.1 F-STOCK-007 销毁批次

##### 处理流程

```
STEP 1: 参数校验
  ├─ batchId 非空且为正整数
  ├─ quantity 非空且为正整数
  ├─ disposeReason 非空且非空字符串
  → 失败返回 1003 BAD_REQUEST / 4005 STOCK_DISPOSE_REASON_EMPTY

STEP 2: 锁定批次行，校验状态（事务开始）
  ├─ SELECT id, status, vaccine_id FROM vaccine_batch WHERE id = #{batchId} FOR UPDATE
  ├─ 记录不存在 → 返回 4006 STOCK_BATCH_NOT_FOUND，ROLLBACK
  └─ status = 3 → 返回 4003 STOCK_BATCH_DISPOSED，ROLLBACK

STEP 3: 锁定库存行，校验销毁数量
  ├─ SELECT available_stock, locked_stock FROM hospital_vaccine_stock
  │   WHERE batch_id = #{batchId} FOR UPDATE
  └─ quantity > (available_stock + locked_stock) → 返回 4004 STOCK_DISPOSE_EXCEED，ROLLBACK

STEP 4: 判断全量销毁 or 部分销毁
  ├─ IF quantity >= (available_stock + locked_stock) THEN
  │   └─ 全量销毁：
  │       ├─ UPDATE vaccine_batch SET status = 3, update_time = NOW() WHERE id = #{batchId}
  │       └─ UPDATE hospital_vaccine_stock
  │           SET available_stock = 0, locked_stock = 0, update_time = NOW()
  │           WHERE batch_id = #{batchId}
  ├─ ELSE
  │   └─ 部分销毁：
  │       ├─ 不修改 vaccine_batch.status
  │       └─ UPDATE hospital_vaccine_stock
  │           SET available_stock = available_stock - #{quantity},
  │               update_time = NOW()
  │           WHERE batch_id = #{batchId} AND available_stock >= #{quantity}
  │           （不修改 locked_stock）
  └─ affected_rows = 0 → 返回 4010 STOCK_DB_ERROR，ROLLBACK

STEP 5: 记录销毁日志
  └─ INSERT INTO batch_dispose_log (...) VALUES (...)

STEP 6: 更新库存汇总
  └─ UPDATE hospital_vaccine_summary (同7.3.2模板)

STEP 7: 事务提交
  └─ COMMIT
```

##### 数据操作（SQL级）

```sql
-- ============================================
-- 事务 T9（批次销毁）
-- 隔离级别：READ_COMMITTED
-- 超时时间：30秒
-- ============================================
BEGIN;

-- STEP 2: 锁定批次行
SELECT id, status, vaccine_id
FROM vaccine_batch
WHERE id = #{batchId}
FOR UPDATE;
-- assert: status IN (0, 1, 2)

-- STEP 3: 锁定库存行
SELECT available_stock, locked_stock
FROM hospital_vaccine_stock
WHERE batch_id = #{batchId}
FOR UPDATE;
-- assert: quantity <= (available_stock + locked_stock)

-- STEP 4: 判断全量销毁 or 部分销毁
IF #{quantity} >= (available_stock + locked_stock) THEN
  -- 全量销毁
  UPDATE vaccine_batch SET status = 3, update_time = NOW() WHERE id = #{batchId};
  UPDATE hospital_vaccine_stock SET available_stock = 0, locked_stock = 0, update_time = NOW() WHERE batch_id = #{batchId};
ELSE
  -- 部分销毁：仅扣减 available_stock，不修改批次状态和 locked_stock
  UPDATE hospital_vaccine_stock
  SET available_stock = available_stock - #{quantity},
      update_time = NOW()
  WHERE batch_id = #{batchId}
    AND available_stock >= #{quantity};
END IF;

-- STEP 5: 记录销毁日志
INSERT INTO batch_dispose_log (
    dispose_no, batch_id, dispose_quantity, dispose_reason,
    operator_id, dispose_time, remark, create_time
) VALUES (
    #{disposeNo}, #{batchId}, #{quantity}, #{disposeReason},
    #{doctorId}, NOW(), #{remark}, NOW()
);

-- STEP 7: 更新库存汇总
UPDATE hospital_vaccine_summary
SET total_stock = (
      SELECT COALESCE(SUM(available_stock + locked_stock), 0)
      FROM hospital_vaccine_stock
      WHERE hospital_id = #{hospitalId} AND batch_id IN (
          SELECT id FROM vaccine_batch WHERE vaccine_id = #{vaccineId}
      )
  ),
  available_stock = (
      SELECT COALESCE(SUM(available_stock), 0)
      FROM hospital_vaccine_stock
      WHERE hospital_id = #{hospitalId} AND batch_id IN (
          SELECT id FROM vaccine_batch WHERE vaccine_id = #{vaccineId}
      )
  ),
  version = version + 1,
  update_time = NOW()
WHERE hospital_id = #{hospitalId}
  AND vaccine_id = #{vaccineId};

COMMIT;
```

##### 销毁号生成规则

| 格式 | 示例 | 说明 |
|------|------|------|
| DS + 8位日期 + 4位序号 | DS202604020001 | 唯一标识 |

##### 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 未登录 | 1001 | 401 | 拦截器拦截 |
| 无权限 | 1002 | 403 | 返回"无权限" |
| 参数格式错误 | 1003 | 400 | 返回具体字段错误 |
| 批次已销毁 | 4003 | 400 | 事务回滚，返回"该批次已销毁" |
| 销毁数量超限 | 4004 | 400 | 事务回滚，返回"销毁数量超过库存" |
| 销毁原因为空 | 4005 | 400 | 返回"请填写销毁原因" |
| 批次不存在 | 4006 | 404 | 事务回滚，返回"批次不存在" |
| 参数校验失败：{field_name} | 4007 | 400 | 返回具体字段错误 |
| 登录已过期 | 4008 | 401 | 清除Token，跳转登录页 |
| 调拨操作失败 | 4009 | 500 | 显示错误，引导重试，记录日志 |
| 数据库连接失败 | 4010 | 500 | 返回友好提示 |
| 数据库异常 | 4010 | 500 | 事务回滚，记录日志 |
| 并发冲突 | 4011 | 409 | 返回友好提示 |

### 7.5 并发风险与防护

| 场景 | 风险 | 防护手段 |
|------|------|----------|
| 多个库存医生同时调拨同一批次 | 调出库存超扣 | `SELECT FOR UPDATE` + `UPDATE WHERE available_stock >= #{quantity}` |
| 调拨与登记同时操作同一批次 | 库存数据不一致 | `SELECT FOR UPDATE` 行锁串行化 |
| 调拨与销毁同时操作同一批次 | 状态冲突 | `SELECT vaccine_batch FOR UPDATE` 校验状态 |
| 汇总表乐观锁冲突 | 更新丢失 | version 字段 + 定时补偿任务 |
| 预警扫描与库存变更并发 | 预警数据不准确 | 预警为参考数据，最终以实时查询为准 |

### 7.6 死锁预防

调拨和销毁操作涉及多行锁，遵循以下加锁顺序防止死锁：

```
加锁顺序（固定，不可变）：
  1. vaccine_batch 行锁（按 batch_id 升序）
  2. hospital_vaccine_stock 行锁（按 batch_id 升序）

原则：
  - 所有事务按相同顺序获取锁
  - 事务内锁的获取顺序一致
  - 事务超时时间 30秒，超时自动回滚释放锁
```

---

## 8. 权限控制汇总

### 8.1 功能权限矩阵

| 功能编号 | 权限编码 | 允许角色 | API路径 |
|----------|----------|----------|---------|
| F-STOCK-001 | `stock.view` | DOCTOR_STOCK | `GET /api/stock/summary` |
| F-STOCK-002 | `stock.view` | DOCTOR_STOCK | `GET /api/stock/batch/{batchId}/stock` |
| F-STOCK-003 | `batch.view` | DOCTOR_STOCK | `GET /api/stock/batch/list` |
| F-STOCK-004 | `batch.view` | DOCTOR_STOCK | `GET /api/stock/batch/{batchId}` |
| F-STOCK-005 | `stock.transfer` | DOCTOR_STOCK | `POST /api/stock/transfer/create` |
| F-STOCK-006 | `stock.transfer` | DOCTOR_STOCK | `GET /api/stock/transfer/list` |
| F-STOCK-007 | `stock.disposal` | DOCTOR_STOCK | `POST /api/stock/batch/dispose` |
| F-STOCK-008 | `stock.view` | DOCTOR_STOCK | `GET /api/stock/alert/list` |
| F-STOCK-009 | `stock.alert.handle` | DOCTOR_STOCK | `POST /api/stock/alert/{alertId}/handle` |
| F-STOCK-010 | `stats.view` | DOCTOR_BUSINESS_ADMIN | `GET /api/stock/statistics` |

### 8.2 数据权限规则

| 角色 | 数据范围 | 实现方式 |
|------|----------|----------|
| DOCTOR_STOCK | 所有批次和库存 | 无限制（全院库存管理） |
| DOCTOR_BUSINESS_ADMIN | 所有统计数据 | 无限制 |

### 8.3 API路径与角色映射

| API前缀 | 允许角色 |
|---------|----------|
| `/api/stock/*` | DOCTOR_STOCK |

### 8.4 权限校验流程

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
  ├─ 批次已销毁 → 返回 { code: 4003, message: "该批次已销毁" }
  ├─ 批次不存在 → 返回 { code: 4006, message: "批次不存在" }
  │
  ▼
执行业务操作
  │
  ▼
返回响应
```

---

## 9. 错误码定义

> 本模块错误码体系遵循 REQ-GLOBAL §4.5 库存模块错误码（4000-4099）和 §4.2 系统级错误码（1001/1002/1003/1007）。系统级与模块级错误码的分层处理策略参见 REQ-GLOBAL §4.11 分层错误处理策略。

### 9.1 本模块错误码段（4000-4099）

| 错误码 | 常量名 | 说明 |
|--------|--------|------|
| 4001 | STOCK_TRANSFER_SAME_LOCATION | 调出调入位置相同 |
| 4002 | STOCK_TRANSFER_INSUFFICIENT | 调出位置库存不足 |
| 4003 | STOCK_BATCH_DISPOSED | 批次已销毁 |
| 4004 | STOCK_DISPOSE_EXCEED | 销毁数量超过库存 |
| 4005 | STOCK_DISPOSE_REASON_EMPTY | 销毁原因为空 |
| 4006 | STOCK_BATCH_NOT_FOUND | 批次不存在 |
| 4007 | STOCK_PARAM_INVALID | 参数校验失败 |
| 4008 | STOCK_TOKEN_EXPIRED | 登录已过期 |
| 4009 | STOCK_TRANSFER_FAILED | 调拨执行失败 |
| 4010 | STOCK_DB_ERROR | 数据库异常 |
| 4011 | STOCK_CONFLICT | 并发冲突 |

### 9.2 共享错误码段（1900-1999）

本模块引用的跨模块共享错误码，定义于全局共享段：

| 错误码 | 常量名 | 说明 | 原属模块 |
|--------|--------|------|----------|

> 共享错误码段 1900-1999 用于跨模块复用的通用错误场景，避免模块间错误码段借用。原 REGISTER 段借用码（5001/5002/5003）已替换为共享码。详见 REQ-GLOBAL 错误码总表。

---

## 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| V1.0 | 2026-04-02 | 初始版本，基于 PRD-STOCK V1.3 / REQ-GLOBAL V1.0 生成 |
| V1.1 | 2026-04-03 | V3 评审修复：(1) 错误码对齐 GLOBAL §4；(2) 业务规则修正（销毁逻辑部分/全量分支/权限矩阵）；(3) 版本号/依赖版本对齐 |
| V1.2 | 2026-04-03 | REQ 评审修复：(1) 全文 11 处 1901 替换为 4009/4010/4011，§9.2 删除 1901 定义；(2) 文档头上游依赖同步至 REQ-GLOBAL V1.3 |

---

**文档结束**
