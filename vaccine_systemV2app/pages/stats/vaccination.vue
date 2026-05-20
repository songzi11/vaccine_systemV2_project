<template>
  <view class="stats-page">
    <uni-nav-bar title="接种统计" :border="false" />

    <view class="date-info">
      <text class="date-text">数据日期：{{ stats.date || '--' }}</text>
    </view>

    <view class="stats-grid">
      <view class="stat-card">
        <text class="stat-value primary">{{ stats.todayAppointments }}</text>
        <text class="stat-label">今日预约</text>
      </view>
      <view class="stat-card">
        <text class="stat-value success">{{ stats.todayCompleted }}</text>
        <text class="stat-label">今日完成</text>
      </view>
      <view class="stat-card">
        <text class="stat-value">{{ stats.totalVaccines }}</text>
        <text class="stat-label">疫苗种类</text>
      </view>
      <view class="stat-card">
        <text class="stat-value">{{ stats.totalBatches }}</text>
        <text class="stat-label">疫苗批次</text>
      </view>
      <view class="stat-card">
        <text class="stat-value">{{ stats.totalUsers }}</text>
        <text class="stat-label">注册用户</text>
      </view>
      <view class="stat-card">
        <text class="stat-value danger">{{ stats.totalAdverseReactions }}</text>
        <text class="stat-label">不良反应</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getVaccinationStats } from '@/api/stats.js'

const stats = ref({})

onShow(() => { loadStats() })

async function loadStats() {
  try {
    stats.value = await getVaccinationStats() || {}
  } catch (e) {
    uni.showToast({ title: '查询失败', icon: 'none' })
  }
}
</script>

<style lang="scss" scoped>
.stats-page { min-height: 100vh; background: $color-bg-page; padding: $spacing-lg; padding-bottom: 140rpx; }
.date-info { margin-bottom: $spacing-md; }
.date-text { font-size: $font-size-sm; color: $color-text-secondary; }
.stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: $spacing-md; }
.stat-card {
  background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-lg $spacing-md;
  box-shadow: $shadow-card; text-align: center; display: flex; flex-direction: column;
  align-items: center; justify-content: center; min-height: 180rpx;
}
.stat-value { font-size: 52rpx; font-weight: 700; color: $color-text-primary; }
.stat-value.primary { color: $color-primary; }
.stat-value.success { color: $color-success; }
.stat-value.danger { color: $color-danger; }
.stat-label { font-size: $font-size-xs; color: $color-text-secondary; margin-top: $spacing-xs; }
</style>
