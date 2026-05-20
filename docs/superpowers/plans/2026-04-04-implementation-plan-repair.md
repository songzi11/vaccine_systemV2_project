# 前端实施计划修复计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 修复 REVIEW-REPORT-003 评审报告中的全部 P1 问题（7个）和关键 P2 问题（医生端独立性问题、核心页面缺少代码示例）

**Architecture:** 直接修改4份实施计划文档（.md文件），补充缺失的代码示例、修复代码错误、调整任务顺序、重构医生端计划为依赖共享基础设施的方案。

**Tech Stack:** Markdown + Vue 3 SFC（`<script setup>`）+ Pinia + uni-ui + SCSS

**评审基准：** `docs/superpowers/specs/前端实施计划评审报告.md`

---

## 修复范围

| Phase | 目标文档 | 修复内容 | 依赖 |
|-------|---------|---------|------|
| Phase 1 | 前端共享基础设施实施计划 | P1-1 uni.scss语法错误、P1-2 store/user.js导入缺失、P1-3 Task顺序错误 | 无 |
| Phase 2 | 医生端APP前端实施计划 | P1-6 onLoad导入、P2-10 VerifyCheckbox、P2-14 BASE_URL、P2-11 about页面、**结构性重构** | 无 |
| Phase 3 | 家长端APP前端实施计划 | P1-4 核心3页面补充Vue代码 | 无 |
| Phase 4 | 医生端APP前端实施计划 | P1-5 核心3页面补充Vue代码 | Phase 2 |
| Phase 5 | 管理员端APP前端实施计划 | P1-7 核心2页面补充Vue代码 + P2-17 ChartPanel修复 | 无 |

---

## Phase 1: 共享基础设施 P1修复

### Task 1: 修复 uni.scss 语法错误（P1-1）

**Files:**
- Modify: `docs/superpowers/plans/前端共享基础设施实施计划.md`（Task 2, Step 1, uni.scss 代码块第292行）

- [ ] **Step 1: 修复 `$status-expired` 缺少 `$` 前缀**

在 Task 2 的 uni.scss 代码块中，将第 292 行：

```scss
-status-expired: #999999;
```

替换为：

```scss
$status-expired: #999999;
```

- [ ] **Step 2: 验证修复**

确认 uni.scss 代码块中所有变量声明均以 `$` 开头，无遗漏。

---

### Task 2: 修复 store/user.js 导入缺失（P1-2）

**Files:**
- Modify: `docs/superpowers/plans/前端共享基础设施实施计划.md`（Task 9, Step 1, store/user.js 代码块）

- [ ] **Step 1: 在 import 语句中添加 `put`**

在 Task 9 的 store/user.js 代码块中，将第 3 行：

```javascript
import { post, get } from '@/utils/request.js'
```

替换为：

```javascript
import { post, get, put } from '@/utils/request.js'
```

同时删除代码块末尾的注释 `// 注意：需要在文件顶部额外导入 put`（如果存在）。

- [ ] **Step 2: 验证修复**

确认 `updateProfile()` 方法中使用的 `put()` 已在文件顶部 import。

---

### Task 3: 修复 Task 5/6 顺序错误（P1-3）

**Files:**
- Modify: `docs/superpowers/plans/前端共享基础设施实施计划.md`（交换 Task 5 和 Task 6 的位置）

- [ ] **Step 1: 交换 Task 5 和 Task 6 的顺序**

将 `## Task 5: HTTP 客户端（utils/request.js）` 整个章节（包括 Files、所有 Steps、代码块）与 `## Task 6: 认证工具（utils/auth.js）` 整个章节互换位置。

互换后：
- 新 Task 5: 认证工具（utils/auth.js）— 先创建 auth.js
- 新 Task 6: HTTP 客户端（utils/request.js）— 后创建 request.js（此时 auth.js 已存在）

- [ ] **Step 2: 更新 Task 4 的依赖说明**

在 Task 4（Pinia 初始化）的代码块中，如果注释提及 request.js 依赖，更新为正确的依赖链：Pinia → store → request.js/auth.js（无变化，仅确认）。

