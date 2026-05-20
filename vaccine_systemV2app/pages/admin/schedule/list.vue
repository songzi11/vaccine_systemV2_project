<template>
  <view class="page">
    <!-- 日期选择 -->
    <view class="date-bar">
      <view class="date-picker" @tap="changeDate(-1)">
        <text class="date-nav">&lt;</text>
      </view>
      <picker mode="date" :value="currentDate" @change="onDatePick">
        <view class="date-display">
          <text class="date-text">{{ currentDate }}</text>
          <text v-if="!isToday" class="today-tag" @tap.stop="goToday">今天</text>
        </view>
      </picker>
      <view class="date-picker" @tap="changeDate(1)">
        <text class="date-nav">&gt;</text>
      </view>
    </view>

    <!-- 排班列表 -->
    <view v-if="groupedList.length > 0" class="card-list">
      <view v-for="group in groupedList" :key="group.roleName" class="role-group">
        <view v-if="group.roleName === '后勤'" class="group-title logistics-title" @tap="logisticsExpanded = !logisticsExpanded">
          <text class="group-title-text">{{ group.roleName }}（{{ group.items.length }}人）</text>
          <text class="expand-arrow">{{ logisticsExpanded ? '收起' : '展开' }}</text>
        </view>
        <text v-else class="group-title">{{ group.roleName }}（{{ group.items.length }}人）</text>
        <template v-if="group.roleName !== '后勤' || logisticsExpanded">
          <view v-for="item in group.items" :key="item.doctorId" class="schedule-card">
            <view class="card-header">
              <text class="doctor-name">{{ item.doctorName }}</text>
              <text v-if="item.windowName" class="window-name">{{ item.windowName }}</text>
              <text v-else class="window-name logistics">后勤</text>
            </view>
            <view v-if="item.windowName" class="slot-row">
              <view class="slot-item" @tap="showStatusPicker(item, 'AM', item.amStatus, item.amScheduleId)">
                <text class="slot-label">上午</text>
                <view class="status-badge" :class="statusClass(item.amStatus)">
                  <text class="status-text">{{ statusText(item.amStatus) }}</text>
                </view>
              </view>
              <view class="slot-divider" />
              <view class="slot-item" @tap="showStatusPicker(item, 'PM', item.pmStatus, item.pmScheduleId)">
                <text class="slot-label">下午</text>
                <view class="status-badge" :class="statusClass(item.pmStatus)">
                  <text class="status-text">{{ statusText(item.pmStatus) }}</text>
                </view>
              </view>
            </view>
          </view>
        </template>
      </view>
    </view>

    <EmptyState v-else-if="!loading" icon="list" title="暂无医生排班数据" />

    <!-- 状态选择弹窗 -->
    <view v-if="pickerVisible" class="picker-mask" @tap="pickerVisible = false">
      <view class="picker-popup" @tap.stop>
        <text class="picker-title">{{ pickerItem.doctorName }} - {{ pickerSlot === 'AM' ? '上午' : '下午' }}</text>
        <view v-for="opt in statusOptions" :key="opt.value" class="picker-option" @tap="handleToggle(opt.value)">
          <view class="option-dot" :style="{ backgroundColor: opt.color }" />
          <text class="option-text">{{ opt.label }}</text>
          <text v-if="pickerCurrentStatus === opt.value" class="option-check">✓</text>
        </view>
        <view class="picker-cancel" @tap="pickerVisible = false">
          <text class="cancel-text">取消</text>
        </view>
      </view>
    </view>

    <CustomTabBar />
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getDailyView, toggleScheduleStatus } from '@/api/schedule.js'
import EmptyState from '@/components/EmptyState/EmptyState.vue'
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue'

const currentDate = ref('')
const list = ref([])
const loading = ref(false)

const pickerVisible = ref(false)
const pickerItem = ref({})
const pickerSlot = ref('')
const pickerCurrentStatus = ref(0)

const statusOptions = [
  { value: 0, label: '正常值班', color: '#07C160' },
  { value: 1, label: '请假', color: '#FF9900' },
  { value: 2, label: '取消排班', color: '#EE0A24' }
]

const isToday = computed(() => currentDate.value === formatDate(new Date()))

function formatDate(d) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function changeDate(offset) {
  const d = new Date(currentDate.value + 'T00:00:00')
  d.setDate(d.getDate() + offset)
  currentDate.value = formatDate(d)
  loadView()
}

function goToday() {
  currentDate.value = formatDate(new Date())
  loadView()
}

function onDatePick(e) {
  currentDate.value = e.detail.value
  loadView()
}

function statusText(status) {
  return { 0: '正常', 1: '请假', 2: '取消' }[status] || '正常'
}

function statusClass(status) {
  return { 0: 'status-normal', 1: 'status-leave', 2: 'status-cancel' }[status] || 'status-normal'
}

const logisticsExpanded = ref(false)

const groupedList = computed(() => {
  const map = {}
  list.value.forEach(item => {
    const role = item.roleName || '其他'
    if (!map[role]) map[role] = []
    map[role].push(item)
  })
  const groups = Object.keys(map).map(role => ({ roleName: role, items: map[role] }))
  const sortOrder = { '后勤': 1, '库存管理': 2 }
  groups.sort((a, b) => (sortOrder[a.roleName] || 0) - (sortOrder[b.roleName] || 0))
  return groups
})

