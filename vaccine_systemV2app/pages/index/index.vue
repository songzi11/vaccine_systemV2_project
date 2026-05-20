<template>
  <view class="home-page">
    <!-- 顶部用户信息 -->
    <view class="user-header">
      <view class="user-info">
        <text class="user-name">{{ userStore.userInfo?.realName || userStore.userInfo?.phone }}</text>
        <text class="user-role">{{ roleText }}</text>
      </view>
      <text class="today-date">{{ todayDate }}</text>
    </view>

    <!-- 管理员工作概览 -->
    <view v-if="userStore.isAdminRole" class="section">
      <text class="section-title">工作概览</text>
      <view class="stats-grid">
        <view v-for="stat in adminStats" :key="stat.label" class="stat-item" @click="stat.url && navigateTo(stat.url)">
          <text class="stat-value" :class="stat.danger ? 'danger' : ''">{{ stat.value }}</text>
          <text class="stat-label">{{ stat.label }}</text>
        </view>
      </view>
    </view>

    <!-- 管理员工作台 -->
    <view v-if="userStore.isAdminRole" class="section">
      <text class="section-title">工作台</text>
      <view class="shortcut-grid">
        <view v-for="item in adminShortcuts" :key="item.label" class="shortcut-item" @click="navigateTo(item.url)">
          <text class="shortcut-icon">{{ item.icon }}</text>
          <text class="shortcut-label">{{ item.label }}</text>
        </view>
      </view>
    </view>

    <!-- 医生工作概览 -->
    <view v-if="userStore.isDoctor && !userStore.isAdminRole" class="section">
      <text class="section-title">工作概览</text>
      <view class="stats-grid">
        <view v-for="stat in doctorStats" :key="stat.label" class="stat-item" @click="stat.url && navigateTo(stat.url)">
          <text class="stat-value">{{ stat.value }}</text>
          <text class="stat-label">{{ stat.label }}</text>
        </view>
      </view>
    </view>

    <!-- 医生快捷入口 -->
    <view v-if="userStore.isDoctor && !userStore.isAdminRole" class="section">
      <text class="section-title">快捷入口</text>
      <view class="shortcut-grid">
        <view v-for="item in doctorShortcuts" :key="item.label" class="shortcut-item" @click="navigateTo(item.url)">
          <text class="shortcut-icon">{{ item.icon }}</text>
          <text class="shortcut-label">{{ item.label }}</text>
        </view>
      </view>
    </view>

    <!-- 当前窗口 -->
    <view v-if="userStore.userInfo?.windowName && !userStore.isAdminRole" class="section">
      <view class="window-info">
        <text class="window-label">当前窗口</text>
        <text class="window-name">{{ userStore.userInfo?.windowName }}</text>
      </view>
    </view>

    <!-- 家长快捷入口 -->
    <view v-if="userStore.isUser" class="section">
      <text class="section-title">快捷服务</text>
      <view class="shortcut-grid">
        <view class="shortcut-item" @click="navigateTo('/pages/child/list')">
          <text class="shortcut-icon">👶</text>
          <text class="shortcut-label">儿童管理</text>
        </view>
        <view class="shortcut-item" @click="navigateTo('/pages/vaccine/list')">
          <text class="shortcut-icon">💉</text>
          <text class="shortcut-label">疫苗查询</text>
        </view>
        <view class="shortcut-item" @click="navigateTo('/pages/record/list')">
          <text class="shortcut-icon">📋</text>
          <text class="shortcut-label">接种记录</text>
        </view>
        <view class="shortcut-item" @click="navigateTo('/pages/notice/list')">
          <text class="shortcut-icon">📢</text>
          <text class="shortcut-label">公告通知</text>
        </view>
        <view class="shortcut-item" @click="navigateTo('/pages/appointment/list')">
          <text class="shortcut-icon">📅</text>
          <text class="shortcut-label">我的预约</text>
        </view>
      </view>
    </view>

    <CustomTabBar />
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user.js'
import { getVaccinationStats, getStockStats, getAnomalyStats } from '@/api/stats.js'
import { getNoticeList } from '@/api/notice-manage.js'
import { getMyTodayStats } from '@/api/appointment.js'
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue'

const userStore = useUserStore()

const todayVaccinations = ref(0)
const stockAlerts = ref(0)
const pendingNotices = ref(0)
const totalAnomalies = ref(0)

