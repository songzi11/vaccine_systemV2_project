# 医生端APP前端实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建疫苗管理系统V2医生端APP，支持6个医生角色（签到/预检/登记/接种/留观/库存），包含签到、预检、登记（含叫号）、接种、留观（含不良反应）、库存管理、接种记录等完整功能。

**Architecture:** 基于 `前端共享基础设施实施计划` 的共享层，仅包含医生端业务页面。API层复用 request.js，认证复用 auth.js，用户状态复用 store/user.js，路由和 TabBar 复用 pages.json 和 CustomTabBar。Pinia 管理业务状态（useQueueStore/useCallingStore），API层按医生模块拆分，Hooks 封装队列轮询和倒计时逻辑。

**设计文档：** `docs/superpowers/specs/医生端APP前端设计.md`

**前置条件：** 前端共享基础设施实施计划已完成（request.js/auth.js/constants.js/store/user.js/hooks/usePagination.js/hooks/useAuth.js/components/EmptyState/StatusTag/CustomTabBar/App.vue/pages.json/api/auth.js/pages/auth/login.vue/pages/common/404.vue 均已实现）

---

## 范围说明

本项目拆分为5个独立阶段（Phase），每个阶段产出可独立验证的软件：

| Phase | 内容 | 依赖 |
|-------|------|------|
| Phase 1 | API层 + Store + Hooks + 业务组件（6个API模块、2个Store、3个Hook、5个组件） | 共享基础设施 |
| Phase 2 | 流程模块（签到、预检、接种队列+操作页、接种记录） | Phase 1 |
| Phase 3 | 登记+叫号模块（登记队列、登记处理、批次切换、叫号面板、过号记录） | Phase 1 |
| Phase 4 | 留观模块（留观队列、留观详情、留观结束、不良反应上报/处理） | Phase 1 |
| Phase 5 | 库存模块（库存总览、批次管理、调拨、预警、销毁） | Phase 1 |

本计划文件包含全部5个Phase的完整任务。每个Phase可独立执行和验证。

---

## Phase 1: API层 + Store + Hooks + 业务组件

> **注意：** 本计划依赖 `前端共享基础设施实施计划`。以下基础设施已由共享层提供，不再重复实现：request.js、auth.js、constants.js、store/user.js、hooks/usePagination.js、hooks/useAuth.js、components/EmptyState、components/StatusTag、components/CustomTabBar、App.vue、pages.json、api/auth.js、pages/auth/login.vue、pages/common/404.vue。

### Task 1.1: 清除Demo内容，重建项目脚手架

**Files:**
- Delete: `pages/` 目录下所有demo页面（保留空目录结构）
- Delete: `store/counter.js`, `store/index.js`（将重建）
- Delete: `common/`, `components/`, `windows/`, `hybrid/`, `wxcomponents/`（demo内容）
- Rewrite: `main.js`
- Rewrite: `App.vue`
- Rewrite: `pages.json`
- Rewrite: `uni.scss`
- Create: `utils/request.js`
- Create: `utils/auth.js`
- Create: `utils/constants.js`
- Create: `utils/tabBar.js`
- Create: `store/index.js`（Pinia实例）
- Create: `store/user.js`
- Create: `store/queue.js`
- Create: `store/calling.js`
- Create: `api/auth.js`

- [ ] **Step 1: 删除所有demo页面和组件**

```bash
# 删除demo页面（保留pages/目录）
rm -rf pages/tabBar pages/component pages/API pages/template pages/about
rm -rf store/counter.js store/index.js
rm -rf common/ components/ windows/ hybrid/ wxcomponents/
```

- [ ] **Step 2: 重写 main.js**

```js
// main.js
import { createSSRApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'

export function createApp() {
  const app = createSSRApp(App)
  const pinia = createPinia()
  app.use(pinia)
  return { app }
}
```

- [ ] **Step 3: 重写 App.vue**

```vue
<!-- App.vue -->
<script>
export default {
  onLaunch() {
    // 检查登录状态
    const token = uni.getStorageSync('token')
    if (!token) {
      uni.reLaunch({ url: '/pages/auth/login' })
    }
  },
  onShow() {},
  onHide() {}
}
</script>

<style lang="scss">
@import '@/uni.scss';
page {
  background-color: $color-bg-page;
  font-family: $font-family;
  font-size: $font-size-base;
  color: $color-text-primary;
}
</style>
```

- [ ] **Step 4: 重写 pages.json**

```json
{
  "easycom": {
    "autoscan": true,
    "custom": {
      "^uni-(.*)": "@dcloudio/uni-ui/lib/uni-$1/uni-$1.vue"
    }
  },
  "pages": [
    { "path": "pages/auth/login", "style": { "navigationBarTitleText": "登录", "navigationStyle": "custom" } },
    { "path": "pages/index/index", "style": { "navigationBarTitleText": "首页" } },
    { "path": "pages/mine/index", "style": { "navigationBarTitleText": "我的" } },
    { "path": "pages/queue/signin", "style": { "navigationBarTitleText": "签到队列" } },
    { "path": "pages/queue/precheck", "style": { "navigationBarTitleText": "预检队列" } },
    { "path": "pages/queue/register", "style": { "navigationBarTitleText": "登记队列" } },
    { "path": "pages/queue/vaccinate", "style": { "navigationBarTitleText": "接种队列" } },
    { "path": "pages/queue/observe", "style": { "navigationBarTitleText": "留观队列" } },
    { "path": "pages/calling/index", "style": { "navigationBarTitleText": "叫号管理" } },
    { "path": "pages/calling/history", "style": { "navigationBarTitleText": "过号记录" } },
    { "path": "pages/stock/summary", "style": { "navigationBarTitleText": "库存总览" } },
    { "path": "pages/stock/batches", "style": { "navigationBarTitleText": "批次管理" } },
    { "path": "pages/stock/batch-detail", "style": { "navigationBarTitleText": "批次详情" } },
    { "path": "pages/stock/transfer", "style": { "navigationBarTitleText": "库存调拨" } },
    { "path": "pages/stock/transfer-records", "style": { "navigationBarTitleText": "调拨记录" } },
    { "path": "pages/stock/alerts", "style": { "navigationBarTitleText": "预警列表" } },
    { "path": "pages/stock/dispose", "style": { "navigationBarTitleText": "批次销毁" } },
    { "path": "pages/process/signin-confirm", "style": { "navigationBarTitleText": "签到确认" } },
    { "path": "pages/process/precheck-assess", "style": { "navigationBarTitleText": "预检评估" } },
    { "path": "pages/process/register-process", "style": { "navigationBarTitleText": "登记处理" } },
    { "path": "pages/process/batch-switch", "style": { "navigationBarTitleText": "批次切换" } },
    { "path": "pages/process/vaccinate-process", "style": { "navigationBarTitleText": "疫苗接种" } },
    { "path": "pages/process/vaccinate-success", "style": { "navigationBarTitleText": "接种完成", "navigationStyle": "custom" } },
    { "path": "pages/process/observe-detail", "style": { "navigationBarTitleText": "留观监控" } },
    { "path": "pages/process/observe-finish", "style": { "navigationBarTitleText": "留观结束" } },
    { "path": "pages/process/adverse-report", "style": { "navigationBarTitleText": "不良反应上报" } },
    { "path": "pages/process/adverse-handle", "style": { "navigationBarTitleText": "不良反应处理" } },
    { "path": "pages/record/list", "style": { "navigationBarTitleText": "接种记录" } },
    { "path": "pages/record/detail", "style": { "navigationBarTitleText": "记录详情" } },
    { "path": "pages/record/child-history", "style": { "navigationBarTitleText": "儿童接种史" } }
  ],
  "globalStyle": {
    "navigationBarTextStyle": "black",
    "navigationBarTitleText": "疫苗管理系统",
    "navigationBarBackgroundColor": "#FFFFFF",
    "backgroundColor": "#F5F5F5"
  },
  "tabBar": {
    "custom": true,
    "color": "#999999",
    "selectedColor": "#07C160",
    "borderStyle": "white",
    "backgroundColor": "#FFFFFF",
    "list": [
      { "pagePath": "pages/index/index", "text": "首页" },
      { "pagePath": "pages/mine/index", "text": "我的" }
    ]
  }
}
```

> **说明：** `"tabBar": { "custom": true }` 启用自定义TabBar模式。`tabBar.list` 中的页面是必须的占位配置，实际显示由 CustomTabBar 组件根据角色动态控制。

- [ ] **Step 5: 重写 uni.scss**

从家长端设计文档复制完整变量定义（配色、字体、间距、圆角、阴影），文件内容见 `docs/superpowers/specs/2026-04-04-parent-app-frontend-design.md` 中"uni.scss 变量汇总"章节。增加医生端特有变量：

```scss
// 医生端特有
$color-calling: #1989FA;       // 叫号中
$color-arrived: #FF9900;       // 已到达
$color-skipped: #EE0A24;       // 已过号
$color-expiring: #FF9900;      // 即将过期
$color-expired: #EE0A24;       // 已过期
$color-disposed: #999999;      // 已销毁
```

- [ ] **Step 6: 创建 utils/request.js**

HTTP请求封装，JWT拦截器，X-Request-Id防重放。完整实现：

```js
// utils/request.js
const BASE_URL = 'http://localhost:8080'

function generateRequestId() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
    const r = Math.random() * 16 | 0
    return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16)
  })
}

const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')
    const header = {
      'Content-Type': 'application/json',
      ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    }
    // 写操作自动添加 X-Request-Id
    if (options.method && ['POST', 'PUT', 'DELETE'].includes(options.method.toUpperCase())) {
      header['X-Request-Id'] = generateRequestId()
    }

    if (options.showLoading) {
      uni.showLoading({ title: options.loadingText || '加载中...', mask: true })
    }

    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data,
      header,
      success: (res) => {
        if (options.showLoading) uni.hideLoading()
        if (res.statusCode === 401) {
          // Token过期，清除登录状态并跳转
          uni.removeStorageSync('token')
          uni.reLaunch({ url: '/pages/auth/login' })
          reject(new Error('登录已过期'))
          return
        }
        if (res.statusCode === 403) {
          uni.showToast({ title: '无操作权限', icon: 'none' })
          reject(new Error('无权限'))
          return
        }
        const data = res.data
        if (data.code === 200) {
          resolve(data.data)
        } else {
          uni.showToast({ title: data.message || '操作失败', icon: 'none' })
          reject(new Error(data.message))
        }
      },
      fail: (err) => {
        if (options.showLoading) uni.hideLoading()
        uni.showToast({ title: '网络异常，请重试', icon: 'none' })
        reject(err)
      }
    })
  })
}

export const get = (url, data, options) => request({ url, method: 'GET', data, ...options })
export const post = (url, data, options) => request({ url, method: 'POST', data, ...options })
export const put = (url, data, options) => request({ url, method: 'PUT', data, ...options })
export const del = (url, data, options) => request({ url, method: 'DELETE', data, ...options })
export default request
```

- [ ] **Step 7: 创建 utils/auth.js**

```js
// utils/auth.js
const TOKEN_KEY = 'token'
const USER_INFO_KEY = 'userInfo'

export function getToken() {
  return uni.getStorageSync(TOKEN_KEY)
}

export function setToken(token) {
  uni.setStorageSync(TOKEN_KEY, token)
}

export function removeToken() {
  uni.removeStorageSync(TOKEN_KEY)
}

export function isLoggedIn() {
  return !!getToken()
}

export function getUserInfo() {
  const info = uni.getStorageSync(USER_INFO_KEY)
  return info ? JSON.parse(info) : null
}

export function setUserInfo(info) {
  uni.setStorageSync(USER_INFO_KEY, JSON.stringify(info))
}

export function removeUserInfo() {
  uni.removeStorageSync(USER_INFO_KEY)
}

export function logout() {
  removeToken()
  removeUserInfo()
  uni.reLaunch({ url: '/pages/auth/login' })
}
```

- [ ] **Step 8: 创建 utils/constants.js**

