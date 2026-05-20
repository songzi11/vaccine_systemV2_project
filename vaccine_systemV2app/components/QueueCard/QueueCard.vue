<template>
  <view class="queue-card" @click="$emit('click')">
    <view class="card-header">
      <text class="queue-no">{{ item.queueNo }}</text>
      <view class="status-badge" :style="{ backgroundColor: statusBgColor, color: statusTextColor }">
        <text class="status-text">{{ statusText }}</text>
      </view>
    </view>
    <view class="card-body">
      <text class="child-name">{{ item.childName }}</text>
      <text class="vaccine-name">{{ item.vaccineName }}</text>
    </view>
    <view class="card-footer">
      <text class="time-text">{{ timeText }}</text>
      <slot name="action" />
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { APPOINTMENT_STATUS_TEXT, APPOINTMENT_STATUS_COLOR } from '@/utils/constants.js'

const props = defineProps({
  item: { type: Object, required: true },
  statusField: { type: String, default: 'status' },
  timeField: { type: String, default: 'signinTime' }
})

defineEmits(['click'])

const statusText = computed(() => {
  return APPOINTMENT_STATUS_TEXT[props.item[props.statusField]] || '未知'
})

const statusTextColor = computed(() => {
  return APPOINTMENT_STATUS_COLOR[props.item[props.statusField]] || '#999999'
})

const statusBgColor = computed(() => {
  return statusTextColor.value + '1A'
})

const timeText = computed(() => props.item[props.timeField] || '')
</script>

<style lang="scss" scoped>
.queue-card {
  background: $color-bg-card;
  border-radius: $radius-lg;
  padding: $spacing-base;
  margin-bottom: $spacing-base;
  box-shadow: $shadow-card;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-sm;
}

.queue-no { font-size: $font-size-lg; font-weight: 600; color: $color-text-primary; }

.status-badge {
  padding: 4rpx 16rpx;
  border-radius: $radius-sm;
}

.status-text { font-size: $font-size-xs; }

.card-body { margin-bottom: $spacing-sm; }
.child-name { font-size: $font-size-base; color: $color-text-primary; display: block; }
.vaccine-name { font-size: $font-size-sm; color: $color-text-secondary; display: block; margin-top: 4rpx; }

.card-footer { display: flex; justify-content: space-between; align-items: center; }
.time-text { font-size: $font-size-xs; color: $color-text-placeholder; }
</style>
