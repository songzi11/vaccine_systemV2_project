# 前后端联调实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 基于已通过评审的前后端联调设计方案，完成全部 57 个 API 的前端服务层搭建、Mock 数据构建、测试数据准备，并按 4 个阶段逐模块联调验证。

**Architecture:** 在前端共享基础设施计划（已完成 `utils/request.js`、`utils/auth.js`、`api/auth.js`）基础上，补全剩余 12 个 API 模块，搭建 Mock 服务层（`mock/`），通过环境变量切换 Mock/真实 API。联调按认证→核心流程→留观库存→排班管理的优先级逐阶段推进。

**Tech Stack:** uni-app (Vue 3), Pinia, uni-ui, utils/request.js（已有）

**前置计划：** 前端共享基础设施实施计划（17 个 Task，已完成项目骨架、HTTP 客户端、认证工具、登录页）

**后端参考：** 57 个 API 端点，JWT 认证，`ApiResponse<T>` 统一响应（code/message/data），Knife4j 文档（`http://localhost:8080/doc.html`）

---

## 文件结构

### 本计划创建/修改的文件

```
vaccine_systemV2app/
├── api/
│   ├── auth.js              # 修改：修正路径以匹配实际后端
│   ├── child.js             # 创建：儿童档案 API
│   ├── appointment.js       # 创建：预约管理 API
│   ├── precheck.js          # 创建：预检评估 API
│   ├── register.js          # 创建：登记排队 API
│   ├── vaccinate.js         # 创建：接种执行与记录 API
│   ├── observe.js           # 创建：留观与不良反应 API
│   ├── stock.js             # 创建：库存管理 API
│   ├── schedule.js          # 创建：排班管理 API
│   ├── admin-users.js       # 创建：用户管理 API
│   ├── admin-roles.js       # 创建：角色管理 API
│   ├── admin-notices.js     # 创建：公告管理 API
│   └── admin-windows.js     # 创建：窗口管理 API
├── mock/
│   ├── index.js             # 创建：Mock 开关与路由分发
│   ├── auth.js              # 创建：认证 Mock 数据
│   ├── child.js             # 创建：儿童档案 Mock 数据
│   ├── appointment.js       # 创建：预约 Mock 数据
│   ├── precheck.js          # 创建：预检 Mock 数据
│   ├── register.js          # 创建：登记 Mock 数据
│   ├── vaccinate.js         # 创建：接种 Mock 数据
│   ├── observe.js           # 创建：留观 Mock 数据
│   ├── stock.js             # 创建：库存 Mock 数据
│   ├── schedule.js          # 创建：排班 Mock 数据
│   ├── admin-users.js       # 创建：用户管理 Mock 数据
│   ├── admin-roles.js       # 创建：角色管理 Mock 数据
│   ├── admin-notices.js     # 创建：公告管理 Mock 数据
│   └── admin-windows.js     # 创建：窗口管理 Mock 数据
└── utils/
    └── request.js           # 修改：增加 Mock 拦截逻辑
```

后端项目新增文件：
```
vaccine_systemV2/
└── vaccine-start/src/main/resources/sql/
    └── integration_test_data.sql    # 创建：联调测试数据 SQL
```

---

## Task 1: 修正 api/auth.js 路径

**Files:**
- Modify: `api/auth.js`

现有 `api/auth.js` 中的部分路径与后端实际实现不一致，需对齐。

- [ ] **Step 1: 更新 api/auth.js**

```javascript
/**
 * 认证相关 API
 * 路径与后端实际 Controller 一致
 */
import { post, get } from '@/utils/request.js'

/** 发送短信验证码 */
export function sendSmsCode(phone) {
  return post('/api/v1/public/auth/sms-code', { phone }, { needToken: false })
}

/** 密码登录 */
export function loginByPassword(phone, password) {
  return post('/api/v1/public/auth/login', { phone, password }, { needToken: false })
}

/** 用户注册 */
export function register(data) {
  return post('/api/v1/public/auth/register', data, { needToken: false })
}

/** 获取当前用户信息 */
export function getUserInfo() {
  return get('/api/v1/user/auth/me')
}

/** 获取已发布公告列表 */
export function getUserNotices() {
  return get('/api/v1/user/notices')
}
```

**路径变更说明：**
| 原路径 | 修正后 | 原因 |
|--------|--------|------|
| `POST /api/v1/public/sms/send` | `POST /api/v1/public/auth/sms-code` | 后端实际 Controller 路径 |
| `POST /api/v1/user/register` | `POST /api/v1/public/auth/register` | 后端归入 public 路径 |
| `GET /api/v1/user/profile` | `GET /api/v1/user/auth/me` | 后端实际路径 |
| 删除 `loginBySmsCode` | — | 后端无短信登录端点 |
| 删除 `logout` | — | 后端无登出端点（JWT 无状态） |
| 删除 `changePassword` | — | 后端无此端点 |
| 删除 `resetPassword` | — | 后端无此端点 |
| 删除 `updateUserProfile` | — | 后端无此端点 |
| 新增 `getUserNotices` | `GET /api/v1/user/notices` | 联调阶段 1 需要 |

- [ ] **Step 2: Commit**

```bash
git add api/auth.js
git commit -m "fix: 修正认证API路径以匹配后端实际Controller"
```

---

## Task 2: 创建 api/child.js（儿童档案 API）

**Files:**
- Create: `api/child.js`

- [ ] **Step 1: 创建 api/child.js**

```javascript
/**
 * 儿童档案 API
 * 对应后端 ChildProfileController
 */
import { get, post, put, del } from '@/utils/request.js'

/** 添加儿童档案 */
export function addChild(data) {
  return post('/api/v1/user/children', data)
}

/** 更新儿童档案 */
export function updateChild(childId, data) {
  return put(`/api/v1/user/children/${childId}`, data)
}

/** 获取儿童详情 */
export function getChildDetail(childId) {
  return get(`/api/v1/user/children/${childId}`)
}

/** 获取当前用户的儿童列表 */
export function getChildList() {
  return get('/api/v1/user/children')
}

/** 删除儿童档案 */
export function deleteChild(childId) {
  return del(`/api/v1/user/children/${childId}`)
}
```

**请求/响应 DTO 参考：**

ChildCreateRequest（POST body）:
```
{ name: String(必填, max50), gender: Integer(必填, 0/1/2),
  birthDate: String(必填, yyyy-MM-dd), idCardType: Integer(可选, 1/2/3),
  idCardNo: String(可选, max18), nativePlace: String(可选, max100),
  nation: String(可选, max50), medicalHistory: String(可选, max500),
  allergyHistory: String(可选, max500) }
```

ChildResponse:
```
{ id: Long, parentId: Long, parentIdCard: String, name: String,
  gender: String, birthDate: String, idCardType: String, idCardNo: String,
  nativePlace: String, nation: String, medicalHistory: String,
  allergyHistory: String, createTime: String }
```

- [ ] **Step 2: Commit**

```bash
git add api/child.js
git commit -m "feat: 儿童档案API模块（增删改查）"
```

---

## Task 3: 创建 api/appointment.js（预约管理 API）

**Files:**
- Create: `api/appointment.js`

- [ ] **Step 1: 创建 api/appointment.js**

```javascript
/**
 * 预约管理 API
 * 对应后端 AppointmentController
 */
import { get, post, put } from '@/utils/request.js'

/** 创建预约 */
export function createAppointment(data) {
  return post('/api/v1/user/appointments', data)
}

/** 取消预约 */
export function cancelAppointment(id, data) {
  return put(`/api/v1/user/appointments/${id}/cancel`, data)
}

/** 获取当前用户的预约列表 */
export function getAppointmentList() {
  return get('/api/v1/user/appointments')
}

/** 获取预约详情 */
export function getAppointmentDetail(id) {
  return get(`/api/v1/user/appointments/${id}`)
}

/** 按日期查询预约（医生端） */
export function getAppointmentsByDate(date) {
  return get('/api/v1/user/appointments/by-date', { date })
}
```

**请求/响应 DTO 参考：**

AppointmentBookRequest（POST body）:
```
{ childId: Long(必填), vaccineId: Long(必填),
  appointmentDate: String(必填, yyyy-MM-dd), timeSlot: String(必填, "AM"/"PM") }
```

AppointmentCancelRequest（PUT body）:
```
{ reason: String(可选, max500) }
```

AppointmentResponse（列表项）:
```
{ id, appointmentNo, childId, vaccineId, appointmentDate, timeSlot, status, createTime }
```

AppointmentDetailResponse（详情）:
```
{ id, appointmentNo, userId, childId, vaccineId, appointmentDate, timeSlot, status,
  currentWindow, signinTime, cancelTime, cancelReason, batchId, createTime, updateTime }
```

- [ ] **Step 2: Commit**

```bash
git add api/appointment.js
git commit -m "feat: 预约管理API模块（创建/取消/列表/详情/按日期查询）"
```

---

## Task 4: 创建 api/precheck.js（预检评估 API）

**Files:**
- Create: `api/precheck.js`

- [ ] **Step 1: 创建 api/precheck.js**

```javascript
/**
 * 预检评估 API
 * 对应后端 PreCheckController
 */
import { get, post } from '@/utils/request.js'

/** 提交预检评估 */
export function submitPreCheck(data) {
  return post('/api/v1/precheck/assess', data)
}

/** 获取预检记录 */
export function getPreCheckRecord(appointmentId) {
  return get(`/api/v1/precheck/records/${appointmentId}`)
}
```

