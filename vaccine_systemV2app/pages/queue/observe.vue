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

    <view class="queue-list" v-if="list.length > 0">
      <view v-for="item in list" :key="item.injectionId" class="observe-card" @click="goDetail(item)">
        <view class="card-top">
          <text class="child-name">{{ item.childName }}</text>
          <text class="vaccine-name">{{ item.vaccineName }}</text>
        </view>
        <view class="card-middle">
          <text class="injection-no">注射号: {{ item.injectionNo }}</text>
          <text class="inject-time">接种: {{ item.injectionTime }}</text>
        </view>
        <CountdownTimer :elapsed="getElapsed(item)" :total="observeTotalSeconds" label="已观察" />
        <view class="card-actions">
          <text v-if="canFinish(item)" class="action-btn finish" @click.stop="goFinish(item)">结束留观</text>
          <text class="action-btn adverse" @click.stop="goAdverse(item)">上报不良反应</text>
        </view>
      </view>
    </view>
    <EmptyState v-else icon="👁" title="暂无留观中" />
    <CustomTabBar />
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow, onHide } from '@dcloudio/uni-app'
import { getQueue } from '@/api/observe.js'
import { useQueue } from '@/hooks/useQueue.js'
import { formatDate } from '@/utils/format.js'
import { OBSERVE_MIN_DURATION, POLLING_INTERVAL } from '@/utils/constants.js'
import { useUserStore } from '@/store/user.js'
import CountdownTimer from '@/components/CountdownTimer/CountdownTimer.vue'
import EmptyState from '@/components/EmptyState/EmptyState.vue'
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue'

const selectedDate = ref(formatDate(new Date(), 'YYYY-MM-DD'))
const observeTotalSeconds = OBSERVE_MIN_DURATION * 60
const tick = ref(0)
let tickInterval = null

const userStore = useUserStore()

const formattedDate = computed(() => {
  const d = new Date(selectedDate.value)
  return `${d.getMonth() + 1}月${d.getDate()}日`
})

const { list, startPolling, stopPolling } = useQueue(
  () => getQueue({ date: selectedDate.value }),
  POLLING_INTERVAL.OBSERVE_QUEUE
)

function getElapsed(item) {
  // 引用 tick.value 让 Vue 追踪依赖，tick 每秒变化时触发模板重渲染
  const _trigger = tick.value
  if (item.injectionTime) {
    const t = new Date(item.injectionTime.replace(' ', 'T'))
    if (!isNaN(t.getTime())) {
      return Math.floor((Date.now() - t.getTime()) / 1000)
    }
  }
  return item.elapsedSeconds || 0
}

function canFinish(item) {
  return getElapsed(item) >= observeTotalSeconds
}

function startTick() {
  stopTick()
  tickInterval = setInterval(() => { tick.value++ }, 1000)
}

function stopTick() {
  if (tickInterval) {
    clearInterval(tickInterval)
    tickInterval = null
  }
}

function goDetail(item) {
  uni.navigateTo({ url: `/pages/process/observe-detail?injectionId=${item.injectionId}` })
}

function goFinish(item) {
  uni.navigateTo({ url: `/pages/process/observe-finish?appointmentId=${item.appointmentId}&hasAdverse=${item.hasAdverseReaction}` })
}

function goAdverse(item) {
  uni.navigateTo({ url: `/pages/process/adverse-report?appointmentId=${item.appointmentId}&childName=${encodeURIComponent(item.childName || '')}&vaccineName=${encodeURIComponent(item.vaccineName || '')}` })
}

onShow(() => { startPolling(); startTick() })
onHide(() => { stopPolling(); stopTick() })
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; padding: $spacing-lg; padding-bottom: 140rpx; }
.window-badge { background: $color-primary-light; color: $color-primary; font-size: $font-size-sm; font-weight: 600; padding: 8rpx $spacing-md; border-radius: $radius-round; display: inline-block; margin-bottom: $spacing-sm; }
.filter-bar { margin-bottom: $spacing-md; }
.date-picker { display: flex; align-items: center; gap: $spacing-xs; font-size: $font-size-base; }

.observe-card {
  background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md;
  margin-bottom: $spacing-md; box-shadow: $shadow-card;
}

.card-top { margin-bottom: $spacing-sm; }
.child-name { font-size: $font-size-base; font-weight: 600; color: $color-text-primary; display: block; }
.vaccine-name { font-size: $font-size-sm; color: $color-text-secondary; display: block; margin-top: 4rpx; }

.card-middle { display: flex; justify-content: space-between; margin-bottom: $spacing-sm; }
.injection-no { font-size: $font-size-xs; color: $color-text-placeholder; }
.inject-time { font-size: $font-size-xs; color: $color-text-placeholder; }

.card-actions {
  display: flex; gap: $spacing-md; margin-top: $spacing-sm;
  padding-top: $spacing-sm; border-top: 1rpx solid $color-border-light;
}

.action-btn {
  flex: 1; text-align: center; padding: 12rpx 0;
  border-radius: $radius-lg; font-size: $font-size-sm;
  &.finish { background: $color-primary-light; color: $color-primary; }
  &.adverse { background: rgba(238, 10, 36, 0.08); color: $color-danger; }
}
</style>
