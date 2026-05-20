# 疫苗管理系统 V2 — 实施计划

## 一、项目现状

已完成：common（21文件）、domain（61文件）、infrastructure 大部分（76文件）
未完成：application（0文件）、adapter（仅1个GlobalExceptionHandler）、SecurityConfig、部分RepositoryImpl

---

## 二、跨层设计约定（所有批次必须遵守）

### 2.1 包命名规范

```
com.tjut.edu.vaccine
  ├── common
  │   ├── constants/
  │   ├── enums/
  │   ├── exception/
  │   └── response/
  ├── domain
  │   ├── identity/          (aggregate/, entity/, vo/, repository/)
  │   ├── appointment/       (aggregate/, entity/, event/, repository/)
  │   ├── register/          (aggregate/, event/, repository/)
  │   ├── vaccinate/         (aggregate/, event/, repository/)
  │   ├── observe/           (aggregate/, entity/, event/, repository/)
  │   └── stock/             (aggregate/, entity/, event/, repository/)
  ├── application
  │   ├── dto/
  │   │   ├── request/
  │   │   └── response/
  │   ├── assembler/
  │   └── service/
  ├── infrastructure
  │   ├── config/
  │   ├── security/
  │   └── persistence/
  │       ├── po/
  │       ├── mapper/
  │       ├── converter/
  │       └── repository/
  ├── adapter
  │   └── web/
  │       ├── auth/
  │       ├── user/
  │       ├── appointment/
  │       ├── precheck/
  │       ├── register/
  │       ├── vaccinate/
  │       ├── observe/
  │       ├── stock/
  │       ├── schedule/
  │       ├── admin/
  │       └── common/
  └── start/
```

### 2.2 Lombok 策略

| 层 | 注解 | 原因 |
|---|---|---|
| PO（infrastructure） | `@Data` | 纯数据容器，需要全部 getter/setter |
| Domain Entity | `@Getter @Setter` | converter 需要 setter 从 DB 重建对象 |
| Domain Aggregate | `@Getter @Setter` | 同上 |
| DTO（application） | `@Data` | 纯数据传输对象 |
| Request DTO（adapter） | `@Data` | 需要反序列化 |

### 2.3 构造器策略

| 类型 | 构造器 | 原因 |
|---|---|---|
| Domain Aggregate | `public` 无参 + `public/private` 有参 | infrastructure converter 需要无参构造器重建对象 |
| Domain Entity | `public` 无参 | 同上 |
| PO | `@Data` 自动生成 | - |
| DTO | `@Data` 自动生成 | - |

### 2.4 Converter 策略

- Domain ↔ PO：手写 static 方法（已有14个，保持一致）
- Domain ↔ DTO：手写 static 方法，放在 application/assembler/
- **每次写 converter/assembler 前，必须先读取目标类的字段列表，确保字段名和类型匹配**

### 2.5 Repository 实现策略

- **写 impl 前，必须先读取接口文件，逐方法实现**
- 接口返回 Domain 对象 → impl 负责转换 PO → Domain
- 接口参数是 Domain 对象 → impl 负责转换 Domain → PO

---

## 三、批次计划

> 每个批次完成后必须：`mvn compile` 通过 → git commit → 再进入下一批次

---

### Batch 0：基础设施补全（前置条件）

**目标**：补全缺失的基础设施组件，确保项目能正确启动和安全运行

#### Task 0.1 — SecurityConfig
- 文件：`infrastructure/security/SecurityConfig.java`
- 内容：
  - 配置 SecurityFilterChain
  - 放行 `/api/v1/public/**`、Swagger/Knife4j 路径
  - 其余路径需 JWT 认证
  - 注入 JwtAuthenticationFilter
- **编译验证** → commit

#### Task 0.2 — 修复 RepositoryImpl 遗留问题
- `VaccinationRecordRepositoryImpl.findByChildId()` — 当前是 stub，需要关联 appointment 表查询
- `VaccineStockRepositoryImpl.sumTotalByVaccine()` / `sumAvailableByVaccine()` — 当前返回硬编码 0，需要正确 SQL
- 新增 `PreCheckRecordRepository` 接口（domain层）+ `PreCheckRecordRepositoryImpl`（infrastructure层）
- **编译验证** → commit

---

### Batch 1：认证模块（auth）

**目标**：用户注册、登录、JWT 鉴权全链路可用

