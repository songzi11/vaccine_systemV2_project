<template>
  <view class="success-page">
    <view class="success-icon-wrap">
      <text class="success-icon">✓</text>
    </view>
    <text class="success-title">接种完成</text>

    <!-- 接种信息卡片 -->
    <view class="info-card">
      <view class="info-row">
        <text class="info-label">注射号</text>
        <text class="info-value">{{ injectionNo }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">注射部位</text>
        <text class="info-value">{{ siteLabel }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">批次号</text>
        <text class="info-value">{{ batchNo }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">接种时间</text>
        <text class="info-value">{{ vaccinateTime }}</text>
      </view>
    </view>

    <!-- 留观提醒 -->
    <view class="observe-reminder">
      <text class="reminder-title">留观提醒</text>
      <text class="reminder-text">请在留观区等待至少30分钟</text>
      <text class="reminder-time">预计可离开时间：{{ leaveTime }}</text>
    </view>

    <button class="btn-back" @click="goBack">返回队列</button>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { INJECTION_SITES } from '@/utils/constants.js'

const injectionNo = ref('')
const site = ref('')
const batchNo = ref('')
const childName = ref('')
const vaccinateTime = ref('')

onLoad((query) => {
  injectionNo.value = query.injectionNo ? decodeURIComponent(query.injectionNo) : ''
  site.value = query.site ? decodeURIComponent(query.site) : ''
  batchNo.value = query.batchNo ? decodeURIComponent(query.batchNo) : ''
  childName.value = query.childName ? decodeURIComponent(query.childName) : ''
  const now = new Date()
  vaccinateTime.value = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
})

const siteLabel = computed(() => {
  const found = INJECTION_SITES.find(s => s.value === site.value)
  return found ? found.label : site.value
})

const leaveTime = computed(() => {
  const now = new Date()
  now.setMinutes(now.getMinutes() + 30)
  return `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
})

function goBack() {
  uni.redirectTo({ url: '/pages/queue/vaccinate' })
}
</script>

<style lang="scss" scoped>
.success-page {
  min-height: 100vh;
  background: $color-bg-page;
  padding: $spacing-xl $spacing-lg;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.success-icon-wrap {
  width: 120rpx; height: 120rpx;
  border-radius: 50%;
  background: $color-primary;
  display: flex; align-items: center; justify-content: center;
  margin-top: 60rpx; margin-bottom: $spacing-lg;
}

.success-icon { font-size: 60rpx; color: #FFFFFF; font-weight: bold; }
.success-title { font-size: $font-size-xl; font-weight: 600; margin-bottom: $spacing-xl; }

.info-card {
  width: 100%;
  background: $color-bg-white;
  border-radius: $radius-lg;
  padding: $spacing-lg;
  box-shadow: $shadow-card;
  margin-bottom: $spacing-lg;
}

.info-row {
  display: flex; justify-content: space-between;
  padding: $spacing-sm 0;
  border-bottom: 1rpx solid $color-border-light;
  &:last-child { border-bottom: none; }
}

.info-label { font-size: $font-size-sm; color: $color-text-secondary; }
.info-value { font-size: $font-size-base; color: $color-text-primary; font-weight: 500; }

.observe-reminder {
  width: 100%;
  background: rgba(255, 153, 0, 0.08);
  border-radius: $radius-lg;
  padding: $spacing-lg;
  margin-bottom: $spacing-xl;
  border-left: 6rpx solid $color-warning;
}

.reminder-title { font-size: $font-size-base; font-weight: 600; color: $color-warning; display: block; margin-bottom: $spacing-xs; }
.reminder-text { font-size: $font-size-sm; color: $color-text-regular; display: block; margin-bottom: $spacing-xs; }
.reminder-time { font-size: $font-size-sm; color: $color-text-secondary; display: block; }

.btn-back {
  width: 100%; height: 88rpx; line-height: 88rpx;
  background: $color-primary; color: #FFFFFF;
  font-size: $font-size-lg; border: none; border-radius: $radius-lg;
  &::after { border: none; }
}
</style>
