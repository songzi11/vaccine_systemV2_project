# 前后端联调方案设计

**项目：** 疫苗管理系统 V2
**日期：** 2026-04-04
**状态：** 已批准

## 背景

后端已完成实施（57 个 API 端点，DDD 六层架构），前端使用 uni-app 框架开发，当前处于初期阶段。本方案采用 **Mock 优先 + 分模块联调** 的方式，确保前后端解耦开发，最终高效完成对接。

## 技术栈

| 层 | 技术 |
|----|------|
| 后端 | Spring Boot 3.2.5 + MyBatis-Plus + MySQL + Redis + JWT |
| 前端 | uni-app (Vue 3) + Pinia + uni-ui |
| API 文档 | Knife4j / OpenAPI 3（`http://localhost:8080/doc.html`） |

---

## 一、前端基础架构搭建

联调前需先搭建以下基础设施（当前 uni-app 模板中均不存在）：

### 1.1 请求层封装

基于 `uni.request()` 封装统一 `http` 模块：

- **baseURL**：通过环境变量区分（开发：`http://localhost:8080`，生产：部署地址）
- **JWT 注入**：请求拦截器自动从 `uni.getStorageSync('token')` 读取并附加 `Authorization: Bearer xxx`
- **错误处理**：
  - 401 → 清除 token，跳转登录页
  - 403 → 提示"无权限"
  - 500 → 提示"服务器异常"
  - 网络错误 → 提示"网络连接失败"
- **统一响应解析**：解包 `UnifiedApiResponse<T>` 结构（`code` / `message` / `data`），仅返回 `data` 给调用方

### 1.2 API 服务层

按模块在 `api/` 目录组织，与后端 57 个 API 一一对应：

```
api/
  auth.js          # 认证：注册、登录、验证码、当前用户
  children.js      # 儿童档案 CRUD
  appointment.js   # 预约管理
  precheck.js      # 预检评估
  register.js      # 登记排队
  vaccinate.js     # 接种执行与记录
  observe.js       # 留观与不良反应
  stock.js         # 库存管理
  schedule.js      # 排班管理
  admin-users.js   # 用户管理
  admin-roles.js   # 角色管理
  admin-notices.js # 公告管理
  admin-windows.js # 窗口管理
```

每个模块导出函数签名示例：

```javascript
// api/auth.js
export function login(phone, password) {
  return http.post('/api/v1/public/auth/login', { phone, password })
}
```

### 1.3 认证与路由守卫

- Token 存储：`uni.setStorageSync('token', jwtToken)`
- 登录状态检查：页面 `onShow` 或 `uni.addInterceptor('navigateTo')` 中判断
- 角色路由守卫：根据 `UserContext.roles` 控制页面访问权限
  - `USER` → 家长端页面
  - `DOCTOR_*` → 医生端页面
  - `SUPER_ADMIN` → 管理员端页面

### 1.4 Mock 服务

在 `mock/` 目录为每个模块提供静态 JSON 响应：

```
mock/
  auth.js
  children.js
  appointment.js
  ...
```

- 通过环境变量 `VITE_USE_MOCK=true` 切换
- API 层判断：Mock 开启时返回本地 JSON，否则走真实请求
- Mock 数据格式与 `UnifiedApiResponse<T>` 一致：`{ code: 200, message: "success", data: ... }`

---

## 二、联调模块划分与优先级

### 阶段 1：认证 + 用户基础（7 个 API）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/public/auth/register` | 用户注册 |
| POST | `/api/v1/public/auth/login` | 用户登录（获取 JWT） |
| POST | `/api/v1/public/auth/sms-code` | 发送短信验证码 |
| GET | `/api/v1/user/auth/me` | 当前用户信息 |
| GET | `/api/v1/user/notices` | 公告列表 |
| POST | `/api/v1/user/children` | 添加儿童档案 |
| GET | `/api/v1/user/children` | 儿童列表 |

**优先原因：** 登录是所有操作的前提；儿童信息是预约的前提；公告是首页展示内容。

### 阶段 2：核心业务流程（12 个 API）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/user/appointments` | 预约接种 |
| GET | `/api/v1/user/appointments` | 预约列表 |
| GET | `/api/v1/user/appointments/{id}` | 预约详情 |
| PUT | `/api/v1/user/appointments/{id}/cancel` | 取消预约 |
| POST | `/api/v1/precheck/assess` | 预检评估 |
| GET | `/api/v1/precheck/records/{appointmentId}` | 预检记录 |
| POST | `/api/v1/register/queue` | 登记排队 |
| PUT | `/api/v1/register/{id}/verify` | 核验登记 |
| GET | `/api/v1/register/today` | 今日排队列表 |
| POST | `/api/v1/vaccinate/execute` | 执行接种 |
| GET | `/api/v1/user/vaccination-records` | 接种记录 |
| GET | `/api/v1/user/children/{childId}/vaccination-records` | 儿童接种记录 |

**优先原因：** 系统核心业务链路：预约 → 预检 → 登记 → 接种，需端到端验证状态流转。

### 阶段 3：留观 + 库存管理（15 个 API）

