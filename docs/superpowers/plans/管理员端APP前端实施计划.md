# 管理员端APP前端实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建疫苗管理系统V2管理员端APP，支持3个管理员角色（SUPER_ADMIN、DOCTOR_BUSINESS_ADMIN、DOCTOR_SCHEDULE），涵盖排班管理、窗口管理、疫苗管理、公告管理、角色权限管理、系统配置和统计分析七大功能模块。

**Architecture:** 单一uni-app项目，三端共用基础设施层（request/auth/constants/scss/CustomTabBar），管理员端使用CustomTabBar（按角色动态显示2-4个Tab）。Pinia管理状态，API层按模块拆分，Hooks封装筛选和分页逻辑。

**Tech Stack:** uni-app + Vue 3 (Composition API) + Pinia + uni-ui + SCSS + u-charts

**设计文档：** `docs/superpowers/specs/管理员端APP前端设计.md`
**前置条件：** 前端共享基础设施实施计划已完成（request.js/auth.js/constants.js/store/user.js/hooks/usePagination.js/hooks/useAuth.js/components/EmptyState/StatusTag/CustomTabBar/App.vue/pages.json/api/auth.js/pages/auth/login.vue/pages/common/404.vue 均已实现）

---

## 范围说明

本计划依赖共享基础设施计划，仅包含管理员端业务页面。共享基础设施已提供：HTTP客户端、认证工具、用户Store、分页Hook、认证Hook、EmptyState/StatusTag组件、CustomTabBar、登录页、404页、App.vue路由守卫、65个路由注册。

| Phase | 内容 | 依赖 |
|-------|------|------|
| Phase 1 | API层 + Store + Hooks + 业务组件（8个API模块、AdminStore、useAdminFilter、4个组件、扩展TabBar配置、扩展UserStore） | 共享基础设施 |
| Phase 2 | 排班管理（排班列表、新增/编辑排班、冲突检测） | Phase 1 |
| Phase 3 | 窗口管理（窗口列表、新增/编辑窗口、配置窗口服务） | Phase 1 |
| Phase 4 | 疫苗管理（疫苗列表、新增/编辑疫苗、上下架切换） | Phase 1 |
| Phase 5 | 公告管理（公告列表、发布公告、审批公告、公告反馈） | Phase 1 |
| Phase 6 | 角色权限 + 系统配置（角色列表、新增/编辑角色、权限树、分配角色、系统配置） | Phase 1 |
| Phase 7 | 统计分析 + 管理中心首页 + 个人中心（4个统计页面、管理中心入口、管理员首页） | Phase 1 |

---

## Phase 1: API层 + Store + Hooks + 业务组件

### Task 1.1: API模块

**Files:**
- Create: `api/schedule.js`
- Create: `api/window.js`
- Create: `api/vaccine-manage.js`
- Create: `api/notice-manage.js`
- Create: `api/stats.js`
- Create: `api/role.js`
- Create: `api/config.js`
- Create: `api/user-manage.js`

- [ ] **Step 1: 创建 api/schedule.js**

```js
// api/schedule.js
import { get, post, put, del } from '@/utils/request.js'

export function getScheduleList(params) {
  return get('/api/v1/admin/schedules', params)
}

export function createSchedule(data) {
  return post('/api/v1/admin/schedules', data, { showLoading: true })
}

export function updateSchedule(id, data) {
  return put(`/api/v1/admin/schedules/${id}`, data, { showLoading: true })
}

export function deleteSchedule(id) {
  return del(`/api/v1/admin/schedules/${id}`, {}, { showLoading: true })
}

export function checkConflict(data) {
  return post('/api/v1/admin/schedules/conflict', data)
}
```

- [ ] **Step 2: 创建 api/window.js**

```js
// api/window.js
import { get, post, put, del } from '@/utils/request.js'

export function getWindowList(params) {
  return get('/api/v1/admin/windows', params)
}

export function createWindow(data) {
  return post('/api/v1/admin/windows', data, { showLoading: true })
}

export function updateWindow(id, data) {
  return put(`/api/v1/admin/windows/${id}`, data, { showLoading: true })
}

export function deleteWindow(id) {
  return del(`/api/v1/admin/windows/${id}`, {}, { showLoading: true })
}

export function saveWindowService(windowId, data) {
  return post(`/api/v1/admin/windows/${windowId}/service`, data, { showLoading: true })
}

export function getWindowService(windowCode) {
  return get(`/api/v1/admin/windows/service/${windowCode}`)
}
```

- [ ] **Step 3: 创建 api/vaccine-manage.js**

```js
// api/vaccine-manage.js
import { get, post, put, del } from '@/utils/request.js'

export function getVaccineList(params) {
  return get('/api/v1/admin/vaccines', params)
}

export function createVaccine(data) {
  return post('/api/v1/admin/vaccines', data, { showLoading: true })
}

export function updateVaccine(id, data) {
  return put(`/api/v1/admin/vaccines/${id}`, data, { showLoading: true })
}

export function deleteVaccine(id) {
  return del(`/api/v1/admin/vaccines/${id}`, {}, { showLoading: true })
}

export function updateShelfStatus(id, status) {
  return put(`/api/v1/admin/vaccines/${id}/shelf-status`, { status }, { showLoading: true })
}
```

- [ ] **Step 4: 创建 api/notice-manage.js**

```js
// api/notice-manage.js
import { get, post, del } from '@/utils/request.js'

export function getNoticeList(params) {
  return get('/api/v1/admin/notices', params)
}

export function publishNotice(data) {
  return post('/api/v1/admin/notices', data, { showLoading: true })
}

export function auditNotice(noticeId, data) {
  return post(`/api/v1/admin/notices/${noticeId}/audit`, data, { showLoading: true })
}

export function deleteNotice(noticeId) {
  return del(`/api/v1/admin/notices/${noticeId}`, {}, { showLoading: true })
}

export function getNoticeFeedback(noticeId, params) {
  return get(`/api/v1/admin/notices/${noticeId}/feedback`, params)
}
```

- [ ] **Step 5: 创建 api/stats.js**

```js
// api/stats.js
import { get } from '@/utils/request.js'

export function getVaccinationStats(params) {
  return get('/api/v1/admin/stats/vaccination', params)
}

export function getStockStats(params) {
  return get('/api/v1/admin/stats/stock', params)
}

export function getEfficiencyStats(params) {
  return get('/api/v1/admin/stats/efficiency', params)
}

export function getAnomalyStats(params) {
  return get('/api/v1/admin/stats/anomaly', params)
}
```

- [ ] **Step 6: 创建 api/role.js**

```js
// api/role.js
import { get, post, put, del } from '@/utils/request.js'

export function getRoleList(params) {
  return get('/api/v1/admin/roles', params)
}

export function createRole(data) {
  return post('/api/v1/admin/roles', data, { showLoading: true })
}

export function updateRole(id, data) {
  return put(`/api/v1/admin/roles/${id}`, data, { showLoading: true })
}

export function deleteRole(id) {
  return del(`/api/v1/admin/roles/${id}`, {}, { showLoading: true })
}

export function assignRoles(userId, roleIds) {
  return post(`/api/v1/admin/users/${userId}/assign-roles`, { roleIds }, { showLoading: true })
}

export function getUserRoles(userId) {
  return get(`/api/v1/admin/users/${userId}/roles`)
}
```

- [ ] **Step 7: 创建 api/config.js**

```js
// api/config.js
import { get, put } from '@/utils/request.js'

export function getConfigList() {
  return get('/api/v1/admin/configs')
}

export function updateConfig(id, value) {
  return put(`/api/v1/admin/configs/${id}`, { value }, { showLoading: true })
}
```

- [ ] **Step 8: 创建 api/user-manage.js**

```js
// api/user-manage.js
import { get, post } from '@/utils/request.js'

export function getUserList(params) {
  return get('/api/v1/admin/users', params)
}

export function freezeUser(userId) {
  return post(`/api/v1/admin/users/${userId}/freeze`, {}, { showLoading: true })
}

export function unfreezeUser(userId) {
  return post(`/api/v1/admin/users/${userId}/unfreeze`, {}, { showLoading: true })
}
```

- [ ] **Step 9: Commit**

```bash
git add api/schedule.js api/window.js api/vaccine-manage.js api/notice-manage.js api/stats.js api/role.js api/config.js api/user-manage.js
git commit -m "feat(admin): 创建8个API模块（schedule/window/vaccine-manage/notice-manage/stats/role/config/user-manage）"
```

---

### Task 1.2: Pinia Store

**Files:**
- Create: `store/admin.js`
- Modify: `store/user.js`（扩展管理员角色getter）

- [ ] **Step 1: 创建 store/admin.js**

```js
// store/admin.js
import { defineStore } from 'pinia'
import { reactive } from 'vue'

export const useAdminStore = defineStore('admin', () => {
  const scheduleFilters = reactive({ doctorId: '', windowId: '', date: '', page: 1, size: 20 })
  const windowFilters = reactive({ functionType: '', status: '', page: 1, size: 20 })
  const vaccineFilters = reactive({ category: '', status: '', page: 1, size: 20 })
  const noticeFilters = reactive({ type: '', status: '', page: 1, size: 20 })

  function saveFilters(module, filters) {
    const target = { schedule: scheduleFilters, window: windowFilters, vaccine: vaccineFilters, notice: noticeFilters }[module]
    if (target) Object.assign(target, filters)
  }

  function resetFilters(module) {
    const defaults = { doctorId: '', windowId: '', date: '', page: 1, size: 20 }
    const windowDefaults = { functionType: '', status: '', page: 1, size: 20 }
    const vaccineDefaults = { category: '', status: '', page: 1, size: 20 }
    const noticeDefaults = { type: '', status: '', page: 1, size: 20 }
    const map = { schedule: defaults, window: windowDefaults, vaccine: vaccineDefaults, notice: noticeDefaults }
    if (map[module]) Object.assign({ schedule: scheduleFilters, window: windowFilters, vaccine: vaccineFilters, notice: noticeFilters }[module], map[module])
  }

  return { scheduleFilters, windowFilters, vaccineFilters, noticeFilters, saveFilters, resetFilters }
})
```

