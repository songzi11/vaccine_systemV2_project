# 管理员端APP前端设计文档

## 概述

疫苗管理系统V2的管理员端APP，基于uni-app框架构建，与家长端、医生端共用同一项目。3个管理员角色（SUPER_ADMIN、DOCTOR_BUSINESS_ADMIN、DOCTOR_SCHEDULE）共用单一APP，登录后根据角色动态显示不同的TabBar和功能入口。涵盖排班管理、窗口管理、疫苗管理、公告管理、角色权限管理、系统配置和统计分析七个功能模块。

## 技术栈

| 项目 | 选型 |
|------|------|
| 框架 | uni-app + Vue 3 (Composition API / `<script setup>`) |
| 状态管理 | Pinia |
| UI组件库 | uni-ui（原架构设计为 uView UI 2.x，因 uni-ui 与 uni-app 原生兼容性更好、维护更活跃，三端统一变更为 uni-ui） |
| HTTP请求 | 复用 `utils/request.js`（JWT拦截器 + X-Request-Id） |
| 图表 | u-charts（项目已有） |
| 目标平台 | H5 + 微信小程序 + APP |
| 样式 | SCSS + uni.scss变量 |

## 项目结构（管理员端新增部分）

```
vaccine_systemV2app/
├── api/
│   ├── schedule.js         # 排班管理接口
│   ├── window.js           # 窗口管理接口
│   ├── vaccine-manage.js   # 疫苗管理接口（区别于家长端vaccine.js查询接口）
│   ├── notice-manage.js    # 公告管理接口（区别于家长端notice.js查询接口）
│   ├── stats.js            # 统计分析接口
│   ├── role.js             # 角色权限管理接口
│   ├── config.js           # 系统配置接口
│   └── user-manage.js      # 用户管理接口
├── components/
│   ├── AdminNavCard.vue    # 管理中心导航卡片
│   ├── StatsCard.vue       # 统计数据卡片
│   ├── ChartPanel.vue      # 图表面板（封装u-charts）
│   └── FilterBar.vue       # 通用筛选栏（下拉+搜索）
├── hooks/
│   ├── usePagination.js    # 通用分页（复用家长端）
│   └── useAdminFilter.js   # 管理端筛选（下拉筛选+搜索+重置）
├── pages/
│   ├── index/              # 首页（TabBar，复用医生端模式）
│   ├── admin/              # 管理中心（TabBar）
│   │   ├── index.vue           # 管理中心入口（功能导航网格）
│   │   ├── schedule/           # 排班管理
│   │   │   ├── list.vue            # 排班列表
│   │   │   └── edit.vue            # 新增/编辑排班
│   │   ├── window/             # 窗口管理
│   │   │   ├── list.vue            # 窗口列表
│   │   │   ├── edit.vue            # 新增/编辑窗口
│   │   │   └── service.vue         # 配置窗口服务
│   │   ├── vaccine/            # 疫苗管理
│   │   │   ├── list.vue            # 疫苗列表
│   │   │   └── edit.vue            # 新增/编辑疫苗
│   │   ├── notice/             # 公告管理
│   │   │   ├── list.vue            # 公告列表
│   │   │   ├── publish.vue         # 发布公告
│   │   │   ├── approve.vue         # 审批公告
│   │   │   └── feedback.vue        # 公告反馈
│   │   ├── role/               # 角色权限
│   │   │   ├── list.vue            # 角色列表
│   │   │   ├── edit.vue            # 新增/编辑角色
│   │   │   └── assign.vue          # 分配角色给用户
│   │   └── config/             # 系统配置
│   │       └── index.vue            # 系统配置列表
│   ├── stats/              # 统计分析（TabBar，仅SUPER_ADMIN）
│   │   ├── vaccination.vue      # 接种统计
│   │   ├── stock.vue            # 库存统计
│   │   ├── efficiency.vue       # 效率统计
│   │   └── anomaly.vue          # 异常统计
│   └── mine/              # 个人中心（TabBar，复用医生端）
├── store/
│   └── admin.js            # 管理端状态（筛选条件缓存等）
└── utils/
    └── tabBar.js           # 扩展管理员TabBar配置
```

## 动态 TabBar 设计

复用医生端的自定义 TabBar 方案（`CustomTabBar.vue`），通过 Pinia store 中的角色信息决定显示哪些 Tab。

### TabBar 配置