```js
// utils/constants.js

// 预约状态
export const APPOINTMENT_STATUS = {
  APPOINTED: 1,
  COMPLETED: 2,
  CANCELLED: 3,
  EXPIRED: 4,
  SIGNED_IN: 6,
  PRECHECK_PASS: 7,
  REGISTERED: 8,
  PRECHECK_FAIL: 9,
  OBSERVING: 10
}

export const APPOINTMENT_STATUS_TEXT = {
  1: '已预约',
  2: '已完成',
  3: '已取消',
  4: '已过期',
  6: '已签到',
  7: '预检通过',
  8: '已登记',
  9: '预检未通过',
  10: '留观中'
}

export const APPOINTMENT_STATUS_COLOR = {
  1: '#999999',
  2: '#07C160',
  3: '#999999',
  4: '#999999',
  6: '#1989FA',
  7: '#07C160',
  8: '#FF9900',
  9: '#EE0A24',
  10: '#FF9900'
}

// 排队项状态（登记叫号）
export const QUEUE_STATUS = {
  PENDING: 0,
  CALLED: 1,
  ARRIVED: 2,
  SKIPPED: 3,
  CANCELLED: 4
}

export const QUEUE_STATUS_TEXT = {
  0: '待叫号',
  1: '已叫号',
  2: '已到达',
  3: '已过号',
  4: '已取消'
}

export const QUEUE_STATUS_COLOR = {
  0: '#999999',
  1: '#1989FA',
  2: '#FF9900',
  3: '#EE0A24',
  4: '#999999'
}

// 批次状态
export const BATCH_STATUS = {
  NORMAL: 0,
  EXPIRING_SOON: 1,
  EXPIRED: 2,
  DISPOSED: 3
}

export const BATCH_STATUS_TEXT = {
  0: '正常',
  1: '即将过期',
  2: '已过期',
  3: '已销毁'
}

export const BATCH_STATUS_COLOR = {
  0: '#07C160',
  1: '#FF9900',
  2: '#EE0A24',
  3: '#999999'
}

// 注射部位
export const INJECTION_SITES = [
  { value: 'LEFT_UPPER_ARM', label: '左上臂' },
  { value: 'RIGHT_UPPER_ARM', label: '右上臂' },
  { value: 'LEFT_BUTTOCK', label: '左臀' },
  { value: 'RIGHT_BUTTOCK', label: '右臀' }
]

// 健康状况
export const HEALTH_STATUS = [
  { value: 'GOOD', label: '良好' },
  { value: 'GENERAL', label: '一般' },
  { value: 'POOR', label: '较差' }
]

// 不良反应类型
export const ADVERSE_REACTION_TYPES = [
  { value: 'LOCAL_REACTION', label: '局部反应' },
  { value: 'ALLERGIC_REACTION', label: '过敏反应' },
  { value: 'FEVER', label: '发热' },
  { value: 'OTHER', label: '其他' }
]

// 严重程度
export const SEVERITY_LEVELS = [
  { value: 'MILD', label: '轻度' },
  { value: 'MODERATE', label: '中度' },
  { value: 'SEVERE', label: '重度' }
]

// 处理结果
export const HANDLE_RESULTS = [
  { value: 'RECOVERED', label: '好转' },
  { value: 'IMPROVED', label: '未好转' },
  { value: 'REFERRED', label: '转诊' }
]

// 角色分组
export const ROLE_GROUPS = {
  FLOW_DOCTOR: ['DOCTOR_SIGNIN', 'DOCTOR_PRECHECK', 'DOCTOR_VACCINATE', 'DOCTOR_OBSERVE'],
  REGISTER_DOCTOR: ['DOCTOR_REGISTER'],
  STOCK_DOCTOR: ['DOCTOR_STOCK']
}

// 轮询间隔
export const POLLING_INTERVAL = {
  QUEUE: 30000,
  OBSERVE_QUEUE: 5000,
  OBSERVE_DETAIL: 1000,
  CALLING: 10000,
  HOME: 60000
}

// 业务常量
export const OBSERVE_MIN_DURATION = 30 // 留观最短时间（分钟）
export const CALL_TIMEOUT = 5 // 叫号超时（分钟）
export const MAX_SKIP_COUNT = 3 // 最大过号次数
export const TEMPERATURE_THRESHOLD = 37.3 // 体温阈值
```

- [ ] **Step 9: 创建 utils/tabBar.js**

```js
// utils/tabBar.js
export const TAB_BAR_CONFIG = {
  FLOW_DOCTOR: [
    { pagePath: '/pages/index/index', text: '首页', icon: 'home' },
    { pagePath: '/pages/queue/signin', text: '今日队列', icon: 'list' },
    { pagePath: '/pages/record/list', text: '接种记录', icon: 'document' },
    { pagePath: '/pages/mine/index', text: '我的', icon: 'person' }
  ],
  REGISTER_DOCTOR: [
    { pagePath: '/pages/index/index', text: '首页', icon: 'home' },
    { pagePath: '/pages/queue/register', text: '登记队列', icon: 'list' },
    { pagePath: '/pages/calling/index', text: '叫号管理', icon: 'sound' },
    { pagePath: '/pages/mine/index', text: '我的', icon: 'person' }
  ],
  STOCK_DOCTOR: [
    { pagePath: '/pages/index/index', text: '首页', icon: 'home' },
    { pagePath: '/pages/stock/summary', text: '库存总览', icon: 'box' },
    { pagePath: '/pages/stock/batches', text: '批次管理', icon: 'bars' },
    { pagePath: '/pages/mine/index', text: '我的', icon: 'person' }
  ]
}
```

- [ ] **Step 10: 创建 Pinia Store**

**store/index.js:**
```js
import { createPinia } from 'pinia'
const pinia = createPinia()
export default pinia
```

**store/user.js:**
```js
// store/user.js
import { defineStore } from 'pinia'
import { post, get } from '@/utils/request'
import { getToken, setToken, removeToken, setUserInfo, removeUserInfo, getUserInfo } from '@/utils/auth'
import { ROLE_GROUPS, TAB_BAR_CONFIG } from '@/utils/constants'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    userInfo: getUserInfo() || {},
    isLoggedIn: !!getToken()
  }),

  getters: {
    roles: (state) => state.userInfo.roles || [],
    roleGroup(state) {
      const roles = this.roles
      if (roles.some(r => ROLE_GROUPS.FLOW_DOCTOR.includes(r))) return 'FLOW_DOCTOR'
      if (roles.some(r => ROLE_GROUPS.REGISTER_DOCTOR.includes(r))) return 'REGISTER_DOCTOR'
      if (roles.some(r => ROLE_GROUPS.STOCK_DOCTOR.includes(r))) return 'STOCK_DOCTOR'
      return 'FLOW_DOCTOR'
    },
    isFlowDoctor: (state) => {
      const roles = state.userInfo.roles || []
      return roles.some(r => ROLE_GROUPS.FLOW_DOCTOR.includes(r))
    },
    isRegisterDoctor: (state) => {
      const roles = state.userInfo.roles || []
      return roles.some(r => ROLE_GROUPS.REGISTER_DOCTOR.includes(r))
    },
    isStockDoctor: (state) => {
      const roles = state.userInfo.roles || []
      return roles.some(r => ROLE_GROUPS.STOCK_DOCTOR.includes(r))
    },
    tabBarConfig() {
      return TAB_BAR_CONFIG[this.roleGroup] || TAB_BAR_CONFIG.FLOW_DOCTOR
    },
    primaryRole: (state) => {
      const roles = state.userInfo.roles || []
      return roles[0] || ''
    }
  },

  actions: {
    async login(phone, password) {
      const data = await post('/public/auth/login', { phone, password }, { showLoading: true })
      this.token = data.token
      this.userInfo = { id: data.userId, phone: data.phone, roles: data.roles }
      this.isLoggedIn = true
      setToken(data.token)
      setUserInfo(this.userInfo)
      return data
    },
    logout() {
      this.token = ''
      this.userInfo = {}
      this.isLoggedIn = false
      removeToken()
      removeUserInfo()
      uni.reLaunch({ url: '/pages/auth/login' })
    },
    async fetchProfile() {
      const data = await get('/user/profile')
      this.userInfo = { ...this.userInfo, ...data }
      setUserInfo(this.userInfo)
      return data
    }
  }
})
```

**store/queue.js:**
```js
// store/queue.js
import { defineStore } from 'pinia'

export const useQueueStore = defineStore('queue', {
  state: () => ({
    currentQueue: [],
    pollingTimer: null,
    isPolling: false,
    lastRefreshTime: null
  }),

  actions: {
    startPolling(fetchFn, interval = 30000) {
      if (this.isPolling) return
      this.isPolling = true
      this.refreshQueue(fetchFn)
      this.pollingTimer = setInterval(() => {
        this.refreshQueue(fetchFn)
      }, interval)
    },

    stopPolling() {
      if (this.pollingTimer) {
        clearInterval(this.pollingTimer)
        this.pollingTimer = null
      }
      this.isPolling = false
    },

    async refreshQueue(fetchFn) {
      try {
        this.currentQueue = await fetchFn()
        this.lastRefreshTime = new Date()
      } catch (e) {
        console.error('队列刷新失败', e)
      }
    }
  }
})
```

**store/calling.js:**
```js
// store/calling.js
import { defineStore } from 'pinia'

export const useCallingStore = defineStore('calling', {
  state: () => ({
    currentCalledItem: null,
    calledHistory: [],
    waitingCount: 0,
    callTimer: null,
    callRemainingSeconds: 0
  }),

  actions: {
    startCallTimer(onTimeout) {
      this.callRemainingSeconds = 5 * 60 // 5分钟
      this.callTimer = setInterval(() => {
        this.callRemainingSeconds--
        if (this.callRemainingSeconds <= 0) {
          this.stopCallTimer()
          if (onTimeout) onTimeout()
        }
      }, 1000)
    },

    stopCallTimer() {
      if (this.callTimer) {
        clearInterval(this.callTimer)
        this.callTimer = null
      }
      this.callRemainingSeconds = 0
    },

    setCurrentCalled(item) {
      this.currentCalledItem = item
    },

    clearCurrentCalled() {
      this.currentCalledItem = null
      this.stopCallTimer()
    }
  }
})
```

- [ ] **Step 11: 创建 api/auth.js**

```js
// api/auth.js
import { post } from '@/utils/request'

export function login(phone, password) {
  return post('/public/auth/login', { phone, password })
}

export function sendSmsCode(phone, type) {
  return post('/public/sms/send', { phone, type })
}
```

- [ ] **Step 12: 创建占位页面文件**

为 `pages.json` 中声明的所有路由创建占位 `.vue` 文件，每个文件包含最小模板：

```vue
<template>
  <view class="container">
    <text>页面标题</text>
  </view>
</template>

<script setup>
</script>

<style lang="scss" scoped>
.container {
  padding: $spacing-lg;
}
</style>
```

需要创建的占位文件（31个）：
`pages/auth/login.vue`, `pages/index/index.vue`, `pages/mine/index.vue`, `pages/queue/signin.vue`, `pages/queue/precheck.vue`, `pages/queue/register.vue`, `pages/queue/vaccinate.vue`, `pages/queue/observe.vue`, `pages/calling/index.vue`, `pages/calling/history.vue`, `pages/stock/summary.vue`, `pages/stock/batches.vue`, `pages/stock/batch-detail.vue`, `pages/stock/transfer.vue`, `pages/stock/transfer-records.vue`, `pages/stock/alerts.vue`, `pages/stock/dispose.vue`, `pages/process/signin-confirm.vue`, `pages/process/precheck-assess.vue`, `pages/process/register-process.vue`, `pages/process/batch-switch.vue`, `pages/process/vaccinate-process.vue`, `pages/process/vaccinate-success.vue`, `pages/process/observe-detail.vue`, `pages/process/observe-finish.vue`, `pages/process/adverse-report.vue`, `pages/process/adverse-handle.vue`, `pages/record/list.vue`, `pages/record/detail.vue`, `pages/record/child-history.vue`

- [ ] **Step 13: 验证项目启动**

Run: 在 HBuilderX 中运行到浏览器或微信开发者工具
Expected: 显示登录页占位页面，无报错

- [ ] **Step 14: Commit**

```bash
git add -A
git commit -m "feat: project scaffolding - clear demo, setup infrastructure layer"
```

---

### Task 1.2: 自定义TabBar组件

**Files:**
- Create: `components/CustomTabBar.vue`

- [ ] **Step 1: 实现 CustomTabBar 组件**