- [ ] **Step 2: 扩展 store/user.js 添加管理员角色getter**

在现有 `store/user.js` 的 getters 中新增：

```js
// 在 getters 中追加
isSuperAdmin: (state) => {
  const roles = state.userInfo.roles || []
  return roles.includes('SUPER_ADMIN')
},
isBusinessAdmin: (state) => {
  const roles = state.userInfo.roles || []
  return roles.includes('DOCTOR_BUSINESS_ADMIN')
},
isScheduleDoctor: (state) => {
  const roles = state.userInfo.roles || []
  return roles.includes('DOCTOR_SCHEDULE')
},
isAdminRole: (state) => {
  const roles = state.userInfo.roles || []
  return roles.some(r => ['SUPER_ADMIN', 'DOCTOR_BUSINESS_ADMIN', 'DOCTOR_SCHEDULE'].includes(r))
}
```

- [ ] **Step 3: Commit**

```bash
git add store/admin.js store/user.js
git commit -m "feat(admin): 创建useAdminStore并扩展useUserStore管理员角色getter"
```

---

### Task 1.3: Hooks

**Files:**
- Create: `hooks/useAdminFilter.js`

- [ ] **Step 1: 创建 hooks/useAdminFilter.js**

```js
// hooks/useAdminFilter.js
import { ref, computed } from 'vue'
import { useAdminStore } from '@/store/admin.js'

export function useAdminFilter(module) {
  const adminStore = useAdminStore()
  const keyword = ref('')
  const filtersLoaded = ref(false)

  const currentFilters = computed(() => {
    const map = { schedule: adminStore.scheduleFilters, window: adminStore.windowFilters, vaccine: adminStore.vaccineFilters, notice: adminStore.noticeFilters }
    return map[module] || {}
  })

  function applyFilters(newFilters) {
    adminStore.saveFilters(module, { ...newFilters, page: 1 })
  }

  function resetAll() {
    keyword.value = ''
    adminStore.resetFilters(module)
  }

  function initFromStore() {
    if (!filtersLoaded.value) {
      keyword.value = currentFilters.value.keyword || ''
      filtersLoaded.value = true
    }
  }

  return { keyword, currentFilters, applyFilters, resetAll, initFromStore }
}
```

- [ ] **Step 2: Commit**

```bash
git add hooks/useAdminFilter.js
git commit -m "feat(admin): 创建useAdminFilter hook（筛选条件缓存/重置）"
```

---

### Task 1.4: 业务组件

**Files:**
- Create: `components/AdminNavCard/AdminNavCard.vue`
- Create: `components/StatsCard/StatsCard.vue`
- Create: `components/ChartPanel/ChartPanel.vue`
- Create: `components/FilterBar/FilterBar.vue`

- [ ] **Step 1: 创建 AdminNavCard**

```vue
<!-- components/AdminNavCard/AdminNavCard.vue -->
<template>
  <view class="admin-nav-card" @tap="$emit('click')">
    <view class="card-icon">
      <text class="icon-text">{{ icon }}</text>
    </view>
    <view class="card-body">
      <text class="card-title">{{ title }}</text>
      <text v-if="permission" class="card-permission">{{ permission }}</text>
    </view>
    <uni-icons type="right" :size="16" color="#C0C0C0" />
  </view>
</template>

<script setup>
defineProps({
  icon: { type: String, default: '' },
  title: { type: String, required: true },
  permission: { type: String, default: '' }
})
defineEmits(['click'])
</script>

<style lang="scss" scoped>
.admin-nav-card {
  display: flex;
  align-items: center;
  background: $color-bg-card;
  border-radius: $radius-lg;
  padding: $spacing-base;
  box-shadow: $shadow-card;
}
.card-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: $radius-base;
  background: $color-primary-light;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: $spacing-base;
  flex-shrink: 0;
}
.icon-text { font-size: 36rpx; }
.card-body { flex: 1; display: flex; flex-direction: column; gap: 4rpx; }
.card-title { font-size: $font-size-base; font-weight: 600; color: $color-text-primary; }
.card-permission { font-size: $font-size-xs; color: $color-text-placeholder; }
</style>
```

- [ ] **Step 2: 创建 StatsCard**

```vue
<!-- components/StatsCard/StatsCard.vue -->
<template>
  <view class="stats-card">
    <text class="stats-value" :class="{ danger: danger }">{{ displayValue }}</text>
    <text class="stats-label">{{ label }}</text>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  value: { type: [Number, String], default: 0 },
  label: { type: String, required: true },
  suffix: { type: String, default: '' },
  danger: { type: Boolean, default: false }
})

const displayValue = computed(() => {
  if (props.suffix) return `${props.value}${props.suffix}`
  return props.value
})
</script>

<style lang="scss" scoped>
.stats-card {
  background: $color-bg-card;
  border-radius: $radius-lg;
  padding: $spacing-base;
  text-align: center;
  min-width: 160rpx;
  flex-shrink: 0;
  box-shadow: $shadow-card;
}
.stats-value {
  font-size: 48rpx;
  font-weight: 600;
  color: $color-primary;
  display: block;
}
.stats-value.danger { color: $color-danger; }
.stats-label {
  font-size: $font-size-xs;
  color: $color-text-secondary;
  margin-top: $spacing-xs;
  display: block;
}
</style>
```

- [ ] **Step 3: 创建 ChartPanel**

```vue
<!-- components/ChartPanel/ChartPanel.vue -->
<template>
  <view class="chart-panel">
    <text v-if="title" class="panel-title">{{ title }}</text>
    <view class="chart-container">
      <canvas :canvas-id="canvasId" :id="canvasId" class="chart-canvas" :style="{ width: width + 'px', height: height + 'px' }" />
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'

const props = defineProps({
  title: { type: String, default: '' },
  canvasId: { type: String, default: 'chart' },
  chartData: { type: Object, default: () => ({}) },
  chartType: { type: String, default: 'column' },
  width: { type: Number, default: 650 },
  height: { type: Number, default: 400 }
})

const uChartsInstance = ref(null)

onMounted(() => {
  initChart()
})

watch(() => props.chartData, () => {
  if (uChartsInstance.value) {
    uChartsInstance.value.updateData(props.chartData)
  }
}, { deep: true })

function initChart() {
  // #ifdef H5
  const canvas = document.getElementById(props.canvasId)
  if (canvas && props.chartData.categories) {
    // H5环境使用 u-charts 渲染
    renderChart()
  }
  // #endif
  // #ifdef MP-WEIXIN || APP-PLUS
  const ctx = uni.createCanvasContext(props.canvasId)
  renderChart()
  // #endif
}

function renderChart() {
  if (!props.chartData || !props.chartData.categories) return

  // #ifdef H5
  const canvas = document.getElementById(props.canvasId)
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  // #endif
  // #ifdef MP-WEIXIN
  const ctx = uni.createCanvasContext(props.canvasId, { type: props.canvasId, component: this })
  // #endif

  const { categories = [], series = [] } = props.chartData
  const opts = { type: props.chartType, canvas: false, animation: true, legend: { data: [] } }

  if (props.chartType === 'pie') {
    opts.type = 'pie'
    series.push({ type: 'pie', radius: '60%', data: props.chartData.map(d => ({ ...d, label: d.label })) })
  } else {
    categories.push(...(props.chartData.map(d => d.label)))
    series.push({ type: props.chartType, data: props.chartData.map(d => d.value), label: d.label, smooth: true })
  }

  // #ifdef H5
  const uChartsInstance = new uCharts(opts)
  uChartsInstance.mount(canvas)
  uChartsInstance.value = uChartsInstance
  // #endif
}
</script>

<style lang="scss" scoped>
.chart-panel {
  background: $color-bg-card;
  border-radius: $radius-lg;
  padding: $spacing-base;
  margin-bottom: $spacing-base;
  box-shadow: $shadow-card;
}
.panel-title {
  font-size: $font-size-base;
  font-weight: 600;
  color: $color-text-primary;
  margin-bottom: $spacing-sm;
  display: block;
}
.chart-container { overflow-x: auto; }
.chart-canvas { width: 100%; }
</style>
```

- [ ] **Step 4: 创建 FilterBar**

