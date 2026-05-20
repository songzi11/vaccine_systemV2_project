<template>
  <view class="admin-nav-card" @tap="$emit('click')">
    <view class="card-icon">
      <text class="icon-text">{{ icon }}</text>
    </view>
    <view class="card-body">
      <text class="card-title">{{ title }}</text>
      <text v-if="permission" class="card-permission">{{ permission }}</text>
    </view>
    <uni-icons type="right" :size="16" color="#C0C0C0" />
  </view>
</template>

<script setup>
defineProps({
  icon: { type: String, default: '' },
  title: { type: String, required: true },
  permission: { type: String, default: '' }
})
defineEmits(['click'])
</script>

<style lang="scss" scoped>
.admin-nav-card {
  display: flex;
  align-items: center;
  background: $color-bg-white;
  border-radius: $radius-lg;
  padding: $spacing-md;
  box-shadow: $shadow-card;
}
.card-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: $radius-lg;
  background: $color-primary-light;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: $spacing-md;
  flex-shrink: 0;
}
.icon-text { font-size: 36rpx; }
.card-body { flex: 1; display: flex; flex-direction: column; gap: 4rpx; }
.card-title { font-size: $font-size-base; font-weight: 600; color: $color-text-primary; }
.card-permission { font-size: $font-size-xs; color: $color-text-placeholder; }
</style>
