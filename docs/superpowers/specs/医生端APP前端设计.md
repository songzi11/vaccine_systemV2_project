# 医生端APP前端设计文档

## 概述

疫苗管理系统V2的医生端APP，基于uni-app框架构建，支持H5、微信小程序和APP多端编译。6个医生角色（签到、预检、登记、接种、留观、库存）共用单一APP，登录后根据角色动态显示不同的TabBar和功能入口。

## 技术栈

| 项目 | 选型 |
|------|------|
| 框架 | uni-app + Vue 3 (Composition API / `<script setup>`) |
| 状态管理 | Pinia |
| UI组件库 | uni-ui（原架构设计为 uView UI 2.x，因 uni-ui 与 uni-app 原生兼容性更好、维护更活跃，三端统一变更为 uni-ui） |
| HTTP请求 | 复用 `utils/request.js`（JWT拦截器 + X-Request-Id） |
| 目标平台 | H5 + 微信小程序 + APP |
| 样式 | SCSS + uni.scss变量 |

## 项目结构（医生端新增部分）

```
vaccine_systemV2app/
├── api/                  # API接口层
│   ├── auth.js           # 认证（复用家长端）
│   ├── signin.js         # 签到模块
│   ├── precheck.js       # 预检模块
│   ├── register.js       # 登记模块
│   ├── vaccinate.js      # 接种模块
│   ├── observe.js        # 留观模块
│   ├── stock.js          # 库存模块
│   └── ...               # 其他复用
├── components/           # 可复用组件
│   ├── QueueCard.vue     # 通用队列卡片
│   ├── StatusTag.vue     # 状态标签
│   ├── InfoCard.vue      # 信息卡片
│   ├── CountdownTimer.vue # 倒计时组件
│   └── VerifyCheckbox.vue  # 核实确认勾选
├── hooks/
│   ├── useAuth.js        # 登录/登出（复用）
│   ├── useQueue.js       # 队列轮询（自动刷新）
│   ├── useCountdown.js   # 留观倒计时（1s刷新）
│   └── usePagination.js  # 通用分页（复用）
├── pages/
│   ├── index/            # 首页（TabBar，角色动态入口）
│   ├── queue/            # 今日队列（TabBar）
│   │   ├── signin.vue        # 签到队列
│   │   ├── precheck.vue      # 预检队列
│   │   ├── register.vue      # 登记队列（含叫号）
│   │   ├── vaccinate.vue     # 接种队列
│   │   └── observe.vue       # 留观队列
│   ├── calling/          # 叫号管理（TabBar，仅登记医生）
│   │   ├── index.vue         # 叫号面板
│   │   └── history.vue       # 过号记录
│   ├── stock/            # 库存管理（TabBar，仅库存医生）
│   │   ├── summary.vue       # 库存总览
│   │   ├── batches.vue       # 批次列表
│   │   ├── batch-detail.vue  # 批次详情
│   │   ├── transfer.vue      # 调拨操作
│   │   ├── transfer-records.vue # 调拨记录
│   │   ├── alerts.vue        # 预警列表
│   │   └── dispose.vue       # 销毁操作
│   ├── process/          # 流程操作页（非Tab）
│   │   ├── signin-confirm.vue    # 签到确认
│   │   ├── precheck-assess.vue   # 预检评估
│   │   ├── register-process.vue  # 登记处理
│   │   ├── batch-switch.vue      # 批次切换
│   │   ├── vaccinate-process.vue # 接种执行
│   │   ├── vaccinate-success.vue # 接种成功
│   │   ├── observe-detail.vue    # 留观详情
│   │   ├── observe-finish.vue    # 留观结束
│   │   ├── adverse-report.vue    # 不良反应上报
│   │   └── adverse-handle.vue    # 不良反应处理
│   ├── record/           # 接种记录
│   │   ├── list.vue           # 记录列表
│   │   ├── detail.vue         # 记录详情
│   │   └── child-history.vue  # 儿童接种史
│   ├── mine/             # 个人中心（TabBar）
│   └── auth/             # 登录（复用）
├── store/
│   ├── user.js           # 用户认证（复用+扩展）
│   ├── queue.js          # 队列状态（轮询控制）
│   └── calling.js        # 叫号状态
├── utils/
│   ├── request.js        # HTTP封装（复用）
│   ├── auth.js           # 认证工具（复用）
│   ├── constants.js      # 常量（角色码、状态码、部位枚举等）
│   ├── tabBar.js         # 动态TabBar配置
│   └── queue.js          # 队列工具（轮询封装、自动清理）
└── components/
    └── CustomTabBar.vue  # 自定义TabBar组件
```

