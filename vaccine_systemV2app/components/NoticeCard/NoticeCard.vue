<template>
  <view class="notice-card" @tap="$emit('click', notice)">
    <view class="notice-header">
      <view class="type-tag" :class="typeClass">{{ typeText }}</view>
      <text class="notice-title">{{ notice.title }}</text>
    </view>
    <text class="notice-date">{{ notice.createdAt }}</text>
  </view>
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps({ notice: { type: Object, required: true } })
defineEmits(['click'])
const typeClass = computed(() => {
  const map = { NORMAL: 'type-normal', URGENT: 'type-urgent', SYSTEM: 'type-system' }
  return map[props.notice.type] || 'type-normal'
})
const typeText = computed(() => {
  const map = { NORMAL: '公告', URGENT: '紧急', SYSTEM: '系统' }
  return map[props.notice.type] || '公告'
})
</script>

<style lang="scss" scoped>
.notice-card {
  padding: $spacing-base 0;
  border-bottom: 1rpx solid $color-border-light;
}
.notice-header { display: flex; align-items: center; gap: $spacing-sm; margin-bottom: $spacing-xs; }
.type-tag {
  padding: 4rpx 12rpx; border-radius: $radius-sm; font-size: $font-size-xs; flex-shrink: 0;
}
.type-normal { background: $color-info-light; color: $color-info; }
.type-urgent { background: $color-danger-light; color: $color-danger; }
.type-system { background: $color-success-light; color: $color-success; }
.notice-title { font-size: $font-size-base; color: $color-text-primary; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.notice-date { font-size: $font-size-xs; color: $color-text-placeholder; }
</style>
