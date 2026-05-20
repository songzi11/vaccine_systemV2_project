<template>
  <view class="page">
    <view class="window-badge" v-if="userStore.currentWindowName">
      <text>{{ userStore.currentWindowName }}</text>
    </view>
    <view class="filter-bar">
      <uni-datetime-picker type="date" v-model="selectedDate" :clear-icon="false" @change="refresh">
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
    <view class="search-bar">
      <uni-easyinput v-model="keyword" placeholder="搜索儿童姓名" @confirm="refresh" />
    </view>
    <view class="queue-list" v-if="filteredList.length > 0">
      <view v-for="item in filteredList" :key="item.appointmentId" class="queue-card-wrap">
        <QueueCard :item="item" status-field="status" time-field="signinTime" @click="handleItemClick(item)" />
        <view v-if="item.status === 1" class="card-action">
          <button class="btn-signin" :loading="signingId === item.appointmentId" @tap.stop="handleSignin(item)">签到</button>
        </view>
        <view v-else class="card-action">
          <button class="btn-precheck" @tap.stop="goAssess(item)">预检</button>
        </view>
      </view>
    </view>
    <EmptyState v-else icon="🔍" title="暂无待处理" />
    <CustomTabBar />
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow, onHide } from '@dcloudio/uni-app'
import { getQueue } from '@/api/precheck.js'
import { executeSignin } from '@/api/signin.js'
import { useQueue } from '@/hooks/useQueue.js'
import { useUserStore } from '@/store/user.js'
import { formatDate } from '@/utils/format.js'
import { APPOINTMENT_STATUS } from '@/utils/constants.js'
import QueueCard from '@/components/QueueCard/QueueCard.vue'
import EmptyState from '@/components/EmptyState/EmptyState.vue'
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue'

const userStore = useUserStore()
const selectedDate = ref(formatDate(new Date(), 'YYYY-MM-DD'))
const keyword = ref('')
const signingId = ref(null)
const currentFilter = ref('ALL')

const filterTabs = [
  { label: '全部', value: 'ALL' },
  { label: '待签到', value: 'NOT_SIGNED' },
  { label: '已签到', value: 'SIGNED' }
]

const formattedDate = computed(() => {
  const d = new Date(selectedDate.value)
  return `${d.getMonth() + 1}月${d.getDate()}日`
})

const fetchList = (params) => getQueue({ date: selectedDate.value, ...params })
const { list, refresh, startPolling, stopPolling } = useQueue(() => fetchList())

const filteredList = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  let result = list.value
  if (currentFilter.value === 'NOT_SIGNED') {
    result = result.filter(i => i.status === APPOINTMENT_STATUS.APPOINTED)
  } else if (currentFilter.value === 'SIGNED') {
    result = result.filter(i => i.status === APPOINTMENT_STATUS.SIGNED_IN)
  }
  if (kw) {
    result = result.filter(i => i.childName && i.childName.toLowerCase().includes(kw))
  }
  return result
})

function switchFilter(value) {
  currentFilter.value = value
}

async function handleSignin(item) {
  signingId.value = item.appointmentId
  try {
    await executeSignin({ appointmentId: item.appointmentId })
    uni.showToast({ title: '签到成功', icon: 'success' })
    refresh()
  } catch (e) {
    uni.showToast({ title: '签到失败', icon: 'none' })
  } finally {
    signingId.value = null
  }
}

function handleItemClick(item) {
  if (item.status === APPOINTMENT_STATUS.SIGNED_IN) {
    goAssess(item)
  }
}

function goAssess(item) {
  uni.navigateTo({
    url: `/pages/process/precheck-assess?appointmentId=${item.appointmentId}&childName=${encodeURIComponent(item.childName)}&vaccineName=${encodeURIComponent(item.vaccineName)}`
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
.filter-tabs { display: flex; gap: $spacing-sm; margin-top: $spacing-sm; }
.filter-tab {
  padding: 8rpx $spacing-md; border-radius: $radius-round;
  font-size: $font-size-sm; background: $color-bg-white; color: $color-text-secondary;
  &.active { background: $color-primary-light; color: $color-primary; }
}
.search-bar { margin-bottom: $spacing-md; }
.queue-card-wrap { margin-bottom: $spacing-base; }
.card-action { padding: 0 $spacing-base $spacing-base; }
.btn-signin, .btn-precheck {
  width: 100%; height: 72rpx; line-height: 72rpx; border-radius: $radius-lg;
  font-size: $font-size-base; border: none;
  &::after { border: none; }
}
.btn-signin { background: $color-primary; color: #FFFFFF; }
.btn-precheck { background: #FF9900; color: #FFFFFF; }
</style>
