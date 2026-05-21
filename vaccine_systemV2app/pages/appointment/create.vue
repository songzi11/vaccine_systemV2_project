<template>
  <view class="create-page">
    <uni-nav-bar :title="stepTitles[currentStep]" :border="false" />

    <!-- 步骤指示器 -->
    <StepIndicator :steps="stepLabels" :current="currentStep" />

    <!-- 步骤1: 选择儿童 -->
    <view v-if="currentStep === 0" class="step-content">
      <view class="section-header">
        <text class="section-title">选择儿童</text>
        <text class="section-action" @tap="navigateToAddChild">+ 添加儿童</text>
      </view>
      <ChildCard
        v-for="child in children"
        :key="child.id"
        :child="child"
        :selectable="true"
        :selected="selectedChildId === child.id"
        @click="selectChild(child)"
      />
      <EmptyState v-if="children.length === 0" icon="person" title="暂无儿童信息" actionText="添加儿童" @action="navigateToAddChild" />
    </view>

    <!-- 步骤2: 选择疫苗 -->
    <view v-if="currentStep === 1" class="step-content">
      <view class="section-header">
        <text class="section-title">选择疫苗</text>
      </view>
      <view class="filter-tabs">
        <view
          v-for="tab in vaccineTabs"
          :key="tab.value"
          class="filter-tab"
          :class="{ active: activeVaccineTab === tab.value }"
          @tap="activeVaccineTab = tab.value"
        >
          {{ tab.label }}
        </view>
      </view>
      <VaccineCard
        v-for="vaccine in filteredVaccines"
        :key="vaccine.id"
        :vaccine="vaccine"
        :selectable="true"
        :selected="selectedVaccineId === vaccine.id"
        @click="selectVaccine(vaccine)"
      />
    </view>

    <!-- 步骤3: 选择时间 -->
    <view v-if="currentStep === 2" class="step-content">
      <view class="section-header">
        <text class="section-title">选择接种时间</text>
      </view>
      <!-- 已选疫苗摘要 -->
      <view v-if="selectedVaccine" class="vaccine-summary">
        <text class="vaccine-name">{{ selectedVaccine.name }}</text>
        <view class="category-tag" :class="selectedVaccine.category === 'CLASS_I' ? 'class-i' : 'class-ii'">
          <text>{{ selectedVaccine.category === 'CLASS_I' ? '一类·免费' : '二类·自费' }}</text>
        </view>
      </view>
      <!-- 日历选择器 -->
      <uni-datetime-picker type="date" v-model="selectedDate" :start="todayStr" :end="maxDateStr" @change="onDateChange" />
      <!-- 时段列表 -->
      <view class="time-slots">
        <view
          v-for="slot in timeSlots"
          :key="slot.id"
          class="time-slot"
          :class="{ selected: selectedTimeSlotId === slot.id, disabled: slot.disabled }"
          @tap="selectTimeSlot(slot)"
        >
          <text class="slot-label">{{ slot.label }}</text>
          <text class="slot-remaining" :class="{ empty: slot.disabled }">{{ slot.statusText }}</text>
        </view>
      </view>
    </view>

    <!-- 底部按钮 -->
    <view class="bottom-bar">
      <button class="btn-prev" v-if="currentStep > 0" @tap="prevStep">上一步</button>
      <button class="btn-next" v-if="currentStep < 2" :disabled="!canNext" @tap="nextStep">下一步</button>
      <button class="btn-submit" v-if="currentStep === 2" :loading="submitting" :disabled="!canSubmit" @tap="submitAppointment">确认预约</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useChildStore } from '@/store/child.js'
import { useAppointment } from '@/hooks/useAppointment.js'
import { getVaccineList } from '@/api/vaccine.js'
import { getSlotAvailability } from '@/api/appointment.js'
import StepIndicator from '@/components/StepIndicator/StepIndicator.vue'
import ChildCard from '@/components/ChildCard/ChildCard.vue'
import VaccineCard from '@/components/VaccineCard/VaccineCard.vue'
import EmptyState from '@/components/EmptyState/EmptyState.vue'

const childStore = useChildStore()
const { submitting, create } = useAppointment()

const currentStep = ref(0)
const selectedChildId = ref(null)
const selectedVaccineId = ref(null)
const selectedVaccine = ref(null)
const selectedDate = ref('')
const selectedTimeSlotId = ref(null)
const timeSlots = ref([])
const vaccines = ref([])
const activeVaccineTab = ref('')