## 动态 TabBar 设计

uni-app 原生 TabBar 不支持运行时动态修改，采用**自定义 TabBar 组件**方案：隐藏原生 TabBar，用 `CustomTabBar.vue` 替代，通过 Pinia store 中的角色信息决定显示哪些 Tab。

### TabBar 配置

| 角色 | Tab 1 | Tab 2 | Tab 3 | Tab 4 |
|------|-------|-------|-------|-------|
| 签到/预检/接种/留观医生 | 首页 | 今日队列 | 接种记录 | 我的 |
| 登记医生 | 首页 | 登记队列 | 叫号管理 | 我的 |
| 库存医生 | 首页 | 库存总览 | 批次管理 | 我的 |

### 实现方式

```js
// utils/tabBar.js
const TAB_BAR_CONFIG = {
  FLOW_DOCTOR: [       // DOCTOR_SIGNIN / DOCTOR_PRECHECK / DOCTOR_VACCINATE / DOCTOR_OBSERVE
    { pagePath: 'pages/index/index', text: '首页', icon: 'home' },
    { pagePath: 'pages/queue/index', text: '今日队列', icon: 'list' },
    { pagePath: 'pages/record/list', text: '接种记录', icon: 'document' },
    { pagePath: 'pages/mine/index', text: '我的', icon: 'person' }
  ],
  REGISTER_DOCTOR: [   // DOCTOR_REGISTER
    { pagePath: 'pages/index/index', text: '首页', icon: 'home' },
    { pagePath: 'pages/queue/register', text: '登记队列', icon: 'list' },
    { pagePath: 'pages/calling/index', text: '叫号管理', icon: 'sound' },
    { pagePath: 'pages/mine/index', text: '我的', icon: 'person' }
  ],
  STOCK_DOCTOR: [      // DOCTOR_STOCK
    { pagePath: 'pages/index/index', text: '首页', icon: 'home' },
    { pagePath: 'pages/stock/summary', text: '库存总览', icon: 'box' },
    { pagePath: 'pages/stock/batches', text: '批次管理', icon: 'bars' },
    { pagePath: 'pages/mine/index', text: '我的', icon: 'person' }
  ]
}
```

## 页面路由设计

### TabBar页面

| 路径 | 说明 | 可见角色 |
|------|------|---------|
| `pages/index/index` | 首页（角色动态入口） | 全部医生 |
| `pages/queue/signin` | 签到队列 | DOCTOR_SIGNIN |
| `pages/queue/precheck` | 预检队列 | DOCTOR_PRECHECK |
| `pages/queue/register` | 登记队列 | DOCTOR_REGISTER |
| `pages/queue/vaccinate` | 接种队列 | DOCTOR_VACCINATE |
| `pages/queue/observe` | 留观队列 | DOCTOR_OBSERVE |
| `pages/calling/index` | 叫号面板 | DOCTOR_REGISTER |
| `pages/stock/summary` | 库存总览 | DOCTOR_STOCK |
| `pages/stock/batches` | 批次列表 | DOCTOR_STOCK |
| `pages/record/list` | 接种记录 | 流程医生 |
| `pages/mine/index` | 个人中心 | 全部医生 |

### 非Tab页面