```vue
<!-- components/CustomTabBar.vue -->
<template>
  <view class="custom-tabbar" :style="{ paddingBottom: safeAreaBottom + 'px' }">
    <view
      v-for="(item, index) in tabs"
      :key="index"
      class="tabbar-item"
      :class="{ active: isActive(item.pagePath) }"
      @click="switchTab(item)"
    >
      <uni-icons :type="item.icon" :size="22" :color="isActive(item.pagePath) ? '#07C160' : '#999999'" />
      <text class="tabbar-text" :class="{ active: isActive(item.pagePath) }">{{ item.text }}</text>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const tabs = computed(() => userStore.tabBarConfig)

const safeAreaBottom = computed(() => {
  const sysInfo = uni.getSystemInfoSync()
  return sysInfo.safeAreaInsets?.bottom || 0
})

function isActive(pagePath) {
  const pages = getCurrentPages()
  if (!pages.length) return false
  const currentPage = pages[pages.length - 1]
  return currentPage.route === pagePath.replace(/^\//, '').replace(/\.vue$/, '')
}

function switchTab(item) {
  if (isActive(item.pagePath)) return
  uni.switchTab({ url: item.pagePath })
}
</script>

<style lang="scss" scoped>
.custom-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100rpx;
  background-color: #FFFFFF;
  border-top: 1rpx solid $color-border-light;
  display: flex;
  z-index: 999;
}

.tabbar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;

  &.active {
    .tabbar-text {
      color: #07C160;
    }
  }
}

.tabbar-text {
  font-size: 20rpx;
  color: #999999;
  margin-top: 4rpx;
}
</style>
```

- [ ] **Step 2: 在所有TabBar页面中引入CustomTabBar**

