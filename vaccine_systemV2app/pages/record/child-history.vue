<template>
  <view class="page">
    <view class="nav-bar">
      <uni-icons type="back" :size="20" @click="uni.navigateBack()" />
      <text class="nav-title">儿童接种历史</text>
    </view>

    <!-- 儿童信息卡片 -->
    <view class="child-card">
      <text class="child-name">{{ childName }}</text>
      <text class="child-info">{{ childGenderText }} | {{ childBirthDate }}</text>
    </view>

    <!-- 接种记录列表 -->
    <scroll-view scroll-y class="list-scroll" @scrolltolower="loadMore">
      <view class="list-content">
        <view v-for="item in records" :key="item.id" class="record-item">
          <view class="record-header">
            <text class="vaccine-name">{{ item.vaccineName }}</text>
            <text class="record-date">{{ item.injectionTime }}</text>
          </view>
          <view class="record-detail">
            <text class="detail-text">批次: {{ item.batchNo }}</text>
            <text class="detail-text">部位: {{ item.injectionSite }}</text>
          </view>
        </view>

        <EmptyState v-if="!loading && records.length === 0" icon="document" title="暂无接种记录" />
        <view v-if="loading" class="loading-tip"><text>加载中...</text></view>
        <view v-else-if="!hasMore && records.length > 0" class="loading-tip"><text>没有更多了</text></view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getChildRecords } from '@/api/vaccinate.js'
import EmptyState from '@/components/EmptyState/EmptyState.vue'

const childId = ref('')
const childName = ref('')
const childGender = ref(0)
const childBirthDate = ref('')
const records = ref([])
const loading = ref(false)
const hasMore = ref(true)
const page = ref(1)

const childGenderText = computed(() => childGender.value === 1 ? '男' : '女')

onLoad(async (query) => {
  childId.value = query.childId || ''
  childName.value = query.childName ? decodeURIComponent(query.childName) : ''
  childGender.value = Number(query.childGender) || 0
  childBirthDate.value = query.childBirthDate || ''
  await refresh()
})

async function refresh() {
  loading.value = true
  page.value = 1
  hasMore.value = true
  try {
    const data = await getChildRecords(childId.value)
    records.value = Array.isArray(data) ? data : []
    hasMore.value = false
  } catch (e) {
    console.error('加载儿童接种记录失败', e)
  } finally {
    loading.value = false
  }
}

async function loadMore() {
  if (!hasMore.value || loading.value) return
  loading.value = true
  page.value++
  try {
    const data = await getChildRecords(childId.value)
    const newRecords = Array.isArray(data) ? data : []
    records.value = [...records.value, ...newRecords]
    hasMore.value = newRecords.length >= 20
  } catch (e) {
    console.error('加载更多失败', e)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; padding: $spacing-lg; background: $color-bg-page; }
.nav-bar { display: flex; align-items: center; gap: $spacing-sm; margin-bottom: $spacing-lg; }
.nav-title { font-size: $font-size-lg; font-weight: 600; }

.child-card {
  background: $color-bg-white;
  border-radius: $radius-lg;
  padding: $spacing-lg;
  box-shadow: $shadow-card;
  margin-bottom: $spacing-lg;
}

.child-name { font-size: $font-size-lg; font-weight: 600; color: $color-text-primary; display: block; }
.child-info { font-size: $font-size-sm; color: $color-text-secondary; margin-top: $spacing-xs; display: block; }

.list-scroll { height: calc(100vh - 200rpx); }
.list-content { padding-bottom: $spacing-lg; }

.record-item {
  background: $color-bg-white;
  border-radius: $radius-lg;
  padding: $spacing-md;
  margin-bottom: $spacing-sm;
  box-shadow: $shadow-card;
}

.record-header { display: flex; justify-content: space-between; margin-bottom: $spacing-xs; }
.vaccine-name { font-size: $font-size-base; font-weight: 500; color: $color-text-primary; }
.record-date { font-size: $font-size-xs; color: $color-text-placeholder; }

.record-detail { display: flex; gap: $spacing-md; }
.detail-text { font-size: $font-size-xs; color: $color-text-secondary; }

.loading-tip { text-align: center; padding: $spacing-lg; text { font-size: $font-size-sm; color: $color-text-placeholder; } }
</style>