const stepLabels = [{ title: '选择儿童' }, { title: '选择疫苗' }, { title: '选择时间' }]
const stepTitles = ['创建预约 (1/3)', '创建预约 (2/3)', '创建预约 (3/3)']
const vaccineTabs = [
  { label: '全部', value: '' },
  { label: '一类疫苗', value: 'CLASS_I' },
  { label: '二类疫苗', value: 'CLASS_II' }
]

const children = computed(() => childStore.children)

const filteredVaccines = computed(() => {
  return activeVaccineTab.value
    ? vaccines.value.filter(v => v.category === activeVaccineTab.value)
    : vaccines.value
})

const todayStr = formatLocalDate(new Date())
const maxDateStr = formatLocalDate(new Date(Date.now() + 7 * 24 * 3600 * 1000))

const canNext = computed(() => {
  if (currentStep.value === 0) return !!selectedChildId.value
  if (currentStep.value === 1) return !!selectedVaccineId.value
  return false
})

const canSubmit = computed(() => selectedChildId.value && selectedVaccineId.value && selectedDate.value && selectedTimeSlotId.value)

onShow(() => {
  childStore.fetchChildren()
  loadVaccines()
})

async function loadVaccines() {
  try {
    const data = await getVaccineList({ category: activeVaccineTab.value })
    vaccines.value = data || []
  } catch (e) {
    console.error('加载疫苗列表失败', e)
  }
}

function selectChild(child) {
  selectedChildId.value = child.id
}

function selectVaccine(vaccine) {
  selectedVaccineId.value = vaccine.id
  selectedVaccine.value = vaccine
  selectedTimeSlotId.value = null
  timeSlots.value = []
}

function onDateChange() {
  selectedTimeSlotId.value = null
  if (selectedDate.value) {
    loadTimeSlots()
  }
}

async function loadTimeSlots() {
  if (!selectedVaccineId.value || !selectedDate.value) return
  try {
    const data = await getSlotAvailability(selectedVaccineId.value, selectedDate.value)
    const slots = [
      { id: 1, label: '08:00-09:00', start: '08:00', slot: '08:00-09:00', period: 'AM' },
      { id: 2, label: '09:00-10:00', start: '09:00', slot: '09:00-10:00', period: 'AM' },
      { id: 3, label: '10:00-11:00', start: '10:00', slot: '10:00-11:00', period: 'AM' },
      { id: 4, label: '11:00-12:00', start: '11:00', slot: '11:00-12:00', period: 'AM' },
      { id: 5, label: '14:00-15:00', start: '14:00', slot: '14:00-15:00', period: 'PM' },
      { id: 6, label: '15:00-16:00', start: '15:00', slot: '15:00-16:00', period: 'PM' },
      { id: 7, label: '16:00-17:00', start: '16:00', slot: '16:00-17:00', period: 'PM' }
    ].map(slot => ({
      ...slot,
      remaining: getSlotRemaining(data, slot)
    })).map(withSlotState)
    timeSlots.value = slots
  } catch (e) {
    console.error('加载时段失败', e)
  }
}

function getSlotRemaining(data, slot) {
  if (!data) return 0
  return data[slot.slot] ?? data[slot.period] ?? 0
}

function selectTimeSlot(slot) {
  if (slot.disabled) {
    uni.showToast({ title: slot.disabledReason, icon: 'none' })
    return
  }
  selectedTimeSlotId.value = slot.id
}

function withSlotState(slot) {
  const expired = isPastSlot(slot)
  const full = slot.remaining <= 0
  return {
    ...slot,
    disabled: expired || full,
    disabledReason: expired ? '该时段已过' : '该时段已约满',
    statusText: expired ? '已过时段' : full ? '已约满' : `剩余 ${slot.remaining}`
  }
}

function isPastSlot(slot) {
  if (selectedDate.value !== todayStr) return false
  const [hour, minute] = slot.start.split(':').map(Number)
  const slotStart = new Date()
  slotStart.setHours(hour, minute, 0, 0)
  return Date.now() >= slotStart.getTime()
}