```vue
<!-- components/FilterBar/FilterBar.vue -->
<template>
  <view class="filter-bar">
    <view v-for="item in dropdowns" :key="item.key" class="filter-dropdown">
      <picker :range="item.options" range-key="label" :value="getSelectedIndex(item)" @change="onPickerChange(item.key, $event)">
        <view class="picker-trigger">
          <text class="picker-text">{{ getSelectedLabel(item) }}</text>
          <uni-icons type="bottom" :size="12" color="#999" />
        </view>
      </picker>
    </view>
    <view v-if="showSearch" class="filter-search">
      <uni-easyinput v-model="keyword" :placeholder="searchPlaceholder" @confirm="onSearch" @clear="onSearch" />
    </view>
    <view v-if="showReset" class="filter-reset" @tap="$emit('reset')">
      <uni-icons type="refreshempty" :size="16" color="#999" />
      <text class="reset-text">重置</text>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  dropdowns: { type: Array, default: () => [] },
  keyword: { type: String, default: '' },
  searchPlaceholder: { type: String, default: '搜索' },
  showSearch: { type: Boolean, default: true },
  showReset: { type: Boolean, default: true }
})

const emit = defineEmits(['update:keyword', 'filter-change', 'reset', 'search'])

function getSelectedIndex(item) {
  const selected = item.selected || ''
  const idx = item.options.findIndex(o => o.value === selected)
  return idx >= 0 ? idx : 0
}

function getSelectedLabel(item) {
  const selected = item.selected || ''
  const found = item.options.find(o => o.value === selected)
  return found ? found.label : item.options[0]?.label || '全部'
}

function onPickerChange(key, event) {
  const idx = event.detail.value
  const item = props.dropdowns.find(d => d.key === key)
  if (item) emit('filter-change', { key, value: item.options[idx]?.value || '' })
}

function onSearch() {
  emit('search', props.keyword)
}
</script>

<style lang="scss" scoped>
.filter-bar {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm $spacing-base;
  background: $color-bg-card;
  border-radius: $radius-lg;
  margin-bottom: $spacing-base;
  box-shadow: $shadow-card;
  flex-wrap: wrap;
}
.filter-dropdown { flex-shrink: 0; }
.picker-trigger {
  display: flex;
  align-items: center;
  gap: 4rpx;
  padding: 8rpx $spacing-sm;
  background: $color-bg-page;
  border-radius: $radius-sm;
}
.picker-text { font-size: $font-size-sm; color: $color-text-secondary; }
.filter-search { flex: 1; min-width: 200rpx; }
.filter-reset {
  display: flex;
  align-items: center;
  gap: 4rpx;
  padding: 8rpx $spacing-sm;
  flex-shrink: 0;
}
.reset-text { font-size: $font-size-xs; color: $color-text-placeholder; }
</style>
```

- [ ] **Step 5: Commit**

```bash
git add components/AdminNavCard/ components/StatsCard/ components/ChartPanel/ components/FilterBar/
git commit -m "feat(admin): 创建4个管理端业务组件（AdminNavCard/StatsCard/ChartPanel/FilterBar）"
```

---

### Task 1.5: 扩展 TabBar 配置 + 常量

**Files:**
- Modify: `utils/tabBar.js`（添加管理员角色TabBar配置）
- Modify: `utils/constants.js`（添加管理员端状态常量）

- [ ] **Step 1: 扩展 utils/tabBar.js 添加管理员角色配置**

在现有 `TAB_BAR_CONFIG` 对象中追加管理员角色配置：

```js
// 在 TAB_BAR_CONFIG 中追加
SUPER_ADMIN: [
  { pagePath: '/pages/index/index', text: '首页', icon: 'home' },
  { pagePath: '/pages/stats/vaccination', text: '统计分析', icon: 'chart' },
  { pagePath: '/pages/admin/index', text: '管理中心', icon: 'settings' },
  { pagePath: '/pages/mine/index', text: '我的', icon: 'person' }
],
BUSINESS_ADMIN: [
  { pagePath: '/pages/index/index', text: '首页', icon: 'home' },
  { pagePath: '/pages/admin/index', text: '管理中心', icon: 'settings' },
  { pagePath: '/pages/mine/index', text: '我的', icon: 'person' }
],
SCHEDULE_DOCTOR: [
  { pagePath: '/pages/index/index', text: '首页', icon: 'home' },
  { pagePath: '/pages/admin/schedule/list', text: '排班管理', icon: 'calendar' },
  { pagePath: '/pages/mine/index', text: '我的', icon: 'person' }
]
```

同时扩展 `store/user.js` 的 `roleGroup` getter，添加管理员角色判断：

```js
// 在 roleGroup getter 中追加管理员判断
if (roles.some(r => ['SUPER_ADMIN', 'DOCTOR_BUSINESS_ADMIN', 'DOCTOR_SCHEDULE'].includes(r))) {
  if (roles.includes('SUPER_ADMIN')) return 'SUPER_ADMIN'
  if (roles.includes('DOCTOR_BUSINESS_ADMIN')) return 'BUSINESS_ADMIN'
  return 'SCHEDULE_DOCTOR'
}
```

- [ ] **Step 2: 扩展 utils/constants.js 添加管理员端状态常量**

```js
// 排班状态
export const SCHEDULE_STATUS = { NORMAL: 0, ON_LEAVE: 1, CANCELLED: 2 }
export const SCHEDULE_STATUS_TEXT = { 0: '正常', 1: '请假', 2: '已取消' }
export const SCHEDULE_STATUS_COLOR = { 0: '#07C160', 1: '#999999', 2: '#FF9900' }

// 窗口状态
export const WINDOW_STATUS = { OPEN: 0, CLOSED: 1 }
export const WINDOW_STATUS_TEXT = { 0: '开放', 1: '关闭' }
export const WINDOW_STATUS_COLOR = { 0: '#07C160', 1: '#999999' }

// 窗口职能类型
export const WINDOW_FUNCTION_TYPES = [
  { value: 'SIGNIN', label: '签到', color: '#1989FA' },
  { value: 'PRECHECK', label: '预检', color: '#07C160' },
  { value: 'REGISTER', label: '登记', color: '#FF9900' },
  { value: 'VACCINATE', label: '接种', color: '#EE0A24' },
  { value: 'OBSERVE', label: '留观', color: '#7232DD' },
  { value: 'STOCK', label: '库存', color: '#17C3B2' }
]

// 疫苗状态
export const VACCINE_STATUS = { ON_SHELF: 0, OFF_SHELF: 1 }
export const VACCINE_STATUS_TEXT = { 0: '上架', 1: '下架' }
export const VACCINE_STATUS_COLOR = { 0: '#07C160', 1: '#999999' }

// 疫苗类别
export const VACCINE_CATEGORY = { CLASS_I: 'CLASS_I', CLASS_II: 'CLASS_II' }
export const VACCINE_CATEGORY_TEXT = { CLASS_I: '一类', CLASS_II: '二类' }
export const VACCINE_CATEGORY_COLOR = { CLASS_I: '#07C160', CLASS_II: '#1989FA' }

// 公告状态
export const NOTICE_STATUS = { PENDING: 0, PUBLISHED: 1, TAKEN_DOWN: 2, REJECTED: 3 }
export const NOTICE_STATUS_TEXT = { 0: '待审批', 1: '已发布', 2: '已下架', 3: '已拒绝' }
export const NOTICE_STATUS_COLOR = { 0: '#FF9900', 1: '#07C160', 2: '#999999', 3: '#EE0A24' }

// 公告类型
export const NOTICE_TYPE = { NORMAL: 'NORMAL', URGENT: 'URGENT', SYSTEM: 'SYSTEM' }
export const NOTICE_TYPE_TEXT = { NORMAL: '公告', URGENT: '紧急', SYSTEM: '系统' }
export const NOTICE_TYPE_COLOR = { NORMAL: '#1989FA', URGENT: '#EE0A24', SYSTEM: '#07C160' }

// 权限分组
export const PERMISSION_GROUPS = [
  { label: '排班权限', permissions: [
    { value: 'doctor.schedule.create', label: '创建排班' },
    { value: 'doctor.schedule.edit', label: '修改排班' },
    { value: 'doctor.schedule.view', label: '查看排班' },
    { value: 'doctor.schedule.delete', label: '删除排班' }
  ]},
  { label: '窗口权限', permissions: [
    { value: 'window.manage', label: '窗口管理' },
    { value: 'window.service.manage', label: '窗口服务配置' }
  ]},
  { label: '疫苗权限', permissions: [
    { value: 'vaccine.catalog.manage', label: '疫苗目录管理' },
    { value: 'vaccine.catalog.view', label: '疫苗目录查看' }
  ]},
  { label: '公告权限', permissions: [
    { value: 'notice.manage', label: '公告管理' },
    { value: 'notice.view', label: '公告查看' },
    { value: 'notice.audit', label: '公告审批' },
    { value: 'notice.feedback', label: '公告反馈查看' }
  ]},
  { label: '统计权限', permissions: [
    { value: 'stats.view', label: '统计查看' }
  ]},
  { label: '用户权限', permissions: [
    { value: 'user.manage', label: '用户管理' },
    { value: 'user.freeze', label: '用户冻结/解冻' }
  ]}
]

// 管理端错误码
export const ADMIN_ERROR_MAP = {
  8001: '该医生在该日期已有排班，时间冲突',
  8002: '排班不存在或已被删除',
  8003: '窗口编码已存在',
  8004: '该窗口下已有排班记录，无法删除',
  8005: '公告不存在',
  8006: '疫苗不存在',
  8008: '角色编码已存在',
  8009: '角色不存在',
  8010: '系统内置角色不可删除',
  8011: '该角色已分配给用户，无法删除',
  8015: '不可修改超级管理员信息'
}
```

- [ ] **Step 3: Commit**

```bash
git add utils/tabBar.js utils/constants.js store/user.js
git commit -m "feat(admin): 扩展TabBar配置（3个管理员角色）和状态常量（排班/窗口/疫苗/公告/权限）"
```

---

## Phase 2: 排班管理

### Task 2.1: 排班列表