function showStatusPicker(item, slot, status) {
  pickerItem.value = item
  pickerSlot.value = slot
  pickerCurrentStatus.value = status
  pickerVisible.value = true
}

async function handleToggle(newStatus) {
  pickerVisible.value = false
  try {
    await toggleScheduleStatus({
      doctorId: pickerItem.value.doctorId,
      windowId: pickerItem.value.windowId,
      scheduleDate: currentDate.value,
      timeSlot: pickerSlot.value,
      status: newStatus
    })
    uni.showToast({ title: '操作成功', icon: 'success' })
    loadView()
  } catch (e) {}
}

async function loadView() {
  loading.value = true
  try {
    const data = await getDailyView(currentDate.value)
    list.value = Array.isArray(data) ? data : []
  } catch (e) {
    console.error('加载排班视图失败', e)
  } finally {
    loading.value = false
  }
}

onShow(() => {
  currentDate.value = formatDate(new Date())
  loadView()
})
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: $color-bg-page; padding-bottom: 140rpx; }

.date-bar {
  display: flex; justify-content: space-between; align-items: center;
  padding: $spacing-md $spacing-lg; background: $color-bg-white;
}
.date-picker {
  width: 72rpx; height: 72rpx; display: flex; align-items: center; justify-content: center;
  background: $color-bg-grey; border-radius: $radius-lg;
}
.date-nav { font-size: $font-size-lg; color: $color-text-primary; }
.date-display { display: flex; align-items: center; gap: $spacing-xs; }
.date-text { font-size: $font-size-lg; font-weight: 600; color: $color-primary; }
.today-tag {
  font-size: $font-size-xs; color: #FFFFFF; background: $color-primary;
  padding: 2rpx 12rpx; border-radius: $radius-sm;
}

.card-list { padding: $spacing-md; }
.role-group { margin-bottom: $spacing-lg; }
.group-title {
  font-size: $font-size-sm; color: $color-text-secondary; font-weight: 600;
  margin-bottom: $spacing-sm; padding-left: $spacing-xs;
}
.logistics-title {
  display: flex; justify-content: space-between; align-items: center;
  padding: $spacing-sm $spacing-xs; background: $color-bg-grey; border-radius: $radius-md;
  margin-bottom: $spacing-sm;
}
.logistics-title:active { opacity: 0.7; }
.group-title-text { font-size: $font-size-sm; color: $color-text-placeholder; font-weight: 600; }
.expand-arrow { font-size: $font-size-xs; color: $color-text-placeholder; }
.schedule-card {
  background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md;
  margin-bottom: $spacing-sm; box-shadow: $shadow-card;
}
.card-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: $spacing-sm;
}
.doctor-name { font-size: $font-size-base; font-weight: 600; color: $color-text-primary; }
.window-name { font-size: $font-size-sm; color: $color-text-secondary; }
.window-name.logistics { color: #999999; }

.slot-row {
  display: flex; align-items: center; border-top: 1rpx solid $color-border-light;
  padding-top: $spacing-sm;
}
.slot-item {
  flex: 1; display: flex; align-items: center; justify-content: center; gap: $spacing-sm;
  padding: $spacing-xs 0;
}
.slot-item:active { opacity: 0.7; }
.slot-label { font-size: $font-size-sm; color: $color-text-secondary; }
.slot-divider { width: 1rpx; height: 48rpx; background: $color-border-light; }

.status-badge { padding: 4rpx 20rpx; border-radius: 20rpx; }
.status-text { font-size: $font-size-xs; font-weight: 500; }
.status-normal { background: rgba(7,193,96,0.1); }
.status-normal .status-text { color: #07C160; }
.status-leave { background: rgba(255,153,0,0.1); }
.status-leave .status-text { color: #FF9900; }
.status-cancel { background: rgba(238,10,36,0.1); }
.status-cancel .status-text { color: #EE0A24; }

.picker-mask {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5); z-index: 999;
  display: flex; align-items: flex-end;
}
.picker-popup {
  width: 100%; background: #FFFFFF; border-radius: 24rpx 24rpx 0 0;
  padding: $spacing-lg;
  padding-bottom: calc(#{$spacing-lg} + env(safe-area-inset-bottom));
}
.picker-title {
  font-size: $font-size-base; font-weight: 600; color: $color-text-primary;
  text-align: center; display: block; margin-bottom: $spacing-md;
}
.picker-option {
  display: flex; align-items: center; padding: $spacing-md 0;
  border-bottom: 1rpx solid $color-border-light;
}
.picker-option:last-of-type { border-bottom: none; }
.picker-option:active { opacity: 0.7; }
.option-dot { width: 16rpx; height: 16rpx; border-radius: 50%; margin-right: $spacing-md; }
.option-text { flex: 1; font-size: $font-size-base; color: $color-text-primary; }
.option-check { font-size: $font-size-lg; color: $color-primary; }
.picker-cancel {
  margin-top: $spacing-md; padding: $spacing-md 0;
  text-align: center; border-top: 1rpx solid $color-border-light;
}
.cancel-text { font-size: $font-size-base; color: $color-text-secondary; }
</style>
