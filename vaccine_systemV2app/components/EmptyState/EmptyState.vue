<template>
  <view class="empty-state">
    <uni-icons :type="icon" :size="iconSize" color="#C0C0C0" />
    <text class="empty-title">{{ title }}</text>
    <text v-if="description" class="empty-desc">{{ description }}</text>
    <view v-if="actionText" class="empty-action" @tap="$emit('action')">
      <text class="empty-action-text">{{ actionText }}</text>
    </view>
  </view>
</template>

<script setup>
defineProps({
  icon: { type: String, default: 'search' },
  title: { type: String, default: '暂无数据' },
  description: { type: String, default: '' },
  actionText: { type: String, default: '' },
  iconSize: { type: Number, default: 120 }
})

defineEmits(['action'])
</script>

<style lang="scss" scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 60rpx;
}

.empty-title {
  font-size: $font-size-base;
  color: $color-text-secondary;
  margin-top: $spacing-lg;
}

.empty-desc {
  font-size: $font-size-sm;
  color: $color-text-placeholder;
  margin-top: $spacing-xs;
  text-align: center;
}

.empty-action {
  margin-top: $spacing-lg;
  padding: $spacing-sm $spacing-xl;
  background-color: $color-primary-light;
  border-radius: $radius-round;
}

.empty-action-text {
  font-size: $font-size-base;
  color: $color-primary;
}
</style>
