<template>
  <view class="countdown">
    <view class="progress-bar">
      <view class="progress-fill" :style="{ width: progressPercent + '%', backgroundColor: barColor }" />
    </view>
    <text class="countdown-text" :style="{ color: barColor }">{{ displayText }}</text>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  elapsed: { type: Number, default: 0 },
  total: { type: Number, default: 1800 },
  label: { type: String, default: '已观察' }
})

const progressPercent = computed(() => Math.min((props.elapsed / props.total) * 100, 100))

const barColor = computed(() => {
  if (progressPercent.value >= 100) return '#07C160'
  if (progressPercent.value >= 80) return '#FF9900'
  return '#1989FA'
})

const remaining = computed(() => Math.max(props.total - props.elapsed, 0))

const displayText = computed(() => {
  const mins = Math.floor(remaining.value / 60)
  const secs = remaining.value % 60
  return `${props.label} ${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')} / ${formatTime(props.total)}`
})

function formatTime(seconds) {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
}
</script>

<style lang="scss" scoped>
.countdown { width: 100%; }
.progress-bar { height: 12rpx; background: $color-border-light; border-radius: 6rpx; overflow: hidden; margin-bottom: $spacing-xs; }
.progress-fill { height: 100%; border-radius: 6rpx; transition: width 1s linear, background-color 0.3s; }
.countdown-text { font-size: $font-size-sm; text-align: center; }
</style>