**请求/响应 DTO 参考：**

PreCheckAssessRequest（POST body）:
```
{ appointmentId: Long(必填), bodyTemperature: BigDecimal(必填),
  weight: BigDecimal(可选), height: BigDecimal(可选),
  healthStatus: String(可选, max200), allergyHistory: String(可选, max500),
  medicationRecent: String(可选, max500), diseaseHistory: String(可选, max500),
  vaccinationRecent: String(可选, max500), result: String(必填, "PASS"/"FAIL"),
  failReason: String(可选, max500) }
```

PreCheckRecordResponse:
```
{ id, appointmentId, checkTime, bodyTemperature, weight, height, healthStatus,
  allergyHistory, medicationRecent, diseaseHistory, vaccinationRecent,
  checkResult, failReason, doctorId, createTime }
```

- [ ] **Step 2: Commit**

```bash
git add api/precheck.js
git commit -m "feat: 预检评估API模块（提交评估/查询记录）"
```

---

## Task 5: 创建 api/register.js（登记排队 API）

**Files:**
- Create: `api/register.js`

- [ ] **Step 1: 创建 api/register.js**

```javascript
/**
 * 登记排队 API
 * 对应后端 RegisterController
 */
import { get, post, put } from '@/utils/request.js'

/** 登记排队 */
export function registerQueue(data) {
  return post('/api/v1/register/queue', data)
}

/** 核验登记 */
export function verifyRegister(id) {
  return put(`/api/v1/register/${id}/verify`)
}

/** 获取今日排队列表 */
export function getTodayQueue() {
  return get('/api/v1/register/today')
}
```

**请求/响应 DTO 参考：**

RegisterQueueRequest（POST body）:
```
{ appointmentId: Long(必填) }
```

RegisterQueueResponse:
```
{ id, appointmentId, registerTime, doctorId, queueNo, batchId, batchNo,
  verifyStatus, verifyTime, createTime }
```

- [ ] **Step 2: Commit**

```bash
git add api/register.js
git commit -m "feat: 登记排队API模块（登记/核验/今日列表）"
```

---

## Task 6: 创建 api/vaccinate.js（接种 API）

**Files:**
- Create: `api/vaccinate.js`

- [ ] **Step 1: 创建 api/vaccinate.js**

```javascript
/**
 * 接种执行与记录 API
 * 对应后端 VaccinateController
 */
import { get, post } from '@/utils/request.js'

/** 执行接种 */
export function executeVaccination(data) {
  return post('/api/v1/vaccinate/execute', data)
}

/** 获取当前用户的接种记录 */
export function getVaccinationRecords() {
  return get('/api/v1/user/vaccination-records')
}

/** 获取指定儿童的接种记录 */
export function getChildVaccinationRecords(childId) {
  return get(`/api/v1/user/children/${childId}/vaccination-records`)
}

/** FEFO 批次推荐（医生端） */
export function getFefoBatch(vaccineId) {
  return get(`/api/v1/vaccinate/fefo-batch/${vaccineId}`)
}
```

**请求/响应 DTO 参考：**

VaccinateExecuteRequest（POST body）:
```
{ appointmentId: Long(必填), injectionSite: String(必填, "LEFT_UPPER_ARM"/"RIGHT_UPPER_ARM"/"LEFT_BUTTOCK"/"RIGHT_BUTTOCK"),
  batchId: Long(必填) }
```

VaccinationRecordResponse:
```
{ id, appointmentId, injectionId, injectionTime, doctorId, injectionSite,
  batchId, batchNo, createTime }
```

FEFOBatchResponse:
```
{ batchId, batchNo, manufacturer, productionDate, expiryDate, status, availableStock }
```

- [ ] **Step 2: Commit**

```bash
git add api/vaccinate.js
git commit -m "feat: 接种API模块（执行接种/记录查询/FEFO推荐）"
```

---

## Task 7: 创建 api/observe.js（留观 API）

**Files:**
- Create: `api/observe.js`

- [ ] **Step 1: 创建 api/observe.js**

```javascript
/**
 * 留观与不良反应 API
 * 对应后端 ObserveController
 */
import { get, post, put } from '@/utils/request.js'

/** 开始留观 */
export function startObserve(data) {
  return post('/api/v1/observe/start', data)
}

/** 结束留观 */
export function finishObserve(id, data) {
  return put(`/api/v1/observe/${id}/finish`, data)
}

/** 上报不良反应 */
export function reportAdverseReaction(data) {
  return post('/api/v1/observe/adverse-reaction', data)
}

/** 处理不良反应 */
export function handleAdverseReaction(id, data) {
  return put(`/api/v1/observe/adverse-reaction/${id}/handle`, data)
}

/** 查询不良反应记录 */
export function getAdverseReactions(observeRecordId) {
  return get(`/api/v1/observe/adverse-reaction/${observeRecordId}`)
}
```

**请求/响应 DTO 参考：**

ObserveStartRequest（POST body）:
```
{ appointmentId: Long(必填), injectionId: String(必填) }
```

ObserveFinishRequest（PUT body）:
```
{ durationMinutes: Integer(必填, min1) }
```

AdverseReactionRequest（POST body）:
```
{ observeRecordId: Long(必填), appointmentId: Long(必填),
  reactionType: String(必填, max100), description: String(可选, max500),
  severity: String(必填, "MILD"/"MODERATE"/"SEVERE") }
```

AdverseReactionHandleRequest（PUT body）:
```
{ handleResult: String(必填, max500) }
```

ObserveRecordResponse:
```
{ id, appointmentId, injectionId, startTime, finishTime, duration,
  observeResult, doctorId, createTime }
```

AdverseReactionResponse:
```
{ id, observeRecordId, appointmentId, reactionType, description, severity,
  reportTime, handleTime, handleResult, handlerId, createTime }
```

- [ ] **Step 2: Commit**

```bash
git add api/observe.js
git commit -m "feat: 留观API模块（开始/结束留观/不良反应上报处理查询）"
```

---

## Task 8: 创建 api/stock.js（库存管理 API）

**Files:**
- Create: `api/stock.js`

- [ ] **Step 1: 创建 api/stock.js**

```javascript
/**
 * 库存管理 API
 * 对应后端 StockController
 */
import { get, post } from '@/utils/request.js'

/** 获取医院库存总览 */
export function getHospitalStock() {
  return get('/api/v1/stock/hospital')
}

/** 获取批次列表 */
export function getBatchList() {
  return get('/api/v1/stock/batches')
}

/** 创建批次（入库） */
export function createBatch(data) {
  return post('/api/v1/stock/batches', data)
}

/** 库存调拨 */
export function transferStock(data) {
  return post('/api/v1/stock/transfer', data)
}

/** 批次报损 */
export function disposeBatch(id, data) {
  return post(`/api/v1/stock/batches/${id}/dispose`, data)
}

/** 获取库存预警 */
export function getStockAlerts() {
  return get('/api/v1/stock/alerts')
}
```

**请求/响应 DTO 参考：**

BatchCreateRequest（POST body）:
```
{ vaccineId: Long(必填), batchNo: String(必填, max50),
  manufacturer: String(可选, max100), productionDate: String(可选, yyyy-MM-dd),
  expiryDate: String(必填, yyyy-MM-dd), warningDays: Integer(可选),
  quantity: Integer(必填) }
```

StockTransferRequest（POST body）:
```
{ batchId: Long(必填), fromType: Integer(必填, 0=仓库/1=接种点),
  fromId: Long(必填), toType: Integer(必填), toId: Long(必填),
  quantity: Integer(必填, min1) }
```

BatchDisposeRequest（POST body）:
```
{ batchId: Long(必填), quantity: Integer(必填, min1),
  reason: String(必填, max500), remark: String(可选, max500) }
```

HospitalStockResponse:
```
{ id, vaccineId, vaccineName, batchNo, availableStock, lockedStock,
  totalStock, status }
```

VaccineBatchResponse:
```
{ id, batchNo, vaccineId, manufacturer, productionDate, expiryDate,
  warningDays, status, createTime }
```

StockAlertResponse:
```
{ id, alertType, vaccineId, batchId, alertValue, expiryDate, handled, createTime }
```

- [ ] **Step 2: Commit**

```bash
git add api/stock.js
git commit -m "feat: 库存管理API模块（库存总览/批次CRUD/调拨/报损/预警）"
```

---

## Task 9: 创建 api/schedule.js（排班管理 API）

**Files:**
- Create: `api/schedule.js`

- [ ] **Step 1: 创建 api/schedule.js**

```javascript
/**
 * 排班管理 API
 * 对应后端 ScheduleController
 */
import { get, post, put, del } from '@/utils/request.js'

/** 创建排班 */
export function createSchedule(data) {
  return post('/api/v1/schedule', data)
}

/** 更新排班 */
export function updateSchedule(id, data) {
  return put(`/api/v1/schedule/${id}`, data)
}

/** 删除排班 */
export function deleteSchedule(id) {
  return del(`/api/v1/schedule/${id}`)
}

/** 获取排班详情 */
export function getScheduleDetail(id) {
  return get(`/api/v1/schedule/${id}`)
}

/** 按日期查询排班 */
export function getScheduleByDate(date) {
  return get('/api/v1/schedule/date', { date })
}
```

**请求/响应 DTO 参考：**

ScheduleCreateRequest（POST body）:
```
{ doctorId: Long(必填), windowId: Long(必填),
  scheduleDate: String(必填, yyyy-MM-dd), timeSlot: String(必填, "AM"/"PM"),
  maxCapacity: Integer(可选) }
```

