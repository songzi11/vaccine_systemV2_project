<template>
  <view class="page">
    <!-- 日期筛选 -->
    <view class="filter-bar">
      <uni-datetime-picker type="daterange" v-model="dateRange" :clear-icon="false" @change="onDateChange">
        <view class="date-picker">
          <text>{{ dateRangeText }}</text>
          <uni-icons type="calendar" :size="14" color="#999" />
        </view>
      </uni-datetime-picker>
    </view>

    <!-- 记录列表 -->
    <scroll-view scroll-y class="list-scroll" @scrolltolower="loadMore" refresher-enabled :refresher-triggered="refreshing" @refresherrefresh="onRefresh">
      <view class="list-content">
        <view v-for="item in records" :key="item.id" class="record-card" @click="goDetail(item)">
          <view class="card-header">
            <text class="injection-no">{{ item.injectionId }}</text>
            <text class="injection-time">{{ item.injectionTime }}</text>
          </view>
          <view class="card-body">
            <text class="child-name">{{ item.childName }}</text>
            <text class="vaccine-name">{{ item.vaccineName }}</text>
          </view>
          <view class="card-footer">
            <text class="site-text">{{ item.injectionSite }}</text>
            <text class="batch-text">{{ item.batchNo }}</text>
          </view>
        </view>

        <EmptyState v-if="!loading && records.length === 0" icon="document" title="暂无接种记录" />
        <view v-if="loading" class="loading-tip"><text>加载中...</text></view>
        <view v-else-if="!hasMore && records.length > 0" class="loading-tip"><text>没有更多了</text></view>
      </view>
    </scroll-view>

    <CustomTabBar />
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getRecords } from '@/api/vaccinate.js'
import EmptyState from '@/components/EmptyState/EmptyState.vue'
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue'

const records = ref([])
const loading = ref(false)
const refreshing = ref(false)
const hasMore = ref(true)
const page = ref(1)
const today = new Date().toISOString().slice(0, 10)
const dateRange = ref([today, today])

const dateRangeText = computed(() => {
  if (dateRange.value && dateRange.value.length === 2) {
    return `${dateRange.value[0]} ~ ${dateRange.value[1]}`
  }
  return '选择日期范围'
})

onShow(() => refresh())

function onDateChange() { refresh() }

async function refresh() {
  refreshing.value = true
  page.value = 1
  hasMore.value = true
  try {
    const params = { page: 1, size: 20 }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const data = await getRecords(params)
    records.value = Array.isArray(data) ? data : []
    hasMore.value = false
  } catch (e) {
    console.error('加载接种记录失败', e)
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
    const params = { page: page.value, size: 20 }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const data = await getRecords(params)
    const newRecords = Array.isArray(data) ? data : []
    records.value = [...records.value, ...newRecords]
    hasMore.value = false
  } catch (e) {
    console.error('加载更多失败', e)
  } finally {
    loading.value = false
  }
}

function goDetail(item) {
  uni.navigateTo({ url: `/pages/record/detail?id=${item.id}` })
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: $color-bg-page; padding-bottom: 140rpx; }
.filter-bar { padding: $spacing-md; background: $color-bg-white; border-bottom: 1rpx solid $color-border-light; }
.date-picker { display: flex; align-items: center; gap: $spacing-xs; font-size: $font-size-base; color: $color-text-primary; }
.list-scroll { height: calc(100vh - 88px - 80rpx); }
.list-content { padding: $spacing-md; }

.record-card {
  background: $color-bg-white;
  border-radius: $radius-lg;
  padding: $spacing-md;
  margin-bottom: $spacing-md;
  box-shadow: $shadow-card;
}

.card-header { display: flex; justify-content: space-between; margin-bottom: $spacing-sm; }
.injection-no { font-size: $font-size-base; font-weight: 600; color: $color-text-primary; }
.injection-time { font-size: $font-size-xs; color: $color-text-placeholder; }

.card-body { margin-bottom: $spacing-sm; }
.child-name { font-size: $font-size-base; color: $color-text-primary; display: block; }
.vaccine-name { font-size: $font-size-sm; color: $color-text-secondary; display: block; margin-top: 4rpx; }

.card-footer { display: flex; justify-content: space-between; }
.site-text { font-size: $font-size-xs; color: $color-text-placeholder; }
.batch-text { font-size: $font-size-xs; color: $color-text-placeholder; }

.loading-tip { text-align: center; padding: $spacing-lg; text { font-size: $font-size-sm; color: $color-text-placeholder; } }
</style>
