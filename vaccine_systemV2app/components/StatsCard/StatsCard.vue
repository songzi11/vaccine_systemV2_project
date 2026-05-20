<template>
  <view class="stats-card">
    <text class="stats-value" :class="{ danger: danger }">{{ displayValue }}</text>
    <text class="stats-label">{{ label }}</text>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  value: { type: [Number, String], default: 0 },
  label: { type: String, required: true },
  suffix: { type: String, default: '' },
  danger: { type: Boolean, default: false }
})

const displayValue = computed(() => {
  if (props.suffix) return `${props.value}${props.suffix}`
  return props.value
})
</script>

<style lang="scss" scoped>
.stats-card {
  background: $color-bg-white;
  border-radius: $radius-lg;
  padding: $spacing-md;
  text-align: center;
  min-width: 160rpx;
  flex-shrink: 0;
  box-shadow: $shadow-card;
}
.stats-value {
  font-size: 48rpx;
  font-weight: 600;
  color: $color-primary;
  display: block;
}
.stats-value.danger { color: $color-danger; }
.stats-label {
  font-size: $font-size-xs;
  color: $color-text-secondary;
  margin-top: $spacing-xs;
  display: block;
}
</style>