在每个TabBar页面（index, mine, queue/*, calling/index, stock/summary, stock/batches, record/list）的 template 末尾添加：

```vue
<CustomTabBar />
```

script 中添加：
```js
import CustomTabBar from '@/components/CustomTabBar.vue'
```

- [ ] **Step 3: 验证TabBar显示**

Run: 登录后查看首页底部是否显示TabBar
Expected: 根据登录角色显示对应Tab

- [ ] **Step 4: Commit**

```bash
git add components/CustomTabBar.vue pages/
git commit -m "feat: implement CustomTabBar with role-based dynamic tabs"
```

---

### Task 1.3: 登录页

**Files:**
- Rewrite: `pages/auth/login.vue`

- [ ] **Step 1: 实现登录页**

页面包含：LOGO + 标题 + 手机号输入 + 密码输入 + 登录按钮。登录成功后根据角色跳转首页。

```vue
<!-- pages/auth/login.vue -->
<template>
  <view class="login-page">
    <view class="login-header">
      <view class="logo">💉</view>
      <text class="app-name">疫苗管理系统</text>
      <text class="app-subtitle">医生端</text>
    </view>

    <view class="login-form">
      <uni-easyinput v-model="form.phone" placeholder="请输入手机号" type="number" maxlength="11" />
      <uni-easyinput v-model="form.password" placeholder="请输入密码" type="password" />

      <button class="login-btn" :loading="loading" @click="handleLogin">登 录</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const form = reactive({ phone: '', password: '' })

async function handleLogin() {
  if (!form.phone || !form.password) {
    uni.showToast({ title: '请输入手机号和密码', icon: 'none' })
    return
  }
  loading.value = true
  try {
    await userStore.login(form.phone, form.password)
    uni.switchTab({ url: '/pages/index/index' })
  } catch (e) {
    // 错误已在request拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background-color: #FFFFFF;
  padding: 0 $spacing-lg;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 120rpx;
}

.logo { font-size: 120rpx; }
.app-name { font-size: $font-size-xl; font-weight: 600; margin-top: $spacing-sm; }
.app-subtitle { font-size: $font-size-sm; color: $color-text-secondary; margin-top: $spacing-xs; }

.login-form {
  .login-btn {
    margin-top: 60rpx;
    background-color: $color-primary;
    color: #FFFFFF;
    border: none;
    border-radius: $radius-base;
    height: 88rpx;
    line-height: 88rpx;
    font-size: $font-size-lg;
  }
}
</style>
```

- [ ] **Step 2: 验证登录流程**

Run: 输入手机号密码，点击登录
Expected: 调用登录API，成功后跳转首页并显示对应角色的TabBar

- [ ] **Step 3: Commit**

```bash
git add pages/auth/login.vue
git commit -m "feat: implement doctor login page"
```

---

### Task 1.4: 首页

**Files:**
- Rewrite: `pages/index/index.vue`

- [ ] **Step 1: 实现首页**

根据角色动态显示工作概览和快捷入口。公告轮播使用 uni-swiper。

```vue
<!-- pages/index/index.vue -->
<template>
  <view class="home-page">
    <!-- 顶部用户信息 -->
    <view class="user-header">
      <view class="user-info">
        <text class="user-name">{{ userStore.userInfo.realName || userStore.userInfo.phone }}</text>
        <text class="user-role">{{ roleText }}</text>
      </view>
      <text class="today-date">{{ todayDate }}</text>
    </view>

    <!-- 工作概览 -->
    <view class="section">
      <text class="section-title">工作概览</text>
      <view class="stats-grid">
        <view v-for="stat in stats" :key="stat.label" class="stat-item">
          <text class="stat-value">{{ stat.value }}</text>
          <text class="stat-label">{{ stat.label }}</text>
        </view>
      </view>
    </view>

    <!-- 快捷入口 -->
    <view class="section">
      <text class="section-title">快捷入口</text>
      <view class="shortcut-grid">
        <view v-for="item in shortcuts" :key="item.label" class="shortcut-item" @click="navigateTo(item.url)">
          <text class="shortcut-icon">{{ item.icon }}</text>
          <text class="shortcut-label">{{ item.label }}</text>
        </view>
      </view>
    </view>

    <!-- 当前窗口 -->
    <view v-if="userStore.userInfo.windowName" class="section">
      <view class="window-info">
        <text class="window-label">当前窗口</text>
        <text class="window-name">{{ userStore.userInfo.windowName }}</text>
      </view>
    </view>

    <CustomTabBar />
  </view>
</template>

<script setup>
import { computed, onShow } from 'vue'
import { useUserStore } from '@/store/user'
import CustomTabBar from '@/components/CustomTabBar.vue'

const userStore = useUserStore()

const todayDate = computed(() => {
  const d = new Date()
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
})

const roleText = computed(() => {
  const map = {
    DOCTOR_SIGNIN: '签到医生',
    DOCTOR_PRECHECK: '预检医生',
    DOCTOR_REGISTER: '登记医生',
    DOCTOR_VACCINATE: '接种医生',
    DOCTOR_OBSERVE: '留观医生',
    DOCTOR_STOCK: '库存管理'
  }
  const role = userStore.primaryRole
  return map[role] || '医生'
})

const stats = computed(() => {
  if (userStore.isRegisterDoctor) {
    return [
      { label: '今日登记', value: 0 },
      { label: '等候中', value: 0 },
      { label: '已过号', value: 0 }
    ]
  }
  if (userStore.isStockDoctor) {
    return [
      { label: '库存预警', value: 0 },
      { label: '即将过期', value: 0 },
      { label: '今日调拨', value: 0 }
    ]
  }
  // 流程医生
  return [
    { label: '今日签到', value: 0 },
    { label: '今日预检', value: 0 },
    { label: '今日接种', value: 0 },
    { label: '留观中', value: 0 }
  ]
})

const shortcuts = computed(() => {
  if (userStore.isRegisterDoctor) {
    return [
      { icon: '📋', label: '登记队列', url: '/pages/queue/register' },
      { icon: '📢', label: '叫号管理', url: '/pages/calling/index' },
      { icon: '📝', label: '过号记录', url: '/pages/calling/history' }
    ]
  }
  if (userStore.isStockDoctor) {
    return [
      { icon: '📦', label: '库存总览', url: '/pages/stock/summary' },
      { icon: '💊', label: '批次管理', url: '/pages/stock/batches' },
      { icon: '⚠️', label: '预警列表', url: '/pages/stock/alerts' },
      { icon: '🔄', label: '调拨记录', url: '/pages/stock/transfer-records' }
    ]
  }
  // 流程医生
  return [
    { icon: '✅', label: '签到队列', url: '/pages/queue/signin' },
    { icon: '🔍', label: '预检队列', url: '/pages/queue/precheck' },
    { icon: '💉', label: '接种记录', url: '/pages/record/list' }
  ]
})

function navigateTo(url) {
  uni.navigateTo({ url })
}

onShow(() => {
  userStore.fetchProfile()
})
</script>

<style lang="scss" scoped>
.home-page {
  min-height: 100vh;
  padding: $spacing-lg;
  padding-bottom: 140rpx;
}

.user-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;
}

.user-info { display: flex; flex-direction: column; }
.user-name { font-size: $font-size-xl; font-weight: 600; }
.user-role { font-size: $font-size-sm; color: $color-text-secondary; margin-top: $spacing-xs; }
.today-date { font-size: $font-size-sm; color: $color-text-secondary; }

.section {
  margin-bottom: $spacing-lg;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: 600;
  margin-bottom: $spacing-base;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150rpx, 1fr));
  gap: $spacing-base;
}

.stat-item {
  background: $color-bg-card;
  border-radius: $radius-lg;
  padding: $spacing-base;
  text-align: center;
  box-shadow: $shadow-card;
}

.stat-value { font-size: 48rpx; font-weight: 600; color: $color-primary; }
.stat-label { font-size: $font-size-xs; color: $color-text-secondary; margin-top: $spacing-xs; display: block; }

.shortcut-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-base;
}

.shortcut-item {
  background: $color-bg-card;
  border-radius: $radius-lg;
  padding: $spacing-md $spacing-sm;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: $shadow-card;
}

.shortcut-icon { font-size: 48rpx; }
.shortcut-label { font-size: $font-size-sm; color: $color-text-regular; margin-top: $spacing-xs; }

.window-info {
  background: $color-bg-card;
  border-radius: $radius-lg;
  padding: $spacing-base;
  box-shadow: $shadow-card;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.window-label { font-size: $font-size-sm; color: $color-text-secondary; }
.window-name { font-size: $font-size-base; font-weight: 600; color: $color-primary; }
</style>
```

- [ ] **Step 2: Commit**

```bash
git add pages/index/index.vue
git commit -m "feat: implement role-based home page with stats and shortcuts"
```

---

### Task 1.5: 个人中心

**Files:**
- Rewrite: `pages/mine/index.vue`

- [ ] **Step 1: 实现个人中心**

```vue
<!-- pages/mine/index.vue -->
<template>
  <view class="mine-page">
    <view class="user-card">
      <view class="avatar">{{ avatarText }}</view>
      <view class="user-info">
        <text class="user-name">{{ userStore.userInfo.realName || '未设置' }}</text>
        <text class="user-phone">{{ maskedPhone }}</text>
        <text class="user-role">{{ roleText }}</text>
      </view>
    </view>

    <view class="menu-list">
      <uni-list>
        <uni-list-item title="修改密码" showArrow @click="navigateTo('/pages/auth/change-password')" />
      </uni-list>
    </view>

    <button class="logout-btn" @click="handleLogout">退出登录</button>

    <CustomTabBar />
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/store/user'
import CustomTabBar from '@/components/CustomTabBar.vue'

const userStore = useUserStore()

const avatarText = computed(() => {
  return (userStore.userInfo.realName || '?').charAt(0)
})

const maskedPhone = computed(() => {
  const phone = userStore.userInfo.phone || ''
  if (phone.length === 11) return phone.slice(0, 3) + '****' + phone.slice(7)
  return phone
})

const roleText = computed(() => {
  const map = {
    DOCTOR_SIGNIN: '签到医生', DOCTOR_PRECHECK: '预检医生',
    DOCTOR_REGISTER: '登记医生', DOCTOR_VACCINATE: '接种医生',
    DOCTOR_OBSERVE: '留观医生', DOCTOR_STOCK: '库存管理'
  }
  return map[userStore.primaryRole] || '医生'
})

function handleLogout() {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) userStore.logout()
    }
  })
}

function navigateTo(url) {
  uni.navigateTo({ url })
}
</script>

<style lang="scss" scoped>
.mine-page {
  min-height: 100vh;
  padding: $spacing-lg;
  padding-bottom: 140rpx;
}

.user-card {
  background: $color-bg-card;
  border-radius: $radius-lg;
  padding: $spacing-xl $spacing-lg;
  display: flex;
  align-items: center;
  margin-bottom: $spacing-lg;
  box-shadow: $shadow-card;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: $color-primary;
  color: #FFFFFF;
  font-size: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: $spacing-base;
}

.user-info { display: flex; flex-direction: column; }
.user-name { font-size: $font-size-lg; font-weight: 600; }
.user-phone { font-size: $font-size-sm; color: $color-text-secondary; margin-top: $spacing-xs; }
.user-role { font-size: $font-size-xs; color: $color-primary; margin-top: 4rpx; }

.logout-btn {
  margin-top: 80rpx;
  background: transparent;
  border: 1rpx solid $color-border;
  color: $color-text-secondary;
  border-radius: $radius-base;
  height: 88rpx;
  line-height: 88rpx;
  font-size: $font-size-base;
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add pages/mine/index.vue
git commit -m "feat: implement mine page with user info and logout"
```

---

### Task 1.6: 共享组件

**Files:**
- Create: `components/QueueCard.vue`
- Create: `components/StatusTag.vue`
- Create: `components/InfoCard.vue`
- Create: `components/CountdownTimer.vue`
- Create: `components/VerifyCheckbox.vue`

- [ ] **Step 1: 实现 StatusTag 组件**

```vue
<!-- components/StatusTag.vue -->
<template>
  <view class="status-tag" :style="{ backgroundColor: bgColor, color: textColor }">
    <text>{{ text }}</text>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  text: { type: String, default: '' },
  color: { type: String, default: '' }
})

const bgColor = computed(() => props.color ? props.color + '1A' : '#9999991A')
const textColor = computed(() => props.color || '#999999')
</script>

<style lang="scss" scoped>
.status-tag {
  display: inline-flex;
  align-items: center;
  height: 40rpx;
  padding: 0 $spacing-sm;
  border-radius: $radius-pill;
  font-size: $font-size-xs;
  font-weight: 500;
}
</style>
```

- [ ] **Step 2: 实现 QueueCard 组件**

```vue
<!-- components/QueueCard.vue -->
<template>
  <view class="queue-card" @click="$emit('click')">
    <view class="card-header">
      <text class="queue-no">{{ item.queueNo }}</text>
      <StatusTag :text="statusText" :color="statusColor" />
    </view>
    <view class="card-body">
      <text class="child-name">{{ item.childName }}</text>
      <text class="vaccine-name">{{ item.vaccineName }}</text>
    </view>
    <view class="card-footer">
      <text class="time-text">{{ timeText }}</text>
      <slot name="action" />
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import StatusTag from './StatusTag.vue'
import { APPOINTMENT_STATUS_TEXT, APPOINTMENT_STATUS_COLOR, QUEUE_STATUS_TEXT, QUEUE_STATUS_COLOR } from '@/utils/constants'

const props = defineProps({
  item: { type: Object, required: true },
  statusField: { type: String, default: 'status' },
  queueStatusField: { type: String, default: '' },
  timeField: { type: String, default: 'signinTime' }
})

defineEmits(['click'])

const statusText = computed(() => {
  if (props.queueStatusField && props.item[props.queueStatusField] !== undefined) {
    return QUEUE_STATUS_TEXT[props.item[props.queueStatusField]] || '未知'
  }
  return APPOINTMENT_STATUS_TEXT[props.item[props.statusField]] || '未知'
})

const statusColor = computed(() => {
  if (props.queueStatusField && props.item[props.queueStatusField] !== undefined) {
    return QUEUE_STATUS_COLOR[props.item[props.queueStatusField]] || '#999999'
  }
  return APPOINTMENT_STATUS_COLOR[props.item[props.statusField]] || '#999999'
})

const timeText = computed(() => {
  const time = props.item[props.timeField]
  return time || ''
})
</script>

<style lang="scss" scoped>
.queue-card {
  background: $color-bg-card;
  border-radius: $radius-lg;
  padding: $spacing-base;
  margin-bottom: $spacing-base;
  box-shadow: $shadow-card;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-sm;
}

.queue-no {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $color-text-primary;
}

.card-body {
  margin-bottom: $spacing-sm;
}

.child-name {
  font-size: $font-size-base;
  color: $color-text-primary;
  display: block;
}

.vaccine-name {
  font-size: $font-size-sm;
  color: $color-text-secondary;
  display: block;
  margin-top: 4rpx;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.time-text {
  font-size: $font-size-xs;
  color: $color-text-placeholder;
}
</style>
```

- [ ] **Step 3: 实现 InfoCard 组件**

```vue
<!-- components/InfoCard.vue -->
<template>
  <view class="info-card">
    <view class="card-header" @click="toggleVerify">
      <text class="card-title">{{ title }}</text>
      <view v-if="verifiable" class="verify-checkbox" :class="{ checked: verified }">
        <uni-icons v-if="verified" type="checkmarkempty" :size="14" color="#FFFFFF" />
      </view>
    </view>
    <view class="card-body">
      <view v-for="field in fields" :key="field.label" class="field-row">
        <text class="field-label">{{ field.label }}</text>
        <text class="field-value" :class="field.class">{{ field.value }}</text>
      </view>
    </view>
    <slot name="extra" />
  </view>
</template>

<script setup>
const props = defineProps({
  title: { type: String, required: true },
  fields: { type: Array, default: () => [] },
  verifiable: { type: Boolean, default: false },
  verified: { type: Boolean, default: false }
})

const emit = defineEmits(['update:verified'])

function toggleVerify() {
  if (props.verifiable) {
    emit('update:verified', !props.verified)
  }
}
</script>

<style lang="scss" scoped>
.info-card {
  background: $color-bg-card;
  border-radius: $radius-lg;
  padding: $spacing-base;
  margin-bottom: $spacing-base;
  box-shadow: $shadow-card;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-sm;
}

.card-title {
  font-size: $font-size-base;
  font-weight: 600;
  color: $color-text-primary;
}

.verify-checkbox {
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  border: 2rpx solid $color-border;
  display: flex;
  align-items: center;
  justify-content: center;

  &.checked {
    background: $color-primary;
    border-color: $color-primary;
  }
}

.card-body {
  background: $color-bg-page;
  border-radius: $radius-sm;
  padding: $spacing-sm $spacing-base;
}

.field-row {
  display: flex;
  justify-content: space-between;
  padding: 8rpx 0;
}

.field-label {
  font-size: $font-size-sm;
  color: $color-text-secondary;
}

.field-value {
  font-size: $font-size-sm;
  color: $color-text-primary;
}

.field-value.danger { color: $color-danger; }
.field-value.warning { color: $color-warning; }
</style>
```

- [ ] **Step 4: 实现 CountdownTimer 组件**

```vue
<!-- components/CountdownTimer.vue -->
<template>
  <view class="countdown">
    <view class="progress-bar">
      <view class="progress-fill" :style="{ width: progressPercent + '%', backgroundColor: barColor }" />
    </view>
    <text class="countdown-text" :style="{ color: barColor }">{{ displayText }}</text>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  elapsed: { type: Number, default: 0 },       // 已过时间（秒）
  total: { type: Number, default: 1800 },         // 总时间（秒），默认30分钟
  label: { type: String, default: '已观察' }
})

const progressPercent = computed(() => {
  return Math.min((props.elapsed / props.total) * 100, 100)
})

const barColor = computed(() => {
  if (progressPercent.value >= 100) return '#07C160'
  if (progressPercent.value >= 80) return '#FF9900'
  return '#1989FA'
})

const remaining = computed(() => Math.max(props.total - props.elapsed, 0))

const displayText = computed(() => {
  const mins = Math.floor(remaining.value / 60)
  const secs = remaining.value % 60
  return `${props.label} ${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')} / ${formatTime(props.total)}`
})

function formatTime(seconds) {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
}
</script>

<style lang="scss" scoped>
.countdown { width: 100%; }

.progress-bar {
  height: 12rpx;
  background: $color-border-light;
  border-radius: 6rpx;
  overflow: hidden;
  margin-bottom: $spacing-xs;
}

.progress-fill {
  height: 100%;
  border-radius: 6rpx;
  transition: width 1s linear, background-color 0.3s;
}

.countdown-text {
  font-size: $font-size-sm;
  text-align: center;
}
</style>
```

- [ ] **Step 5: 实现 EmptyState 组件**

```vue
<!-- components/EmptyState.vue -->
<template>
  <view class="empty-state">
    <text class="empty-icon">{{ icon }}</text>
    <text class="empty-title">{{ title }}</text>
    <text v-if="description" class="empty-desc">{{ description }}</text>
    <button v-if="actionText" class="empty-action" @click="$emit('action')">{{ actionText }}</button>
  </view>
</template>

<script setup>
defineProps({
  icon: { type: String, default: '📭' },
  title: { type: String, default: '暂无数据' },
  description: { type: String, default: '' },
  actionText: { type: String, default: '' }
})

defineEmits(['action'])
</script>

<style lang="scss" scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 0;
}

.empty-icon { font-size: 120rpx; margin-bottom: $spacing-base; }
.empty-title { font-size: $font-size-base; color: $color-text-secondary; }
.empty-desc { font-size: $font-size-sm; color: $color-text-placeholder; margin-top: $spacing-xs; }

.empty-action {
  margin-top: $spacing-md;
  background: $color-primary;
  color: #FFFFFF;
  border: none;
  border-radius: $radius-base;
  padding: 0 $spacing-xl;
  height: 72rpx;
  line-height: 72rpx;
  font-size: $font-size-sm;
}
</style>
```

- [ ] **Step 6: 实现 VerifyCheckbox 组件**

```vue
<!-- components/VerifyCheckbox.vue -->
<template>
  <view class="verify-checkbox" :class="{ checked: modelValue }" @tap="toggle">
    <view class="checkbox-icon">
      <text v-if="modelValue" class="iconfont">&#xe60a;</text>
      <view v-else class="checkbox-border"></view>
    </view>
    <text class="checkbox-label">{{ label }}</text>
  </view>
</template>

<script setup>
const props = defineProps({
  modelValue: { type: Boolean, default: false },
  label: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue'])

function toggle() {
  emit('update:modelValue', !props.modelValue)
}
</script>

<style lang="scss" scoped>
.verify-checkbox {
  display: flex;
  align-items: center;
  padding: $spacing-sm 0;

  &.checked {
    .checkbox-icon {
      background-color: $color-primary;
      border-color: $color-primary;
    }
  }

  .checkbox-icon {
    width: 40rpx;
    height: 40rpx;
    border: 2rpx solid $color-border;
    border-radius: $border-radius-sm;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: $spacing-sm;
    transition: all 0.2s;

    .iconfont {
      font-size: $font-size-md;
      color: #FFFFFF;
    }

    .checkbox-border {
      width: 20rpx;
      height: 20rpx;
      border: 2rpx solid $color-border-light;
    }
  }

  .checkbox-label {
    font-size: $font-size-base;
    color: $color-text-regular;
  }
}
</style>
```

- [ ] **Step 7: Commit**

```bash
git add components/
git commit -m "feat: implement shared components (StatusTag, QueueCard, InfoCard, CountdownTimer, VerifyCheckbox)"
```

---

### Task 1.7: Hooks

**Files:**
- Create: `hooks/useQueue.js`
- Create: `hooks/useCountdown.js`
- Create: `hooks/usePagination.js`
- Create: `hooks/useCallingTimer.js`

- [ ] **Step 1: 实现 useQueue hook**

```js
// hooks/useQueue.js
import { ref, onShow, onHide } from 'vue'
import { POLLING_INTERVAL } from '@/utils/constants'

export function useQueue(fetchFn, interval = POLLING_INTERVAL.QUEUE) {
  const list = ref([])
  const loading = ref(false)
  const error = ref(null)
  let timer = null

  async function refresh() {
    loading.value = true
    error.value = null
    try {
      list.value = await fetchFn()
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  function startPolling() {
    refresh()
    timer = setInterval(refresh, interval)
  }

  function stopPolling() {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
  }

  return { list, loading, error, refresh, startPolling, stopPolling }
}
```

- [ ] **Step 2: 实现 useCountdown hook**

```js
// hooks/useCountdown.js
import { ref, onUnmounted } from 'vue'

export function useCountdown(startTime) {
  const elapsed = ref(0)
  const canFinish = ref(false)
  const remaining = ref(0)
  let timer = null

  function start(totalSeconds) {
    stop()
    elapsed.value = 0
    canFinish.value = false
    const start = startTime ? new Date(startTime).getTime() : Date.now()
    timer = setInterval(() => {
      elapsed.value = Math.floor((Date.now() - start) / 1000)
      remaining.value = Math.max(totalSeconds - elapsed.value, 0)
      canFinish.value = elapsed.value >= totalSeconds
    }, 1000)
  }

  function stop() {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
  }

  onUnmounted(stop)

  return { elapsed, canFinish, remaining, start, stop }
}
```

- [ ] **Step 3: 实现 usePagination hook**

```js
// hooks/usePagination.js
import { ref } from 'vue'

export function usePagination(fetchFn, defaultSize = 20) {
  const list = ref([])
  const page = ref(1)
  const size = ref(defaultSize)
  const total = ref(0)
  const loading = ref(false)
  const hasMore = ref(true)

  async function loadData(reset = false) {
    if (loading.value) return
    if (!reset && !hasMore.value) return

    if (reset) {
      page.value = 1
      list.value = []
      hasMore.value = true
    }

    loading.value = true
    try {
      const data = await fetchFn(page.value, size.value)
      const records = data.records || data || []
      list.value = reset ? records : [...list.value, ...records]
      total.value = data.total || records.length
      hasMore.value = list.value.length < total.value
      page.value++
    } catch (e) {
      console.error('分页加载失败', e)
    } finally {
      loading.value = false
    }
  }

  function reset() {
    loadData(true)
  }

  return { list, loading, hasMore, total, loadData, reset }
}
```

- [ ] **Step 4: 实现 useCallingTimer hook**

```js
// hooks/useCallingTimer.js
import { ref, onUnmounted } from 'vue'
import { CALL_TIMEOUT } from '@/utils/constants'

export function useCallingTimer(onTimeout) {
  const remainingSeconds = ref(CALL_TIMEOUT * 60)
  const isRunning = ref(false)
  let timer = null

  function start() {
    stop()
    remainingSeconds.value = CALL_TIMEOUT * 60
    isRunning.value = true
    timer = setInterval(() => {
      remainingSeconds.value--
      if (remainingSeconds.value <= 0) {
        stop()
        if (onTimeout) onTimeout()
      }
    }, 1000)
  }

  function stop() {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
    isRunning.value = false
  }

  onUnmounted(stop)

  return { remainingSeconds, isRunning, start, stop }
}
```

- [ ] **Step 5: Commit**

```bash
git add hooks/
git commit -m "feat: implement hooks (useQueue, useCountdown, usePagination, useCallingTimer)"
```

---

## Phase 2: 流程模块（签到、预检、接种、记录）

### Task 2.1: 签到模块

**Files:**
- Create: `api/signin.js`
- Rewrite: `pages/queue/signin.vue`
- Rewrite: `pages/process/signin-confirm.vue`

- [ ] **Step 1: 创建 api/signin.js**

```js
// api/signin.js
import { get, post } from '@/utils/request'

export function getTodayList(params) {
  return get('/api/v1/signin/today', params)
}

export function executeSignin(data) {
  return post('/api/v1/signin/execute', data, { showLoading: true })
}
```

- [ ] **Step 2: 实现签到队列页**

页面结构：日期选择器 + 筛选Tab（全部/已签到/未签到）+ 搜索框 + 队列卡片列表 + 30秒轮询。点击未签到项跳转签到确认页。

```vue
<!-- pages/queue/signin.vue -->
<template>
  <view class="page">
    <!-- 顶部筛选 -->
    <view class="filter-bar">
      <uni-datetime-picker type="date" v-model="selectedDate" :clear-icon="false" @change="onDateChange">
        <view class="date-picker">
          <text>{{ formattedDate }}</text>
          <uni-icons type="bottom" :size="14" />
        </view>
      </uni-datetime-picker>
      <view class="filter-tabs">
        <view v-for="tab in filterTabs" :key="tab.value" class="filter-tab" :class="{ active: currentFilter === tab.value }" @click="currentFilter = tab.value">
          <text>{{ tab.label }}</text>
        </view>
      </view>
    </view>

    <!-- 搜索 -->
    <view class="search-bar">
      <uni-easyinput v-model="keyword" placeholder="搜索预约号/儿童姓名" @confirm="handleSearch" />
    </view>

    <!-- 队列列表 -->
    <view class="queue-list" v-if="filteredList.length > 0">
      <QueueCard v-for="item in filteredList" :key="item.appointmentId" :item="item" status-field="status" time-field="appointmentDate" @click="handleItemClick(item)" />
    </view>
    <EmptyState v-else icon="📋" title="暂无预约" description="今日暂无需要签到的预约" />

    <CustomTabBar />
  </view>
</template>

<script setup>
import { ref, computed, onShow, onHide } from 'vue'
import { getTodayList } from '@/api/signin'
import { useQueue } from '@/hooks/useQueue'
import { APPOINTMENT_STATUS } from '@/utils/constants'
import QueueCard from '@/components/QueueCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import CustomTabBar from '@/components/CustomTabBar.vue'

const selectedDate = ref(new Date().toISOString().slice(0, 10))
const currentFilter = ref('ALL')
const keyword = ref('')

const filterTabs = [
  { label: '全部', value: 'ALL' },
  { label: '已签到', value: 'SIGNED_IN' },
  { label: '未签到', value: 'NOT_ARRIVED' }
]

const fetchList = (params) => getTodayList({ date: selectedDate.value, filter: currentFilter.value, keyword: keyword.value, ...params })
const { list, loading, startPolling, stopPolling } = useQueue(() => fetchList())

const formattedDate = computed(() => {
  const d = new Date(selectedDate.value)
  return `${d.getMonth() + 1}月${d.getDate()}日`
})

const filteredList = computed(() => list.value)

function onDateChange() { /* 日期变更后轮询会自动刷新 */ }
function handleSearch() { /* 关键字变更后轮询会自动刷新 */ }