| 角色 | Tab 1 | Tab 2 | Tab 3 | Tab 4 |
|------|-------|-------|-------|-------|
| SUPER_ADMIN | 首页 | 统计分析 | 管理中心 | 我的 |
| DOCTOR_BUSINESS_ADMIN | 首页 | 管理中心 | 我的 | - |
| DOCTOR_SCHEDULE | 首页 | 排班管理 | 我的 | - |

### 实现方式

```js
// utils/tabBar.js — 在医生端配置基础上扩展
const TAB_BAR_CONFIG = {
  // ... 医生端配置（FLOW_DOCTOR, REGISTER_DOCTOR, STOCK_DOCTOR）保留不变

  SUPER_ADMIN: [
    { pagePath: 'pages/index/index', text: '首页', icon: 'home' },
    { pagePath: 'pages/stats/vaccination', text: '统计分析', icon: 'chart' },
    { pagePath: 'pages/admin/index', text: '管理中心', icon: 'settings' },
    { pagePath: 'pages/mine/index', text: '我的', icon: 'person' }
  ],
  BUSINESS_ADMIN: [
    { pagePath: 'pages/index/index', text: '首页', icon: 'home' },
    { pagePath: 'pages/admin/index', text: '管理中心', icon: 'settings' },
    { pagePath: 'pages/mine/index', text: '我的', icon: 'person' }
  ],
  SCHEDULE_DOCTOR: [
    { pagePath: 'pages/index/index', text: '首页', icon: 'home' },
    { pagePath: 'pages/admin/schedule/list', text: '排班管理', icon: 'calendar' },
    { pagePath: 'pages/mine/index', text: '我的', icon: 'person' }
  ]
}
```

## 页面路由设计

### TabBar页面

| 路径 | 说明 | 可见角色 |
|------|------|---------|
| `pages/index/index` | 首页（复用医生端模式） | 全部管理员 |
| `pages/admin/index` | 管理中心 | SUPER_ADMIN, DOCTOR_BUSINESS_ADMIN |
| `pages/stats/vaccination` | 接种统计 | SUPER_ADMIN |
| `pages/mine/index` | 个人中心 | 全部管理员 |

### 非Tab页面

| 模块 | 路径 | 说明 |
|------|------|------|
| 排班 | `pages/admin/schedule/list` | 排班列表 |
| 排班 | `pages/admin/schedule/edit` | 新增/编辑排班 |
| 窗口 | `pages/admin/window/list` | 窗口列表 |
| 窗口 | `pages/admin/window/edit` | 新增/编辑窗口 |
| 窗口 | `pages/admin/window/service` | 配置窗口服务 |
| 疫苗 | `pages/admin/vaccine/list` | 疫苗列表 |
| 疫苗 | `pages/admin/vaccine/edit` | 新增/编辑疫苗 |
| 公告 | `pages/admin/notice/list` | 公告列表 |
| 公告 | `pages/admin/notice/publish` | 发布公告 |
| 公告 | `pages/admin/notice/approve` | 审批公告 |
| 公告 | `pages/admin/notice/feedback` | 公告反馈 |
| 角色 | `pages/admin/role/list` | 角色列表 |
| 角色 | `pages/admin/role/edit` | 新增/编辑角色 |
| 角色 | `pages/admin/role/assign` | 分配角色给用户 |
| 配置 | `pages/admin/config/index` | 系统配置 |
| 统计 | `pages/stats/stock` | 库存统计 |
| 统计 | `pages/stats/efficiency` | 效率统计 |
| 统计 | `pages/stats/anomaly` | 异常统计 |

### 404兜底

无效路由（未在 `pages.json` 中注册的路径）由 uni-app 框架自动跳转到 404 页面。管理端复用公共 404 页面 `pages/common/404`，显示"页面不存在"提示和返回首页按钮。

## API层设计

### `api/schedule.js`

| 函数 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `getScheduleList` | GET | `/api/v1/admin/schedules` | 排班列表（doctorId, windowId, date, startDate, endDate, page, size） |
| `createSchedule` | POST | `/api/v1/admin/schedules` | 创建排班（doctorId, windowId, scheduleDate, startTime, endTime） |
| `updateSchedule` | PUT | `/api/v1/admin/schedules/{id}` | 修改排班 |
| `deleteSchedule` | DELETE | `/api/v1/admin/schedules/{id}` | 删除排班 |
| `checkConflict` | POST | `/api/v1/admin/schedules/conflict` | 排班冲突检测（doctorId, scheduleDate, startTime, endTime, excludeId） |

