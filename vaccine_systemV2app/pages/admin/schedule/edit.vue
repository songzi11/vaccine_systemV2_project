<template>
  <view class="page">
    <view class="nav-bar">
      <uni-icons type="back" :size="20" @click="uni.navigateBack()" />
      <text class="nav-title">{{ isEdit ? '编辑排班' : '新增排班' }}</text>
    </view>

    <view class="form-section">
      <view class="form-item">
        <text class="form-label">选择医生 *</text>
        <picker :range="doctorOptions" range-key="label" @change="onDoctorChange">
          <view class="picker-value">
            <text :class="{ placeholder: !form.doctorName }">{{ form.doctorName || '请选择医生' }}</text>
            <uni-icons type="bottom" :size="14" color="#999" />
          </view>
        </picker>
      </view>

      <view class="form-item">
        <text class="form-label">选择窗口 *</text>
        <picker :range="windowOptions" range-key="label" @change="onWindowChange">
          <view class="picker-value">
            <text :class="{ placeholder: !form.windowName }">{{ form.windowName || '请选择窗口' }}</text>
            <uni-icons type="bottom" :size="14" color="#999" />
          </view>
        </picker>
      </view>

      <view class="form-item">
        <text class="form-label">排班日期 *</text>
        <picker mode="date" :start="todayStr" @change="onDateChange">
          <view class="picker-value">
            <text :class="{ placeholder: !form.scheduleDate }">{{ form.scheduleDate || '请选择日期' }}</text>
            <uni-icons type="bottom" :size="14" color="#999" />
          </view>
        </picker>
      </view>

      <view class="form-item">
        <text class="form-label">时段 *</text>
        <picker :range="timeSlotOptions" range-key="label" @change="onTimeSlotChange">
          <view class="picker-value">
            <text :class="{ placeholder: !form.timeSlot }">{{ selectedTimeSlotLabel }}</text>
            <uni-icons type="bottom" :size="14" color="#999" />
          </view>
        </picker>
      </view>

      <view class="form-item">
        <text class="form-label">容量</text>
        <uni-easyinput v-model="form.maxCapacity" type="number" placeholder="默认50" />
      </view>
    </view>

    <view class="tip-card">
      <text class="tip-title">温馨提示</text>
      <text class="tip-text">1. 同一医生同一窗口同一日期同一时段不可重复排班</text>
      <text class="tip-text">2. 时段分为上午(AM)和下午(PM)</text>
    </view>

    <view class="bottom-actions">
      <button class="btn-cancel" @click="uni.navigateBack()">取消</button>
      <button class="btn-confirm" :loading="submitting" @click="handleSubmit">保存</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { createSchedule, updateSchedule, getScheduleDetail, getDoctorList, getWindowList } from '@/api/schedule.js'

const isEdit = ref(false)
const scheduleId = ref('')
const submitting = ref(false)
const todayStr = new Date().toISOString().slice(0, 10)

const form = reactive({
  doctorId: '',
  doctorName: '',
  windowId: '',
  windowName: '',
  scheduleDate: todayStr,
  timeSlot: '',
  maxCapacity: ''
})

const timeSlotOptions = [
  { label: '上午 (AM)', value: 'AM' },
  { label: '下午 (PM)', value: 'PM' }
]

const selectedTimeSlotLabel = computed(() => {
  const found = timeSlotOptions.find(t => t.value === form.timeSlot)
  return found ? found.label : '请选择时段'
})

const doctorOptions = ref([])
const windowOptions = ref([])

function onDoctorChange(e) {
  const idx = e.detail.value
  form.doctorId = doctorOptions.value[idx]?.value || ''
  form.doctorName = doctorOptions.value[idx]?.label || ''
}

function onWindowChange(e) {
  const idx = e.detail.value
  form.windowId = windowOptions.value[idx]?.value || ''
  form.windowName = windowOptions.value[idx]?.label || ''
}

function onDateChange(e) {
  form.scheduleDate = e.detail.value
}