#### Task 1.1 — Auth DTO
- 文件：`application/dto/request/RegisterRequest.java`
- 文件：`application/dto/request/LoginRequest.java`
- 文件：`application/dto/request/SmsRequest.java`
- 文件：`application/dto/response/AuthResponse.java`（token + 用户基本信息）
- 文件：`application/dto/response/UserInfoResponse.java`
- **编译验证**

#### Task 1.2 — Auth Assembler
- 文件：`application/assembler/AuthAssembler.java`
- 方法：toResponse(User user) → AuthResponse, toUserInfoResponse(User user)
- **编译验证**

#### Task 1.3 — AuthApplicationService
- 文件：`application/service/AuthApplicationService.java`
- 方法：
  - `register(RegisterRequest)` → 校验短信验证码 → 创建 User → 分配默认角色 → 保存 → 返回 token
  - `login(LoginRequest)` → 校验手机号密码 → 生成 JWT → 返回 AuthResponse
  - `sendSmsCode(String phone)` → 生成验证码 → 存 Redis（5分钟过期） → 返回（模拟发送）
  - `getCurrentUser()` → 从 UserContext 获取当前用户
- **编译验证**

#### Task 1.4 — AuthController
- 文件：`adapter/web/auth/AuthController.java`
- 端点：
  - `POST /api/v1/public/auth/register`
  - `POST /api/v1/public/auth/login`
  - `POST /api/v1/public/auth/sms-code`
  - `GET /api/v1/user/auth/me`（需认证）
- **编译验证** → commit

---

### Batch 2：儿童档案模块（child-profile）

**目标**：家长管理儿童信息

#### Task 2.1 — DTO + Assembler
- `request/ChildCreateRequest.java`, `request/ChildUpdateRequest.java`
- `response/ChildResponse.java`
- `assembler/ChildProfileAssembler.java`

#### Task 2.2 — ChildProfileApplicationService
- `create(Long parentId, ChildCreateRequest)` → 校验数量上限 → 创建 → 保存
- `update(Long parentId, Long childId, ChildUpdateRequest)` → 校验归属 → 更新
- `findById(Long childId)`
- `findByParentId(Long parentId)`
- `deleteById(Long parentId, Long childId)` → 校验归属 → 删除

#### Task 2.3 — ChildProfileController
- `POST /api/v1/user/children`
- `PUT /api/v1/user/children/{childId}`
- `GET /api/v1/user/children/{childId}`
- `GET /api/v1/user/children`
- `DELETE /api/v1/user/children/{childId}`

**编译验证** → commit

---

### Batch 3：预约模块（appointment）

**目标**：用户预约、取消、查看预约

#### Task 3.1 — DTO + Assembler
- `request/AppointmentBookRequest.java`（childId, vaccineId, date, timeSlot）
- `request/AppointmentCancelRequest.java`（reason）
- `response/AppointmentResponse.java`
- `response/AppointmentDetailResponse.java`
- `assembler/AppointmentAssembler.java`

#### Task 3.2 — AppointmentApplicationService
- `book(Long userId, AppointmentBookRequest)` → 校验用户状态 → 校验儿童归属 → 校验疫苗库存 → 创建预约 → 保存
- `cancel(Long userId, Long appointmentId, String reason)` → 校验归属 → 校验可取消 → 取消
- `findByUserId(Long userId)` → 用户预约列表
- `findById(Long appointmentId)`
- `findByDate(LocalDate date)` → 某日预约列表（工作人员用）

#### Task 3.3 — AppointmentController
- `POST /api/v1/user/appointments`
- `PUT /api/v1/user/appointments/{id}/cancel`
- `GET /api/v1/user/appointments`
- `GET /api/v1/user/appointments/{id}`
- `GET /api/v1/signin/appointments`（工作人员，按日期查）

**编译验证** → commit

---

### Batch 4：签到与预检模块（precheck + register）

**目标**：接种流程前两步：预检 → 登记

#### Task 4.1 — PreCheck DTO + Service + Controller
- 预检评估：`POST /api/v1/precheck/assess`
- 查看预检记录：`GET /api/v1/precheck/records/{appointmentId}`
- 禁忌症检查（查询儿童过敏史/接种史）

#### Task 4.2 — Register DTO + Service + Controller
- 排队登记：`POST /api/v1/register/queue`
- 核验：`PUT /api/v1/register/{id}/verify`
- 查看今日队列：`GET /api/v1/register/today`

