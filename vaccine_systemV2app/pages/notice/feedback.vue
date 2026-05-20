<template>
  <view class="feedback-page">
    <uni-nav-bar title="公告反馈" :border="false" />

    <view class="form-content">
      <!-- 公告标题 -->
      <view class="notice-info">
        <text class="notice-title">{{ noticeTitle }}</text>
      </view>

      <!-- 反馈内容 -->
      <view class="feedback-section">
        <textarea
          v-model="content"
          class="feedback-textarea"
          placeholder="请输入您的疑问或反馈（10-500字）"
          :maxlength="500"
          @input="onInput"
        />
        <text class="char-count">{{ content.length }}/500</text>
      </view>

      <!-- 提交按钮 -->
      <button class="submit-btn" :loading="submitting" :disabled="content.length < 10" @tap="handleSubmit">提交反馈</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getNoticeList, submitFeedback } from '@/api/notice.js'

const noticeId = ref(null)
const noticeTitle = ref('')
const content = ref('')
const submitting = ref(false)

onShow(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.$page?.options || currentPage.options || {}
  noticeId.value = options.id
  if (noticeId.value) {
    loadNoticeTitle()
  }
})

async function loadNoticeTitle() {
  try {
    const data = await getNoticeList({ id: noticeId.value })
    const records = data.records || data || []
    const notice = records.find(n => n.id == noticeId.value) || records[0] || null
    noticeTitle.value = notice?.title || ''
  } catch {
    // ignore
  }
}

function onInput() {
  // 字数统计由 v-model + content.length 自动处理
}

async function handleSubmit() {
  if (content.value.length < 10) {
    uni.showToast({ title: '反馈内容至少10个字', icon: 'none' })
    return
  }

  submitting.value = true
  try {
    await submitFeedback(noticeId.value, { content: content.value })
    uni.showToast({ title: '提交成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch {
    // 错误已由 interceptor 处理
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.feedback-page {
  min-height: 100vh;
  background-color: $color-bg-page;
}

.form-content {
  padding: $spacing-md;
}

.notice-info {
  background-color: $color-bg-white;
  border-radius: $radius-lg;
  padding: $spacing-lg;
  margin-bottom: $spacing-md;
}

.notice-title {
  font-size: $font-size-base;
  font-weight: 600;
  color: $color-text-primary;
}

.feedback-section {
  background-color: $color-bg-white;
  border-radius: $radius-lg;
  padding: $spacing-lg;
  margin-bottom: $spacing-lg;
}

.feedback-textarea {
  width: 100%;
  min-height: 300rpx;
  font-size: $font-size-base;
  color: $color-text-primary;
  line-height: 1.6;
}

.char-count {
  display: block;
  text-align: right;
  font-size: $font-size-xs;
  color: $color-text-placeholder;
  margin-top: $spacing-sm;
}

.submit-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background-color: $color-primary;
  color: #FFFFFF;
  font-size: $font-size-lg;
  border: none;
  border-radius: $radius-lg;

  &:disabled { opacity: 0.5; }
  &::after { border: none; }
}
</style>
