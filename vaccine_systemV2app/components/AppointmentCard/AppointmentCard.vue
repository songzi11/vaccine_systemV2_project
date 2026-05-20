<template>
  <view class="appointment-card" @tap="$emit('click', appointment)">
    <view class="card-header">
      <text class="vaccine-name">{{ appointment.vaccineName }}</text>
      <AppStatusTag :status="appointment.status" />
    </view>
    <view class="card-body">
      <text class="info-text">👶 {{ appointment.childName }}</text>
      <text class="info-text">📅 {{ appointment.appointmentDate }}</text>
      <text class="info-text">🕐 {{ formatTimeSlot(appointment.timeSlot) }}</text>
      <text v-if="appointment.status === 1" class="info-text window-text">🪟 请前往预检窗口</text>
      <text v-else-if="appointment.windowName" class="info-text window-text">🪟 请前往 {{ appointment.windowName }}</text>
    </view>
    <slot name="action" />
  </view>
</template>

<script setup>
import AppStatusTag from '@/components/AppStatusTag/AppStatusTag.vue'
import { formatTimeSlot } from '@/utils/format.js'
defineProps({ appointment: { type: Object, required: true } })
defineEmits(['click'])
</script>

<style lang="scss" scoped>
.appointment-card {
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
.vaccine-name { font-size: $font-size-base; font-weight: 600; color: $color-text-primary; }
.card-body { display: flex; flex-direction: column; gap: 4rpx; }
.info-text { font-size: $font-size-sm; color: $color-text-secondary; }
.window-text { color: $color-primary; font-weight: $font-weight-medium; }
</style>
