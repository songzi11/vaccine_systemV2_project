<template>
  <view class="user-info-card">
    <view class="avatar">{{ avatarText }}</view>
    <view class="user-detail">
      <text class="user-name">{{ userInfo.realName || '未设置' }}</text>
      <text class="user-phone">{{ maskedPhone }}</text>
      <text class="user-status">账号状态正常</text>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { maskPhone } from '@/utils/format.js'

const props = defineProps({ userInfo: { type: Object, required: true } })
const avatarText = computed(() => (props.userInfo.realName || '?').charAt(0))
const maskedPhone = computed(() => maskPhone(props.userInfo.phone))
</script>

<style lang="scss" scoped>
.user-info-card {
  background: $color-bg-card;
  border-radius: $radius-lg;
  padding: $spacing-xl $spacing-lg;
  display: flex;
  align-items: center;
  margin-bottom: $spacing-lg;
  box-shadow: $shadow-card;
}
.avatar {
  width: 120rpx; height: 120rpx; border-radius: 50%;
  background: $color-primary;
  color: #fff; font-size: 48rpx;
  display: flex; align-items: center; justify-content: center;
  margin-right: $spacing-base; flex-shrink: 0;
}
.user-detail { display: flex; flex-direction: column; gap: 4rpx; }
.user-name { font-size: $font-size-lg; font-weight: 600; color: $color-text-primary; }
.user-phone { font-size: $font-size-sm; color: $color-text-secondary; }
.user-status { font-size: $font-size-xs; color: $color-success; }
</style>
