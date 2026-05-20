<template>
  <scroll-view scroll-x class="step-indicator">
    <view class="steps">
      <view v-for="(step, index) in steps" :key="index" class="step-item">
        <view class="step-dot" :class="{ completed: index < current, current: index === current }">
          <uni-icons v-if="index < current" type="checkmarkempty" :size="14" color="#07C160" />
          <view v-else-if="index === current" class="dot-active" />
          <view v-else class="dot-pending" />
        </view>
        <text class="step-text" :class="{ active: index <= current }">{{ step.title }}</text>
        <view v-if="index < steps.length - 1" class="step-line" :class="{ completed: index < current }" />
      </view>
    </view>
  </scroll-view>
</template>

<script setup>
const props = defineProps({
  steps: { type: Array, required: true },
  current: { type: Number, default: 0 }
})
</script>

<style lang="scss" scoped>
.step-indicator { white-space: nowrap; }
.steps { display: inline-flex; align-items: flex-start; padding: 0 $spacing-base; }
.step-item { display: flex; flex-direction: column; align-items: center; min-width: 100rpx; }
.step-dot {
  width: 40rpx; height: 40rpx; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  border: 2rpx solid $color-border;
  margin-bottom: $spacing-xs;
}
.step-dot.completed { border-color: $color-success; background: $color-success-light; }
.step-dot.current { border-color: $color-success; }
.dot-active { width: 16rpx; height: 16rpx; border-radius: 50%; background: $color-success; }
.dot-pending { width: 12rpx; height: 12rpx; border-radius: 50%; background: $color-text-placeholder; }
.step-text { font-size: $font-size-xs; color: $color-text-placeholder; }
.step-text.active { color: $color-text-primary; font-weight: 500; }
.step-line {
  width: 40rpx; height: 2rpx; background: $color-border;
  margin: 0 $spacing-xs 18rpx;
  align-self: flex-start; margin-top: 18rpx;
}
.step-line.completed { background: $color-success; }
</style>
