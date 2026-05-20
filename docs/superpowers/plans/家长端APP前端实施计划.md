# 家长端APP前端实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建疫苗管理系统V2家长端APP，支持家长管理儿童档案、预约接种、查看流程指引、浏览疫苗目录和系统公告，涵盖认证、儿童管理、预约、疫苗目录、公告、接种记录等完整功能。

**Architecture:** 单一uni-app项目，三端共用基础设施层（request/auth/constants/scss/CustomTabBar），家长端使用原生TabBar（3 tabs）。Pinia管理状态，API层按模块拆分，Hooks封装业务逻辑。

**Tech Stack:** uni-app + Vue 3 (Composition API) + Pinia + uni-ui + SCSS

**设计文档：** `docs/superpowers/specs/家长端APP前端设计.md`
**前置条件：** 前端共享基础设施实施计划已完成（request.js/auth.js/constants.js/store/user.js/hooks/usePagination.js/hooks/useAuth.js/components/EmptyState/StatusTag/CustomTabBar/App.vue/pages.json/api/auth.js/pages/auth/login.vue/pages/common/404.vue 均已实现）

---

## 范围说明

本计划依赖共享基础设施计划，仅包含家长端业务页面。共享基础设施已提供：HTTP客户端、认证工具、用户Store、分页Hook、认证Hook、EmptyState/StatusTag组件、CustomTabBar、登录页、404页、App.vue路由守卫、65个路由注册、原生TabBar配置。

| Phase | 内容 | 依赖 |
|-------|------|------|
| Phase 1 | Store + API层 + 业务组件（3个Store、6个API模块、3个Hook、9个组件） | 共享基础设施 |
| Phase 2 | 认证页面（注册、忘记密码、修改密码） | 共享基础设施 |
| Phase 3 | 首页 + 儿童管理（首页、儿童列表、添加/编辑儿童） | Phase 1 |
| Phase 4 | 预约模块（创建预约3步、预约列表、预约详情、流程指引） | Phase 1 |
| Phase 5 | 疫苗目录 + 公告 + 接种记录 | Phase 1 |

---

## Phase 1: Store + API层 + 业务组件

### Task 1.1: API模块

**Files:**
- Create: `api/user.js`
- Create: `api/child.js`
- Create: `api/appointment.js`
- Create: `api/vaccine.js`
- Create: `api/notice.js`
- Create: `api/record.js`

- [ ] **Step 1: 创建 api/user.js**

```js
// api/user.js
import { get, put } from '@/utils/request.js'

export function getUserProfile() {
  return get('/api/v1/user/profile')
}

export function updateUserProfile(data) {
  return put('/api/v1/user/profile', data)
}
```

- [ ] **Step 2: 创建 api/child.js**

```js
// api/child.js
import { get, post, put, del } from '@/utils/request.js'

export function getChildList() {
  return get('/api/v1/user/children')
}

export function addChild(data) {
  return post('/api/v1/user/children', data, { showLoading: true })
}

export function updateChild(childId, data) {
  return put(`/api/v1/user/children/${childId}`, data, { showLoading: true })
}

export function deleteChild(childId) {
  return del(`/api/v1/user/children/${childId}`, {}, { showLoading: true })
}
```

- [ ] **Step 3: 创建 api/appointment.js**

```js
// api/appointment.js
import { get, post } from '@/utils/request.js'

export function createAppointment(data) {
  return post('/api/v1/user/appointments', data, { showLoading: true })
}

export function cancelAppointment(id) {
  return post(`/api/v1/user/appointments/${id}/cancel`, {}, { showLoading: true })
}

export function getAppointmentList(params) {
  return get('/api/v1/user/appointments', params)
}

export function getAppointmentDetail(id) {
  return get(`/api/v1/user/appointments/${id}`)
}

export function getAppointmentGuide(id) {
  return get(`/api/v1/user/appointments/${id}/guide`)
}

export function getAppointmentQueue(id) {
  return get(`/api/v1/user/appointments/${id}/queue`)
}
```

- [ ] **Step 4: 创建 api/vaccine.js**

```js
// api/vaccine.js
import { get } from '@/utils/request.js'

export function getVaccineList(params) {
  return get('/api/v1/public/vaccines', params, { needToken: false })
}
```

- [ ] **Step 5: 创建 api/notice.js**

```js
// api/notice.js
import { get, post } from '@/utils/request.js'

export function getNoticeList(params) {
  return get('/api/v1/public/notices', params, { needToken: false })
}

export function submitFeedback(noticeId, data) {
  return post(`/api/v1/public/notices/${noticeId}/feedback`, data, { showLoading: true })
}
```

- [ ] **Step 6: 创建 api/record.js**

```js
// api/record.js
import { get } from '@/utils/request.js'

export function getRecordList(params) {
  return get('/api/v1/user/records', params)
}

export function getChildRecords(childId, params) {
  return get(`/api/v1/user/records/child/${childId}`, params)
}
```

- [ ] **Step 7: Commit**

```bash
git add api/user.js api/child.js api/appointment.js api/vaccine.js api/notice.js api/record.js
git commit -m "feat(parent): 创建6个API模块（user/child/appointment/vaccine/notice/record）"
```

---

### Task 1.2: Pinia Store

**Files:**
- Create: `store/child.js`
- Create: `store/appointment.js`

- [ ] **Step 1: 创建 store/child.js**

```js
// store/child.js
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getChildList, addChild, updateChild, deleteChild } from '@/api/child.js'

export const useChildStore = defineStore('child', () => {
  const children = ref([])
  const currentChild = ref(null)
  const loading = ref(false)

  async function fetchChildren() {
    loading.value = true
    try {
      children.value = await getChildList()
    } catch (e) {
      console.error('获取儿童列表失败', e)
    } finally {
      loading.value = false
    }
  }

  async function add(data) {
    const res = await addChild(data)
    await fetchChildren()
    return res
  }

  async function update(childId, data) {
    const res = await updateChild(childId, data)
    await fetchChildren()
    return res
  }

  async function remove(childId) {
    await deleteChild(childId)
    await fetchChildren()
  }

  return { children, currentChild, loading, fetchChildren, add, update, remove }
})
```

- [ ] **Step 2: 创建 store/appointment.js**

```js
// store/appointment.js
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getAppointmentList, getAppointmentDetail, getAppointmentGuide, getAppointmentQueue } from '@/api/appointment.js'

export const useAppointmentStore = defineStore('appointment', () => {
  const appointments = ref([])
  const currentAppointment = ref(null)
  const guideData = ref(null)
  const queueData = ref(null)
  const filters = ref({ status: '', page: 1, size: 20 })
  const total = ref(0)
  const loading = ref(false)

  async function fetchAppointments(reset = true) {
    if (reset) filters.value.page = 1
    loading.value = true
    try {
      const data = await getAppointmentList(filters.value)
      appointments.value = data.records || data || []
      total.value = data.total || 0
    } catch (e) {
      console.error('获取预约列表失败', e)
    } finally {
      loading.value = false
    }
  }

  async function fetchDetail(id) {
    currentAppointment.value = await getAppointmentDetail(id)
    return currentAppointment.value
  }

  async function fetchGuide(id) {
    guideData.value = await getAppointmentGuide(id)
    return guideData.value
  }

  async function fetchQueue(id) {
    queueData.value = await getAppointmentQueue(id)
    return queueData.value
  }

  return { appointments, currentAppointment, guideData, queueData, filters, total, loading, fetchAppointments, fetchDetail, fetchGuide, fetchQueue }
})
```

