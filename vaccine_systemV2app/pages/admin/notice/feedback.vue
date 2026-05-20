<template>
  <view class="feedback-page">
    <view class="nav-bar">
      <uni-icons type="back" :size="20" @click="uni.navigateBack()" />
      <text class="nav-title">{{ noticeTitle }}</text>
    </view>

    <view class="feedback-list">
      <view v-for="item in feedbackList" :key="item.id" class="feedback-card">
        <view class="feedback-header">
          <text class="feedback-phone">{{ maskPhone(item.userPhone) }}</text>
          <text class="feedback-time">{{ item.feedbackTime }}</text>
        </view>
        <text class="feedback-content">{{ item.content }}</text>
      </view>
      <EmptyState v-if="!loading && feedbackList.length === 0" icon="chat" title="暂无反馈" />
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getNoticeFeedback } from '@/api/notice-manage.js'
import EmptyState from '@/components/EmptyState/EmptyState.vue'

const noticeId = ref('')
const noticeTitle = ref('公告反馈')
const feedbackList = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)

function maskPhone(phone) {
  if (!phone || phone.length !== 11) return phone || '未知'
  return phone.slice(0, 3) + '****' + phone.slice(7)
}

async function loadMore(reset = false) {
  if (reset) { page.value = 1; feedbackList.value = [] }
  if (loading.value) return
  if (feedbackList.value.length >= total.value && !reset) return
  loading.value = true
  try {
    const data = await getNoticeFeedback(noticeId.value, { page: page.value, size: 20 })
    feedbackList.value = reset ? (data.records || []) : [...feedbackList.value, ...(data.records || [])]
    total.value = data.total || 0
    page.value++
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

onLoad((query) => {
  if (query.id) noticeId.value = query.id
  if (query.title) noticeTitle.value = decodeURIComponent(query.title)
  loadMore(true)
})

onReachBottom(() => { loadMore() })
</script>

<style lang="scss" scoped>
.feedback-page { min-height: 100vh; padding: $spacing-lg; background: $color-bg-page; }
.nav-bar { display: flex; align-items: center; gap: $spacing-sm; margin-bottom: $spacing-lg; }
.nav-title { font-size: $font-size-lg; font-weight: 600; }
.feedback-list { display: flex; flex-direction: column; }
.feedback-card { background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md; margin-bottom: $spacing-md; box-shadow: $shadow-card; }
.feedback-header { display: flex; justify-content: space-between; margin-bottom: $spacing-sm; }
.feedback-phone { font-size: $font-size-sm; color: $color-text-secondary; }
.feedback-time { font-size: $font-size-xs; color: $color-text-placeholder; }
.feedback-content { font-size: $font-size-sm; color: $color-text-primary; line-height: 1.6; }
</style>