| 模块 | 路径 | 说明 |
|------|------|------|
| 签到 | `pages/process/signin-confirm` | 签到确认 |
| 预检 | `pages/process/precheck-assess` | 预检评估 |
| 登记 | `pages/process/register-process` | 登记处理 |
| 登记 | `pages/process/batch-switch` | 批次切换 |
| 接种 | `pages/process/vaccinate-process` | 接种执行 |
| 接种 | `pages/process/vaccinate-success` | 接种成功 |
| 留观 | `pages/process/observe-detail` | 留观详情监控 |
| 留观 | `pages/process/observe-finish` | 留观结束确认 |
| 留观 | `pages/process/adverse-report` | 不良反应上报 |
| 留观 | `pages/process/adverse-handle` | 不良反应处理 |
| 库存 | `pages/stock/batch-detail` | 批次详情 |
| 库存 | `pages/stock/transfer` | 库存调拨 |
| 库存 | `pages/stock/transfer-records` | 调拨记录 |
| 库存 | `pages/stock/alerts` | 预警列表 |
| 库存 | `pages/stock/dispose` | 批次销毁 |
| 记录 | `pages/record/detail` | 记录详情 |
| 记录 | `pages/record/child-history` | 儿童接种史 |
| 叫号 | `pages/calling/history` | 过号记录 |

### 404兜底

无效路由由 uni-app 框架自动跳转到 `pages/common/404`，显示"页面不存在"提示和返回首页按钮。

## API层设计

### `api/signin.js`

| 函数 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `executeSignin` | POST | `/api/v1/signin/execute` | 执行签到（appointmentId, idCard） |
| `getTodayList` | GET | `/api/v1/signin/today` | 今日预约列表（filter, date, page, size） |

### `api/precheck.js`

| 函数 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `getQueue` | GET | `/api/v1/precheck/queue` | 预检队列（date） |
| `executePrecheck` | POST | `/api/v1/precheck/execute` | 执行预检（体温/体重/身高/健康状态/过敏史/用药/病史/接种史） |

### `api/register.js`

| 函数 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `getQueue` | GET | `/api/v1/register/queue` | 登记队列（date） |
| `getBatches` | GET | `/api/v1/register/batches/{vaccineId}` | 可用批次（FEFO排序） |
| `executeRegister` | POST | `/api/v1/register/execute` | 执行登记（appointmentId） |
| `switchBatch` | POST | `/api/v1/register/{id}/switch-batch` | 切换批次（newBatchId） |

### `api/vaccinate.js`

| 函数 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `getQueue` | GET | `/api/v1/vaccinate/queue` | 接种队列（date） |
| `verifyInfo` | GET | `/api/v1/vaccinate/{id}/verify` | 核实接种信息 |
| `executeVaccinate` | POST | `/api/v1/vaccinate/execute` | 执行接种（appointmentId, injectionSite） |
| `getRecords` | GET | `/api/v1/vaccinate/records` | 接种记录列表（startDate, endDate, page, size） |
| `getChildRecords` | GET | `/api/v1/vaccinate/records/child/{id}` | 儿童接种史 |

### `api/observe.js`

| 函数 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `getQueue` | GET | `/api/v1/observe/queue` | 留观队列（date） |
| `getStatus` | GET | `/api/v1/observe/{injectionId}` | 留观状态（倒计时信息） |
| `finishObserve` | POST | `/api/v1/observe/{id}/finish` | 确认留观结束 |
| `reportAdverse` | POST | `/api/v1/observe/adverse/report` | 上报不良反应（reactionType, severity, description） |
| `handleAdverse` | POST | `/api/v1/observe/adverse/{id}/handle` | 处理不良反应（handleResult） |

### `api/stock.js`

| 函数 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `getSummary` | GET | `/api/v1/stock/summary` | 库存总览 |
| `getBatches` | GET | `/api/v1/stock/batches` | 批次列表（vaccineId, status, keyword, page, size） |
| `getBatchDetail` | GET | `/api/v1/stock/batches/{id}` | 批次详情 |
| `createTransfer` | POST | `/api/v1/stock/transfer` | 库存调拨（batchId, fromType, fromId, toType, toId, quantity, remark） |
| `getTransferRecords` | GET | `/api/v1/stock/transfer/records` | 调拨记录（page, size） |
| `disposeBatch` | POST | `/api/v1/stock/batches/{id}/dispose` | 批次销毁（disposeQuantity, disposeReason） |
| `getAlerts` | GET | `/api/v1/stock/alerts` | 预警列表（handled, page, size） |
| `handleAlert` | PUT | `/api/v1/stock/alerts/{id}/handle` | 处理预警 |

## Pinia Store 设计