### `api/window.js`

| 函数 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `getWindowList` | GET | `/api/v1/admin/windows` | 窗口列表（windowCode, functionType, status, page, size） |
| `createWindow` | POST | `/api/v1/admin/windows` | 新增窗口 |
| `updateWindow` | PUT | `/api/v1/admin/windows/{id}` | 修改窗口 |
| `deleteWindow` | DELETE | `/api/v1/admin/windows/{id}` | 删除窗口 |
| `saveWindowService` | POST | `/api/v1/admin/windows/{id}/service` | 配置窗口服务 |
| `getWindowService` | GET | `/api/v1/admin/windows/service/{windowCode}` | 查询窗口服务 |

### `api/vaccine-manage.js`

| 函数 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `getVaccineList` | GET | `/api/v1/admin/vaccines` | 疫苗列表（vaccineCode, category, status, page, size） |
| `createVaccine` | POST | `/api/v1/admin/vaccines` | 新增疫苗 |
| `updateVaccine` | PUT | `/api/v1/admin/vaccines/{id}` | 修改疫苗 |
| `deleteVaccine` | DELETE | `/api/v1/admin/vaccines/{id}` | 删除疫苗 |
| `updateShelfStatus` | PUT | `/api/v1/admin/vaccines/{id}/shelf-status` | 疫苗上下架（status） |

### `api/notice-manage.js`

| 函数 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `getNoticeList` | GET | `/api/v1/admin/notices` | 公告列表（type, status, page, size） |
| `publishNotice` | POST | `/api/v1/admin/notices` | 发布公告 |
| `auditNotice` | POST | `/api/v1/admin/notices/{id}/audit` | 审批公告（result, opinion） |
| `deleteNotice` | DELETE | `/api/v1/admin/notices/{id}` | 删除公告 |
| `getNoticeFeedback` | GET | `/api/v1/admin/notices/{id}/feedback` | 公告反馈（page, size） |

### `api/stats.js`

| 函数 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `getVaccinationStats` | GET | `/api/v1/admin/stats/vaccination` | 接种统计（statsType, date, startDate, endDate, vaccineId, doctorId, windowId） |
| `getStockStats` | GET | `/api/v1/admin/stats/stock` | 库存统计（statsType, vaccineId, batchId, date） |
| `getEfficiencyStats` | GET | `/api/v1/admin/stats/efficiency` | 效率统计（statsType, startDate, endDate, windowId） |
| `getAnomalyStats` | GET | `/api/v1/admin/stats/anomaly` | 异常统计（startDate, endDate, dimensions） |

### `api/role.js`

| 函数 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `getRoleList` | GET | `/api/v1/admin/roles` | 角色列表（page, size） |
| `createRole` | POST | `/api/v1/admin/roles` | 创建角色 |
| `updateRole` | PUT | `/api/v1/admin/roles/{id}` | 修改角色 |
| `deleteRole` | DELETE | `/api/v1/admin/roles/{id}` | 删除角色 |
| `assignRoles` | POST | `/api/v1/admin/users/{userId}/assign-roles` | 分配角色给用户 |
| `getUserRoles` | GET | `/api/v1/admin/users/{userId}/roles` | 查询用户角色 |

### `api/config.js`

| 函数 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `getConfigList` | GET | `/api/v1/admin/configs` | 系统配置列表 |
| `updateConfig` | PUT | `/api/v1/admin/configs/{id}` | 更新系统配置 |

### `api/user-manage.js`

| 函数 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `getUserList` | GET | `/api/v1/admin/users` | 用户列表（phone, status, role, page, size） |
| `freezeUser` | POST | `/api/v1/admin/users/{userId}/freeze` | 冻结用户 |
| `unfreezeUser` | POST | `/api/v1/admin/users/{userId}/unfreeze` | 解冻用户 |

## Pinia Store 设计

### `useUserStore`（复用+扩展）

在医生端基础上扩展管理员角色 getter：

- 新增 Getters:
  - `isSuperAdmin` — roles 包含 SUPER_ADMIN
  - `isBusinessAdmin` — roles 包含 DOCTOR_BUSINESS_ADMIN
  - `isScheduleDoctor` — roles 包含 DOCTOR_SCHEDULE
  - `isAdminRole` — 任意管理员角色
  - `tabBarConfig` — 扩展返回管理员 TabBar 配置
