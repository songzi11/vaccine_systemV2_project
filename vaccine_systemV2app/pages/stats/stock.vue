<template>
  <view class="stock-stats-page">
    <uni-nav-bar title="库存统计" :border="false" />

    <view class="filter-section">
      <view class="filter-row">
        <view class="filter-item">
          <text class="filter-label">统计类型</text>
          <picker :range="['总览', '批次', '预警']" @change="onTypeChange">
            <view class="filter-value">{{ filter.statsType || '总览' }}</view>
          </picker>
        </view>
        <button class="query-btn" @tap="queryStats">查询</button>
      </view>
    </view>

    <view class="summary-scroll">
      <view class="summary-card">
        <text class="summary-value">{{ stats.totalStock || 0 }}</text>
        <text class="summary-label">总数</text>
      </view>
      <view class="summary-card">
        <text class="summary-value">{{ stats.availableStock || 0 }}</text>
        <text class="summary-label">可用数</text>
      </view>
      <view class="summary-card">
        <text class="summary-value">{{ stats.lockedStock || 0 }}</text>
        <text class="summary-label">锁定数</text>
      </view>
      <view class="summary-card">
        <text class="summary-value">{{ stats.usageRate || '0%' }}</text>
        <text class="summary-label">使用率</text>
      </view>
      <view class="summary-card">
        <text class="summary-value warning">{{ stats.nearExpiryCount || 0 }}</text>
        <text class="summary-label">临期数</text>
      </view>
      <view class="summary-card">
        <text class="summary-value danger">{{ stats.expiredCount || 0 }}</text>
        <text class="summary-label">过期数</text>
      </view>
    </view>

    <view class="chart-section">
      <ChartPanel canvasId="stockPieChart" title="库存分布" :chartType="'pie'" :chartData="chartData.pie" />
    </view>
    <view class="chart-section">
      <ChartPanel canvasId="stockBarChart" title="各疫苗库存对比" :chartType="'bar'" :chartData="chartData.bar" />
    </view>

    <view v-if="filter.statsType === '预警'" class="alert-section">
      <text class="section-title">预警批次</text>
      <view v-for="item in (stats.alertList || [])" :key="item.id" class="alert-card">
        <view class="alert-header">
          <text class="alert-name">{{ item.vaccineName }}</text>
          <text class="alert-status" :class="item.level === 'EXPIRED' ? 'danger' : 'warning'">
            {{ item.level === 'EXPIRED' ? '已过期' : '临期' }}
          </text>
        </view>
        <text class="alert-meta">批次号：{{ item.batchNo }}</text>
        <text class="alert-meta">有效期至：{{ item.expiryDate }}</text>
        <text class="alert-meta">剩余数量：{{ item.remaining }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getStockStats } from '@/api/stats.js'
import ChartPanel from '@/components/ChartPanel/ChartPanel.vue'

const filter = reactive({ statsType: '总览' })
const stats = ref({})
const chartData = reactive({ pie: {}, bar: {} })

onShow(() => { queryStats() })

async function queryStats() {
  try {
    const params = { statsType: filter.statsType }
    const data = await getStockStats(params)
    stats.value = data || {}
    chartData.pie = data?.pieData || {}
    chartData.bar = data?.barData || {}
  } catch (e) {
    uni.showToast({ title: '查询失败', icon: 'none' })
  }
}

function onTypeChange(e) {
  const types = ['总览', '批次', '预警']
  filter.statsType = types[e.detail.value] || '总览'
}
</script>

<style lang="scss" scoped>
.stock-stats-page { min-height: 100vh; background: $color-bg-page; padding: $spacing-lg; }
.filter-section { padding: $spacing-md $spacing-lg; background: $color-bg-white; border-radius: $radius-lg; margin-bottom: $spacing-md; }
.filter-row { display: flex; gap: $spacing-md; align-items: center; }
.filter-item { flex: 1; }
.filter-label { font-size: $font-size-sm; color: $color-text-secondary; margin-bottom: $spacing-xs; display: block; }
.filter-value { font-size: $font-size-base; color: $color-text-primary; padding: $spacing-xs $spacing-sm; background: $color-bg-grey; border-radius: $radius-sm; }
.query-btn { padding: $spacing-xs $spacing-lg; background: $color-primary; color: #FFFFFF; font-size: $font-size-base; border: none; border-radius: $radius-md; }
.summary-scroll { display: flex; gap: $spacing-md; overflow-x: auto; padding: $spacing-md 0; margin-bottom: $spacing-lg; }
.summary-card { flex: 1; min-width: 140rpx; padding: $spacing-md; background: $color-bg-white; border-radius: $radius-lg; box-shadow: $shadow-card; text-align: center; }
.summary-value { font-size: $font-size-xl; font-weight: 600; color: $color-text-primary; }
.summary-value.warning { color: $color-warning; }
.summary-value.danger { color: $color-danger; }
.summary-label { font-size: $font-size-xs; color: $color-text-secondary; margin-top: $spacing-xs; display: block; }
.chart-section { background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-lg; margin-bottom: $spacing-md; min-height: 400rpx; }
.alert-section { margin-top: $spacing-md; }
.section-title { font-size: $font-size-base; font-weight: 600; margin-bottom: $spacing-md; display: block; }
.alert-card { background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md; margin-bottom: $spacing-md; box-shadow: $shadow-card; }
.alert-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: $spacing-sm; }
.alert-name { font-size: $font-size-base; font-weight: 600; color: $color-text-primary; }
.alert-status { font-size: $font-size-xs; padding: 2rpx 12rpx; border-radius: $radius-sm; }
.alert-status.warning { background: $color-warning-light; color: $color-warning; }
.alert-status.danger { background: $color-danger-light; color: $color-danger; }
.alert-meta { font-size: $font-size-xs; color: $color-text-secondary; display: block; }
</style>