- [ ] **Step 3: Commit**

```bash
git add store/child.js store/appointment.js
git commit -m "feat(parent): 创建useChildStore和useAppointmentStore"
```

---

### Task 1.3: 业务Hooks

**Files:**
- Create: `hooks/useChild.js`
- Create: `hooks/useAppointment.js`

- [ ] **Step 1: 创建 hooks/useChild.js**

```js
// hooks/useChild.js
import { ref, onMounted } from 'vue'
import { useChildStore } from '@/store/child.js'

export function useChild() {
  const childStore = useChildStore()
  const submitting = ref(false)

  onMounted(() => {
    if (childStore.children.length === 0) {
      childStore.fetchChildren()
    }
  })

  async function addChild(data) {
    submitting.value = true
    try {
      return await childStore.add(data)
    } finally {
      submitting.value = false
    }
  }

  async function updateChild(childId, data) {
    submitting.value = true
    try {
      return await childStore.update(childId, data)
    } finally {
      submitting.value = false
    }
  }

  async function deleteChild(childId) {
    submitting.value = true
    try {
      return await childStore.remove(childId)
    } finally {
      submitting.value = false
    }
  }

  return { children: childStore.children, currentChild: childStore.currentChild, submitting, addChild, updateChild, deleteChild }
}
```

- [ ] **Step 2: 创建 hooks/useAppointment.js**

```js
// hooks/useAppointment.js
import { ref } from 'vue'
import { createAppointment, cancelAppointment } from '@/api/appointment.js'
import { useAppointmentStore } from '@/store/appointment.js'

export function useAppointment() {
  const appointmentStore = useAppointmentStore()
  const submitting = ref(false)

  async function create(data) {
    submitting.value = true
    try {
      return await createAppointment(data)
    } finally {
      submitting.value = false
    }
  }

  async function cancel(id) {
    submitting.value = true
    try {
      return await cancelAppointment(id)
    } finally {
      submitting.value = false
    }
  }

  return { appointments: appointmentStore.appointments, currentAppointment: appointmentStore.currentAppointment, submitting, create, cancel, fetchAppointments: appointmentStore.fetchAppointments, fetchDetail: appointmentStore.fetchDetail, fetchGuide: appointmentStore.fetchGuide, fetchQueue: appointmentStore.fetchQueue }
}
```

- [ ] **Step 3: Commit**

```bash
git add hooks/useChild.js hooks/useAppointment.js
git commit -m "feat(parent): 创建useChild和useAppointment hooks"
```

---

### Task 1.4: 业务组件

**Files:**
- Create: `components/AppStatusTag/AppStatusTag.vue`
- Create: `components/AppointmentCard/AppointmentCard.vue`
- Create: `components/ChildCard/ChildCard.vue`
- Create: `components/VaccineCard/VaccineCard.vue`
- Create: `components/NoticeCard/NoticeCard.vue`
- Create: `components/StepIndicator/StepIndicator.vue`
- Create: `components/QueueInfoCard/QueueInfoCard.vue`
- Create: `components/UserInfoCard/UserInfoCard.vue`
- Create: `components/RecordCard/RecordCard.vue`

- [ ] **Step 1: 创建 AppStatusTag**

```vue
<!-- components/AppStatusTag/AppStatusTag.vue -->
<template>
  <view class="app-status-tag" :style="{ backgroundColor: bgColor, color: textColor }">
    <text>{{ text }}</text>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { APPOINTMENT_STATUS_TEXT, APPOINTMENT_STATUS_COLOR } from '@/utils/constants.js'

const props = defineProps({ status: { type: Number, required: true } })

const text = computed(() => APPOINTMENT_STATUS_TEXT[props.status] || '未知')
const bgColor = computed(() => (APPOINTMENT_STATUS_COLOR[props.status] || '#999999') + '1A')
const textColor = computed(() => APPOINTMENT_STATUS_COLOR[props.status] || '#999999')
</script>

<style lang="scss" scoped>
.app-status-tag {
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

- [ ] **Step 2: 创建 AppointmentCard**

```vue
<!-- components/AppointmentCard/AppointmentCard.vue -->
<template>
  <view class="appointment-card" @tap="$emit('click', appointment)">
    <view class="card-header">
      <text class="vaccine-name">{{ appointment.vaccineName }}</text>
      <AppStatusTag :status="appointment.status" />
    </view>
    <view class="card-body">
      <text class="info-text">👶 {{ appointment.childName }}</text>
      <text class="info-text">📅 {{ appointment.appointmentDate }}</text>
      <text class="info-text">🕐 {{ appointment.timeSlot === 'AM' ? '上午' : '下午' }}</text>
    </view>
    <slot name="action" />
  </view>
</template>

<script setup>
import AppStatusTag from '@/components/AppStatusTag/AppStatusTag.vue'
defineProps({ appointment: { type: Object, required: true } })
defineEmits(['click'])
</script>

