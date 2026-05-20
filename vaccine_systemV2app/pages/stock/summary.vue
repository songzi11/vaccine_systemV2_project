<template>
  <view class="page">
    <!-- 搜索与筛选 -->
    <view class="filter-bar">
      <uni-easyinput v-model="keyword" placeholder="搜索疫苗名称" @confirm="refresh" />
      <view class="filter-tabs">
        <view v-for="tab in filterTabs" :key="tab.value" class="filter-tab" :class="{ active: currentFilter === tab.value }" @click="currentFilter = tab.value; refresh()">
          <text>{{ tab.label }}</text>
        </view>
      </view>
    </view>

    <!-- 统计卡片 -->
    <scroll-view scroll-x class="stats-scroll">
      <view class="stats-grid">
        <view class="stat-item">
          <text class="stat-value">{{ summary.totalVaccines || 0 }}</text>
          <text class="stat-label">总品种</text>
        </view>
        <view class="stat-item warning" @click="goAlerts">
          <text class="stat-value">{{ summary.alertCount || 0 }}</text>
          <text class="stat-label">库存预警</text>
        </view>
        <view class="stat-item danger">
          <text class="stat-value">{{ summary.expiredCount || 0 }}</text>
          <text class="stat-label">已过期</text>
        </view>
        <view class="stat-item" @click="goTransferRecords">
          <text class="stat-value">{{ summary.todayTransfers || 0 }}</text>
          <text class="stat-label">今日调拨</text>
        </view>
      </view>
    </scroll-view>

    <!-- 库存列表 -->
    <scroll-view scroll-y class="list-scroll" @scrolltolower="loadMore" refresher-enabled :refresher-triggered="refreshing" @refresherrefresh="onRefresh">
      <view class="list-content">
        <view v-for="item in filteredList" :key="item.vaccineId" class="stock-card" @click="goBatches(item)">
          <view class="card-header">
            <text class="vaccine-name">{{ item.vaccineName }}</text>
            <view class="type-tag" :class="item.vaccineType === 'CLASS_I' ? 'class1' : 'class2'">
              <text>{{ item.vaccineType === 'CLASS_I' ? '一类' : '二类' }}</text>
            </view>
          </view>
          <view class="stock-info">
            <text class="stock-text">总数: {{ item.totalStock || 0 }}</text>
            <text class="stock-text">可用数: {{ item.availableStock || 0 }}</text>
            <text class="stock-text">锁定数: {{ item.lockedStock || 0 }}</text>
          </view>
          <view class="progress-bar">
            <view class="progress-fill" :style="{ width: remainRatio(item) + '%', backgroundColor: progressColor(remainRatio(item)) }" />
          </view>
          <text class="remain-text">可用比例: {{ remainRatio(item) }}%</text>
        </view>

        <EmptyState v-if="!loading && filteredList.length === 0" icon="📦" title="暂无库存数据" />
        <view v-if="loading" class="loading-tip"><text>加载中...</text></view>
      </view>
    </scroll-view>

    <CustomTabBar />
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getSummary } from '@/api/stock.js'
import EmptyState from '@/components/EmptyState/EmptyState.vue'
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue'

const keyword = ref('')
const currentFilter = ref('ALL')
const loading = ref(false)
const refreshing = ref(false)
const list = ref([])
const summary = reactive({ totalVaccines: 0, alertCount: 0, expiredCount: 0, todayTransfers: 0 })

const filterTabs = [
  { label: '全部', value: 'ALL' },
  { label: '充足', value: 'SUFFICIENT' },
  { label: '预警', value: 'ALERT' },
  { label: '过期', value: 'EXPIRED' }
]

const filteredList = computed(() => {
  let result = list.value
  if (keyword.value) {
    const kw = keyword.value.toLowerCase()
    result = result.filter(i => (i.vaccineName || '').toLowerCase().includes(kw))
  }
  if (currentFilter.value === 'SUFFICIENT') result = result.filter(i => remainRatio(i) >= 50)
  else if (currentFilter.value === 'ALERT') result = result.filter(i => remainRatio(i) >= 20 && remainRatio(i) < 50)
  else if (currentFilter.value === 'EXPIRED') result = result.filter(i => remainRatio(i) < 20)
  return result.sort((a, b) => remainRatio(a) - remainRatio(b))
})

