<template>
  <view class="queue-info-card" :class="{ calling: isCalling }">
    <text class="window-name">📍 {{ windowName }}</text>
    <text class="queue-position">{{ queuePosition }}</text>
    <text class="wait-info">前方还有 {{ queuePosition - 1 }} 人</text>
    <text v-if="estimatedWait" class="wait-info">预计等待约 {{ estimatedWait }} 分钟</text>
  </view>
</template>

<script setup>
defineProps({
  windowName: { type: String, default: '' },
  queuePosition: { type: Number, default: 0 },
  estimatedWait: { type: Number, default: 0 },
  isCalling: { type: Boolean, default: false }
})
</script>

<style lang="scss" scoped>
.queue-info-card {
  background: $color-primary-light;
  border-radius: $radius-lg;
  padding: $spacing-base;
  text-align: center;
  margin: $spacing-md 0;
}
.queue-info-card.calling { border: 2rpx solid $color-warning; animation: blink 1s infinite; }
@keyframes blink { 0%, 100% { border-color: $color-warning; } 50% { border-color: transparent; } }
.window-name { display: block; font-size: $font-size-sm; color: $color-text-secondary; margin-bottom: $spacing-sm; }
.queue-position { display: block; font-size: 80rpx; font-weight: 600; color: $color-primary; }
.wait-info { display: block; font-size: $font-size-sm; color: $color-text-secondary; margin-top: 4rpx; }
</style>
