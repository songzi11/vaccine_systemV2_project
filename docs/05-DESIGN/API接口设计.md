# 疫苗管理系统 — API 接口设计

| 项目 | 内容 |
|------|------|
| 文档编号 | API-DESIGN-001 |
| 版本 | V1.1 |
| 状态 | 评审修复后发布 |
| 创建日期 | 2026-04-03 |
| 前置文档 | ARCH-DESIGN-001（系统架构设计）、DOMAIN-APP-SERVICE-001（应用层设计） |

---

## 1. 接口概述

### 1.1 基础信息

| 项目 | 值 |
|------|---|
| Base URL | `/api/v1` |
| 协议 | HTTPS |
| 数据格式 | JSON（`Content-Type: application/json`） |
| 字符编码 | UTF-8 |
| 认证方式 | JWT Bearer Token |
| API 文档 | Knife4j（`/doc.html`） |

### 1.2 统一响应格式

**成功响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "timestamp": 1700000000000
}
```

**业务错误响应：**
```json
{
  "code": 2005,
  "message": "预约时段已满",
  "data": null,
  "timestamp": 1700000000000
}
```

**HTTP 状态码约定：**

| 场景 | HTTP 状态码 | business code | 说明 |
|------|-----------|-------------|------|
| 正常成功 | 200 | 200 | data 返回业务数据 |
| 业务规则失败 | 200 | 非 200 | 前端根据 code 处理，不弹网络错误 |
| 未认证 | 401 | 1001 | Token 无效或过期 |
| 无权限 | 403 | 1002 | 角色权限不足 |
| 参数校验失败 | 200 | 1003 | @Valid 校验不通过 |
| 系统异常 | 500 | 1007 | 全局兜底 |

> **设计决策**：业务规则失败返回 HTTP 200 + 非 200 的 business code，前端统一在响应拦截器中处理。只有认证/权限/系统级错误才使用非 200 HTTP 状态码。

### 1.3 分页约定

**请求：**
```
GET /api/v1/appointments?page=1&size=20
```

| 参数 | 类型 | 默认值 | 说明 |
|------|------|-------|------|
| page | int | 1 | 页码（从 1 开始） |
| size | int | 20 | 每页条数 |

**响应：**
```json
{
  "code": 200,
  "data": {
    "records": [ ... ],
    "total": 100,
    "page": 1,
    "size": 20,
    "pages": 5
  }
}
```

### 1.4 认证机制

**请求头：**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**JWT Payload：**
```json
{
  "sub": "1",
  "phone": "13800000001",
  "roles": ["USER"],
  "iat": 1700000000,
  "exp": 1700086400
}
```

**Token 有效期**：24 小时（86400000ms），滑动续期。

**登出处理**：Token 加入 Redis 黑名单（TTL = Token 剩余有效期），后续请求被拒绝。

### 1.5 API 路由与角色映射

| 路由前缀 | 允许角色 | 需要认证 |
|---------|---------|---------|
| `/api/v1/public/sms/send` | 无 | 否 |
| `/api/v1/public/auth/login` | 无 | 否 |
| `/api/v1/public/vaccines` | 无（对应 vaccine.catalog.view） | 否 |
| `/api/v1/public/notices` | 无（对应 notice.view） | 否 |
| `/api/v1/public/notices/{id}/feedback` | USER+（对应 notice.feedback） | 是 |
| `/api/v1/user/*` | USER | 是 |
| `/api/v1/signin/*` | DOCTOR_SIGNIN | 是 |
| `/api/v1/precheck/*` | DOCTOR_PRECHECK | 是 |
| `/api/v1/register/*` | DOCTOR_REGISTER | 是 |
| `/api/v1/vaccinate/*` | DOCTOR_VACCINATE | 是 |
| `/api/v1/observe/*` | DOCTOR_OBSERVE | 是 |
| `/api/v1/stock/*` | DOCTOR_STOCK | 是 |
| `/api/v1/schedule/*` | DOCTOR_SCHEDULE, DOCTOR_BUSINESS_ADMIN | 是 |
| `/api/v1/business/*` | DOCTOR_BUSINESS_ADMIN | 是 |
| `/api/v1/admin/*` | SUPER_ADMIN | 是 |

---

## 2. 公共接口（无需认证）

### 2.1 发送短信验证码

```
POST /api/v1/public/sms/send
```

**请求体：**
```json
{
  "phone": "13800000001",
  "type": "REGISTER"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| phone | string | 是 | 手机号（11位） |
| type | string | 是 | 用途：REGISTER / RESET_PASSWORD |

**响应：**
```json
{
  "code": 200,
  "message": "验证码已发送",
  "data": null
}
```

**限流**：同一手机号 60 秒内只能发送一次。

---

### 2.2 用户登录

```
POST /api/v1/public/auth/login
```

**请求体：**
```json
{
  "phone": "13800000001",
  "password": "123456"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| phone | string | 是 | 手机号 |
| password | string | 是 | 密码 |

**响应：**
```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1,
    "phone": "13800000001",
    "roles": ["USER"]
  }
}
```

---

### 2.3 获取疫苗目录（公共）

```
GET /api/v1/public/vaccines
```

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| vaccineType | string | 否 | 按类型筛选（CLASS_I/CLASS_II） |
| keyword | string | 否 | 疫苗名称模糊搜索 |

**响应：**
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "vaccineName": "乙型肝炎疫苗",
      "vaccineType": "CLASS_I",
      "manufacturer": "深圳康泰生物",
      "isOnShelf": 1
    }
  ]
}
```

> 对应权限码：`vaccine.catalog.view`（通用权限，所有认证用户均可访问）

### 2.4 获取公告列表（公共）

```
GET /api/v1/public/notices?status=1&page=1&size=10
```

**响应：**
```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "title": "疫苗接种通知",
        "noticeType": "NORMAL",
        "content": "...",
        "publishTime": "2026-04-01 10:00:00"
      }
    ],
    "total": 1,
    "page": 1,
    "size": 10,
    "pages": 1
  }
}
```

> 对应权限码：`notice.view`（通用权限）

### 2.5 提交公告反馈（需认证）

```
POST /api/v1/public/notices/{noticeId}/feedback
```

**请求体：**
```json
{
  "content": "通知内容很清晰"
}
```

> 对应权限码：`notice.feedback`（通用权限，需登录）

---

## 3. 用户模块（`/api/v1/user/*`）

### 3.1 用户注册

```
POST /api/v1/user/register
```

**请求体：**
```json
{
  "phone": "13800000001",
  "smsCode": "123456",
  "password": "abc123456",
  "realName": "张三"
}
```

| 字段 | 类型 | 必填 | 校验规则 |
|------|------|------|---------|
| phone | string | 是 | 11位手机号 |
| smsCode | string | 是 | 6位数字 |
| password | string | 是 | 6-20位，必须包含字母和数字 |
| realName | string | 是 | 2-50个字符 |

### 3.2 修改密码

```
PUT /api/v1/user/password
```

**请求体：**
```json
{
  "oldPassword": "abc123456",
  "newPassword": "def654321"
}
```

### 3.3 重置密码

```
POST /api/v1/user/password/reset
```

**请求体：**
```json
{
  "phone": "13800000001",
  "smsCode": "123456",
  "newPassword": "abc123456"
}
```

### 3.4 获取用户信息

```
GET /api/v1/user/profile
```

**响应：**
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "username": "13800000001",
    "phone": "138****0001",
    "realName": "张三",
    "gender": 1,
    "status": 0,
    "roles": ["USER"]
  }
}
```

### 3.5 更新用户信息

```
PUT /api/v1/user/profile
```

**请求体：**
```json
{
  "realName": "张三",
  "gender": 1,
  "idCardType": 1,
  "idCardNo": "120101199001011234"
}
```

### 3.6 用户登出

```
POST /api/v1/user/logout
```

**请求体：** 无

---

## 4. 儿童档案（`/api/v1/user/children/*`）

### 4.1 获取儿童列表

```
GET /api/v1/user/children
```

**响应：**
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "小明",
      "gender": 1,
      "birthDate": "2023-06-15",
      "idCardNo": "120101202306150012"
    }
  ]
}
```

### 4.2 添加儿童

```
POST /api/v1/user/children
```

**请求体：**
```json
{
  "name": "小明",
  "gender": 1,
  "birthDate": "2023-06-15",
  "idCardNo": "120101202306150012"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 是 | 儿童姓名 |
| gender | int | 是 | 1男/2女 |
| birthDate | string | 是 | 出生日期（yyyy-MM-dd） |
| idCardNo | string | 否 | 证件号码 |

### 4.3 修改儿童信息

```
PUT /api/v1/user/children/{childId}
```

**请求体：** 同添加（全部字段可选）

### 4.4 删除儿童

```
DELETE /api/v1/user/children/{childId}
```

**前置条件：** 该儿童无进行中的预约（status ∈ {1,6,7,8,10}）

---

## 5. 预约模块（`/api/v1/user/appointments/*`）

### 5.1 创建预约

```
POST /api/v1/user/appointments
```

**请求体：**
```json
{
  "childId": 1,
  "vaccineId": 1,
  "appointmentDate": "2026-04-10",
  "timeSlot": "AM"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| childId | long | 是 | 儿童档案ID |
| vaccineId | long | 是 | 疫苗ID |
| appointmentDate | string | 是 | 预约日期 |
| timeSlot | string | 是 | AM / PM |

**业务规则：**
- 预约日期必须在今天之后且不超过 `appointment.advance_days` 天
- 疫苗必须上架（is_on_shelf = 1）
- 同一儿童同日同疫苗不可重复预约
- 时段容量不可超过 `appointment.max_capacity`

### 5.2 取消预约

```
POST /api/v1/user/appointments/{appointmentId}/cancel
```

**请求体：**
```json
{
  "reason": "临时有事"
}
```

**前置条件：** 预约状态必须为 1（已预约）

### 5.3 获取预约详情

```
GET /api/v1/user/appointments/{appointmentId}
```

**响应：**
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "appointmentNo": "APT202604100001",
    "status": 1,
    "statusText": "已预约",
    "childName": "小明",
    "vaccineName": "乙型肝炎疫苗",
    "vaccineType": "CLASS_I",
    "appointmentDate": "2026-04-10",
    "timeSlot": "AM",
    "createTime": "2026-04-03 10:00:00"
  }
}
```

### 5.4 获取我的预约列表

```
GET /api/v1/user/appointments?status=1&page=1&size=20
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | int | 否 | 按状态筛选（不传则查全部） |
| page | int | 否 | 页码 |
| size | int | 否 | 每页条数 |

### 5.5 获取流程指引

```
GET /api/v1/user/appointments/{appointmentId}/guide
```

**响应：**
```json
{
  "code": 200,
  "data": {
    "appointmentId": 1,
    "status": 6,
    "statusText": "已签到",
    "currentWindow": "PRECHECK_01",
    "nextGuide": {
      "action": "WAIT_PRECHECK",
      "message": "请前往预检窗口1等待预检",
      "windowCode": "PRECHECK_01"
    }
  }
}
```

### 5.6 获取排队信息

```
GET /api/v1/user/appointments/{appointmentId}/queue
```

**响应：**
```json
{
  "code": 200,
  "data": {
    "windowCode": "PRECHECK_01",
    "currentQueue": 3,
    "capacity": 1,
    "estimatedWaitMinutes": 15
  }
}
```

---

## 6. 签到模块（`/api/v1/signin/*`）

### 6.1 执行签到

```
POST /api/v1/signin/execute
```

**请求体：**
```json
{
  "appointmentId": 1,
  "idCard": "120101202306150012"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| appointmentId | long | 是 | 预约ID |
| idCard | string | 是 | 儿童身份证号 |

**业务规则：**
- 预约状态必须为 1（已预约）
- 必须是当日预约
- 身份证号必须与儿童档案匹配
- 签到后状态 → 6（已签到）

### 6.2 获取今日预约列表

```
GET /api/v1/signin/today?filter=SIGNED_IN&page=1&size=20
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| filter | string | 否 | ALL / SIGNED_IN / PRECHECK_PASS / PRECHECK_FAIL / REGISTERED / OBSERVING |
| date | string | 否 | 日期（默认今天） |

---

## 7. 预检模块（`/api/v1/precheck/*`）

### 7.1 获取预检队列

```
GET /api/v1/precheck/queue?date=2026-04-03
```

**响应：**
```json
{
  "code": 200,
  "data": [
    {
      "appointmentId": 1,
      "queueNo": "A001",
      "childName": "小明",
      "vaccineName": "乙型肝炎疫苗",
      "signinTime": "2026-04-03 08:30:00"
    }
  ]
}
```

### 7.2 执行预检

```
POST /api/v1/precheck/execute
```

**请求体：**
```json
{
  "appointmentId": 1,
  "bodyTemperature": 36.5,
  "weight": 12.5,
  "height": 85.0,
  "healthStatus": "NORMAL",
  "allergyHistory": "无",
  "medicationRecent": "无",
  "diseaseHistory": "无",
  "vaccinationRecent": "无"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| appointmentId | long | 是 | 预约ID |
| bodyTemperature | decimal | 是 | 体温（>37.3°C 判定异常） |
| weight | decimal | 否 | 体重（kg） |
| height | decimal | 否 | 身高（cm） |
| healthStatus | string | 是 | 健康状况评估 |
| allergyHistory | string | 否 | 过敏史 |
| medicationRecent | string | 否 | 近期用药 |
| diseaseHistory | string | 否 | 疾病史 |
| vaccinationRecent | string | 否 | 近期接种史 |

**业务规则：**
- 状态 6（已签到）→ 预检
- 体温 > 37.3°C → 预检失败，状态 → 9
- 健康状况异常 → 预检失败，状态 → 9
- 通过 → 状态 → 7（预检通过）

---

## 8. 登记模块（`/api/v1/register/*`）

### 8.1 获取登记队列

```
GET /api/v1/register/queue?date=2026-04-03
```

**响应：**
```json
{
  "code": 200,
  "data": [
    {
      "appointmentId": 1,
      "queueNo": "A001",
      "childName": "小明",
      "vaccineName": "乙型肝炎疫苗",
      "checkResult": "PASS",
      "precheckTime": "2026-04-03 08:35:00"
    }
  ]
}
```

### 8.2 获取可用批次列表

```
GET /api/v1/register/batches/{vaccineId}
```

**响应：**
```json
{
  "code": 200,
  "data": [
    {
      "batchId": 1,
      "batchNo": "B20260101001",
      "manufacturer": "深圳康泰生物",
      "expiryDate": "2027-01-01",
      "availableStock": 45,
      "lockedStock": 5
    }
  ]
}
```

> 按有效期升序排列（FEFO 策略），仅返回 status=0 且 available_stock > locked_stock 的批次。

### 8.3 执行登记

```
POST /api/v1/register/execute
```

**请求体：**
```json
{
  "appointmentId": 1
}
```

**业务规则（事务 T5）：**
- 状态 7（预检通过）→ 登记
- 自动 FEFO 选批 → 锁定库存
- 生成排队号
- 状态 → 8（已登记）

**响应：**
```json
{
  "code": 200,
  "data": {
    "registerId": 1,
    "queueNo": "A001",
    "batchNo": "B20260101001"
  }
}
```

### 8.4 切换批次

```
POST /api/v1/register/{appointmentId}/switch-batch
```

**请求体：**
```json
{
  "newBatchId": 2
}
```

**业务规则：** 释放旧批次锁定库存 → 锁定新批次

---

## 9. 接种模块（`/api/v1/vaccinate/*`）

### 9.1 获取接种队列

```
GET /api/v1/vaccinate/queue?date=2026-04-03
```

**响应：**
```json
{
  "code": 200,
  "data": [
    {
      "appointmentId": 1,
      "queueNo": "A001",
      "childName": "小明",
      "vaccineName": "乙型肝炎疫苗",
      "batchNo": "B20260101001",
      "registerTime": "2026-04-03 08:40:00"
    }
  ]
}
```

### 9.2 核实接种信息

```
GET /api/v1/vaccinate/{appointmentId}/verify
```

**响应：**
```json
{
  "code": 200,
  "data": {
    "appointmentId": 1,
    "childName": "小明",
    "gender": 1,
    "birthDate": "2023-06-15",
    "vaccineName": "乙型肝炎疫苗",
    "vaccineType": "CLASS_I",
    "batchNo": "B20260101001",
    "manufacturer": "深圳康泰生物",
    "expiryDate": "2027-01-01"
  }
}
```

### 9.3 执行接种

```
POST /api/v1/vaccinate/execute
```

**请求体：**
```json
{
  "appointmentId": 1,
  "injectionSite": "LEFT_UPPER_ARM"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| appointmentId | long | 是 | 预约ID |
| injectionSite | string | 是 | LEFT_UPPER_ARM / RIGHT_UPPER_ARM / LEFT_BUTTOCK / RIGHT_BUTTOCK |

**业务规则（事务 T6）：**
- 状态 8（已登记）→ 接种
- 生成注射号
- 扣减库存（locked → available -1, locked -1）
- 创建接种记录
- 状态 → 10（留观中）
- 发布 VaccinationExecutedEvent → 创建留观记录

**响应：**
```json
{
  "code": 200,
  "data": {
    "injectionId": "INJ202604030001",
    "batchNo": "B20260101001",
    "injectionSite": "LEFT_UPPER_ARM"
  }
}
```

### 9.4 接种记录列表

```
GET /api/v1/vaccinate/records?page=1&size=20&startDate=2026-04-01&endDate=2026-04-30
```

### 9.5 儿童接种记录

```
GET /api/v1/vaccinate/records/child/{childId}
```

---

## 10. 留观模块（`/api/v1/observe/*`）

### 10.1 获取留观队列

```
GET /api/v1/observe/queue?date=2026-04-03
```

**响应：**
```json
{
  "code": 200,
  "data": [
    {
      "appointmentId": 1,
      "childName": "小明",
      "injectionId": "INJ202604030001",
      "injectionTime": "2026-04-03 08:45:00",
      "duration": 15,
      "remainingMinutes": 15,
      "observeResult": "NORMAL"
    }
  ]
}
```

### 10.2 查询留观状态

```
GET /api/v1/observe/{injectionId}
```

**响应：**
```json
{
  "code": 200,
  "data": {
    "appointmentId": 1,
    "injectionId": "INJ202604030001",
    "startTime": "2026-04-03 08:45:00",
    "duration": 25,
    "canFinish": false,
    "observeResult": "NORMAL",
    "hasAdverseReaction": false
  }
}
```

### 10.3 确认留观结束

```
POST /api/v1/observe/{appointmentId}/finish
```

**业务规则（事务 T7）：**
- 状态 10（留观中）→ 完成
- 留观时间必须 >= 30 分钟（`observe.min_duration` 配置）
- 若 observe_result = ABNORMAL 且未上报不良反应 → 拒绝（错误码 3009）
- 状态 → 2（已完成）
- 发布 ObservationFinishedEvent

### 10.4 上报不良反应

```
POST /api/v1/observe/adverse/report
```

**请求体：**
```json
{
  "appointmentId": 1,
  "reactionType": "发热",
  "description": "体温38.5°C，持续2小时",
  "severity": "MILD"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| appointmentId | long | 是 | 预约ID |
| reactionType | string | 是 | 反应类型 |
| description | string | 否 | 详细描述 |
| severity | string | 是 | MILD / MODERATE / SEVERE |

### 10.5 处理不良反应

```
POST /api/v1/observe/adverse/{reactionId}/handle
```

**请求体：**
```json
{
  "handleResult": "已给予退热药，观察中"
}
```

---

## 11. 库存模块（`/api/v1/stock/*`）

### 11.1 库存总览

```
GET /api/v1/stock/summary
```

**响应：**
```json
{
  "code": 200,
  "data": [
    {
      "vaccineId": 1,
      "vaccineName": "乙型肝炎疫苗",
      "vaccineType": "CLASS_I",
      "totalStock": 100,
      "availableStock": 80,
      "warningThreshold": 20
    }
  ]
}
```

### 11.2 批次列表

```
GET /api/v1/stock/batches?vaccineId=1&page=1&size=20
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| vaccineId | long | 否 | 按疫苗ID筛选 |
| status | int | 否 | 按批次状态筛选 |
| keyword | string | 否 | 批次号模糊搜索 |

**响应：**
```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "batchId": 1,
        "batchNo": "B20260101001",
        "vaccineName": "乙型肝炎疫苗",
        "manufacturer": "深圳康泰生物",
        "productionDate": "2026-01-01",
        "expiryDate": "2027-01-01",
        "status": 0,
        "statusText": "正常",
        "availableStock": 45,
        "lockedStock": 5
      }
    ],
    "total": 1,
    "page": 1,
    "size": 20,
    "pages": 1
  }
}
```

### 11.3 批次详情

```
GET /api/v1/stock/batches/{batchId}
```

### 11.4 创建库存调拨

```
POST /api/v1/stock/transfer
```

**请求体：**
```json
{
  "batchId": 1,
  "fromType": 0,
  "fromId": 1,
  "toType": 1,
  "toId": 2,
  "quantity": 10,
  "remark": "日常调拨"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| batchId | long | 是 | 批次ID |
| fromType | int | 是 | 调出类型（0总仓/1接种点） |
| fromId | long | 是 | 调出位置ID |
| toType | int | 是 | 调入类型（0总仓/1接种点） |
| toId | long | 是 | 调入位置ID |
| quantity | int | 是 | 调拨数量 |
| remark | string | 否 | 备注 |

### 11.5 调拨记录列表

```
GET /api/v1/stock/transfer/records?page=1&size=20
```

### 11.6 批次销毁

```
POST /api/v1/stock/batches/{batchId}/dispose
```

**请求体：**
```json
{
  "disposeQuantity": 5,
  "disposeReason": "已过期"
}
```

### 11.7 预警列表

```
GET /api/v1/stock/alerts?handled=false&page=1&size=20
```

### 11.8 处理预警

```
PUT /api/v1/stock/alerts/{alertId}/handle
```

---

## 12. 管理模块（`/api/v1/admin/*`）

### 12.1 疫苗目录管理

```
GET    /api/v1/admin/vaccines              # 疫苗列表
POST   /api/v1/admin/vaccines              # 添加疫苗
PUT    /api/v1/admin/vaccines/{vaccineId}  # 修改疫苗
PUT    /api/v1/admin/vaccines/{vaccineId}/shelf-status  # 上下架
```

**添加疫苗请求体：**
```json
{
  "vaccineName": "新型疫苗",
  "vaccineType": "CLASS_II",
  "manufacturer": "某生物制药",
  "description": "疫苗说明"
}
```

### 12.2 角色权限管理

```
GET    /api/v1/admin/roles                        # 角色列表
POST   /api/v1/admin/roles                        # 创建角色
PUT    /api/v1/admin/roles/{roleId}               # 修改角色
DELETE /api/v1/admin/roles/{roleId}               # 删除角色
POST   /api/v1/admin/roles/{roleId}/permissions   # 分配权限
```

**分配权限请求体：**
```json
{
  "permissionCodes": ["appointment.book", "appointment.cancel.own"]
}
```

### 12.3 用户管理

```
GET    /api/v1/admin/users?page=1&size=20          # 用户列表
POST   /api/v1/admin/users/{userId}/freeze         # 冻结用户
POST   /api/v1/admin/users/{userId}/unfreeze       # 解冻用户
POST   /api/v1/admin/users/{userId}/assign-roles   # 分配角色
```

**分配角色请求体：**
```json
{
  "roleIds": [1, 2]
}
```

### 12.4 窗口管理

```
GET    /api/v1/admin/windows                  # 窗口列表
POST   /api/v1/admin/windows                  # 创建窗口
PUT    /api/v1/admin/windows/{windowId}       # 修改窗口
```

### 12.5 排班管理

```
GET    /api/v1/admin/schedules?date=2026-04-03     # 排班列表
POST   /api/v1/admin/schedules                     # 创建排班
PUT    /api/v1/admin/schedules/{scheduleId}        # 修改排班
DELETE /api/v1/admin/schedules/{scheduleId}        # 删除排班
```

### 12.6 系统配置管理

```
GET    /api/v1/admin/configs               # 获取所有配置
PUT    /api/v1/admin/configs               # 更新配置
```

**更新配置请求体：**
```json
{
  "configs": [
    { "configKey": "appointment.max_capacity", "configValue": "60" },
    { "configKey": "observe.min_duration", "configValue": "30" }
  ]
}
```

### 12.7 公告管理

```
GET    /api/v1/admin/notices                # 公告列表
POST   /api/v1/admin/notices                # 创建公告
PUT    /api/v1/admin/notices/{noticeId}     # 修改公告
POST   /api/v1/admin/notices/{noticeId}/audit  # 审核公告
```

### 12.8 统计数据

```
GET /api/v1/admin/stats/overview              # 总览统计
GET /api/v1/admin/stats/vaccination?startDate=2026-04-01&endDate=2026-04-30  # 接种统计
```

**总览统计响应：**
```json
{
  "code": 200,
  "data": {
    "todayAppointments": 15,
    "todayVaccinations": 12,
    "totalUsers": 230,
    "lowStockAlerts": 3,
    "expiredBatches": 1
  }
}
```

---

## 13. 错误码速查

| 模块 | code 范围 | 说明 |
|------|----------|------|
| 系统 | 1001-1012 | 认证、权限、参数错误 |
| 预约 | 2001-2009 | 预约创建/取消相关 |
| 流程 | 3001-3010 | 签到/预检/留观相关 |
| 库存 | 4001-4011 | 库存操作相关 |
| 登记 | 5001-5004 | 登记排队相关 |
| 接种 | 6001-6010 | 接种执行相关 |
| 用户 | 7001-7014 | 用户/儿童档案相关 |
| 管理 | 8001-8014 | 管理员操作相关 |

> 完整错误码列表见 `docs/04-DOMAIN/共享内核.md` §3

---

## 14. 防重放机制

关键写操作要求前端生成 `requestId`（UUID v4），后端在 Redis 中去重：

```
请求头：X-Request-Id: 550e8400-e29b-41d4-a716-446655440000
```

**适用接口：**
- `POST /api/v1/register/execute`（登记）
- `POST /api/v1/vaccinate/execute`（接种）
- `POST /api/v1/observe/{appointmentId}/finish`（留观结束）
- `POST /api/v1/stock/transfer`（库存调拨）
- `POST /api/v1/stock/batches/{batchId}/dispose`（批次销毁）

**规则：** 同一 requestId 在 5 秒内重复请求将被拒绝（返回 code 1005 CONFLICT）。

---

## 15. 接口清单汇总

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| **公共** | POST | /public/sms/send | 发送验证码 |
| | POST | /public/auth/login | 登录 |
| | GET | /public/vaccines | 疫苗目录（公共） |
| | GET | /public/notices | 公告列表（公共） |
| | POST | /public/notices/{id}/feedback | 公告反馈 |
| **用户** | POST | /user/register | 注册 |
| | PUT | /user/password | 修改密码 |
| | POST | /user/password/reset | 重置密码 |
| | GET | /user/profile | 获取信息 |
| | PUT | /user/profile | 更新信息 |
| | POST | /user/logout | 登出 |
| **儿童** | GET | /user/children | 儿童列表 |
| | POST | /user/children | 添加儿童 |
| | PUT | /user/children/{id} | 修改儿童 |
| | DELETE | /user/children/{id} | 删除儿童 |
| **预约** | POST | /user/appointments | 创建预约 |
| | POST | /user/appointments/{id}/cancel | 取消预约 |
| | GET | /user/appointments/{id} | 预约详情 |
| | GET | /user/appointments | 预约列表 |
| | GET | /user/appointments/{id}/guide | 流程指引 |
| | GET | /user/appointments/{id}/queue | 排队信息 |
| **签到** | POST | /signin/execute | 执行签到 |
| | GET | /signin/today | 今日预约 |
| **预检** | GET | /precheck/queue | 预检队列 |
| | POST | /precheck/execute | 执行预检 |
| **登记** | GET | /register/queue | 登记队列 |
| | GET | /register/batches/{vaccineId} | 可用批次 |
| | POST | /register/execute | 执行登记 |
| | POST | /register/{id}/switch-batch | 切换批次 |
| **接种** | GET | /vaccinate/queue | 接种队列 |
| | GET | /vaccinate/{id}/verify | 核实信息 |
| | POST | /vaccinate/execute | 执行接种 |
| | GET | /vaccinate/records | 接种记录 |
| | GET | /vaccinate/records/child/{id} | 儿童记录 |
| **留观** | GET | /observe/queue | 留观队列 |
| | GET | /observe/{injectionId} | 留观状态 |
| | POST | /observe/{id}/finish | 确认结束 |
| | POST | /observe/adverse/report | 上报不良反应 |
| | POST | /observe/adverse/{id}/handle | 处理不良反应 |
| **库存** | GET | /stock/summary | 库存总览 |
| | GET | /stock/batches | 批次列表 |
| | GET | /stock/batches/{id} | 批次详情 |
| | POST | /stock/transfer | 库存调拨 |
| | GET | /stock/transfer/records | 调拨记录 |
| | POST | /stock/batches/{id}/dispose | 批次销毁 |
| | GET | /stock/alerts | 预警列表 |
| | PUT | /stock/alerts/{id}/handle | 处理预警 |
| **管理** | GET/POST | /admin/vaccines | 疫苗目录 |
| | PUT | /admin/vaccines/{id}/shelf-status | 上下架 |
| | GET/POST | /admin/roles | 角色管理 |
| | PUT/DELETE | /admin/roles/{id} | 角色操作 |
| | POST | /admin/roles/{id}/permissions | 分配权限 |
| | GET | /admin/users | 用户列表 |
| | POST | /admin/users/{id}/freeze | 冻结 |
| | POST | /admin/users/{id}/unfreeze | 解冻 |
| | POST | /admin/users/{id}/assign-roles | 分配角色 |
| | GET/POST | /admin/windows | 窗口管理 |
| | PUT | /admin/windows/{id} | 修改窗口 |
| | GET/POST | /admin/schedules | 排班管理 |
| | PUT/DELETE | /admin/schedules/{id} | 排班操作 |
| | GET/PUT | /admin/configs | 系统配置 |
| | GET/POST | /admin/notices | 公告管理 |
| | PUT | /admin/notices/{id} | 修改公告 |
| | POST | /admin/notices/{id}/audit | 审核公告 |
| | GET | /admin/stats/overview | 总览统计 |
| | GET | /admin/stats/vaccination | 接种统计 |

**共计：56 个接口端点**