- 持久化：Token通过 `utils/auth.js` 的 `setToken()/getToken()/removeToken()` 使用 `uni.setStorageSync` 持久化，用户信息（roles、realName等）登录成功后写入 Pinia Store，应用启动时从 Token 解析 roles 用于路由守卫，完整用户信息通过 `fetchProfile()` 从后端获取

### `useAdminStore`

- State: `scheduleFilters`、`windowFilters`、`vaccineFilters`、`noticeFilters`（缓存各模块筛选条件）
- Actions: `resetFilters(module)`、`saveFilters(module, filters)`

## Hooks 设计

| Hook | 用途 | 核心逻辑 |
|------|------|---------|
| `usePagination` | 通用分页 | page/size/total/hasMore/loadMore/reset（复用家长端） |
| `useAdminFilter` | 管理端筛选 | 封装下拉筛选+搜索+重置逻辑，支持筛选条件缓存到 Pinia |

## 核心页面交互设计

### 管理中心入口 (`pages/admin/index.vue`)

- **顶部**：管理员姓名 + 角色 + 今日日期（复用医生端首页模式）
- **统计概览卡片**（SUPER_ADMIN / DOCTOR_BUSINESS_ADMIN）：今日接种数、库存预警数、待审批公告数，横向滚动
- **功能导航网格**（根据角色动态显示）：
  - SUPER_ADMIN：排班管理、窗口管理、疫苗管理、公告管理、角色权限、系统配置
  - DOCTOR_BUSINESS_ADMIN：排班管理（只读）、窗口管理、疫苗管理、公告管理、角色权限
  - DOCTOR_SCHEDULE：排班管理（完整 CRUD）
- 每个入口卡片：图标 + 名称 + 角色标签（只读/可编辑）
- **公告轮播**：系统公告横向滚动（复用家长端/医生端）

### 首页 (`pages/index/index.vue`)

- **顶部**：管理员姓名 + 角色 + 今日日期
- **工作概览卡片**（根据角色动态显示）：
  - SUPER_ADMIN：今日接种数、待审批公告数、库存预警数、异常事件数
  - DOCTOR_BUSINESS_ADMIN：今日接种数、库存预警数、待审批公告数
  - DOCTOR_SCHEDULE：今日排班数、待执行排班数
- **快捷入口网格**：根据角色显示对应功能入口
- **公告轮播**：系统公告横向滚动

### 排班列表 (`pages/admin/schedule/list`)

- **顶部**：FilterBar（医生姓名搜索 + 窗口下拉 + 日期选择）
- **列表**：卡片式，排班号、医生姓名、窗口名称、日期时段、容量、状态标签
- **状态标签颜色**：正常-绿色、ON_LEAVE-灰色、CANCELLED-橙色
- **操作**：DOCTOR_SCHEDULE 显示 [修改][删除]，DOCTOR_BUSINESS_ADMIN 仅查看
- **浮动按钮**：DOCTOR_SCHEDULE 显示"新增排班"FAB
- **分页**：上拉加载更多
- **点击卡片**：跳转排班详情/编辑页

### 新增/编辑排班 (`pages/admin/schedule/edit`)

- **顶部**：返回 + "新增排班"/"编辑排班"
- **表单区**：
  - 医生姓名（下拉选择，仅显示正常状态医生）
  - 窗口（下拉选择，仅显示开放状态窗口，选择后自动显示窗口职能）
  - 排班日期（日期选择器，不早于今天）
  - 开始时间 / 结束时间（时间选择器）
- **温馨提示卡片**：排班时间须在窗口开放时间内；同一医生同一日期排班不可冲突
- **底部**：保存 / 取消
- **前端校验**：开始时间 < 结束时间
- **提交流程**：前端先调冲突检测接口（`checkConflict`），通过后提交创建/修改
- **编辑前置校验**：加载排班详情时，若排班日期已过且有关联预约，编辑页显示提示条"该排班日期已过且已有预约，仅可查看不可修改时间"，时间选择器置灰
- **删除前置校验**：点击删除按钮时，调用删除API，若返回错误码（关联数据存在），弹窗显示具体原因

### 窗口列表 (`pages/admin/window/list`)

