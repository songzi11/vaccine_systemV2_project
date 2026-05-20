# PRD-APPOINTMENT 疫苗预约模块 PRD

## 2. 功能清单

### 2.1 功能清单

| 编号 | 功能名称 | 功能描述 | 使用角色 | 优先级 |
|------|----------|----------|----------|--------|
| F-APPOINTMENT-001 | 创建预约 | 用户提交预约申请 | USER | P0 |
| F-APPOINTMENT-002 | 查询预约详情 | 查询预约详情 | USER | P0 |
| F-APPOINTMENT-005 | 获取窗口指引 | 获取窗口指引 | USER | P0 |
| F-APPOINTMENT-007 | 查询今日预约 | 查询今日预约 | DOCTOR_SIGNIN | P0 | → PRD-FLOW F-FLOW-001 |

### 6.1 预约表（appointment）字段

| 字段名 | 中文名 | 数据类型 | 长度 | 是否必填 | 默认值 | 说明 |
|--------|--------|----------|------|----------|--------|------|
| id | 预约ID | bigint | - | 是 | - | 主键 |
| user_id | 用户ID | bigint | - | 是 | - | 外键 |
| status | 预约状态 | tinyint | - | 是 | 1 | 状态 |
| appointment_date | 预约日期 | date | - | 是 | - | 日期 |

### 7.1 功能权限映射

| 功能编号 | 功能名称 | 需要权限 | 角色说明 |
|----------|----------|----------|----------|
| F-APPOINTMENT-001 | 创建预约 | appointment.book | USER |
| F-APPOINTMENT-002 | 查询预约详情 | appointment.view.own | USER |

#### 用户端权限

| 权限编码 | 权限名称 | 说明 | 允许操作 |
|----------|----------|------|----------|
| appointment.book | 预约接种 | 创建预约 | 创建预约 |
| appointment.view.own | 查看自己的预约 | 查询预约 | 查询 |

### 8. 异常场景

#### E-APPOINTMENT-001：用户已冻结

**场景描述：** 用户被冻结

**错误码：** 1001
**错误消息：** `账户已冻结`
**HTTP状态码：** 403 Forbidden

---

#### E-APPOINTMENT-002：时段已满

**场景描述：** 时段预约已满

**错误码：** 2005
**错误消息：** `时段已满`
**HTTP状态码：** 409 Conflict

---

#### E-APPOINTMENT-101：数据库异常

**场景描述：** 数据库连接失败

**错误码：** 5001
**错误消息：** `系统繁忙`
**HTTP状态码：** 500

---

## 附录A：API接口映射

### A.1 用户端API

| 接口路径 | 请求方法 | 功能编号 | 说明 |
|----------|----------|----------|------|
| /api/user/appointment | POST | F-APPOINTMENT-001 | 创建预约 |
| /api/user/appointment/{appointmentId} | GET | F-APPOINTMENT-002 | 查询详情 |
| /api/user/appointment/{appointmentId}/guide | GET | F-APPOINTMENT-005 | 窗口指引 |