### `useUserStore`（复用+扩展）

- State: `token`, `userInfo` (id, phone, realName, roles[], windowCode, windowName), `isLoggedIn`
- Getters:
  - `isFlowDoctor` — roles 包含 SIGNIN/PRECHECK/VACCINATE/OBSERVE
  - `isRegisterDoctor` — roles 包含 REGISTER
  - `isStockDoctor` — roles 包含 STOCK
  - `tabBarConfig` — 根据 getter 返回对应 TabBar 配置
- Actions: `login()`, `logout()`, `fetchProfile()`

### `useQueueStore`

- State: `currentQueue[]`, `pollingTimer`, `isPolling`, `lastRefreshTime`
- Actions:
  - `startPolling(fetchFn, interval=30000)` — 启动轮询，自动在 onShow 调用
  - `stopPolling()` — 停止轮询，在 onHide/onUnload 调用
  - `refreshQueue()` — 手动刷新

### `useCallingStore`

- State: `currentCalledItem`, `calledHistory[]`, `waitingCount`, `callTimer`
- Actions:
  - `callNext()` — 叫下一个号
  - `confirmArrival()` — 确认到达
  - `skipCurrent()` — 过号

## Hooks 设计

| Hook | 用途 | 核心逻辑 |
|------|------|---------|
| `useQueue` | 队列列表页 | 封装30s自动轮询、loading/error状态、下拉刷新、上拉加载 |
| `useCountdown` | 留观倒计时 | 每秒更新 remaining/progress/canFinish，页面离开自动清除 interval |
| `usePagination` | 通用分页 | page/size/total/hasMore/loadMore/reset（复用家长端） |
| `useCallingTimer` | 叫号倒计时 | 5分钟倒计时，归零触发超时确认弹窗 |

## 核心页面交互设计

### 签到队列页 (`pages/queue/signin.vue`)

- **顶部**：日期选择器（默认今天）+ 筛选Tab（全部/已签到/未签到）+ 搜索框（预约号/儿童姓名）
- **列表**：卡片式，每项显示预约号、儿童姓名、疫苗名称、预约时段、签到状态标签
- **操作**：点击未签到项 → 跳转签到确认页
- **轮询**：30秒自动刷新

### 签到确认页 (`pages/process/signin-confirm.vue`)

- **顶部**：返回按钮 + "签到确认"标题
- **预约信息卡片**：预约号、疫苗名称、预约日期时段
- **儿童信息卡片**：儿童姓名、性别、出生日期
- **身份核验**：身份证号输入框（必填，18位），自动与档案比对
- **底部**：取消 / 确认签到
- **签到成功**：Toast → 返回队列（自动刷新）

### 预检队列页 (`pages/queue/precheck.vue`)

- **顶部**：日期选择器 + 搜索框
- **列表**：卡片式，排队号、儿童姓名、疫苗名称、签到时间
- **排序**：按签到时间升序（先到先检）
- **操作**：点击 → 预检评估页

### 预检评估页 (`pages/process/precheck-assess.vue`)

- **顶部**：返回 + "预检评估" + 儿童姓名/疫苗摘要
- **体征录入区**：
  - 体温（必填，数字键盘，>37.3°C 红色警告）
  - 体重（选填，kg）、身高（选填，cm）
- **健康评估区**：
  - 健康状况（单选：良好/一般/较差）
  - 过敏史、近期用药、疾病史、近期接种史（文本，默认"无"）
- **预检结果**：通过 / 不通过（单选）
  - 不通过时展开原因（必填文本）
- **自动判定**：体温>37.3°C 自动标记不通过，弹出确认
- **底部**：取消 / 提交

### 登记队列页 (`pages/queue/register.vue`)

- **顶部**：日期选择器 + 筛选Tab（全部/待叫号/已叫号/已到达/已过号）+ 搜索框
- **列表**：卡片式，排队号（大字）、儿童姓名、疫苗名称、预检时间、登记状态标签
- **状态标签颜色**：待叫号-灰色、已叫号-蓝色、已到达-橙色、已过号-红色、已完成-绿色
- **操作**：点击 → 登记处理页

### 登记处理页 (`pages/process/register-process.vue`)