ScheduleUpdateRequest（PUT body）:
```
{ status: Integer(可选), maxCapacity: Integer(可选), timeSlot: String(可选, "AM"/"PM") }
```

ScheduleResponse:
```
{ id, doctorId, windowId, scheduleDate, timeSlot, status, maxCapacity, createTime }
```

- [ ] **Step 2: Commit**

```bash
git add api/schedule.js
git commit -m "feat: 排班管理API模块（CRUD + 按日期查询）"
```

---

## Task 10: 创建 api/admin-users.js（用户管理 API）

**Files:**
- Create: `api/admin-users.js`

- [ ] **Step 1: 创建 api/admin-users.js**

```javascript
/**
 * 用户管理 API（管理员端）
 * 对应后端 UserAdminController
 */
import { get, put } from '@/utils/request.js'

/** 获取用户列表 */
export function getUserList() {
  return get('/api/v1/admin/users')
}

/** 冻结用户 */
export function freezeUser(id) {
  return put(`/api/v1/admin/users/${id}/freeze`)
}

/** 解冻用户 */
export function unfreezeUser(id) {
  return put(`/api/v1/admin/users/${id}/unfreeze`)
}
```

**响应 DTO 参考：**

UserResponse（列表项）:
```
{ userId, phone, realName, gender, status, createTime }
```

- [ ] **Step 2: Commit**

```bash
git add api/admin-users.js
git commit -m "feat: 用户管理API模块（列表/冻结/解冻）"
```

---

## Task 11: 创建 api/admin-roles.js（角色管理 API）

**Files:**
- Create: `api/admin-roles.js`

- [ ] **Step 1: 创建 api/admin-roles.js**

```javascript
/**
 * 角色管理 API（管理员端）
 * 对应后端 RoleAdminController
 */
import { get, post, put, del } from '@/utils/request.js'

/** 创建角色 */
export function createRole(data) {
  return post('/api/v1/admin/roles', data)
}

/** 更新角色 */
export function updateRole(id, data) {
  return put(`/api/v1/admin/roles/${id}`, data)
}

/** 删除角色 */
export function deleteRole(id) {
  return del(`/api/v1/admin/roles/${id}`)
}

/** 获取角色列表 */
export function getRoleList() {
  return get('/api/v1/admin/roles')
}
```

**请求/响应 DTO 参考：**

RoleCreateRequest（POST body）:
```
{ roleCode: String(必填, max50), roleName: String(必填, max50),
  roleGroup: String(可选, max50), description: String(可选, max200) }
```

RoleUpdateRequest（PUT body）:
```
{ roleName: String(可选, max50), roleGroup: String(可选, max50),
  description: String(可选, max200), status: Integer(可选) }
```

RoleResponse:
```
{ id, roleCode, roleName, roleGroup, description, status, isSystem, createTime }
```

- [ ] **Step 2: Commit**

```bash
git add api/admin-roles.js
git commit -m "feat: 角色管理API模块（CRUD）"
```

---

## Task 12: 创建 api/admin-notices.js（公告管理 API）

**Files:**
- Create: `api/admin-notices.js`

- [ ] **Step 1: 创建 api/admin-notices.js**

```javascript
/**
 * 公告管理 API（管理员端）
 * 对应后端 NoticeAdminController
 */
import { get, post, put, del } from '@/utils/request.js'

/** 创建公告 */
export function createNotice(data) {
  return post('/api/v1/admin/notices', data)
}

/** 更新公告 */
export function updateNotice(id, data) {
  return put(`/api/v1/admin/notices/${id}`, data)
}

/** 删除公告 */
export function deleteNotice(id) {
  return del(`/api/v1/admin/notices/${id}`)
}

/** 获取公告列表 */
export function getNoticeList() {
  return get('/api/v1/admin/notices')
}
```

**请求/响应 DTO 参考：**

NoticeCreateRequest（POST body）:
```
{ title: String(必填, max200), content: String(必填),
  noticeType: String(必填), startTime: String(可选, yyyy-MM-dd),
  endTime: String(可选, yyyy-MM-dd) }
```

NoticeUpdateRequest（PUT body）:
```
{ title: String(可选, max200), content: String(可选),
  noticeType: String(可选, max20), status: Integer(可选),
  startTime: String(可选, yyyy-MM-dd), endTime: String(可选, yyyy-MM-dd) }
```

NoticeResponse:
```
{ id, title, content, noticeType, status, authorId, startTime, endTime,
  publishTime, createTime }
```

- [ ] **Step 2: Commit**

```bash
git add api/admin-notices.js
git commit -m "feat: 公告管理API模块（CRUD）"
```

---

## Task 13: 创建 api/admin-windows.js（窗口管理 API）

**Files:**
- Create: `api/admin-windows.js`

- [ ] **Step 1: 创建 api/admin-windows.js**

```javascript
/**
 * 窗口管理 API（管理员端）
 * 对应后端 WindowAdminController
 */
import { get, post, put, del } from '@/utils/request.js'

/** 创建窗口 */
export function createWindow(data) {
  return post('/api/v1/admin/windows', data)
}

/** 更新窗口 */
export function updateWindow(id, data) {
  return put(`/api/v1/admin/windows/${id}`, data)
}

/** 删除窗口 */
export function deleteWindow(id) {
  return del(`/api/v1/admin/windows/${id}`)
}

/** 获取窗口列表 */
export function getWindowList() {
  return get('/api/v1/admin/windows')
}

/** 按功能类型筛选窗口 */
export function getWindowsByType(functionType) {
  return get('/api/v1/admin/windows/type', { functionType })
}
```

**请求/响应 DTO 参考：**

WindowCreateRequest（POST body）:
```
{ windowCode: String(必填, max30), windowName: String(必填, max50),
  windowFunctionType: String(必填), capacity: Integer(可选),
  avgHandleTime: Integer(可选), sortOrder: Integer(可选) }
```

WindowUpdateRequest（PUT body）:
```
{ windowName: String(可选, max50), windowFunctionType: String(可选, max30),
  capacity: Integer(可选), avgHandleTime: Integer(可选),
  sortOrder: Integer(可选), status: Integer(可选) }
```

WindowResponse:
```
{ id, windowCode, windowName, windowFunctionType, status, capacity,
  avgHandleTime, sortOrder, createTime }
```

- [ ] **Step 2: Commit**

```bash
git add api/admin-windows.js
git commit -m "feat: 窗口管理API模块（CRUD + 按类型筛选）"
```

---

## Task 14: 搭建 Mock 服务层

**Files:**
- Create: `mock/index.js`
- Create: `mock/auth.js`
- Create: `mock/child.js`
- Create: `mock/appointment.js`
- Create: `mock/precheck.js`
- Create: `mock/register.js`
- Create: `mock/vaccinate.js`
- Create: `mock/observe.js`
- Create: `mock/stock.js`
- Create: `mock/schedule.js`
- Create: `mock/admin-users.js`
- Create: `mock/admin-roles.js`
- Create: `mock/admin-notices.js`
- Create: `mock/admin-windows.js`
- Modify: `utils/request.js`

### Step 1: 创建 mock/index.js（Mock 开关与路由分发）

- [ ] **Step 1a: 创建 mock/index.js**

