<template>
  <view class="info-card">
    <view class="card-header" @click="toggleVerify">
      <text class="card-title">{{ title }}</text>
      <view v-if="verifiable" class="verify-checkbox" :class="{ checked: verified }">
        <uni-icons v-if="verified" type="checkmarkempty" :size="14" color="#FFFFFF" />
      </view>
    </view>
    <view class="card-body">
      <view v-for="field in fields" :key="field.label" class="field-row">
        <text class="field-label">{{ field.label }}</text>
        <text class="field-value" :class="field.class">{{ field.value }}</text>
      </view>
    </view>
    <slot name="extra" />
  </view>
</template>

<script setup>
const props = defineProps({
  title: { type: String, required: true },
  fields: { type: Array, default: () => [] },
  verifiable: { type: Boolean, default: false },
  verified: { type: Boolean, default: false }
})

defineEmits(['update:verified'])

function toggleVerify() {
  if (props.verifiable) {
    // emit is not directly supported, use modelValue pattern
  }
}
</script>

<style lang="scss" scoped>
.info-card {
  background: $color-bg-card;
  border-radius: $radius-lg;
  padding: $spacing-base;
  margin-bottom: $spacing-base;
  box-shadow: $shadow-card;
}

.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: $spacing-sm; }
.card-title { font-size: $font-size-base; font-weight: 600; color: $color-text-primary; }

.verify-checkbox {
  width: 36rpx; height: 36rpx; border-radius: 50%;
  border: 2rpx solid $color-border;
  display: flex; align-items: center; justify-content: center;
  &.checked { background: $color-primary; border-color: $color-primary; }
}

.card-body { background: $color-bg-page; border-radius: $radius-sm; padding: $spacing-sm $spacing-base; }
.field-row { display: flex; justify-content: space-between; padding: 8rpx 0; }
.field-label { font-size: $font-size-sm; color: $color-text-secondary; }
.field-value { font-size: $font-size-sm; color: $color-text-primary; }
.field-value.danger { color: $color-danger; }
.field-value.warning { color: $color-warning; }
</style>