- [ ] **Step 3: 验证修复**

确认任务顺序中 auth.js（提供 getToken/removeToken）在 request.js（导入 auth.js）之前。

---

## Phase 2: 医生端结构性修复

### Task 4: 重构医生端计划为依赖共享基础设施

**Files:**
- Modify: `docs/superpowers/plans/医生端APP前端实施计划.md`（计划头部 + Phase 1）

- [ ] **Step 1: 更新计划头部，声明依赖共享基础设施**

将计划头部的 Architecture 和前置条件替换为：

```markdown
**Architecture:** 基于 `前端共享基础设施实施计划` 的共享层，仅包含医生端业务页面。API层复用 request.js，认证复用 auth.js，用户状态复用 store/user.js，路由和 TabBar 复用 pages.json 和 CustomTabBar。Pinia 管理业务状态（useQueueStore/useCallingStore），API层按医生模块拆分，Hooks 封装队列轮询和倒计时逻辑。

**设计文档：** `docs/superpowers/specs/医生端APP前端设计.md`

**前置条件：** 前端共享基础设施实施计划已完成（request.js/auth.js/constants.js/store/user.js/hooks/usePagination.js/hooks/useAuth.js/components/EmptyState/StatusTag/CustomTabBar/App.vue/pages.json/api/auth.js/pages/auth/login.vue/pages/common/404.vue 均已实现）
```

- [ ] **Step 2: 重写 Phase 1，移除重复的基础设施任务**

将 Phase 1 从17个基础设施任务重构为仅包含医生端特有的 API/Store/Hooks/Components：

新 Phase 1 内容替换为：

```markdown
## Phase 1: API层 + Store + Hooks + 业务组件

### Task 1.1: 医生端 API 模块

**Files:**
- Create: `api/signin.js`
- Create: `api/precheck.js`
- Create: `api/register.js`
- Create: `api/vaccinate.js`
- Create: `api/observe.js`
- Create: `api/stock.js`

（保留现有代码示例，将 `import { get, post } from '@/utils/request'` 改为 `import { get, post, put } from '@/utils/request.js'`，将 API 路径加上 `/api/v1` 前缀使其与共享基础设施的 BASE_URL 方式一致）

### Task 1.2: 业务 Store

**Files:**
- Create: `store/queue.js`
- Create: `store/calling.js`

（保留现有代码示例，删除 useUserStore 定义——已在共享基础设施中创建。在 store/queue.js 和 store/calling.js 中导入 useUserStore：`import { useUserStore } from '@/store/user.js'`）

### Task 1.3: 业务 Hooks

**Files:**
- Create: `hooks/useQueue.js`
- Create: `hooks/useCountdown.js`
- Create: `hooks/useCallingTimer.js`

（保留现有代码示例，删除 usePagination.js——已在共享基础设施中创建。在 useQueue.js 中导入：`import { useUserStore } from '@/store/user.js'`）

### Task 1.4: 业务组件

**Files:**
- Create: `components/QueueCard.vue`
- Create: `components/StatusTag.vue`
- Create: `components/InfoCard.vue`
- Create: `components/VerifyCheckbox.vue`
- Create: `components/CountdownTimer.vue`

（保留 QueueCard/StatusTag/InfoCard/CountdownTimer 的现有代码示例。新增 VerifyCheckbox 组件代码（见 Task 7）。删除 EmptyState.vue 和 CustomTabBar.vue——已在共享基础设施中创建）
```

- [ ] **Step 3: 删除原 Phase 1 中的重复任务**

删除以下原 Phase 1 任务（已在共享基础设施中实现）：
- Task 1.1 中的项目清理、main.js、App.vue、pages.json、uni.scss 重写
- Task 1.2 CustomTabBar.vue（共享层已提供）
- Task 1.3 登录页（共享层已提供）
- Task 1.4 首页（改为 Phase 2 业务页面）
- Task 1.5 个人中心（改为 Phase 2 业务页面）
- Task 1.6 EmptyState.vue（共享层已提供）
- Task 1.7 中 usePagination.js（共享层已提供）