- **顶部**：返回 + "登记处理" + 排队号
- **信息核实区**（4张卡片，每张带勾选确认框）：
  1. 预约信息：预约号、日期时段、疫苗名称
  2. 儿童信息：姓名、性别、出生日期、身份证号（脱敏）
  3. 预检结果：预检时间、体温、健康状况、评估结果
  4. 批次分配（自动FEFO）：批次号、厂家、有效期、可用库存 + "更换批次"按钮
- **提交校验**：所有信息卡片必须勾选确认
- **提交后**：自动FEFO选批 + 锁定库存 + 生成排队号，成功显示排队号和等候人数

### 叫号管理页 (`pages/calling/index.vue`)

- **当前叫号卡片**（顶部大卡片）：
  - 当前排队号（超大字号）、儿童姓名、疫苗名称
  - 叫号时间、等待时长、5分钟倒计时
  - 3个操作按钮：**确认到达** / **过号** / **跳过**

- **等候队列列表**（下方）：
  - 排队号、儿童姓名、疫苗、状态标签
  - 已到达排在最前

- **叫号操作**：
  - **叫下一个**：取出下一个待叫号项 → 已叫号 → 启动5分钟倒计时
  - **确认到达**：→ 已到达，进入接种等待
  - **过号**：→ 已过号，call_count+1，提示剩余机会
    - 3次过号：自动取消，释放库存，Toast"已3次过号，自动取消登记"
  - **5分钟超时**：倒计时归零触发确认弹窗，确认后自动过号

- **自动轮询**：10秒刷新队列状态

### 批次切换页 (`pages/process/batch-switch.vue`)

- **当前批次信息**（只读）：批次号、厂家、有效期、可用库存
- **可选批次列表**：按有效期升序（FEFO），单选，选中高亮
- **底部**：取消 / 确认切换
- **逻辑**：释放旧批次锁定 → 锁定新批次

### 接种队列页 (`pages/queue/vaccinate.vue`)

- **顶部**：日期选择器 + 搜索框
- **列表**：卡片式，排队号、儿童姓名、疫苗名称、批次号、登记时间
- **批次有效期**：即将过期时红色警告
- **操作**：点击 → 接种执行页

### 接种执行页 (`pages/process/vaccinate-process.vue`)

- **顶部**：返回 + "疫苗接种" + 排队号
- **信息核实区**（4张卡片，每张带勾选确认框）：
  1. 预约信息：预约号、日期时段、疫苗名称
  2. 儿童信息：姓名、性别、出生日期
  3. 预检结果：体温、健康状况
  4. 批次信息：批次号、厂家、有效期（已过期红色警告）、库存
- **注射部位选择**（4按钮组，必选）：左上臂 / 右上臂 / 左臀 / 右臀
- **温馨提示**：核对疫苗名称、确认儿童身份、检查有效期
- **底部**：取消 / **执行接种**（红色醒目）
- **二次确认弹窗**："确认对 [儿童姓名] 接种 [疫苗名称]？" → 确认后调用API（带X-Request-Id）

### 接种成功页 (`pages/process/vaccinate-success.vue`)

- **成功图标** + "接种完成"
- **接种信息卡片**：注射号、注射部位、批次号、接种时间
- **留观提醒卡片**（橙色背景）：
  - "请在留观区等待至少30分钟"
  - 预计可离开时间 = 接种时间 + 30分钟
  - 留观区位置提示
- **底部**：返回队列

### 留观队列页 (`pages/queue/observe.vue`)

- **顶部**：日期选择器
- **列表**：卡片式，儿童姓名、疫苗名称、注射号、接种时间
- **实时倒计时**：已观察 XX:XX / 需30分钟，进度条
  - 倒计时完成：进度条变绿 + "可结束留观"标签
- **操作按钮**：结束留观 / 上报不良反应
- **轮询**：5秒刷新

### 留观详情页 (`pages/process/observe-detail.vue`)

- **留观进度**：圆环进度条 + 已观察时间 / 最少30分钟
  - 不足30分钟：橙色，显示剩余时间
  - 满30分钟：绿色，"可以结束留观"