```javascript
/**
 * Mock 服务层
 * - 通过环境变量 VITE_USE_MOCK=true 开启
 * - 拦截 API 请求并返回本地 Mock 数据
 * - 所有 Mock 数据格式与后端 ApiResponse<T> 一致
 */

// Mock 开关（编译时常量，HBuilderX 或 CLI 可配置）
const USE_MOCK = false // 改为 true 启用 Mock

import * as authMock from './auth.js'
import * as childMock from './child.js'
import * as appointmentMock from './appointment.js'
import * as precheckMock from './precheck.js'
import * as registerMock from './register.js'
import * as vaccinateMock from './vaccinate.js'
import * as observeMock from './observe.js'
import * as stockMock from './stock.js'
import * as scheduleMock from './schedule.js'
import * as adminUsersMock from './admin-users.js'
import * as adminRolesMock from './admin-roles.js'
import * as adminNoticesMock from './admin-notices.js'
import * as adminWindowsMock from './admin-windows.js'

/**
 * Mock 路由表
 * key: method + url 正则
 * value: (url, data) => ApiResponse
 */
const mockRoutes = [
  // Auth
  { method: 'POST', pattern: /\/api\/v1\/public\/auth\/login$/, handler: authMock.login },
  { method: 'POST', pattern: /\/api\/v1\/public\/auth\/register$/, handler: authMock.register },
  { method: 'POST', pattern: /\/api\/v1\/public\/auth\/sms-code$/, handler: authMock.sendSmsCode },
  { method: 'GET',  pattern: /\/api\/v1\/user\/auth\/me$/, handler: authMock.getUserInfo },
  { method: 'GET',  pattern: /\/api\/v1\/user\/notices$/, handler: authMock.getNotices },

  // Children
  { method: 'GET',    pattern: /\/api\/v1\/user\/children$/, handler: childMock.list },
  { method: 'POST',   pattern: /\/api\/v1\/user\/children$/, handler: childMock.create },
  { method: 'PUT',    pattern: /\/api\/v1\/user\/children\/\d+$/, handler: childMock.update },
  { method: 'GET',    pattern: /\/api\/v1\/user\/children\/\d+$/, handler: childMock.detail },
  { method: 'DELETE', pattern: /\/api\/v1\/user\/children\/\d+$/, handler: childMock.remove },

  // Appointments
  { method: 'GET',  pattern: /\/api\/v1\/user\/appointments\/by-date/, handler: appointmentMock.listByDate },
  { method: 'GET',  pattern: /\/api\/v1\/user\/appointments\/\d+$/, handler: appointmentMock.detail },
  { method: 'PUT',  pattern: /\/api\/v1\/user\/appointments\/\d+\/cancel$/, handler: appointmentMock.cancel },
  { method: 'GET',  pattern: /\/api\/v1\/user\/appointments$/, handler: appointmentMock.list },
  { method: 'POST', pattern: /\/api\/v1\/user\/appointments$/, handler: appointmentMock.create },

  // PreCheck
  { method: 'GET',  pattern: /\/api\/v1\/precheck\/records\/\d+$/, handler: precheckMock.record },
  { method: 'POST', pattern: /\/api\/v1\/precheck\/assess$/, handler: precheckMock.assess },

  // Register
  { method: 'GET', pattern: /\/api\/v1\/register\/today$/, handler: registerMock.today },
  { method: 'PUT', pattern: /\/api\/v1\/register\/\d+\/verify$/, handler: registerMock.verify },
  { method: 'POST', pattern: /\/api\/v1\/register\/queue$/, handler: registerMock.queue },

  // Vaccinate
  { method: 'GET',  pattern: /\/api\/v1\/vaccinate\/fefo-batch\/\d+$/, handler: vaccinateMock.fefoBatch },
  { method: 'GET',  pattern: /\/api\/v1\/user\/children\/\d+\/vaccination-records$/, handler: vaccinateMock.childRecords },
  { method: 'GET',  pattern: /\/api\/v1\/user\/vaccination-records$/, handler: vaccinateMock.records },
  { method: 'POST', pattern: /\/api\/v1\/vaccinate\/execute$/, handler: vaccinateMock.execute },

  // Observe
  { method: 'GET',  pattern: /\/api\/v1\/observe\/adverse-reaction\/\d+$/, handler: observeMock.adverseList },
  { method: 'PUT',  pattern: /\/api\/v1\/observe\/adverse-reaction\/\d+\/handle$/, handler: observeMock.handleAdverse },
  { method: 'POST', pattern: /\/api\/v1\/observe\/adverse-reaction$/, handler: observeMock.reportAdverse },
  { method: 'PUT',  pattern: /\/api\/v1\/observe\/\d+\/finish$/, handler: observeMock.finish },
  { method: 'POST', pattern: /\/api\/v1\/observe\/start$/, handler: observeMock.start },

  // Stock
  { method: 'GET',  pattern: /\/api\/v1\/stock\/alerts$/, handler: stockMock.alerts },
  { method: 'POST', pattern: /\/api\/v1\/stock\/batches\/\d+\/dispose$/, handler: stockMock.dispose },
  { method: 'POST', pattern: /\/api\/v1\/stock\/transfer$/, handler: stockMock.transfer },
  { method: 'POST', pattern: /\/api\/v1\/stock\/batches$/, handler: stockMock.createBatch },
  { method: 'GET',  pattern: /\/api\/v1\/stock\/batches$/, handler: stockMock.batchList },
  { method: 'GET',  pattern: /\/api\/v1\/stock\/hospital$/, handler: stockMock.hospital },

  // Schedule
  { method: 'GET',    pattern: /\/api\/v1\/schedule\/date/, handler: scheduleMock.listByDate },
  { method: 'GET',    pattern: /\/api\/v1\/schedule\/\d+$/, handler: scheduleMock.detail },
  { method: 'DELETE', pattern: /\/api\/v1\/schedule\/\d+$/, handler: scheduleMock.remove },
  { method: 'PUT',    pattern: /\/api\/v1\/schedule\/\d+$/, handler: scheduleMock.update },
  { method: 'POST',   pattern: /\/api\/v1\/schedule$/, handler: scheduleMock.create },

  // Admin Users
  { method: 'PUT', pattern: /\/api\/v1\/admin\/users\/\d+\/unfreeze$/, handler: adminUsersMock.unfreeze },
  { method: 'PUT', pattern: /\/api\/v1\/admin\/users\/\d+\/freeze$/, handler: adminUsersMock.freeze },
  { method: 'GET', pattern: /\/api\/v1\/admin\/users$/, handler: adminUsersMock.list },

  // Admin Roles
  { method: 'DELETE', pattern: /\/api\/v1\/admin\/roles\/\d+$/, handler: adminRolesMock.remove },
  { method: 'GET',    pattern: /\/api\/v1\/admin\/roles$/, handler: adminRolesMock.list },
  { method: 'PUT',    pattern: /\/api\/v1\/admin\/roles\/\d+$/, handler: adminRolesMock.update },
  { method: 'POST',   pattern: /\/api\/v1\/admin\/roles$/, handler: adminRolesMock.create },

  // Admin Notices
  { method: 'DELETE', pattern: /\/api\/v1\/admin\/notices\/\d+$/, handler: adminNoticesMock.remove },
  { method: 'GET',    pattern: /\/api\/v1\/admin\/notices$/, handler: adminNoticesMock.list },
  { method: 'PUT',    pattern: /\/api\/v1\/admin\/notices\/\d+$/, handler: adminNoticesMock.update },
  { method: 'POST',   pattern: /\/api\/v1\/admin\/notices$/, handler: adminNoticesMock.create },

  // Admin Windows
  { method: 'GET',  pattern: /\/api\/v1\/admin\/windows\/type/, handler: adminWindowsMock.listByType },
  { method: 'DELETE', pattern: /\/api\/v1\/admin\/windows\/\d+$/, handler: adminWindowsMock.remove },
  { method: 'GET',    pattern: /\/api\/v1\/admin\/windows$/, handler: adminWindowsMock.list },
  { method: 'PUT',    pattern: /\/api\/v1\/admin\/windows\/\d+$/, handler: adminWindowsMock.update },
  { method: 'POST',   pattern: /\/api\/v1\/admin\/windows$/, handler: adminWindowsMock.create },
]

/**
 * 匹配 Mock 路由
 * @param {string} method - HTTP 方法
 * @param {string} url - 请求路径
 * @param {Object} data - 请求数据
 * @returns {Object|null} Mock 响应或 null
 */
export function matchMock(method, url, data) {
  if (!USE_MOCK) return null

  for (const route of mockRoutes) {
    if (route.method === method && route.pattern.test(url)) {
      return route.handler(url, data)
    }
  }

  console.warn(`[Mock] 未匹配路由: ${method} ${url}`)
  return { code: 404, message: 'Mock: 未找到匹配路由', data: null, timestamp: Date.now() }
}

export { USE_MOCK }
```

### Step 2: 创建各模块 Mock 数据文件

以下每个 handler 返回 `ApiResponse<T>` 格式：`{ code: 200, message: 'success', data: ..., timestamp: ... }`

- [ ] **Step 2a: 创建 mock/auth.js**

```javascript
const MOCK_TOKEN = 'mock_jwt_token_for_testing_1234567890'

export function login(url, data) {
  return {
    code: 200, message: 'success', timestamp: Date.now(),
    data: {
      token: MOCK_TOKEN,
      userId: 1,
      phone: data.phone || '13800138001',
      realName: '测试用户',
      roles: ['USER']
    }
  }
}

export function register(url, data) {
  return {
    code: 200, message: 'success', timestamp: Date.now(),
    data: {
      token: MOCK_TOKEN,
      userId: 2,
      phone: data.phone,
      realName: data.realName,
      roles: ['USER']
    }
  }
}

export function sendSmsCode(url, data) {
  return { code: 200, message: 'success', data: '验证码已发送', timestamp: Date.now() }
}

export function getUserInfo(url, data) {
  return {
    code: 200, message: 'success', timestamp: Date.now(),
    data: {
      userId: 1, phone: '13800138001', realName: '测试用户',
      gender: 'MALE', idCardType: 'ID_CARD', idCardNo: '120101199001011234',
      status: 'NORMAL', createTime: '2026-01-15 10:00:00',
      roles: ['USER']
    }
  }
}

export function getNotices(url, data) {
  return {
    code: 200, message: 'success', timestamp: Date.now(),
    data: [
      { id: 1, title: '春季疫苗接种通知', content: '春季疫苗接种已开始...', noticeType: 'NORMAL',
        status: 'PUBLISHED', authorId: 1, startTime: '2026-03-01', endTime: '2026-06-30',
        publishTime: '2026-03-01 08:00:00', createTime: '2026-02-28 16:00:00' }
    ]
  }
}
```

- [ ] **Step 2b: 创建 mock/child.js**