保留并移至新 Phase 1：
- store/queue.js、store/calling.js
- hooks/useQueue.js、hooks/useCountdown.js、hooks/useCallingTimer.js
- components/QueueCard.vue、StatusTag.vue、InfoCard.vue、CountdownTimer.vue
- 全部 API 模块代码示例

- [ ] **Step 4: 更新范围说明表**

将"范围说明"部分的 Phase 表更新为：

| Phase | 内容 | 依赖 |
|-------|------|------|
| Phase 1 | API层 + Store + Hooks + 业务组件（6个API模块、2个Store、3个Hook、5个组件） | 共享基础设施 |
| Phase 2 | 流程模块（签到、预检、接种队列+操作页、接种记录） | Phase 1 |
| Phase 3 | 登记+叫号模块（登记队列、登记处理、批次切换、叫号面板、过号记录） | Phase 1 |
| Phase 4 | 留观模块（留观队列、留观详情、留观结束、不良反应上报/处理） | Phase 1 |
| Phase 5 | 库存模块（库存总览、批次管理、调拨、预警、销毁） | Phase 1 |

- [ ] **Step 5: 验证重构**

确认：
1. 计划头部声明了共享基础设施依赖
2. Phase 1 仅包含医生端特有的文件
3. 无重复定义（request.js、auth.js、constants.js、user store、EmptyState、CustomTabBar、login.vue、404.vue）
4. 所有 import 路径使用 `@/` 别名
5. API 路径包含 `/api/v1` 前缀

---

### Task 5: 修复 API 路径 BASE_URL 一致性（P2-14）

**Files:**
- Modify: `docs/superpowers/plans/医生端APP前端实施计划.md`（Task 1.1 中的6个 API 模块代码块）

- [ ] **Step 1: 为所有 API 模块添加 `/api/v1` 前缀**

在 Task 1.1 的每个 API 模块代码块中，将相对路径加上 `/api/v1` 前缀：

**api/signin.js** — 将路径修改为：
```js
export function getTodayList(params) {
  return get('/api/v1/signin/today', params)
}
export function executeSignin(data) {
  return post('/api/v1/signin/execute', data, { showLoading: true })
}
```

**api/precheck.js** — 将路径修改为：
```js
export function getQueue(params) {
  return get('/api/v1/precheck/queue', params)
}
export function executePrecheck(data) {
  return post('/api/v1/precheck/execute', data, { showLoading: true })
}
```

**api/register.js** — 将路径修改为：
```js
export function getQueue(params) {
  return get('/api/v1/register/queue', params)
}
export function getBatches(vaccineId) {
  return get(`/api/v1/register/batches/${vaccineId}`)
}
export function executeRegister(data) {
  return post('/api/v1/register/execute', data, { showLoading: true })
}
export function switchBatch(appointmentId, data) {
  return post(`/api/v1/register/${appointmentId}/switch-batch`, data, { showLoading: true })
}
```

**api/vaccinate.js** — 将路径修改为：
```js
export function getQueue(params) {
  return get('/api/v1/vaccinate/queue', params)
}
export function verifyInfo(appointmentId) {
  return get(`/api/v1/vaccinate/${appointmentId}/verify`)
}
export function executeVaccinate(data) {
  return post('/api/v1/vaccinate/execute', data, { showLoading: true })
}
export function getRecords(params) {
  return get('/api/v1/vaccinate/records', params)
}
export function getChildRecords(childId) {
  return get(`/api/v1/vaccinate/records/child/${childId}`)
}
```

**api/observe.js** — 将路径修改为：
```js
export function getQueue(params) {
  return get('/api/v1/observe/queue', params)
}
export function getStatus(injectionId) {
  return get(`/api/v1/observe/${injectionId}`)
}
export function finishObserve(appointmentId) {
  return post(`/api/v1/observe/${appointmentId}/finish`, {}, { showLoading: true })
}
export function reportAdverse(data) {
  return post('/api/v1/observe/adverse/report', data, { showLoading: true })
}
export function handleAdverse(reactionId, data) {
  return post(`/api/v1/observe/adverse/${reactionId}/handle`, data, { showLoading: true })
}
```

