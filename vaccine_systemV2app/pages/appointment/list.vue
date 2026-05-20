<template>
  <view class="appointment-list-page">
    <!-- Tab 筛选 -->
    <view class="tab-bar">
      <uni-segmented-control
        :current="currentTab"
        :values="tabLabels"
        @clickItem="onTabChange"
        style-type="text"
        active-color="#07C160"
      />
    </view>

    <!-- 列表 -->
    <scroll-view
      scroll-y
      class="list-scroll"
      @scrolltolower="loadMore"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="refresh"
    >
      <view class="list-content">
        <AppointmentCard
          v-for="item in appointments"
          :key="item.id"
          :appointment="item"
          @click="goToDetail(item)"
        />
        <EmptyState v-if="!loading && appointments.length === 0" icon="calendar" title="暂无预约记录" />
        <view v-if="loading" class="loading-tip">
          <text>加载中...</text>
        </view>
        <view v-else-if="!hasMore && appointments.length > 0" class="loading-tip">
          <text>没有更多了</text>
        </view>
      </view>
    </scroll-view>

    <!-- 浮动添加按钮 -->
    <view class="fab" @tap="goToCreate">
      <text class="fab-text">+</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getAppointmentList } from '@/api/appointment.js'
import { useAppointmentStore } from '@/store/appointment.js'
import AppointmentCard from '@/components/AppointmentCard/AppointmentCard.vue'
import EmptyState from '@/components/EmptyState/EmptyState.vue'

const appointmentStore = useAppointmentStore()
const loading = computed(() => appointmentStore.loading)
const appointments = computed(() => appointmentStore.appointments)

const currentTab = ref(0)
const refreshing = ref(false)
const hasMore = ref(true)
const page = ref(1)

const tabLabels = ['全部', '待签到', '进行中', '已完成', '已取消']
const tabStatuses = ['', 'PENDING_SIGN_IN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED']

onShow(() => {
  refresh()
})

function onTabChange(e) {
  currentTab.value = e.currentIndex
  refresh()
}

async function refresh() {
  refreshing.value = true
  page.value = 1
  try {
    const data = await getAppointmentList()
    const list = Array.isArray(data) ? data : (data.records || [])
    // 前端按 tab 过滤
    const status = tabStatuses[currentTab.value]
    appointmentStore.appointments = status ? list.filter(item => {
      const s = item.status
      if (status === 'PENDING_SIGN_IN') return s === 1
      if (status === 'IN_PROGRESS') return [6, 7, 10].includes(s)
      if (status === 'COMPLETED') return s === 2
      if (status === 'CANCELLED') return [3, 4, 9].includes(s)
      return true
    }) : list
    hasMore.value = false
  } catch (e) {
    console.error('加载预约列表失败', e)
  } finally {
    refreshing.value = false
  }
}

async function loadMore() {
  // 后端不支持分页，无更多数据
}

function goToDetail(item) {
  uni.navigateTo({ url: `/pages/appointment/detail?id=${item.id}` })
}

function goToCreate() {
  uni.navigateTo({ url: '/pages/appointment/create' })
}
</script>

<style lang="scss" scoped>
.appointment-list-page {
  min-height: 100vh;
  background-color: $color-bg-page;
  display: flex;
  flex-direction: column;
}

.tab-bar {
  padding: $spacing-md;
  background-color: $color-bg-white;
}

.list-scroll {
  flex: 1;
  height: calc(100vh - 100rpx - 120rpx - env(safe-area-inset-bottom));
}

.list-content {
  padding: $spacing-md;
}

.loading-tip {
  text-align: center;
  padding: $spacing-lg;

  text {
    font-size: $font-size-sm;
    color: $color-text-placeholder;
  }
}

.fab {
  position: fixed;
  right: $spacing-lg;
  bottom: calc(120rpx + env(safe-area-inset-bottom));
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background-color: $color-primary;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 16rpx rgba(7, 193, 96, 0.4);
}

.fab-text {
  font-size: 56rpx;
  color: #FFFFFF;
  line-height: 1;
  margin-top: -4rpx;
}
</style>
