<template>
  <view class="page-404">
    <uni-icons type="info" size="120" color="#C0C0C0" />
    <text class="title">页面不存在</text>
    <text class="desc">您访问的页面不存在或已被移除</text>
    <view class="action" @tap="goHome">
      <text class="action-text">返回首页</text>
    </view>
  </view>
</template>

<script setup>
function goHome() {
  uni.reLaunch({ url: '/pages/index/index' })
}
</script>

<style lang="scss" scoped>
.page-404 {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 60rpx;
}

.title {
  font-size: $font-size-xl;
  color: $color-text-regular;
  margin-top: $spacing-xl;
}

.desc {
  font-size: $font-size-sm;
  color: $color-text-secondary;
  margin-top: $spacing-sm;
}

.action {
  margin-top: $spacing-xl;
  padding: $spacing-sm $spacing-xl;
  background-color: $color-primary-light;
  border-radius: $radius-round;
}

.action-text {
  font-size: $font-size-base;
  color: $color-primary;
}
</style>
