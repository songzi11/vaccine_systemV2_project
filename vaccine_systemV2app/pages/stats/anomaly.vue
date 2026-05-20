<template>
  <view class="anomaly-page">
    <uni-nav-bar title="异常统计" :border="false" />

    <view class="stats-grid">
      <view class="stat-card" @tap="navigateTo('/pages/stock/alerts')">
        <text class="stat-value warning">{{ stats.unhandledAlerts }}</text>
        <text class="stat-label">库存预警</text>
      </view>
      <view class="stat-card">
        <text class="stat-value warning">{{ stats.nearExpiryBatches }}</text>
        <text class="stat-label">即将过期</text>
      </view>
      <view class="stat-card">
        <text class="stat-value danger">{{ stats.expiredBatches }}</text>
        <text class="stat-label">已过期批次</text>
      </view>
      <view class="stat-card">
        <text class="stat-value danger">{{ stats.adverseReactionCount }}</text>
        <text class="stat-label">不良反应</text>
      </view>
      <view class="stat-card highlight">
        <text class="stat-value danger">{{ stats.totalAnomalies }}</text>
        <text class="stat-label">异常总数</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getAnomalyStats } from '@/api/stats.js'

const stats = ref({ unhandledAlerts: 0, nearExpiryBatches: 0, expiredBatches: 0, adverseReactionCount: 0, totalAnomalies: 0 })

function navigateTo(url) { uni.navigateTo({ url }) }

onShow(() => { loadStats() })

async function loadStats() {
  try {
    stats.value = await getAnomalyStats() || {}
  } catch (e) {
    uni.showToast({ title: '查询失败', icon: 'none' })
  }
}
</script>

<style lang="scss" scoped>
.anomaly-page { min-height: 100vh; background: $color-bg-page; padding: $spacing-lg; padding-bottom: 140rpx; }
.stats-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: $spacing-md; margin-top: $spacing-md; }
.stat-card {
  background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-lg $spacing-md;
  box-shadow: $shadow-card; text-align: center;
}
.stat-card.highlight { grid-column: span 2; }
.stat-value { font-size: 52rpx; font-weight: 700; color: $color-text-primary; }
.stat-value.warning { color: #FF9900; }
.stat-value.danger { color: $color-danger; }
.stat-label { font-size: $font-size-xs; color: $color-text-secondary; margin-top: $spacing-xs; display: block; }
</style>