// 医生今日统计
const todayStats = ref({
  appointed: 0,
  signedIn: 0,
  precheckPass: 0,
  observing: 0,
  completed: 0,
  cancelled: 0
})

const todayDate = computed(() => {
  const d = new Date()
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
})

const roleText = computed(() => {
  if (userStore.isSuperAdmin) return '系统管理员'
  if (userStore.isBusinessAdmin) return '业务主管'
  const map = {
    DOCTOR_SIGNIN: '签到医生',
    DOCTOR_PRECHECK: '预检医生',
    DOCTOR_REGISTER: '登记医生',
    DOCTOR_VACCINATE: '接种医生',
    DOCTOR_OBSERVE: '留观医生',
    DOCTOR_STOCK: '库存管理'
  }
  const roles = userStore.userInfo?.roles || []
  const primary = roles.find(r => r.startsWith('DOCTOR_'))
  if (primary) return map[primary] || '医生'
  if (userStore.isUser) return '家长'
  return '用户'
})

// 管理员概览
const adminStats = computed(() => {
  if (userStore.isSuperAdmin) return [
    { label: '待审批', value: pendingNotices.value, url: '/pages/admin/notice/list' },
    { label: '异常事件', value: totalAnomalies.value, danger: true, url: '/pages/stats/anomaly' }
  ]
  if (userStore.isBusinessAdmin) return [
    { label: '今日接种', value: todayVaccinations.value, url: '/pages/stats/vaccination' },
    { label: '库存预警', value: stockAlerts.value, danger: true, url: '/pages/stock/alerts' },
    { label: '待审批', value: pendingNotices.value, url: '/pages/admin/notice/list' }
  ]
  return []
})

const adminShortcuts = computed(() => {
  if (userStore.isSuperAdmin) return [
    { icon: '🔑', label: '角色权限', url: '/pages/admin/role/list' },
    { icon: '🎫', label: '注册验证码', url: '/pages/admin/verify-code/list' },
    { icon: '⚙️', label: '系统配置', url: '/pages/admin/config/index' }
  ]
  if (userStore.isBusinessAdmin) return [
    { icon: '📅', label: '排班管理', url: '/pages/admin/schedule/list' },
    { icon: '🪟', label: '窗口管理', url: '/pages/admin/window/list' },
    { icon: '💉', label: '疫苗管理', url: '/pages/admin/vaccine/list' },
    { icon: '📢', label: '公告管理', url: '/pages/admin/notice/list' }
  ]
  return []
})

// 医生概览
const doctorStats = computed(() => {
  const s = todayStats.value
  if (userStore.isStockDoctor) return [
    { label: '库存预警', value: 0, url: '/pages/stock/alerts' },
    { label: '即将过期', value: 0, url: '/pages/stock/batches' },
    { label: '今日调拨', value: 0, url: '/pages/stock/transfer-records' }
  ]
  if (userStore.isSigninDoctor) return [
    { label: '待签到', value: s.appointed, url: '/pages/queue/signin' },
    { label: '已签到', value: s.signedIn, url: '/pages/queue/signin' },
    { label: '已完成', value: s.completed, url: '/pages/record/list' }
  ]
  if (userStore.isPrecheckDoctor) return [
    { label: '待处理', value: s.appointed + s.signedIn, url: '/pages/queue/precheck' },
    { label: '留观中', value: s.observing, url: '/pages/queue/observe' }
  ]
  if (userStore.isRegisterDoctor) return [
    { label: '待登记', value: s.precheckPass, url: '/pages/queue/vaccinate' },
    { label: '留观中', value: s.observing, url: '/pages/queue/observe' },
    { label: '已完成', value: s.completed, url: '/pages/record/list' }
  ]
  return [
    { label: '待处理', value: s.appointed + s.signedIn, url: '/pages/queue/precheck' },
    { label: '待接种', value: s.precheckPass, url: '/pages/queue/vaccinate' },
    { label: '留观中', value: s.observing, url: '/pages/queue/observe' }
  ]
})

