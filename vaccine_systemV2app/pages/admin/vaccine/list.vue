<template>
  <view class="vaccine-overview-page">
    <!-- 搜索与筛选 -->
    <view class="filter-bar">
      <uni-easyinput v-model="keyword" placeholder="搜索疫苗名称" @confirm="refresh" />
      <view class="filter-tabs">
        <view v-for="tab in filterTabs" :key="tab.value" class="filter-tab" :class="{ active: currentFilter === tab.value }" @tap="currentFilter = tab.value; refresh()">
          <text>{{ tab.label }}</text>
        </view>
      </view>
    </view>

    <!-- 库存列表 -->
    <view class="card-list">
      <view v-for="item in filteredList" :key="item.vaccineId" class="stock-card" @tap="goBatches(item)">
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

      <EmptyState v-if="!loading && filteredList.length === 0" icon="list" title="暂无库存数据" />
    </view>

    <CustomTabBar />
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getSummary } from '@/api/stock.js'
import EmptyState from '@/components/EmptyState/EmptyState.vue'
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue'

const keyword = ref('')
const currentFilter = ref('ALL')
const loading = ref(false)
const list = ref([])

const filterTabs = [
  { label: '全部', value: 'ALL' },
  { label: '一类', value: 'CLASS_I' },
  { label: '二类', value: 'CLASS_II' },
  { label: '缺货', value: 'OUT_OF_STOCK' }
]

const filteredList = computed(() => {
  let result = list.value
  if (keyword.value) {
    const kw = keyword.value.toLowerCase()
    result = result.filter(i => (i.vaccineName || '').toLowerCase().includes(kw))
  }
  if (currentFilter.value === 'CLASS_I') result = result.filter(i => i.vaccineType === 'CLASS_I')
  else if (currentFilter.value === 'CLASS_II') result = result.filter(i => i.vaccineType === 'CLASS_II')
  else if (currentFilter.value === 'OUT_OF_STOCK') result = result.filter(i => (i.availableStock || 0) === 0)
  return result
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
  loading.value = true
  try {
    const data = await getSummary()
    list.value = data.vaccines || []
  } catch (e) {
    console.error('加载库存概览失败', e)
  } finally {
    loading.value = false
  }
}

function goBatches(item) {
  uni.navigateTo({ url: `/pages/stock/batches?vaccineId=${item.vaccineId}&vaccineName=${encodeURIComponent(item.vaccineName || '')}` })
}
</script>

<style lang="scss" scoped>
.vaccine-overview-page { min-height: 100vh; background: $color-bg-page; padding-bottom: 140rpx; }

.filter-bar { padding: $spacing-md; background: $color-bg-white; border-bottom: 1rpx solid $color-border-light; }
.filter-tabs { display: flex; gap: $spacing-sm; margin-top: $spacing-sm; }
.filter-tab {
  padding: 6rpx $spacing-md; border-radius: $radius-round;
  font-size: $font-size-xs; background: $color-bg-grey; color: $color-text-secondary;
  &.active { background: $color-primary-light; color: $color-primary; }
}

.card-list { padding: 0 $spacing-md; margin-top: $spacing-md; }
.stock-card {
  background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md;
  margin-bottom: $spacing-sm; box-shadow: $shadow-card;
}
.stock-card:active { opacity: 0.7; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: $spacing-sm; }
.vaccine-name { font-size: $font-size-base; font-weight: 600; color: $color-text-primary; }
.type-tag {
  padding: 4rpx 12rpx; border-radius: $radius-sm; font-size: $font-size-xs;
  &.class1 { background: rgba(7,193,96,0.1); color: #07C160; }
  &.class2 { background: rgba(25,137,250,0.1); color: #1989FA; }
}

.stock-info { display: flex; gap: $spacing-md; margin-bottom: $spacing-sm; }
.stock-text { font-size: $font-size-xs; color: $color-text-secondary; }

.progress-bar { height: 8rpx; background: $color-border-light; border-radius: 4rpx; overflow: hidden; }
.progress-fill { height: 100%; border-radius: 4rpx; transition: width 0.3s; }

.remain-text { font-size: $font-size-xs; color: $color-text-placeholder; margin-top: 4rpx; display: block; }
</style>
