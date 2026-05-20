<template>
  <view class="page">
    <uni-nav-bar title="调拨记录" :border="false" />

    <view class="filter-bar">
      <uni-easyinput v-model="keyword" placeholder="搜索批次号" />
      <uni-datetime-picker type="daterange" v-model="dateRange" :clear-icon="false">
        <view class="date-picker">
          <text>{{ dateRangeText }}</text>
          <uni-icons type="calendar" :size="14" color="#999" />
        </view>
      </uni-datetime-picker>
    </view>

    <scroll-view scroll-y class="list-scroll" @scrolltolower="loadMore" refresher-enabled :refresher-triggered="refreshing" @refresherrefresh="onRefresh">
      <view class="list-content">
        <view v-for="item in list" :key="item.id" class="transfer-item">
          <view class="item-header">
            <text class="transfer-no">{{ item.transferNo }}</text>
            <text class="transfer-time">{{ item.createTime }}</text>
          </view>
          <view class="item-body">
            <text class="batch-no">批次: {{ item.batchNo }}</text>
            <text class="location-text">{{ item.fromLocationName }} → {{ item.toLocationName }}</text>
            <text class="quantity-text">数量: {{ item.quantity }}</text>
          </view>
          <view class="item-footer">
            <text class="operator">操作人: {{ item.operatorName }}</text>
          </view>
        </view>

        <EmptyState v-if="!loading && list.length === 0" icon="document" title="暂无调拨记录" />
        <view v-if="loading" class="loading-tip"><text>加载中...</text></view>
        <view v-else-if="!hasMore && list.length > 0" class="loading-tip"><text>没有更多了</text></view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getTransferRecords } from '@/api/stock.js'
import EmptyState from '@/components/EmptyState/EmptyState.vue'

const keyword = ref('')
const dateRange = ref([])
const list = ref([])
const loading = ref(false)
const refreshing = ref(false)
const hasMore = ref(true)
const page = ref(1)

const dateRangeText = computed(() => {
  if (dateRange.value && dateRange.value.length === 2) return `${dateRange.value[0]} ~ ${dateRange.value[1]}`
  return '选择日期范围'
})

onShow(() => refresh())

async function refresh() {
  refreshing.value = true
  page.value = 1
  hasMore.value = true
  try {
    const params = { page: 1, size: 20 }
    if (keyword.value) params.keyword = keyword.value
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const data = await getTransferRecords(params)
    list.value = data.records || (Array.isArray(data) ? data : [])
    hasMore.value = list.value.length >= 20
  } catch (e) {
    console.error('加载调拨记录失败', e)
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
    if (keyword.value) params.keyword = keyword.value
    const data = await getTransferRecords(params)
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
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: $color-bg-page; }
.filter-bar { padding: $spacing-md; background: $color-bg-white; border-bottom: 1rpx solid $color-border-light; }
.date-picker { display: flex; align-items: center; gap: $spacing-xs; font-size: $font-size-base; color: $color-text-primary; margin-top: $spacing-sm; }

.list-scroll { height: calc(100vh - 88px - 160rpx); }
.list-content { padding: $spacing-md; }

.transfer-item {
  background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md;
  margin-bottom: $spacing-sm; box-shadow: $shadow-card;
}
.item-header { display: flex; justify-content: space-between; margin-bottom: $spacing-sm; }
.transfer-no { font-size: $font-size-base; font-weight: 600; color: $color-text-primary; }
.transfer-time { font-size: $font-size-xs; color: $color-text-placeholder; }

.item-body { margin-bottom: $spacing-sm; }
.batch-no { font-size: $font-size-sm; color: $color-text-primary; display: block; }
.location-text { font-size: $font-size-sm; color: $color-info; display: block; margin-top: 4rpx; }
.quantity-text { font-size: $font-size-sm; color: $color-text-secondary; display: block; margin-top: 4rpx; }

.item-footer { border-top: 1rpx solid $color-border-light; padding-top: $spacing-sm; }
.operator { font-size: $font-size-xs; color: $color-text-placeholder; }

.loading-tip { text-align: center; padding: $spacing-lg; text { font-size: $font-size-sm; color: $color-text-placeholder; } }
</style>
