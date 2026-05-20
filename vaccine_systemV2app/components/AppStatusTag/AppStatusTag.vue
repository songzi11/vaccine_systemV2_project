<template>
  <view class="app-status-tag" :style="{ backgroundColor: bgColor, color: textColor }">
    <text>{{ text }}</text>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { APPOINTMENT_STATUS_TEXT, APPOINTMENT_STATUS_COLOR } from '@/utils/constants.js'

const props = defineProps({ status: { type: Number, required: true } })

const text = computed(() => APPOINTMENT_STATUS_TEXT[props.status] || '未知')
const bgColor = computed(() => (APPOINTMENT_STATUS_COLOR[props.status] || '#999999') + '1A')
const textColor = computed(() => APPOINTMENT_STATUS_COLOR[props.status] || '#999999')
</script>

<style lang="scss" scoped>
.app-status-tag {
  display: inline-flex;
  align-items: center;
  height: 40rpx;
  padding: 0 $spacing-sm;
  border-radius: $radius-pill;
  font-size: $font-size-xs;
  font-weight: 500;
}
</style>
