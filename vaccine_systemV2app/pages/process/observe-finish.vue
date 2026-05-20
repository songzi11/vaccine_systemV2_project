<template>
  <view class="page">
    <view class="nav-bar">
      <uni-icons type="back" :size="20" @click="uni.navigateBack()" />
      <text class="nav-title">结束留观</text>
    </view>

    <!-- 留观结果 -->
    <view class="section">
      <text class="section-title">留观结果 *</text>
      <view class="result-options">
        <view class="result-btn normal" :class="{ active: result === 'NORMAL' }" @click="result = 'NORMAL'">正常</view>
        <view class="result-btn abnormal" :class="{ active: result === 'ABNORMAL' }" @click="result = 'ABNORMAL'">异常</view>
      </view>
      <view v-if="result === 'ABNORMAL'" class="abnormal-tip">
        <text class="tip-text">异常留观需先上报不良反应，否则无法结束留观</text>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="bottom-bar">
      <button class="btn-cancel" @click="uni.navigateBack()">取消</button>
      <button class="btn-confirm" :loading="submitting" @click="handleSubmit">确认结束</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { finishObserve } from '@/api/observe.js'
import { OBSERVE_MIN_DURATION } from '@/utils/constants.js'

const appointmentId = ref('')
const hasAdverse = ref(false)
const result = ref('NORMAL')
const submitting = ref(false)

onLoad((query) => {
  appointmentId.value = query.appointmentId || ''
  hasAdverse.value = query.hasAdverse === 'true'
})

async function handleSubmit() {
  if (result.value === 'ABNORMAL' && !hasAdverse.value) {
    uni.showToast({ title: '请先上报不良反应', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    await finishObserve(appointmentId.value, { durationMinutes: OBSERVE_MIN_DURATION })
    uni.showToast({ title: '留观已结束', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) {
    uni.showToast({ title: e.message || '操作失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; padding: $spacing-lg; background: $color-bg-page; padding-bottom: 140rpx; }
.nav-bar { display: flex; align-items: center; gap: $spacing-sm; margin-bottom: $spacing-lg; }
.nav-title { font-size: $font-size-lg; font-weight: 600; }

.section { background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md; margin-bottom: $spacing-lg; }
.section-title { font-size: $font-size-base; font-weight: 600; margin-bottom: $spacing-md; display: block; }

.result-options { display: flex; gap: $spacing-md; }
.result-btn {
  flex: 1; text-align: center; padding: 24rpx; border-radius: $radius-lg;
  border: 1rpx solid $color-border; font-size: $font-size-base;
  &.normal.active { border-color: $color-success; background: rgba(7,193,96,0.1); color: $color-success; }
  &.abnormal.active { border-color: $color-danger; background: rgba(238,10,36,0.1); color: $color-danger; }
}

.abnormal-tip {
  margin-top: $spacing-md; padding: $spacing-sm $spacing-md;
  background: rgba(255, 153, 0, 0.08); border-radius: $radius-lg;
}
.tip-text { font-size: $font-size-sm; color: $color-warning; }

.bottom-bar {
  position: fixed; bottom: 0; left: 0; right: 0;
  display: flex; gap: $spacing-md;
  padding: $spacing-md $spacing-lg;
  padding-bottom: calc(#{$spacing-md} + env(safe-area-inset-bottom));
  background: #FFFFFF;
}
.btn-cancel { flex: 1; background: transparent; border: 1rpx solid $color-border; color: $color-text-secondary; border-radius: $radius-lg; height: 88rpx; line-height: 88rpx; }
.btn-confirm { flex: 2; background: $color-primary; color: #FFF; border: none; border-radius: $radius-lg; height: 88rpx; line-height: 88rpx; }
</style>
