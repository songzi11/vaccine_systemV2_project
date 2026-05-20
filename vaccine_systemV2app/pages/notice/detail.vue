<template>
  <view class="notice-detail-page">
    <uni-nav-bar title="公告详情" :border="false" />

    <view v-if="notice" class="detail-content">
      <text class="notice-title">{{ notice.title }}</text>
      <text class="notice-date">{{ notice.createdAt }}</text>
      <view class="notice-type-tag" :class="typeClass">
        <text>{{ typeText }}</text>
      </view>

      <view class="notice-body">
        <text class="notice-content">{{ notice.content }}</text>
      </view>

      <!-- 底部操作 -->
      <view class="bottom-bar">
        <button class="btn-feedback" @tap="goToFeedback">我有疑问</button>
      </view>
    </view>

    <view v-else class="loading-tip">
      <text>加载中...</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getNoticeList } from '@/api/notice.js'

const notice = ref(null)
const noticeId = ref(null)

const typeClass = computed(() => {
  const map = { NORMAL: 'type-normal', URGENT: 'type-urgent', SYSTEM: 'type-system' }
  return map[notice.value?.type] || 'type-normal'
})

const typeText = computed(() => {
  const map = { NORMAL: '公告', URGENT: '紧急', SYSTEM: '系统' }
  return map[notice.value?.type] || '公告'
})

onShow(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.$page?.options || currentPage.options || {}
  noticeId.value = options.id
  if (noticeId.value) {
    loadDetail()
  }
})

async function loadDetail() {
  try {
    const data = await getNoticeList({ id: noticeId.value })
    const records = data.records || data || []
    notice.value = records.find(n => n.id == noticeId.value) || records[0] || null
  } catch (e) {
    console.error('加载公告详情失败', e)
  }
}

function goToFeedback() {
  uni.navigateTo({ url: `/pages/notice/feedback?id=${noticeId.value}` })
}
</script>

<style lang="scss" scoped>
.notice-detail-page {
  min-height: 100vh;
  background-color: $color-bg-page;
}

.detail-content {
  padding: $spacing-md;
}

.notice-title {
  display: block;
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: $color-text-primary;
  margin-bottom: $spacing-sm;
}

.notice-date {
  display: block;
  font-size: $font-size-sm;
  color: $color-text-placeholder;
  margin-bottom: $spacing-sm;
}

.notice-type-tag {
  display: inline-block;
  padding: 4rpx 16rpx;
  border-radius: $radius-sm;
  font-size: $font-size-xs;
  margin-bottom: $spacing-lg;
}
.type-normal { background: $color-info-light; color: $color-info; }
.type-urgent { background: $color-danger-light; color: $color-danger; }
.type-system { background: $color-success-light; color: $color-success; }

.notice-body {
  background-color: $color-bg-white;
  border-radius: $radius-lg;
  padding: $spacing-lg;
}

.notice-content {
  font-size: $font-size-base;
  color: $color-text-regular;
  line-height: 1.8;
}

.bottom-bar {
  padding: $spacing-xl 0;
  padding-bottom: calc(#{$spacing-xl} + env(safe-area-inset-bottom));
}

.btn-feedback {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background-color: $color-primary;
  color: #FFFFFF;
  font-size: $font-size-lg;
  border: none;
  border-radius: $radius-lg;

  &::after { border: none; }
}

.loading-tip {
  text-align: center;
  padding: 120rpx;

  text { font-size: $font-size-sm; color: $color-text-placeholder; }
}
</style>