- **儿童信息卡片**：姓名、疫苗、接种时间
- **不良反应状态**：无 / 已上报（点击查看详情）
- **底部操作**：
  - 不足30分钟：仅"上报不良反应"可点
  - 满30分钟：**结束留观** + 上报不良反应

### 留观结束页 (`pages/process/observe-finish.vue`)

- **留观结果**：正常 / 异常（单选）
  - 异常时：必须已上报不良反应，否则提示"请先上报不良反应"
- **确认按钮**：提交后预约状态 → 已完成

### 不良反应上报页 (`pages/process/adverse-report.vue`)

- **基本信息**（只读）：儿童姓名、疫苗名称、注射号
- **反应类型**：下拉（局部反应/过敏反应/发热/其他）
- **严重程度**：单选（轻度/中度/重度）
- **发生时间**：日期时间选择器
- **详细描述**：多行文本
- **处理措施**：多行文本（选填）
- **处理结果**：下拉（好转/未好转/转诊）
- **底部**：取消 / 提交

### 不良反应处理页 (`pages/process/adverse-handle.vue`)

- **上报信息展示**（只读）：反应类型、严重程度、描述、发生时间
- **处理记录**：处理结果文本输入
- **底部**：取消 / 确认处理

### 库存总览页 (`pages/stock/summary.vue`)

- **顶部**：搜索框（疫苗名称）+ 筛选（全部/库存充足/库存预警/已过期）
- **统计卡片**（横向滚动）：总品种数、库存预警数、已过期批次、今日调拨数
- **列表**：卡片式，按剩余比例升序（低库存优先）
  - 疫苗名称、类型标签（一类/二类）
  - 总库存 / 可用 / 锁定
  - 剩余比例进度条（<20%红色、<50%橙色、>=50%绿色）
  - 使用率
- **操作**：点击 → 该疫苗批次列表

### 批次列表页 (`pages/stock/batches.vue`)

- **顶部**：搜索框（批次号）+ 筛选（全部/正常/即将过期/已过期/已销毁）
- **列表**：卡片式，按有效期升序（FEFO）
  - 批次号、疫苗名称、厂家、生产日期/有效期（已过期红色）
  - 可用库存 / 锁定库存、状态标签
- **操作**：点击 → 批次详情

### 批次详情页 (`pages/stock/batch-detail.vue`)

- **批次信息卡片**：批次号、疫苗名称、类型、厂家、生产日期、有效期、状态
- **库存信息卡片**：可用/锁定/总计、剩余比例、使用率
- **预警信息**（如有）：预警类型、详情、处理状态
- **操作按钮**：调拨 / 销毁 / 返回列表

### 库存调拨页 (`pages/stock/transfer.vue`)

- **批次信息**（只读）：批次号、疫苗名称、当前库存
- **调拨表单**：
  - 调出位置（类型下拉 + 名称下拉，显示可用库存）
  - 调入位置（类型下拉 + 名称下拉）
  - 调拨数量（数字输入，最大=调出可用库存）
  - 备注（选填）
- **校验**：调出≠调入、数量>0且<=可用库存
- **底部**：取消 / 确认调拨

### 调拨记录页 (`pages/stock/transfer-records.vue`)

- **筛选**：批次号搜索 + 日期范围
- **列表**：调拨单号、批次号、调出→调入、数量、时间、操作人
- **分页**：上拉加载更多

### 预警列表页 (`pages/stock/alerts.vue`)

- **筛选Tab**：全部 / 低库存 / 即将过期 / 已过期
- **列表**：批次号、疫苗名称、预警类型、详情、处理状态
- **操作**：未处理的显示"标记已处理"按钮

### 首页 (`pages/index/index.vue`)

- **顶部**：医生姓名 + 角色 + 今日日期
- **工作概览卡片**（根据角色动态显示）：
  - 流程医生：今日签到数、预检数、接种数、留观数
  - 登记医生：今日登记数、等候中、已过号
  - 库存医生：库存预警数、即将过期、今日调拨
- **快捷入口**（网格）：
  - 流程医生：签到队列、预检队列、接种记录
  - 登记医生：登记队列、叫号管理、过号记录
  - 库存医生：库存总览、批次管理、预警列表、调拨记录