```javascript
let mockChildren = [
  { id: 1, parentId: 1, parentIdCard: '120101199001011234', name: '小明',
    gender: 'MALE', birthDate: '2023-06-15', idCardType: 'ID_CARD',
    idCardNo: '120101202306150012', nativePlace: '天津市', nation: '汉',
    medicalHistory: '无', allergyHistory: '无', createTime: '2026-01-20 10:00:00' },
  { id: 2, parentId: 1, parentIdCard: '120101199001011234', name: '小红',
    gender: 'FEMALE', birthDate: '2024-03-10', idCardType: 'ID_CARD',
    idCardNo: '120101202403100025', nativePlace: '天津市', nation: '汉',
    medicalHistory: '无', allergyHistory: '鸡蛋过敏', createTime: '2026-02-01 14:00:00' }
]

export function list(url, data) {
  return { code: 200, message: 'success', data: mockChildren, timestamp: Date.now() }
}

export function create(url, data) {
  const newChild = { id: Date.now(), parentId: 1, parentIdCard: '120101199001011234', ...data, createTime: new Date().toISOString().slice(0, 19).replace('T', ' ') }
  mockChildren.push(newChild)
  return { code: 200, message: 'success', data: newChild, timestamp: Date.now() }
}

export function detail(url, data) {
  const id = parseInt(url.match(/\/(\d+)$/)[1])
  const child = mockChildren.find(c => c.id === id)
  return child
    ? { code: 200, message: 'success', data: child, timestamp: Date.now() }
    : { code: 1004, message: '儿童不存在', timestamp: Date.now() }
}

export function update(url, data) {
  const id = parseInt(url.match(/\/children\/(\d+)/)[1])
  const child = mockChildren.find(c => c.id === id)
  if (child) Object.assign(child, data)
  return child
    ? { code: 200, message: 'success', data: child, timestamp: Date.now() }
    : { code: 1004, message: '儿童不存在', timestamp: Date.now() }
}

export function remove(url, data) {
  const id = parseInt(url.match(/\/(\d+)$/)[1])
  mockChildren = mockChildren.filter(c => c.id !== id)
  return { code: 200, message: 'success', data: null, timestamp: Date.now() }
}
```

- [ ] **Step 2c: 创建 mock/appointment.js**

```javascript
let mockAppointments = [
  { id: 1, appointmentNo: 'APT20260404001', childId: 1, vaccineId: 1,
    appointmentDate: '2026-04-10', timeSlot: 'AM', status: 'APPOINTED',
    createTime: '2026-04-04 09:00:00' }
]

export function list(url, data) {
  return { code: 200, message: 'success', data: mockAppointments, timestamp: Date.now() }
}

export function create(url, data) {
  const newAppt = { id: Date.now(), appointmentNo: `APT${Date.now()}`, ...data, status: 'APPOINTED', createTime: new Date().toISOString().slice(0, 19).replace('T', ' ') }
  mockAppointments.push(newAppt)
  return { code: 200, message: 'success', data: newAppt, timestamp: Date.now() }
}

export function detail(url, data) {
  const id = parseInt(url.match(/\/appointments\/(\d+)/)[1])
  const appt = mockAppointments.find(a => a.id === id)
  return appt
    ? { code: 200, message: 'success', data: { ...appt, userId: 1, currentWindow: '1号窗口' }, timestamp: Date.now() }
    : { code: 1004, message: '预约不存在', timestamp: Date.now() }
}

export function cancel(url, data) {
  const id = parseInt(url.match(/\/appointments\/(\d+)/)[1])
  const appt = mockAppointments.find(a => a.id === id)
  if (appt) appt.status = 'CANCELLED'
  return { code: 200, message: 'success', data: null, timestamp: Date.now() }
}

export function listByDate(url, data) {
  return { code: 200, message: 'success', data: mockAppointments, timestamp: Date.now() }
}
```

- [ ] **Step 2d: 创建 mock/precheck.js**

```javascript
export function assess(url, data) {
  return {
    code: 200, message: 'success', timestamp: Date.now(),
    data: {
      id: Date.now(), appointmentId: data.appointmentId, checkTime: '2026-04-10 08:30:00',
      bodyTemperature: data.bodyTemperature, weight: data.weight, height: data.height,
      healthStatus: data.healthStatus, allergyHistory: data.allergyHistory,
      medicationRecent: data.medicationRecent, diseaseHistory: data.diseaseHistory,
      vaccinationRecent: data.vaccinationRecent, checkResult: data.result,
      failReason: data.failReason, doctorId: 10, createTime: '2026-04-10 08:30:00'
    }
  }
}

export function record(url, data) {
  return {
    code: 200, message: 'success', timestamp: Date.now(),
    data: {
      id: 1, appointmentId: 1, checkTime: '2026-04-10 08:30:00',
      bodyTemperature: 36.5, weight: 12.0, height: 85.0,
      healthStatus: '良好', allergyHistory: '无', medicationRecent: '无',
      diseaseHistory: '无', vaccinationRecent: '无', checkResult: 'PASS',
      failReason: null, doctorId: 10, createTime: '2026-04-10 08:30:00'
    }
  }
}
```

- [ ] **Step 2e: 创建 mock/register.js**

```javascript
export function queue(url, data) {
  return {
    code: 200, message: 'success', timestamp: Date.now(),
    data: { id: Date.now(), appointmentId: data.appointmentId, registerTime: '2026-04-10 08:35:00',
      doctorId: 10, queueNo: 'A001', batchId: 1, batchNo: 'BATCH-001',
      verifyStatus: 'UNVERIFIED', verifyTime: null, createTime: '2026-04-10 08:35:00' }
  }
}

export function verify(url, data) {
  return {
    code: 200, message: 'success', timestamp: Date.now(),
    data: { id: 1, appointmentId: 1, registerTime: '2026-04-10 08:35:00',
      doctorId: 10, queueNo: 'A001', batchId: 1, batchNo: 'BATCH-001',
      verifyStatus: 'VERIFIED', verifyTime: '2026-04-10 08:36:00', createTime: '2026-04-10 08:35:00' }
  }
}

export function today(url, data) {
  return {
    code: 200, message: 'success', timestamp: Date.now(),
    data: [
      { id: 1, appointmentId: 1, registerTime: '2026-04-10 08:35:00',
        doctorId: 10, queueNo: 'A001', batchId: 1, batchNo: 'BATCH-001',
        verifyStatus: 'VERIFIED', verifyTime: '2026-04-10 08:36:00', createTime: '2026-04-10 08:35:00' }
    ]
  }
}
```

- [ ] **Step 2f: 创建 mock/vaccinate.js**

```javascript
export function execute(url, data) {
  return {
    code: 200, message: 'success', timestamp: Date.now(),
    data: { id: Date.now(), appointmentId: data.appointmentId, injectionId: 'INJ' + Date.now(),
      injectionTime: '2026-04-10 09:00:00', doctorId: 10, injectionSite: data.injectionSite,
      batchId: data.batchId, batchNo: 'BATCH-001', createTime: '2026-04-10 09:00:00' }
  }
}

export function records(url, data) {
  return {
    code: 200, message: 'success', timestamp: Date.now(),
    data: [
      { id: 1, appointmentId: 1, injectionId: 'INJ001', injectionTime: '2026-04-10 09:00:00',
        doctorId: 10, injectionSite: 'LEFT_UPPER_ARM', batchId: 1, batchNo: 'BATCH-001',
        createTime: '2026-04-10 09:00:00' }
    ]
  }
}

export function childRecords(url, data) {
  return { code: 200, message: 'success', data: [], timestamp: Date.now() }
}

export function fefoBatch(url, data) {
  return {
    code: 200, message: 'success', timestamp: Date.now(),
    data: { batchId: 1, batchNo: 'BATCH-001', manufacturer: '国药集团',
      productionDate: '2025-12-01', expiryDate: '2027-06-01', status: 'NORMAL', availableStock: 50 }
  }
}
```

- [ ] **Step 2g: 创建 mock/observe.js**

```javascript
export function start(url, data) {
  return {
    code: 200, message: 'success', timestamp: Date.now(),
    data: { id: Date.now(), appointmentId: data.appointmentId, injectionId: data.injectionId,
      startTime: '2026-04-10 09:05:00', finishTime: null, duration: null,
      observeResult: null, doctorId: 10, createTime: '2026-04-10 09:05:00' }
  }
}

export function finish(url, data) {
  return {
    code: 200, message: 'success', timestamp: Date.now(),
    data: { id: 1, appointmentId: 1, injectionId: 'INJ001', startTime: '2026-04-10 09:05:00',
      finishTime: '2026-04-10 09:35:00', duration: data.durationMinutes, observeResult: 'NORMAL',
      doctorId: 10, createTime: '2026-04-10 09:05:00' }
  }
}

export function reportAdverse(url, data) {
  return {
    code: 200, message: 'success', timestamp: Date.now(),
    data: { id: Date.now(), observeRecordId: data.observeRecordId, appointmentId: data.appointmentId,
      reactionType: data.reactionType, description: data.description, severity: data.severity,
      reportTime: '2026-04-10 09:15:00', handleTime: null, handleResult: null,
      handlerId: null, createTime: '2026-04-10 09:15:00' }
  }
}

export function handleAdverse(url, data) {
  return {
    code: 200, message: 'success', timestamp: Date.now(),
    data: { id: 1, observeRecordId: 1, appointmentId: 1, reactionType: '局部红肿',
      description: '接种部位轻微红肿', severity: 'MILD', reportTime: '2026-04-10 09:15:00',
      handleTime: '2026-04-10 09:20:00', handleResult: data.handleResult, handlerId: 10,
      createTime: '2026-04-10 09:15:00' }
  }
}

export function adverseList(url, data) {
  return { code: 200, message: 'success', data: [], timestamp: Date.now() }
}
```

- [ ] **Step 2h: 创建 mock/stock.js**

