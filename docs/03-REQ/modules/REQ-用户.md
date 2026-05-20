# 用户模块 — 研发需求文档

**文档编号:** REQ-USER-001
**版本:** V1.2
**状态:** 正式发布
**日期:** 2026-04-03
**上游依赖:** PRD-USER V2.2 / REQ-GLOBAL V1.3

---

## 目录

1. [模块定位与边界](#1-模块定位与边界)
2. [功能清单](#2-功能清单)
3. [F-USER-001 用户注册](#3-f-user-001-用户注册)
4. [F-USER-002 用户登录](#4-f-user-002-用户登录)
5. [F-USER-003 用户登出](#5-f-user-003-用户登出)
6. [F-USER-004 修改密码](#6-f-user-004-修改密码)
7. [F-USER-005 重置密码](#7-f-user-005-重置密码)
8. [F-USER-006 查询用户信息](#8-f-user-006-查询用户信息)
9. [F-USER-007 修改用户信息](#9-f-user-007-修改用户信息)
10. [F-USER-012 冻结用户（管理员）](#10-f-user-012-冻结用户管理员)
11. [F-USER-013 解冻用户（管理员）](#11-f-user-013-解冻用户管理员)
12. [F-USER-019 查询儿童档案列表](#12-f-user-019-查询儿童档案列表)
13. [F-USER-020 查询儿童档案详情](#13-f-user-020-查询儿童档案详情)
14. [F-USER-021 创建儿童档案](#14-f-user-021-创建儿童档案)
15. [F-USER-022 修改儿童档案](#15-f-user-022-修改儿童档案)
16. [F-USER-023 删除儿童档案](#16-f-user-023-删除儿童档案)
17. [F-USER-029 注销账户](#17-f-user-029-注销账户)
18. [爽约记录与计数（跨模块接口）](#18-爽约记录与计数跨模块接口)
19. [冻结状态管理](#19-冻结状态管理)
20. [用户状态流转](#20-用户状态流转)
21. [预约约束支持](#21-预约约束支持)
22. [权限控制汇总](#22-权限控制汇总)
23. [通用系统异常](#23-通用系统异常)

---

## 1. 模块定位与边界

### 1.1 核心定位

USER 是系统的基础模块，负责：
- **用户认证** — 注册、登录、登出、密码管理
- **用户信息管理** — 个人信息查询与修改、管理员冻结/解冻
- **儿童档案** — 家长儿童档案的 CRUD，作为预约的业务实体
- **爽约记录** — 爽约次数的累计与管理，为预约约束提供数据基础
- **冻结状态** — 统一冻结模型（管理员冻结同时设置 status 和 freeze_end_time），约束预约创建

### 1.2 与预约模块的关联

```
USER ────────────────────────── APPOINTMENT
  │                                  │
  │  sys_user.id                     │
  │  ──────> appointment.user_id     │
  │                                  │
  │  child_profile.id                │
  │  ──────> appointment.child_id    │
  │                                  │
  │  sys_user.status                 │
  │  sys_user.freeze_end_time        │
  │  ──────> 预约创建前置校验         │
  │                                  │
  │  sys_user.no_show_count          │
  │  <────── 取消预约时累加           │
  │                                  │
  │  sys_user.freeze_start_time      │
  │  sys_user.freeze_end_time        │
  │  <────── 爽约≥3次时设置           │
```

### 1.3 不包含内容

| 禁止范围 | 归属模块 |
|----------|----------|
| 疫苗预约创建/取消/过期 | APPOINTMENT |
| 接种记录查询 | VACCINATE |
| 公告管理/查询 | ADMIN |
| 疫苗目录查询 | STOCK |
| 排班管理 | ADMIN |
| 窗口管理 | ADMIN |
| 角色权限分配 | ADMIN（DOCTOR_BUSINESS_ADMIN 操作见 PRD-USER） |

---

## 2. 功能清单

| 编号 | 功能名称 | API | 角色 | 优先级 |
|------|----------|-----|------|--------|
| F-USER-001 | 用户注册 | `POST /api/public/register` | 匿名 | P0 |
| F-USER-001 | 用户注册（PRD路径） | `POST /api/user/register` | 匿名 | P0 |
| F-USER-002 | 用户登录 | `POST /api/public/login` | 匿名 | P0 |
| F-USER-002 | 用户登录（PRD路径） | `POST /api/user/login` | 匿名 | P0 |
| F-USER-003 | 用户登出 | `POST /api/user/logout` | USER | P0 |
| F-USER-004 | 修改密码 | `POST /api/user/changePassword` | USER | P0 |
| F-USER-005 | 重置密码 | `POST /api/public/resetPassword` | 匿名 | P1 |
| F-USER-005 | 重置密码（PRD路径） | `POST /api/user/resetPassword` | 匿名 | P1 |
| F-USER-006 | 查询用户信息 | `GET /api/user/profile` | USER | P0 |
| F-USER-007 | 修改用户信息 | `PUT /api/user/profile` | USER | P0 |
| F-USER-012 | 冻结用户 | `POST /api/admin/user/{id}/freeze` | SUPER_ADMIN | P0 |
| F-USER-012 | 冻结用户（PRD路径） | `POST /api/admin/user/{id}` | SUPER_ADMIN | P0 |
| F-USER-013 | 解冻用户 | `POST /api/admin/user/{id}/unfreeze` | SUPER_ADMIN | P0 |
| F-USER-019 | 查询儿童档案列表 | `GET /api/user/children` | USER | P0 |
| F-USER-020 | 查询儿童档案详情 | `GET /api/user/child/{id}` | USER | P0 |
| F-USER-021 | 创建儿童档案 | `POST /api/user/child` | USER | P0 |
| F-USER-022 | 修改儿童档案 | `PUT /api/user/child/{id}` | USER | P0 |
| F-USER-023 | 删除儿童档案 | `DELETE /api/user/child/{id}` | USER | P1 |
| F-USER-008 | 创建用户档案 | `POST /api/user/profile` | USER | P0 |
| F-USER-009 | 查询公告列表 | `GET /api/user/notice/list` | USER | P1 |
| F-USER-010 | 查询公告详情 | `GET /api/user/notice/{noticeId}` | USER | P1 |
| F-USER-011 | 提交公告反馈 | `POST /api/user/notice/{noticeId}/feedback` | USER | P1 |
| F-USER-014 | 查询用户列表 | `GET /api/admin/user/list` | SUPER_ADMIN | P1 |
| F-USER-015 | 查询用户详情 | `GET /api/admin/user/{id}` | SUPER_ADMIN | P1 |
| F-USER-016 | 创建用户 | `POST /api/admin/user` | SUPER_ADMIN | P1 |
| F-USER-017 | 修改用户 | `PUT /api/admin/user/{id}` | SUPER_ADMIN | P1 |
| F-USER-018 | 删除用户 | `DELETE /api/admin/user/{id}` | SUPER_ADMIN | P1 |
| F-USER-024 | 查询疫苗列表 | `GET /api/user/vaccine/list` | USER | P1 |
| F-USER-025 | 查询疫苗详情 | `GET /api/user/vaccine/{vaccineId}` | USER | P1 |
| F-USER-026 | 查询用户角色 | `GET /api/admin/user/{id}/roles` | SUPER_ADMIN | P1 |
| F-USER-027 | 查询用户权限 | `GET /api/admin/user/{id}/permissions` | SUPER_ADMIN | P1 |
| F-USER-028 | 分配角色 | `POST /api/business/user/{id}/assignRole` | DOCTOR_BUSINESS_ADMIN | P1 |
| F-USER-029 | 注销账户 | `POST /api/user/deactivate` | USER | P1 |
| F-USER-030 | 回收角色 | `POST /api/business/user/{id}/revokeRole` | DOCTOR_BUSINESS_ADMIN | P1 |

> **跨模块功能归属说明：**
> - F-USER-009~011（公告查询/详情/反馈）归属 ADMIN 模块，详见 REQ-ADMIN
> - F-USER-014~018（用户管理 CRUD）归属 ADMIN 模块，详见 REQ-ADMIN
> - F-USER-024~025（疫苗查询）归属 STOCK 模块，详见 REQ-STOCK
> - F-USER-026~030（角色权限）归属 ADMIN 模块，详见 REQ-ADMIN

---

## 3. F-USER-001 用户注册

### 3.1 功能描述

新用户通过手机号注册账号，需短信验证码校验。注册成功后自动登录。

### 3.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 手机号未被注册 | `sys_user.phone` 唯一性 |
| PRE-002 | 验证码正确且未过期 | `sms_verification` 表校验 |
| PRE-003 | 手机号格式合法 | 正则 `^1[3-9]\d{9}$` |

### 3.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| phone | String | 是 | 请求体 | 手机号，正则校验 |
| code | String | 是 | 请求体 | 短信验证码，6位数字 |
| password | String | 是 | 请求体 | 密码，8-20位，含大写字母、小写字母和数字 |
| confirmPassword | String | 是 | 请求体 | 确认密码，须与 password 一致 |
| realName | String | 是 | 请求体 | 真实姓名 |

### 3.4 处理流程

```
STEP 1: 参数格式校验
  ├─ phone 正则校验 ^1[3-9]\d{9}$
  ├─ password 正则校验 ^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,20}$
  ├─ password == confirmPassword
  ├─ realName 非空
  └─ 失败返回 1003 BAD_REQUEST

STEP 2: 手机号唯一性校验
  ├─ SELECT COUNT(*) FROM sys_user WHERE phone = #{phone}
  └─ count > 0 → 返回 7001 USER_PHONE_DUPLICATE

STEP 3: 验证码校验
  ├─ SELECT COUNT(*) FROM sms_verification
  │   WHERE phone = #{phone} AND code = #{code}
  │   AND is_used = 0 AND expire_time > NOW()
  └─ count = 0 → 返回 7007 SMS_CODE_INVALID

STEP 4: 创建用户
  ├─ username = phone（用户名自动生成为手机号）
  ├─ password = BCrypt加密
  ├─ status = 0（正常）
  └─ INSERT INTO sys_user (username, phone, password, real_name, status, create_time, update_time)
    VALUES (#{phone}, #{phone}, #{encryptedPassword}, #{realName}, 0, NOW(), NOW())

STEP 5: 标记验证码已使用
  └─ UPDATE sms_verification SET is_used = 1 WHERE phone = #{phone} AND code = #{code}

STEP 6: 自动登录（生成Token）
  ├─ 查询用户角色、权限
  └─ 生成 JWT Token

STEP 7: 返回结果
  └─ 返回 Token、用户信息、角色列表、权限列表
```

### 3.5 数据操作（SQL级）

```sql
-- STEP 2: 手机号唯一性
SELECT COUNT(*) AS cnt FROM sys_user WHERE phone = #{phone};

-- STEP 3: 验证码校验
SELECT COUNT(*) AS cnt
FROM sms_verification
WHERE phone = #{phone} AND code = #{code}
  AND is_used = 0 AND expire_time > NOW();

-- STEP 4: 创建用户
INSERT INTO sys_user (username, phone, password, real_name, status, no_show_count, create_time, update_time)
VALUES (#{phone}, #{phone}, #{encryptedPassword}, #{realName}, 0, 0, NOW(), NOW());

-- STEP 5: 标记验证码已使用
UPDATE sms_verification SET is_used = 1
WHERE phone = #{phone} AND code = #{code};
```

### 3.6 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 手机号已注册 | 7001 | 409 | 返回"该手机号已注册"，提示使用其他手机号或直接登录 |
| 验证码无效 | 7007 | 400 | 返回"验证码不正确或已过期"，提示重新获取验证码 |
| 参数格式错误 | 1003 | 400 | 返回具体字段错误 |
| 系统异常 | 1007 | 500 | 事务回滚，记录日志 |

### 3.7 并发控制

```sql
-- 手机号唯一性依赖数据库 UNIQUE 约束
-- INSERT 时若手机号重复，数据库抛出 DuplicateKeyException
-- 捕获后返回 7001 USER_PHONE_DUPLICATE
```

### 3.8 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 无需认证，公开接口 |
| API前缀 | `/api/public/*` |

---

## 4. F-USER-002 用户登录

### 4.1 功能描述

用户通过手机号和密码登录系统，校验通过后返回 JWT Token。

### 4.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 用户存在 | `sys_user.phone` 存在 |
| PRE-002 | 密码正确 | BCrypt 校验 |
| PRE-003 | 用户状态正常 | `sys_user.status = 0` |
| PRE-004 | 未被登录锁定 | 连续失败 < 5 次 |

### 4.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| phone | String | 是 | 请求体 | 手机号 |
| password | String | 是 | 请求体 | 密码 |

### 4.4 处理流程

```
STEP 1: 参数校验
  ├─ phone 非空
  ├─ password 非空
  └─ 失败返回 1003 BAD_REQUEST

STEP 2: 查询用户
  ├─ SELECT id, username, phone, password, real_name, status
  │   FROM sys_user WHERE phone = #{phone}
  └─ 用户不存在 → 返回 7003 USER_NOT_FOUND

STEP 3: 登录锁定校验
  ├─ 检查 Redis 登录失败计数：login_fail:{phone}
  ├─ count >= 5 AND 锁定未过期 → 返回 7008 LOGIN_LOCKED
  └─ 锁定过期则清除计数

STEP 4: 密码校验
  ├─ BCrypt.checkpw(inputPassword, storedPassword)
  └─ 密码错误 → Redis 计数+1，返回 7002 USER_LOGIN_FAILED
     （若计数达到5，设置锁定30分钟）

STEP 5: 用户状态校验
  ├─ status != 0 → 返回 7009 USER_FROZEN
  └─ 返回冻结原因（如有）

STEP 6: 登录成功
  ├─ 清除 Redis 登录失败计数
  ├─ 查询用户角色
  │   SELECT r.role_code, r.role_name
  │   FROM sys_user_role ur JOIN sys_role r ON ur.role_id = r.id
  │   WHERE ur.user_id = #{userId}
  ├─ 查询用户权限（角色权限 + 额外权限）
  │   SELECT p.permission_code, p.permission_name
  │   FROM sys_user_role ur
  │   JOIN sys_role_permission rp ON ur.role_id = rp.role_id
  │   JOIN sys_permission p ON rp.permission_id = p.id
  │   WHERE ur.user_id = #{userId}
  │   UNION
  │   SELECT p.permission_code, p.permission_name
  │   FROM sys_user_permission up
  │   JOIN sys_permission p ON up.permission_id = p.id
  │   WHERE up.user_id = #{userId}
  └─ 生成 JWT Token

STEP 7: 返回结果
  └─ 返回 Token、用户信息、角色列表、权限列表
```

### 4.5 数据操作（SQL级）

```sql
-- STEP 2: 查询用户
SELECT id, username, phone, password, real_name, status
FROM sys_user
WHERE phone = #{phone};

-- STEP 6: 查询用户角色
SELECT r.role_code, r.role_name
FROM sys_user_role ur
JOIN sys_role r ON ur.role_id = r.id
WHERE ur.user_id = #{userId};

-- STEP 6: 查询用户权限（角色权限 + 额外权限）
SELECT p.permission_code, p.permission_name
FROM sys_user_role ur
JOIN sys_role_permission rp ON ur.role_id = rp.role_id
JOIN sys_permission p ON rp.permission_id = p.id
WHERE ur.user_id = #{userId}

UNION

SELECT p.permission_code, p.permission_name
FROM sys_user_permission up
JOIN sys_permission p ON up.permission_id = p.id
WHERE up.user_id = #{userId};
```

### 4.6 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 用户不存在 | 7003 | 404 | 返回"用户不存在，请先注册"，引导注册 |
| 密码错误 | 7002 | 401 | 返回"用户名或密码错误"，引导重新输入 |
| 用户已冻结 | 7009 | 403 | 返回"该用户已被冻结，无法登录"，提示联系管理员 |
| 登录锁定 | 7008 | 403 | 返回"登录失败次数过多，账号已锁定30分钟"，提示等待30分钟自动解锁 |
| 系统异常 | 1007 | 500 | 记录日志 |

### 4.7 并发控制

登录锁定使用 Redis 计数器，原子操作：

> 规则来源：PRD-USER §5.2 登录安全策略（连续失败 5 次锁定，锁定时长 30 分钟）

```
-- 登录失败时
INCR login_fail:{phone}
IF count == 1 THEN EXPIRE login_fail:{phone} 1800  -- 30分钟

-- 登录成功时
DEL login_fail:{phone}
```

### 4.8 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 无需认证，公开接口 |
| API前缀 | `/api/public/*` |

---

## 5. F-USER-003 用户登出

### 5.1 功能描述

用户退出登录，清除服务端会话/Token 黑名单。

### 5.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|---------|---------|---------|
| PRE-001 | 用户已登录且 Token 有效 | Token 校验 |

### 5.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| userId | Long | 是 | Token 解析 | 当前登录用户ID |

### 5.4 处理流程

```
STEP 1: 将当前 Token 加入黑名单（Redis，TTL = Token 剩余有效期）
STEP 2: 返回登出成功
```

### 5.5 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|---------|--------|------|---------|
| Token 无效或已过期 | 1001 | 401 | 清除 Token，跳转登录页 |
| 用户不存在 | 1004 | 404 | 返回"用户不存在"提示 |

### 5.6 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| API前缀 | `/api/user/*` — 仅 USER 角色可访问 |

---

## 6. F-USER-004 修改密码

### 6.1 功能描述

已登录用户修改密码，需验证旧密码。

### 6.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|---------|---------|---------|
| PRE-001 | 用户已登录且 Token 有效 | Token 校验 |
| PRE-002 | 用户账户状态正常 | 查询 sys_user.status |

### 6.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| userId | Long | 是 | Token 解析 | 当前登录用户ID |
| oldPassword | String | 是 | 请求体 | 旧密码 |
| newPassword | String | 是 | 请求体 | 新密码，8-20位，含大写字母、小写字母和数字 |
| confirmPassword | String | 是 | 请求体 | 确认新密码 |

### 6.4 处理流程

```
STEP 1: 参数校验
  ├─ newPassword 正则校验 ^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,20}$
  ├─ newPassword == confirmPassword
  └─ 失败返回 1003 BAD_REQUEST

STEP 2: 查询用户当前密码
  └─ SELECT password FROM sys_user WHERE id = #{userId}

STEP 3: 旧密码校验
  └─ BCrypt.checkpw(oldPassword, storedPassword) == false → 返回 7010 OLD_PASSWORD_ERROR

STEP 4: 更新密码
  └─ UPDATE sys_user SET password = #{newEncryptedPassword}, update_time = NOW() WHERE id = #{userId}

STEP 5: 返回成功（可选：清除该用户其他设备Token）
```

### 6.5 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 旧密码错误 | 7010 | 400 | 返回"旧密码不正确" |
| 新密码格式错误 | 1003 | 400 | 返回"密码长度8-20位，必须包含大写字母、小写字母和数字" |
| 两次密码不一致 | 1003 | 400 | 返回"两次输入的密码不一致" |

### 6.6 数据操作（SQL级）

```sql
-- STEP 2: 查询当前密码
SELECT password FROM sys_user WHERE id = #{userId};

-- STEP 4: 更新密码
UPDATE sys_user SET password = #{newEncryptedPassword}, update_time = NOW() WHERE id = #{userId};
```

### 6.7 并发控制

修改密码涉及单行 UPDATE，无特殊并发要求，数据库默认隔离级别即可保证安全。

### 6.8 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| API前缀 | `/api/user/*` |

---

## 7. F-USER-005 重置密码

### 7.1 功能描述

用户通过短信验证码重置密码，用于忘记密码场景。

### 7.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|---------|---------|---------|
| PRE-001 | 用户手机号已注册 | 查询 sys_user.phone |
| PRE-002 | 验证码正确且未过期 | sms_verification 表校验 |

### 7.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| phone | String | 是 | 请求体 | 手机号 |
| code | String | 是 | 请求体 | 短信验证码 |
| newPassword | String | 是 | 请求体 | 新密码 |
| confirmPassword | String | 是 | 请求体 | 确认新密码 |

### 7.4 处理流程

```
STEP 1: 参数校验
  ├─ phone 正则校验
  ├─ newPassword 正则校验 ^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,20}$
  ├─ newPassword == confirmPassword
  └─ 失败返回 1003 BAD_REQUEST

STEP 2: 验证码校验
  └─ sms_verification 校验 → 失败返回 7007 SMS_CODE_INVALID

STEP 3: 查询用户
  └─ SELECT id FROM sys_user WHERE phone = #{phone}
     不存在 → 返回 7003 USER_NOT_FOUND

STEP 4: 更新密码
  └─ UPDATE sys_user SET password = #{newEncryptedPassword}, update_time = NOW()
      WHERE id = #{userId}

STEP 5: 标记验证码已使用 + 清除登录锁定计数
```

### 7.5 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 无需认证，公开接口 |
| API前缀 | `/api/public/*` |

### 7.6 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 验证码无效 | 7007 | 400 | 返回"验证码不正确或已过期"，提示重新获取验证码 |
| 用户不存在 | 7003 | 404 | 返回"用户不存在"，引导注册 |
| 新密码格式错误 | 1003 | 400 | 返回"密码长度8-20位，必须包含大写字母、小写字母和数字" |
| 两次密码不一致 | 1003 | 400 | 返回"两次输入的密码不一致" |

### 7.7 数据操作（SQL级）

```sql
-- STEP 2: 验证码校验
SELECT COUNT(*) AS cnt
FROM sms_verification
WHERE phone = #{phone} AND code = #{code}
  AND is_used = 0 AND expire_time > NOW();

-- STEP 3: 查询用户
SELECT id FROM sys_user WHERE phone = #{phone};

-- STEP 4: 更新密码
UPDATE sys_user SET password = #{newEncryptedPassword}, update_time = NOW()
WHERE id = #{userId};

-- STEP 5: 标记验证码已使用
UPDATE sms_verification SET is_used = 1
WHERE phone = #{phone} AND code = #{code};
```

### 7.8 并发控制

重置密码涉及单行 UPDATE，无特殊并发要求，数据库默认隔离级别即可保证安全。

---

## 8. F-USER-006 查询用户信息

### 8.1 功能描述

查询当前登录用户的详细信息，包含爽约次数和冻结状态。

### 8.2 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| userId | Long | 是 | Token 解析 | 当前登录用户ID |

### 8.3 处理流程

```
STEP 1: 查询用户信息
  └─ SELECT id, username, phone, real_name, gender, id_card_type, id_card_no,
          status, no_show_count, freeze_start_time, freeze_end_time,
          create_time, update_time
      FROM sys_user WHERE id = #{userId}
  └─ 用户不存在 → 返回 7003 USER_NOT_FOUND

STEP 2: 计算冻结状态
  ├─ IF freeze_end_time IS NOT NULL AND freeze_end_time > NOW()
  │     freezeStatus = FROZEN（冻结中）
  └─ ELSE
        freezeStatus = NORMAL（正常）
```

### 8.4 数据操作（SQL级）

```sql
SELECT
    id, username, phone, real_name, gender,
    id_card_type, id_card_no,
    status, no_show_count,
    freeze_start_time, freeze_end_time,
    create_time, update_time
FROM sys_user
WHERE id = #{userId};
```

### 8.5 返回字段

| 字段名 | 类型 | 说明 |
|--------|------|------|
| userId | Long | 用户ID |
| username | String | 用户名 |
| phone | String | 手机号（脱敏显示：138****8000） |
| realName | String | 真实姓名 |
| gender | Integer | 性别：0=未知,1=男,2=女 |
| idCardType | String | 证件类型 |
| idCardNo | String | 证件号码（脱敏显示） |
| status | Integer | 账号状态：0=正常,1=已冻结,2=已注销 |
| noShowCount | Integer | 爽约累计次数 |
| freezeStatus | String | 冻结状态：NORMAL / FROZEN |
| freezeEndTime | String | 冻结到期时间（冻结中时返回） |

### 8.6 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 数据权限 | 仅查询自己的信息 `WHERE id = #{userId}` |
| API前缀 | `/api/user/*` |

### 8.7 并发控制

> 本接口为只读查询，不涉及状态变更。返回结果中包含用户当前状态值（status）和冻结状态（freezeStatus），状态含义参见 REQ-GLOBAL §2.1 及本文 §20 用户状态流转。查询结果反映查询时刻的一致性快照。

---

## 9. F-USER-007 修改用户信息

### 9.1 功能描述

修改当前用户的基本信息（真实姓名、性别、证件信息）。

### 9.2 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| userId | Long | 是 | Token 解析 | 当前登录用户ID |
| realName | String | 否 | 请求体 | 真实姓名 |
| gender | Integer | 否 | 请求体 | 性别：0/1/2 |
| idCardType | String | 否 | 请求体 | 证件类型 |
| idCardNo | String | 否 | 请求体 | 证件号码 |

### 9.3 处理流程

```
STEP 1: 参数校验
  ├─ 至少传一个可选字段
  └─ 失败返回 1003 BAD_REQUEST

STEP 2: 更新用户信息
  └─ UPDATE sys_user
      SET real_name = #{realName}, gender = #{gender},
          id_card_type = #{idCardType}, id_card_no = #{idCardNo},
          update_time = NOW()
      WHERE id = #{userId}

STEP 3: 同步更新儿童档案的家长身份证号
  └─ UPDATE child_profile SET parent_id_card = #{idCardNo}, update_time = NOW()
      WHERE parent_id = #{userId}
  （仅当 idCardNo 变更时执行）
```

### 9.4 数据操作（SQL级）

```sql
-- STEP 2: 更新用户信息
UPDATE sys_user
SET real_name = #{realName},
    gender = #{gender},
    id_card_type = #{idCardType},
    id_card_no = #{idCardNo},
    update_time = NOW()
WHERE id = #{userId};

-- STEP 3: 同步家长身份证号到儿童档案（仅 idCardNo 变更时）
UPDATE child_profile
SET parent_id_card = #{idCardNo}, update_time = NOW()
WHERE parent_id = #{userId};
```

> **预约关联：** `child_profile.parent_id_card` 为冗余字段，在签到身份核验时使用（FLOW-SIGNIN 模块）。用户修改身份证号后需同步更新。

### 9.5 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 数据权限 | 仅修改自己的信息 |
| API前缀 | `/api/user/*` |

---

## 10. F-USER-012 冻结用户（管理员）

### 10.1 功能描述

系统管理员冻结用户账号，冻结时同时设置 `status=1` 和 `freeze_end_time=NOW()+7天`。被冻结用户无法登录系统，也无法创建新预约。冻结后所有未完成的预约不受影响（仍可签到、接种）。

> 冻结模型参见 REQ-GLOBAL §9.5 数据权限隔离

### 10.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 用户存在 | `sys_user.id` 存在 |
| PRE-002 | 用户未被冻结 | `sys_user.status != 1` |
| PRE-003 | 用户未被注销 | `sys_user.status != 2` |

### 10.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| userId | Long | 是 | 路径参数 | 目标用户ID |
| operatorId | Long | 是 | Token 解析 | 操作人ID |
| freezeReason | String | 是 | 请求体 | 冻结原因 |

### 10.4 处理流程

```
STEP 1: 参数校验
  └─ freezeReason 非空
     失败返回 1003 BAD_REQUEST

STEP 2: 查询目标用户
  └─ SELECT id, status FROM sys_user WHERE id = #{userId}
     不存在 → 返回 7003 USER_NOT_FOUND

STEP 3: 状态校验
  ├─ status = 1 → 返回 7004 USER_ALREADY_FROZEN
  └─ status = 2 → 返回 1003 BAD_REQUEST（已注销用户不可冻结）

STEP 4: 执行冻结
  └─ UPDATE sys_user SET status = 1, freeze_end_time = DATE_ADD(NOW(), INTERVAL 7 DAY), update_time = NOW() WHERE id = #{userId}

STEP 5: 记录操作日志
  └─ INSERT INTO sys_permission_audit (user_id, permission_code, api_path, request_method, result, ip_address, create_time)
      VALUES (#{operatorId}, 'user.manage', '/api/admin/user/{userId}/freeze', 'POST', 0, #{ip}, NOW())

STEP 6: 返回冻结成功
```

### 10.5 数据操作（SQL级）

```sql
-- STEP 2: 查询目标用户
SELECT id, status FROM sys_user WHERE id = #{userId};

-- STEP 4: 执行冻结（统一模型：同时设置 status 和 freeze_end_time）
UPDATE sys_user SET status = 1, freeze_end_time = DATE_ADD(NOW(), INTERVAL 7 DAY), update_time = NOW() WHERE id = #{userId};
```

> **预约关联：** 管理员冻结同时设置 `status = 1` 和 `freeze_end_time = NOW()+7天`（统一冻结模型），预约创建时校验 `sys_user.status = 0`，被冻结用户无法创建新预约。已有预约（status=1/6/7/8/10）不受影响，可继续完成流程。

### 10.6 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 用户不存在 | 7003 | 404 | 返回"用户不存在" |
| 用户已冻结 | 7004 | 409 | 返回"用户已处于冻结状态" |
| 冻结原因为空 | 1003 | 400 | 返回"请填写冻结原因" |

### 10.7 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `user.manage` |
| API前缀 | `/api/admin/*` — 仅 SUPER_ADMIN 可访问 |

---

## 11. F-USER-013 解冻用户（管理员）

### 11.1 功能描述

系统管理员解冻用户账号，解冻时同时清除 `status` 和 `freeze_end_time`，恢复用户正常登录和预约权限。

### 11.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 用户存在 | `sys_user.id` 存在 |
| PRE-002 | 用户处于冻结状态 | `sys_user.status = 1` |

### 11.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| userId | Long | 是 | 路径参数 | 目标用户ID |
| operatorId | Long | 是 | Token 解析 | 操作人ID |

### 11.4 处理流程

```
STEP 1: 查询目标用户
  └─ SELECT id, status FROM sys_user WHERE id = #{userId}
     不存在 → 返回 7003 USER_NOT_FOUND

STEP 2: 状态校验
  └─ status != 1 → 返回 1003 BAD_REQUEST（仅冻结状态可解冻）

STEP 3: 执行解冻
  └─ UPDATE sys_user SET status = 0, freeze_end_time = NULL, update_time = NOW() WHERE id = #{userId}

STEP 4: 记录操作日志

STEP 5: 返回解冻成功
```

### 11.5 数据操作（SQL级）

```sql
-- STEP 3: 执行解冻（统一模型：同时清除 status 和 freeze_end_time）
UPDATE sys_user SET status = 0, freeze_end_time = NULL, update_time = NOW() WHERE id = #{userId};
```

### 11.6 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `user.manage` |
| API前缀 | `/api/admin/*` — 仅 SUPER_ADMIN 可访问 |

---

## 12. F-USER-019 查询儿童档案列表

### 12.1 功能描述

查询当前用户的儿童档案列表，供预约时选择儿童使用。

### 12.2 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| userId | Long | 是 | Token 解析 | 当前登录用户ID |

### 12.3 处理流程

```
STEP 1: 查询儿童档案列表
  └─ SELECT id, parent_id, name, gender, birth_date, id_card_type, id_card_no,
          native_place, nation, create_time, update_time
      FROM child_profile
      WHERE parent_id = #{userId}
      ORDER BY create_time DESC

STEP 2: 组装返回结果（列表）
```

### 12.4 数据操作（SQL级）

```sql
SELECT
    id, parent_id, name, gender, birth_date,
    id_card_type, id_card_no,
    native_place, nation,
    create_time, update_time
FROM child_profile
WHERE parent_id = #{userId}
ORDER BY create_time DESC;
```

### 12.5 返回字段

| 字段名 | 类型 | 说明 |
|--------|------|------|
| childId | Long | 档案ID（预约时作为 childId 传入） |
| name | String | 儿童姓名 |
| gender | Integer | 性别：1=男,2=女 |
| birthDate | String | 出生日期，格式 YYYY-MM-DD |
| idCardType | String | 证件类型 |
| idCardNo | String | 证件号码 |
| createTime | String | 创建时间 |

### 12.6 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `child.view.own` |
| 数据权限 | SQL WHERE `parent_id = #{userId}` |
| API前缀 | `/api/user/*` |

### 12.7 排序与分页

- 默认按创建时间倒序排列（最新优先）
- 本接口返回当前用户的全部儿童档案（上限 5 条），不做分页处理
- 分页参数：无（数据量受儿童档案数量限制约束，见 §14.2 PRE-002）

---

## 13. F-USER-020 查询儿童档案详情

### 13.1 功能描述

查询单个儿童档案的完整详细信息，供预约前核对。

### 13.2 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| childId | Long | 是 | 路径参数 | 档案ID |
| userId | Long | 是 | Token 解析 | 当前登录用户ID |

### 13.3 处理流程

```
STEP 1: 参数校验
  └─ childId 为正整数
     失败返回 1003 BAD_REQUEST

STEP 2: 查询儿童档案（含归属校验）
  ├─ SELECT * FROM child_profile WHERE id = #{childId} AND parent_id = #{userId}
  └─ 不存在 → 返回 7006 CHILD_NOT_FOUND

STEP 3: 组装返回结果
```

### 13.4 数据操作（SQL级）

```sql
SELECT
    id, parent_id, parent_id_card, name, gender, birth_date,
    id_card_type, id_card_no,
    native_place, nation,
    medical_history, allergy_history,
    create_time, update_time
FROM child_profile
WHERE id = #{childId}
  AND parent_id = #{userId};
```

> **预约关联：** 本接口返回的 `childId` 用于预约创建时传入 `childId` 参数。预约模块（F-APPOINTMENT-001）会校验 `child_profile.parent_id = #{userId}` 确保归属。

### 13.5 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 档案不存在 | 7006 | 404 | 返回"儿童档案不存在"，提示刷新页面（含无权访问情况） |

### 13.6 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `child.view.own` |
| 数据权限 | SQL WHERE `parent_id = #{userId}` |
| API前缀 | `/api/user/*` |

---

## 14. F-USER-021 创建儿童档案

### 14.1 功能描述

创建新的儿童档案，用于后续预约接种。

### 14.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 用户已登录 | Token 有效 |
| PRE-002 | 儿童档案数量未超限 | `COUNT(parent_id) < 5` |

> 规则来源：REQ-GLOBAL §4.8 用户模块错误码（7011 CHILD_COUNT_EXCEEDED 描述："儿童档案数量已达上限（5个）"）

### 14.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| userId | Long | 是 | Token 解析 | 当前登录用户ID |
| name | String | 是 | 请求体 | 儿童姓名 |
| gender | Integer | 是 | 请求体 | 性别：1=男,2=女 |
| birthDate | String | 是 | 请求体 | 出生日期，格式 YYYY-MM-DD |
| idCardType | String | 否 | 请求体 | 证件类型 |
| idCardNo | String | 否 | 请求体 | 证件号码 |
| nativePlace | String | 否 | 请求体 | 籍贯 |
| nation | String | 否 | 请求体 | 民族 |
| medicalHistory | String | 否 | 请求体 | 既往病史 |
| allergyHistory | String | 否 | 请求体 | 过敏史 |

### 14.4 处理流程

```
STEP 1: 参数校验
  ├─ name 非空
  ├─ gender IN (1, 2)
  ├─ birthDate 格式校验（YYYY-MM-DD）且不超过当前日期
  ├─ idCardNo 格式校验（如填写）
  └─ 失败返回 1003 BAD_REQUEST

STEP 2: 儿童档案数量校验
  ├─ SELECT COUNT(*) FROM child_profile WHERE parent_id = #{userId}
  └─ count >= 5 → 返回 7011 CHILD_COUNT_EXCEEDED

STEP 3: 查询家长身份证号
  └─ SELECT id_card_no FROM sys_user WHERE id = #{userId}

STEP 4: 插入儿童档案
  └─ INSERT INTO child_profile
      (parent_id, parent_id_card, name, gender, birth_date,
       id_card_type, id_card_no, native_place, nation,
       medical_history, allergy_history, create_time, update_time)
      VALUES (#{userId}, #{parentIdCard}, #{name}, #{gender}, #{birthDate},
              #{idCardType}, #{idCardNo}, #{nativePlace}, #{nation},
              #{medicalHistory}, #{allergyHistory}, NOW(), NOW())

STEP 5: 返回档案ID和档案信息
```

### 14.5 数据操作（SQL级）

```sql
-- STEP 2: 数量校验
SELECT COUNT(*) AS cnt FROM child_profile WHERE parent_id = #{userId};

-- STEP 3: 查询家长身份证号
SELECT id_card_no FROM sys_user WHERE id = #{userId};

-- STEP 4: 插入儿童档案
INSERT INTO child_profile (
    parent_id, parent_id_card, name, gender, birth_date,
    id_card_type, id_card_no, native_place, nation,
    medical_history, allergy_history, create_time, update_time
) VALUES (
    #{userId}, #{parentIdCard}, #{name}, #{gender}, #{birthDate},
    #{idCardType}, #{idCardNo}, #{nativePlace}, #{nation},
    #{medicalHistory}, #{allergyHistory}, NOW(), NOW()
);
```

> **预约关联：** `parent_id_card` 从 `sys_user.id_card_no` 复制，为冗余字段，供 FLOW-SIGNIN 签到时身份证核验使用。创建后的 `childId` 可用于预约创建（F-APPOINTMENT-001）。

### 14.6 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 儿童档案超限 | 7011 | 400 | 返回"每个用户最多添加5个儿童档案"，提示删除部分档案后再添加 |
| 必填字段为空 | 7005 | 400 | 返回"儿童档案信息不完整" |
| 出生日期格式错误 | 1003 | 400 | 返回"出生日期格式不正确" |
| 证件号码格式错误 | 1003 | 400 | 返回"证件号码格式不正确" |

### 14.7 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `child.add.own` |
| 数据权限 | 自动限定 `parent_id = #{userId}` |
| API前缀 | `/api/user/*` |

---

## 15. F-USER-022 修改儿童档案

### 15.1 功能描述

修改儿童档案信息。

### 15.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 儿童档案存在且归属当前用户 | `child_profile.id = ? AND parent_id = ?` |

### 15.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| childId | Long | 是 | 路径参数 | 档案ID |
| userId | Long | 是 | Token 解析 | 当前登录用户ID |
| name | String | 否 | 请求体 | 儿童姓名 |
| gender | Integer | 否 | 请求体 | 性别 |
| birthDate | String | 否 | 请求体 | 出生日期 |
| idCardType | String | 否 | 请求体 | 证件类型 |
| idCardNo | String | 否 | 请求体 | 证件号码 |
| nativePlace | String | 否 | 请求体 | 籍贯 |
| nation | String | 否 | 请求体 | 民族 |
| medicalHistory | String | 否 | 请求体 | 既往病史 |
| allergyHistory | String | 否 | 请求体 | 过敏史 |

### 15.4 处理流程

```
STEP 1: 参数校验
  ├─ 至少传一个可选字段
  ├─ gender 如传值须 IN (1, 2)
  └─ 失败返回 1003 BAD_REQUEST

STEP 2: 归属校验
  ├─ SELECT id FROM child_profile WHERE id = #{childId} AND parent_id = #{userId}
  └─ 不存在 → 返回 7006 CHILD_NOT_FOUND

STEP 3: 更新儿童档案
  └─ UPDATE child_profile
      SET name=#{name}, gender=#{gender}, birth_date=#{birthDate},
          id_card_type=#{idCardType}, id_card_no=#{idCardNo},
          native_place=#{nativePlace}, nation=#{nation},
          medical_history=#{medicalHistory}, allergy_history=#{allergyHistory},
          update_time = NOW()
      WHERE id = #{childId}

STEP 4: 返回更新后的档案信息
```

### 15.5 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 档案不存在 | 7006 | 404 | 返回"儿童档案不存在"，提示刷新页面 |

### 15.6 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `child.edit.own` |
| 数据权限 | SQL WHERE `parent_id = #{userId}` |
| API前缀 | `/api/user/*` |

---

## 16. F-USER-023 删除儿童档案

### 16.1 功能描述

删除儿童档案。有关联的未完成预约时不允许删除。

### 16.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|----------|----------|----------|
| PRE-001 | 儿童档案存在且归属当前用户 | `child_profile.id = ? AND parent_id = ?` |
| PRE-002 | 无关联的未完成预约 | `appointment` 表校验 |

### 16.3 输入参数

| 参数名 | 类型 | 必填 | 来源 | 说明 |
|--------|------|------|------|------|
| childId | Long | 是 | 路径参数 | 档案ID |
| userId | Long | 是 | Token 解析 | 当前登录用户ID |

### 16.4 处理流程

```
STEP 1: 归属校验
  └─ SELECT id FROM child_profile WHERE id = #{childId} AND parent_id = #{userId}
     不存在 → 返回 7006 CHILD_NOT_FOUND

STEP 2: 关联预约校验（跨模块查询）
  ├─ SELECT COUNT(*) FROM appointment
  │   WHERE child_id = #{childId}
  │   AND status IN (1, 6, 7, 8, 10)
  └─ count > 0 → 返回 7012 CHILD_HAS_APPOINTMENT

STEP 3: 执行删除
  └─ DELETE FROM child_profile WHERE id = #{childId} AND parent_id = #{userId}

STEP 4: 返回删除成功
```

### 16.5 数据操作（SQL级）

```sql
-- STEP 1: 归属校验
SELECT id FROM child_profile WHERE id = #{childId} AND parent_id = #{userId};

-- STEP 2: 关联预约校验
SELECT COUNT(*) AS cnt
FROM appointment
WHERE child_id = #{childId}
  AND status IN (1, 6, 7, 8, 10);

-- STEP 3: 执行删除
DELETE FROM child_profile WHERE id = #{childId} AND parent_id = #{userId};
```

> **预约关联：** 删除前必须校验该儿童是否存在未完成的预约（进行中状态），防止数据不一致。已完成/已取消/已过期的历史预约保留，不阻止删除。

### 16.6 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 档案不存在 | 7006 | 404 | 返回"儿童档案不存在"，提示刷新页面 |
| 存在未完成预约 | 7012 | 409 | 返回"该儿童存在未完成的预约，无法删除" |

### 16.7 权限控制

| 控制项 | 规则 |
|--------|------|
| 认证 | 必须携带有效 Token |
| 功能权限 | `child.delete.own` |
| 数据权限 | SQL WHERE `parent_id = #{userId}` |
| API前缀 | `/api/user/*` |

---

## 17. F-USER-029 注销账户

### 17.1 功能描述

用户主动申请注销账户。注销前系统校验用户无进行中的预约和接种流程，注销后该用户账户不可恢复，关联的儿童档案标记为已归档。

### 17.2 前置条件

| 条件编号 | 条件描述 | 校验方式 |
|---------|---------|---------|
| PRE-001 | 用户已登录且 Token 有效 | Token 校验 |
| PRE-002 | 用户账户状态正常（status != 1 且未注销） | 查询 sys_user.status |
| PRE-003 | 无进行中的预约 | 查询 appointment WHERE user_id = #{userId} AND status IN (1,6,7,8,10) |
| PRE-004 | 无未完成的接种流程 | 查询 appointment WHERE user_id = #{userId} AND status IN (7,8,10) |

### 17.3 输入参数

本接口无外部输入参数，使用当前登录用户身份。

### 17.4 处理流程

```
STEP 1: 参数校验
  └─ 校验当前用户身份有效（Token 未过期）
STEP 2: 校验账户状态
  └─ SELECT status FROM sys_user WHERE id = #{userId}
  └─ IF status = 1 THEN 返回 7004 USER_ALREADY_FROZEN（用户已冻结，无法注销）
  └─ IF status = 2 THEN 返回 1004 NOT_FOUND（用户已注销）
STEP 3: 校验无进行中预约
  └─ SELECT COUNT(*) FROM appointment WHERE user_id = #{userId} AND status IN (1,6,7,8,10)
  └─ IF count > 0 THEN 返回 7014 USER_HAS_ACTIVE_APPOINTMENT（存在进行中的预约，无法注销）
STEP 4: 执行注销
  └─ BEGIN;
      UPDATE sys_user SET status = 2, update_time = NOW() WHERE id = #{userId};
      UPDATE child_profile SET status = 1, update_time = NOW() WHERE parent_id = #{userId} AND status = 0;
      COMMIT;
STEP 5: 返回结果
  └─ 返回注销成功提示
```

### 17.5 数据操作（SQL级）

```sql
BEGIN;
-- 校验账户状态
SELECT id, status FROM sys_user WHERE id = #{userId} FOR UPDATE;
-- assert status != 1 AND status != 2

-- 校验无进行中预约
SELECT COUNT(*) AS active_count FROM appointment
WHERE user_id = #{userId} AND status IN (1, 6, 7, 8, 10);
-- assert active_count = 0

-- 执行注销
UPDATE sys_user SET status = 2, update_time = NOW() WHERE id = #{userId};

-- 归档关联儿童档案
UPDATE child_profile SET status = 1, update_time = NOW()
WHERE parent_id = #{userId} AND status = 0;

COMMIT;
```

### 17.6 状态流转

注销操作将 sys_user.status 从 0 变更为 2（已注销）。关联的 child_profile.status 从 0 变更为 1（已归档）。

> 数据保留策略参见 REQ-GLOBAL §11 数据生命周期管理。

### 17.7 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|---------|--------|------|---------|
| Token 无效或已过期 | 1001 | 401 | 清除 Token，跳转登录页 |
| 用户不存在 | 1004 | 404 | 返回"用户不存在"提示 |
| 用户已冻结 | 7004 | 409 | 返回"用户已冻结，无法注销"提示 |
| 用户已注销 | 1004 | 404 | 返回"用户已注销"提示 |
| 存在进行中的预约 | 7014 | 409 | 返回"存在进行中的预约，请先取消"提示 |

### 17.8 并发控制

注销操作涉及 sys_user 和 child_profile 两张表的 UPDATE，使用数据库事务保证原子性。SELECT FOR UPDATE 确保校验和更新之间数据不被修改。

### 17.9 权限控制

| 角色 | 权限编码 | 数据权限 |
|------|---------|---------|
| USER（本人） | — | 仅允许操作本人账户 |

---

## 18. 爽约记录与计数（跨模块接口）

> 爽约计数由 APPOINTMENT 模块触发更新，参见 REQ-预约 §10

> **注意：** 本节中的爽约自动冻结机制（17.5）已被统一冻结模型吸收。管理员冻结（F-USER-012）已改为同时设置 `status=1` 和 `freeze_end_time`，爽约冻结仍保留独立的 `freeze_end_time` 设置逻辑。两者统一使用 `freeze_end_time` 字段来约束预约创建。

### 18.1 概述

爽约记录是 USER 模块与 APPOINTMENT 模块的跨模块接口。爽约计数数据存储在 `sys_user.no_show_count`，由 APPOINTMENT 模块的取消预约操作触发更新。

```
APPOINTMENT 模块                    USER 模块
（F-APPOINTMENT-004 取消预约）
      │
      ├─ cancel_time >= appointment_date ?
      │
      └─ 是 → 调用爽约计数接口
               │
               ▼
          no_show_count += 1
               │
               ▼
          no_show_count >= 3 ?
               │
               └─ 是 → 触发爽约冻结
```

### 18.2 爽约定义

| 场景 | 是否爽约 | 判定条件 |
|------|----------|----------|
| 预约日期之前取消 | 否 | `cancel_time < appointment_date` |
| 预约当天或之后取消 | **是** | `cancel_time >= appointment_date` |
| 系统过期（定时任务） | 否 | 过期不计入爽约次数 |

### 18.3 爽约计数规则

| 规则 | 说明 |
|------|------|
| 计数方式 | 累加，永不清零 |
| 重置条件 | 无。`no_show_count` 一旦增加不会减少 |
| 冻结解除后 | `no_show_count` 保持不变，继续累计 |
| 冻结阈值 | `no_show_count >= 3` |

> 规则来源：REQ-GLOBAL §10.3 爽约规则（冻结触发：no_show_count >= 3 时，冻结用户预约权限）

### 18.4 爽约计数接口（供 APPOINTMENT 模块调用）

> **调用方：** APPOINTMENT 模块 F-APPINTMENT-004 STEP 6
> **执行方式：** 在取消预约的同一事务内执行

```sql
-- 爽约计数 +1（在取消预约事务内执行）
UPDATE sys_user
SET no_show_count = no_show_count + 1,
    update_time = NOW()
WHERE id = #{userId};
```

### 18.5 爽约冻结触发（供 APPOINTMENT 模块调用）

> **调用方：** APPOINTMENT 模块 F-APPOINTMENT-004 STEP 6
> **执行方式：** 爽约计数 +1 后，立即校验是否需要触发冻结

> 规则来源：REQ-GLOBAL §10.3 爽约规则（冻结时长 7 天，冻结实现设置 freeze_end_time = NOW() + INTERVAL 7 DAY）

```sql
-- 爽约冻结（no_show_count >= 3 且当前未冻结时执行）
UPDATE sys_user
SET freeze_start_time = NOW(),
    freeze_end_time = DATE_ADD(NOW(), INTERVAL 7 DAY),
    update_time = NOW()
WHERE id = #{userId}
  AND no_show_count >= 3
  AND (freeze_end_time IS NULL OR freeze_end_time <= NOW());
```

### 18.6 爽约完整流程

```
用户取消预约（F-APPOINTMENT-004）
    │
    ▼
cancel_time >= appointment_date ?
    │
    ├─ 否 → 正常取消，不计爽约
    │
    └─ 是 → no_show_count += 1
              │
              ▼
         no_show_count >= 3 且当前未冻结？
              │
              ├─ 否 → 取消完成
              │
              └─ 是 → 设置 freeze_start_time = NOW()
                        freeze_end_time = NOW() + 7天
                        │
                        ▼
                   冻结到期？
                        │
                        ├─ 否 → 冻结中，禁止创建预约（返回 1012）
                        │
                        └─ 是 → 自动解冻，可正常预约
```

### 18.7 并发控制

```sql
-- 爽约计数在取消预约事务内执行
-- 取消预约已通过 SELECT FOR UPDATE 锁定预约行
-- sys_user 行通过取消预约事务的行级锁保护
-- 多个取消操作并发时，no_show_count 更新串行执行，不会丢失计数
```

### 18.8 事务归属

爽约计数和爽约冻结的 SQL **必须在取消预约事务内执行**，不属于独立事务。

| 操作 | 事务归属 | 说明 |
|------|----------|------|
| `no_show_count += 1` | 取消预约事务（T2） | 与预约状态变更同一事务 |
| 设置冻结时间 | 取消预约事务（T2） | 与爽约计数同一事务 |

---

## 19. 冻结状态管理

### 19.1 冻结类型

本系统采用统一冻结模型，冻结时同时设置 `status` 和 `freeze_end_time`，解冻时同时清除两者：

| 冻结类型 | 字段 | 触发条件 | 影响 |
|----------|------|----------|------|
| **管理员冻结** | `sys_user.status = 1` + `freeze_end_time = NOW()+7天` | 管理员手动冻结（F-USER-012） | 无法登录 + 无法创建预约 |
| **爽约冻结** | `sys_user.freeze_end_time` | 累计爽约 ≥ 3次 | 可登录 + 无法创建预约 |

> **统一冻结模型：** 管理员冻结时同时设置 `status=1` 和 `freeze_end_time=NOW()+7天`；解冻时同时清除 `status` 和 `freeze_end_time`。爽约冻结仅设置 `freeze_end_time`，不影响 `status`。

### 19.2 冻结校验矩阵

| 操作 | 管理员冻结 status=1 | 爽约冻结 freeze_end_time > NOW() | 校验结果 |
|------|---------------------|-------------------------------|----------|
| 用户登录 | 拒绝 | 忽略 | 返回 7009 USER_FROZEN |
| 创建预约 | 拒绝（status != 0） | 拒绝（freeze_end_time > NOW()） | 返回 1012 NO_SHOW_FROZEN |
| 查看预约 | 允许 | 允许 | 通过 |
| 取消预约 | 允许 | 允许 | 通过 |
| 查看儿童档案 | 允许 | 允许 | 通过 |
| 管理儿童档案 | 允许 | 允许 | 通过 |

### 19.3 管理员冻结

#### 冻结操作

```sql
-- 统一模型：同时设置 status 和 freeze_end_time
UPDATE sys_user SET status = 1, freeze_end_time = DATE_ADD(NOW(), INTERVAL 7 DAY), update_time = NOW() WHERE id = #{userId};
```

- 执行角色：SUPER_ADMIN
- 触发方式：管理端手动操作
- 解除方式：管理员手动解冻（F-USER-013）

#### 解冻操作

```sql
-- 统一模型：同时清除 status 和 freeze_end_time
UPDATE sys_user SET status = 0, freeze_end_time = NULL, update_time = NOW() WHERE id = #{userId};
```

### 19.4 爽约冻结

#### 冻结参数

| 参数 | 值 | 说明 |
|------|-----|------|
| 触发阈值 | `no_show_count >= 3` | 累计爽约次数 |
| 冻结时长 | 7天 | 从触发时刻起算 |
| 冻结字段 | `freeze_start_time`, `freeze_end_time` | 记录在 `sys_user` 表 |
| 冻结范围 | 仅禁止创建新预约 | 不影响登录、查看、取消已有预约 |

#### 冻结校验（预约创建时）

> 由 APPOINTMENT 模块 F-APPOINTMENT-001 STEP 2 调用

```sql
-- 预约创建时校验冻结状态
SELECT status, freeze_end_time, no_show_count
FROM sys_user
WHERE id = #{userId};

-- 校验逻辑
IF status != 0 THEN
    -- 管理员冻结，拒绝创建预约
    RETURN 1012 NO_SHOW_FROZEN
END IF;

IF freeze_end_time IS NOT NULL AND freeze_end_time > NOW() THEN
    -- 爽约冻结中，拒绝创建预约，返回解冻时间
    RETURN 1012 NO_SHOW_FROZEN (附带 freeze_end_time)
END IF;
```

#### 自动解冻机制

| 方式 | 说明 |
|------|------|
| 条件判断解冻 | 冻结到期后，下次请求时 `freeze_end_time < NOW()` 自然通过校验 |
| 无需定时任务 | 不需要定时任务清空冻结字段 |
| 字段保留 | `freeze_start_time` 和 `freeze_end_time` 历史值保留不清除 |

### 19.5 冻结状态查询接口

> 供各模块在需要时查询用户冻结状态

```sql
SELECT
    id, status, no_show_count,
    freeze_start_time, freeze_end_time,
    CASE
        WHEN status = 1 THEN 'ADMIN_FROZEN'
        WHEN freeze_end_time IS NOT NULL AND freeze_end_time > NOW() THEN 'NO_SHOW_FROZEN'
        ELSE 'NORMAL'
    END AS freeze_type
FROM sys_user
WHERE id = #{userId};
```

### 19.6 冻结状态返回结构

```json
{
  "freezeType": "NO_SHOW_FROZEN",
  "freezeEndTime": "2026-04-09 14:30:00",
  "noShowCount": 3,
  "message": "爽约次数已达上限，预约权限冻结至 2026-04-09"
}
```

---

## 20. 用户状态流转

### 20.1 用户账号状态（sys_user.status）

```
status=0（正常）
    │
    ├─ 管理员冻结 ──→ status=1（已冻结）+ freeze_end_time=NOW()+7天
    │                     │
    │                     └─ 管理员解冻 ──→ status=0（正常）+ freeze_end_time=NULL
    │
    └─ 管理员注销 ──→ status=2（已注销）【终态】
```

| 操作 | 前置状态 | 目标状态 | freeze_end_time 变化 | 执行角色 |
|------|----------|----------|----------------------|----------|
| 冻结 | 0（正常） | 1（已冻结） | 设置为 NOW()+7天 | SUPER_ADMIN |
| 解冻 | 1（已冻结） | 0（正常） | 清除为 NULL | SUPER_ADMIN |
| 注销 | 0（正常）/ 1（已冻结） | 2（已注销） | 不变 | SUPER_ADMIN |

### 20.2 预约冻结状态（爽约冻结）

```
no_show_count < 3
    │
    └─ 爽约累计达到3次 ──→ freeze_end_time = NOW() + 7天
                              │
                              └─ 冻结到期 ──→ 自动解冻（条件判断通过）
```

> 预约冻结不修改 `sys_user.status`，仅设置 `freeze_end_time`。管理员冻结采用统一模型，同时设置 `status=1` 和 `freeze_end_time=NOW()+7天`。

### 20.3 状态校验矩阵

> 规则来源：REQ-GLOBAL §2.1 预约状态枚举（状态值定义）、§2.3 状态分组（正常进行中 1,6,7,8,10）、§4.2 系统级错误码（1011 USER_FROZEN / 1012 NO_SHOW_FROZEN / 7009 USER_FROZEN_LOGIN）

| 当前状态 | 尝试操作 | 结果 |
|----------|----------|------|
| status=0, freeze_end=NULL | 登录 | 允许 |
| status=0, freeze_end=NULL | 创建预约 | 允许 |
| status=0, freeze_end>NOW() | 登录 | 允许 |
| status=0, freeze_end>NOW() | 创建预约 | 拒绝（1012） |
| status=1 | 登录 | 拒绝（7009） |
| status=1 | 创建预约 | 拒绝（1012） |
| status=2 | 登录 | 拒绝（7009） |
| status=2 | 所有操作 | 拒绝 |

---

## 21. 预约约束支持

### 21.1 USER 模块为 APPOINTMENT 提供的校验接口

预约模块在创建预约时依赖 USER 模块提供以下校验数据：

| 校验项 | 校验SQL | 返回数据 | 预约模块使用位置 |
|--------|---------|----------|-----------------|
| 用户状态校验 | `SELECT status, freeze_end_time, no_show_count FROM sys_user WHERE id = ?` | status, freeze_end_time | F-APPOINTMENT-001 STEP 2 |
| 儿童档案归属校验 | `SELECT id FROM child_profile WHERE id = ? AND parent_id = ?` | childId | F-APPOINTMENT-001 STEP 3 |
| 爽约计数 | `UPDATE sys_user SET no_show_count = no_show_count + 1 WHERE id = ?` | - | F-APPOINTMENT-004 STEP 6 |
| 爽约冻结 | `UPDATE sys_user SET freeze_start_time=?, freeze_end_time=? WHERE id = ? AND no_show_count >= 3` | - | F-APPOINTMENT-004 STEP 6 |

### 21.2 用户状态校验（完整逻辑）

> 供 APPOINTMENT 模块 F-APPOINTMENT-001 STEP 2 调用

```java
/**
 * 预约创建 — 用户状态校验
 * @return UserStatusCheckResult 包含是否允许预约、冻结类型、解冻时间
 */
public UserStatusCheckResult checkUserStatusForAppointment(Long userId) {
    SysUser user = userMapper.selectById(userId);
    if (user == null) {
        throw new BusinessException(7003, "用户不存在");
    }

    // 1. 管理员冻结校验
    if (user.getStatus() != 0) {
        return UserStatusCheckResult.rejected("ADMIN_FROZEN", "账号已被冻结，无法预约");
    }

    // 2. 爽约冻结校验
    if (user.getFreezeEndTime() != null && user.getFreezeEndTime().isAfter(LocalDateTime.now())) {
        return UserStatusCheckResult.rejected("NO_SHOW_FROZEN",
            "爽约次数过多，预约权限冻结至" + user.getFreezeEndTime());
    }

    return UserStatusCheckResult.allowed();
}
```

### 21.3 儿童档案预约约束

| 约束 | 说明 | 校验时机 |
|------|------|----------|
| 归属校验 | `child_profile.parent_id = #{userId}` | 创建预约时 |
| 存在性校验 | `child_profile.id = #{childId}` | 创建预约时 |
| 删除约束 | 存在进行中预约时不允许删除 | 删除儿童档案时 |
| 数量限制 | 每用户最多5个 | 创建儿童档案时 |

### 21.4 sys_user 表预约相关字段汇总

| 字段 | 类型 | 预约用途 | 写入方 | 读取方 |
|------|------|----------|--------|--------|
| id | bigint | 预约关联外键 | USER 注册 | APPOINTMENT 创建预约 |
| status | tinyint | 账号状态校验 | ADMIN 冻结/解冻 | APPOINTMENT 创建预约 |
| no_show_count | int | 爽约累计计数 | APPOINTMENT 取消预约 | USER 查询用户信息 |
| freeze_start_time | datetime | 爽约冻结开始 | APPOINTMENT 取消预约 | USER 查询用户信息 |
| freeze_end_time | datetime | 爽约冻结截止 | APPOINTMENT 取消预约 | APPOINTMENT 创建预约 |

### 21.5 child_profile 表预约相关字段汇总

| 字段 | 类型 | 预约用途 | 写入方 | 读取方 |
|------|------|----------|--------|--------|
| id | bigint | 预约关联外键 | USER 创建档案 | APPOINTMENT 创建预约 |
| parent_id | bigint | 归属校验 | USER 创建档案 | APPOINTMENT 创建预约 |
| parent_id_card | varchar | 签到身份核验 | USER 创建/修改档案 | FLOW-SIGNIN 签到 |
| name | varchar | 预约详情展示 | USER 创建/修改档案 | APPOINTMENT 查询详情 |
| gender | tinyint | 预约详情展示 | USER 创建/修改档案 | APPOINTMENT 查询详情 |
| birth_date | date | 预约详情展示 | USER 创建/修改档案 | APPOINTMENT 查询详情 |

---

## 22. 权限控制汇总

> 本模块权限编码和角色分配规则参见 REQ-GLOBAL §9.3 按模块的权限矩阵（全系统唯一权限来源）。

### 22.1 功能权限矩阵

| 功能编号 | 权限编码 | 允许角色 | API路径 |
|----------|----------|----------|---------|
| F-USER-001 | 无（公开） | 匿名 | `POST /api/public/register` |
| F-USER-002 | 无（公开） | 匿名 | `POST /api/public/login` |
| F-USER-003 | 无 | USER | `POST /api/user/logout` |
| F-USER-004 | 无 | USER | `POST /api/user/changePassword` |
| F-USER-005 | 无（公开） | 匿名 | `POST /api/public/resetPassword` |
| F-USER-006 | 无 | USER | `GET /api/user/profile` |
| F-USER-007 | 无 | USER | `PUT /api/user/profile` |
| F-USER-012 | `user.manage` | SUPER_ADMIN | `POST /api/admin/user/{userId}/freeze` |
| F-USER-013 | `user.manage` | SUPER_ADMIN | `POST /api/admin/user/{userId}/unfreeze` |
| F-USER-019 | `child.view.own` | USER | `GET /api/user/children` |
| F-USER-020 | `child.view.own` | USER | `GET /api/user/child/{childId}` |
| F-USER-021 | `child.add.own` | USER | `POST /api/user/child` |
| F-USER-022 | `child.edit.own` | USER | `PUT /api/user/child/{childId}` |
| F-USER-023 | `child.delete.own` | USER | `DELETE /api/user/child/{childId}` |

### 22.2 数据权限规则

| 角色 | 数据范围 | 实现方式 |
|------|----------|----------|
| USER | 仅自己的用户信息和儿童档案 | `WHERE id = #{userId}` / `WHERE parent_id = #{userId}` |
| SUPER_ADMIN | 全部用户 | 无限制 |

### 22.3 API路径与角色映射

| API前缀 | 允许角色 | 说明 |
|---------|----------|------|
| `/api/public/*` | 匿名 | 注册、登录、重置密码 |
| `/api/user/*` | USER | 用户个人操作 |
| `/api/admin/*` | SUPER_ADMIN | 管理员操作 |

---

## 23. 通用系统异常

> 本节定义 USER 模块中适用于所有功能的通用系统异常错误码。系统级错误码（1001/1003/1004/1005/1007）的权威定义参见 REQ-GLOBAL §4.2 系统级错误码。

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 系统繁忙（DB连接失败） | 1007 | 500 | 返回友好提示 |
| 系统繁忙（并发冲突） | 1005 | 409 | 返回友好提示 |
| 参数校验失败：{field_name} | 1003 | 400 | 返回具体字段错误 |
| 登录已过期，请重新登录 | 1001 | 401 | 清除Token，跳转登录页 |

---

## 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| V1.0 | 2026-04-02 | 初始版本，基于 PRD-USER V1.3 / REQ-GLOBAL V1.0 生成 |
| V1.1 | 2026-04-03 | V3 评审修复：(1) 错误码对齐 GLOBAL §4；(2) 业务规则修正（冻结/密码/过期爽约/FEFO）；(3) 版本号/依赖版本对齐；(4) F-029 注销账户补充；(5) 模板补全（§5/§6/§7）；(6) 功能编号归属标注 |
| V1.2 | 2026-04-03 | REQ 评审修复：(1) §8.5/§20.1 术语"已禁用"统一为"已冻结"（5处）；(2) 文档头上游依赖同步至 REQ-GLOBAL V1.3 |

---

**文档结束**