async function loadOptions() {
  try {
    const [doctors, windows] = await Promise.all([getDoctorList({ size: 200 }), getWindowList()])
    const doctorList = doctors.records || (Array.isArray(doctors) ? doctors : [])
    doctorOptions.value = doctorList
      .filter(u => u.realName)
      .map(u => ({ value: u.userId, label: u.realName }))
    const windowList = windows.records || (Array.isArray(windows) ? windows : [])
    windowOptions.value = windowList.map(w => ({ value: w.id, label: `${w.windowName} (${w.windowCode})` }))
  } catch (e) {
    console.error('加载选项失败', e)
  }
}

async function handleSubmit() {
  if (!form.doctorId) { uni.showToast({ title: '请选择医生', icon: 'none' }); return }
  if (!form.windowId) { uni.showToast({ title: '请选择窗口', icon: 'none' }); return }
  if (!form.scheduleDate) { uni.showToast({ title: '请选择排班日期', icon: 'none' }); return }
  if (!form.timeSlot) { uni.showToast({ title: '请选择时段', icon: 'none' }); return }

  submitting.value = true
  try {
    const data = {
      doctorId: Number(form.doctorId),
      windowId: Number(form.windowId),
      scheduleDate: form.scheduleDate,
      timeSlot: form.timeSlot,
      maxCapacity: form.maxCapacity ? Number(form.maxCapacity) : 50
    }
    if (isEdit.value) {
      await updateSchedule(scheduleId.value, data)
    } else {
      await createSchedule(data)
    }
    uni.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) {
    uni.showToast({ title: e.message || '保存失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

onLoad(async (query) => {
  await loadOptions()
  if (query.id) {
    isEdit.value = true
    scheduleId.value = query.id
    try {
      const item = await getScheduleDetail(query.id)
      if (item) {
        form.doctorId = item.doctorId
        form.doctorName = item.doctorName
        form.windowId = item.windowId
        form.windowName = item.windowName
        form.scheduleDate = item.scheduleDate
        form.timeSlot = item.timeSlot
        form.maxCapacity = item.maxCapacity || ''
      }
    } catch (e) {
      console.error('加载排班详情失败', e)
    }
  }
})
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; padding: $spacing-lg; background: $color-bg-page; padding-bottom: 140rpx; }
.nav-bar { display: flex; align-items: center; gap: $spacing-sm; margin-bottom: $spacing-lg; }
.nav-title { font-size: $font-size-lg; font-weight: 600; }

.form-section { background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md; margin-bottom: $spacing-md; box-shadow: $shadow-card; }
.form-item { margin-bottom: $spacing-md; }
.form-label { font-size: $font-size-sm; color: $color-text-secondary; margin-bottom: $spacing-xs; display: block; }
.picker-value {
  display: flex; justify-content: space-between; align-items: center;
  padding: $spacing-sm $spacing-md; border: 1rpx solid $color-border; border-radius: $radius-lg;
  font-size: $font-size-base;
  .placeholder { color: $color-text-placeholder; }
}

.tip-card { background: rgba(25,137,250,0.05); padding: $spacing-md; border-radius: $radius-lg; margin-bottom: $spacing-lg; }
.tip-title { font-size: $font-size-sm; font-weight: 600; color: $color-info; display: block; margin-bottom: $spacing-xs; }
.tip-text { font-size: $font-size-xs; color: $color-text-secondary; display: block; margin-top: 4rpx; }

.bottom-actions {
  position: fixed; bottom: 0; left: 0; right: 0;
  display: flex; gap: $spacing-md;
  padding: $spacing-md $spacing-lg;
  padding-bottom: calc(#{$spacing-md} + env(safe-area-inset-bottom));
  background: #FFFFFF;
}
.btn-cancel { flex: 1; background: transparent; border: 1rpx solid $color-border; color: $color-text-secondary; border-radius: $radius-lg; height: 88rpx; line-height: 88rpx; }
.btn-confirm { flex: 2; background: $color-primary; color: #FFF; border: none; border-radius: $radius-lg; height: 88rpx; line-height: 88rpx; }
</style>
