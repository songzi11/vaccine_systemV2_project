<template>
  <view class="vaccine-card" :class="{ selectable, selected }" @tap="$emit('click', vaccine)">
    <view class="card-header">
      <text class="vaccine-name">{{ vaccine.vaccineName || vaccine.name }}</text>
      <view class="category-tag" :class="vaccine.category === 'CLASS_I' ? 'class-i' : 'class-ii'">
        <text>{{ vaccine.category === 'CLASS_I' ? '一类' : '二类' }}</text>
      </view>
    </view>
    <view class="card-body">
      <text class="info-text">厂家：{{ vaccine.manufacturer }}</text>
    </view>
    <view v-if="selectable" class="radio-wrap">
      <uni-icons :type="selected ? 'radio-dot' : 'circle'" :size="22" :color="selected ? '#07C160' : '#C0C0C0'" />
    </view>
    <uni-icons v-else type="right" :size="16" color="#C0C0C0" />
  </view>
</template>

<script setup>
defineProps({
  vaccine: { type: Object, required: true },
  selectable: { type: Boolean, default: false },
  selected: { type: Boolean, default: false }
})
defineEmits(['click'])
</script>

<style lang="scss" scoped>
.vaccine-card {
  position: relative;
  background: $color-bg-card;
  border: 2rpx solid transparent;
  border-radius: $radius-lg;
  padding: $spacing-base;
  margin-bottom: $spacing-base;
  box-shadow: $shadow-card;
  transition: border-color 0.2s, background-color 0.2s;

  &.selectable {
    cursor: pointer;
  }

  &.selected {
    border-color: $color-primary;
    background: $color-primary-light;
  }
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-sm;
}
.vaccine-name { font-size: $font-size-base; font-weight: 600; color: $color-text-primary; flex: 1; }
.category-tag {
  padding: 4rpx 16rpx;
  border-radius: $radius-sm;
  font-size: $font-size-xs;
}
.class-i { background: $color-success-light; color: $color-success; }
.class-ii { background: $color-info-light; color: $color-info; }
.card-body { display: flex; flex-direction: column; gap: 4rpx; }
.info-text { font-size: $font-size-sm; color: $color-text-secondary; }
.radio-wrap { flex-shrink: 0; }
</style>