- **顶部**：FilterBar（职能类型下拉 + 状态下拉）
- **列表**：卡片式，窗口编码、名称、职能类型标签、容量、开放时间、状态标签
- **状态标签颜色**：开放-绿色、关闭-灰色
- **职能类型标签**：签到/预检/登记/接种/留观/库存，各类型不同颜色
- **操作**：[修改][删除][配置服务]
- **浮动按钮**："新增窗口"FAB

### 新增/编辑窗口 (`pages/admin/window/edit`)

- **顶部**：返回 + "新增窗口"/"编辑窗口"
- **表单区**：窗口编码、名称、描述（选填）、职能类型（下拉，选择后自动匹配角色编码）、角色编码（可手动调整）、容量、开放时间、关闭时间、排序（选填）、状态
- **校验**：编码唯一、职能类型有效、容量>0、开放时间<关闭时间、角色与职能匹配
- **底部**：保存 / 取消

### 配置窗口服务 (`pages/admin/window/service`)

- **顶部**：返回 + "配置窗口服务"
- **窗口信息展示**（只读）：窗口编码、名称、职能类型
- **表单区**：业务名称、业务描述、业务详情说明（选填）、预估办理时间（分钟）、温馨提示（选填）、需携带物品（选填）
- **底部**：保存 / 取消

### 疫苗列表 (`pages/admin/vaccine/list`)

- **顶部**：FilterBar（疫苗类别下拉 + 状态下拉）
- **列表**：卡片式，疫苗编码、名称、类别标签（一类/二类）、适龄范围、剂次、厂家、状态标签
- **状态标签颜色**：上架-绿色、下架-灰色
- **操作**：[修改][删除][上架/下架切换]
- **上架/下架切换**：点击后二次确认弹窗，调用 `toggleVaccineStatus`
- **浮动按钮**："新增疫苗"FAB

### 新增/编辑疫苗 (`pages/admin/vaccine/edit`)

- **顶部**：返回 + "新增疫苗"/"编辑疫苗"
- **分区表单**（3个卡片区块）：
  1. 基本信息：疫苗编码、名称、类别（下拉）、厂家、规格、状态（下拉）
  2. 接种规则：适龄范围最小月龄、适龄范围最大月龄、接种剂次、接种间隔天数
  3. 疫苗说明：描述、接种程序说明、禁忌症说明、不良反应说明（均为选填文本域）
- **校验**：编码唯一、最小月龄<=最大月龄、剂次>0、间隔>0
- **底部**：保存 / 取消

### 公告列表 (`pages/admin/notice/list`)

- **顶部**：FilterBar（公告类型下拉 + 状态下拉）
- **列表**：卡片式，标题、类型标签、状态标签、发布人、生效/失效时间
- **状态标签颜色**：PENDING-橙色、PUBLISHED-绿色、TAKEN_DOWN-灰色、REJECTED-红色
- **类型标签颜色**：NORMAL-蓝色、URGENT-红色、SYSTEM-绿色
- **操作**：
  - DOCTOR_BUSINESS_ADMIN：[查看详情][删除被拒绝的公告]
  - SUPER_ADMIN：[查看详情][审批（仅待审批PENDING）][删除]
- **浮动按钮**：DOCTOR_BUSINESS_ADMIN 显示"发布公告"FAB

### 发布公告 (`pages/admin/notice/publish`)

- **顶部**：返回 + "发布公告"
- **表单区**：标题、内容（多行文本）、公告类型（下拉）、是否置顶（开关）、生效时间（日期时间选择器）、失效时间（日期时间选择器）
- **校验**：标题必填、内容必填、生效时间<失效时间
- **底部**：提交审批 / 取消
- **提交后**：状态为 PENDING，Toast"已提交审批"，返回列表页

### 审批公告 (`pages/admin/notice/approve`)

- **顶部**：返回 + "审批公告"
- **公告信息展示**（只读卡片）：标题、内容、类型、是否置顶、发布人、生效/失效时间
- **审批意见展示**（如被拒绝过）：显示上次拒绝原因
- **审批表单**：审批结果（通过/拒绝 单选按钮组）、审批意见（选填多行文本）
- **底部**：提交审批 / 取消

### 公告反馈 (`pages/admin/notice/feedback`)

- **顶部**：返回 + 公告标题（只读）
- **列表**：反馈人（手机号脱敏）、反馈内容、反馈时间
- **分页**：上拉加载更多

### 角色列表 (`pages/admin/role/list`)

