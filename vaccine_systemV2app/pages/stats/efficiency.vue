<template>
  <view class="efficiency-stats-page">
    <uni-nav-bar title="效率统计" :border="false" />

    <view class="filter-section">
      <view class="filter-row">
        <view class="filter-item">
          <text class="filter-label">统计类型</text>
          <picker :range="['时长', '排队', '完成率']" @change="onTypeChange">
            <view class="filter-value">{{ filter.statsType || '时长' }}</view>
          </picker>
        </view>
        <view class="filter-item">
          <text class="filter-label">日期范围</text>
          <uni-datetime-picker type="daterange" v-model="filter.dateRange" start="2025-01-01" end="2026-12-31" />
        </view>
        <button class="query-btn" @tap="queryStats">查询</button>
      </view>
    </view>

    <view class="summary-scroll">
      <view class="summary-card">
        <text class="summary-value">{{ stats.avgSigninDuration || '0' }}</text>
        <text class="summary-unit">min</text>
        <text class="summary-label">平均签到时长</text>
      </view>
      <view class="summary-card">
        <text class="summary-value">{{ stats.avgPrecheckDuration || '0' }}</text>
        <text class="summary-unit">min</text>
        <text class="summary-label">平均预检时长</text>
      </view>
      <view class="summary-card">
        <text class="summary-value">{{ stats.avgVaccinateDuration || '0' }}</text>
        <text class="summary-unit">min</text>
        <text class="summary-label">平均接种时长</text>
      </view>
      <view class="summary-card">
        <text class="summary-value">{{ stats.avgObserveDuration || '0' }}</text>
        <text class="summary-unit">min</text>
        <text class="summary-label">平均留观时长</text>
      </view>
    </view>

    <view class="chart-section">
      <ChartPanel canvasId="durationBarChart" title="各环节时长对比" :chartType="'bar'" :chartData="chartData.bar" />
    </view>
    <view class="chart-section">
      <ChartPanel canvasId="completionLineChart" title="完成率趋势" :chartType="'line'" :chartData="chartData.line" />
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getEfficiencyStats } from '@/api/stats.js'
import ChartPanel from '@/components/ChartPanel/ChartPanel.vue'

const filter = reactive({ statsType: '时长', dateRange: [] })
const stats = ref({})
const chartData = reactive({ bar: {}, line: {} })

onShow(() => { queryStats() })

async function queryStats() {
  try {
    const params = { statsType: filter.statsType }
    if (filter.dateRange && filter.dateRange.length === 2) {
      params.startDate = filter.dateRange[0]
      params.endDate = filter.dateRange[1]
    }
    const data = await getEfficiencyStats(params)
    stats.value = data || {}
    chartData.bar = data?.barData || {}
    chartData.line = data?.lineData || {}
  } catch (e) {
    uni.showToast({ title: '查询失败', icon: 'none' })
  }
}

function onTypeChange(e) {
  const types = ['时长', '排队', '完成率']
  filter.statsType = types[e.detail.value] || '时长'
}
</script>

<style lang="scss" scoped>
.efficiency-stats-page { min-height: 100vh; background: $color-bg-page; padding: $spacing-lg; }
.filter-section { padding: $spacing-md $spacing-lg; background: $color-bg-white; border-radius: $radius-lg; margin-bottom: $spacing-md; }
.filter-row { display: flex; flex-wrap: wrap; gap: $spacing-md; }
.filter-item { flex: 1; min-width: 200rpx; }
.filter-label { font-size: $font-size-sm; color: $color-text-secondary; margin-bottom: $spacing-xs; display: block; }
.filter-value { font-size: $font-size-base; color: $color-text-primary; padding: $spacing-xs $spacing-sm; background: $color-bg-grey; border-radius: $radius-sm; }
.query-btn { padding: $spacing-xs $spacing-lg; background: $color-primary; color: #FFFFFF; font-size: $font-size-base; border: none; border-radius: $radius-md; }
.summary-scroll { display: flex; gap: $spacing-md; overflow-x: auto; padding: $spacing-md 0; margin-bottom: $spacing-lg; }
.summary-card { flex: 1; min-width: 140rpx; padding: $spacing-md; background: $color-bg-white; border-radius: $radius-lg; box-shadow: $shadow-card; text-align: center; }
.summary-value { font-size: $font-size-xxl; font-weight: 600; color: $color-primary; }
.summary-unit { font-size: $font-size-xs; color: $color-text-placeholder; display: block; }
.summary-label { font-size: $font-size-xs; color: $color-text-secondary; margin-top: $spacing-xs; display: block; }
.chart-section { background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-lg; margin-bottom: $spacing-md; min-height: 400rpx; }
</style>