function handleItemClick(item) {
  if (item.status !== APPOINTMENT_STATUS.APPOINTED) {
    uni.showToast({ title: '该预约已签到', icon: 'none' })
    return
  }
  uni.navigateTo({ url: `/pages/process/signin-confirm?appointmentId=${item.appointmentId}&childName=${item.childName}&vaccineName=${item.vaccineName}` })
}

onShow(() => startPolling())
onHide(() => stopPolling())
</script>

<style lang="scss" scoped>
@import '@/uni.scss';
.page { min-height: 100vh; padding: $spacing-lg; padding-bottom: 140rpx; }
.filter-bar { margin-bottom: $spacing-base; }
.date-picker { display: flex; align-items: center; gap: $spacing-xs; font-size: $font-size-base; }
.filter-tabs { display: flex; gap: $spacing-sm; margin-top: $spacing-sm; }
.filter-tab {
  padding: 8rpx $spacing-base; border-radius: $radius-pill;
  font-size: $font-size-sm; background: $color-bg-card; color: $color-text-secondary;
  &.active { background: $color-primary-light; color: $color-primary; }
}
.search-bar { margin-bottom: $spacing-base; }
</style>
```

- [ ] **Step 3: 实现签到确认页**

```vue
<!-- pages/process/signin-confirm.vue -->
<template>
  <view class="page">
    <view class="nav-bar">
      <uni-icons type="back" :size="20" @click="uni.navigateBack()" />
      <text class="nav-title">签到确认</text>
    </view>

    <!-- 预约信息 -->
    <InfoCard title="预约信息" :fields="appointmentFields" />
    <!-- 儿童信息 -->
    <InfoCard title="儿童信息" :fields="childFields" />

    <!-- 身份核验 -->
    <view class="verify-section">
      <text class="section-label">身份核验 *</text>
      <uni-easyinput v-model="idCard" placeholder="请输入儿童身份证号（18位）" type="idcard" maxlength="18" />
    </view>

    <!-- 操作按钮 -->
    <view class="bottom-actions">
      <button class="btn-cancel" @click="uni.navigateBack()">取消</button>
      <button class="btn-confirm" :loading="submitting" @click="handleSubmit">确认签到</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { executeSignin } from '@/api/signin'
import InfoCard from '@/components/InfoCard.vue'

const props = defineProps({
  appointmentId: { type: [String, Number] },
  childName: String,
  vaccineName: String,
  appointmentDate: String,
  timeSlot: String,
  childGender: Number,
  childBirthDate: String,
  appointmentNo: String
})

// 从URL参数获取数据（uni-app页面传参方式）
const appointmentId = ref('')
const childName = ref('')
const vaccineName = ref('')
const appointmentDate = ref('')
const timeSlot = ref('')
const childGender = ref(0)
const childBirthDate = ref('')
const appointmentNo = ref('')
const idCard = ref('')
const submitting = ref(false)

// uni-app 全局生命周期（onLoad/onShow/onHide/onUnload）无需从 Vue 导入
onLoad((query) => {
  appointmentId.value = query.appointmentId
  childName.value = query.childName || ''
  vaccineName.value = query.vaccineName || ''
  appointmentDate.value = query.appointmentDate || ''
  timeSlot.value = query.timeSlot || ''
  appointmentNo.value = query.appointmentNo || ''
})

const appointmentFields = computed(() => [
  { label: '预约号', value: appointmentNo.value },
  { label: '疫苗', value: vaccineName.value },
  { label: '日期', value: appointmentDate.value },
  { label: '时段', value: timeSlot.value === 'AM' ? '上午' : '下午' }
])

const childFields = computed(() => [
  { label: '姓名', value: childName.value },
  { label: '性别', value: childGender.value === 1 ? '男' : '女' },
  { label: '出生日期', value: childBirthDate.value }
])