**api/stock.js** — 将路径修改为：
```js
export function getSummary() {
  return get('/api/v1/stock/summary')
}
export function getBatches(params) {
  return get('/api/v1/stock/batches', params)
}
export function getBatchDetail(batchId) {
  return get(`/api/v1/stock/batches/${batchId}`)
}
export function createTransfer(data) {
  return post('/api/v1/stock/transfer', data, { showLoading: true })
}
export function getTransferRecords(params) {
  return get('/api/v1/stock/transfer/records', params)
}
export function disposeBatch(batchId, data) {
  return post(`/api/v1/stock/batches/${batchId}/dispose`, data, { showLoading: true })
}
export function getAlerts(params) {
  return get('/api/v1/stock/alerts', params)
}
export function handleAlert(alertId, data) {
  return put(`/api/v1/stock/alerts/${alertId}/handle`, data, { showLoading: true })
}
```

- [ ] **Step 2: 删除 request.js 中 `BASE_URL = 'http://localhost:8080/api/v1'` 的定义**

如果重构后的计划不再包含 Task 5（request.js 创建），则此步骤自动完成。如果保留了 request.js 代码块作为参考，则将其 BASE_URL 改为 `'http://localhost:8080'`（与共享基础设施一致）。

---

### Task 6: 修复 onLoad 导入问题（P1-6）

**Files:**
- Modify: `docs/superpowers/plans/医生端APP前端实施计划.md`（Task 2.1 signin-confirm.vue 和 Task 2.2 precheck-assess.vue 代码块）

- [ ] **Step 1: 在 signin-confirm.vue 代码块的 `<script setup>` 中添加 onLoad 导入**

找到 Task 2.1 Step 2 中 `pages/process/signin-confirm.vue` 的代码块，在 `<script setup>` 开头添加：

```javascript
import { ref, onMounted } from 'vue'
```

将代码中使用的 `onLoad(query)` 替换为 `onMounted(() => { ... })`，通过 `const query = ...` 方式获取页面参数（或保留 `onLoad` 并添加注释说明 uni-app 全局生命周期无需导入）。

推荐方式——添加注释说明：

```javascript
// uni-app 全局生命周期（onLoad/onShow/onHide/onUnload）无需从 Vue 导入
```

- [ ] **Step 2: 在 precheck-assess.vue 代码块中做同样处理**

在 Task 2.2 Step 3 中 `pages/process/precheck-assess.vue` 的代码块中添加同样的注释说明。

---

### Task 7: 添加 VerifyCheckbox 组件（P2-10）

**Files:**
- Modify: `docs/superpowers/plans/医生端APP前端实施计划.md`（Task 1.4 业务组件）

- [ ] **Step 1: 在 Task 1.4 中新增 VerifyCheckbox.vue 代码示例**

在 Task 1.4 的"新建业务组件"列表中添加 `components/VerifyCheckbox/VerifyCheckbox.vue`，并在步骤中添加完整代码：