- **列表**：角色名称、角色编码、权限数量、关联用户数、是否系统内置标签
- **系统内置标签**：灰色"系统内置"，不可编辑/删除
- **操作**：[查看权限][编辑]（系统内置角色隐藏编辑/删除）、[分配用户]
- **浮动按钮**："创建角色"FAB

### 新增/编辑角色 (`pages/admin/role/edit`)

- **顶部**：返回 + "创建角色"/"编辑角色"
- **表单区**：角色名称、角色编码
- **权限树**：按模块分组展示权限码（checkbox 列表），已勾选为已分配权限
  - 排班权限组：doctor.schedule.create / doctor.schedule.edit / doctor.schedule.view / doctor.schedule.delete
  - 窗口权限组：window.manage / window.service.manage
  - 疫苗权限组：vaccine.catalog.manage / vaccine.catalog.view
  - 公告权限组：notice.manage / notice.view / notice.audit / notice.feedback
  - 统计权限组：stats.view
  - 用户权限组：user.manage / user.freeze
- **底部**：保存 / 取消

### 分配角色给用户 (`pages/admin/role/assign`)

- **顶部**：返回 + "分配角色"
- **搜索区**：手机号/姓名搜索用户
- **用户信息卡片**（搜索后显示）：姓名、手机号、当前角色标签列表
- **角色选择区**：角色列表（多选 checkbox），已分配的角色勾选
- **底部**：保存 / 取消
- **校验**：必须先搜索并选中用户

### 系统配置 (`pages/admin/config/index`)

- **列表**：配置项名称、配置键（code）、当前值、说明
- **操作**：点击某项 → 弹出编辑弹窗（输入框）→ 保存
- **仅 SUPER_ADMIN 可见此页面**

### 统计分析（Tab 切换，仅 SUPER_ADMIN）

统计页面内通过顶部 Tab 切换四个维度：接种统计 | 库存统计 | 效率统计 | 异常统计

#### 接种统计 (`pages/stats/vaccination.vue`)

- **筛选区**：统计维度（日/周/月/疫苗/医生/窗口 下拉）+ 日期或日期范围选择 + 疫苗/医生/窗口（选填下拉）
- **汇总卡片**（横向滚动）：总接种人数、成功接种人数、失败接种人数、预约完成率（百分比）
- **图表区**：柱状图/折线图（u-charts），按维度展示趋势或对比
- **明细列表**：按维度分组的统计数据列表

#### 库存统计 (`pages/stats/stock.vue`)

- **筛选区**：统计类型（总览/批次/预警 下拉）+ 疫苗（选填下拉）
- **汇总卡片**：总库存、可用库存、锁定库存、使用率（百分比）、临期数、过期数
- **图表区**：饼图（库存分布）、柱状图（各疫苗库存对比）
- **预警列表**：预警批次卡片列表（仅统计类型为"预警"时显示）

#### 效率统计 (`pages/stats/efficiency.vue`)

- **筛选区**：统计类型（时长/排队/完成率 下拉）+ 日期范围 + 窗口（选填下拉）
- **汇总卡片**：平均签到时长、平均预检时长、平均登记时长、平均接种时长、平均留观时长（单位：分钟）
- **图表区**：柱状图（各环节时长对比）、折线图（完成率趋势）

#### 异常统计 (`pages/stats/anomaly.vue`)

- **筛选区**：日期范围 + 异常维度（多选 checkbox：爽约/取消/预检失败/不良反应/过期批次）
- **汇总卡片**：各维度异常数量，异常项红色高亮
- **图表区**：柱状图（各异常类型分布）

### 个人中心 (`pages/mine/index`)

- **用户信息卡片**：姓名、角色、手机号（脱敏）（复用医生端）
- **菜单列表**：修改密码、关于系统（复用家长端/医生端）
- **退出登录**：二次确认弹窗（复用家长端/医生端）

## 状态颜色映射

### 排班状态

| 状态码 | 状态名 | 颜色 | 典型场景 |
|--------|--------|------|---------|
| 0 | 正常 | #07C160 绿色 | 有效排班 |
| 1 | ON_LEAVE（请假） | #999999 灰色 | 医生请假 |
| 2 | CANCELLED（已取消） | #FF9900 橙色 | 已取消的排班 |

### 窗口状态

| 状态码 | 状态名 | 颜色 |
|--------|--------|------|
| 0 | 开放 | #07C160 绿色 |
| 1 | 关闭 | #999999 灰色 |