**留观模块（5 个）：**
- POST `/api/v1/observe/start` — 开始留观
- PUT `/api/v1/observe/{id}/finish` — 结束留观
- POST `/api/v1/observe/adverse-reaction` — 上报不良反应
- PUT `/api/v1/observe/adverse-reaction/{id}/handle` — 处理不良反应
- GET `/api/v1/observe/adverse-reaction/{observeRecordId}` — 查询不良反应

**库存模块（10 个）：**
- GET `/api/v1/stock/hospital` — 库存总览
- GET `/api/v1/stock/batches` — 批次列表
- POST `/api/v1/stock/batches` — 入库
- POST `/api/v1/stock/transfer` — 调拨
- POST `/api/v1/stock/batches/{id}/dispose` — 报损
- GET `/api/v1/stock/alerts` — 库存预警
- GET `/api/v1/vaccinate/fefo-batch/{vaccineId}` — FEFO 批次推荐
- GET `/api/v1/user/appointments/by-date` — 按日期查询预约
- GET `/api/v1/user/children/{childId}` — 儿童详情
- PUT `/api/v1/user/children/{childId}` — 更新儿童档案

### 阶段 4：排班 + 管理后台（23 个 API）

**排班模块（5 个）：**
- POST `/api/v1/schedule` — 创建排班
- PUT `/api/v1/schedule/{id}` — 更新排班
- DELETE `/api/v1/schedule/{id}` — 删除排班
- GET `/api/v1/schedule/{id}` — 排班详情
- GET `/api/v1/schedule/date` — 按日期查询排班

**管理后台（18 个）：**
- 用户管理：GET 列表、PUT 冻结、PUT 解冻（3 个）
- 角色管理：POST/PUT/DELETE/GET CRUD（4 个）
- 公告管理：POST/PUT/DELETE/GET CRUD（4 个）
- 窗口管理：POST/PUT/DELETE/GET CRUD + 按类型筛选（5 个）
- DELETE `/api/v1/user/children/{childId}` — 删除儿童档案（1 个）
- PUT `/api/v1/admin/users/{id}/roles` — 分配角色（1 个，需后端补充实现）

---

## 三、联调流程规范

### 3.1 单个 API 联调检查项

每个 API 对接时逐项验证：

**请求层：**
- [ ] 请求方法（GET/POST/PUT/DELETE）正确
- [ ] 请求路径和路径参数正确
- [ ] 请求体 JSON 格式与后端 DTO 一致
- [ ] JWT token 正确携带在 `Authorization: Bearer` 头中
- [ ] 日期、枚举等格式与后端一致

**响应层：**
- [ ] 能正确解析 `UnifiedApiResponse` 结构（`code` / `message` / `data`）
- [ ] 分页参数处理正确（`current` / `size`）
- [ ] 列表为空时不报错
- [ ] 错误响应能正确提示用户

**业务层：**
- [ ] 正常流程走通
- [ ] 权限不足时正确拦截（403）
- [ ] Token 过期时跳转登录（401）
- [ ] 网络异常时有友好提示

### 3.2 联调工具

| 工具 | 用途 |
|------|------|
| Knife4j (`http://localhost:8080/doc.html`) | API 文档查阅、在线调试 |
| MySQL | 直接查看数据库验证数据一致性 |
| Redis | 验证缓存和 token 状态 |
| 浏览器 DevTools | 查看网络请求/响应 |
| HBuilderX 控制台 | 查看前端日志 |

### 3.3 测试数据准备

联调前需准备以下测试数据：

- 各角色用户账号（USER、DOCTOR_*、SUPER_ADMIN）
- 测试儿童档案（至少 2 个）
- 测试疫苗库存（至少 3 种疫苗、各 2 个批次）
- 测试预约数据（覆盖不同状态）

可基于后端 `vaccine_db_init.sql` 种子数据，或编写额外 SQL 脚本补充。

---

## 四、风险与应对

| 风险 | 影响 | 应对措施 |
|------|------|----------|
| uni-app H5 跨域限制 | H5 模式请求被 CORS 拦截 | 后端已配置 `AllowedOriginPatterns(*)`；App/小程序无此问题 |
| 小程序平台 Header 限制 | JWT token 传递受限 | 使用 `uni.request` 的 `header` 参数传递，需测试验证各平台 |
| 日期格式差异 | 前端 Date 与后端 LocalDateTime 不一致 | 统一 ISO 8601 格式（`yyyy-MM-dd'T'HH:mm:ss`），后端 Jackson 已配置 |
| 分页参数格式 | MyBatis-Plus 分页与前端组件参数名不同 | 前端统一转换为 `current` / `size` |
| 角色分配 API 未实现 | 无法通过 API 分配角色 | 联调前补充实现，或通过 SQL 手动分配 |
| 预约状态机流转 | 9 个状态，非法转换可能出错 | 严格按照后端定义的合法状态转换实现前端 |

### 异常场景联调清单

- Token 过期 → 自动跳转登录
- 网络断开 → 本地提示 + 自动重试
- 并发操作 → 验证后端乐观锁 / 幂等处理
- 数据不存在（404）→ 友好提示
- 参数校验失败（400）→ 显示后端返回的具体错误信息