async function handleSubmit() {
  if (!idCard.value || idCard.value.length !== 18) {
    uni.showToast({ title: '请输入18位身份证号', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    await executeSignin({ appointmentId: Number(appointmentId.value), idCard: idCard.value })
    uni.showToast({ title: '签到成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) {
    // 错误已在request拦截器处理
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
@import '@/uni.scss';
.page { min-height: 100vh; padding: $spacing-lg; background: $color-bg-page; }
.nav-bar { display: flex; align-items: center; gap: $spacing-sm; margin-bottom: $spacing-lg; }
.nav-title { font-size: $font-size-lg; font-weight: 600; }
.verify-section { margin-bottom: $spacing-lg; }
.section-label { font-size: $font-size-base; font-weight: 600; margin-bottom: $spacing-sm; display: block; }
.bottom-actions { display: flex; gap: $spacing-base; margin-top: $spacing-xl; }
.btn-cancel {
  flex: 1; background: transparent; border: 1rpx solid $color-border;
  color: $color-text-secondary; border-radius: $radius-base; height: 88rpx; line-height: 88rpx;
}
.btn-confirm {
  flex: 2; background: $color-primary; color: #FFFFFF; border: none;
  border-radius: $radius-base; height: 88rpx; line-height: 88rpx;
}
</style>
```

- [ ] **Step 4: Commit**

```bash
git add api/signin.js pages/queue/signin.vue pages/process/signin-confirm.vue
git commit -m "feat: implement signin module (queue list + confirm page)"
```

---

### Task 2.2: 预检模块

**Files:**
- Create: `api/precheck.js`
- Rewrite: `pages/queue/precheck.vue`
- Rewrite: `pages/process/precheck-assess.vue`

- [ ] **Step 1: 创建 api/precheck.js**

```js
// api/precheck.js
import { get, post } from '@/utils/request'

export function getQueue(params) {
  return get('/api/v1/precheck/queue', params)
}

export function executePrecheck(data) {
  return post('/api/v1/precheck/execute', data, { showLoading: true })
}
```

- [ ] **Step 2: 实现预检队列页**

结构与签到队列类似：日期选择 + 搜索 + 卡片列表 + 30秒轮询。按签到时间升序。点击跳转预检评估页。

```vue
<!-- pages/queue/precheck.vue -->
<template>
  <view class="page">
    <view class="filter-bar">
      <uni-datetime-picker type="date" v-model="selectedDate" :clear-icon="false">
        <view class="date-picker">
          <text>{{ formattedDate }}</text>
          <uni-icons type="bottom" :size="14" />
        </view>
      </uni-datetime-picker>
    </view>
    <view class="search-bar">
      <uni-easyinput v-model="keyword" placeholder="搜索儿童姓名" />
    </view>
    <view class="queue-list" v-if="list.length > 0">
      <QueueCard v-for="item in list" :key="item.appointmentId" :item="item" status-field="status" time-field="signinTime" @click="goAssess(item)" />
    </view>
    <EmptyState v-else icon="🔍" title="暂无待预检" />
    <CustomTabBar />
  </view>
</template>

<script setup>
import { ref, computed, onShow, onHide } from 'vue'
import { getQueue } from '@/api/precheck'
import { useQueue } from '@/hooks/useQueue'
import QueueCard from '@/components/QueueCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import CustomTabBar from '@/components/CustomTabBar.vue'

const selectedDate = ref(new Date().toISOString().slice(0, 10))
const keyword = ref('')
const formattedDate = computed(() => {
  const d = new Date(selectedDate.value)
  return `${d.getMonth() + 1}月${d.getDate()}日`
})
const { list, startPolling, stopPolling } = useQueue(() => getQueue({ date: selectedDate.value }))

function goAssess(item) {
  uni.navigateTo({ url: `/pages/process/precheck-assess?appointmentId=${item.appointmentId}&childName=${item.childName}&vaccineName=${item.vaccineName}` })
}
onShow(() => startPolling())
onHide(() => stopPolling())
</script>

<style lang="scss" scoped>
@import '@/uni.scss';
.page { min-height: 100vh; padding: $spacing-lg; padding-bottom: 140rpx; }
.filter-bar { margin-bottom: $spacing-base; }
.date-picker { display: flex; align-items: center; gap: $spacing-xs; font-size: $font-size-base; }
.search-bar { margin-bottom: $spacing-base; }
</style>
```

- [ ] **Step 3: 实现预检评估页**

包含：体征录入（体温必填、>37.3°C警告）、健康评估（4项文本+状况单选）、预检结果（通过/不通过）、提交。

```vue
<!-- pages/process/precheck-assess.vue -->
<template>
  <view class="page">
    <view class="nav-bar">
      <uni-icons type="back" :size="20" @click="uni.navigateBack()" />
      <text class="nav-title">预检评估</text>
      <text class="nav-subtitle">{{ childName }} / {{ vaccineName }}</text>
    </view>

    <!-- 体征录入 -->
    <view class="section">
      <text class="section-title">体征录入</text>
      <view class="form-item">
        <text class="form-label">体温 * <text v-if="form.bodyTemperature > 37.3" class="temp-warning">（偏高！）</text></text>
        <uni-easyinput v-model="form.bodyTemperature" placeholder="请输入体温" type="digit" />
      </view>
      <view class="form-row">
        <view class="form-item half">
          <text class="form-label">体重（kg）</text>
          <uni-easyinput v-model="form.weight" placeholder="选填" type="digit" />
        </view>
        <view class="form-item half">
          <text class="form-label">身高（cm）</text>
          <uni-easyinput v-model="form.height" placeholder="选填" type="digit" />
        </view>
      </view>
    </view>

    <!-- 健康评估 -->
    <view class="section">
      <text class="section-title">健康评估</text>
      <view class="radio-group">
        <text class="form-label">健康状况 *</text>
        <view class="radio-options">
          <view v-for="opt in healthOptions" :key="opt.value" class="radio-item" :class="{ active: form.healthStatus === opt.value }" @click="form.healthStatus = opt.value">
            <text>{{ opt.label }}</text>
          </view>
        </view>
      </view>
      <view class="form-item">
        <text class="form-label">过敏史</text>
        <uni-easyinput v-model="form.allergyHistory" placeholder="无" />
      </view>
      <view class="form-item">
        <text class="form-label">近期用药</text>
        <uni-easyinput v-model="form.medicationRecent" placeholder="无" />
      </view>
      <view class="form-item">
        <text class="form-label">疾病史</text>
        <uni-easyinput v-model="form.diseaseHistory" placeholder="无" />
      </view>
      <view class="form-item">
        <text class="form-label">近期接种史</text>
        <uni-easyinput v-model="form.vaccinationRecent" placeholder="无" />
      </view>
    </view>

    <!-- 预检结果 -->
    <view class="section">
      <text class="section-title">预检结果 *</text>
      <view class="result-options">
        <view class="result-btn pass" :class="{ active: form.checkResult === 'PASS' }" @click="form.checkResult = 'PASS'">通过</view>
        <view class="result-btn fail" :class="{ active: form.checkResult === 'FAIL' }" @click="form.checkResult = 'FAIL'">不通过</view>
      </view>
      <view v-if="form.checkResult === 'FAIL'" class="form-item">
        <text class="form-label">不通过原因 *</text>
        <uni-easyinput v-model="form.failReason" type="textarea" placeholder="请输入不通过原因" />
      </view>
    </view>

    <!-- 操作 -->
    <view class="bottom-actions">
      <button class="btn-cancel" @click="uni.navigateBack()">取消</button>
      <button class="btn-confirm" :loading="submitting" @click="handleSubmit">提交评估</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { executePrecheck } from '@/api/precheck'
import { TEMPERATURE_THRESHOLD, HEALTH_STATUS } from '@/utils/constants'

const appointmentId = ref('')
const childName = ref('')
const vaccineName = ref('')
const submitting = ref(false)

const healthOptions = HEALTH_STATUS

const form = reactive({
  bodyTemperature: '',
  weight: '',
  height: '',
  healthStatus: 'GOOD',
  allergyHistory: '无',
  medicationRecent: '无',
  diseaseHistory: '无',
  vaccinationRecent: '无',
  checkResult: 'PASS',
  failReason: ''
})

// uni-app 全局生命周期（onLoad/onShow/onHide/onUnload）无需从 Vue 导入
onLoad((query) => {
  appointmentId.value = query.appointmentId
  childName.value = query.childName || ''
  vaccineName.value = query.vaccineName || ''
})

async function handleSubmit() {
  if (!form.bodyTemperature) {
    uni.showToast({ title: '请输入体温', icon: 'none' })
    return
  }
  const temp = parseFloat(form.bodyTemperature)
  if (temp > TEMPERATURE_THRESHOLD && form.checkResult !== 'FAIL') {
    uni.showModal({
      title: '体温异常',
      content: `当前体温 ${form.bodyTemperature}°C，超过 ${TEMPERATURE_THRESHOLD}°C 阈值，建议标记为不通过`,
      success: (res) => {
        if (res.confirm) {
          form.checkResult = 'FAIL'
        }
      }
    })
    return
  }
  if (form.checkResult === 'FAIL' && !form.failReason) {
    uni.showToast({ title: '请输入不通过原因', icon: 'none' })
    return
  }

  submitting.value = true
  try {
    await executePrecheck({
      appointmentId: Number(appointmentId.value),
      bodyTemperature: temp,
      weight: form.weight ? parseFloat(form.weight) : undefined,
      height: form.height ? parseFloat(form.height) : undefined,
      healthStatus: form.healthStatus,
      allergyHistory: form.allergyHistory,
      medicationRecent: form.medicationRecent,
      diseaseHistory: form.diseaseHistory,
      vaccinationRecent: form.vaccinationRecent
    })
    uni.showToast({ title: '评估完成', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) { /* handled by interceptor */ } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
@import '@/uni.scss';
.page { min-height: 100vh; padding: $spacing-lg; background: $color-bg-page; }
.nav-bar { display: flex; flex-direction: column; margin-bottom: $spacing-lg; }
.nav-title { font-size: $font-size-lg; font-weight: 600; }
.nav-subtitle { font-size: $font-size-sm; color: $color-text-secondary; margin-top: $spacing-xs; }
.section { background: $color-bg-card; border-radius: $radius-lg; padding: $spacing-base; margin-bottom: $spacing-base; }
.section-title { font-size: $font-size-base; font-weight: 600; margin-bottom: $spacing-base; }
.form-item { margin-bottom: $spacing-base; }
.form-label { font-size: $font-size-sm; color: $color-text-secondary; margin-bottom: $spacing-xs; display: block; }
.temp-warning { color: $color-danger; font-weight: 600; }
.form-row { display: flex; gap: $spacing-base; }
.half { flex: 1; }
.radio-options { display: flex; gap: $spacing-sm; margin-top: $spacing-xs; }
.radio-item {
  padding: 12rpx $spacing-base; border-radius: $radius-base;
  border: 1rpx solid $color-border; font-size: $font-size-sm;
  &.active { border-color: $color-primary; background: $color-primary-light; color: $color-primary; }
}
.result-options { display: flex; gap: $spacing-base; margin-bottom: $spacing-base; }
.result-btn {
  flex: 1; text-align: center; padding: 20rpx; border-radius: $radius-base;
  border: 1rpx solid $color-border; font-size: $font-size-base;
  &.pass.active { border-color: $color-success; background: rgba(7,193,96,0.1); color: $color-success; }
  &.fail.active { border-color: $color-danger; background: rgba(238,10,36,0.1); color: $color-danger; }
}
.bottom-actions { display: flex; gap: $spacing-base; margin-top: $spacing-lg; }
.btn-cancel { flex: 1; background: transparent; border: 1rpx solid $color-border; color: $color-text-secondary; border-radius: $radius-base; height: 88rpx; line-height: 88rpx; }
.btn-confirm { flex: 2; background: $color-primary; color: #FFF; border: none; border-radius: $radius-base; height: 88rpx; line-height: 88rpx; }
</style>
```

- [ ] **Step 4: Commit**

```bash
git add api/precheck.js pages/queue/precheck.vue pages/process/precheck-assess.vue
git commit -m "feat: implement precheck module (queue + assessment page with auto temperature check)"
```

---

### Task 2.3: 接种模块

**Files:**
- Create: `api/vaccinate.js`
- Rewrite: `pages/queue/vaccinate.vue`
- Rewrite: `pages/process/vaccinate-process.vue`
- Rewrite: `pages/process/vaccinate-success.vue`

- [ ] **Step 1: 创建 api/vaccinate.js**

```js
// api/vaccinate.js
import { get, post } from '@/utils/request'

export function getQueue(params) {
  return get('/api/v1/vaccinate/queue', params)
}

export function verifyInfo(appointmentId) {
  return get(`/vaccinate/${appointmentId}/verify`)
}

export function executeVaccinate(data) {
  return post('/api/v1/vaccinate/execute', data, { showLoading: true })
}

export function getRecords(params) {
  return get('/api/v1/vaccinate/records', params)
}

export function getChildRecords(childId) {
  return get(`/vaccinate/records/child/${childId}`)
}
```

- [ ] **Step 2: 实现接种队列页**

结构同签到/预检队列：日期+搜索+卡片列表+轮询。显示排队号、儿童、疫苗、批次号、登记时间。点击跳转接种执行页。

- [ ] **Step 3: 实现接种执行页**

4张InfoCard核实信息（预约/儿童/预检/批次），注射部位4按钮选择，二次确认弹窗，执行接种。接种成功跳转成功页。

- [ ] **Step 4: 实现接种成功页**

显示注射号、部位、批次号、接种时间，留观提醒卡片（30分钟），返回队列按钮。

- [ ] **Step 5: Commit**

```bash
git add api/vaccinate.js pages/queue/vaccinate.vue pages/process/vaccinate-process.vue pages/process/vaccinate-success.vue
git commit -m "feat: implement vaccinate module (queue, process with verify, success page)"
```

---

### Task 2.4: 接种记录模块

**Files:**
- Rewrite: `pages/record/list.vue`
- Rewrite: `pages/record/detail.vue`
- Rewrite: `pages/record/child-history.vue`

- [ ] **Step 1: 实现记录列表页**

日期范围筛选 + 分页卡片列表。每项显示注射号、儿童、疫苗、部位、批次、时间。

- [ ] **Step 2: 实现记录详情页**

接种信息（注射号、部位、时间、医生）、儿童信息、疫苗信息（名称、类型、批次、厂家、有效期）。

- [ ] **Step 3: 实现儿童接种史页**

顶部儿童信息卡片 + 该儿童全部接种记录列表（按时间倒序）。

- [ ] **Step 4: Commit**

```bash
git add pages/record/
git commit -m "feat: implement vaccination records (list, detail, child history)"
```

---

## Phase 3: 登记+叫号模块

### Task 3.1: 登记模块

**Files:**
- Create: `api/register.js`
- Rewrite: `pages/queue/register.vue`
- Rewrite: `pages/process/register-process.vue`
- Rewrite: `pages/process/batch-switch.vue`

- [ ] **Step 1: 创建 api/register.js**

```js
// api/register.js
import { get, post } from '@/utils/request'

export function getQueue(params) {
  return get('/api/v1/register/queue', params)
}

export function getBatches(vaccineId) {
  return get(`/register/batches/${vaccineId}`)
}

export function executeRegister(data) {
  return post('/api/v1/register/execute', data, { showLoading: true })
}

export function switchBatch(appointmentId, data) {
  return post(`/register/${appointmentId}/switch-batch`, data, { showLoading: true })
}
```

- [ ] **Step 2: 实现登记队列页**

日期+筛选Tab（全部/待叫号/已叫号/已到达/已过号）+搜索+卡片列表+30秒轮询。使用 `QUEUE_STATUS` 状态映射颜色。

- [ ] **Step 3: 实现登记处理页**

```vue
<!-- pages/process/register-process.vue -->
<template>
  <view class="register-process">
    <uni-nav-bar title="登记处理" :border="false" />

    <!-- 儿童信息 -->
    <InfoCard title="儿童信息" :fields="childFields" />

    <!-- 预检信息 -->
    <InfoCard title="预检结果" :fields="precheckFields" />

    <!-- 预约信息 -->
    <InfoCard title="预约信息" :fields="appointmentFields" />

    <!-- 批次信息（自动FEFO） -->
    <view class="batch-section">
      <view class="batch-header">
        <text class="batch-title">当前批次（FEFO自动选取）</text>
        <text class="batch-action" @tap="switchBatch">更换批次</text>
      </view>
      <InfoCard title="批次信息" :fields="batchFields" />
    </view>

    <!-- 4项核实勾选 -->
    <view class="verify-section">
      <text class="verify-title">请逐项核实以上信息</text>
      <VerifyCheckbox label="儿童信息与证件一致" v-model="verified.child" />
      <VerifyCheckbox label="预检结果已确认" v-model="verified.precheck" />
      <VerifyCheckbox label="预约信息正确" v-model="verified.appointment" />
      <VerifyCheckbox label="批次信息无误" v-model="verified.batch" />
    </view>

    <!-- 提交按钮 -->
    <view class="bottom-bar">
      <button class="btn-submit" :loading="submitting" :disabled="!allVerified" @tap="submitRegister">
        确认登记
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { getBatches, executeRegister } from '@/api/register.js'
import InfoCard from '@/components/InfoCard/InfoCard.vue'
import VerifyCheckbox from '@/components/VerifyCheckbox/VerifyCheckbox.vue'

const submitting = ref(false)
const verified = reactive({ child: false, precheck: false, appointment: false, batch: false })
const currentBatch = ref(null)

const childFields = ref([])
const precheckFields = ref([])
const appointmentFields = ref([])
const batchFields = ref([])

const allVerified = computed(() => Object.values(verified).every(Boolean))

// uni-app 全局生命周期（onLoad/onShow/onHide/onUnload）无需从 Vue 导入
onLoad(async (query) => {
  await Promise.all([loadChildInfo(query), loadPrecheckInfo(query), loadAppointmentInfo(query)])
  await autoSelectBatch(query.vaccineId)
})

async function loadChildInfo(query) {
  childFields.value = [
    { label: '儿童姓名', value: query.childName },
    { label: '性别', value: query.gender === 1 ? '男' : '女' },
    { label: '出生日期', value: query.birthDate }
  ]
}

async function loadPrecheckInfo(query) {
  precheckFields.value = [
    { label: '体温', value: query.bodyTemperature + '°C' },
    { label: '健康状况', value: query.healthStatus },
    { label: '预检时间', value: query.precheckTime }
  ]
}

async function loadAppointmentInfo(query) {
  appointmentFields.value = [
    { label: '疫苗', value: query.vaccineName },
    { label: '预约日期', value: query.appointmentDate },
    { label: '时段', value: query.timeSlot }
  ]
}

async function autoSelectBatch(vaccineId) {
  if (!vaccineId) return
  try {
    const batches = await getBatches(vaccineId)
    currentBatch.value = batches.find(b => b.availableStock > b.lockedStock) || batches[0] || null
    if (currentBatch.value) {
      batchFields.value = [
        { label: '批次号', value: currentBatch.value.batchNo },
        { label: '疫苗', value: currentBatch.value.vaccineName },
        { label: '厂家', value: currentBatch.value.manufacturer },
        { label: '有效期', value: currentBatch.value.expiryDate },
        { label: '可用库存', value: `${currentBatch.value.availableStock - currentBatch.value.lockedStock}` }
      ]
    }
  } catch (e) {
    console.error('获取批次信息失败', e)
  }
}

function switchBatch() {
  const vaccineId = appointmentFields.value.find(f => f.label === '疫苗')?.rawValue
  if (!vaccineId) return
  uni.navigateTo({ url: `/pages/process/batch-switch?vaccineId=${vaccineId}` })
}

async function submitRegister() {
  if (!allVerified.value) return
  submitting.value = true
  try {
    const result = await executeRegister({ appointmentId: uni.getStorageSync('registerAppointmentId') })
    uni.showToast({ title: `登记成功，排队号: ${result.queueNo}`, icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) {
    uni.showToast({ title: e.message || '登记失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.register-process {
  min-height: 100vh;
  background-color: $color-bg-page;
  padding: $spacing-lg;
  padding-bottom: 140rpx;

  .batch-section {
    margin-top: $spacing-md;

    .batch-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: $spacing-sm;

      .batch-title { font-size: $font-size-base; font-weight: $font-weight-bold; color: $color-text-primary; }
      .batch-action { font-size: $font-size-sm; color: $color-primary; }
    }
  }

  .verify-section {
    margin-top: $spacing-lg;
    padding: $spacing-md;
    background-color: $color-bg-white;
    border-radius: $border-radius-lg;
    border: 2rpx dashed $color-border;

    .verify-title { display: block; font-size: $font-size-base; font-weight: $font-weight-medium; color: $color-text-regular; margin-bottom: $spacing-md; }
  }

  .bottom-bar {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    padding: $spacing-md $spacing-lg;
    padding-bottom: calc(#{$spacing-md} + env(safe-area-inset-bottom));
    background-color: #FFFFFF;

    .btn-submit {
      width: 100%;
      height: 88rpx;
      line-height: 88rpx;
      background-color: $color-primary;
      color: #FFFFFF;
      font-size: $font-size-lg;
      border: none;
      border-radius: $border-radius-lg;

      &:disabled { opacity: 0.5; }
      &::after { border: none; }
    }
  }
}
</style>
```

- [ ] **Step 4: 实现批次切换页**

当前批次信息（只读）+ 可选批次列表（FEFO排序，单选）+ 确认切换。切换逻辑：释放旧批次锁定→锁定新批次。

- [ ] **Step 5: Commit**

```bash
git add api/register.js pages/queue/register.vue pages/process/register-process.vue pages/process/batch-switch.vue
git commit -m "feat: implement register module (queue, process with FEFO batch, batch switch)"
```

---

### Task 3.2: 叫号管理模块

**Files:**
- Rewrite: `pages/calling/index.vue`
- Rewrite: `pages/calling/history.vue`

- [ ] **Step 1: 实现叫号面板**

```vue
<!-- pages/calling/index.vue -->
<template>
  <view class="calling-page">
    <uni-nav-bar title="叫号管理" :border="false" />

    <!-- 当前叫号卡片 -->
    <view v-if="calledItem" class="called-card" :class="{ urgent: remainingSeconds < 60 }">
      <view class="called-info">
        <text class="called-name">{{ calledItem.childName }}</text>
        <text class="called-vaccine">{{ calledItem.vaccineName }}</text>
        <text class="called-queue">排队号: {{ calledItem.queueNo }}</text>
      </view>
      <view class="timer-section">
        <CountdownTimer :seconds="remainingSeconds" :urgent="remainingSeconds < 60" />
        <text class="timer-label">{{ remainingSeconds > 0 ? '等待确认到达' : '超时处理' }}</text>
      </view>
      <view class="called-actions">
        <button class="btn-arrive" @tap="confirmArrival">确认到达</button>
        <button class="btn-skip" @tap="skipCurrent">过号</button>
        <button class="btn-cancel" @tap="cancelCall">取消叫号</button>
      </view>
    </view>

    <!-- 候候队列 -->
    <view class="waiting-section">
      <view class="waiting-header">
        <text class="waiting-title">等候队列</text>
        <text class="waiting-count">共 {{ waitingList.length }} 人</text>
      </view>
      <view class="waiting-list">
        <QueueCard
          v-for="item in waitingList"
          :key="item.queueId"
          :item="item"
          @click="viewDetail(item)"
        />
        <EmptyState v-if="waitingList.length === 0" icon="list" title="暂无等候人员" />
      </view>
    </view>

    <!-- 过号记录入口 -->
    <view class="history-link">
      <text @tap="uni.navigateTo({ url: '/pages/calling/history' })">过号记录 →</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
// uni-app 全局生命周期（onShow/onHide）无需从 Vue 导入
import { useCallingStore } from '@/store/calling.js'
import { useCallingTimer } from '@/hooks/useCallingTimer.js'
import QueueCard from '@/components/QueueCard/QueueCard.vue'
import CountdownTimer from '@/components/CountdownTimer/CountdownTimer.vue'
import EmptyState from '@/components/EmptyState/EmptyState.vue'

const callingStore = useCallingStore()
const { remainingSeconds, isRunning, start, stop } = useCallingTimer()
const waitingList = ref([])

const calledItem = computed(() => callingStore.currentCalledItem)
const MAX_SKIP_COUNT = 3

// uni-app 全局生命周期无需从 Vue 导入
onShow(() => {
  refreshWaitingList()
  if (calledItem.value) {
    start(onTimeout)
  }
})

onHide(() => { stop() })

async function refreshWaitingList() {
  try {
    const { getRegisterQueue } = await import('@/api/register.js')
    const data = await getRegisterQueue({ date: undefined })
    waitingList.value = data.filter(i => i.status === 0 || i.status === 1)
  } catch (e) {
    console.error('刷新等候队列失败', e)
  }
}

function onTimeout() {
  uni.showModal({
    title: '超时提醒',
    content: `该患者已5分钟未确认到达，是否自动过号？(已过号${calledItem.value.callCount}次，${MAX_SKIP_COUNT - (calledItem.value.callCount || 0)}次后自动取消)`,
    success: () => skipCurrent()
  })
}

async function confirmArrival() {
  stop()
  try {
    await callingStore.confirmArrival()
    uni.showToast({ title: '已确认到达', icon: 'success' })
    await refreshWaitingList()
  } catch (e) {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

async function skipCurrent() {
  stop()
  try {
    const result = await callingStore.skipCurrent()
    if (result.cancelled) {
      uni.showToast({ title: `已过号${result.callCount}次，预约已自动取消`, icon: 'none' })
    } else {
      uni.showToast({ title: '已过号，已排至队尾', icon: 'success' })
    }
    await refreshWaitingList()
  } catch (e) {
    uni.showToast({ title: e.message || '操作失败', icon: 'none' })
  }
}

function cancelCall() {
  stop()
  uni.showToast({ title: '已取消叫号', icon: 'none' })
}

function viewDetail(item) {
  // 可查看排队项详情（扩展功能）
}
</script>

<style lang="scss" scoped>
.calling-page {
  min-height: 100vh;
  background-color: $color-bg-page;
  padding: $spacing-lg;

  .called-card {
    padding: $spacing-lg;
    background: #FFFFFF;
    border-radius: $border-radius-lg;
    box-shadow: $shadow-card;
    margin-bottom: $spacing-lg;
    border-left: 6rpx solid $color-warning;

    &.urgent { border-left-color: $color-danger; }

    .called-info {
      .called-name { font-size: $font-size-xl; font-weight: $font-weight-bold; color: $color-text-primary; }
      .called-vaccine { font-size: $font-size-base; color: $color-text-regular; margin-top: $spacing-xs; }
      .called-queue { font-size: $font-size-sm; color: $color-text-secondary; margin-top: $spacing-xs; }
    }

    .timer-section {
      display: flex;
      align-items: center;
      gap: $spacing-md;
      margin-top: $spacing-md;

      .timer-label { font-size: $font-size-sm; color: $color-text-secondary; }
    }

    .called-actions {
      display: flex;
      gap: $spacing-sm;
      margin-top: $spacing-lg;

      button {
        flex: 1;
        height: 72rpx;
        border-radius: $border-radius-md;
        font-size: $font-size-base;
        border: none;
      }
      .btn-arrive { background-color: $color-primary; color: #FFFFFF; }
      .btn-skip { background-color: $color-warning; color: #FFFFFF; }
      .btn-cancel { background-color: $color-bg-grey; color: $color-text-regular; }
    }
  }

  .waiting-section {
    .waiting-header {
      display: flex;
      justify-content: space-between;
      margin-bottom: $spacing-md;

      .waiting-title { font-size: $font-size-base; font-weight: $font-weight-bold; color: $color-text-primary; }
      .waiting-count { font-size: $font-size-sm; color: $color-text-secondary; }
    }
  }

  .history-link {
    text-align: center;
    margin-top: $spacing-xl;

    text { font-size: $font-size-sm; color: $color-primary; }
  }
}
</style>
```

- [ ] **Step 2: 实现过号记录页**

过号历史列表，显示排队号、儿童、疫苗、过号时间、过号次数。

- [ ] **Step 3: Commit**

```bash
git add pages/calling/
git commit -m "feat: implement calling system (call panel with 5min timeout, skip history)"
```

---

## Phase 4: 留观模块

### Task 4.1: 留观模块

**Files:**
- Create: `api/observe.js`
- Rewrite: `pages/queue/observe.vue`
- Rewrite: `pages/process/observe-detail.vue`
- Rewrite: `pages/process/observe-finish.vue`
- Rewrite: `pages/process/adverse-report.vue`
- Rewrite: `pages/process/adverse-handle.vue`

- [ ] **Step 1: 创建 api/observe.js**

```js
// api/observe.js
import { get, post } from '@/utils/request'

export function getQueue(params) {
  return get('/api/v1/observe/queue', params)
}

export function getStatus(injectionId) {
  return get(`/observe/${injectionId}`)
}

export function finishObserve(appointmentId) {
  return post(`/observe/${appointmentId}/finish`, {}, { showLoading: true })
}

export function reportAdverse(data) {
  return post('/api/v1/observe/adverse/report', data, { showLoading: true })
}

export function handleAdverse(reactionId, data) {
  return post(`/observe/adverse/${reactionId}/handle`, data, { showLoading: true })
}
```

- [ ] **Step 2: 实现留观队列页**

日期选择+卡片列表（每项含CountdownTimer倒计时进度条+5秒轮询）。倒计时完成后显示"可结束留观"标签+操作按钮。

- [ ] **Step 3: 实现留观详情页**

```vue
<!-- pages/process/observe-detail.vue -->
<template>
  <view class="observe-detail">
    <uni-nav-bar title="留观详情" :border="false" />

    <!-- 儿童与接种信息 -->
    <view class="info-card">
      <view class="info-row">
        <text class="info-label">儿童姓名</text>
        <text class="info-value">{{ observeInfo.childName }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">疫苗名称</text>
        <text class="info-value">{{ observeInfo.vaccineName }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">批次号</text>
        <text class="info-value">{{ observeInfo.batchNo }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">接种时间</text>
        <text class="info-value">{{ observeInfo.injectionTime }}</text>
      </view>
    </view>

    <!-- 倒计时进度 -->
    <view class="countdown-section">
      <view class="countdown-circle">
        <CountdownTimer :seconds="elapsed" :total="1800" />
      </view>
      <view class="countdown-meta">
        <text class="countdown-time">已观察 {{ elapsedTime }}</text>
        <text class="countdown-status" :class="{ ready: canFinish }">
          {{ canFinish ? '可结束留观' : '需观察满30分钟' }}
        </text>
      </view>
    </view>

    <!-- 不良反应状态 -->
    <view v-if="observeInfo.hasAdverseReaction" class="adverse-tag">
      <text class="adverse-text">⚠ 已上报不良反应</text>
    </view>

    <!-- 底部操作 -->
    <view class="bottom-bar">
      <button class="btn-adverse" @tap="goToReport">上报不良反应</button>
      <button class="btn-finish" :disabled="!canFinish" :loading="submitting" @tap="finishObserve">
        结束留观
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
// uni-app 全局生命周期（onShow/onHide/onUnload）无需从 Vue 导入
import { getStatus, finishObserve as finishObserveApi } from '@/api/observe.js'
import CountdownTimer from '@/components/CountdownTimer/CountdownTimer.vue'

const injectionId = ref(null)
const observeInfo = ref({})
const elapsedTime = ref('00:00')
const submitting = ref(false)
let countdownInterval = null

const canFinish = computed(() => elapsedTime.value >= '30:00')

// uni-app 全局生命周期无需从 Vue 导入
onLoad(async (query) => {
  injectionId.value = query.injectionId
  await loadStatus()
  startCountdown()
})

onShow(() => { startCountdown() })
onHide(() => { stopCountdown() })
onUnload(() => { stopCountdown() })

async function loadStatus() {
  const data = await getStatus(injectionId.value)
  observeInfo.value = data
}

function startCountdown() {
  stopCountdown()
  countdownInterval = setInterval(() => {
    if (elapsedTime.value < '30:00') {
      const parts = elapsedTime.value.split(':')
      let s = parseInt(parts[2]) + 1
      let m = parseInt(parts[1])
      let h = parseInt(parts[0])
      if (s >= 60) { s = 0; m += 1 }
      if (m >= 60) { m = 0; h += 1 }
      elapsedTime.value = `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
    }
  }, 1000)
}

function stopCountdown() {
  if (countdownInterval) {
    clearInterval(countdownInterval)
    countdownInterval = null
  }
}

async function finishObserve() {
  if (!canFinish.value) {
    uni.showToast({ title: '需观察满30分钟', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    const appointmentId = observeInfo.value.appointmentId
    await finishObserveApi(appointmentId)
    uni.showToast({ title: '留观已完成', icon: 'success' })
    setTimeout(() => uni.switchTab({ url: '/pages/queue/observe' }), 1500)
  } catch (e) {
    if (e.message?.includes('3009')) {
      uni.showModal({ title: '无法结束', content: '异常留观需先上报不良反应', showCancel: false })
    } else {
      uni.showToast({ title: e.message || '操作失败', icon: 'none' })
    }
  } finally {
    submitting.value = false
  }
}

function goToReport() {
  uni.navigateTo({ url: '/pages/process/adverse-report', query: { appointmentId: observeInfo.value.appointmentId } })
}
</script>

<style lang="scss" scoped>
.observe-detail {
  min-height: 100vh;
  background-color: $color-bg-page;
  padding: $spacing-lg;
  padding-bottom: 140rpx;

  .info-card {
    padding: $spacing-lg;
    background: #FFFFFF;
    border-radius: $border-radius-lg;
    box-shadow: $shadow-card;

    .info-row {
      display: flex;
      justify-content: space-between;
      padding: $spacing-sm 0;
      border-bottom: 1rpx solid $color-border-light;

      &:last-child { border-bottom: none; }
      .info-label { font-size: $font-size-sm; color: $color-text-secondary; }
      .info-value { font-size: $font-size-base; color: $color-text-primary; font-weight: $font-weight-medium; }
    }
  }

  .countdown-section {
    display: flex;
    align-items: center;
    gap: $spacing-lg;
    padding: $spacing-lg;
    background: #FFFFFF;
    border-radius: $border-radius-lg;
    box-shadow: $shadow-card;
    margin-top: $spacing-lg;

    .countdown-circle { width: 200rpx; height: 200rpx; }
    .countdown-meta {
      .countdown-time { font-size: $font-size-xxl; font-weight: $font-weight-bold; color: $color-text-primary; }
      .countdown-status { font-size: $font-size-sm; margin-top: $spacing-xs; color: $color-text-secondary; &.ready { color: $color-success; } }
    }
  }

  .adverse-tag {
    margin-top: $spacing-md;
    padding: $spacing-xs $spacing-md;
    background: rgba(238, 10, 36, 0.1);
    border-radius: $border-radius-md;

    .adverse-text { font-size: $font-size-sm; color: $color-danger; }
  }

  .bottom-bar {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    display: flex;
    gap: $spacing-md;
    padding: $spacing-md $spacing-lg;
    padding-bottom: calc(#{$spacing-md} + env(safe-area-inset-bottom));
    background-color: #FFFFFF;

    .btn-adverse {
      flex: 1;
      height: 88rpx;
      line-height: 88rpx;
      background: #FFFFFF;
      color: $color-danger;
      font-size: $font-size-base;
      border: 2rpx solid $color-danger;
      border-radius: $border-radius-lg;
    }
    .btn-finish {
      flex: 1;
      height: 88rpx;
      line-height: 88rpx;
      background-color: $color-primary;
      color: #FFFFFF;
      font-size: $font-size-lg;
      border: none;
      border-radius: $border-radius-lg;
      &:disabled { opacity: 0.5; }
      &::after { border: none; }
    }
  }
}
</style>
```

- [ ] **Step 4: 实现留观结束页**

正常/异常单选。异常时必须先上报不良反应。提交后预约→已完成。

- [ ] **Step 5: 实现不良反应上报页**

基本信息（只读）+ 反应类型下拉 + 严重程度单选 + 发生时间 + 详细描述 + 处理措施 + 处理结果。

- [ ] **Step 6: 实现不良反应处理页**

上报信息（只读）+ 处理结果文本输入 + 确认处理按钮。

- [ ] **Step 7: Commit**

```bash
git add api/observe.js pages/queue/observe.vue pages/process/observe-detail.vue pages/process/observe-finish.vue pages/process/adverse-report.vue pages/process/adverse-handle.vue
git commit -m "feat: implement observe module (queue with countdown, detail, finish, adverse reaction)"
```

---

## Phase 5: 库存模块

### Task 5.1: 库存管理模块

**Files:**
- Create: `api/stock.js`
- Rewrite: `pages/stock/summary.vue`
- Rewrite: `pages/stock/batches.vue`
- Rewrite: `pages/stock/batch-detail.vue`
- Rewrite: `pages/stock/transfer.vue`
- Rewrite: `pages/stock/transfer-records.vue`
- Rewrite: `pages/stock/alerts.vue`
- Rewrite: `pages/stock/dispose.vue`

- [ ] **Step 1: 创建 api/stock.js**

```js
// api/stock.js
import { get, post, put } from '@/utils/request'

export function getSummary() {
  return get('/api/v1/stock/summary')
}

export function getBatches(params) {
  return get('/api/v1/stock/batches', params)
}

export function getBatchDetail(batchId) {
  return get(`/stock/batches/${batchId}`)
}

export function createTransfer(data) {
  return post('/api/v1/stock/transfer', data, { showLoading: true })
}

export function getTransferRecords(params) {
  return get('/api/v1/stock/transfer/records', params)
}

export function disposeBatch(batchId, data) {
  return post(`/stock/batches/${batchId}/dispose`, data, { showLoading: true })
}

export function getAlerts(params) {
  return get('/api/v1/stock/alerts', params)
}

export function handleAlert(alertId) {
  return put(`/stock/alerts/${alertId}/handle`)
}
```

- [ ] **Step 2: 实现库存总览页**

搜索框+筛选（全部/充足/预警/过期）+ 统计卡片（横向滚动）+ 列表（按剩余比例升序，含进度条颜色映射）。点击跳转批次列表。

- [ ] **Step 3: 实现批次列表页**

搜索+筛选（全部/正常/即将过期/已过期/已销毁）+ 分页卡片列表（FEFO排序）。点击跳转批次详情。

- [ ] **Step 4: 实现批次详情页**

批次信息卡片 + 库存信息卡片（可用/锁定/总计/剩余比例）+ 预警信息 + 操作按钮（调拨/销毁）。

- [ ] **Step 5: 实现库存调拨页**

批次信息（只读）+ 调拨表单（调出/调入位置选择、数量、备注）+ 校验（调出≠调入、数量<=可用）+ 确认调拨。

- [ ] **Step 6: 实现调拨记录页**

筛选（批次号+日期范围）+ 分页列表（调拨单号、批次号、调出→调入、数量、时间、操作人）。

- [ ] **Step 7: 实现预警列表页**

筛选Tab（全部/低库存/即将过期/已过期）+ 分页列表（批次号、疫苗、类型、详情、处理状态）+ "标记已处理"按钮。

- [ ] **Step 8: 实现批次销毁页**

批次信息（只读）+ 销毁表单（销毁原因必选+数量）+ 校验 + 确认销毁。

- [ ] **Step 9: Commit**

```bash
git add api/stock.js pages/stock/
git commit -m "feat: implement stock management module (summary, batches, transfer, alerts, dispose)"
```

---

## 验证清单

Phase 1 完成后验证：
- [ ] 登录页正常显示，登录成功后跳转首页
- [ ] 不同角色登录后TabBar显示不同的Tab
- [ ] 首页显示对应角色的工作概览和快捷入口
- [ ] 个人中心显示用户信息和退出登录
- [ ] 自定义TabBar点击切换正常

Phase 2 完成后验证：
- [ ] 签到队列30秒自动刷新，点击可签到
- [ ] 预检评估体温>37.3°C自动提示
- [ ] 接种执行4张卡片全部勾选+选部位才能提交
- [ ] 接种成功页显示留观提醒

Phase 3 完成后验证：
- [ ] 登记处理自动FEFO选批
- [ ] 批次切换正确释放旧批次锁定新批次
- [ ] 叫号面板5分钟超时自动过号
- [ ] 3次过号自动取消并释放库存

Phase 4 完成后验证：
- [ ] 留观队列5秒刷新倒计时
- [ ] 留观详情1秒刷新进度
- [ ] 不足30分钟无法结束留观
- [ ] 异常结束必须先上报不良反应

Phase 5 完成后验证：
- [ ] 库存总览进度条颜色正确（红/橙/绿）
- [ ] 调拨校验调出≠调入
- [ ] 销毁必须填写原因
- [ ] 预警可标记已处理
