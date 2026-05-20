<template>
  <view class="page">
    <view class="window-badge" v-if="userStore.currentWindowName">
      <text>{{ userStore.currentWindowName }}</text>
    </view>
    <view class="filter-bar">
      <uni-datetime-picker type="date" v-model="selectedDate" :clear-icon="false">
        <view class="date-picker">
          <text>{{ formattedDate }}</text>
          <uni-icons type="bottom" :size="14" />
        </view>
      </uni-datetime-picker>
    </view>
    <view class="search-bar">
      <uni-easyinput v-model="keyword" placeholder="搜索儿童姓名" />
    </view>
    <view class="queue-list" v-if="list.length > 0">
      <QueueCard v-for="item in list" :key="item.queueId" :item="item" time-field="precheckTime" @click="goVaccinate(item)" />
    </view>
    <EmptyState v-else icon="💉" title="暂无待接种" />
    <CustomTabBar />
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow, onHide } from '@dcloudio/uni-app'
import { getQueue } from '@/api/vaccinate.js'
import { useQueue } from '@/hooks/useQueue.js'
import { useUserStore } from '@/store/user.js'
import { formatDate } from '@/utils/format.js'
import QueueCard from '@/components/QueueCard/QueueCard.vue'
import EmptyState from '@/components/EmptyState/EmptyState.vue'
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue'

const selectedDate = ref(formatDate(new Date(), 'YYYY-MM-DD'))
const keyword = ref('')
const formattedDate = computed(() => {
  const d = new Date(selectedDate.value)
  return `${d.getMonth() + 1}月${d.getDate()}日`
})
const { list, startPolling, stopPolling } = useQueue(() => getQueue({ date: selectedDate.value, keyword: keyword.value }))

const userStore = useUserStore()

function goVaccinate(item) {
  uni.navigateTo({
    url: `/pages/process/vaccinate-process?queueId=${item.queueId}&appointmentId=${item.appointmentId}&childName=${encodeURIComponent(item.childName)}&vaccineName=${encodeURIComponent(item.vaccineName)}`
  })
}

onShow(() => startPolling())
onHide(() => stopPolling())
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; padding: $spacing-lg; padding-bottom: 140rpx; }
.window-badge { background: $color-primary-light; color: $color-primary; font-size: $font-size-sm; font-weight: 600; padding: 8rpx $spacing-md; border-radius: $radius-round; display: inline-block; margin-bottom: $spacing-sm; }
.filter-bar { margin-bottom: $spacing-md; }
.date-picker { display: flex; align-items: center; gap: $spacing-xs; font-size: $font-size-base; }
.search-bar { margin-bottom: $spacing-md; }
</style>