### 窗口职能类型

| 类型 | 颜色 |
|------|------|
| SIGNIN | #1989FA 蓝色 |
| PRECHECK | #07C160 绿色 |
| REGISTER | #FF9900 橙色 |
| VACCINATE | #EE0A24 红色 |
| OBSERVE | #7232DD 紫色 |
| STOCK | #17C3B2 青色 |

### 疫苗状态

| 状态码 | 状态名 | 颜色 |
|--------|--------|------|
| 0 | 上架 | #07C160 绿色 |
| 1 | 下架 | #999999 灰色 |

### 疫苗类别

| 类别 | 颜色 |
|------|------|
| CLASS_I | #07C160 绿色 |
| CLASS_II | #1989FA 蓝色 |

### 公告状态

| 状态码 | 状态名 | 颜色 | 典型场景 |
|--------|--------|------|---------|
| 0 | PENDING（待审批） | #FF9900 橙色 | 新发布的公告 |
| 1 | PUBLISHED（已发布） | #07C160 绿色 | 审批通过的公告 |
| 2 | TAKEN_DOWN（已下架） | #999999 灰色 | 被下架的公告 |
| 3 | REJECTED（已拒绝） | #EE0A24 红色 | 被拒绝的公告 |

### 公告类型

| 类型 | 颜色 |
|------|------|
| NORMAL | #1989FA 蓝色 |
| URGENT | #EE0A24 红色 |
| SYSTEM | #07C160 绿色 |

## 轮询策略

管理端页面以手动刷新为主，无高频实时数据需求。

| 页面类型 | 轮询间隔 | 说明 |
|----------|---------|------|
| 首页概览 | 60秒 | 复用医生端策略，概览数据时效性低 |
| 管理中心 | 60秒 | 概览数据刷新 |
| 统计分析 | 不自动轮询 | 手动点击查询 |
| 管理列表页 | 不自动轮询 | 手动下拉刷新 |

> 轮询生命周期管理：首页概览和管理中心的60秒轮询复用医生端 `useQueue` hook 的生命周期管理模式——`onShow` 启动轮询，`onHide`/`onUnload` 停止轮询，页面切换时自动清理旧轮询避免内存泄漏。

## 业务约束

| 约束 | 说明 |
|------|------|
| 排班日期有效性 | 排班日期 >= 当天 |
| 排班时间校验 | 开始时间 < 结束时间，且在窗口开放时间内 |
| 排班冲突检测 | 同一医生同一日期排班时间不可重叠 |
| 排班唯一约束 | (doctor_id, schedule_date, window_id) 唯一 |
| 排班修改限制 | 排班日期已过且已有预约的排班不能修改时间 |
| 排班删除限制 | 已有预约的排班不可删除 |
| 窗口编码唯一 | 窗口编码全局唯一 |
| 窗口职能-角色匹配 | 职能类型必须与分配的角色编码对应 |
| 窗口容量有效性 | 容量 > 0 |
| 窗口删除限制 | 窗口下无关联数据方可删除 |
| 疫苗编码唯一 | 疫苗编码全局唯一 |
| 疫苗适龄范围 | 最小月龄 <= 最大月龄 |
| 疫苗删除限制 | 疫苗下无批次记录方可删除 |
| 公告时间校验 | 生效时间 < 失效时间 |
| 公告审批权限 | 仅 SUPER_ADMIN 可审批公告 |
| 公告删除权限 | 仅 SUPER_ADMIN 可删除公告 |
| 公告驳回重提 | 被拒绝的公告可修改后重新提交（REJECTED → PENDING） |
| 公告失效机制 | 失效时间到期后系统自动标记为"已失效" |
| 角色删除限制 | 系统内置角色不可删除 |
| 统计权限 | 仅 SUPER_ADMIN 和 DOCTOR_BUSINESS_ADMIN 可查看统计 |
| 统计日期范围 | 开始日期 <= 结束日期 |

## 错误处理策略