- **公告轮播**：系统公告横向滚动
- **底部**：当前窗口信息（窗口号、窗口类型）

### 个人中心 (`pages/mine/index.vue`)

- **用户信息卡片**：姓名、角色、手机号（脱敏）、所属窗口
- **菜单列表**：修改密码、关于系统
- **退出登录**：二次确认弹窗

### 接种记录列表 (`pages/record/list.vue`)

- **筛选**：日期范围选择器
- **列表**：注射号、儿童姓名、疫苗名称、注射部位、批次号、接种时间
- **分页**：上拉加载更多
- **操作**：点击 → 记录详情

### 接种记录详情 (`pages/record/detail.vue`)

- 接种信息：注射号、注射部位、接种时间、接种医生
- 儿童信息：姓名、性别、出生日期
- 疫苗信息：疫苗名称、类型、批次号、厂家、有效期

### 儿童接种史 (`pages/record/child-history.vue`)

- **顶部**：儿童信息卡片（姓名、性别、出生日期）
- **列表**：该儿童全部接种记录，按时间倒序

## 状态颜色映射

### 预约状态（医生端视角）

| 状态码 | 状态名 | 颜色 | 典型场景 |
|--------|--------|------|---------|
| 1 | APPOINTED | #999999 灰色 | 签到队列中的未签到项 |
| 6 | SIGNED_IN | #1989fa 蓝色 | 预检队列 |
| 7 | PRECHECK_PASS | #07c160 绿色 | 登记队列 |
| 8 | REGISTERED | #ff9900 橙色 | 接种队列 |
| 9 | PRECHECK_FAIL | #ee0a24 红色 | 预检失败 |
| 10 | OBSERVING | #ff9900 橙色 | 留观队列 |
| 2 | COMPLETED | #07c160 绿色 | 已完成 |

### 排队项状态（登记叫号）

| 状态 | 颜色 |
|------|------|
| 待叫号 | #999999 灰色 |
| 已叫号 | #1989fa 蓝色 |
| 已到达 | #ff9900 橙色 |
| 已过号 | #ee0a24 红色 |
| 已完成 | #07c160 绿色 |

### 批次状态

| 状态 | 颜色 |
|------|------|
| 正常 | #07c160 绿色 |
| 即将过期 | #ff9900 橙色 |
| 已过期 | #ee0a24 红色 |
| 已销毁 | #999999 灰色 |

### 库存比例

| 比例 | 颜色 |
|------|------|
| >= 50% | #07c160 绿色 |
| 20% ~ 50% | #ff9900 橙色 |
| < 20% | #ee0a24 红色（预警） |

## 轮询策略

| 页面类型 | 轮询间隔 | 生命周期 | 说明 |
|----------|---------|---------|------|
| 签到/预检/登记/接种队列 | 30秒 | onShow启动，onHide停止 | useQueue hook |
| 留观队列 | 5秒 | onShow启动，onHide停止 | 倒计时精度要求高 |
| 留观详情 | 1秒 | 页面级，离开即停 | useCountdown hook |
| 叫号面板 | 10秒 | onShow启动，onHide停止 | 叫号时效性要求高 |
| 首页概览 | 60秒 | onShow启动 | 概览数据时效性低 |
| 库存页面 | 不自动轮询 | 手动下拉刷新 | 库存操作低频 |

## 叫号状态机

```
排队项状态流转：

0(待叫号) → [叫下一个] → 1(已叫号) → [确认到达] → 2(已到达) → 接种等待
                              ↓
                         [5分钟超时/手动过号]
                              ↓
                         3(已过号) → 返回队列末尾，call_count + 1
                              ↓
                         [call_count >= 3]
                              ↓
                         4(已取消) → 释放库存，预约回退到 status=8
```

## 业务约束