```javascript
export function hospital(url, data) {
  return {
    code: 200, message: 'success', timestamp: Date.now(),
    data: [
      { id: 1, vaccineId: 1, vaccineName: '乙肝疫苗', batchNo: 'BATCH-001',
        availableStock: 50, lockedStock: 5, totalStock: 55, status: 'NORMAL' },
      { id: 2, vaccineId: 2, vaccineName: '卡介苗', batchNo: 'BATCH-002',
        availableStock: 30, lockedStock: 2, totalStock: 32, status: 'NORMAL' }
    ]
  }
}

export function batchList(url, data) {
  return {
    code: 200, message: 'success', timestamp: Date.now(),
    data: [
      { id: 1, batchNo: 'BATCH-001', vaccineId: 1, manufacturer: '国药集团',
        productionDate: '2025-12-01', expiryDate: '2027-06-01', warningDays: 90,
        status: 'NORMAL', createTime: '2026-01-10 10:00:00' }
    ]
  }
}

export function createBatch(url, data) {
  return {
    code: 200, message: 'success', timestamp: Date.now(),
    data: { id: Date.now(), batchNo: data.batchNo, vaccineId: data.vaccineId,
      manufacturer: data.manufacturer, productionDate: data.productionDate,
      expiryDate: data.expiryDate, warningDays: data.warningDays,
      status: 'NORMAL', createTime: new Date().toISOString().slice(0, 19).replace('T', ' ') }
  }
}

export function transfer(url, data) {
  return { code: 200, message: 'success', data: null, timestamp: Date.now() }
}

export function dispose(url, data) {
  return { code: 200, message: 'success', data: null, timestamp: Date.now() }
}

export function alerts(url, data) {
  return {
    code: 200, message: 'success', timestamp: Date.now(),
    data: [
      { id: 1, alertType: 'LOW_STOCK', vaccineId: 3, batchId: 3,
        alertValue: 5, expiryDate: '2027-03-01', handled: false, createTime: '2026-04-01 08:00:00' }
    ]
  }
}
```

- [ ] **Step 2i: 创建 mock/schedule.js**

```javascript
export function create(url, data) {
  return {
    code: 200, message: 'success', timestamp: Date.now(),
    data: { id: Date.now(), doctorId: data.doctorId, windowId: data.windowId,
      scheduleDate: data.scheduleDate, timeSlot: data.timeSlot, status: 'NORMAL',
      maxCapacity: data.maxCapacity || 30, createTime: new Date().toISOString().slice(0, 19).replace('T', ' ') }
  }
}

export function update(url, data) {
  return {
    code: 200, message: 'success', timestamp: Date.now(),
    data: { id: 1, doctorId: 10, windowId: 1, scheduleDate: '2026-04-10',
      timeSlot: 'AM', status: 'NORMAL', maxCapacity: 30, createTime: '2026-04-01 10:00:00' }
  }
}

export function remove(url, data) {
  return { code: 200, message: 'success', data: null, timestamp: Date.now() }
}

export function detail(url, data) {
  return {
    code: 200, message: 'success', timestamp: Date.now(),
    data: { id: 1, doctorId: 10, windowId: 1, scheduleDate: '2026-04-10',
      timeSlot: 'AM', status: 'NORMAL', maxCapacity: 30, createTime: '2026-04-01 10:00:00' }
  }
}

export function listByDate(url, data) {
  return {
    code: 200, message: 'success', timestamp: Date.now(),
    data: [
      { id: 1, doctorId: 10, windowId: 1, scheduleDate: '2026-04-10',
        timeSlot: 'AM', status: 'NORMAL', maxCapacity: 30, createTime: '2026-04-01 10:00:00' }
    ]
  }
}
```

- [ ] **Step 2j: 创建 mock/admin-users.js**

```javascript
export function list(url, data) {
  return {
    code: 200, message: 'success', timestamp: Date.now(),
    data: [
      { userId: 1, phone: '13800138001', realName: '张三', gender: 'MALE', status: 'NORMAL', createTime: '2026-01-15 10:00:00' },
      { userId: 2, phone: '13800138002', realName: '李医生', gender: 'FEMALE', status: 'NORMAL', createTime: '2026-01-16 10:00:00' }
    ]
  }
}

export function freeze(url, data) {
  return { code: 200, message: 'success', data: null, timestamp: Date.now() }
}

export function unfreeze(url, data) {
  return { code: 200, message: 'success', data: null, timestamp: Date.now() }
}
```

- [ ] **Step 2k: 创建 mock/admin-roles.js**

```javascript
export function create(url, data) {
  return {
    code: 200, message: 'success', timestamp: Date.now(),
    data: { id: Date.now(), roleCode: data.roleCode, roleName: data.roleName,
      roleGroup: data.roleGroup, description: data.description, status: 'NORMAL',
      isSystem: false, createTime: new Date().toISOString().slice(0, 19).replace('T', ' ') }
  }
}

export function update(url, data) {
  return { code: 200, message: 'success', data: null, timestamp: Date.now() }
}

export function remove(url, data) {
  return { code: 200, message: 'success', data: null, timestamp: Date.now() }
}

export function list(url, data) {
  return {
    code: 200, message: 'success', timestamp: Date.now(),
    data: [
      { id: 1, roleCode: 'USER', roleName: '普通用户', roleGroup: 'USER', description: '家长端用户', status: 'NORMAL', isSystem: true, createTime: '2026-01-01 00:00:00' },
      { id: 2, roleCode: 'SUPER_ADMIN', roleName: '超级管理员', roleGroup: 'ADMIN', description: '系统管理员', status: 'NORMAL', isSystem: true, createTime: '2026-01-01 00:00:00' }
    ]
  }
}
```

- [ ] **Step 2l: 创建 mock/admin-notices.js**

```javascript
export function create(url, data) {
  return {
    code: 200, message: 'success', timestamp: Date.now(),
    data: { id: Date.now(), title: data.title, content: data.content,
      noticeType: data.noticeType, status: 'DRAFT', authorId: 1,
      startTime: data.startTime, endTime: data.endTime, publishTime: null,
      createTime: new Date().toISOString().slice(0, 19).replace('T', ' ') }
  }
}

export function update(url, data) {
  return { code: 200, message: 'success', data: null, timestamp: Date.now() }
}

export function remove(url, data) {
  return { code: 200, message: 'success', data: null, timestamp: Date.now() }
}

export function list(url, data) {
  return {
    code: 200, message: 'success', timestamp: Date.now(),
    data: [
      { id: 1, title: '春季接种通知', content: '...', noticeType: 'NORMAL',
        status: 'PUBLISHED', authorId: 1, startTime: '2026-03-01', endTime: '2026-06-30',
        publishTime: '2026-03-01 08:00:00', createTime: '2026-02-28 16:00:00' }
    ]
  }
}
```

- [ ] **Step 2m: 创建 mock/admin-windows.js**

```javascript
export function create(url, data) {
  return {
    code: 200, message: 'success', timestamp: Date.now(),
    data: { id: Date.now(), windowCode: data.windowCode, windowName: data.windowName,
      windowFunctionType: data.windowFunctionType, status: 'ENABLED',
      capacity: data.capacity, avgHandleTime: data.avgHandleTime, sortOrder: data.sortOrder,
      createTime: new Date().toISOString().slice(0, 19).replace('T', ' ') }
  }
}

export function update(url, data) {
  return { code: 200, message: 'success', data: null, timestamp: Date.now() }
}

export function remove(url, data) {
  return { code: 200, message: 'success', data: null, timestamp: Date.now() }
}

export function list(url, data) {
  return {
    code: 200, message: 'success', timestamp: Date.now(),
    data: [
      { id: 1, windowCode: 'WIN001', windowName: '1号签到窗口', windowFunctionType: 'SIGNIN',
        status: 'ENABLED', capacity: 10, avgHandleTime: 3, sortOrder: 1, createTime: '2026-01-01 00:00:00' }
    ]
  }
}

export function listByType(url, data) {
  return {
    code: 200, message: 'success', timestamp: Date.now(),
    data: [
      { id: 1, windowCode: 'WIN001', windowName: '1号签到窗口', windowFunctionType: 'SIGNIN',
        status: 'ENABLED', capacity: 10, avgHandleTime: 3, sortOrder: 1, createTime: '2026-01-01 00:00:00' }
    ]
  }
}
```

### Step 3: 修改 utils/request.js 增加 Mock 拦截

- [ ] **Step 3a: 在 request.js 顶部导入 Mock**

在 `utils/request.js` 文件的 `import { getToken, removeToken } from './auth.js'` 之后添加：

```javascript
import { matchMock } from '@/mock/index.js'
```

- [ ] **Step 3b: 在 request() 函数体开头添加 Mock 拦截**

在 `function request(options) {` 函数体中，`const { url, method, ... } = options` 之后，`if (show) showLoading()` 之前插入：

```javascript
  // Mock 拦截
  const mockResult = matchMock(method, url, data)
  if (mockResult) {
    if (show) hideLoading()
    if (mockResult.code === 200) {
      resolve(mockResult.data !== undefined ? mockResult.data : mockResult)
    } else {
      handleBusinessError(mockResult.code, mockResult.message)
      reject(new Error(mockResult.message || 'Mock 请求失败'))
    }
    return
  }
```

- [ ] **Step 4: Commit**

```bash
git add mock/ utils/request.js
git commit -m "feat: Mock服务层（14个模块Mock数据 + Mock路由分发 + request拦截）"
```

---

## Task 15: 创建联调测试数据 SQL

**Files:**
- Create: `vaccine-start/src/main/resources/sql/integration_test_data.sql`

- [ ] **Step 1: 创建测试数据 SQL**