| 错误码 | 场景 | 处理方式 |
|--------|------|---------|
| 8001 | 排班时间冲突 | Toast"该医生在该日期已有排班，时间冲突" |
| 8002 | 排班不存在 | Toast"排班不存在或已被删除" |
| 8003 | 窗口编码已存在 | Toast"窗口编码已存在" |
| 8004 | 窗口下有关联数据 | Toast"该窗口下已有排班记录，无法删除" |
| 8005 | 公告不存在 | Toast"公告不存在" |
| 8006 | 疫苗不存在 | Toast"疫苗不存在" |
| 8008 | 角色编码已存在 | Toast"角色编码已存在" |
| 8009 | 角色不存在 | Toast"角色不存在" |
| 8010 | 系统内置角色不可删除 | Toast"系统内置角色不可删除" |
| 8011 | 角色已分配给用户 | Toast"该角色已分配给用户，无法删除" |
| 8015 | 不可修改超级管理员 | Toast"不可修改超级管理员信息" |
| 1001 | Token无效/过期 | 跳转登录页，清除状态 |
| 1002 | 无权限 | Toast"无操作权限" |
| 1005 | 防重放冲突 | Toast"请勿重复操作" |
| 网络异常 | 请求失败 | Toast"网络异常，请重试" + 手动刷新按钮 |

## 安全方案

复用家长端/医生端的安全机制（`utils/request.js` + `utils/auth.js`）：

| 机制 | 实现方式 |
|------|---------|
| Token存储 | `uni.setStorageSync('token', jwt)`，通过 `utils/auth.js` 管理 |
| Token自动注入 | `request.js` 请求拦截器自动注入 `Authorization: Bearer <token>` |
| Token过期处理 | 响应拦截器检测 code=1001 → 清除Token + Pinia状态 → `uni.reLaunch` 跳转登录页 |
| 注销流程 | 调用 `POST /api/v1/user/logout`（后端将Token加入Redis黑名单）→ 前端清除全部状态 → 跳转登录页 |
| 防重放 | 排班创建、公告发布、疫苗上下架等写操作由 `request.js` 自动生成 `X-Request-Id`（UUID v4） |
| 角色路由守卫 | 登录后按roles跳转不同首页，动态TabBar按角色显示（复用医生端CustomTabBar） |
| 敏感数据脱敏 | 个人中心手机号脱敏（前3后4）、公告反馈页反馈人手机号脱敏 |

## 可复用组件清单

| 组件 | 用途 | 使用页面 |
|------|------|---------|
| `CustomTabBar.vue` | 自定义TabBar（动态显示） | 所有TabBar页面（复用医生端） |
| `StatusTag.vue` | 状态标签（颜色映射） | 所有列表页（复用医生端） |
| `EmptyState.vue` | 空数据占位 | 所有列表页（复用家长端） |
| `AdminNavCard.vue` | 管理中心功能导航卡片（图标+名称+权限标签） | 管理中心入口 |
| `StatsCard.vue` | 统计数据卡片（数值+标签+趋势箭头） | 统计分析页面、首页概览 |
| `ChartPanel.vue` | 图表面板（封装u-charts，支持柱状图/折线图/饼图） | 统计分析页面 |
| `FilterBar.vue` | 通用筛选栏（下拉选择器+搜索框+重置按钮） | 所有管理列表页 |
| `usePagination` | 通用分页hook | 所有列表页（复用家长端） |
| `useAdminFilter` | 管理端筛选hook | 所有管理列表页 |

## 空状态处理

管理端所有列表页复用家长端 `EmptyState.vue` 组件，各页面定制提示：

| 页面 | 标题 | 描述 | 操作按钮 |
|------|------|------|----------|
| 排班列表 | 暂无排班记录 | 当前筛选条件下无排班 | — |
| 窗口列表 | 暂无窗口 | 尚未配置服务窗口 | 新增窗口 |
| 疫苗列表 | 暂无疫苗 | 尚未录入疫苗信息 | 新增疫苗 |
| 公告列表 | 暂无公告 | 目前没有系统公告 | — |
| 角色列表 | 暂无角色 | 尚未创建自定义角色 | 创建角色 |
| 统计数据 | 暂无统计数据 | 当前筛选条件下无数据 | — |

## 边界场景处理

| 场景 | 处理方式 |
|------|---------|
| 并发编辑排班 | 后端乐观锁，前端提交失败后刷新数据并Toast提示 |
| 重复提交公告 | 防重放X-Request-Id机制，重复提交返回1005 |
| 删除有关联数据的窗口/疫苗 | 后端返回错误码（8004/8006），前端弹窗展示具体原因 |
| 审批已被他人审批的公告 | 后端返回状态异常错误码，前端Toast"该公告已被处理" |
