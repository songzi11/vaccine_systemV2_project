<template>
  <view class="page">
    <uni-nav-bar title="批次管理" :border="false" />

    <view class="filter-bar">
      <view class="filter-top">
        <uni-easyinput v-model="keyword" placeholder="搜索批次号" @confirm="refresh" />
        <button class="btn-add" @click="goCreate">新建批次</button>
      </view>
      <view class="filter-tabs">
        <view v-for="tab in filterTabs" :key="tab.value" class="filter-tab" :class="{ active: currentFilter === tab.value }" @click="currentFilter = tab.value; refresh()">
          <text>{{ tab.label }}</text>
        </view>
      </view>
    </view>

    <scroll-view scroll-y class="list-scroll" @scrolltolower="loadMore" refresher-enabled :refresher-triggered="refreshing" @refresherrefresh="onRefresh">
      <view class="list-content">
        <view v-for="item in list" :key="item.id" class="batch-card" @click="goDetail(item)">
          <view class="card-header">
            <text class="batch-no">{{ item.batchNo }}</text>
            <view class="status-badge" :style="{ backgroundColor: statusBg(item) }">
              <text class="status-text" :style="{ color: statusColor(item) }">{{ item.status || '正常' }}</text>
            </view>
          </view>
          <view class="card-body">
            <text class="vaccine-name">{{ item.vaccineName }}</text>
            <text class="manufacturer">{{ item.manufacturer }}</text>
          </view>
          <view class="card-footer">
            <text class="date-text" :class="{ expired: isExpired(item.expiryDate) }">有效期: {{ item.expiryDate }}</text>
            <text class="stock-text">总数: {{ item.totalStock || 0 }}  可用数: {{ item.availableStock || 0 }}  锁定数: {{ item.lockedStock || 0 }}</text>
          </view>
        </view>

        <EmptyState v-if="!loading && list.length === 0" icon="💊" title="暂无批次数据" />
        <view v-if="loading" class="loading-tip"><text>加载中...</text></view>
        <view v-else-if="!hasMore && list.length > 0" class="loading-tip"><text>没有更多了</text></view>
      </view>
    </scroll-view>

    <CustomTabBar />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getBatches } from '@/api/stock.js'
import EmptyState from '@/components/EmptyState/EmptyState.vue'
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue'

const keyword = ref('')
const currentFilter = ref('ALL')
const vaccineId = ref('')
const vaccineName = ref('')
const list = ref([])
const loading = ref(false)
const refreshing = ref(false)
const hasMore = ref(true)
const page = ref(1)

const filterTabs = [
  { label: '全部', value: 'ALL' },
  { label: '正常', value: 'NORMAL' },
  { label: '即将过期', value: 'NEAR_EXPIRY' },
  { label: '已过期', value: 'EXPIRED' },
  { label: '已销毁', value: 'DISPOSED' }
]

function isExpired(dateStr) {
  if (!dateStr) return false
  return new Date(dateStr) < new Date()
}

function statusBg(item) {
  if (item.status === '过期' || item.status === '已销毁') return 'rgba(238,10,36,0.1)'
  if (item.status === '临期') return 'rgba(255,153,0,0.1)'
  return 'rgba(7,193,96,0.1)'
}

function statusColor(item) {
  if (item.status === '过期' || item.status === '已销毁') return '#EE0A24'
  if (item.status === '临期') return '#FF9900'
  return '#07C160'
}

onLoad((query) => {
  vaccineId.value = query.vaccineId || ''
  vaccineName.value = query.vaccineName ? decodeURIComponent(query.vaccineName) : ''
  refresh()
})

async function refresh() {
  refreshing.value = true
  page.value = 1
  hasMore.value = true
  try {
    const params = { page: 1, size: 20 }
    if (vaccineId.value) params.vaccineId = vaccineId.value
    if (currentFilter.value !== 'ALL') params.status = currentFilter.value
    if (keyword.value) params.keyword = keyword.value
    const data = await getBatches(params)
    list.value = data.records || (Array.isArray(data) ? data : [])
    hasMore.value = list.value.length >= 20
  } catch (e) {
    console.error('加载批次列表失败', e)
  } finally {
    refreshing.value = false
  }
}

async function loadMore() {
  if (!hasMore.value || loading.value) return
  loading.value = true
  page.value++
  try {
    const params = { page: page.value, size: 20 }
    if (vaccineId.value) params.vaccineId = vaccineId.value
    if (currentFilter.value !== 'ALL') params.status = currentFilter.value
    if (keyword.value) params.keyword = keyword.value
    const data = await getBatches(params)
    const newItems = data.records || (Array.isArray(data) ? data : [])
    list.value = [...list.value, ...newItems]
    hasMore.value = newItems.length >= 20
  } catch (e) {
    console.error('加载更多失败', e)
  } finally {
    loading.value = false
  }
}

function onRefresh() { refresh() }

function goDetail(item) {
  uni.navigateTo({ url: `/pages/stock/batch-detail?batchId=${item.id}` })
}

function goCreate() {
  uni.navigateTo({ url: '/pages/stock/create-batch' })
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: $color-bg-page; padding-bottom: 140rpx; }
.filter-bar { padding: $spacing-md; background: $color-bg-white; border-bottom: 1rpx solid $color-border-light; }
.filter-top { display: flex; gap: $spacing-sm; align-items: center; }
.filter-top .uni-easyinput { flex: 1; }
.btn-add {
  flex-shrink: 0; padding: 0 $spacing-md; height: 70rpx; line-height: 70rpx;
  background: $color-primary; color: #FFF; font-size: $font-size-sm;
  border: none; border-radius: $radius-lg;
  &::after { border: none; }
}
.filter-tabs { display: flex; gap: $spacing-xs; margin-top: $spacing-sm; flex-wrap: wrap; }
.filter-tab {
  padding: 6rpx $spacing-sm; border-radius: $radius-round;
  font-size: $font-size-xs; background: $color-bg-grey; color: $color-text-secondary;
  &.active { background: $color-primary-light; color: $color-primary; }
}

.list-scroll { height: calc(100vh - 88px - 160rpx); }
.list-content { padding: $spacing-md; }

.batch-card {
  background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md;
  margin-bottom: $spacing-sm; box-shadow: $shadow-card;
}
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: $spacing-sm; }
.batch-no { font-size: $font-size-base; font-weight: 600; color: $color-text-primary; }
.status-badge { padding: 4rpx 12rpx; border-radius: $radius-sm; }
.status-text { font-size: $font-size-xs; }

.card-body { margin-bottom: $spacing-sm; }
.vaccine-name { font-size: $font-size-base; color: $color-text-primary; display: block; }
.manufacturer { font-size: $font-size-sm; color: $color-text-secondary; display: block; margin-top: 4rpx; }

.card-footer { display: flex; justify-content: space-between; border-top: 1rpx solid $color-border-light; padding-top: $spacing-sm; }
.date-text { font-size: $font-size-xs; color: $color-text-secondary; &.expired { color: $color-danger; } }
.stock-text { font-size: $font-size-xs; color: $color-text-placeholder; }

.loading-tip { text-align: center; padding: $spacing-lg; text { font-size: $font-size-sm; color: $color-text-placeholder; } }
</style>
