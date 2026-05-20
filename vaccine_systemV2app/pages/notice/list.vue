<template>
  <view class="notice-list-page">
    <scroll-view scroll-y class="list-scroll" @scrolltolower="loadMore" refresher-enabled :refresher-triggered="refreshing" @refresherrefresh="refresh">
      <view class="list-content">
        <NoticeCard
          v-for="item in notices"
          :key="item.id"
          :notice="item"
          @click="goToDetail(item)"
        />
        <EmptyState v-if="!loading && notices.length === 0" icon="info" title="暂无公告" />
        <view v-if="loading" class="loading-tip"><text>加载中...</text></view>
        <view v-else-if="!hasMore && notices.length > 0" class="loading-tip"><text>没有更多了</text></view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getNoticeList } from '@/api/notice.js'
import NoticeCard from '@/components/NoticeCard/NoticeCard.vue'
import EmptyState from '@/components/EmptyState/EmptyState.vue'

const notices = ref([])
const loading = ref(false)
const refreshing = ref(false)
const hasMore = ref(true)
const page = ref(1)

onShow(() => {
  refresh()
})

async function refresh() {
  refreshing.value = true
  page.value = 1
  hasMore.value = true
  try {
    const data = await getNoticeList()
    notices.value = Array.isArray(data) ? data : (data.records || [])
    hasMore.value = false
  } catch (e) {
    console.error('加载公告列表失败', e)
  } finally {
    refreshing.value = false
  }
}

async function loadMore() {
  if (!hasMore.value || loading.value) return
  loading.value = true
  page.value++
  try {
    const data = await getNoticeList({ page: page.value, size: 20 })
    const newRecords = data.records || []
    notices.value = [...notices.value, ...newRecords]
    hasMore.value = newRecords.length >= 20
  } catch (e) {
    console.error('加载更多失败', e)
  } finally {
    loading.value = false
  }
}

function goToDetail(item) {
  uni.navigateTo({ url: `/pages/notice/detail?id=${item.id}` })
}
</script>

<style lang="scss" scoped>
.notice-list-page {
  min-height: 100vh;
  background-color: $color-bg-page;
}

.list-scroll {
  height: calc(100vh - 44px);
}

.list-content {
  padding: 0 $spacing-md;
}

.loading-tip {
  text-align: center;
  padding: $spacing-lg;

  text { font-size: $font-size-sm; color: $color-text-placeholder; }
}
</style>