```vue
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
  modelValue: {
    type: Boolean,
    default: false
  },
  label: {
    type: String,
    default: ''
  }
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

- [ ] **Step 2: 在 Task 3.1 Step 3 登记处理页描述中引用 VerifyCheckbox**

在 Task 3.1 Step 3 的文字描述中，将"4张InfoCard（带VerifyCheckbox勾选）"改为：

> 4张 InfoCard，每张卡片底部使用 `<VerifyCheckbox :label="卡片标题" v-model="verifiedMap.xxx" />` 实现勾选确认。所有卡片 verified 值为 true 时才启用提交按钮。

---

### Task 8: 修复 about 页面引用（P2-11）

**Files:**
- Modify: `docs/superpowers/plans/医生端APP前端实施计划.md`（Task 1.5 个人中心）

- [ ] **Step 1: 删除 about 页面引用**

在 Task 1.5 个人中心的菜单列表描述中，删除"关于系统"菜单项（或改为跳转页面详情），确保不再引用不存在的 `pages/about/about.vue`。

- [ ] **Step 2: 如果保留菜单项，在 pages.json 中注册路由并创建占位文件**

选择方案A（删除引用）或方案B（补充文件）。推荐方案A——该页面不在设计文档中。

---

## Phase 3: 家长端核心页面代码补充

### Task 9: 补充注册页完整代码（家长端 P1-4）

**Files:**
- Modify: `docs/superpowers/plans/家长端APP前端实施计划.md`（Task 2.1）

- [ ] **Step 1: 用完整 Vue SFC 代码替换 Task 2.1 Step 1 的文字描述**

将 Task 2.1 Step 1 的文字描述替换为：

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

---

### Task 10: 补充创建预约页完整代码（家长端 P1-4）

**Files:**
- Modify: `docs/superpowers/plans/家长端APP前端实施计划.md`（Task 4.1）

- [ ] **Step 1: 用完整 Vue SFC 代码替换 Task 4.1 Step 1 的文字描述**

将 Task 4.1 Step 1 的文字描述替换为：

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

---

### Task 11: 补充流程指引页完整代码（家长端 P1-4）

**Files:**
- Modify: `docs/superpowers/plans/家长端APP前端实施计划.md`（Task 4.4）

- [ ] **Step 1: 用完整 Vue SFC 代码替换 Task 4.4 Step 1 的文字描述**

将 Task 4.4 Step 1 的文字描述替换为：

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
      <button class="btn-cancel" @tap="cancelAppointment">取消预约</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onShow, onHide, onUnload } from 'vue'
import { getAppointmentGuide, getAppointmentQueue } from '@/api/appointment.js'
import { cancelAppointment } from '@/api/appointment.js'
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

async function cancelAppointment() {
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

---

## Phase 4: 医生端核心页面代码补充

### Task 12: 补充登记处理页完整代码（医生端 P1-5）

**Files:**
- Modify: `docs/superpowers/plans/医生端APP前端实施计划.md`（Task 3.1 Step 3）

- [ ] **Step 1: 用完整 Vue SFC 代码替换 Task 3.1 Step 3 的文字描述**

将 Task 3.1 Step 3 "实现登记处理页"的文字描述替换为：

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

onLoad(async (query) => {
  await Promise.all([loadChildInfo(query), loadPrecheckInfo(query), loadAppointmentInfo(query)])
  await autoSelectBatch(query.vaccineId)
})

async function loadChildInfo(query) {
  // 通过 appointmentId 获取关联的儿童信息
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

---

### Task 13: 补充叫号面板完整代码（医生端 P1-5）

**Files:**
- Modify: `docs/superpowers/plans/医生端APP前端实施计划.md`（Task 3.2 Step 1）

- [ ] **Step 1: 用完整 Vue SFC 代码替换 Task 3.2 Step 1 的文字描述**

将 Task 3.2 Step 1 "实现叫号面板"的文字描述替换为：

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
import { ref, onShow, onHide } from 'vue'
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
    const { updateProfile } = await import('@/api/user.js')
    await updateProfile({ status: 'arrived' })
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

---

### Task 14: 补充留观详情页完整代码（医生端 P1-5）

**Files:**
- Modify: `docs/superpowers/plans/医生端APP前端实施计划.md`（Task 4.1 Step 3）

- [ ] **Step 1: 用完整 Vue SFC 代码替换 Task 4.1 Step 3 的文字描述**

将 Task 4.1 Step 3 "实现留观详情页"的文字描述替换为：

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
import { ref, computed, onShow, onHide, onUnload } from 'vue'
import { getStatus, finishObserve as finishObserveApi } from '@/api/observe.js'
import CountdownTimer from '@/components/CountdownTimer/CountdownTimer.vue'

const injectionId = ref(null)
const observeInfo = ref({})
const elapsedTime = ref('00:00')
const submitting = ref(false)
let countdownInterval = null

const canFinish = computed(() => elapsedTime.value >= '30:00')

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

---

## Phase 5: 管理员端页面代码补充

### Task 15: 补充窗口列表页完整代码（管理员端 P1-7）

**Files:**
- Modify: `docs/superpowers/plans/管理员端APP前端实施计划.md`（Task 3.1）

- [ ] **Step 1: 用完整 Vue SFC 代码替换 Task 3.1 Step 1 的文字描述**

将 Task 3.1 Step 1 "实现窗口列表页"的文字描述替换为：

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
import { ref, computed, onShow } from 'vue'
import { getWindowList, deleteWindow } from '@/api/window.js'
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
        await deleteWindow(item.id)
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

---

### Task 16: 补充统计页面代码并修复 ChartPanel（管理员端 P1-7 + P2-17）

**Files:**
- Modify: `docs/superpowers/plans/管理员端APP前端实施计划.md`（Task 7.1 + Task 1.4 ChartPanel）

- [ ] **Step 1: 修复 ChartPanel.vue renderChart() 空方法**

在 Task 1.4 的 ChartPanel.vue 代码块中，将 `renderChart()` 方法体替换为完整实现：

```vue
<!-- components/ChartPanel/ChartPanel.vue — renderChart() 部分替换 -->
    renderChart(canvasId, chartType, chartData) {
      // #ifdef H5
      const canvas = document.getElementById(canvasId)
      if (!canvas) return
      const ctx = canvas.getContext('2d')
      // #endif
      // #ifdef MP-WEIXIN
      const ctx = uni.createCanvasContext(canvasId, { type: canvasId, component: this })
      // #endif

      const { categories = [], series = [] } = chartData
      const opts = { type: chartType, canvas: false, animation: true, legend: { data: [] } }

      if (chartType === 'pie') {
        opts.type = 'pie'
        series.push({ type: 'pie', radius: '60%', data: chartData.map(d => ({ ...d, label: d.label })) })
      } else {
        categories.push(...(chartData.map(d => d.label)))
        series.push({ type: chartType, data: chartData.map(d => d.value), label: d.label, smooth: true }))
      }

      // #ifdef H5
      const uChartsInstance = new uCharts(opts)
      uChartsInstance.mount(canvas)
      this._chartInstance = uChartsInstance
      // #endif
    }
