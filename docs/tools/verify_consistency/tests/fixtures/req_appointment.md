# REQ-APPOINTMENT 预约模块需求

## 2. 功能清单

| 编号 | 功能名称 | API | 角色 | 优先级 |
|------|----------|-----|------|--------|
| F-APPOINTMENT-001 | 创建预约 | `POST /api/user/appointment` | USER | P0 |
| F-APPOINTMENT-002 | 查询预约详情 | `GET /api/user/appointment/{appointmentId}` | USER | P0 |
| F-APPOINTMENT-004 | 取消预约 | `DELETE /api/user/appointment/{appointmentId}` | USER | P1 |
| F-APPOINTMENT-013 | 过期处理 | 内部定时任务 | SYSTEM | P1 |

## 3. F-APPOINTMENT-001 创建预约

### 3.5 数据操作（SQL级）

```sql
INSERT INTO appointment (user_id, child_id, status)
VALUES (#{userId}, #{childId}, 1);
```

### 3.7 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 用户未登录 | 1001 | 401 | 拦截 |
| 儿童档案不存在 | 2001 | 404 | 返回 |
| 时段已满 | 2005 | 409 | 返回 |

### 3.9 权限控制

| 控制项 | 规则 |
|--------|------|
| 功能权限 | `appointment.book` |

## 4. F-APPOINTMENT-004 取消预约

### 4.7 异常处理

| 异常场景 | 错误码 | HTTP | 处理方式 |
|----------|--------|------|----------|
| 预约不存在 | 2007 | 404 | 返回 |

## 12. 权限控制汇总

### 12.1 功能权限矩阵

| 功能编号 | 权限编码 | 允许角色 | API路径 |
|----------|----------|----------|---------|
| F-APPOINTMENT-001 | `appointment.book` | USER | `POST /api/user/appointment` |
| F-APPOINTMENT-002 | `appointment.view.own` | USER | `GET /api/user/appointment/{id}` |
| F-APPOINTMENT-004 | `appointment.cancel.own` | USER | `DELETE /api/user/appointment/{id}` |
| F-APPOINTMENT-013 | 系统内部 | SYSTEM | 内部定时任务 |