const doctorShortcuts = computed(() => {
  if (userStore.isSigninDoctor) return [
    { icon: '📝', label: '签到队列', url: '/pages/queue/signin' },
    { icon: '📋', label: '接种记录', url: '/pages/record/list' }
  ]
  if (userStore.isRegisterDoctor) return [
    { icon: '🧾', label: '登记队列', url: '/pages/queue/vaccinate' },
    { icon: '📋', label: '接种记录', url: '/pages/record/list' }
  ]
  if (userStore.isVaccinateDoctor) return [
    { icon: '💉', label: '接种队列', url: '/pages/queue/vaccinate' },
    { icon: '📋', label: '接种记录', url: '/pages/record/list' }
  ]
  if (userStore.isObserveDoctor) return [
    { icon: '👁', label: '留观队列', url: '/pages/queue/observe' },
    { icon: '📋', label: '接种记录', url: '/pages/record/list' }
  ]
  if (userStore.isStockDoctor) return [
    { icon: '📦', label: '库存总览', url: '/pages/stock/summary' },
    { icon: '💊', label: '批次管理', url: '/pages/stock/batches' },
    { icon: '⚠️', label: '预警列表', url: '/pages/stock/alerts' },
    { icon: '🔄', label: '调拨记录', url: '/pages/stock/transfer-records' }
  ]
  return [
    { icon: '🔍', label: '预检队列', url: '/pages/queue/precheck' },
    { icon: '📋', label: '接种记录', url: '/pages/record/list' }
  ]
})

const TAB_PAGES = ['/pages/index/index', '/pages/appointment/list', '/pages/mine/index']

function navigateTo(url) {
  if (TAB_PAGES.includes(url)) {
    uni.switchTab({ url })
  } else {
    uni.navigateTo({ url })
  }
}

onShow(() => {
  if (userStore.isLoggedIn) {
    userStore.fetchProfile()
    if (userStore.isAdminRole) loadAdminStats()
    if (userStore.isDoctor && !userStore.isAdminRole) loadDoctorStats()
  }
})

async function loadAdminStats() {
  try {
    const [vacData, stockData, anomalyData] = await Promise.all([
      getVaccinationStats(),
      getStockStats(),
      getAnomalyStats()
    ])
    todayVaccinations.value = vacData?.todayCompleted || 0
    stockAlerts.value = stockData?.unhandledAlerts || 0
    totalAnomalies.value = anomalyData?.totalAnomalies || 0
  } catch (e) {
    console.error('加载统计失败', e)
  }
  try {
    const notices = await getNoticeList({})
    const list = notices || []
    pendingNotices.value = Array.isArray(list) ? list.filter(n => n.statusCode === 0).length : 0
  } catch (e) {
    console.error('加载待审批数失败', e)
  }
}

async function loadDoctorStats() {
  try {
    const data = await getMyTodayStats()
    if (data) {
      todayStats.value = {
        appointed: data.appointed || 0,
        signedIn: data.signedIn || 0,
        precheckPass: data.precheckPass || 0,
        observing: data.observing || 0,
        completed: data.completed || 0,
        cancelled: data.cancelled || 0
      }
    }
  } catch (e) {
    console.error('加载医生统计失败', e)
  }
}
</script>

<style lang="scss" scoped>
.home-page { min-height: 100vh; padding: $spacing-lg; padding-bottom: 140rpx; }
.user-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: $spacing-lg; }
.user-info { display: flex; flex-direction: column; }
.user-name { font-size: $font-size-xl; font-weight: 600; }
.user-role { font-size: $font-size-sm; color: $color-text-secondary; margin-top: $spacing-xs; }
.today-date { font-size: $font-size-sm; color: $color-text-secondary; }
.section { margin-bottom: $spacing-lg; }
.section-title { font-size: $font-size-lg; font-weight: 600; margin-bottom: $spacing-md; }
.stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(150rpx, 1fr)); gap: $spacing-md; }
.stat-item { background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md; text-align: center; box-shadow: $shadow-card; }
.stat-item:active { opacity: 0.7; }
.stat-value { font-size: 48rpx; font-weight: 600; color: $color-primary; }
.stat-value.danger { color: $color-danger; }
.stat-label { font-size: $font-size-xs; color: $color-text-secondary; margin-top: $spacing-xs; display: block; }
.shortcut-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: $spacing-md; }
.shortcut-item { background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md $spacing-sm; display: flex; flex-direction: column; align-items: center; box-shadow: $shadow-card; }
.shortcut-icon { font-size: 48rpx; }
.shortcut-label { font-size: $font-size-sm; color: $color-text-regular; margin-top: $spacing-xs; }
.window-info { background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md; box-shadow: $shadow-card; display: flex; justify-content: space-between; align-items: center; }
.window-label { font-size: $font-size-sm; color: $color-text-secondary; }
.window-name { font-size: $font-size-base; font-weight: 600; color: $color-primary; }
</style>