function formatLocalDate(date) {
  const year = date.getFullYear()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${year}-${month}-${day}`
}

function prevStep() { currentStep.value-- }
function nextStep() { currentStep.value++ }

async function submitAppointment() {
  const selectedSlot = timeSlots.value.find(s => s.id === selectedTimeSlotId.value)
  const latestSlot = selectedSlot ? withSlotState(selectedSlot) : null
  if (!latestSlot || latestSlot.disabled) {
    if (latestSlot) {
      timeSlots.value = timeSlots.value.map(slot => slot.id === latestSlot.id ? latestSlot : withSlotState(slot))
      selectedTimeSlotId.value = null
    }
    uni.showToast({ title: latestSlot?.disabledReason || '请选择接种时段', icon: 'none' })
    return
  }

  try {
    await create({
      childId: selectedChildId.value,
      vaccineId: selectedVaccineId.value,
      appointmentDate: selectedDate.value,
      timeSlot: latestSlot.slot
    })
    uni.showToast({ title: '预约成功', icon: 'success' })
    await loadTimeSlots()
    selectedTimeSlotId.value = null
    setTimeout(() => uni.navigateBack(), 1500)
  } catch {
    // 错误已由 interceptor 处理
  }
}

function navigateToAddChild() {
  uni.navigateTo({ url: '/pages/child/add' })
}
</script>

<style lang="scss" scoped>
.create-page {
  min-height: 100vh;
  background-color: $color-bg-page;
  padding-bottom: 140rpx;

  .step-content {
    padding: $spacing-lg;
    background-color: $color-bg-white;
    border-radius: $radius-lg;
    margin: $spacing-md;
  }

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-md;

    .section-title { font-size: $font-size-lg; font-weight: $font-weight-bold; color: $color-text-primary; }
    .section-action { font-size: $font-size-sm; color: $color-primary; }
  }

  .filter-tabs {
    display: flex;
    gap: $spacing-sm;
    margin-bottom: $spacing-md;

    .filter-tab {
      padding: $spacing-xs $spacing-md;
      font-size: $font-size-sm;
      color: $color-text-secondary;
      background-color: $color-bg-grey;
      border-radius: $radius-lg;

      &.active { color: #FFFFFF; background-color: $color-primary; }
    }
  }

  .vaccine-summary {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    padding: $spacing-md;
    background-color: $color-bg-grey;
    border-radius: $radius-md;
    margin-bottom: $spacing-md;

    .vaccine-name { font-size: $font-size-base; font-weight: $font-weight-bold; color: $color-text-primary; }
  }

  .category-tag {
    padding: 4rpx 16rpx;
    border-radius: $radius-sm;
    font-size: $font-size-xs;
  }
  .class-i { background: rgba(7,193,96,0.1); color: #07C160; }
  .class-ii { background: rgba(25,137,250,0.1); color: #1989FA; }

  .time-slots {
    display: flex;
    overflow-x: auto;
    gap: $spacing-sm;
    padding-bottom: $spacing-xs;
    white-space: nowrap;

    .time-slot {
      flex-shrink: 0;
      width: 220rpx;
      padding: $spacing-md $spacing-sm;
      background-color: $color-bg-grey;
      border: 2rpx solid $color-border-light;
      border-radius: $radius-md;
      text-align: center;

      &.selected { border-color: $color-primary; background-color: $color-primary-light; }
      &.disabled { opacity: 0.5; }

      .slot-label { display: block; font-size: $font-size-base; color: $color-text-primary; }
      .slot-remaining { display: block; font-size: $font-size-xs; color: $color-text-secondary; margin-top: $spacing-xs; &.empty { color: $color-danger; } }
    }
  }

  .bottom-bar {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    display: flex;
    gap: $spacing-md;
    padding: $spacing-md $spacing-lg;
    background-color: #FFFFFF;
    box-shadow: 0 -2rpx 8rpx rgba(0, 0, 0, 0.04);
    padding-bottom: calc(#{$spacing-md} + env(safe-area-inset-bottom));

    .btn-prev, .btn-next, .btn-submit {
      flex: 1;
      height: 88rpx;
      line-height: 88rpx;
      border: none;
      border-radius: $radius-lg;
      font-size: $font-size-lg;

      &::after { border: none; }
    }
    .btn-prev { background-color: $color-bg-grey; color: $color-text-regular; }
    .btn-next { background-color: $color-primary; color: #FFFFFF; }
    .btn-next:disabled { opacity: 0.5; }
    .btn-submit { background-color: $color-primary; color: #FFFFFF; }
    .btn-submit:disabled { opacity: 0.5; }
  }
}
</style>