function remainRatio(item) {
  const total = item.totalStock || 1
  return Math.round(((item.availableStock || 0) / total) * 100)
}

function progressColor(ratio) {
  if (ratio >= 50) return '#07C160'
  if (ratio >= 20) return '#FF9900'
  return '#EE0A24'
}

onShow(() => refresh())

async function refresh() {
  refreshing.value = true
  try {
    const data = await getSummary()
    summary.totalVaccines = data.totalVaccines || 0
    summary.alertCount = data.alertCount || 0
    summary.expiredCount = data.expiredCount || 0
    summary.todayTransfers = data.todayTransfers || 0
    list.value = data.vaccines || []
  } catch (e) {
    console.error('加载库存总览失败', e)
  } finally {
    refreshing.value = false
  }
}

function onRefresh() { refresh() }
function loadMore() { /* 总览页面一次加载全部 */ }

function goBatches(item) {
  uni.navigateTo({ url: `/pages/stock/batches?vaccineId=${item.vaccineId}&vaccineName=${encodeURIComponent(item.vaccineName || '')}` })
}

function goAlerts() {
  uni.navigateTo({ url: '/pages/stock/alerts' })
}

function goTransferRecords() {
  uni.navigateTo({ url: '/pages/stock/transfer-records' })
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: $color-bg-page; padding-bottom: 140rpx; }

.filter-bar { padding: $spacing-md; background: $color-bg-white; border-bottom: 1rpx solid $color-border-light; }
.filter-tabs { display: flex; gap: $spacing-sm; margin-top: $spacing-sm; }
.filter-tab {
  padding: 6rpx $spacing-md; border-radius: $radius-round;
  font-size: $font-size-xs; background: $color-bg-grey; color: $color-text-secondary;
  &.active { background: $color-primary-light; color: $color-primary; }
}

.stats-scroll { margin: $spacing-md 0; white-space: nowrap; }
.stats-grid { display: flex; gap: $spacing-md; padding: 0 $spacing-md; }
.stat-item {
  min-width: 160rpx; background: $color-bg-white; border-radius: $radius-lg;
  padding: $spacing-md; text-align: center; box-shadow: $shadow-card;
  &.warning { border-left: 4rpx solid $color-warning; }
  &.danger { border-left: 4rpx solid $color-danger; }
}
.stat-value { font-size: 40rpx; font-weight: 600; color: $color-primary; display: block; }
.stat-label { font-size: $font-size-xs; color: $color-text-secondary; margin-top: $spacing-xs; display: block; }

.list-scroll { height: calc(100vh - 400rpx - 120rpx); }
.list-content { padding: 0 $spacing-md; }

.stock-card {
  background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md;
  margin-bottom: $spacing-sm; box-shadow: $shadow-card;
}
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: $spacing-sm; }
.vaccine-name { font-size: $font-size-base; font-weight: 600; color: $color-text-primary; }
.type-tag {
  padding: 4rpx 12rpx; border-radius: $radius-sm; font-size: $font-size-xs;
  &.class1 { background: rgba(7,193,96,0.1); color: $color-success; }
  &.class2 { background: rgba(25,137,250,0.1); color: $color-info; }
}

.stock-info { display: flex; gap: $spacing-md; margin-bottom: $spacing-sm; }
.stock-text { font-size: $font-size-xs; color: $color-text-secondary; }

.progress-bar { height: 8rpx; background: $color-border-light; border-radius: 4rpx; overflow: hidden; }
.progress-fill { height: 100%; border-radius: 4rpx; transition: width 0.3s; }

.remain-text { font-size: $font-size-xs; color: $color-text-placeholder; margin-top: 4rpx; display: block; }
.loading-tip { text-align: center; padding: $spacing-lg; text { font-size: $font-size-sm; color: $color-text-placeholder; } }
</style>
