<template>
  <view class="page">
    <uni-nav-bar title="库存预警" :border="false" />

    <view class="filter-tabs">
      <view v-for="tab in filterTabs" :key="tab.value" class="filter-tab" :class="{ active: currentFilter === tab.value }" @click="currentFilter = tab.value; refresh()">
        <text>{{ tab.label }}</text>
      </view>
    </view>

    <scroll-view scroll-y class="list-scroll" @scrolltolower="loadMore" refresher-enabled :refresher-triggered="refreshing" @refresherrefresh="onRefresh">
      <view class="list-content">
        <view v-for="item in list" :key="item.id" class="alert-item">
          <view class="item-header">
            <text class="batch-no">{{ item.batchNo }}</text>
            <view class="status-badge" :style="{ backgroundColor: item.handled ? 'rgba(7,193,96,0.1)' : 'rgba(255,153,0,0.1)' }">
              <text class="status-text" :style="{ color: item.handled ? '#07C160' : '#FF9900' }">{{ item.handled ? '已处理' : '未处理' }}</text>
            </view>
          </view>
          <view class="item-body">
            <text class="vaccine-name">{{ item.vaccineName }}</text>
            <text class="alert-type">{{ item.alertType }}</text>
            <text class="alert-detail">{{ item.detail }}</text>
          </view>
          <view v-if="!item.handled" class="item-footer">
            <button class="btn-handle" @click="handleAlert(item)">标记已处理</button>
          </view>
        </view>

        <EmptyState v-if="!loading && list.length === 0" icon="document" title="暂无预警" />
        <view v-if="loading" class="loading-tip"><text>加载中...</text></view>
        <view v-else-if="!hasMore && list.length > 0" class="loading-tip"><text>没有更多了</text></view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getAlerts, handleAlert as handleAlertApi } from '@/api/stock.js'
import EmptyState from '@/components/EmptyState/EmptyState.vue'

const currentFilter = ref('ALL')
const list = ref([])
const loading = ref(false)
const refreshing = ref(false)
const hasMore = ref(true)
const page = ref(1)

const filterTabs = [
  { label: '全部', value: 'ALL' },
  { label: '低库存', value: 'LOW_STOCK' },
  { label: '即将过期', value: 'EXPIRY_SOON' },
  { label: '已过期', value: 'EXPIRED' }
]

let allAlerts = []

onShow(() => refresh())

async function refresh() {
  refreshing.value = true
  try {
    const data = await getAlerts()
    allAlerts = Array.isArray(data) ? data : (data.records || [])
    applyFilter()
  } catch (e) {
    console.error('加载预警列表失败', e)
  } finally {
    refreshing.value = false
  }
}

function applyFilter() {
  if (currentFilter.value === 'ALL') {
    list.value = allAlerts
  } else {
    list.value = allAlerts.filter(a => a.alertTypeCode === currentFilter.value)
  }
  hasMore.value = false
}

async function loadMore() { /* 单次加载，无需翻页 */ }

function onRefresh() { refresh() }

async function handleAlert(item) {
  try {
    await handleAlertApi(item.id)
    item.handled = true
    uni.showToast({ title: '已标记处理', icon: 'success' })
  } catch (e) {
    uni.showToast({ title: e.message || '操作失败', icon: 'none' })
  }
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: $color-bg-page; }
.filter-tabs { display: flex; gap: $spacing-sm; padding: $spacing-md; background: $color-bg-white; border-bottom: 1rpx solid $color-border-light; }
.filter-tab {
  padding: 8rpx $spacing-md; border-radius: $radius-round;
  font-size: $font-size-sm; background: $color-bg-grey; color: $color-text-secondary;
  &.active { background: $color-primary-light; color: $color-primary; }
}

.list-scroll { height: calc(100vh - 88px - 80rpx); }
.list-content { padding: $spacing-md; }

.alert-item {
  background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md;
  margin-bottom: $spacing-sm; box-shadow: $shadow-card;
}
.item-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: $spacing-sm; }
.batch-no { font-size: $font-size-base; font-weight: 600; color: $color-text-primary; }
.status-badge { padding: 4rpx 12rpx; border-radius: $radius-sm; }
.status-text { font-size: $font-size-xs; }

.item-body { margin-bottom: $spacing-sm; }
.vaccine-name { font-size: $font-size-base; color: $color-text-primary; display: block; }
.alert-type { font-size: $font-size-sm; color: $color-warning; display: block; margin-top: 4rpx; }
.alert-detail { font-size: $font-size-sm; color: $color-text-secondary; display: block; margin-top: 4rpx; }

.item-footer { border-top: 1rpx solid $color-border-light; padding-top: $spacing-sm; }
.btn-handle {
  width: 100%; height: 64rpx; line-height: 64rpx;
  background: $color-primary; color: #FFF; font-size: $font-size-sm;
  border: none; border-radius: $radius-lg;
  &::after { border: none; }
}

.loading-tip { text-align: center; padding: $spacing-lg; text { font-size: $font-size-sm; color: $color-text-placeholder; } }
</style>
