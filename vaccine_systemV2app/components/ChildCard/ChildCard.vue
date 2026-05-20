<template>
  <view class="child-card" :class="{ selectable, selected }" @tap="$emit('click', child)">
    <view class="avatar" :style="{ backgroundColor: child.gender === '男' ? '#1989FA' : '#EE0A24' }">
      <text class="avatar-text">{{ (child.name || '?').charAt(0) }}</text>
    </view>
    <view class="child-info">
      <text class="child-name">{{ child.name }}</text>
      <text class="child-detail">{{ genderText }} · {{ ageText }}</text>
      <text class="child-id">{{ maskedId }}</text>
    </view>
    <view v-if="selectable" class="radio-wrap">
      <uni-icons :type="selected ? 'radio-dot' : 'circle'" :size="22" :color="selected ? '#07C160' : '#C0C0C0'" />
    </view>
    <uni-icons v-else type="right" :size="16" color="#C0C0C0" />
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { maskIdCard } from '@/utils/format.js'

const props = defineProps({
  child: { type: Object, required: true },
  selectable: { type: Boolean, default: false },
  selected: { type: Boolean, default: false },
  showDelete: { type: Boolean, default: false }
})
defineEmits(['click', 'delete'])

const genderText = computed(() => {
  const g = props.child.gender
  return g === '男' || g === '女' ? g : '未知'
})
const maskedId = computed(() => maskIdCard(props.child.idCardNo))
const ageText = computed(() => {
  if (!props.child.birthDate) return ''
  const birth = new Date(props.child.birthDate)
  const now = new Date()
  const months = (now.getFullYear() - birth.getFullYear()) * 12 + (now.getMonth() - birth.getMonth())
  const years = Math.floor(months / 12)
  const remainMonths = months % 12
  return years > 0 ? `${years}岁${remainMonths > 0 ? remainMonths + '个月' : ''}` : `${remainMonths}个月`
})
</script>

<style lang="scss" scoped>
.child-card {
  display: flex;
  align-items: center;
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
.avatar {
  width: 80rpx; height: 80rpx; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  margin-right: $spacing-base;
  flex-shrink: 0;
}
.avatar-text { color: #fff; font-size: $font-size-lg; font-weight: 600; }
.child-info { flex: 1; display: flex; flex-direction: column; gap: 4rpx; }
.child-name { font-size: $font-size-base; font-weight: 600; color: $color-text-primary; }
.child-detail { font-size: $font-size-sm; color: $color-text-secondary; }
.child-id { font-size: $font-size-xs; color: $color-text-placeholder; }
.radio-wrap { flex-shrink: 0; }
</style>