在 `vaccine_systemV2/vaccine-start/src/main/resources/sql/integration_test_data.sql` 中写入：

```sql
-- =====================================================
-- 疫苗管理系统 V2 — 联调测试数据
-- 在 vaccine_db_init.sql 种子数据基础上补充
-- =====================================================

USE vaccine_db;

-- ---------------------------------------------------
-- 1. 测试用户（各角色各一个，密码统一为 123456）
-- ---------------------------------------------------
-- 家长用户
INSERT INTO sys_user (phone, password, real_name, gender, status, create_time)
VALUES ('13800000001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '家长张三', 1, 0, NOW())
ON DUPLICATE KEY UPDATE phone = phone;

INSERT INTO sys_user (phone, password, real_name, gender, status, create_time)
VALUES ('13800000002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '家长李四', 2, 0, NOW())
ON DUPLICATE KEY UPDATE phone = phone;

-- 预检医生
INSERT INTO sys_user (phone, password, real_name, gender, status, create_time)
VALUES ('13900000001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '王医生(预检)', 1, 0, NOW())
ON DUPLICATE KEY UPDATE phone = phone;

-- 登记医生
INSERT INTO sys_user (phone, password, real_name, gender, status, create_time)
VALUES ('13900000002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '赵医生(登记)', 1, 0, NOW())
ON DUPLICATE KEY UPDATE phone = phone;

-- 接种医生
INSERT INTO sys_user (phone, password, real_name, gender, status, create_time)
VALUES ('13900000003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '钱医生(接种)', 1, 0, NOW())
ON DUPLICATE KEY UPDATE phone = phone;

-- 留观医生
INSERT INTO sys_user (phone, password, real_name, gender, status, create_time)
VALUES ('13900000004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '孙医生(留观)', 2, 0, NOW())
ON DUPLICATE KEY UPDATE phone = phone;

-- 库管员
INSERT INTO sys_user (phone, password, real_name, gender, status, create_time)
VALUES ('13900000005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '周医生(库管)', 1, 0, NOW())
ON DUPLICATE KEY UPDATE phone = phone;

-- 超级管理员
INSERT INTO sys_user (phone, password, real_name, gender, status, create_time)
VALUES ('13700000001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '管理员Admin', 1, 0, NOW())
ON DUPLICATE KEY UPDATE phone = phone;

-- ---------------------------------------------------
-- 2. 分配角色（需要查出用户ID后手动替换）
--    以下使用子查询动态获取
-- ---------------------------------------------------
-- 家长角色
INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r
WHERE u.phone = '13800000001' AND r.role_code = 'USER';

INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r
WHERE u.phone = '13800000002' AND r.role_code = 'USER';

-- 预检医生
INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r
WHERE u.phone = '13900000001' AND r.role_code = 'DOCTOR_PRECHECK';

-- 登记医生
INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r
WHERE u.phone = '13900000002' AND r.role_code = 'DOCTOR_REGISTER';

-- 接种医生
INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r
WHERE u.phone = '13900000003' AND r.role_code = 'DOCTOR_VACCINATE';

-- 留观医生
INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r
WHERE u.phone = '13900000004' AND r.role_code = 'DOCTOR_OBSERVE';

-- 库管员
INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r
WHERE u.phone = '13900000005' AND r.role_code = 'DOCTOR_STOCK';

-- 超级管理员
INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r
WHERE u.phone = '13700000001' AND r.role_code = 'SUPER_ADMIN';

-- ---------------------------------------------------
-- 3. 儿童档案（归属家长张三）
-- ---------------------------------------------------
INSERT INTO child_profile (parent_id, name, gender, birth_date, id_card_type, id_card_no, native_place, nation, medical_history, allergy_history, create_time)
SELECT id, '小明', 1, '2023-06-15', 1, '120101202306150012', '天津市', '汉', '无', '无', NOW()
FROM sys_user WHERE phone = '13800000001'
ON DUPLICATE KEY UPDATE name = name;

INSERT INTO child_profile (parent_id, name, gender, birth_date, id_card_type, id_card_no, native_place, nation, medical_history, allergy_history, create_time)
SELECT id, '小红', 2, '2024-03-10', 1, '120101202403100025', '天津市', '汉', '无', '鸡蛋过敏', NOW()
FROM sys_user WHERE phone = '13800000001'
ON DUPLICATE KEY UPDATE name = name;

-- ---------------------------------------------------
-- 4. 疫苗库存补充
-- ---------------------------------------------------
INSERT INTO vaccine_batch (vaccine_id, batch_no, manufacturer, production_date, expiry_date, warning_days, quantity, status, create_time)
SELECT id, 'TEST-BATCH-001', '国药集团', '2025-12-01', '2027-06-01', 90, 100, 0, NOW()
FROM vaccine WHERE name = '乙肝疫苗' LIMIT 1
ON DUPLICATE KEY UPDATE batch_no = batch_no;

INSERT INTO vaccine_batch (vaccine_id, batch_no, manufacturer, production_date, expiry_date, warning_days, quantity, status, create_time)
SELECT id, 'TEST-BATCH-002', '武汉生物', '2025-10-15', '2027-04-15', 90, 80, 0, NOW()
FROM vaccine WHERE name = '卡介苗' LIMIT 1
ON DUPLICATE KEY UPDATE batch_no = batch_no;

INSERT INTO vaccine_batch (vaccine_id, batch_no, manufacturer, production_date, expiry_date, warning_days, quantity, status, create_time)
SELECT id, 'TEST-BATCH-003', '北京科兴', '2026-01-10', '2027-07-10', 90, 60, 0, NOW()
FROM vaccine WHERE name LIKE '%脊灰%' LIMIT 1
ON DUPLICATE KEY UPDATE batch_no = batch_no;

-- ---------------------------------------------------
-- 5. 测试公告
-- ---------------------------------------------------
INSERT INTO system_notice (title, content, notice_type, status, author_id, start_time, end_time, publish_time, create_time)
SELECT '春季疫苗接种通知', '各位家长，春季疫苗接种已开始，请及时预约。', 'NORMAL', 1,
  (SELECT id FROM sys_user WHERE phone = '13700000001' LIMIT 1),
  '2026-03-01', '2026-06-30', NOW(), NOW()
ON DUPLICATE KEY UPDATE title = title;

INSERT INTO system_notice (title, content, notice_type, status, author_id, start_time, end_time, publish_time, create_time)
SELECT '五一假期接种安排', '五一假期期间正常接种，请合理安排时间。', 'NORMAL', 1,
  (SELECT id FROM sys_user WHERE phone = '13700000001' LIMIT 1),
  '2026-04-25', '2026-05-10', NOW(), NOW()
ON DUPLICATE KEY UPDATE title = title;

-- ---------------------------------------------------
-- 测试账号速查表
-- ---------------------------------------------------
-- | 手机号       | 密码   | 角色           |
-- |-------------|--------|----------------|
-- | 13800000001 | 123456 | 家长 USER      |
-- | 13800000002 | 123456 | 家长 USER      |
-- | 13900000001 | 123456 | 预检医生       |
-- | 13900000002 | 123456 | 登记医生       |
-- | 13900000003 | 123456 | 接种医生       |
-- | 13900000004 | 123456 | 留观医生       |
-- | 13900000005 | 123456 | 库管员         |
-- | 13700000001 | 123456 | 超级管理员     |
```

**注意：** 上面的 BCrypt hash 是示例占位值，实际使用前需要通过后端注册接口或 Java BCryptPasswordEncoder 生成正确的 `123456` 对应 hash。

- [ ] **Step 2: Commit**

```bash
git add vaccine-start/src/main/resources/sql/integration_test_data.sql
git commit -m "feat: 联调测试数据SQL（8个角色账号/2个儿童/3个批次/2条公告）"
```

---

## Task 16: 阶段 1 联调 — 认证 + 用户基础

**前置条件：** 后端启动（`mvn spring-boot:run`），数据库执行 `vaccine_db_init.sql` + `integration_test_data.sql`

**本阶段覆盖 API（7 个）：**
1. `POST /api/v1/public/auth/register`
2. `POST /api/v1/public/auth/login`
3. `POST /api/v1/public/auth/sms-code`
4. `GET /api/v1/user/auth/me`
5. `GET /api/v1/user/notices`
6. `POST /api/v1/user/children`
7. `GET /api/v1/user/children`

- [ ] **Step 1: 启动后端并验证 Knife4j 可访问**

打开浏览器访问 `http://localhost:8080/doc.html`，确认所有接口文档正常显示。

- [ ] **Step 2: 关闭 Mock（`mock/index.js` 中 `USE_MOCK = false`）**

- [ ] **Step 3: 逐个 API 联调验证**

对每个 API 执行以下检查：

**3a. POST /api/v1/public/auth/register — 注册**

- [ ] 请求方法 POST、路径正确
- [ ] 请求体字段（phone, password, realName, smsCode）与后端 DTO 一致
- [ ] 手机号格式校验：`^1[3-9]\d{9}$`
- [ ] 密码长度校验：6-20 位
- [ ] 成功响应返回 `{ code: 200, data: { token, userId, phone, realName, roles } }`
- [ ] 手机号已注册时返回错误码 7001

**3b. POST /api/v1/public/auth/login — 登录**

- [ ] 请求体字段（phone, password）正确
- [ ] 成功响应返回 token，前端存储到 `uni.setStorageSync('token', token)`
- [ ] 密码错误返回错误码 7002
- [ ] 账号冻结返回错误码 7009
- [ ] 登录成功后后续请求自动携带 `Authorization: Bearer <token>`

