<template>
  <view class="page">
    <!-- 顶部筛选 -->
    <view class="filter-bar">
      <uni-datetime-picker type="date" v-model="selectedDate" :clear-icon="false" @change="onDateChange">
        <view class="date-picker">
          <text>{{ formattedDate }}</text>
          <uni-icons type="bottom" :size="14" />
        </view>
      </uni-datetime-picker>
      <view class="filter-tabs">
        <view v-for="tab in filterTabs" :key="tab.value" class="filter-tab" :class="{ active: currentFilter === tab.value }" @click="switchFilter(tab.value)">
          <text>{{ tab.label }}</text>
        </view>
      </view>
    </view>

    <!-- 搜索 -->
    <view class="search-bar">
      <uni-easyinput v-model="keyword" placeholder="搜索预约号/儿童姓名" @confirm="handleSearch" />
    </view>

    <!-- 队列列表 -->
    <view class="queue-list" v-if="filteredList.length > 0">
      <QueueCard v-for="item in filteredList" :key="item.appointmentId" :item="item" status-field="status" time-field="appointmentDate" @click="handleItemClick(item)" />
    </view>
    <EmptyState v-else icon="📋" title="暂无预约" description="今日暂无需要签到的预约" />

    <CustomTabBar />
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow, onHide } from '@dcloudio/uni-app'
import { getTodayList } from '@/api/signin.js'
import { useQueue } from '@/hooks/useQueue.js'
import { formatDate } from '@/utils/format.js'
import { APPOINTMENT_STATUS } from '@/utils/constants.js'
import QueueCard from '@/components/QueueCard/QueueCard.vue'
import EmptyState from '@/components/EmptyState/EmptyState.vue'
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue'

const selectedDate = ref(formatDate(new Date(), 'YYYY-MM-DD'))
const currentFilter = ref('ALL')
const keyword = ref('')

const filterTabs = [
  { label: '全部', value: 'ALL' },
  { label: '已签到', value: 'SIGNED_IN' },
  { label: '未签到', value: 'NOT_ARRIVED' }
]

const fetchList = (params) => getTodayList({ date: selectedDate.value, filter: currentFilter.value, ...params })
const { list, loading, refresh, startPolling, stopPolling } = useQueue(() => fetchList())

const formattedDate = computed(() => {
  const d = new Date(selectedDate.value)
  return `${d.getMonth() + 1}月${d.getDate()}日`
})

const filteredList = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return list.value
  return list.value.filter(item =>
    (item.queueNo && item.queueNo.toLowerCase().includes(kw)) ||
    (item.childName && item.childName.toLowerCase().includes(kw))
  )
})

function switchFilter(value) {
  currentFilter.value = value
  refresh()
}

function onDateChange() { refresh() }
function handleSearch() { /* keyword 通过 computed 过滤，无需额外请求 */ }

function handleItemClick(item) {
  if (item.status !== APPOINTMENT_STATUS.APPOINTED) {
    uni.showToast({ title: '该预约已签到', icon: 'none' })
    return
  }
  const params = new URLSearchParams()
  params.set('appointmentId', item.appointmentId)
  if (item.childName) params.set('childName', item.childName)
  if (item.vaccineName) params.set('vaccineName', item.vaccineName)
  if (item.appointmentDate) params.set('appointmentDate', item.appointmentDate)
  if (item.timeSlot) params.set('timeSlot', item.timeSlot)
  if (item.queueNo) params.set('appointmentNo', item.queueNo)
  uni.navigateTo({ url: `/pages/process/signin-confirm?${params.toString()}` })
}

// uni-app 全局生命周期，无需从 vue 导入
onShow(() => startPolling())
onHide(() => stopPolling())
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; padding: $spacing-lg; padding-bottom: 140rpx; }
.filter-bar { margin-bottom: $spacing-md; }
.date-picker { display: flex; align-items: center; gap: $spacing-xs; font-size: $font-size-base; }
.filter-tabs { display: flex; gap: $spacing-sm; margin-top: $spacing-sm; }
.filter-tab {
  padding: 8rpx $spacing-md; border-radius: $radius-round;
  font-size: $font-size-sm; background: $color-bg-white; color: $color-text-secondary;
  &.active { background: $color-primary-light; color: $color-primary; }
}
.search-bar { margin-bottom: $spacing-md; }
</style>