```

- [ ] **Step 2: 用完整 Vue SFC 代码替换 Task 7.1 Step 1 的文字描述**

将 Task 7.1 Step 1 "实现接种统计页"的文字描述替换为：

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
import { ref, onShow } from 'vue'
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

---

## Phase 6: 验证修复

### Task 17: 验证所有修复

- [ ] **Step 1: 检查共享基础设施计划**

1. 搜索 uni.scss 代码块，确认无 `$status-expired`（应为 `$status-expired`）
2. 搜索 store/user.js 代码块，确认 import 包含 `put`
3. 确认 Task 5 是 auth.js，Task 6 是 request.js

- [ ] **Step 2: 检查医生端计划**

1. 确认计划头部声明了共享基础设施依赖
2. 确认无重复的基础设施任务（request.js、auth.js、constants.js、user store、EmptyState、CustomTabBar、login.vue）
3. 确认所有 API 路径包含 `/api/v1` 前缀
4. 确认 signin-confirm.vue 和 precheck-assess.vue 有 onLoad 说明
5. 确认 VerifyCheckbox 组件已创建
6. 确认 about 页面引用已删除
7. 确认 register-process.vue、calling/index.vue、observe-detail.vue 有完整 Vue SFC 代码

- [ ] **Step 3: 检查家长端计划**

1. 确认 Task 2.1 注册页有完整 Vue SFC 代码
2. 确认 Task 4.1 创建预约页有完整 Vue SFC 代码
3. 确认 Task 4.4 流程指引页有完整 Vue SFC 代码（含30秒轮询生命周期）

- [ ] **Step 4: 检查管理员端计划**

1. 确认 Task 3.1 窗口列表页有完整 Vue SFC 代码
2. 确认 Task 7.1 接种统计页有完整 Vue SFC 代码（含 u-charts 集成）
3. 确认 ChartPanel.vue renderChart() 方法不再是空方法