**Files:**
- Rewrite: `pages/admin/schedule/list.vue`

- [ ] **Step 1: 实现排班列表页**

页面结构：FilterBar（医生姓名搜索 + 窗口下拉选择 + 日期选择器）+ 排班卡片列表 + 上拉加载更多 + EmptyState + DOCTOR_SCHEDULE角色显示浮动"新增排班"按钮。

排班卡片显示：排班号、医生姓名、窗口名称、日期时段、容量、状态标签（SCHEDULE_STATUS颜色映射）。DOCTOR_BUSINESS_ADMIN仅查看，DOCTOR_SCHEDULE显示[修改][删除]操作。点击卡片跳转编辑页。

```vue
<!-- pages/admin/schedule/list.vue -->
<template>
  <view class="schedule-list-page">
    <!-- 筛选栏 -->
    <FilterBar
      :dropdowns="windowOptions"
      v-model:keyword="keyword"
      search-placeholder="搜索医生姓名"
      @filter-change="onFilterChange"
      @search="handleSearch"
      @reset="handleReset"
    />

    <!-- 排班列表 -->
    <view v-if="list.length > 0" class="card-list">
      <view v-for="item in list" :key="item.id" class="schedule-card" @tap="goEdit(item)">
        <view class="card-header">
          <text class="schedule-no">{{ item.scheduleNo }}</text>
          <StatusTag :text="statusText(item.status)" :color="statusColor(item.status)" />
        </view>
        <view class="card-body">
          <text class="info-text">👨‍⚕️ {{ item.doctorName }}</text>
          <text class="info-text">🏥 {{ item.windowName }}</text>
          <text class="info-text">📅 {{ item.scheduleDate }}</text>
          <text class="info-text">🕐 {{ item.startTime }} - {{ item.endTime }}</text>
          <text class="info-text">👥 容量：{{ item.capacity }}</text>
        </view>
        <view v-if="isScheduleDoctor" class="card-actions">
          <view class="action-btn" @tap.stop="goEdit(item)"><text>修改</text></view>
          <view class="action-btn danger" @tap.stop="handleDelete(item)"><text>删除</text></view>
        </view>
      </view>
    </view>
    <EmptyState v-else-if="!loading" icon="📋" title="暂无排班记录" description="当前筛选条件下无排班" />

    <!-- 浮动新增按钮 -->
    <view v-if="isScheduleDoctor" class="fab" @tap="goCreate">
      <text class="fab-icon">+</text>
    </view>

    <CustomTabBar />
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getScheduleList, deleteSchedule } from '@/api/schedule.js'
import { usePagination } from '@/hooks/usePagination.js'
import { useAdminFilter } from '@/hooks/useAdminFilter.js'
import { useUserStore } from '@/store/user.js'
import { SCHEDULE_STATUS_TEXT, SCHEDULE_STATUS_COLOR } from '@/utils/constants.js'
import FilterBar from '@/components/FilterBar/FilterBar.vue'
import StatusTag from '@/components/StatusTag/StatusTag.vue'
import EmptyState from '@/components/EmptyState/EmptyState.vue'
import CustomTabBar from '@/components/CustomTabBar.vue'

const userStore = useUserStore()
const isScheduleDoctor = computed(() => userStore.isScheduleDoctor)
const keyword = ref('')

const windowOptions = ref([
  { key: 'windowId', options: [{ value: '', label: '全部窗口' }], selected: '' }
])

function statusText(status) { return SCHEDULE_STATUS_TEXT[status] || '未知' }
function statusColor(status) { return SCHEDULE_STATUS_COLOR[status] || '#999999' }

function fetchList(page, size) {
  return getScheduleList({ page, size, keyword: keyword.value })
}
const { list, loading, hasMore, loadData, reset } = usePagination(fetchList)

function onFilterChange({ key, value }) { loadData(true) }
function handleSearch() { loadData(true) }
function handleReset() { keyword.value = ''; loadData(true) }

function goCreate() { uni.navigateTo({ url: '/pages/admin/schedule/edit' }) }
function goEdit(item) {
  const url = `/pages/admin/schedule/edit?id=${item.id}`
  uni.navigateTo({ url })
}

function handleDelete(item) {
  uni.showModal({
    title: '确认删除',
    content: `确定删除 ${item.doctorName} 的排班吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteSchedule(item.id)
          uni.showToast({ title: '删除成功', icon: 'success' })
          loadData(true)
        } catch (e) { /* 错误由拦截器处理 */ }
      }
    }
  })
}

onMounted(() => { loadData(true) })

// 上拉加载更多
onReachBottom(() => { if (hasMore.value) loadData() })
</script>

