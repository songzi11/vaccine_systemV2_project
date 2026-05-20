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
    <StepIndicator :steps="stepLabels" :current="currentStep" />

    <!-- 排队信息 -->
    <view v-if="queueData && queueData.currentQueue > 0" class="queue-section">
      <QueueInfoCard
        :windowName="queueData.currentWindow || '等候区'"
        :queuePosition="queueData.currentQueue"
        :estimatedWait="queueData.estimatedWaitMinutes"
        :isCalling="queueData.currentQueue === 1"
      />
    </view>

    <!-- 当前步骤描述 -->
    <view class="step-description">
      <text>{{ nextGuide || '请等待叫号...' }}</text>
    </view>

    <!-- 窗口与医生信息 -->
    <view v-if="windowInfo" class="window-section">
      <view class="window-card">
        <text class="window-label">当前窗口</text>
        <text class="window-name">{{ windowInfo.windowName }}</text>
        <text v-if="windowInfo.doctorName" class="doctor-name">值班医生：{{ windowInfo.doctorName }}</text>
      </view>
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
import { onShow, onHide, onUnload } from '@dcloudio/uni-app'
import { getAppointmentGuide, getAppointmentQueue, cancelAppointment } from '@/api/appointment.js'
import { APPOINTMENT_STATUS_TEXT } from '@/utils/constants.js'
import { formatTimeSlot } from '@/utils/format.js'
import StepIndicator from '@/components/StepIndicator/StepIndicator.vue'
import QueueInfoCard from '@/components/QueueInfoCard/QueueInfoCard.vue'

const appointmentId = ref(null)
const guideData = ref(null)
const queueData = ref(null)
let pollingTimer = null

const stepLabels = [{ title: '签到' }, { title: '预检' }, { title: '接种' }, { title: '留观' }, { title: '完成' }]

// 状态码 → 步骤索引
const STATUS_TO_STEP = { 1: 0, 6: 1, 7: 2, 10: 3, 2: 4 }

// 状态码 → 下一步指引文案
const STATUS_GUIDE = {
  1: '请前往预检窗口，预检医生将为您签到并预检',
  6: '已签到，请等待预检叫号',
  7: '预检通过，请前往接种窗口等候',
  10: '接种完成，请前往留观区观察30分钟',
  2: '接种流程已全部完成'
}

const currentStep = computed(() => STATUS_TO_STEP[guideData.value?.status] ?? 0)

const nextGuide = computed(() => STATUS_GUIDE[guideData.value?.status] || '请等待叫号...')

const canCancel = computed(() => {
  const status = guideData.value?.status
  return status === 1 || status === 6
})

const appointmentInfo = computed(() => {
  if (!guideData.value) return {}
  const g = guideData.value
  return {
    ...g,
    statusText: APPOINTMENT_STATUS_TEXT[g.status] || '未知',
    timeSlot: formatTimeSlot(g.timeSlot)
  }
})

const windowInfo = computed(() => {
  const g = guideData.value
  if (!g) return null
  const status = g.status
  if (g.windowName) return { windowName: g.windowName, doctorName: g.doctorName }
  // 兜底：根据状态显示默认窗口
  if (status === 7) return { windowName: '接种窗口', doctorName: g.doctorName || '' }
  return null
})

onShow(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.$page?.options || currentPage.options || {}
  appointmentId.value = options.id || uni.getStorageSync('guideAppointmentId') || ''
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
  } catch (e) {
    console.error('加载流程指引失败', e)
  }
}

async function handleCancelAppointment() {
  uni.showModal({
    title: '确认取消',
    content: '确定要取消该预约吗？取消后需重新预约。',
    success: async (res) => {
      if (res.confirm) {
        try {
          await cancelAppointment(appointmentId.value)
          uni.showToast({ title: '已取消', icon: 'success' })
          setTimeout(() => uni.navigateBack(), 1500)
        } catch (e) {
          uni.showToast({ title: e.message || '取消失败', icon: 'none' })
        }
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.guide-page {
  min-height: 100vh;
  background-color: $color-bg-page;
  padding: $spacing-md;

  .status-card {
    padding: $spacing-lg;
    background-color: #FFFFFF;
    border-radius: $radius-lg;
    box-shadow: $shadow-card;

    .status-header { margin-bottom: $spacing-sm; }
    .status-title { font-size: $font-size-xl; font-weight: $font-weight-bold; color: $color-text-primary; }
    .status-meta {
      display: flex;
      flex-direction: column;
      font-size: $font-size-sm;
      color: $color-text-secondary;
      gap: 4rpx;
    }
  }

  .queue-section { margin-top: $spacing-lg; }

  .step-description {
    padding: $spacing-md $spacing-lg;
    background-color: $color-primary-light;
    border-radius: $radius-md;
    margin-top: $spacing-md;

    text { font-size: $font-size-base; color: $color-primary; font-weight: $font-weight-medium; }
  }

  .window-section {
    margin-top: $spacing-lg;

    .window-card {
      background-color: #FFFFFF;
      border-radius: $radius-lg;
      padding: $spacing-lg;
      box-shadow: $shadow-card;
      text-align: center;
    }

    .window-label {
      display: block;
      font-size: $font-size-xs;
      color: $color-text-secondary;
      margin-bottom: $spacing-xs;
    }

    .window-name {
      display: block;
      font-size: 48rpx;
      font-weight: $font-weight-bold;
      color: $color-primary;
    }

    .doctor-name {
      display: block;
      font-size: $font-size-sm;
      color: $color-text-regular;
      margin-top: $spacing-sm;
    }
  }

  .tips-card {
    margin-top: $spacing-lg;
    padding: $spacing-md $spacing-lg;
    background-color: #FFF8E1;
    border-radius: $radius-md;

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
      border-radius: $radius-lg;

      &::after { border: none; }
    }
  }
}
</style>
