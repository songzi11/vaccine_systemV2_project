<template>
  <view class="vaccine-list-page">
    <!-- 搜索框 -->
    <view class="search-bar">
      <uni-search-bar v-model="keyword" placeholder="搜索疫苗名称" @confirm="onSearch" @clear="onSearch" cancelButton="none" />
    </view>

    <!-- 分类 Tab -->
    <view class="filter-tabs">
      <view
        v-for="tab in categoryTabs"
        :key="tab.value"
        class="filter-tab"
        :class="{ active: activeCategory === tab.value }"
        @tap="switchCategory(tab.value)"
      >
        {{ tab.label }}
      </view>
    </view>

    <!-- 疫苗列表 -->
    <scroll-view scroll-y class="list-scroll" @scrolltolower="loadMore" refresher-enabled :refresher-triggered="refreshing" @refresherrefresh="onRefresh">
      <view class="list-content">
        <VaccineCard
          v-for="item in vaccines"
          :key="item.id"
          :vaccine="item"
          @click="goToDetail(item)"
        />
        <EmptyState v-if="!loading && vaccines.length === 0" icon="search" title="未找到疫苗" />
        <view v-if="loading" class="loading-tip"><text>加载中...</text></view>
        <view v-else-if="!hasMore && vaccines.length > 0" class="loading-tip"><text>没有更多了</text></view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getVaccineList } from '@/api/vaccine.js'
import VaccineCard from '@/components/VaccineCard/VaccineCard.vue'
import EmptyState from '@/components/EmptyState/EmptyState.vue'

const keyword = ref('')
const activeCategory = ref('')
const vaccines = ref([])
const loading = ref(false)
const refreshing = ref(false)
const hasMore = ref(true)
const page = ref(1)

const categoryTabs = [
  { label: '全部', value: '' },
  { label: '一类疫苗', value: 'CLASS_I' },
  { label: '二类疫苗', value: 'CLASS_II' }
]

onShow(() => {
  refresh()
})

function switchCategory(value) {
  activeCategory.value = value
  refresh()
}

function onSearch() {
  refresh()
}

async function refresh() {
  refreshing.value = true
  page.value = 1
  hasMore.value = true
  try {
    const data = await getVaccineList({
      keyword: keyword.value,
      category: activeCategory.value,
      page: 1,
      size: 20
    })
    vaccines.value = data.records || data || []
    hasMore.value = (data.records || []).length >= 20
  } catch (e) {
    console.error('加载疫苗列表失败', e)
  } finally {
    refreshing.value = false
  }
}

function onRefresh() { refresh() }

async function loadMore() {
  if (!hasMore.value || loading.value) return
  loading.value = true
  page.value++
  try {
    const data = await getVaccineList({
      keyword: keyword.value,
      category: activeCategory.value,
      page: page.value,
      size: 20
    })
    const newRecords = data.records || []
    vaccines.value = [...vaccines.value, ...newRecords]
    hasMore.value = newRecords.length >= 20
  } catch (e) {
    console.error('加载更多失败', e)
  } finally {
    loading.value = false
  }
}

function goToDetail(item) {
  uni.navigateTo({ url: `/pages/vaccine/detail?id=${item.id}` })
}
</script>

<style lang="scss" scoped>
.vaccine-list-page {
  min-height: 100vh;
  background-color: $color-bg-page;
  display: flex;
  flex-direction: column;
}

.search-bar {
  background-color: $color-bg-white;
  padding: $spacing-xs 0;
}

.filter-tabs {
  display: flex;
  gap: $spacing-sm;
  padding: $spacing-sm $spacing-md;
  background-color: $color-bg-white;
  border-bottom: 1rpx solid $color-border-light;
}

.filter-tab {
  padding: $spacing-xs $spacing-md;
  font-size: $font-size-sm;
  color: $color-text-secondary;
  background-color: $color-bg-grey;
  border-radius: $radius-lg;

  &.active { color: #FFFFFF; background-color: $color-primary; }
}

.list-scroll {
  flex: 1;
  height: calc(100vh - 180rpx);
}

.list-content {
  padding: $spacing-md;
}

.loading-tip {
  text-align: center;
  padding: $spacing-lg;

  text { font-size: $font-size-sm; color: $color-text-placeholder; }
}
</style>