<style lang="scss" scoped>
.schedule-list-page { min-height: 100vh; padding: $spacing-lg; padding-bottom: 140rpx; }
.card-list { display: flex; flex-direction: column; }
.schedule-card {
  background: $color-bg-card; border-radius: $radius-lg; padding: $spacing-base;
  margin-bottom: $spacing-base; box-shadow: $shadow-card;
}
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: $spacing-sm; }
.schedule-no { font-size: $font-size-base; font-weight: 600; color: $color-text-primary; }
.card-body { display: flex; flex-direction: column; gap: 4rpx; }
.info-text { font-size: $font-size-sm; color: $color-text-secondary; }
.card-actions { display: flex; gap: $spacing-sm; margin-top: $spacing-base; justify-content: flex-end; }
.action-btn {
  padding: 8rpx $spacing-base; border-radius: $radius-sm; font-size: $font-size-sm;
  background: $color-primary-light; color: $color-primary;
}
.action-btn.danger { background: rgba(238,10,36,0.1); color: $color-danger; }
.fab {
  position: fixed; right: $spacing-lg; bottom: 160rpx; width: 100rpx; height: 100rpx;
  border-radius: 50%; background: $color-primary; display: flex; align-items: center;
  justify-content: center; box-shadow: 0 4rpx 16rpx rgba(7,193,96,0.4);
}
.fab-icon { font-size: 48rpx; color: #fff; }
</style>
```

- [ ] **Step 2: Commit**

```bash
git add pages/admin/schedule/list.vue
git commit -m "feat(admin): 实现排班列表（FilterBar/卡片列表/角色权限控制/FAB新增）"
```

---

### Task 2.2: 新增/编辑排班

**Files:**
- Rewrite: `pages/admin/schedule/edit.vue`

- [ ] **Step 1: 实现新增/编辑排班页**

页面结构：导航栏（返回+"新增排班"/"编辑排班"）、表单区（医生下拉选择、窗口下拉选择、排班日期picker、开始/结束时间picker）、温馨提示卡片、底部保存/取消按钮。

前端校验：开始时间<结束时间。提交流程：先调 `checkConflict` 接口检测冲突，通过后提交创建/修改。编辑模式加载排班详情时，若排班日期已过且有关联预约，显示提示条并禁用时间选择器。

```vue
<!-- pages/admin/schedule/edit.vue -->
<template>
  <view class="schedule-edit-page">
    <view class="nav-bar">
      <uni-icons type="back" :size="20" @click="uni.navigateBack()" />
      <text class="nav-title">{{ isEdit ? '编辑排班' : '新增排班' }}</text>
    </view>

    <!-- 过期排班提示 -->
    <view v-if="isEdit && isPastSchedule" class="warning-banner">
      <text>该排班日期已过且已有预约，仅可查看不可修改时间</text>
    </view>

    <!-- 表单 -->
    <view class="form-section">
      <view class="form-item">
        <text class="form-label">医生 *</text>
        <picker :range="doctorOptions" range-key="label" @change="onDoctorChange">
          <view class="picker-value">{{ form.doctorName || '请选择医生' }}</view>
        </picker>
      </view>
      <view class="form-item">
        <text class="form-label">窗口 *</text>
        <picker :range="windowOptions" range-key="label" @change="onWindowChange">
          <view class="picker-value">{{ form.windowName || '请选择窗口' }}</view>
        </picker>
        <text v-if="form.windowFunction" class="form-hint">职能：{{ form.windowFunction }}</text>
      </view>
      <view class="form-item">
        <text class="form-label">排班日期 *</text>
        <uni-datetime-picker type="date" v-model="form.scheduleDate" :start="todayStr" :clear-icon="false" />
      </view>
      <view class="form-row">
        <view class="form-item half">
          <text class="form-label">开始时间 *</text>
          <uni-datetime-picker type="time" v-model="form.startTime" :clear-icon="false" :disabled="timeDisabled" />
        </view>
        <view class="form-item half">
          <text class="form-label">结束时间 *</text>
          <uni-datetime-picker type="time" v-model="form.endTime" :clear-icon="false" :disabled="timeDisabled" />
        </view>
      </view>
    </view>

    <!-- 温馨提示 -->
    <view class="tip-card">
      <text class="tip-title">温馨提示</text>
      <text class="tip-text">1. 排班时间须在窗口开放时间内</text>
      <text class="tip-text">2. 同一医生同一日期排班不可冲突</text>
    </view>

    <!-- 底部按钮 -->
    <view class="bottom-actions">
      <button class="btn-cancel" @click="uni.navigateBack()">取消</button>
      <button class="btn-confirm" :loading="submitting" @click="handleSubmit">保存</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { createSchedule, updateSchedule, checkConflict, getScheduleList } from '@/api/schedule.js'

const isEdit = ref(false)
const scheduleId = ref('')
const isPastSchedule = ref(false)
const submitting = ref(false)
const todayStr = new Date().toISOString().slice(0, 10)
const timeDisabled = computed(() => isEdit.value && isPastSchedule.value)

const form = reactive({
  doctorId: '', doctorName: '', windowId: '', windowName: '', windowFunction: '',
  scheduleDate: todayStr, startTime: '', endTime: ''
})

const doctorOptions = ref([])
const windowOptions = ref([])

function onDoctorChange(e) {
  const idx = e.detail.value
  form.doctorId = doctorOptions.value[idx]?.value || ''
  form.doctorName = doctorOptions.value[idx]?.label || ''
}

function onWindowChange(e) {
  const idx = e.detail.value
  form.windowId = windowOptions.value[idx]?.value || ''
  form.windowName = windowOptions.value[idx]?.label || ''
  form.windowFunction = windowOptions.value[idx]?.function || ''
}

async function handleSubmit() {
  if (!form.doctorId) { uni.showToast({ title: '请选择医生', icon: 'none' }); return }
  if (!form.windowId) { uni.showToast({ title: '请选择窗口', icon: 'none' }); return }
  if (!form.scheduleDate) { uni.showToast({ title: '请选择排班日期', icon: 'none' }); return }
  if (!form.startTime || !form.endTime) { uni.showToast({ title: '请选择开始/结束时间', icon: 'none' }); return }
  if (form.startTime >= form.endTime) { uni.showToast({ title: '开始时间须早于结束时间', icon: 'none' }); return }

  submitting.value = true
  try {
    // 冲突检测
    const conflictData = await checkConflict({
      doctorId: form.doctorId, scheduleDate: form.scheduleDate,
      startTime: form.startTime, endTime: form.endTime,
      excludeId: isEdit.value ? scheduleId.value : undefined
    })
    if (conflictData.conflict) {
      uni.showToast({ title: '排班时间冲突', icon: 'none' })
      return
    }

    const data = { doctorId: form.doctorId, windowId: form.windowId, scheduleDate: form.scheduleDate, startTime: form.startTime, endTime: form.endTime }
    if (isEdit.value) {
      await updateSchedule(scheduleId.value, data)
    } else {
      await createSchedule(data)
    }
    uni.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) { /* handled by interceptor */ } finally { submitting.value = false }
}

onLoad(async (query) => {
  if (query.id) {
    isEdit.value = true
    scheduleId.value = query.id
    // 加载排班详情，预填表单
    const res = await getScheduleList({ id: query.id })
    if (res.records?.length) {
      const item = res.records[0]
      Object.assign(form, { doctorId: item.doctorId, doctorName: item.doctorName, windowId: item.windowId, windowName: item.windowName, windowFunction: item.windowFunction, scheduleDate: item.scheduleDate, startTime: item.startTime, endTime: item.endTime })
      // 判断是否过期排班
      if (new Date(item.scheduleDate) < new Date() && item.hasAppointment) isPastSchedule.value = true
    }
  }
})
</script>

<style lang="scss" scoped>
.schedule-edit-page { min-height: 100vh; padding: $spacing-lg; background: $color-bg-page; }
.nav-bar { display: flex; align-items: center; gap: $spacing-sm; margin-bottom: $spacing-lg; }
.nav-title { font-size: $font-size-lg; font-weight: 600; }
.warning-banner { background: rgba(255,153,0,0.1); padding: $spacing-sm $spacing-base; border-radius: $radius-sm; margin-bottom: $spacing-base; }
.warning-banner text { font-size: $font-size-sm; color: #FF9900; }
.form-section { background: $color-bg-card; border-radius: $radius-lg; padding: $spacing-base; margin-bottom: $spacing-base; box-shadow: $shadow-card; }
.form-item { margin-bottom: $spacing-base; }
.form-label { font-size: $font-size-sm; color: $color-text-secondary; margin-bottom: $spacing-xs; display: block; }
.picker-value { font-size: $font-size-base; color: $color-text-primary; padding: 12rpx $spacing-sm; background: $color-bg-page; border-radius: $radius-sm; }
.form-hint { font-size: $font-size-xs; color: $color-text-placeholder; margin-top: 4rpx; display: block; }
.form-row { display: flex; gap: $spacing-base; }
.half { flex: 1; }
.tip-card { background: rgba(25,137,250,0.05); padding: $spacing-base; border-radius: $radius-lg; margin-bottom: $spacing-lg; }
.tip-title { font-size: $font-size-sm; font-weight: 600; color: #1989FA; display: block; margin-bottom: $spacing-xs; }
.tip-text { font-size: $font-size-xs; color: $color-text-secondary; display: block; margin-top: 4rpx; }
.bottom-actions { display: flex; gap: $spacing-base; }
.btn-cancel { flex: 1; background: transparent; border: 1rpx solid $color-border; color: $color-text-secondary; border-radius: $radius-base; height: 88rpx; line-height: 88rpx; }
.btn-confirm { flex: 2; background: $color-primary; color: #FFF; border: none; border-radius: $radius-base; height: 88rpx; line-height: 88rpx; }
</style>
```

- [ ] **Step 2: Commit**

```bash
git add pages/admin/schedule/edit.vue
git commit -m "feat(admin): 实现新增/编辑排班（冲突检测/过期排班保护/前端校验）"
```

---

## Phase 3: 窗口管理

### Task 3.1: 窗口列表

**Files:**
- Rewrite: `pages/admin/window/list.vue`

- [ ] **Step 1: 实现窗口列表页**

```vue
<!-- pages/admin/window/list.vue -->
<template>
  <view class="window-list-page">
    <uni-nav-bar title="窗口管理" :border="false" />

    <!-- 筛选栏 -->
    <FilterBar :filters="filterConfig" @search="onSearch" @reset="onResetFilter" />

    <!-- 窗口卡片列表 -->
    <view class="card-list">
      <view v-for="item in windowList" :key="item.id" class="window-card" @click="goToEdit(item)">
        <view class="card-header">
          <text class="window-code">{{ item.windowCode }}</text>
          <StatusTag :status="item.status" :options="WINDOW_STATUS" />
        </view>
        <view class="card-body">
          <text class="window-name">{{ item.windowName }}</text>
          <view class="card-tags">
            <text class="tag" v-for="ft in item.functionTypes" :key="ft" :style="{ color: WINDOW_FUNCTION_TYPES[ft]?.color }">
              {{ WINDOW_FUNCTION_TYPES[ft]?.label }}
            </text>
          </view>
          <text class="card-meta">容量 {{ item.capacity }} · {{ item.openTime }}-{{ item.closeTime }}</text>
        </view>
        <view class="card-actions">
          <text class="action-text" @tap.stop="goToService(item)">配置服务</text>
          <text class="action-text" @tap.stop="goToEdit(item)">修改</text>
          <text class="action-text danger" @tap.stop="deleteWindow(item)">删除</text>
        </view>
      </view>
      <EmptyState v-if="!loading && windowList.length === 0" icon="list" title="暂无窗口记录" actionText="新增窗口" @action="goToAdd" />
      <uni-load-more :status="loadMoreStatus" @loadmore="loadMore" />
    </view>

    <!-- 浮动新增按钮 -->
    <view class="fab-btn" @tap="goToAdd">
      <text class="fab-icon">+</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
// uni-app 全局生命周期（onShow）无需从 Vue 导入
import { getWindowList, deleteWindow as deleteWindowApi } from '@/api/window.js'
import FilterBar from '@/components/FilterBar/FilterBar.vue'
import StatusTag from '@/components/StatusTag/StatusTag.vue'
import EmptyState from '@/components/EmptyState/EmptyState.vue'
import { WINDOW_STATUS, WINDOW_FUNCTION_TYPES } from '@/utils/constants.js'

const windowList = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const loadMoreStatus = ref('more')

const keyword = ref('')

const filterConfig = computed(() => [
  { key: 'functionType', label: '职能类型', type: 'select', options: Object.entries(WINDOW_FUNCTION_TYPES).map(([k, v]) => ({ label: v.label, value: k })) },
  { key: 'status', label: '状态', type: 'select', options: Object.entries(WINDOW_STATUS).map(([k, v]) => ({ label: v.text, value: Number(k) })) }
])

const hasMore = computed(() => windowList.value.length < total.value)

onShow(() => { loadMore(true) })

async function loadMore(reset = false) {
  if (reset) { page.value = 1; windowList.value = [] }
  if (loading.value || !hasMore.value) return
  loading.value = true
  try {
    const data = await getWindowList({ functionType: filterConfig.value.functionType, status: filterConfig.value.status, keyword: keyword.value, page: page.value, size: size.value })
    windowList.value = reset ? data.records : [...windowList.value, ...data.records]
    total.value = data.total
    page.value++
    loadMoreStatus.value = hasMore.value ? 'more' : 'noMore'
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function onSearch(val) { keyword.value = val; loadMore(true) }
function onResetFilter() { Object.keys(filterConfig.value).forEach(k => filterConfig.value[k] = ''); loadMore(true) }
function goToAdd() { uni.navigateTo({ url: '/pages/admin/window/edit' }) }
function goToEdit(item) { uni.navigateTo({ url: '/pages/admin/window/edit', query: { id: item.id } }) }
function goToService(item) { uni.navigateTo({ url: '/pages/admin/window/service', query: { windowCode: item.windowCode } }) }

async function deleteWindow(item) {
  uni.showModal({
    title: '确认删除',
    content: `确定要删除窗口「${item.windowName}」吗？`,
    success: async () => {
      try {
        await deleteWindowApi(item.id)
        uni.showToast({ title: '删除成功', icon: 'success' })
        loadMore(true)
      } catch (e) {
        uni.showToast({ title: e.message || '删除失败', icon: 'none' })
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.window-list-page {
  min-height: 100vh;
  background-color: $color-bg-page;
  padding: $spacing-lg;
  padding-bottom: 120rpx;

  .card-list { margin-top: $spacing-md; }

  .window-card {
    background: #FFFFFF;
    border-radius: $border-radius-lg;
    box-shadow: $shadow-card;
    margin-bottom: $spacing-md;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: $spacing-md $spacing-lg $spacing-sm;

      .window-code { font-size: $font-size-base; font-weight: $font-weight-bold; color: $color-text-primary; }
    }

    .card-body { padding: 0 $spacing-lg $spacing-md; }

    .card-tags {
      display: flex;
      flex-wrap: wrap;
      gap: $spacing-xs;
      margin-bottom: $spacing-xs;

      .tag {
        padding: 2rpx 12rpx;
        font-size: $font-size-xs;
        background-color: $color-bg-grey;
        border-radius: $border-radius-sm;
      }
    }

    .card-meta { font-size: $font-size-xs; color: $color-text-placeholder; }

    .card-actions {
      display: flex;
      justify-content: flex-end;
      gap: $spacing-md;
      padding: 0 $spacing-lg $spacing-md;
      border-top: 1rpx solid $color-border-light;

      .action-text { font-size: $font-size-sm; color: $color-text-secondary; }
      .action-text.danger { color: $color-danger; }
    }
  }

  .fab-btn {
    position: fixed;
    right: $spacing-lg;
    bottom: 160rpx;
    width: 100rpx;
    height: 100rpx;
    background-color: $color-primary;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: $shadow-lg;

    .fab-icon { font-size: 56rpx; color: #FFFFFF; line-height: 1; }
  }
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add pages/admin/window/list.vue
git commit -m "feat(admin): 实现窗口列表（职能类型标签/状态标签/配置服务入口）"
```

---

### Task 3.2: 新增/编辑窗口

**Files:**
- Rewrite: `pages/admin/window/edit.vue`

- [ ] **Step 1: 实现新增/编辑窗口页**

页面结构：导航栏（返回+"新增窗口"/"编辑窗口"）、表单区（窗口编码、名称、描述、职能类型下拉、角色编码、容量、开放/关闭时间、排序、状态下拉）、校验规则（编码唯一、容量>0、开放<关闭时间）、底部保存/取消按钮。

- [ ] **Step 2: Commit**

```bash
git add pages/admin/window/edit.vue
git commit -m "feat(admin): 实现新增/编辑窗口（职能类型联动角色编码/完整表单校验）"
```

---

### Task 3.3: 配置窗口服务

**Files:**
- Rewrite: `pages/admin/window/service.vue`

- [ ] **Step 1: 实现配置窗口服务页**

页面结构：导航栏（返回+"配置窗口服务"）、窗口信息展示（只读：编码、名称、职能类型）、服务配置表单（业务名称、业务描述、业务详情说明、预估办理时间分钟数、温馨提示、需携带物品）、底部保存/取消按钮。

- [ ] **Step 2: Commit**

```bash
git add pages/admin/window/service.vue
git commit -m "feat(admin): 实现配置窗口服务页（只读窗口信息+服务配置表单）"
```

---

## Phase 4: 疫苗管理

### Task 4.1: 疫苗列表

**Files:**
- Rewrite: `pages/admin/vaccine/list.vue`

- [ ] **Step 1: 实现疫苗列表页**

页面结构：FilterBar（疫苗类别下拉：一类/二类 + 状态下拉：上架/下架）+ 疫苗卡片列表 + 上拉加载更多 + EmptyState + 浮动"新增疫苗"按钮。

疫苗卡片显示：疫苗编码、名称、类别标签（CLASS_I绿色/CLASS_II蓝色）、适龄范围、剂次、厂家、状态标签（上架绿色/下架灰色）。操作：[修改][删除][上架/下架切换]。上下架切换：点击后二次确认弹窗，调用 `updateShelfStatus`。

- [ ] **Step 2: Commit**

```bash
git add pages/admin/vaccine/list.vue
git commit -m "feat(admin): 实现疫苗列表（类别/状态筛选/上下架切换二次确认）"
```

---

### Task 4.2: 新增/编辑疫苗

**Files:**
- Rewrite: `pages/admin/vaccine/edit.vue`

- [ ] **Step 1: 实现新增/编辑疫苗页**

页面结构：导航栏（返回+"新增疫苗"/"编辑疫苗"）、分区表单（3个卡片区块）：
1. 基本信息：疫苗编码、名称、类别下拉、厂家、规格、状态下拉
2. 接种规则：最小月龄、最大月龄、接种剂次、接种间隔天数
3. 疫苗说明：描述、接种程序说明、禁忌症说明、不良反应说明（均为选填textarea）

校验：编码唯一、最小月龄<=最大月龄、剂次>0、间隔>0。底部保存/取消按钮。

- [ ] **Step 2: Commit**

```bash
git add pages/admin/vaccine/edit.vue
git commit -m "feat(admin): 实现新增/编辑疫苗（3区表单/月龄范围校验/剂次间隔校验）"
```

---

## Phase 5: 公告管理

### Task 5.1: 公告列表

**Files:**
- Rewrite: `pages/admin/notice/list.vue`

- [ ] **Step 1: 实现公告列表页**

页面结构：FilterBar（公告类型下拉 + 状态下拉）+ 公告卡片列表 + 上拉加载更多 + EmptyState + DOCTOR_BUSINESS_ADMIN显示浮动"发布公告"按钮。

公告卡片显示：标题、类型标签（NOTICE_TYPE颜色映射）、状态标签（NOTICE_STATUS颜色映射）、发布人、生效/失效时间。操作根据角色不同：
- DOCTOR_BUSINESS_ADMIN：[查看详情][删除被拒绝的公告]
- SUPER_ADMIN：[查看详情][审批（仅PENDING）][删除]

- [ ] **Step 2: Commit**

```bash
git add pages/admin/notice/list.vue
git commit -m "feat(admin): 实现公告列表（类型/状态筛选/角色权限操作控制）"
```

---

### Task 5.2: 发布公告

**Files:**
- Rewrite: `pages/admin/notice/publish.vue`

- [ ] **Step 1: 实现发布公告页**

页面结构：导航栏（返回+"发布公告"）、表单区（标题、内容多行textarea、公告类型下拉NORMAL/URGENT/SYSTEM、是否置顶switch、生效时间datetime picker、失效时间datetime picker）、校验（标题必填、内容必填、生效<失效时间）、底部"提交审批"/取消按钮。提交后状态PENDING，Toast"已提交审批"，返回列表。

- [ ] **Step 2: Commit**

```bash
git add pages/admin/notice/publish.vue
git commit -m "feat(admin): 实现发布公告（类型选择/置顶开关/时间校验/提交审批）"
```

---

### Task 5.3: 审批公告

**Files:**
- Rewrite: `pages/admin/notice/approve.vue`

- [ ] **Step 1: 实现审批公告页**

页面结构：导航栏（返回+"审批公告"）、公告信息展示（只读卡片：标题、内容、类型、是否置顶、发布人、生效/失效时间）、审批意见展示（如被拒绝过显示上次拒绝原因）、审批表单（审批结果通过/拒绝Radio按钮组、审批意见选填textarea）、底部"提交审批"/取消按钮。仅SUPER_ADMIN可访问。

- [ ] **Step 2: Commit**

```bash
git add pages/admin/notice/approve.vue
git commit -m "feat(admin): 实现审批公告（公告只读展示/通过拒绝Radio/拒绝原因展示）"
```

---

### Task 5.4: 公告反馈

**Files:**
- Rewrite: `pages/admin/notice/feedback.vue`

- [ ] **Step 1: 实现公告反馈页**

页面结构：导航栏（返回+公告标题只读）、反馈列表（反馈人手机号脱敏、反馈内容、反馈时间）、上拉加载更多。使用 `getNoticeFeedback` API分页加载。

- [ ] **Step 2: Commit**

```bash
git add pages/admin/notice/feedback.vue
git commit -m "feat(admin): 实现公告反馈页（手机号脱敏/分页列表）"
```

---

## Phase 6: 角色权限 + 系统配置

### Task 6.1: 角色列表

**Files:**
- Rewrite: `pages/admin/role/list.vue`

- [ ] **Step 1: 实现角色列表页**

页面结构：角色卡片列表 + 浮动"创建角色"按钮。每张卡片显示：角色名称、角色编码、权限数量、关联用户数、系统内置标签（灰色"系统内置"）。系统内置角色隐藏编辑/删除按钮。操作：[查看权限][编辑][分配用户]。

- [ ] **Step 2: Commit**

```bash
git add pages/admin/role/list.vue
git commit -m "feat(admin): 实现角色列表（系统内置标签/权限数量/关联用户数）"
```

---

### Task 6.2: 新增/编辑角色

**Files:**
- Rewrite: `pages/admin/role/edit.vue`

- [ ] **Step 1: 实现新增/编辑角色页**

页面结构：导航栏（返回+"创建角色"/"编辑角色"）、表单区（角色名称、角色编码）、权限树（按PERMISSION_GROUPS分组展示checkbox列表，已勾选为已分配权限，6个权限组：排班/窗口/疫苗/公告/统计/用户）、底部保存/取消按钮。

```vue
<!-- pages/admin/role/edit.vue -->
<template>
  <view class="role-edit-page">
    <view class="nav-bar">
      <uni-icons type="back" :size="20" @click="uni.navigateBack()" />
      <text class="nav-title">{{ isEdit ? '编辑角色' : '创建角色' }}</text>
    </view>

    <!-- 基本信息 -->
    <view class="form-section">
      <view class="form-item">
        <text class="form-label">角色名称 *</text>
        <uni-easyinput v-model="form.roleName" placeholder="请输入角色名称" />
      </view>
      <view class="form-item">
        <text class="form-label">角色编码 *</text>
        <uni-easyinput v-model="form.roleCode" placeholder="请输入角色编码" :disabled="isEdit" />
      </view>
    </view>

    <!-- 权限树 -->
    <view class="form-section">
      <text class="section-title">权限配置</text>
      <view v-for="group in permissionGroups" :key="group.label" class="permission-group">
        <view class="group-header">
          <text class="group-label">{{ group.label }}</text>
        </view>
        <view v-for="perm in group.permissions" :key="perm.value" class="permission-item">
          <checkbox-group @change="onPermissionChange(group.label, perm.value, $event)">
            <label class="checkbox-label">
              <checkbox :value="perm.value" :checked="selectedPermissions.includes(perm.value)" />
              <text class="checkbox-text">{{ perm.label }}</text>
            </label>
          </checkbox-group>
        </view>
      </view>
    </view>

    <view class="bottom-actions">
      <button class="btn-cancel" @click="uni.navigateBack()">取消</button>
      <button class="btn-confirm" :loading="submitting" @click="handleSubmit">保存</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { createRole, updateRole, getRoleList } from '@/api/role.js'
import { PERMISSION_GROUPS } from '@/utils/constants.js'

const isEdit = ref(false)
const roleId = ref('')
const submitting = ref(false)
const permissionGroups = PERMISSION_GROUPS
const selectedPermissions = ref([])

const form = reactive({ roleName: '', roleCode: '' })

function onPermissionChange(group, value, event) {
  const values = event.detail.value
  if (values.includes(value)) {
    if (!selectedPermissions.value.includes(value)) selectedPermissions.value.push(value)
  } else {
    selectedPermissions.value = selectedPermissions.value.filter(p => p !== value)
  }
}

async function handleSubmit() {
  if (!form.roleName) { uni.showToast({ title: '请输入角色名称', icon: 'none' }); return }
  if (!form.roleCode) { uni.showToast({ title: '请输入角色编码', icon: 'none' }); return }
  submitting.value = true
  try {
    const data = { roleName: form.roleName, roleCode: form.roleCode, permissions: selectedPermissions.value }
    if (isEdit.value) await updateRole(roleId.value, data)
    else await createRole(data)
    uni.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) { /* handled */ } finally { submitting.value = false }
}

onLoad(async (query) => {
  if (query.id) {
    isEdit.value = true
    roleId.value = query.id
    // 加载角色详情预填
    const res = await getRoleList({ id: query.id })
    if (res.records?.length) {
      const role = res.records[0]
      form.roleName = role.roleName
      form.roleCode = role.roleCode
      selectedPermissions.value = role.permissions || []
    }
  }
})
</script>

<style lang="scss" scoped>
.role-edit-page { min-height: 100vh; padding: $spacing-lg; background: $color-bg-page; }
.nav-bar { display: flex; align-items: center; gap: $spacing-sm; margin-bottom: $spacing-lg; }
.nav-title { font-size: $font-size-lg; font-weight: 600; }
.form-section { background: $color-bg-card; border-radius: $radius-lg; padding: $spacing-base; margin-bottom: $spacing-base; box-shadow: $shadow-card; }
.section-title { font-size: $font-size-base; font-weight: 600; margin-bottom: $spacing-base; display: block; }
.form-item { margin-bottom: $spacing-base; }
.form-label { font-size: $font-size-sm; color: $color-text-secondary; margin-bottom: $spacing-xs; display: block; }
.permission-group { margin-bottom: $spacing-base; }
.group-header { margin-bottom: $spacing-xs; }
.group-label { font-size: $font-size-sm; font-weight: 600; color: $color-text-primary; }
.permission-item { padding: 8rpx 0; }
.checkbox-label { display: flex; align-items: center; gap: $spacing-sm; }
.checkbox-text { font-size: $font-size-sm; color: $color-text-secondary; }
.bottom-actions { display: flex; gap: $spacing-base; margin-top: $spacing-lg; }
.btn-cancel { flex: 1; background: transparent; border: 1rpx solid $color-border; color: $color-text-secondary; border-radius: $radius-base; height: 88rpx; line-height: 88rpx; }
.btn-confirm { flex: 2; background: $color-primary; color: #FFF; border: none; border-radius: $radius-base; height: 88rpx; line-height: 88rpx; }
</style>
```

- [ ] **Step 2: Commit**

```bash
git add pages/admin/role/edit.vue
git commit -m "feat(admin): 实现新增/编辑角色（权限树checkbox分组/编辑预填/编码锁定）"
```

---

### Task 6.3: 分配角色给用户

**Files:**
- Rewrite: `pages/admin/role/assign.vue`

- [ ] **Step 1: 实现分配角色页**

页面结构：导航栏（返回+"分配角色"）、搜索区（手机号/姓名搜索用户）、用户信息卡片（搜索后显示：姓名、手机号、当前角色标签列表）、角色选择区（角色列表多选checkbox，已分配的角色勾选）、底部保存/取消按钮。校验：必须先搜索并选中用户。

- [ ] **Step 2: Commit**

```bash
git add pages/admin/role/assign.vue
git commit -m "feat(admin): 实现分配角色（搜索用户/角色多选/已分配预填）"
```

---

### Task 6.4: 系统配置

**Files:**
- Rewrite: `pages/admin/config/index.vue`

- [ ] **Step 1: 实现系统配置页**

页面结构：配置项列表（配置项名称、配置键code、当前值、说明）。点击某项弹出编辑弹窗（uni-popup或modal内输入框），修改后调用 `updateConfig` 保存。仅SUPER_ADMIN可访问。

- [ ] **Step 2: Commit**

```bash
git add pages/admin/config/index.vue
git commit -m "feat(admin): 实现系统配置（列表展示/弹窗编辑/仅SUPER_ADMIN可见）"
```

---

## Phase 7: 统计分析 + 管理中心首页 + 个人中心

### Task 7.1: 接种统计

**Files:**
- Rewrite: `pages/stats/vaccination.vue`

- [ ] **Step 1: 实现接种统计页**

```vue
<!-- pages/stats/vaccination.vue -->
<template>
  <view class="stats-page">
    <uni-nav-bar title="接种统计" :border="false" />

    <!-- 筛选区 -->
    <view class="filter-section">
      <view class="filter-row">
        <view class="filter-item">
          <text class="filter-label">统计维度</text>
          <picker :range="['日', '周', '月', '疫苗', '医生', '窗口']" @change="onDimensionChange">
            <view class="filter-value">{{ filter.dimension || '日' }}</view>
          </picker>
        </view>
        <view class="filter-item">
          <text class="filter-label">日期范围</text>
          <uni-datetime-picker type="daterange" v-model="filter.dateRange" start="2025-01-01" end="2026-12-31" />
        </view>
        <view class="filter-item" v-if="filter.dimension === '疫苗'">
          <picker :range="vaccineNames" @change="onVaccineChange">
            <view class="filter-value">{{ filter.vaccine || '全部' }}</view>
          </picker>
        </view>
        <button class="query-btn" @tap="queryStats">查询</button>
      </view>
    </view>

    <!-- 汇总卡片 -->
    <view class="summary-scroll">
      <view class="summary-card">
        <text class="summary-value">{{ stats.totalCount }}</text>
        <text class="summary-label">总接种人次</text>
      </view>
      <view class="summary-card">
        <text class="summary-value">{{ stats.successCount }}</text>
        <text class="summary-label">成功接种</text>
      </view>
      <view class="summary-card">
        <text class="summary-value">{{ stats.successRate }}%</text>
        <text class="summary-label">预约完成率</text>
      </view>
    </view>

    <!-- 图表区 -->
    <view class="chart-section">
      <ChartPanel canvasId="vaccinationChart" :chartType="chartType" :chartData="chartData" />
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
// uni-app 全局生命周期（onShow）无需从 Vue 导入
import { getVaccinationStats } from '@/api/stats.js'
import ChartPanel from '@/components/ChartPanel/ChartPanel.vue'

const filter = reactive({ dimension: '日', dateRange: [], vaccine: '' })
const stats = ref({ totalCount: 0, successCount: 0, successRate: '0%' })
const chartData = ref([])
const chartType = ref('bar')

const vaccineNames = ['全部']

onShow(() => { queryStats() })

async function queryStats() {
  uni.showLoading({ title: '加载中...' })
  try {
    const params = { statsType: filter.dimension }
    if (filter.dateRange && filter.dateRange.length === 2) {
      params.startDate = filter.dateRange[0]
      params.endDate = filter.dateRange[1]
    }
    if (filter.vaccine) params.vaccineName = filter.vaccine
    const data = await getVaccinationStats(params)
    stats.value = data
    chartData.value = data.details || []
    chartType.value = data.details?.length > 1 ? 'bar' : 'bar'
  } catch (e) {
    uni.showToast({ title: '查询失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

function onDimensionChange(e) {
  filter.dimension = e.detail.value
}

function onVaccineChange(e) {
  filter.vaccine = e.detail.value
}
</script>

<style lang="scss" scoped>
.stats-page {
  min-height: 100vh;
  background-color: $color-bg-page;
  padding: $spacing-lg;

  .filter-section {
    padding: $spacing-md $spacing-lg;
    background: #FFFFFF;
    border-radius: $border-radius-lg;
    margin-bottom: $spacing-md;

    .filter-row {
      display: flex;
      flex-wrap: wrap;
      gap: $spacing-md;

      .filter-item { flex: 1; min-width: 200rpx; }
      .filter-label { font-size: $font-size-sm; color: $color-text-secondary; margin-bottom: $spacing-xs; }
      .filter-value { font-size: $font-size-base; color: $color-text-primary; padding: $spacing-xs $spacing-sm; background: $color-bg-grey; border-radius: $border-radius-sm; }

      .query-btn {
        padding: $spacing-xs $spacing-lg;
        background-color: $color-primary;
        color: #FFFFFF;
        font-size: $font-size-base;
        border: none;
        border-radius: $border-radius-md;
      }
    }
  }

  .summary-scroll {
    display: flex;
    gap: $spacing-md;
    overflow-x: auto;
    padding: $spacing-md 0;
    margin-bottom: $spacing-lg;
    -webkit-overflow-scrolling: touch;

    .summary-card {
      flex: 1;
      min-width: 180rpx;
      padding: $spacing-lg;
      background: #FFFFFF;
      border-radius: $border-radius-lg;
      box-shadow: $shadow-card;
      text-align: center;

      .summary-value { font-size: $font-size-xxl; font-weight: $font-weight-bold; color: $color-text-primary; }
      .summary-label { font-size: $font-size-xs; color: $color-text-secondary; margin-top: $spacing-xs; }
    }
  }

  .chart-section {
    background: #FFFFFF;
    border-radius: $border-radius-lg;
    padding: $spacing-lg;
    min-height: 500rpx;
  }
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add pages/stats/vaccination.vue
git commit -m "feat(admin): 实现接种统计（多维筛选/汇总卡片/u-charts图表/手动查询）"
```

---

### Task 7.2: 库存统计

**Files:**
- Rewrite: `pages/stats/stock.vue`

- [ ] **Step 1: 实现库存统计页**

页面结构：筛选区（统计类型下拉：总览/批次/预警 + 疫苗选填下拉）、汇总卡片（总库存、可用库存、锁定库存、使用率百分比、临期数、过期数）、图表区（饼图库存分布、柱状图各疫苗库存对比）、预警列表（仅统计类型为"预警"时显示预警批次卡片）。手动查询。

- [ ] **Step 2: Commit**

```bash
git add pages/stats/stock.vue
git commit -m "feat(admin): 实现库存统计（总览/批次/预警三模式/饼图+柱状图）"
```

---

### Task 7.3: 效率统计

**Files:**
- Rewrite: `pages/stats/efficiency.vue`

- [ ] **Step 1: 实现效率统计页**

页面结构：筛选区（统计类型下拉：时长/排队/完成率 + 日期范围 + 窗口选填下拉）、汇总卡片（平均签到时长、平均预检时长、平均登记时长、平均接种时长、平均留观时长，单位分钟）、图表区（柱状图各环节时长对比、折线图完成率趋势）。手动查询。

- [ ] **Step 2: Commit**

```bash
git add pages/stats/efficiency.vue
git commit -m "feat(admin): 实现效率统计（5环节平均时长/柱状图对比/折线图趋势）"
```

---

### Task 7.4: 异常统计

**Files:**
- Rewrite: `pages/stats/anomaly.vue`

- [ ] **Step 1: 实现异常统计页**

页面结构：筛选区（日期范围 + 异常维度多选checkbox：爽约/取消/预检失败/不良反应/过期批次）、汇总卡片（各维度异常数量，异常项红色高亮）、图表区（u-charts柱状图各异常类型分布）。手动查询。

- [ ] **Step 2: Commit**

```bash
git add pages/stats/anomaly.vue
git commit -m "feat(admin): 实现异常统计（5维度多选/异常红色高亮/柱状图分布）"
```

---

### Task 7.5: 管理中心入口

**Files:**
- Rewrite: `pages/admin/index.vue`

- [ ] **Step 1: 实现管理中心入口页**

页面结构：顶部管理员姓名+角色+今日日期、统计概览卡片横向滚动（SUPER_ADMIN/DOCTOR_BUSINESS_ADMIN：今日接种数/库存预警数/待审批公告数）、功能导航网格（根据角色动态显示）：
- SUPER_ADMIN：排班管理、窗口管理、疫苗管理、公告管理、角色权限、系统配置（6项）
- DOCTOR_BUSINESS_ADMIN：排班管理（只读）、窗口管理、疫苗管理、公告管理、角色权限（5项）
- DOCTOR_SCHEDULE：排班管理（完整CRUD，1项）

每个入口使用AdminNavCard组件（图标+名称+角色标签：只读/可编辑）。公告轮播（横向滚动）。TabBar页面。

- [ ] **Step 2: Commit**

```bash
git add pages/admin/index.vue
git commit -m "feat(admin): 实现管理中心入口（角色动态导航网格/统计概览卡片/公告轮播）"
```

---

### Task 7.6: 管理员首页

**Files:**
- Rewrite: `pages/index/index.vue`（管理员端首页，复用医生端页面结构，根据角色动态展示）

- [ ] **Step 1: 扩展首页支持管理员角色**

在现有首页基础上，添加管理员角色的 `stats` 和 `shortcuts` 计算：

- SUPER_ADMIN：今日接种数/待审批公告数/库存预警数/异常事件数，快捷入口：管理中心/统计分析/排班管理/疫苗管理
- DOCTOR_BUSINESS_ADMIN：今日接种数/库存预警数/待审批公告数，快捷入口：管理中心/排班管理/疫苗管理/公告管理
- DOCTOR_SCHEDULE：今日排班数/待执行排班数，快捷入口：排班管理

管理员首页同样包含公告轮播。60秒轮询刷新概览数据（onShow启动/onHide停止）。

- [ ] **Step 2: Commit**

```bash
git add pages/index/index.vue
git commit -m "feat(admin): 扩展首页支持3个管理员角色（动态概览/快捷入口/60秒轮询）"
```

---

### Task 7.7: 管理员个人中心

**Files:**
- Rewrite: `pages/mine/index.vue`（管理员端个人中心，复用医生端页面结构）

- [ ] **Step 1: 扩展个人中心支持管理员角色**

复用医生端个人中心结构：UserInfoCard（姓名、角色标签、手机号脱敏）+ 菜单列表（修改密码、关于系统）+ 退出登录按钮（二次确认弹窗）。角色标签根据管理员角色显示：超级管理员/业务管理员/排班管理员。

- [ ] **Step 2: Commit**

```bash
git add pages/mine/index.vue
git commit -m "feat(admin): 扩展个人中心支持管理员角色标签"
```

---

## 验证清单

Phase 1 完成后验证：
- [ ] 8个API模块创建完成，路径与设计文档一致
- [ ] useAdminStore筛选条件缓存/重置正常
- [ ] useAdminFilter hook工作正常
- [ ] 4个业务组件（AdminNavCard/StatsCard/ChartPanel/FilterBar）渲染正常
- [ ] utils/tabBar.js包含3个管理员角色配置
- [ ] utils/constants.js包含所有管理员端状态常量
- [ ] useUserStore管理员角色getter返回正确

Phase 2 完成后验证：
- [ ] 排班列表FilterBar筛选正常
- [ ] DOCTOR_BUSINESS_ADMIN仅查看，DOCTOR_SCHEDULE可CRUD
- [ ] 新增排班冲突检测正常（同医生同日期时间重叠）
- [ ] 过期排班编辑页时间选择器置灰
- [ ] 删除有关联预约的排班返回错误码8002

Phase 3 完成后验证：
- [ ] 窗口列表职能类型标签颜色正确（6种）
- [ ] 新增窗口职能类型联动角色编码
- [ ] 配置窗口服务保存正常
- [ ] 删除有关联数据的窗口返回错误码8004

Phase 4 完成后验证：
- [ ] 疫苗列表类别/状态筛选正常
- [ ] 上下架切换二次确认弹窗
- [ ] 新增疫苗月龄范围校验（最小<=最大）
- [ ] 删除有批次记录的疫苗返回错误码8006

Phase 5 完成后验证：
- [ ] 公告列表角色操作权限正确（BUSINESS_ADMIN无审批按钮）
- [ ] 发布公告提交后状态为PENDING
- [ ] 审批公告拒绝原因展示正常
- [ ] 公告反馈手机号脱敏（前3后4）
- [ ] 仅SUPER_ADMIN可审批/删除公告

Phase 6 完成后验证：
- [ ] 系统内置角色隐藏编辑/删除按钮
- [ ] 权限树6个分组checkbox正常
- [ ] 分配角色搜索用户后显示当前角色
- [ ] 系统配置弹窗编辑保存正常
- [ ] 仅SUPER_ADMIN可访问系统配置页

Phase 7 完成后验证：
- [ ] 4个统计页面手动查询正常
- [ ] u-charts图表渲染正常（柱状图/折线图/饼图）
- [ ] 管理中心导航网格根据角色动态显示
- [ ] 首页概览60秒轮询，离开页面停止
- [ ] 个人中心角色标签正确
- [ ] 仅SUPER_ADMIN可见统计分析Tab
