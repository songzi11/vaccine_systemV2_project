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
        <text class="info-label">注射号</text>
        <text class="info-value">{{ observeInfo.injectionNo }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">接种时间</text>
        <text class="info-value">{{ observeInfo.injectionTime }}</text>
      </view>
    </view>

    <!-- 倒计时进度 -->
    <view class="countdown-section">
      <view class="countdown-circle">
        <CountdownTimer :elapsed="elapsedSeconds" :total="observeTotalSeconds" label="已观察" />
      </view>
      <view class="countdown-meta">
        <text class="countdown-time">{{ formattedElapsedTime }}</text>
        <text class="countdown-status" :class="{ ready: canFinish }">
          {{ canFinish ? '可结束留观' : '需观察满30分钟' }}
        </text>
      </view>
    </view>

    <!-- 不良反应状态 -->
    <view v-if="observeInfo && observeInfo.hasAdverseReaction" class="adverse-tag">
      <text class="adverse-text">已上报不良反应</text>
    </view>

    <!-- 底部操作 -->
    <view class="bottom-bar">
      <button class="btn-adverse" @tap="goToReport">上报不良反应</button>
      <button class="btn-finish" :disabled="!canFinish" :loading="submitting" @tap="handleFinishObserve">
        结束留观
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad, onShow, onHide, onUnload } from '@dcloudio/uni-app'
import { getStatus, finishObserve as finishObserveApi } from '@/api/observe.js'
import { OBSERVE_MIN_DURATION } from '@/utils/constants.js'
import CountdownTimer from '@/components/CountdownTimer/CountdownTimer.vue'

const injectionId = ref(null)
const observeInfo = ref({})
const injectionTime = ref(null)
const elapsedSeconds = ref(0)
const submitting = ref(false)
let countdownInterval = null

const observeTotalSeconds = OBSERVE_MIN_DURATION * 60

const canFinish = computed(() => elapsedSeconds.value >= observeTotalSeconds)

const formattedElapsedTime = computed(() => {
  const mins = Math.floor(elapsedSeconds.value / 60)
  const secs = elapsedSeconds.value % 60
  return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
})

function calcElapsed() {
  if (!injectionTime.value) return 0
  const t = new Date(injectionTime.value.replace(' ', 'T'))
  if (isNaN(t.getTime())) return 0
  return Math.floor((Date.now() - t.getTime()) / 1000)
}

onLoad(async (query) => {
  injectionId.value = query.injectionId || ''
  await loadStatus()
  startCountdown()
})

onShow(() => { startCountdown() })
onHide(() => { stopCountdown() })
onUnload(() => { stopCountdown() })

async function loadStatus() {
  try {
    const data = await getStatus(injectionId.value)
    observeInfo.value = data || {}
    if (data && data.injectionTime) {
      injectionTime.value = data.injectionTime
      elapsedSeconds.value = calcElapsed()
    }
  } catch (e) {
    console.error('加载留观状态失败', e)
  }
}

function startCountdown() {
  stopCountdown()
  elapsedSeconds.value = calcElapsed()
  countdownInterval = setInterval(() => {
    elapsedSeconds.value = calcElapsed()
  }, 1000)
}

function stopCountdown() {
  if (countdownInterval) {
    clearInterval(countdownInterval)
    countdownInterval = null
  }
}

async function handleFinishObserve() {
  if (!canFinish.value) {
    uni.showToast({ title: '需观察满30分钟', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    const appointmentId = observeInfo.value.appointmentId
    const durationMinutes = Math.max(Math.ceil(elapsedSeconds.value / 60), OBSERVE_MIN_DURATION)
    await finishObserveApi(appointmentId, { durationMinutes })
    uni.showToast({ title: '留观已完成', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) {
    uni.showToast({ title: e.message || '操作失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

function goToReport() {
  const appointmentId = observeInfo.value.appointmentId || ''
  uni.navigateTo({ url: `/pages/process/adverse-report?appointmentId=${appointmentId}` })
}
</script>

<style lang="scss" scoped>
.observe-detail {
  min-height: 100vh;
  background-color: $color-bg-page;
  padding: $spacing-lg;
  padding-bottom: 140rpx;
}

.info-card {
  padding: $spacing-lg;
  background: #FFFFFF;
  border-radius: $radius-lg;
  box-shadow: $shadow-card;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: $spacing-sm 0;
  border-bottom: 1rpx solid $color-border-light;

  &:last-child { border-bottom: none; }
}

.info-label { font-size: $font-size-sm; color: $color-text-secondary; }
.info-value { font-size: $font-size-base; color: $color-text-primary; font-weight: 500; }

.countdown-section {
  display: flex;
  align-items: center;
  gap: $spacing-lg;
  padding: $spacing-lg;
  background: #FFFFFF;
  border-radius: $radius-lg;
  box-shadow: $shadow-card;
  margin-top: $spacing-lg;
}

.countdown-circle { width: 200rpx; height: 200rpx; }
.countdown-time { font-size: $font-size-xxl; font-weight: 600; color: $color-text-primary; display: block; }
.countdown-status { font-size: $font-size-sm; margin-top: $spacing-xs; color: $color-text-secondary; &.ready { color: $color-success; } }

.adverse-tag {
  margin-top: $spacing-md;
  padding: $spacing-xs $spacing-md;
  background: rgba(238, 10, 36, 0.1);
  border-radius: $radius-lg;
}

.adverse-text { font-size: $font-size-sm; color: $color-danger; }

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
}

.btn-adverse {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  background: #FFFFFF;
  color: $color-danger;
  font-size: $font-size-base;
  border: 2rpx solid $color-danger;
  border-radius: $radius-lg;
}

.btn-finish {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  background-color: $color-primary;
  color: #FFFFFF;
  font-size: $font-size-lg;
  border: none;
  border-radius: $radius-lg;

  &:disabled { opacity: 0.5; }
  &::after { border: none; }
}
</style>