**编译验证** → commit

---

### Batch 5：接种模块（vaccinate）

**目标**：执行接种、记录

#### Task 5.1 — DTO + Service + Controller
- 执行接种：`POST /api/v1/vaccinate/execute`（appointmentId, doctorId, injectionSite, batchId）
- 查看接种记录：`GET /api/v1/user/vaccination-records`（用户侧）
- 查看儿童接种记录：`GET /api/v1/user/children/{childId}/vaccination-records`
- FEFO 批次选择：`GET /api/v1/vaccinate/fefo-batch/{vaccineId}`（工作人员选可用批次）

**编译验证** → commit

---

### Batch 6：留观模块（observe）

**目标**：留观管理、不良反应报告

#### Task 6.1 — Observe DTO + Service + Controller
- 开始留观：`POST /api/v1/observe/start`
- 完成留观：`PUT /api/v1/observe/{id}/finish`
- 不良反应报告：`POST /api/v1/observe/adverse-reaction`
- 处理不良反应：`PUT /api/v1/observe/adverse-reaction/{id}/handle`
- 查看留观记录：`GET /api/v1/observe/records`

**编译验证** → commit

---

### Batch 7：库存管理模块（stock）

**目标**：疫苗库存查看、入库、调拨、过期处理

#### Task 7.1 — Stock DTO + Service + Controller
- 查看医院库存：`GET /api/v1/stock/hospital`
- 查看疫苗批次列表：`GET /api/v1/stock/batches`
- 新增批次：`POST /api/v1/stock/batches`
- 库存调拨：`POST /api/v1/stock/transfer`
- 批次处置：`POST /api/v1/stock/batches/{id}/dispose`
- 库存预警列表：`GET /api/v1/stock/alerts`

**编译验证** → commit

---

### Batch 8：排班与窗口管理（schedule + admin）

**目标**：医生排班、接种窗口管理

#### Task 8.1 — Schedule DTO + Service + Controller
- 排班 CRUD：`GET/POST/PUT/DELETE /api/v1/schedule/...`
- 查看某日排班：`GET /api/v1/schedule/date`

#### Task 8.2 — Window 管理接口
- 窗口 CRUD：`GET/POST/PUT/DELETE /api/v1/admin/windows`

**编译验证** → commit

---

### Batch 9：系统管理模块（admin）

**目标**：用户管理、角色权限、公告管理

#### Task 9.1 — 用户管理
- 用户列表/冻结/解冻/分配角色
- `GET /api/v1/admin/users`, `PUT /api/v1/admin/users/{id}/freeze`, `PUT /api/v1/admin/users/{id}/roles`

#### Task 9.2 — 角色权限管理
- 角色 CRUD、权限分配
- `GET/POST/PUT/DELETE /api/v1/admin/roles`

#### Task 9.3 — 公告管理
- 公告 CRUD、反馈查看
- `GET/POST/PUT/DELETE /api/v1/admin/notices`
- `GET /api/v1/user/notices`（用户侧）

**编译验证** → commit

---

### Batch 10：定时任务与事件处理

**目标**：自动化任务和跨聚合副作用

#### Task 10.1 — 定时任务
- `AppointmentExpiryScanner`：扫描超时未签到预约 → 自动取消 → 释放库存
- `BatchExpiryScanner`：扫描即将过期批次 → 标记状态 → 生成预警

#### Task 10.2 — 领域事件处理
- AppointmentCancelled → 释放锁定库存
- VaccinationExecuted → 扣减库存 + 更新汇总
- AdverseReactionReported → 生成预警通知

**编译验证** → commit

---

## 四、质量保障流程（每个批次严格执行）

```
1. 读取相关接口/实体定义 → 确认字段和方法签名
2. 编写代码
3. mvn compile → 必须通过
4. git add 相关文件 → git commit
5. 下一批次
```

### 编译检查清单

- [ ] 所有 import 路径正确（特别是 `infrastructure.persistence.po` 不是 `infrastructure.po`）
- [ ] Domain 类有 `@Getter @Setter`（converter 需要调用 setter）
- [ ] Domain 类有无参 `public` 构造器（converter 需要实例化）
- [ ] Repository impl 的每个方法都在接口中声明了
- [ ] DTO 类使用 `@Data` 注解
- [ ] Assembler/Converter 的字段映射与目标类字段一致