<style lang="scss" scoped>
.appointment-card {
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
.vaccine-name { font-size: $font-size-base; font-weight: 600; color: $color-text-primary; }
.card-body { display: flex; flex-direction: column; gap: 4rpx; }
.info-text { font-size: $font-size-sm; color: $color-text-secondary; }
</style>
```

- [ ] **Step 3: 创建 ChildCard**

```vue
<!-- components/ChildCard/ChildCard.vue -->
<template>
  <view class="child-card" @tap="$emit('click', child)">
    <view class="avatar" :style="{ backgroundColor: child.gender === 1 ? '#1989FA' : '#EE0A24' }">
      <text class="avatar-text">{{ (child.name || '?').charAt(0) }}</text>
    </view>
    <view class="child-info">
      <text class="child-name">{{ child.name }}</text>
      <text class="child-detail">{{ genderText }} · {{ ageText }}</text>
      <text class="child-id">{{ maskedId }}</text>
    </view>
    <view v-if="selectable" class="radio-wrap">
      <uni-icons :type="selected ? 'radio-dot' : 'circle'" :size="22" :color="selected ? '#07C160' : '#C0C0C0'" />
    </view>
    <uni-icons v-else type="right" :size="16" color="#C0C0C0" />
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { maskIdCard } from '@/utils/format.js'

const props = defineProps({
  child: { type: Object, required: true },
  selectable: { type: Boolean, default: false },
  selected: { type: Boolean, default: false },
  showDelete: { type: Boolean, default: false }
})
defineEmits(['click', 'delete'])

const genderText = computed(() => props.child.gender === 1 ? '男' : props.child.gender === 2 ? '女' : '未知')
const maskedId = computed(() => maskIdCard(props.child.idCardNo))
const ageText = computed(() => {
  if (!props.child.birthDate) return ''
  const birth = new Date(props.child.birthDate)
  const now = new Date()
  const months = (now.getFullYear() - birth.getFullYear()) * 12 + (now.getMonth() - birth.getMonth())
  const years = Math.floor(months / 12)
  const remainMonths = months % 12
  return years > 0 ? `${years}岁${remainMonths > 0 ? remainMonths + '个月' : ''}` : `${remainMonths}个月`
})
</script>

<style lang="scss" scoped>
.child-card {
  display: flex;
  align-items: center;
  background: $color-bg-card;
  border-radius: $radius-lg;
  padding: $spacing-base;
  margin-bottom: $spacing-base;
  box-shadow: $shadow-card;
}
.avatar {
  width: 80rpx; height: 80rpx; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  margin-right: $spacing-base;
  flex-shrink: 0;
}
.avatar-text { color: #fff; font-size: $font-size-lg; font-weight: 600; }
.child-info { flex: 1; display: flex; flex-direction: column; gap: 4rpx; }
.child-name { font-size: $font-size-base; font-weight: 600; color: $color-text-primary; }
.child-detail { font-size: $font-size-sm; color: $color-text-secondary; }
.child-id { font-size: $font-size-xs; color: $color-text-placeholder; }
.radio-wrap { flex-shrink: 0; }
</style>
```

- [ ] **Step 4: 创建 VaccineCard**

```vue
<!-- components/VaccineCard/VaccineCard.vue -->
<template>
  <view class="vaccine-card" @tap="$emit('click', vaccine)">
    <view class="card-header">
      <text class="vaccine-name">{{ vaccine.name }}</text>
      <view class="category-tag" :class="vaccine.category === 'CLASS_I' ? 'class-i' : 'class-ii'">
        <text>{{ vaccine.category === 'CLASS_I' ? '一类' : '二类' }}</text>
      </view>
    </view>
    <view class="card-body">
      <text class="info-text">厂家：{{ vaccine.manufacturer }}</text>
      <text class="info-text">适龄：{{ vaccine.minAgeMonth }}-{{ vaccine.maxAgeMonth }}月龄</text>
      <text class="info-text">剂次：共{{ vaccine.doses }}剂，间隔{{ vaccine.intervalDays }}天</text>
    </view>
    <view v-if="selectable" class="radio-wrap">
      <uni-icons :type="selected ? 'radio-dot' : 'circle'" :size="22" :color="selected ? '#07C160' : '#C0C0C0'" />
    </view>
    <uni-icons v-else type="right" :size="16" color="#C0C0C0" />
  </view>
</template>

<script setup>
defineProps({
  vaccine: { type: Object, required: true },
  selectable: { type: Boolean, default: false },
  selected: { type: Boolean, default: false }
})
defineEmits(['click'])
</script>

<style lang="scss" scoped>
.vaccine-card {
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
.vaccine-name { font-size: $font-size-base; font-weight: 600; color: $color-text-primary; flex: 1; }
.category-tag {
  padding: 4rpx 16rpx;
  border-radius: $radius-sm;
  font-size: $font-size-xs;
}
.class-i { background: rgba(7,193,96,0.1); color: #07C160; }
.class-ii { background: rgba(25,137,250,0.1); color: #1989FA; }
.card-body { display: flex; flex-direction: column; gap: 4rpx; }
.info-text { font-size: $font-size-sm; color: $color-text-secondary; }
.radio-wrap { flex-shrink: 0; }
</style>
```

- [ ] **Step 5: 创建 NoticeCard**

```vue
<!-- components/NoticeCard/NoticeCard.vue -->
<template>
  <view class="notice-card" @tap="$emit('click', notice)">
    <view class="notice-header">
      <view class="type-tag" :class="typeClass">{{ typeText }}</view>
      <text class="notice-title">{{ notice.title }}</text>
    </view>
    <text class="notice-date">{{ notice.createdAt }}</text>
  </view>
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps({ notice: { type: Object, required: true } })
defineEmits(['click'])
const typeClass = computed(() => {
  const map = { NORMAL: 'type-normal', URGENT: 'type-urgent', SYSTEM: 'type-system' }
  return map[props.notice.type] || 'type-normal'
})
const typeText = computed(() => {
  const map = { NORMAL: '公告', URGENT: '紧急', SYSTEM: '系统' }
  return map[props.notice.type] || '公告'
})
</script>

<style lang="scss" scoped>
.notice-card {
  padding: $spacing-base 0;
  border-bottom: 1rpx solid $color-border-light;
}
.notice-header { display: flex; align-items: center; gap: $spacing-sm; margin-bottom: $spacing-xs; }
.type-tag {
  padding: 4rpx 12rpx; border-radius: $radius-sm; font-size: $font-size-xs; flex-shrink: 0;
}
.type-normal { background: rgba(25,137,250,0.1); color: #1989FA; }
.type-urgent { background: rgba(238,10,36,0.1); color: #EE0A24; }
.type-system { background: rgba(7,193,96,0.1); color: #07C160; }
.notice-title { font-size: $font-size-base; color: $color-text-primary; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.notice-date { font-size: $font-size-xs; color: $color-text-placeholder; }
</style>
```

- [ ] **Step 6: 创建 StepIndicator**

```vue
<!-- components/StepIndicator/StepIndicator.vue -->
<template>
  <scroll-view scroll-x class="step-indicator">
    <view class="steps">
      <view v-for="(step, index) in steps" :key="index" class="step-item">
        <view class="step-dot" :class="{ completed: index < current, current: index === current }">
          <uni-icons v-if="index < current" type="checkmarkempty" :size="14" color="#07C160" />
          <view v-else-if="index === current" class="dot-active" />
          <view v-else class="dot-pending" />
        </view>
        <text class="step-text" :class="{ active: index <= current }">{{ step.title }}</text>
        <view v-if="index < steps.length - 1" class="step-line" :class="{ completed: index < current }" />
      </view>
    </view>
  </scroll-view>
</template>

<script setup>
const props = defineProps({
  steps: { type: Array, required: true },
  current: { type: Number, default: 0 }
})
</script>

<style lang="scss" scoped>
.step-indicator { white-space: nowrap; }
.steps { display: inline-flex; align-items: flex-start; padding: 0 $spacing-base; }
.step-item { display: flex; flex-direction: column; align-items: center; min-width: 100rpx; }
.step-dot {
  width: 40rpx; height: 40rpx; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  border: 2rpx solid $color-border;
  margin-bottom: $spacing-xs;
}
.step-dot.completed { border-color: #07C160; background: rgba(7,193,96,0.1); }
.step-dot.current { border-color: #07C160; }
.dot-active { width: 16rpx; height: 16rpx; border-radius: 50%; background: #07C160; }
.dot-pending { width: 12rpx; height: 12rpx; border-radius: 50%; background: $color-text-placeholder; }
.step-text { font-size: $font-size-xs; color: $color-text-placeholder; }
.step-text.active { color: $color-text-primary; font-weight: 500; }
.step-line {
  width: 40rpx; height: 2rpx; background: $color-border;
  margin: 0 $spacing-xs 18rpx;
  align-self: flex-start; margin-top: 18rpx;
}
.step-line.completed { background: #07C160; }
</style>
```

- [ ] **Step 7: 创建 QueueInfoCard**

```vue
<!-- components/QueueInfoCard/QueueInfoCard.vue -->
<template>
  <view class="queue-info-card" :class="{ calling: isCalling }">
    <text class="window-name">📍 {{ windowName }}</text>
    <text class="queue-position">{{ queuePosition }}</text>
    <text class="wait-info">前方还有 {{ queuePosition - 1 }} 人</text>
    <text v-if="estimatedWait" class="wait-info">预计等待约 {{ estimatedWait }} 分钟</text>
  </view>
</template>

<script setup>
defineProps({
  windowName: { type: String, default: '' },
  queuePosition: { type: Number, default: 0 },
  estimatedWait: { type: Number, default: 0 },
  isCalling: { type: Boolean, default: false }
})
</script>

<style lang="scss" scoped>
.queue-info-card {
  background: $color-primary-light;
  border-radius: $radius-lg;
  padding: $spacing-base;
  text-align: center;
  margin: $spacing-md 0;
}
.queue-info-card.calling { border: 2rpx solid $color-warning; animation: blink 1s infinite; }
@keyframes blink { 0%, 100% { border-color: $color-warning; } 50% { border-color: transparent; } }
.window-name { display: block; font-size: $font-size-sm; color: $color-text-secondary; margin-bottom: $spacing-sm; }
.queue-position { display: block; font-size: 80rpx; font-weight: 600; color: $color-primary; }
.wait-info { display: block; font-size: $font-size-sm; color: $color-text-secondary; margin-top: 4rpx; }
</style>
```

- [ ] **Step 8: 创建 UserInfoCard**

```vue
<!-- components/UserInfoCard/UserInfoCard.vue -->
<template>
  <view class="user-info-card">
    <view class="avatar">{{ avatarText }}</view>
    <view class="user-detail">
      <text class="user-name">{{ userInfo.realName || '未设置' }}</text>
      <text class="user-phone">{{ maskedPhone }}</text>
      <text class="user-status">账号状态正常</text>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { maskPhone } from '@/utils/format.js'

const props = defineProps({ userInfo: { type: Object, required: true } })
const avatarText = computed(() => (props.userInfo.realName || '?').charAt(0))
const maskedPhone = computed(() => maskPhone(props.userInfo.phone))
</script>

<style lang="scss" scoped>
.user-info-card {
  background: $color-bg-card;
  border-radius: $radius-lg;
  padding: $spacing-xl $spacing-lg;
  display: flex;
  align-items: center;
  margin-bottom: $spacing-lg;
  box-shadow: $shadow-card;
}
.avatar {
  width: 120rpx; height: 120rpx; border-radius: 50%;
  background: $color-primary;
  color: #fff; font-size: 48rpx;
  display: flex; align-items: center; justify-content: center;
  margin-right: $spacing-base; flex-shrink: 0;
}
.user-detail { display: flex; flex-direction: column; gap: 4rpx; }
.user-name { font-size: $font-size-lg; font-weight: 600; color: $color-text-primary; }
.user-phone { font-size: $font-size-sm; color: $color-text-secondary; }
.user-status { font-size: $font-size-xs; color: $color-success; }
</style>
```

- [ ] **Step 9: 创建 RecordCard**

```vue
<!-- components/RecordCard/RecordCard.vue -->
<template>
  <view class="record-card">
    <view class="card-header">
      <text class="vaccine-name">{{ record.vaccineName }}</text>
      <view class="status-tag status-completed">已完成 ✓</view>
    </view>
    <view class="card-body">
      <text class="info-text">👶 {{ record.childName }}</text>
      <text class="info-text">📅 {{ record.injectionDate }}</text>
      <text class="info-text">🏥 {{ record.stationName }}</text>
      <text class="info-text">📋 批次: {{ record.batchNo }}</text>
    </view>
  </view>
</template>

<script setup>
defineProps({ record: { type: Object, required: true } })
</script>

<style lang="scss" scoped>
.record-card {
  background: $color-bg-card;
  border-radius: $radius-lg;
  padding: $spacing-base;
  margin-bottom: $spacing-base;
  box-shadow: $shadow-card;
}
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: $spacing-sm; }
.vaccine-name { font-size: $font-size-base; font-weight: 600; color: $color-text-primary; }
.status-tag { padding: 4rpx 16rpx; border-radius: $radius-sm; font-size: $font-size-xs; background: rgba(7,193,96,0.1); color: #07C160; }
.card-body { display: flex; flex-direction: column; gap: 4rpx; }
.info-text { font-size: $font-size-sm; color: $color-text-secondary; }
</style>
```

- [ ] **Step 10: Commit**

```bash
git add components/
git commit -m "feat(parent): 创建9个业务组件（AppStatusTag/AppointmentCard/ChildCard/VaccineCard/NoticeCard/StepIndicator/QueueInfoCard/UserInfoCard/RecordCard）"
```

---

## Phase 2: 认证页面

### Task 2.1: 注册页

**Files:**
- Rewrite: `pages/auth/register.vue`

- [ ] **Step 1: 实现注册页**

```vue
<!-- pages/auth/register.vue -->
<template>
  <view class="register-page">
    <uni-nav-bar title="注册" :border="false" />

    <!-- 注册须知 -->
    <view class="notice-card">
      <text class="notice-text">注册即表示您同意《用户服务协议》和《隐私政策》</text>
    </view>

    <!-- 表单 -->
    <view class="form-section">
      <uni-forms ref="formRef" :modelValue="form" :rules="rules" validate-trigger="bind">
        <!-- 手机号 -->
        <uni-forms-item label="手机号" name="phone" required>
          <uni-easyinput v-model="form.phone" type="number" maxlength="11" placeholder="请输入手机号" />
        </uni-forms-item>

        <!-- 验证码 -->
        <uni-forms-item label="验证码" name="smsCode" required>
          <view class="sms-row">
            <uni-easyinput v-model="form.smsCode" type="number" maxlength="6" placeholder="请输入验证码" />
            <button class="sms-btn" :disabled="smsCountdown > 0" @tap="sendSmsCode">
              {{ smsCountdown > 0 ? `${smsCountdown}s` : '获取验证码' }}
            </button>
          </view>
        </uni-forms-item>

        <!-- 密码 -->
        <uni-forms-item label="密码" name="password" required>
          <uni-easyinput v-model="form.password" type="password" placeholder="6-20位，需含字母和数字" />
        </uni-forms-item>

        <!-- 确认密码 -->
        <uni-forms-item label="确认密码" name="confirmPassword" required>
          <uni-easyinput v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" />
        </uni-forms-item>

        <!-- 真实姓名 -->
        <uni-forms-item label="真实姓名" name="realName" required>
          <uni-easyinput v-model="form.realName" placeholder="2-50位中文" />
        </uni-forms-item>
      </uni-forms>

      <!-- 注册按钮 -->
      <button class="submit-btn" :loading="submitting" @tap="handleRegister">注册</button>

      <!-- 底部链接 -->
      <view class="bottom-links">
        <text class="link" @tap="navigateBack">已有账号？去登录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { register } from '@/api/auth.js'

const formRef = ref(null)
const submitting = ref(false)
const smsCountdown = ref(0)

const form = reactive({
  phone: '',
  smsCode: '',
  password: '',
  confirmPassword: '',
  realName: ''
})

const rules = {
  phone: { rules: [{ required: true, errorMessage: '请输入手机号' }, { pattern: /^1[3-9]\d{9}$/, errorMessage: '手机号格式不正确' }] },
  smsCode: { rules: [{ required: true, errorMessage: '请输入验证码' }, { pattern: /^\d{6}$/, errorMessage: '验证码为6位数字' }] },
  password: { rules: [{ required: true, errorMessage: '请输入密码' }, { pattern: /^(?=.*[a-zA-Z])(?=.*\d).{6,20}$/, errorMessage: '6-20位，需含字母和数字' }] },
  confirmPassword: { rules: [{ required: true, errorMessage: '请确认密码' }, { validate: (rule, value, callback) => value !== form.password, errorMessage: '两次密码不一致' }] },
  realName: { rules: [{ required: true, errorMessage: '请输入真实姓名' }, { pattern: /^[\u4e00-\u9fa5·]+$/, errorMessage: '姓名仅支持中文' }] }
}

async function sendSmsCode() {
  if (smsCountdown.value > 0) return
  try {
    const { sendSmsCode } = await import('@/api/auth.js')
    await sendSmsCode(form.phone)
    smsCountdown.value = 60
    const timer = setInterval(() => {
      smsCountdown.value--
      if (smsCountdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (e) {
    uni.showToast({ title: '发送失败', icon: 'none' })
  }
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await register({ phone: form.phone, smsCode: form.smsCode, password: form.password, realName: form.realName })
    uni.showToast({ title: '注册成功', icon: 'success' })
    setTimeout(() => navigateBack(), 1500)
  } catch (e) {
    uni.showToast({ title: e.message || '注册失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

function navigateBack() {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  background-color: $color-bg-page;
  padding: 0 $spacing-lg;

  .notice-card {
    margin: $spacing-lg $spacing-lg 0;
    padding: $spacing-md $spacing-lg;
    background-color: $color-primary-light;
    border-radius: $border-radius-md;

    .notice-text {
      font-size: $font-size-sm;
      color: $color-text-secondary;
      line-height: 1.6;
    }
  }

  .form-section {
    padding: $spacing-lg;
    background-color: $color-bg-white;
    border-radius: $border-radius-lg;
    margin-top: $spacing-lg;

    .sms-row {
      display: flex;
      align-items: center;
      gap: $spacing-sm;

      .sms-btn {
        flex-shrink: 0;
        padding: 0 $spacing-md;
        font-size: $font-size-sm;
        color: $color-primary;
        background: none;
        border: 2rpx solid $color-primary;
        border-radius: $border-radius-lg;
      }
    }
  }

  .submit-btn {
    width: 100%;
    margin-top: $spacing-lg;
    height: 88rpx;
    line-height: 88rpx;
    background-color: $color-primary;
    color: #FFFFFF;
    font-size: $font-size-lg;
    border: none;
    border-radius: $border-radius-lg;

    &::after { border: none; }
  }

  .bottom-links {
    text-align: center;
    margin-top: $spacing-xl;

    .link {
      font-size: $font-size-sm;
      color: $color-primary;
    }
  }
}
</style>
```

- [ ] **Step 2: 验证**

Run: 输入信息，点击注册
Expected: 表单校验通过，调用注册API，成功后跳转登录页

- [ ] **Step 3: Commit**

```bash
git add pages/auth/register.vue
git commit -m "feat(parent): 实现注册页（手机号+验证码+密码+姓名）"
```

---

### Task 2.2: 忘记密码页

**Files:**
- Rewrite: `pages/auth/forgot-password.vue`

- [ ] **Step 1: 实现忘记密码页**

页面包含：返回导航栏、手机号+获取验证码、验证码、新密码+确认新密码、重置密码按钮。调用 `POST /api/v1/user/password/reset`。重置成功后跳转登录页。

- [ ] **Step 2: Commit**

```bash
git add pages/auth/forgot-password.vue
git commit -m "feat(parent): 实现忘记密码页（短信验证码重置密码）"
```

---

### Task 2.3: 修改密码页

**Files:**
- Rewrite: `pages/auth/change-password.vue`

- [ ] **Step 1: 实现修改密码页**

页面包含：返回导航栏、当前密码+新密码+确认新密码表单、确认修改按钮。调用 `PUT /api/v1/user/password`。修改成功后清除token并跳转登录页（需重新登录）。

- [ ] **Step 2: Commit**

```bash
git add pages/auth/change-password.vue
git commit -m "feat(parent): 实现修改密码页"
```

---

## Phase 3: 首页与儿童管理

### Task 3.1: 首页

**Files:**
- Rewrite: `pages/index/index.vue`

- [ ] **Step 1: 实现首页**

页面结构：用户问候语+今日日期、公告轮播（uni-swiper）、今日预约卡片（AppointmentCard或EmptyState）、快捷服务网格（2x2：预约接种、儿童管理、疫苗目录、系统公告）。原生TabBar页面，无需CustomTabBar。

- [ ] **Step 2: 验证**

Run: 登录后查看首页
Expected: 显示用户名、公告轮播、今日预约卡片、快捷入口

- [ ] **Step 3: Commit**

```bash
git add pages/index/index.vue
git commit -m "feat(parent): 实现首页（公告轮播/今日预约/快捷入口）"
```

---

### Task 3.2: 儿童列表

**Files:**
- Rewrite: `pages/child/list.vue`

- [ ] **Step 1: 实现儿童列表页**

页面结构：导航栏（标题+"+"按钮，显示儿童数量/上限）、儿童卡片列表（ChildCard，支持左滑删除）、EmptyState。使用 useChild hook 加载数据。最多5位儿童限制提示。

- [ ] **Step 2: Commit**

```bash
git add pages/child/list.vue
git commit -m "feat(parent): 实现儿童列表（ChildCard/左滑删除/最多5人限制）"
```

---

### Task 3.3: 添加/编辑儿童

**Files:**
- Rewrite: `pages/child/add.vue`

- [ ] **Step 1: 实现添加/编辑儿童页**

页面结构：导航栏（返回+"保存"按钮）、基本信息分组（姓名、性别Radio、出生日期picker）、证件信息分组（证件类型picker、证件号码）。编辑模式通过URL参数 `id` 判断，加载时预填数据。校验：姓名必填、性别必选、出生日期<=今天、证件号码格式校验。使用 useChild hook 提交。

- [ ] **Step 2: Commit**

```bash
git add pages/child/add.vue
git commit -m "feat(parent): 实现添加/编辑儿童页（表单校验/编辑模式预填）"
```

---

## Phase 4: 预约模块

### Task 4.1: 创建预约

**Files:**
- Rewrite: `pages/appointment/create.vue`

- [ ] **Step 1: 实现创建预约页**

```vue
<!-- pages/appointment/create.vue -->
<template>
  <view class="create-page">
    <uni-nav-bar :title="stepTitles[currentStep]" :border="false" />

    <!-- 步骤指示器 -->
    <StepIndicator :steps="stepLabels" :current="currentStep" @change="onStepChange" />

    <!-- 步骤1: 选择儿童 -->
    <view v-if="currentStep === 0" class="step-content">
      <view class="section-header">
        <text class="section-title">选择儿童</text>
        <text class="section-action" @tap="navigateToAddChild">+ 添加儿童</text>
      </view>
      <ChildCard
        v-for="child in children"
        :key="child.id"
        :child="child"
        :selectable="true"
        :selected="selectedChildId === child.id"
        @click="selectChild(child)"
      />
      <EmptyState v-if="children.length === 0" icon="person" title="暂无儿童信息" actionText="添加儿童" @action="navigateToAddChild" />
    </view>

    <!-- 步骤2: 选择疫苗 -->
    <view v-if="currentStep === 1" class="step-content">
      <view class="section-header">
        <text class="section-title">选择疫苗</text>
      </view>
      <view class="filter-tabs">
        <view
          v-for="tab in vaccineTabs"
          :key="tab.value"
          class="filter-tab"
          :class="{ active: activeVaccineTab === tab.value }"
          @tap="activeVaccineTab = tab.value"
        >
          {{ tab.label }}
        </view>
      </view>
      <VaccineCard
        v-for="vaccine in filteredVaccines"
        :key="vaccine.id"
        :vaccine="vaccine"
        :selectable="true"
        :selected="selectedVaccineId === vaccine.id"
        @click="selectVaccine(vaccine)"
      />
    </view>

    <!-- 步骤3: 选择时间 -->
    <view v-if="currentStep === 2" class="step-content">
      <view class="section-header">
        <text class="section-title">选择接种时间</text>
      </view>
      <!-- 已选疫苗摘要 -->
      <view v-if="selectedVaccine" class="vaccine-summary">
        <text class="vaccine-name">{{ selectedVaccine.vaccineName }}</text>
        <StatusTag :status="selectedVaccine.vaccineType === 'CLASS_I' ? 'free' : 'paid'" :label="selectedVaccine.vaccineType === 'CLASS_I' ? '一类·免费' : '二类·自费'" />
      </view>
      <!-- 日历选择器 -->
      <uni-datetime-picker type="date" v-model="selectedDate" :start="todayStr" :end="maxDateStr" @change="onDateChange" />
      <!-- 时段列表 -->
      <view class="time-slots">
        <view
          v-for="slot in timeSlots"
          :key="slot.id"
          class="time-slot"
          :class="{ selected: selectedTimeSlotId === slot.id, disabled: slot.remaining <= 0 }"
          @tap="selectTimeSlot(slot)"
        >
          <text class="slot-label">{{ slot.label }}</text>
          <text class="slot-remaining" :class="{ empty: slot.remaining <= 0 }">剩余 {{ slot.remaining }}</text>
        </view>
      </view>
    </view>

    <!-- 底部按钮 -->
    <view class="bottom-bar">
      <button class="btn-prev" v-if="currentStep > 0" @tap="prevStep">上一步</button>
      <button class="btn-next" v-if="currentStep < 2" :disabled="!canNext" @tap="nextStep">下一步</button>
      <button class="btn-submit" v-if="currentStep === 2" :loading="submitting" :disabled="!canSubmit" @tap="submitAppointment">确认预约</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useChildStore } from '@/store/child.js'
import { useAppointment } from '@/hooks/useAppointment.js'
import { getVaccines } from '@/api/vaccine.js'
import { getAppointments } from '@/api/appointment.js'
import StepIndicator from '@/components/StepIndicator/StepIndicator.vue'
import ChildCard from '@/components/ChildCard/ChildCard.vue'
import VaccineCard from '@/components/VaccineCard/VaccineCard.vue'
import StatusTag from '@/components/AppStatusTag/AppStatusTag.vue'
import EmptyState from '@/components/EmptyState/EmptyState.vue'

const childStore = useChildStore()
const { submitting, create } = useAppointment()

const currentStep = ref(0)
const selectedChildId = ref(null)
const selectedVaccineId = ref(null)
const selectedVaccine = ref(null)
const selectedDate = ref('')
const selectedTimeSlotId = ref(null)
const timeSlots = ref([])
const vaccines = ref([])
const activeVaccineTab = ref('')

const stepLabels = ['选择儿童', '选择疫苗', '选择时间']
const stepTitles = ['创建预约 (1/3)', '创建预约 (2/3)', '创建预约 (3/3)']
const vaccineTabs = [
  { label: '全部', value: '' },
  { label: '一类疫苗', value: 'CLASS_I' },
  { label: '二类疫苗', value: 'CLASS_II' }
]

const children = computed(() => childStore.children)

const filteredVaccines = computed(() => {
  return activeVaccineTab.value
    ? vaccines.value.filter(v => v.vaccineType === activeVaccineTab.value)
    : vaccines.value
})

const todayStr = new Date().toISOString().split('T')[0]
const maxDateStr = new Date(Date.now() + 7 * 24 * 3600 * 1000).toISOString().split('T')[0]

const canNext = computed(() => {
  if (currentStep.value === 0) return !!selectedChildId.value
  if (currentStep.value === 1) return !!selectedVaccineId.value
  return false
})

const canSubmit = computed(() => selectedChildId.value && selectedVaccineId.value && selectedDate.value && selectedTimeSlotId.value)

onShow(() => {
  childStore.fetchChildren()
  loadVaccines()
})

async function loadVaccines() {
  const data = await getVaccines({ vaccineType: activeVaccineTab.value })
  vaccines.value = data
}

function selectChild(child) {
  selectedChildId.value = child.id
}

function selectVaccine(vaccine) {
  selectedVaccineId.value = vaccine.id
  selectedVaccine.value = vaccine
  selectedTimeSlotId.value = null
  timeSlots.value = []
}

function onDateChange() {
  selectedTimeSlotId.value = null
  loadTimeSlots()
}

async function loadTimeSlots() {
  const data = await getAppointments({ date: selectedDate.value, vaccineId: selectedVaccineId.value })
  timeSlots.value = data.timeSlots || []
}

function selectTimeSlot(slot) {
  if (slot.remaining <= 0) return
  selectedTimeSlotId.value = slot.id
}

function prevStep() { currentStep.value-- }
function nextStep() { currentStep.value++ }

async function submitAppointment() {
  await create({
    childId: selectedChildId.value,
    vaccineId: selectedVaccineId.value,
    appointmentDate: selectedDate.value,
    timeSlot: timeSlots.value.find(s => s.id === selectedTimeSlotId.value)?.label
  })
  uni.showToast({ title: '预约成功', icon: 'success' })
  setTimeout(() => uni.switchTab({ url: '/pages/appointment/list' }), 1500)
}

function navigateToAddChild() {
  uni.navigateTo({ url: '/pages/child/add' })
}

function onStepChange(step) {
  currentStep.value = step
}
</script>

<style lang="scss" scoped>
.create-page {
  min-height: 100vh;
  background-color: $color-bg-page;
  padding-bottom: 140rpx;

  .step-content {
    padding: $spacing-lg;
    background-color: $color-bg-white;
    border-radius: $border-radius-lg;
    margin-top: $spacing-md;
  }

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-md;

    .section-title { font-size: $font-size-lg; font-weight: $font-weight-bold; color: $color-text-primary; }
    .section-action { font-size: $font-size-sm; color: $color-primary; }
  }

  .filter-tabs {
    display: flex;
    gap: $spacing-sm;
    margin-bottom: $spacing-md;

    .filter-tab {
      padding: $spacing-xs $spacing-md;
      font-size: $font-size-sm;
      color: $color-text-secondary;
      background-color: $color-bg-grey;
      border-radius: $border-radius-lg;

      &.active { color: #FFFFFF; background-color: $color-primary; }
    }
  }

  .vaccine-summary {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    padding: $spacing-md;
    background-color: $color-bg-grey;
    border-radius: $border-radius-md;
    margin-bottom: $spacing-md;

    .vaccine-name { font-size: $font-size-base; font-weight: $font-weight-bold; color: $color-text-primary; }
  }

  .time-slots {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;

    .time-slot {
      flex: 0 0 calc(50% - #{$spacing-sm});
      padding: $spacing-md;
      background-color: $color-bg-grey;
      border: 2rpx solid $color-border-light;
      border-radius: $border-radius-md;
      text-align: center;

      &.selected { border-color: $color-primary; background-color: $color-primary-light; }
      &.disabled { opacity: 0.5; }

      .slot-label { display: block; font-size: $font-size-base; color: $color-text-primary; }
      .slot-remaining { display: block; font-size: $font-size-xs; color: $color-text-secondary; margin-top: $spacing-xs; &.empty { color: $color-danger; } }
    }
  }

  .bottom-bar {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    display: flex;
    gap: $spacing-md;
    padding: $spacing-md $spacing-lg;
    background-color: #FFFFFF;
    box-shadow: 0 -2rpx 8rpx rgba(0, 0, 0, 0.04);
    padding-bottom: calc(#{$spacing-md} + env(safe-area-inset-bottom));

    .btn-prev, .btn-next, .btn-submit {
      flex: 1;
      height: 88rpx;
      line-height: 88rpx;
      border: none;
      border-radius: $border-radius-lg;
      font-size: $font-size-lg;
    }
    .btn-prev { background-color: $color-bg-grey; color: $color-text-regular; }
    .btn-next { background-color: $color-primary; color: #FFFFFF; }
    .btn-next:disabled { opacity: 0.5; }
    .btn-submit { background-color: $color-primary; color: #FFFFFF; }
    .btn-submit:disabled { opacity: 0.5; }
  }
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add pages/appointment/create.vue
git commit -m "feat(parent): 实现创建预约（3步骤：选儿童→选疫苗→选时间）"
```

---

### Task 4.2: 预约列表

**Files:**
- Rewrite: `pages/appointment/list.vue`

- [ ] **Step 1: 实现预约列表页**

页面结构：导航栏（标题）、Tab筛选栏（uni-segmented-control：全部/待签到/进行中/已完成/已取消）、AppointmentCard列表（下拉刷新+上拉加载更多）、EmptyState + 浮动"+"按钮→创建预约。Tab切换时重置筛选并刷新。原生TabBar页面。

- [ ] **Step 2: Commit**

```bash
git add pages/appointment/list.vue
git commit -m "feat(parent): 实现预约列表（Tab筛选/下拉刷新/上拉加载/浮动按钮）"
```

---

### Task 4.3: 预约详情

**Files:**
- Rewrite: `pages/appointment/detail.vue`

- [ ] **Step 1: 实现预约详情页**

页面结构：导航栏（返回）、状态卡片（状态色背景+大号状态文字+疫苗名称）、预约信息卡片（编号/日期/时段）、儿童信息卡片（姓名/性别/出生日期）、疫苗信息卡片（名称/类型/厂家）、底部操作按钮（status=1显示取消按钮，status=6/7/8/10显示查看流程指引按钮）。取消使用乐观更新策略。

- [ ] **Step 2: Commit**

```bash
git add pages/appointment/detail.vue
git commit -m "feat(parent): 实现预约详情（状态卡片/信息区/取消/查看流程）"
```

---

### Task 4.4: 流程指引

**Files:**
- Rewrite: `pages/appointment/guide.vue`

- [ ] **Step 1: 实现流程指引页**

```vue
<!-- pages/appointment/guide.vue -->
<template>
  <view class="guide-page">
    <uni-nav-bar title="接种流程" :border="false" />

    <!-- 预约状态卡片 -->
    <view class="status-card">
      <view class="status-header">
        <text class="status-title">{{ appointmentInfo.statusText || '加载中...' }}</text>
      </view>
      <view class="status-meta">
        <text>{{ appointmentInfo.childName }} · {{ appointmentInfo.vaccineName }}</text>
        <text>{{ appointmentInfo.appointmentDate }} {{ appointmentInfo.timeSlot }}</text>
      </view>
    </view>

    <!-- 步骤指示器 -->
    <StepIndicator :steps="steps" :current="currentStep" />

    <!-- 排队信息 -->
    <view v-if="queueData" class="queue-section">
      <QueueInfoCard
        :windowName="queueData.windowCode"
        :queuePosition="queueData.currentQueue"
        :estimatedWait="queueData.estimatedWaitMinutes"
        :isCalling="queueData.currentQueue === 1"
      />
    </view>

    <!-- 当前步骤描述 -->
    <view class="step-description">
      <text>{{ nextGuide || '请等待叫号...' }}</text>
    </view>

    <!-- 温馨提示 -->
    <view class="tips-card">
      <text class="tips-title">温馨提示</text>
      <text class="tips-content">接种前请确保儿童身体状况良好，如有发热、腹泻等症状请提前告知医生。</text>
    </view>

    <!-- 底部操作 -->
    <view v-if="canCancel" class="bottom-bar">
      <button class="btn-cancel" @tap="handleCancelAppointment">取消预约</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
// uni-app 全局生命周期（onShow/onHide/onUnload）无需从 Vue 导入
import { getAppointmentGuide, getAppointmentQueue, cancelAppointment } from '@/api/appointment.js'
import StepIndicator from '@/components/StepIndicator/StepIndicator.vue'
import QueueInfoCard from '@/components/QueueInfoCard/QueueInfoCard.vue'

const appointmentId = ref(null)
const currentStep = ref(0)
const guideData = ref(null)
const queueData = ref(null)
let pollingTimer = null

const steps = ['签到', '预检', '登记', '接种', '留观', '完成']

const canCancel = computed(() => {
  const status = guideData.value?.status
  return status === 1 || status === 6
})

const nextGuide = computed(() => {
  if (!guideData.value?.nextGuide) return ''
  return guideData.value.nextGuide.message || ''
})

const appointmentInfo = computed(() => guideData.value || {})

onShow(() => {
  appointmentId.value = uni.getStorageSync('guideAppointmentId') || ''
  if (appointmentId.value) {
    startPolling()
  }
})

onHide(() => { stopPolling() })
onUnload(() => { stopPolling() })

async function startPolling() {
  stopPolling()
  await loadData()
  pollingTimer = setInterval(loadData, 30000)
}

function stopPolling() {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
}

async function loadData() {
  try {
    const [guide, queue] = await Promise.all([
      getAppointmentGuide(appointmentId.value),
      getAppointmentQueue(appointmentId.value)
    ])
    guideData.value = guide
    queueData.value = queue
    if (guide.steps) currentStep.value = guide.steps.length - 1
  } catch (e) {
    console.error('加载流程指引失败', e)
  }
}

async function handleCancelAppointment() {
  uni.showModal({
    title: '确认取消',
    content: '确定要取消该预约吗？取消后需重新预约。',
    success: async () => {
      try {
        await cancelAppointment(appointmentId.value)
        uni.showToast({ title: '已取消', icon: 'success' })
        setTimeout(() => uni.switchTab({ url: '/pages/appointment/list' }), 1500)
      } catch (e) {
        uni.showToast({ title: e.message || '取消失败', icon: 'none' })
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.guide-page {
  min-height: 100vh;
  background-color: $color-bg-page;
  padding: $spacing-lg;

  .status-card {
    padding: $spacing-lg;
    background-color: #FFFFFF;
    border-radius: $border-radius-lg;
    box-shadow: $shadow-card;

    .status-header { margin-bottom: $spacing-sm; }
    .status-title { font-size: $font-size-xl; font-weight: $font-weight-bold; color: $color-text-primary; }
    .status-meta { font-size: $font-size-sm; color: $color-text-secondary; }
  }

  .queue-section { margin-top: $spacing-lg; }

  .step-description {
    padding: $spacing-md $spacing-lg;
    background-color: $color-primary-light;
    border-radius: $border-radius-md;
    margin-top: $spacing-md;

    text { font-size: $font-size-base; color: $color-primary; font-weight: $font-weight-medium; }
  }

  .tips-card {
    margin-top: $spacing-lg;
    padding: $spacing-md $spacing-lg;
    background-color: #FFF8E1;
    border-radius: $border-radius-md;

    .tips-title { display: block; font-size: $font-size-base; font-weight: $font-weight-bold; color: #FF9900; margin-bottom: $spacing-xs; }
    .tips-content { font-size: $font-size-sm; color: $color-text-regular; line-height: 1.6; }
  }

  .bottom-bar {
    margin-top: $spacing-xl;
    padding: 0;

    .btn-cancel {
      width: 100%;
      height: 88rpx;
      line-height: 88rpx;
      background-color: #FFFFFF;
      color: $color-danger;
      font-size: $font-size-lg;
      border: 2rpx solid $color-danger;
      border-radius: $border-radius-lg;
    }
  }
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add pages/appointment/guide.vue
git commit -m "feat(parent): 实现流程指引（6步指示器/排队信息/30秒轮询生命周期管理）"
```

---

## Phase 5: 疫苗目录 + 公告 + 接种记录

### Task 5.1: 疫苗目录

**Files:**
- Rewrite: `pages/vaccine/list.vue`
- Rewrite: `pages/vaccine/detail.vue`

- [ ] **Step 1: 实现疫苗列表页**

搜索框 + 一类/二类Tab + VaccineCard列表。公开页面无需token。点击跳转详情。

- [ ] **Step 2: 实现疫苗详情页**

疫苗名称 + 类型标签 + 基本信息（厂家/规格/剂次/间隔/适龄/价格）+ 疫苗说明 + 底部"立即预约"按钮（跳转创建预约页并携带vaccineId）。

- [ ] **Step 3: Commit**

```bash
git add pages/vaccine/
git commit -m "feat(parent): 实现疫苗目录（列表搜索/分类Tab/详情页/立即预约入口）"
```

---

### Task 5.2: 公告模块

**Files:**
- Rewrite: `pages/notice/list.vue`
- Rewrite: `pages/notice/detail.vue`
- Rewrite: `pages/notice/feedback.vue`

- [ ] **Step 1: 实现公告列表页**

NoticeCard列表，下拉刷新+上拉加载。公开页面。

- [ ] **Step 2: 实现公告详情页**

标题 + 日期 + 正文内容 + 底部"我有疑问"按钮（跳转反馈页）。

- [ ] **Step 3: 实现公告反馈页**

公告标题（只读）+ 反馈内容textarea（10-500字，字数统计）+ 提交反馈按钮。调用 submitFeedback API。

- [ ] **Step 4: Commit**

```bash
git add pages/notice/
git commit -m "feat(parent): 实现公告模块（列表/详情/反馈页）"
```

---

### Task 5.3: 接种记录 + 个人中心

**Files:**
- Rewrite: `pages/record/list.vue`
- Rewrite: `pages/mine/index.vue`

- [ ] **Step 1: 实现接种记录页**

顶部儿童筛选下拉（全部/具体儿童）+ RecordCard列表（按月分组显示）+ 上拉加载更多 + EmptyState。

- [ ] **Step 2: 实现个人中心**

UserInfoCard + 菜单列表（儿童管理/接种记录/修改密码/关于系统）+ 退出登录按钮（二次确认弹窗）。原生TabBar页面。

- [ ] **Step 3: Commit**

```bash
git add pages/record/list.vue pages/mine/index.vue
git commit -m "feat(parent): 实现接种记录（按月分组/儿童筛选）和个人中心"
```

---

## 验证清单

Phase 1 完成后验证：
- [ ] 6个API模块创建完成，路径与API设计文档一致
- [ ] useChildStore和useAppointmentStore可正常使用
- [ ] 9个业务组件渲染正常

Phase 2 完成后验证：
- [ ] 注册页表单校验正确（手机号/密码/姓名）
- [ ] 忘记密码页短信验证码倒计时正常
- [ ] 修改密码成功后跳转登录页

Phase 3 完成后验证：
- [ ] 首页公告轮播正常
- [ ] 儿童列表最多5人限制生效
- [ ] 左滑删除儿童正常

Phase 4 完成后验证：
- [ ] 创建预约3步流程正常跳转
- [ ] 日历仅显示未来7天
- [ ] 预约列表Tab筛选正常
- [ ] 流程指引30秒自动刷新，离开页面停止
- [ ] 取消预约乐观更新正常

Phase 5 完成后验证：
- [ ] 疫苗目录分类筛选正常
- [ ] 公告反馈字数统计正确
- [ ] 接种记录按月分组显示
- [ ] 个人中心退出登录二次确认
