# 系统管理模块 — 研发需求文档

**文档编号:** REQ-ADMIN-001
**版本:** V1.2
**状态:** 正式发布
**日期:** 2026-04-03
**上游依赖:** PRD-ADMIN V2.2 / REQ-GLOBAL V1.3

---

## 目录

1. [模块定位与边界](#1-模块定位与边界)
2. [功能清单](#2-功能清单)
3. [RBAC 权限模型](#3-rbac-权限模型)
4. [F-ADMIN-101 创建角色](#4-f-admin-101-创建角色)
5. [F-ADMIN-102 修改角色](#5-f-admin-102-修改角色)
6. [F-ADMIN-103 查询角色列表](#6-f-admin-103-查询角色列表)
7. [F-ADMIN-104 删除角色](#7-f-admin-104-删除角色)
8. [F-ADMIN-105 分配权限给角色](#8-f-admin-105-分配权限给角色)
9. [F-ADMIN-106 查询角色权限](#9-f-admin-106-查询角色权限)
10. [F-ADMIN-107 分配角色给用户](#10-f-admin-107-分配角色给用户)
11. [F-ADMIN-108 查询用户角色与权限](#11-f-admin-108-查询用户角色与权限)
12. [F-ADMIN-201 查询系统配置](#12-f-admin-201-查询系统配置)
13. [F-ADMIN-202 更新系统配置](#13-f-admin-202-更新系统配置)
14. [系统配置项清单](#14-系统配置项清单)
15. [权限控制汇总](#15-权限控制汇总)
16. [管理模块错误码（8000-8999）](#16-管理模块错误码8000-8999)

---

## 1. 模块定位与边界

### 1.1 核心定位

ADMIN（系统管理）是系统权限与配置的核心模块，负责：
- **RBAC 权限模型** — 用户、角色、权限三层架构的定义与维护
- **角色管理** — 角色的增删改查、权限分配、用户角色绑定
- **系统配置** — 预约上限、冻结参数等全局运行参数的查询与修改

### 1.2 不包含内容

| 禁止范围 | 归属模块 |
|----------|----------|
| 医生排班管理 | ADMIN-SCHEDULE（PRD-ADMIN 第3节） |
| 窗口配置管理 | ADMIN-WINDOW（PRD-ADMIN 第4节） |
| 疫苗目录管理 | ADMIN-VACCINE（PRD-ADMIN 第5节） |
| 公告发布/审批 | ADMIN-NOTICE（PRD-ADMIN 第6节） |
| 统计分析 | ADMIN-STATS（PRD-ADMIN 第7节） |
| 用户注册/登录 | USER |
| 预约创建/取消 | APPOINTMENT |

### 1.3 涉及角色

| 角色编码 | 角色名称 | 使用场景 |
|----------|----------|----------|
| SUPER_ADMIN | 系统管理员 | 角色管理、权限分配、系统配置（全部权限） |
| DOCTOR_BUSINESS_ADMIN | 业务管理医生 | 分配医生角色、分配医生权限 |

---

## 2. 功能清单

| 编号 | 功能名称 | API | 角色 | 优先级 |
|------|----------|-----|------|--------|
| F-ADMIN-101 | 创建角色 | `POST /api/admin/role` | SUPER_ADMIN | P0 |
| F-ADMIN-102 | 修改角色 | `PUT /api/admin/role/{roleId}` | SUPER_ADMIN | P0 |
| F-ADMIN-103 | 查询角色列表 | `GET /api/admin/role` | SUPER_ADMIN | P0 |
| F-ADMIN-104 | 删除角色 | `DELETE /api/admin/role/{roleId}` | SUPER_ADMIN | P1 |
| F-ADMIN-105 | 分配权限给角色 | `PUT /api/admin/role/{roleId}/permissions` | SUPER_ADMIN | P0 |
| F-ADMIN-106 | 查询角色权限 | `GET /api/admin/role/{roleId}/permissions` | SUPER_ADMIN | P0 |
| F-ADMIN-107 | 分配角色给用户 | `PUT /api/admin/user/{userId}/roles` | SUPER_ADMIN, DOCTOR_BUSINESS_ADMIN | P0 |
| F-ADMIN-108 | 查询用户角色与权限 | `GET /api/admin/user/{userId}/roles` | SUPER_ADMIN, DOCTOR_BUSINESS_ADMIN | P0 |
| F-ADMIN-201 | 查询系统配置 | `GET /api/admin/config` | SUPER_ADMIN | P0 |
| F-ADMIN-202 | 更新系统配置 | `PUT /api/admin/config` | SUPER_ADMIN | P0 |
| F-ADMIN-001 | 创建排班 | `POST /api/business/schedule` | DOCTOR_SCHEDULE, DOCTOR_BUSINESS_ADMIN | P0 |
| F-ADMIN-002 | 修改排班 | `PUT /api/business/schedule/{id}` | DOCTOR_SCHEDULE, DOCTOR_BUSINESS_ADMIN | P0 |
| F-ADMIN-003 | 查询排班 | `GET /api/business/schedule` | DOCTOR_SCHEDULE, DOCTOR_BUSINESS_ADMIN | P0 |
| F-ADMIN-004 | 删除排班 | `DELETE /api/business/schedule/{id}` | DOCTOR_BUSINESS_ADMIN | P1 |
| F-ADMIN-005 | 排班冲突检测 | `POST /api/business/schedule/conflict` | DOCTOR_SCHEDULE, DOCTOR_BUSINESS_ADMIN | P0 |
| F-ADMIN-006 | 新增窗口 | `POST /api/business/window` | DOCTOR_BUSINESS_ADMIN | P0 |
| F-ADMIN-007 | 修改窗口 | `PUT /api/business/window/{id}` | DOCTOR_BUSINESS_ADMIN | P0 |
| F-ADMIN-008 | 删除窗口 | `DELETE /api/business/window/{id}` | DOCTOR_BUSINESS_ADMIN | P1 |
| F-ADMIN-009 | 查询窗口 | `GET /api/business/window` | DOCTOR_BUSINESS_ADMIN | P0 |
| F-ADMIN-010 | 配置窗口服务 | `POST /api/business/window/service` | DOCTOR_BUSINESS_ADMIN | P0 |
| F-ADMIN-011 | 新增疫苗 | `POST /api/business/vaccine` | DOCTOR_BUSINESS_ADMIN | P0 |
| F-ADMIN-012 | 修改疫苗 | `PUT /api/business/vaccine/{id}` | DOCTOR_BUSINESS_ADMIN | P0 |
| F-ADMIN-013 | 删除疫苗 | `DELETE /api/business/vaccine/{id}` | DOCTOR_BUSINESS_ADMIN | P1 |
| F-ADMIN-014 | 查询疫苗 | `GET /api/business/vaccine` | DOCTOR_BUSINESS_ADMIN | P0 |
| F-ADMIN-015 | 疫苗上下架 | `PUT /api/business/vaccine/{id}/status` | DOCTOR_BUSINESS_ADMIN | P0 |
| F-ADMIN-016 | 发布公告 | `POST /api/business/notice` | DOCTOR_BUSINESS_ADMIN | P0 |
| F-ADMIN-017 | 审批公告 | `PUT /api/admin/notice/{id}/approve` | SUPER_ADMIN | P0 |
| F-ADMIN-018 | 查询公告 | `GET /api/business/notice` | DOCTOR_BUSINESS_ADMIN, SUPER_ADMIN | P0 |
| F-ADMIN-019 | 删除公告 | `DELETE /api/admin/notice/{id}` | DOCTOR_BUSINESS_ADMIN | P1 |
| F-ADMIN-020 | 查看公告反馈 | `GET /api/admin/notice/{id}/feedback` | SUPER_ADMIN | P1 |
| F-ADMIN-021 | 接种统计 | `POST /api/stats/vaccination` | DOCTOR_BUSINESS_ADMIN, SUPER_ADMIN | P1 |
| F-ADMIN-022 | 库存统计 | `POST /api/stats/stock` | DOCTOR_BUSINESS_ADMIN, SUPER_ADMIN | P1 |
| F-ADMIN-023 | 效率统计 | `POST /api/stats/efficiency` | DOCTOR_BUSINESS_ADMIN, SUPER_ADMIN | P1 |
| F-ADMIN-024 | 异常统计 | `POST /api/stats/anomaly` | DOCTOR_BUSINESS_ADMIN, SUPER_ADMIN | P1 |

---

## 3. RBAC 权限模型

### 3.1 三层架构

```
用户(User) ──N:N──> 角色(Role) ──N:N──> 权限(Permission)
     │                    │                    │
     │                    │                    │
  sys_user           sys_role           sys_permission
  sys_user_role      sys_role_permission
```

### 3.2 数据表模型

#### sys_role（角色表）

| 字段名 | 中文名 | 数据类型 | 长度 | 必填 | 默认值 | 说明 |
|--------|--------|----------|------|------|--------|------|
| id | 角色ID | bigint | - | 是 | — | 主键，自增 |
| role_code | 角色编码 | varchar | 50 | 是 | — | 唯一，如 `DOCTOR_SIGNIN` |
| role_name | 角色名称 | varchar | 100 | 是 | — | 如 `签到医生` |
| role_group | 角色分组 | varchar | 20 | 是 | — | 枚举：USER/DOCTOR/ADMIN |
| description | 角色描述 | varchar | 500 | 否 | NULL | — |
| status | 状态 | tinyint | - | 是 | 0 | 0=启用，1=禁用 |
| is_system | 是否系统内置 | tinyint | - | 是 | 0 | 0=自定义，1=系统内置（不可删除） |
| create_time | 创建时间 | datetime | - | 是 | NOW() | — |
| update_time | 更新时间 | datetime | - | 是 | NOW() ON UPDATE NOW() | — |

#### sys_permission（权限表）

| 字段名 | 中文名 | 数据类型 | 长度 | 必填 | 默认值 | 说明 |
|--------|--------|----------|------|------|--------|------|
| id | 权限ID | bigint | - | 是 | — | 主键，自增 |
| permission_code | 权限编码 | varchar | 100 | 是 | — | 唯一，如 `appointment.book` |
| permission_name | 权限名称 | varchar | 100 | 是 | — | 如 `预约接种` |
| module | 所属模块 | varchar | 50 | 是 | — | 如 APPOINTMENT、FLOW、STOCK 等 |
| description | 权限描述 | varchar | 500 | 否 | NULL | — |
| create_time | 创建时间 | datetime | - | 是 | NOW() | — |

#### sys_user_role（用户-角色关联表）

| 字段名 | 中文名 | 数据类型 | 长度 | 必填 | 默认值 | 说明 |
|--------|--------|----------|------|------|--------|------|
| id | 主键 | bigint | - | 是 | — | 自增 |
| user_id | 用户ID | bigint | - | 是 | — | 外键→sys_user.id |
| role_id | 角色ID | bigint | - | 是 | — | 外键→sys_role.id |
| create_time | 创建时间 | datetime | - | 是 | NOW() | — |

**唯一约束：** `UNIQUE KEY uk_user_role (user_id, role_id)`

#### sys_role_permission（角色-权限关联表）

| 字段名 | 中文名 | 数据类型 | 长度 | 必填 | 默认值 | 说明 |
|--------|--------|----------|------|------|--------|------|
| id | 主键 | bigint | - | 是 | — | 自增 |
| role_id | 角色ID | bigint | - | 是 | — | 外键→sys_role.id |
| permission_id | 权限ID | bigint | - | 是 | — | 外键→sys_permission.id |
| create_time | 创建时间 | datetime | - | 是 | NOW() | — |

**唯一约束：** `UNIQUE KEY uk_role_permission (role_id, permission_id)`

#### sys_config（系统配置表）

| 字段名 | 中文名 | 数据类型 | 长度 | 必填 | 默认值 | 说明 |
|--------|--------|----------|------|------|--------|------|
| id | 主键 | bigint | - | 是 | — | 自增 |
| config_key | 配置键 | varchar | 100 | 是 | — | 唯一，如 `appointment.max_capacity` |
| config_value | 配置值 | varchar | 500 | 是 | — | — |
| config_desc | 配置描述 | varchar | 500 | 是 | — | — |
| value_type | 值类型 | varchar | 20 | 是 | — | 枚举：INT/STRING/BOOLEAN |
| update_time | 更新时间 | datetime | - | 是 | NOW() ON UPDATE NOW() | — |

### 3.3 预置角色

| 角色编码 | 角色名称 | 分组 | 是否系统内置 | 说明 |
|----------|----------|------|-------------|------|
| USER | 用户 | USER | 是 | 家长APP端用户 |
| DOCTOR_SIGNIN | 签到医生 | DOCTOR | 是 | 签到窗口操作 |
| DOCTOR_PRECHECK | 预检医生 | DOCTOR | 是 | 预检窗口操作 |
| DOCTOR_REGISTER | 登记医生 | DOCTOR | 是 | 登记窗口操作 |
| DOCTOR_VACCINATE | 接种医生 | DOCTOR | 是 | 接种窗口操作 |
| DOCTOR_OBSERVE | 留观医生 | DOCTOR | 是 | 留观室操作 |
| DOCTOR_STOCK | 库存管理医生 | DOCTOR | 是 | 库存窗口操作 |
| DOCTOR_SCHEDULE | 排班医生 | DOCTOR | 是 | 排班管理 |
| DOCTOR_BUSINESS_ADMIN | 业务管理医生 | ADMIN | 是 | 医生权限分配、窗口配置、排班管理 |
| SUPER_ADMIN | 系统管理员 | ADMIN | 是 | 系统最高权限 |

### 3.4 预置权限

> 以下权限编码与 REQ-GLOBAL 第9.3节权限矩阵完全对应，是全系统的唯一权限来源。

| 模块 | 权限编码 | 权限名称 |
|------|----------|----------|
| APPOINTMENT | `appointment.book` | 预约接种 |
| APPOINTMENT | `appointment.cancel.own` | 取消自己的预约 |
| APPOINTMENT | `appointment.view.own` | 查看自己的预约 |
| FLOW | `appointment.signin` | 用户签到 |
| FLOW | `appointment.confirm` | 预约确认 |
| FLOW | `appointment.view.today` | 查看今日预约 |
| FLOW | `appointment.view.queue` | 查看待处理队列 |
| FLOW | `appointment.view.register` | 查看待登记队列 |
| FLOW | `appointment.view.vaccinate` | 查看待接种队列 |
| FLOW | `appointment.view.observe` | 查看留观队列 |
| FLOW | `precheck.assess` | 预检评估 |
| FLOW | `precheck.contraindication` | 禁忌筛查 |
| FLOW | `precheck.result.view` | 查看预检结果 |
| FLOW | `observe.manage` | 留观管理 |
| FLOW | `observe.finish` | 留观结束确认 |
| FLOW | `adverse.report` | 不良反应上报 |
| FLOW | `adverse.handle` | 不良反应处理 |
| REGISTER | `register.verify` | 登记核实 |
| REGISTER | `register.batch.assign` | 批次分配 |
| REGISTER | `register.queue.manage` | 排队管理 |
| REGISTER | `register.view` | 查看登记详情 |
| REGISTER | `register.save` | 保存登记记录 |
| REGISTER | `stock.lock` | 锁定批次库存 |
| USER | `child.view.own` | 查看自己的儿童档案 |
| USER | `child.add.own` | 添加自己的儿童档案 |
| USER | `child.edit.own` | 修改自己的儿童档案 |
| USER | `child.delete.own` | 删除自己的儿童档案 |
| VACCINATE | `vaccinate.execute` | 执行接种 |
| VACCINATE | `vaccinate.record` | 记录接种信息 |
| VACCINATE | `vaccinate.view` | 查看接种详情 |
| VACCINATE | `vaccinate.verify` | 核实儿童/预约/批次信息 |
| VACCINATE | `vaccinate.site.select` | 选择接种部位 |
| VACCINATE | `vaccinate.id.generate` | 生成注射号 |
| VACCINATE | `stock.deduct` | 扣减库存 |
| VACCINATE | `record.view.own` | 查看自己的接种记录 |
| VACCINATE | `record.view.child` | 查看儿童接种记录 |
| STOCK | `stock.view` | 查看库存 |
| STOCK | `stock.transfer` | 库存调拨 |
| STOCK | `stock.disposal` | 批次销毁 |
| STOCK | `batch.manage` | 批次管理 |
| STOCK | `batch.view` | 查看批次列表和详情 |
| STOCK | `stock.transfer.create` | 创建调拨单 |
| STOCK | `stock.transfer.confirm` | 确认调拨 |
| STOCK | `stock.transfer.view` | 查看调拨记录 |
| STOCK | `batch.disposal` | 批次销毁 |
| STOCK | `stock.alert.view` | 查看库存预警 |
| SCHEDULE | `doctor.schedule.view` | 查看排班 |
| SCHEDULE | `doctor.schedule.create` | 创建排班 |
| SCHEDULE | `doctor.schedule.edit` | 修改排班 |
| SCHEDULE | `doctor.schedule.delete` | 删除排班 |
| ADMIN | `doctor.assign.role` | 分配医生角色 |
| ADMIN | `doctor.assign.permission` | 分配医生权限 |
| ADMIN | `window.manage` | 窗口管理 |
| ADMIN | `window.service.manage` | 窗口服务管理 |
| ADMIN | `vaccine.catalog.manage` | 疫苗目录管理 |
| ADMIN | `vaccine.catalog.view` | 疫苗目录查看 |
| ADMIN | `notice.manage` | 公告管理 |
| ADMIN | `notice.audit` | 公告审批 |
| ADMIN | `notice.view` | 公告查看 |
| ADMIN | `notice.feedback` | 公告反馈查看 |
| ADMIN | `stats.view` | 统计分析 |
| ADMIN | `role.manage` | 角色管理 |
| ADMIN | `permission.manage` | 权限管理 |
| ADMIN | `config.manage` | 系统配置管理 |
| ADMIN | `user.manage` | 用户管理 |
| ADMIN | `all.data.view` | 全局数据查看 |

### 3.5 预置角色-权限绑定

> 系统初始化时自动写入 `sys_role_permission`，数据来源为 REQ-GLOBAL 第9.3节权限矩阵。此处不逐一列举，仅说明绑定规则：

```
初始化流程：
  FOR EACH role IN 预置角色列表:
    FOR EACH permission IN 权限矩阵中该角色标记为 Y 的权限:
      INSERT INTO sys_role_permission (role_id, permission_id)
```

### 3.6 权限校验流程

> 本流程为 REQ-GLOBAL §9.6 权限校验流程在本模块的具体实现。

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
  ├─ 数据越权 → 返回对应模块错误码
  │
  ▼
执行业务操作
  │
  ▼
返回响应
```

### 3.7 接口级权限控制实现

```java
/**
 * 权限校验注解 — 标注在 Controller 方法上
 * 拦截器在请求到达 Controller 前校验当前用户是否持有该权限
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    String value();  // 权限编码，如 "appointment.book"
}
```

```java
/**
 * 权限校验拦截器伪代码
 */
public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    // 1. 解析 Token
    Long userId = tokenService.parseToken(request.getHeader("Authorization"));
    if (userId == null) {
        throw new BusinessException(1001, "未登录或登录已过期");
    }

    // 2. 获取用户权限列表（带缓存）
    Set<String> permissions = permissionService.getUserPermissions(userId);
    if (permissions == null) {
        throw new BusinessException(1002, "无权限访问");
    }

    // 3. 校验接口权限
    RequirePermission annotation = getAnnotation(handler);
    if (annotation != null) {
        if (!permissions.contains(annotation.value())) {
            throw new BusinessException(1002, "无权限访问");
        }
    }

    // 4. 将 userId 和 permissions 存入请求上下文
    RequestContextHolder.setUserId(userId);
    RequestContextHolder.setPermissions(permissions);

    return true;
}
```

### 3.8 API 路径与角色前缀映射

| API前缀 | 允许角色 | 说明 |
|---------|----------|------|
| `/api/user/*` | USER | 用户端接口 |
| `/api/signin/*` | DOCTOR_SIGNIN | 签到窗口 |
| `/api/precheck/*` | DOCTOR_PRECHECK | 预检窗口 |
| `/api/register/*` | DOCTOR_REGISTER | 登记窗口 |
| `/api/vaccinate/*` | DOCTOR_VACCINATE | 接种窗口 |
| `/api/observe/*` | DOCTOR_OBSERVE | 留观窗口 |
| `/api/stock/*` | DOCTOR_STOCK | 库存窗口 |
| `/api/schedule/*` | DOCTOR_SCHEDULE, DOCTOR_BUSINESS_ADMIN | 排班 |
| `/api/business/*` | DOCTOR_BUSINESS_ADMIN | 业务管理 |
| `/api/stats/*` | DOCTOR_BUSINESS_ADMIN, SUPER_ADMIN | 统计分析 |
| `/api/admin/*` | SUPER_ADMIN | 系统管理（仅本模块） |
| `/api/public/*` | 无需认证 | 公开接口 |

> **注意：** API 前缀拦截是第一层粗粒度过滤，`@RequirePermission` 注解是第二层细粒度校验。两者共同保证接口级权限控制。

---

## 4. F-ADMIN-101 创建角色

### 4.1 功能描述

系统管理员创建新的自定义角色，角色创建后不自动分配任何权限，需通过 F-ADMIN-105 单独分配。

### 4.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 操作者为 SUPER_ADMIN | Token 解析 + 角色校验 |
| PRE-002 | 角色编码不存在 | `sys_role.role_code = #{roleCode}` 不存在 |

### 4.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| roleCode | String | 是 | 请求体 | 角色编码，如 `DOCTOR_NURSE` |
| roleName | String | 是 | 请求体 | 角色名称，如 `护士` |
| roleGroup | String | 是 | 请求体 | 角色分组：USER/DOCTOR/ADMIN |
| description | String | 否 | 请求体 | 角色描述 |

### 4.4 处理流程

```
STEP 1: 参数校验
  ├─ roleCode 格式校验（字母、数字、下划线，2-50位）
  ├─ roleName 非空且长度 <= 100
  ├─ roleGroup 枚举校验（USER/DOCTOR/ADMIN）
  → 失败返回 1003 BAD_REQUEST

STEP 2: 角色编码唯一性校验
  ├─ SELECT id FROM sys_role WHERE role_code = #{roleCode}
  └─ 已存在 → 返回 8008 ROLE_CODE_DUPLICATE

STEP 3: 插入角色记录
  └─ INSERT INTO sys_role (role_code, role_name, role_group, description, status, is_system) VALUES (...)
  └─ is_system = 0（自定义角色）

STEP 4: 返回结果
  └─ 返回角色ID、角色详情
```

### 4.5 数据操作（SQL级）

```sql
INSERT INTO sys_role (role_code, role_name, role_group, description, status, is_system, create_time, update_time)
VALUES (#{roleCode}, #{roleName}, #{roleGroup}, #{description}, 0, 0, NOW(), NOW());
```

### 4.6 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 角色编码已存在 | 8008 | 409 | 返回"角色编码已存在" |
| 参数格式错误 | 1003 | 400 | 返回具体字段错误 |
| 无权限 | 1002 | 403 | 仅 SUPER_ADMIN 可操作 |

### 4.7 并发控制

角色编码唯一约束由数据库唯一索引保证，无需额外并发控制。

### 4.8 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `role.manage` |
| API前缀 | `/api/admin/*` — 仅 SUPER_ADMIN 可访问 |

---

## 5. F-ADMIN-102 修改角色

### 5.1 功能描述

修改角色基本信息（名称、描述、分组）。不支持修改角色编码。

### 5.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 操作者为 SUPER_ADMIN | Token 解析 + 角色校验 |
| PRE-002 | 角色存在 | `sys_role.id = #{roleId}` |

### 5.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| roleId | Long | 是 | 路径参数 | 角色ID |
| roleName | String | 是 | 请求体 | 角色名称 |
| roleGroup | String | 是 | 请求体 | 角色分组：USER/DOCTOR/ADMIN |
| description | String | 否 | 请求体 | 角色描述 |

### 5.4 处理流程

```
STEP 1: 参数校验
  ├─ roleId 非空且为正整数
  ├─ roleName 非空且长度 <= 100
  ├─ roleGroup 枚举校验（USER/DOCTOR/ADMIN）
  → 失败返回 1003 BAD_REQUEST

STEP 2: 角色存在性校验
  ├─ SELECT id, is_system FROM sys_role WHERE id = #{roleId}
  └─ 不存在 → 返回 8009 ROLE_NOT_FOUND

STEP 3: 执行修改
  └─ UPDATE sys_role SET role_name = #{roleName}, role_group = #{roleGroup}, description = #{description}, update_time = NOW() WHERE id = #{roleId}

STEP 4: 返回结果
  └─ 返回修改后的角色详情
```

### 5.5 数据操作（SQL级）

```sql
UPDATE sys_role
SET role_name = #{roleName},
    role_group = #{roleGroup},
    description = #{description},
    update_time = NOW()
WHERE id = #{roleId};
```

### 5.6 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 角色不存在 | 8009 | 404 | 返回"角色不存在" |
| 参数格式错误 | 1003 | 400 | 返回具体字段错误 |

### 5.7 并发控制

本接口为简单更新操作，无需额外并发控制。

### 5.8 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `role.manage` |
| API前缀 | `/api/admin/*` — 仅 SUPER_ADMIN 可访问 |

---

## 6. F-ADMIN-103 查询角色列表

### 6.1 功能描述

查询系统角色列表，支持按角色分组、状态筛选和分页查询。

### 6.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 操作者为 SUPER_ADMIN | Token 解析 + 角色校验 |

### 6.3 输入参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| roleGroup | String | 否 | ALL | 角色分组筛选：ALL/USER/DOCTOR/ADMIN |
| status | Integer | 否 | ALL | 状态筛选：ALL=全部/0=启用/1=禁用 |
| keyword | String | 否 | — | 关键词搜索（编码或名称模糊匹配） |
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 10 | 每页数量，最大50 |

### 6.4 处理流程

```
STEP 1: 参数校验
  ├─ roleGroup 枚举校验（ALL/USER/DOCTOR/ADMIN）
  ├─ status 枚举校验（ALL/0/1）
  ├─ page >= 1, size ∈ [1, 50]
  → 失败返回 1003 BAD_REQUEST

STEP 2: 构建查询条件
  ├─ roleGroup != ALL → AND role_group = #{roleGroup}
  ├─ status != ALL → AND status = #{status}
  └─ keyword != NULL → AND (role_code LIKE CONCAT('%',#{keyword},'%') OR role_name LIKE CONCAT('%',#{keyword},'%'))

STEP 3: 查询总数
  └─ SELECT COUNT(*) FROM sys_role WHERE 1=1 [动态条件]

STEP 4: 查询分页数据
  └─ SELECT * FROM sys_role WHERE 1=1 [动态条件] ORDER BY create_time DESC LIMIT #{offset}, #{size}

STEP 5: 组装分页响应
  └─ 返回 records + total + page + size + pages
```

### 6.5 数据操作（SQL级）

```sql
-- 查询总数
SELECT COUNT(*)
FROM sys_role
WHERE 1=1
  AND role_group = #{roleGroup}        -- 动态
  AND status = #{status}               -- 动态
  AND (role_code LIKE #{keywordPattern} OR role_name LIKE #{keywordPattern})  -- 动态;

-- 查询分页数据
SELECT id, role_code, role_name, role_group, description, status, is_system, create_time, update_time
FROM sys_role
WHERE 1=1
  AND role_group = #{roleGroup}        -- 动态
  AND status = #{status}               -- 动态
  AND (role_code LIKE #{keywordPattern} OR role_name LIKE #{keywordPattern})  -- 动态
ORDER BY create_time DESC
LIMIT #{offset}, #{size};
```

### 6.6 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 参数格式错误 | 1003 | 400 | 返回具体字段错误 |

### 6.7 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `role.manage` |
| API前缀 | `/api/admin/*` — 仅 SUPER_ADMIN 可访问 |

---

## 7. F-ADMIN-104 删除角色

### 7.1 功能描述

删除自定义角色。系统内置角色（`is_system=1`）不可删除。已绑定用户的角色不可删除。

### 7.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 操作者为 SUPER_ADMIN | Token 解析 + 角色校验 |
| PRE-002 | 角色存在 | `sys_role.id = #{roleId}` |
| PRE-003 | 角色非系统内置 | `sys_role.is_system = 0` |
| PRE-004 | 角色下无绑定用户 | `sys_user_role` 中无该角色记录 |

### 7.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| roleId | Long | 是 | 路径参数 | 角色ID |

### 7.4 处理流程

```
STEP 1: 参数校验
  └─ roleId 非空且为正整数
  → 失败返回 1003 BAD_REQUEST

STEP 2: 角色存在性校验
  ├─ SELECT id, is_system, role_name FROM sys_role WHERE id = #{roleId}
  └─ 不存在 → 返回 8009 ROLE_NOT_FOUND

STEP 3: 系统角色校验
  └─ is_system = 1 → 返回 8010 ROLE_SYSTEM_PROTECTED

STEP 4: 用户绑定校验
  ├─ SELECT COUNT(*) FROM sys_user_role WHERE role_id = #{roleId}
  └─ count > 0 → 返回 8011 ROLE_IN_USE

STEP 5: 执行删除（事务内）
  ├─ DELETE FROM sys_role_permission WHERE role_id = #{roleId}
  └─ DELETE FROM sys_role WHERE id = #{roleId}

STEP 6: 返回结果
  └─ 返回删除成功确认
```

### 7.5 数据操作（SQL级）

```sql
-- 事务开始
BEGIN;

-- STEP 5: 先删关联权限，再删角色
DELETE FROM sys_role_permission WHERE role_id = #{roleId};
DELETE FROM sys_role WHERE id = #{roleId};

COMMIT;
```

### 7.6 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 角色不存在 | 8009 | 404 | 返回"角色不存在" |
| 系统角色不可删除 | 8010 | 409 | 返回"系统内置角色不可删除" |
| 角色已绑定用户 | 8011 | 409 | 返回"该角色已分配给用户，无法删除" |

### 7.7 并发控制

删除操作使用事务保证 `sys_role_permission` 和 `sys_role` 的级联删除一致性。

### 7.8 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `role.manage` |
| API前缀 | `/api/admin/*` — 仅 SUPER_ADMIN 可访问 |

---

## 8. F-ADMIN-105 分配权限给角色

### 8.1 功能描述

为指定角色分配一组权限。采用**全量覆盖**策略：每次调用替换该角色的全部权限。

### 8.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 操作者为 SUPER_ADMIN | Token 解析 + 角色校验 |
| PRE-002 | 角色存在 | `sys_role.id = #{roleId}` |
| PRE-003 | 所有权限ID均存在 | `sys_permission.id IN (...)` |

### 8.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| roleId | Long | 是 | 路径参数 | 角色ID |
| permissionIds | List\<Long\> | 是 | 请求体 | 权限ID列表，可为空数组（清空权限） |

### 8.4 处理流程

```
STEP 1: 参数校验
  ├─ roleId 非空且为正整数
  ├─ permissionIds 非空（允许空数组）
  → 失败返回 1003 BAD_REQUEST

STEP 2: 角色存在性校验
  ├─ SELECT id FROM sys_role WHERE id = #{roleId}
  └─ 不存在 → 返回 8009 ROLE_NOT_FOUND

STEP 3: 权限存在性校验（permissionIds 非空时）
  ├─ SELECT COUNT(*) FROM sys_permission WHERE id IN (#{permissionIds})
  └─ count != permissionIds.size() → 返回 8012 PERMISSION_NOT_FOUND

STEP 4: 全量替换权限（事务内）
  ├─ DELETE FROM sys_role_permission WHERE role_id = #{roleId}
  ├─ IF permissionIds 非空:
  │   └─ 批量 INSERT INTO sys_role_permission (role_id, permission_id) VALUES (...)
  └─ ELSE:
        → 已清空，无需插入

STEP 5: 清除该角色的权限缓存
  └─ 清除 Redis 中该角色的权限缓存 key

STEP 6: 返回结果
  └─ 返回当前角色的权限列表
```

### 8.5 数据操作（SQL级）

```sql
-- 事务开始
BEGIN;

-- STEP 4a: 清空旧权限
DELETE FROM sys_role_permission WHERE role_id = #{roleId};

-- STEP 4b: 批量插入新权限（permissionIds 非空时）
INSERT INTO sys_role_permission (role_id, permission_id, create_time)
VALUES
    (#{roleId}, #{permissionId1}, NOW()),
    (#{roleId}, #{permissionId2}, NOW()),
    ...;

COMMIT;
```

### 8.6 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 角色不存在 | 8009 | 404 | 返回"角色不存在" |
| 权限不存在 | 8012 | 404 | 返回"部分权限ID不存在" |
| 系统异常 | 1007 | 500 | 事务回滚 |

### 8.7 并发控制

事务保证全量替换的原子性。权限缓存清除在事务提交后执行。

### 8.8 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `permission.manage` |
| API前缀 | `/api/admin/*` — 仅 SUPER_ADMIN 可访问 |

---

## 9. F-ADMIN-106 查询角色权限

### 9.1 功能描述

查询指定角色当前拥有的全部权限列表。

### 9.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 操作者为 SUPER_ADMIN | Token 解析 + 角色校验 |
| PRE-002 | 角色存在 | `sys_role.id = #{roleId}` |

### 9.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| roleId | Long | 是 | 路径参数 | 角色ID |

### 9.4 处理流程

```
STEP 1: 参数校验
  └─ roleId 非空且为正整数
  → 失败返回 1003 BAD_REQUEST

STEP 2: 查询角色权限
  ├─ SELECT p.id, p.permission_code, p.permission_name, p.module, p.description
  │   FROM sys_permission p
  │   INNER JOIN sys_role_permission rp ON p.id = rp.permission_id
  │   WHERE rp.role_id = #{roleId}
  │   ORDER BY p.module, p.permission_code
  └─ 角色不存在 → 返回 8009 ROLE_NOT_FOUND（通过先查角色表校验）

STEP 3: 返回结果
  └─ 返回权限列表
```

### 9.5 数据操作（SQL级）

```sql
-- 角色存在性校验
SELECT id FROM sys_role WHERE id = #{roleId};

-- 查询角色权限
SELECT p.id, p.permission_code, p.permission_name, p.module, p.description
FROM sys_permission p
INNER JOIN sys_role_permission rp ON p.id = rp.permission_id
WHERE rp.role_id = #{roleId}
ORDER BY p.module, p.permission_code;
```

### 9.6 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 角色不存在 | 8009 | 404 | 返回"角色不存在" |

### 9.7 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `role.manage` |
| API前缀 | `/api/admin/*` — 仅 SUPER_ADMIN 可访问 |

---

## 10. F-ADMIN-107 分配角色给用户

### 10.1 功能描述

为指定用户分配一组角色。采用**全量覆盖**策略：每次调用替换该用户的全部角色。

**业务约束：** `SUPER_ADMIN` 角色只能由 `SUPER_ADMIN` 分配；`DOCTOR_BUSINESS_ADMIN` 可分配医生角色（DOCTOR_*），不可分配 `SUPER_ADMIN`。

### 10.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 操作者为 SUPER_ADMIN 或 DOCTOR_BUSINESS_ADMIN | Token 解析 + 角色校验 |
| PRE-002 | 目标用户存在 | `sys_user.id = #{userId}` |
| PRE-003 | 所有角色ID均存在 | `sys_role.id IN (...)` |
| PRE-004 | 权限边界校验 | BIZ_ADMIN 不可分配 SUPER_ADMIN 角色 |

### 10.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| userId | Long | 是 | 路径参数 | 目标用户ID |
| roleIds | List\<Long\> | 是 | 请求体 | 角色ID列表，可为空数组（清空角色） |

### 10.4 处理流程

```
STEP 1: 参数校验
  ├─ userId 非空且为正整数
  ├─ roleIds 非空（允许空数组）
  → 失败返回 1003 BAD_REQUEST

STEP 2: 用户存在性校验
  ├─ SELECT id FROM sys_user WHERE id = #{userId}
  └─ 不存在 → 返回 1004 NOT_FOUND

STEP 3: 角色存在性校验（roleIds 非空时）
  ├─ SELECT id, role_code FROM sys_role WHERE id IN (#{roleIds})
  └─ count != roleIds.size() → 返回 8009 ROLE_NOT_FOUND

STEP 4: 权限边界校验
  ├─ IF 当前操作者角色为 DOCTOR_BUSINESS_ADMIN:
  │   ├─ 检查 roleIds 中是否包含 SUPER_ADMIN 的角色ID
  │   └─ 包含 → 返回 1002 FORBIDDEN
  └─ ELSE IF 当前操作者角色为 SUPER_ADMIN:
        → 无额外限制

STEP 5: 全量替换角色（事务内）
  ├─ DELETE FROM sys_user_role WHERE user_id = #{userId}
  ├─ IF roleIds 非空:
  │   └─ 批量 INSERT INTO sys_user_role (user_id, role_id) VALUES (...)
  └─ ELSE:
        → 已清空，无需插入

STEP 6: 清除该用户的权限缓存
  └─ 清除 Redis 中该用户的权限缓存 key

STEP 7: 返回结果
  └─ 返回当前用户的角色列表和权限列表
```

### 10.5 数据操作（SQL级）

```sql
-- 事务开始
BEGIN;

-- STEP 5a: 清空旧角色
DELETE FROM sys_user_role WHERE user_id = #{userId};

-- STEP 5b: 批量插入新角色（roleIds 非空时）
INSERT INTO sys_user_role (user_id, role_id, create_time)
VALUES
    (#{userId}, #{roleId1}, NOW()),
    (#{userId}, #{roleId2}, NOW()),
    ...;

COMMIT;
```

### 10.6 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 用户不存在 | 1004 | 404 | 返回"用户不存在" |
| 角色不存在 | 8009 | 404 | 返回"角色不存在" |
| 权限越界 | 1002 | 403 | BIZ_ADMIN 分配 SUPER_ADMIN 角色 |
| 系统异常 | 1007 | 500 | 事务回滚 |

### 10.7 并发控制

事务保证全量替换的原子性。权限缓存清除在事务提交后执行。

### 10.8 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `doctor.assign.role`（BIZ_ADMIN）/ `user.manage`（SUPER_ADMIN） |
| 数据权限 | BIZ_ADMIN 仅可分配 DOCTOR 分组角色 |
| API前缀 | `/api/admin/*` |

---

## 11. F-ADMIN-108 查询用户角色与权限

### 11.1 功能描述

查询指定用户的角色列表及其所有权限编码。

### 11.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 操作者为 SUPER_ADMIN 或 DOCTOR_BUSINESS_ADMIN | Token 解析 + 角色校验 |
| PRE-002 | 目标用户存在 | `sys_user.id = #{userId}` |

### 11.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| userId | Long | 是 | 路径参数 | 目标用户ID |

### 11.4 处理流程

```
STEP 1: 参数校验
  └─ userId 非空且为正整数
  → 失败返回 1003 BAD_REQUEST

STEP 2: 查询用户角色
  ├─ SELECT r.id, r.role_code, r.role_name, r.role_group
  │   FROM sys_role r
  │   INNER JOIN sys_user_role ur ON r.id = ur.role_id
  │   WHERE ur.user_id = #{userId}
  └─ 用户不存在 → 返回 1004 NOT_FOUND（通过先查用户表校验）

STEP 3: 查询用户全部权限（去重）
  ├─ SELECT DISTINCT p.permission_code, p.permission_name, p.module
  │   FROM sys_permission p
  │   INNER JOIN sys_role_permission rp ON p.id = rp.permission_id
  │   INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id
  │   WHERE ur.user_id = #{userId}
  │   ORDER BY p.module, p.permission_code

STEP 4: 返回结果
  └─ 返回 { roles: [...], permissions: [...] }
```

### 11.5 数据操作（SQL级）

```sql
-- 用户存在性校验
SELECT id FROM sys_user WHERE id = #{userId};

-- 查询用户角色
SELECT r.id, r.role_code, r.role_name, r.role_group
FROM sys_role r
INNER JOIN sys_user_role ur ON r.id = ur.role_id
WHERE ur.user_id = #{userId}
ORDER BY r.role_group, r.role_code;

-- 查询用户全部权限
SELECT DISTINCT p.permission_code, p.permission_name, p.module
FROM sys_permission p
INNER JOIN sys_role_permission rp ON p.id = rp.permission_id
INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id
WHERE ur.user_id = #{userId}
ORDER BY p.module, p.permission_code;
```

### 11.6 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 用户不存在 | 1004 | 404 | 返回"用户不存在" |

### 11.7 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `doctor.assign.role`（BIZ_ADMIN）/ `user.manage`（SUPER_ADMIN） |
| API前缀 | `/api/admin/*` |

---

## 12. F-ADMIN-201 查询系统配置

### 12.1 功能描述

查询系统配置项列表。支持按配置键精确查询或查询全部配置。

### 12.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 操作者为 SUPER_ADMIN | Token 解析 + 角色校验 |

### 12.3 输入参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| configKey | String | 否 | — | 配置键，不传则返回全部 |

### 12.4 处理流程

```
STEP 1: 参数校验
  → 无需特殊校验

STEP 2: 查询配置
  ├─ IF configKey 非空:
  │   └─ SELECT * FROM sys_config WHERE config_key = #{configKey}
  └─ ELSE:
        └─ SELECT * FROM sys_config ORDER BY config_key

STEP 3: 返回结果
  └─ 返回配置列表
```

### 12.5 数据操作（SQL级）

```sql
-- 按键查询
SELECT id, config_key, config_value, config_desc, value_type, update_time
FROM sys_config
WHERE config_key = #{configKey};

-- 查询全部
SELECT id, config_key, config_value, config_desc, value_type, update_time
FROM sys_config
ORDER BY config_key;
```

### 12.6 异常处理

本接口为只读查询，异常仅限系统级错误。

### 12.7 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `config.manage` |
| API前缀 | `/api/admin/*` — 仅 SUPER_ADMIN 可访问 |

---

## 13. F-ADMIN-202 更新系统配置

### 13.1 功能描述

更新系统配置项的值。支持批量更新多个配置项。

### 13.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 操作者为 SUPER_ADMIN | Token 解析 + 角色校验 |
| PRE-002 | 配置键存在 | `sys_config.config_key = #{configKey}` |
| PRE-003 | 配置值符合值类型约束 | 根据 value_type 校验 |

### 13.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| configs | List\<ConfigItem\> | 是 | 请求体 | 配置项列表 |

**ConfigItem 结构：**

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| configKey | String | 是 | 配置键 |
| configValue | String | 是 | 配置值 |

### 13.4 处理流程

```
STEP 1: 参数校验
  ├─ configs 非空且不为空数组
  ├─ 每个 ConfigItem 的 configKey 和 configValue 非空
  → 失败返回 1003 BAD_REQUEST

STEP 2: 配置项存在性校验
  ├─ SELECT config_key, value_type FROM sys_config WHERE config_key IN (#{configKeys})
  └─ 不存在的 key → 返回 8013 CONFIG_KEY_NOT_FOUND

STEP 3: 值类型校验
  ├─ FOR EACH config:
  │   ├─ value_type = 'INT' → 校验 configValue 为正整数
  │   ├─ value_type = 'STRING' → 校验 configValue 非空
  │   └─ value_type = 'BOOLEAN' → 校验 configValue 为 true/false
  └─ 校验失败 → 返回 8014 CONFIG_VALUE_INVALID

STEP 4: 批量更新
  └─ UPDATE sys_config SET config_value = #{configValue}, update_time = NOW() WHERE config_key = #{configKey}

STEP 5: 刷新配置缓存
  └─ 更新 Redis 中对应配置的缓存值

STEP 6: 返回结果
  └─ 返回更新后的配置列表
```

### 13.5 数据操作（SQL级）

```sql
-- 批量更新（逐条 UPDATE）
UPDATE sys_config
SET config_value = #{configValue}, update_time = NOW()
WHERE config_key = #{configKey};
```

### 13.6 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 配置键不存在 | 8013 | 404 | 返回"配置项不存在" |
| 配置值无效 | 8014 | 400 | 返回"配置值格式不正确" |
| 参数格式错误 | 1003 | 400 | 返回具体字段错误 |

### 13.7 并发控制

配置更新为低频操作，采用乐观策略：直接 UPDATE，冲突时后写覆盖。配置缓存使用 Redis 缓存 + 版本号，更新时同步刷新。

### 13.8 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `config.manage` |
| API前缀 | `/api/admin/*` — 仅 SUPER_ADMIN 可访问 |

---

## 14. 系统配置项清单

### 14.1 预置配置项

| config_key | 默认值 | value_type | 说明 | 影响模块 |
|------------|--------|------------|------|----------|
| `appointment.max_capacity` | `50` | INT | 单时段最大预约容量 | APPOINTMENT |
| `appointment.advance_days` | `7` | INT | 可提前预约天数 | APPOINTMENT |
| `no_show.freeze_threshold` | `3` | INT | 爽约冻结触发次数 | APPOINTMENT |
| `no_show.freeze_days` | `7` | INT | 爽约冻结天数 | APPOINTMENT |
| `observe.min_duration` | `30` | INT | 最短留观时长（分钟） | FLOW |
| `stock.expiry_warning_days` | `30` | INT | 批次临期预警天数 | STOCK |
| `stock.low_threshold_percent` | `20` | INT | 库存低量预警百分比 | STOCK |
| `schedule.cron_expire` | `0 30 0 * * ?` | STRING | 预约过期扫描 cron 表达式 | APPOINTMENT |
| `batch.expiry_scan_interval` | `60` | INT | 批次过期扫描间隔（分钟） | STOCK |

### 14.2 配置项使用规范

```
配置读取优先级：
  Redis 缓存 → sys_config 表 → 预置默认值

配置变更流程：
  SUPER_ADMIN 更新 sys_config 表
      │
      ▼
  同步刷新 Redis 缓存
      │
      ▼
  下次请求时各模块从 Redis 读取最新值

禁止硬编码：
  所有可配置参数必须从 sys_config 或 Redis 读取
  代码中禁止出现魔法数字
```

### 14.3 配置项与模块关联

| 配置项 | 读取模块 | 读取时机 |
|--------|----------|----------|
| `appointment.max_capacity` | APPOINTMENT | 创建预约时校验时段容量 |
| `appointment.advance_days` | APPOINTMENT | 创建预约时校验日期范围 |
| `no_show.freeze_threshold` | APPOINTMENT | 取消预约时判定冻结 |
| `no_show.freeze_days` | APPOINTMENT | 冻结时计算到期时间 |
| `observe.min_duration` | FLOW-OBSERVE | 结束留观时校验时长 |
| `stock.expiry_warning_days` | STOCK | 批次过期扫描时判定临期 |
| `stock.low_threshold_percent` | STOCK | 库存操作时判定低量预警 |
| `schedule.cron_expire` | SYSTEM | 定时任务配置 |
| `batch.expiry_scan_interval` | SYSTEM | 批次过期扫描间隔 |

---

## 15. 权限控制汇总

### 15.1 功能权限矩阵

| 功能编号 | 权限编码 | 允许角色 | API路径 |
|----------|----------|----------|---------|
| F-ADMIN-101 | `role.manage` | SUPER_ADMIN | `POST /api/admin/role` |
| F-ADMIN-102 | `role.manage` | SUPER_ADMIN | `PUT /api/admin/role/{roleId}` |
| F-ADMIN-103 | `role.manage` | SUPER_ADMIN | `GET /api/admin/role` |
| F-ADMIN-104 | `role.manage` | SUPER_ADMIN | `DELETE /api/admin/role/{roleId}` |
| F-ADMIN-105 | `permission.manage` | SUPER_ADMIN | `PUT /api/admin/role/{roleId}/permissions` |
| F-ADMIN-106 | `role.manage` | SUPER_ADMIN | `GET /api/admin/role/{roleId}/permissions` |
| F-ADMIN-107 | `doctor.assign.role` / `user.manage` | BIZ_ADMIN / SUPER_ADMIN | `PUT /api/admin/user/{userId}/roles` |
| F-ADMIN-108 | `doctor.assign.role` / `user.manage` | BIZ_ADMIN / SUPER_ADMIN | `GET /api/admin/user/{userId}/roles` |
| F-ADMIN-201 | `config.manage` | SUPER_ADMIN | `GET /api/admin/config` |
| F-ADMIN-202 | `config.manage` | SUPER_ADMIN | `PUT /api/admin/config` |

### 15.2 数据权限规则

| 角色 | 数据范围 | 实现方式 |
|------|----------|----------|
| SUPER_ADMIN | 全局所有角色、权限、配置 | 无限制 |
| DOCTOR_BUSINESS_ADMIN | 仅可分配 DOCTOR 分组角色 | Service 层校验 `role_group = 'DOCTOR'` |

### 15.3 权限缓存策略

```
缓存 Key 设计：
  user:permissions:{userId}    → Set<String>  用户全部权限编码（合并多角色去重）
  role:permissions:{roleId}    → Set<String>  角色全部权限编码

缓存失效时机：
  F-ADMIN-105 执行后 → 清除 role:permissions:{roleId}
  F-ADMIN-107 执行后 → 清除 user:permissions:{userId}
  F-ADMIN-202 执行后 → 清除对应 config 缓存

缓存 TTL：30 分钟（兜底自动刷新）
```

---

## 16. 管理模块错误码（8000-8999）

> 本节补充 REQ-GLOBAL 第4.9节中管理模块错误码。与 REQ-GLOBAL 已有错误码合并呈现。本模块使用的系统级错误码（1001/1002/1003/1004/1007）的权威定义参见 REQ-GLOBAL §4.2 系统级错误码。

| 错误码 | 常量名 | HTTP | 描述 |
|--------|--------|------|------|
| 8001 | SCHEDULE_CONFLICT | 409 | 排班时间冲突（REQ-GLOBAL 已定义） |
| 8002 | SCHEDULE_NOT_FOUND | 404 | 排班记录不存在（REQ-GLOBAL 已定义） |
| 8003 | WINDOW_CODE_DUPLICATE | 409 | 窗口编码已存在（REQ-GLOBAL 已定义） |
| 8004 | WINDOW_IN_USE | 409 | 窗口存在关联数据，无法删除（REQ-GLOBAL 已定义） |
| 8005 | NOTICE_NOT_FOUND | 404 | 公告不存在（REQ-GLOBAL 已定义） |
| 8006 | VACCINE_NOT_FOUND | 404 | 疫苗不存在（REQ-GLOBAL 已定义） |
| 8007 | ROLE_ASSIGN_FAILED | 500 | 角色分配失败（REQ-GLOBAL 已定义） |
| 8008 | ROLE_CODE_DUPLICATE | 409 | 角色编码已存在 |
| 8009 | ROLE_NOT_FOUND | 404 | 角色不存在 |
| 8010 | ROLE_SYSTEM_PROTECTED | 409 | 系统内置角色不可删除 |
| 8011 | ROLE_IN_USE | 409 | 角色已绑定用户，无法删除 |
| 8012 | PERMISSION_NOT_FOUND | 404 | 权限ID不存在 |
| 8013 | CONFIG_KEY_NOT_FOUND | 404 | 配置项不存在 |
| 8014 | CONFIG_VALUE_INVALID | 400 | 配置值格式不正确 |
| 8015 | CANNOT_MODIFY_SYSADMIN | 403 | 不能修改系统管理员（REQ-GLOBAL 已定义） |

---

## 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| V1.0 | 2026-04-02 | 初始版本，基于 PRD-ADMIN V1.2 / REQ-GLOBAL V1.0 生成 |
| V1.1 | 2026-04-03 | V3 评审修复：(1) 错误码对齐 GLOBAL §4；(2) 业务规则修正（权限模型/角色分配）；(3) 版本号/依赖版本对齐 |
| V1.2 | 2026-04-03 | REQ 评审修复：(1) §16 新增 8015 CANNOT_MODIFY_SYSADMIN 错误码行；(2) 文档头上游依赖同步至 REQ-GLOBAL V1.3 |

---

**文档结束**