| 约束 | 说明 |
|------|------|
| 签到身份验证 | 身份证号18位，必须与儿童档案匹配 |
| 预检体温阈值 | >37.3°C 自动判定不通过 |
| 预检必填项 | 体温、健康状况 |
| 登记核实 | 4张信息卡片全部勾选才能提交 |
| FEFO策略 | 自动选最早过期批次，支持手动切换 |
| 接种部位必选 | 必须选择注射部位 |
| 接种二次确认 | 执行接种前弹窗确认 |
| 留观最短时间 | 30分钟，不足不可结束 |
| 异常留观 | 异常结束必须先上报不良反应 |
| 过号上限 | 3次过号自动取消，释放库存 |
| 过号超时 | 叫号后5分钟未确认自动过号 |
| 批次销毁原因 | 销毁操作必须填写原因 |
| 调拨校验 | 调出≠调入，数量<=可用库存 |
| 防重放 | 登记、接种、留观结束、调拨、销毁需 X-Request-Id |

## 按钮级权限控制

除按角色大类控制TabBar和页面入口外，关键操作按钮通过权限码做细粒度显隐控制：

| 页面 | 按钮 | 权限码 | 控制逻辑 |
|------|------|--------|---------|
| 签到确认 | 执行签到 | `appointment.signin` | `v-if="hasPermission('appointment.signin')"` |
| 预检评估 | 提交预检 | `precheck.assess` | `v-if="hasPermission('precheck.assess')"` |
| 登记处理 | 提交登记 | `register.save` | `v-if="hasPermission('register.save')"` |
| 接种执行 | 执行接种 | `vaccinate.execute` | `v-if="hasPermission('vaccinate.execute')"` |
| 留观详情 | 结束留观 | `observe.finish` | `v-if="hasPermission('observe.finish')"` |
| 不良反应 | 上报/处理 | `adverse.report` / `adverse.handle` | 按权限码控制 |

权限码判断通过 `useUserStore` 的 `userInfo.roles` 与权限码映射表比对实现。

## 错误处理策略

| 错误码 | 场景 | 处理方式 |
|--------|------|---------|
| 3002 | 身份证不匹配 | 弹窗提示"身份证号与儿童档案不匹配" |
| 3001/3003 | 状态异常 | Toast提示 + 返回队列页 |
| 6002 | 库存不足 | Toast"库存不足，请更换批次" |
| 6003 | 批次过期 | Toast"批次已过期，请更换批次" |
| 1005 | 防重放冲突 | Toast"请勿重复操作" |
| 5002 | 无可用批次 | Toast"暂无可用批次，请联系库存管理" |
| 1001 | Token无效/过期 | 跳转登录页，清除状态 |
| 1002 | 无权限 | Toast"无操作权限" |
| 网络异常 | 请求失败 | Toast"网络异常，请重试" + 手动刷新按钮 |

## 空状态处理

医生端所有队列列表页和记录列表页复用家长端 `EmptyState.vue` 组件：

| 页面 | 标题 | 描述 | 操作按钮 |
|------|------|------|----------|
| 签到队列 | 暂无签到记录 | 今日暂无待签到预约 | — |
| 预检队列 | 暂无预检记录 | 当前无待预检儿童 | — |
| 登记队列 | 暂无登记记录 | 当前无待登记儿童 | — |
| 接种队列 | 暂无接种记录 | 当前无待接种儿童 | — |
| 留观队列 | 暂无留观记录 | 当前无留观中儿童 | — |
| 接种记录 | 暂无接种记录 | 暂无接种记录 | — |
| 库存批次 | 暂无批次记录 | 当前筛选条件下无批次 | — |
| 调拨记录 | 暂无调拨记录 | 暂无调拨操作记录 | — |
| 库存预警 | 暂无预警 | 当前无库存预警 | — |

## 可复用组件清单

| 组件 | 用途 | 使用页面 |
|------|------|---------|
| `QueueCard.vue` | 通用队列卡片（排队号/姓名/疫苗/时间/状态标签/操作按钮） | 所有队列列表页 |
| `StatusTag.vue` | 状态标签（颜色映射） | 所有列表页 |
| `InfoCard.vue` | 信息卡片（标题+字段列表+可选勾选框） | 所有操作确认页 |
| `CountdownTimer.vue` | 倒计时组件（圆环/进度条+数字） | 留观队列/详情、叫号面板 |
| `VerifyCheckbox.vue` | 核实确认勾选 | 登记处理、接种执行 |
| `CustomTabBar.vue` | 自定义TabBar（动态显示） | 所有TabBar页面 |
