<template>
  <view class="record-card">
    <view class="card-header">
      <text class="vaccine-name">{{ record.vaccineName }}</text>
      <view class="status-tag status-completed">已完成 ✓</view>
    </view>
    <view class="card-body">
      <text class="info-text">👶 {{ record.childName }}</text>
      <text class="info-text">📅 {{ record.injectionDate }}</text>
      <text class="info-text">🏥 {{ record.stationName }}</text>
      <text class="info-text">📋 批次: {{ record.batchNo }}</text>
    </view>
  </view>
</template>

<script setup>
defineProps({ record: { type: Object, required: true } })
</script>

<style lang="scss" scoped>
.record-card {
  background: $color-bg-card;
  border-radius: $radius-lg;
  padding: $spacing-base;
  margin-bottom: $spacing-base;
  box-shadow: $shadow-card;
}
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: $spacing-sm; }
.vaccine-name { font-size: $font-size-base; font-weight: 600; color: $color-text-primary; }
.status-tag { padding: 4rpx 16rpx; border-radius: $radius-sm; font-size: $font-size-xs; background: $color-success-light; color: $color-success; }
.card-body { display: flex; flex-direction: column; gap: 4rpx; }
.info-text { font-size: $font-size-sm; color: $color-text-secondary; }
</style>
