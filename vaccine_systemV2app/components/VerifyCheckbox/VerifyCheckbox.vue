<template>
  <view class="verify-checkbox" :class="{ checked: modelValue }" @tap="toggle">
    <view class="checkbox-icon">
      <view v-if="modelValue" class="checkbox-check">✓</view>
      <view v-else class="checkbox-border" />
    </view>
    <text class="checkbox-label">{{ label }}</text>
  </view>
</template>

<script setup>
const props = defineProps({
  modelValue: { type: Boolean, default: false },
  label: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue'])

function toggle() {
  emit('update:modelValue', !props.modelValue)
}
</script>

<style lang="scss" scoped>
.verify-checkbox {
  display: flex;
  align-items: center;
  padding: $spacing-sm 0;
  &.checked { .checkbox-icon { background-color: $color-primary; border-color: $color-primary; } }
  .checkbox-icon {
    width: 40rpx; height: 40rpx; border: 2rpx solid $color-border;
    border-radius: $radius-sm;
    display: flex; align-items: center; justify-content: center;
    margin-right: $spacing-sm;
  }
  .checkbox-check { font-size: 24rpx; color: #FFFFFF; font-weight: bold; }
  .checkbox-border { width: 20rpx; height: 20rpx; border: 2rpx solid $color-border-light; }
  .checkbox-label { font-size: $font-size-base; color: $color-text-regular; }
}
</style>
