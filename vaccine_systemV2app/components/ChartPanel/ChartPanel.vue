<template>
  <view class="chart-panel">
    <text v-if="title" class="panel-title">{{ title }}</text>
    <view class="chart-container">
      <canvas :canvas-id="canvasId" :id="canvasId" class="chart-canvas" :style="{ width: width + 'px', height: height + 'px' }" />
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'

const props = defineProps({
  title: { type: String, default: '' },
  canvasId: { type: String, default: 'chart' },
  chartData: { type: Object, default: () => ({}) },
  chartType: { type: String, default: 'column' },
  width: { type: Number, default: 650 },
  height: { type: Number, default: 400 }
})

onMounted(() => {
  // 图表初始化需要 u-charts 库，在实际环境中配置
})

watch(() => props.chartData, () => {
  // 图表数据更新时重新渲染
}, { deep: true })
</script>

<style lang="scss" scoped>
.chart-panel {
  background: $color-bg-white;
  border-radius: $radius-lg;
  padding: $spacing-md;
  margin-bottom: $spacing-md;
  box-shadow: $shadow-card;
}
.panel-title {
  font-size: $font-size-base;
  font-weight: 600;
  color: $color-text-primary;
  margin-bottom: $spacing-sm;
  display: block;
}
.chart-container { overflow-x: auto; }
.chart-canvas { width: 100%; }
</style>