**3c. POST /api/v1/public/auth/sms-code — 验证码**

- [ ] 请求体字段（phone）正确
- [ ] 无需 Token（`needToken: false`）
- [ ] 成功响应返回提示信息

**3d. GET /api/v1/user/auth/me — 当前用户**

- [ ] 携带 Token 请求
- [ ] 响应包含 userId, phone, realName, roles
- [ ] roles 用于前端角色判断和路由守卫
- [ ] 无 Token 时返回 401

**3e. GET /api/v1/user/notices — 公告列表**

- [ ] 响应为 NoticeResponse 数组
- [ ] 列表为空时前端不报错

**3f. POST /api/v1/user/children — 添加儿童**

- [ ] 请求体字段与 ChildCreateRequest 一致
- [ ] birthDate 格式为 `yyyy-MM-dd`
- [ ] gender 为 Integer（0/1/2）
- [ ] 成功响应返回 ChildResponse

**3g. GET /api/v1/user/children — 儿童列表**

- [ ] 响应为 ChildResponse 数组
- [ ] 列表为空时前端不报错

- [ ] **Step 4: 异常场景验证**

- [ ] Token 过期 → 前端跳转登录页
- [ ] 无效 Token → 前端跳转登录页
- [ ] 网络断开 → 前端显示"网络连接失败"
- [ ] 后端未启动 → 前端显示"系统异常，请稍后重试"

- [ ] **Step 5: 阶段 1 联调通过标记**

确认以上所有检查项通过后，记录联调结果。

---

## Task 17: 阶段 2 联调 — 核心业务流程

**前置条件：** 阶段 1 通过，测试数据库中有儿童档案和疫苗库存

**本阶段覆盖 API（12 个）：**
1. `POST /api/v1/user/appointments` — 创建预约
2. `GET /api/v1/user/appointments` — 预约列表
3. `GET /api/v1/user/appointments/{id}` — 预约详情
4. `PUT /api/v1/user/appointments/{id}/cancel` — 取消预约
5. `POST /api/v1/precheck/assess` — 预检评估
6. `GET /api/v1/precheck/records/{appointmentId}` — 预检记录
7. `POST /api/v1/register/queue` — 登记排队
8. `PUT /api/v1/register/{id}/verify` — 核验登记
9. `GET /api/v1/register/today` — 今日排队
10. `POST /api/v1/vaccinate/execute` — 执行接种
11. `GET /api/v1/user/vaccination-records` — 接种记录
12. `GET /api/v1/user/children/{childId}/vaccination-records` — 儿童接种记录

- [ ] **Step 1: 端到端流程联调**

按以下顺序操作，验证状态机流转：

```
1. 家长登录 (13800000001 / 123456)
2. 创建预约（选儿童 + 选疫苗 + 选日期时段）
   → 验证返回状态 APPOINTED(1)
3. 查看预约列表 → 验证包含新创建的预约
4. 查看预约详情 → 验证所有字段正确

--- 切换到预检医生账号 (13900000001) ---
5. 获取今日排队列表
6. 提交预检评估（result: PASS）
   → 验证返回 PreCheckRecordResponse
7. 查询预检记录 → 验证数据一致

--- 切换到登记医生账号 (13900000002) ---
8. 登记排队
   → 验证返回 queueNo
9. 核验登记
   → 验证 verifyStatus 变为 VERIFIED

--- 切换到接种医生账号 (13900000003) ---
10. 获取 FEFO 批次推荐
11. 执行接种
    → 验证返回 VaccinationRecordResponse

--- 切换到留观医生账号 (13900000004) ---
12. 开始留观
13. 结束留观（durationMinutes >= 1）
    → 验证状态流转到 COMPLETED(2)

--- 切回家长账号 ---
14. 查看接种记录 → 验证包含刚完成的接种
15. 查看儿童接种记录 → 验证数据一致
```

- [ ] **Step 2: 取消预约验证**

```
1. 创建新预约
2. 取消预约（提供 reason）
   → 验证状态变为 CANCELLED(3)
3. 验证已取消的预约不可再次取消（后端状态机校验）
```

- [ ] **Step 3: 权限验证**

- [ ] 家长账号无法访问 `/api/v1/precheck/assess`（应返回 403）
- [ ] 预检医生无法访问 `/api/v1/vaccinate/execute`（应返回 403）
- [ ] 接种医生无法访问 `/api/v1/register/queue`（应返回 403）

- [ ] **Step 4: 阶段 2 联调通过标记**

---

## Task 18: 阶段 3 联调 — 留观 + 库存管理

**前置条件：** 阶段 2 通过

**本阶段覆盖 API（15 个）：**

**留观（5 个）：**
1. `POST /api/v1/observe/start`
2. `PUT /api/v1/observe/{id}/finish`
3. `POST /api/v1/observe/adverse-reaction`
4. `PUT /api/v1/observe/adverse-reaction/{id}/handle`
5. `GET /api/v1/observe/adverse-reaction/{observeRecordId}`

**库存（10 个）：**
6. `GET /api/v1/stock/hospital`
7. `GET /api/v1/stock/batches`
8. `POST /api/v1/stock/batches`
9. `POST /api/v1/stock/transfer`
10. `POST /api/v1/stock/batches/{id}/dispose`
11. `GET /api/v1/stock/alerts`
12. `GET /api/v1/vaccinate/fefo-batch/{vaccineId}`
13. `GET /api/v1/user/appointments/by-date`
14. `GET /api/v1/user/children/{childId}`
15. `PUT /api/v1/user/children/{childId}`

- [ ] **Step 1: 不良反应流程联调**

```
1. 留观医生开始留观
2. 上报不良反应（severity: MILD/MODERATE/SEVERE）
3. 查询不良反应记录
4. 处理不良反应（填写 handleResult）
5. 结束留观
```

- [ ] **Step 2: 库存管理联调**

```
1. 查看库存总览
2. 查看批次列表
3. 创建新批次（入库）
4. 库存调拨（从仓库到接种点）
5. 批次报损
6. 查看库存预警
```

- [ ] **Step 3: 数据一致性验证**

- [ ] 入库后库存总览数量增加
- [ ] 调拨后源位置减少、目标位置增加
- [ ] 报损后批次数量减少
- [ ] FEFO 推荐返回最早过期的可用批次

- [ ] **Step 4: 阶段 3 联调通过标记**

---

## Task 19: 阶段 4 联调 — 排班 + 管理后台

**前置条件：** 阶段 3 通过

**本阶段覆盖 API（23 个）：**

**排班（5 个）：**
1. `POST /api/v1/schedule`
2. `PUT /api/v1/schedule/{id}`
3. `DELETE /api/v1/schedule/{id}`
4. `GET /api/v1/schedule/{id}`
5. `GET /api/v1/schedule/date`

**管理后台（18 个）：**
6-8. 用户管理：GET 列表、PUT 冻结、PUT 解冻
9-12. 角色管理：POST/PUT/DELETE/GET
13-16. 公告管理：POST/PUT/DELETE/GET
17-21. 窗口管理：POST/PUT/DELETE/GET + 按类型筛选
22. `DELETE /api/v1/user/children/{childId}`
23. `PUT /api/v1/admin/users/{id}/roles`（需后端补充实现）

- [ ] **Step 1: 排班管理联调**

```
1. 创建排班（选医生 + 窗口 + 日期 + 时段）
2. 查看排班详情
3. 按日期查询排班
4. 更新排班
5. 删除排班
```

- [ ] **Step 2: 管理后台 CRUD 联调**

对用户管理、角色管理、公告管理、窗口管理各执行一次完整 CRUD：

```
1. 查看列表 → 验证数据展示
2. 创建 → 验证列表中出现新记录
3. 更新 → 验证数据变更
4. 删除 → 验证列表中记录消失
```

- [ ] **Step 3: 用户冻结/解冻验证**

```
1. 冻结用户 → 验证被冻结用户无法登录（错误码 7009）
2. 解冻用户 → 验证可以正常登录
```

- [ ] **Step 4: 角色分配验证**

- [ ] 检查 `PUT /api/v1/admin/users/{id}/roles` 是否已实现
- [ ] 如未实现，通过 SQL 手动分配角色作为临时方案
- [ ] 如已实现，验证角色分配和变更生效

- [ ] **Step 5: 权限边界验证**

- [ ] 普通用户无法访问任何 `/api/v1/admin/*` 端点
- [ ] 非 SUPER_ADMIN 无法执行冻结/解冻操作
- [ ] 系统内置角色（isSystem=true）不可删除

- [ ] **Step 6: 阶段 4 联调通过标记**

---

## Task 20: 全量回归与收尾

**前置条件：** 阶段 1-4 全部通过

- [ ] **Step 1: 全链路回归测试**

使用测试账号完整走一遍从预约到完成的全流程（参考 Task 17 Step 1），确认无回归问题。

- [ ] **Step 2: 清理 Mock**

将 `mock/index.js` 中 `USE_MOCK` 恢复为 `false`，确认所有功能在真实后端下正常。

- [ ] **Step 3: 联调报告**

记录以下信息：
- 各阶段通过日期
- 发现并修复的问题清单
- 已知遗留问题（如有）
- 需要后端补充实现的 API 清单

- [ ] **Step 4: Final Commit**

```bash
git add -A
git commit -m "chore: 前后端联调完成（57个API全部验证通过）"
```
