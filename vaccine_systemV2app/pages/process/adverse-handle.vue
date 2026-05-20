<template>
  <view class="page">
    <view class="nav-bar">
      <uni-icons type="back" :size="20" @click="uni.navigateBack()" />
      <text class="nav-title">不良反应处理</text>
    </view>

    <!-- 上报信息（只读） -->
    <view class="info-card">
      <text class="card-title">上报信息</text>
      <view class="info-row"><text class="info-label">反应类型</text><text class="info-value">{{ reactionInfo.reactionType }}</text></view>
      <view class="info-row"><text class="info-label">严重程度</text><text class="info-value" :class="severityClass">{{ reactionInfo.severity }}</text></view>
      <view class="info-row"><text class="info-label">发生时间</text><text class="info-value">{{ reactionInfo.occurTime }}</text></view>
      <view class="info-row"><text class="info-label">描述</text><text class="info-value">{{ reactionInfo.description }}</text></view>
    </view>

    <!-- 处理记录 -->
    <view class="section">
      <text class="section-title">处理记录 *</text>
      <uni-easyinput v-model="handleResult" type="textarea" placeholder="请输入处理结果" :maxlength="500" />
    </view>

    <!-- 操作 -->
    <view class="bottom-bar">
      <button class="btn-cancel" @click="uni.navigateBack()">取消</button>
      <button class="btn-confirm" :loading="submitting" @click="handleSubmit">确认处理</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { handleAdverse } from '@/api/observe.js'

const reactionId = ref('')
const handleResult = ref('')
const submitting = ref(false)
const reactionInfo = ref({
  reactionType: '',
  severity: '',
  occurTime: '',
  description: ''
})

const severityClass = computed(() => {
  const map = { '轻度': '', '中度': 'warning', '重度': 'danger' }
  return map[reactionInfo.value.severity] || ''
})

onLoad((query) => {
  reactionId.value = query.reactionId || ''
  if (query.info) {
    try {
      reactionInfo.value = JSON.parse(decodeURIComponent(query.info))
    } catch (e) {
      console.error('解析不良反应信息失败', e)
    }
  }
})

async function handleSubmit() {
  if (!handleResult.value.trim()) {
    uni.showToast({ title: '请输入处理结果', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    await handleAdverse(reactionId.value, { handleResult: handleResult.value })
    uni.showToast({ title: '处理成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) {
    uni.showToast({ title: e.message || '处理失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; padding: $spacing-lg; background: $color-bg-page; padding-bottom: 140rpx; }
.nav-bar { display: flex; align-items: center; gap: $spacing-sm; margin-bottom: $spacing-lg; }
.nav-title { font-size: $font-size-lg; font-weight: 600; }

.info-card {
  background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md;
  margin-bottom: $spacing-md; box-shadow: $shadow-card;
}
.card-title { font-size: $font-size-base; font-weight: 600; display: block; margin-bottom: $spacing-sm; }
.info-row { display: flex; justify-content: space-between; padding: $spacing-sm 0; border-bottom: 1rpx solid $color-border-light; &:last-child { border-bottom: none; } }
.info-label { font-size: $font-size-sm; color: $color-text-secondary; flex-shrink: 0; margin-right: $spacing-md; }
.info-value { font-size: $font-size-base; color: $color-text-primary; text-align: right; flex: 1; }
.info-value.warning { color: $color-warning; }
.info-value.danger { color: $color-danger; }

.section { background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md; margin-bottom: $spacing-md; }
.section-title { font-size: $font-size-base; font-weight: 600; margin-bottom: $spacing-md; display: block; }

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
