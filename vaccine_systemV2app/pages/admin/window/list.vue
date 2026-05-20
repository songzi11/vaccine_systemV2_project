<template>
  <view class="window-list-page">
    <view class="card-list">
      <view v-for="item in windowList" :key="item.id" class="window-card">
        <view class="card-header">
          <text class="window-code">{{ item.windowCode }}</text>
          <view class="status-toggle" :class="item.status === 0 ? 'active' : 'paused'" @tap.stop="toggleStatus(item)">
            <text class="toggle-text">{{ item.status === 0 ? '正常服务' : '暂停服务' }}</text>
          </view>
        </view>
        <view class="card-body">
          <text class="window-name">{{ item.windowName }}</text>
          <view class="card-tags">
            <text class="tag">{{ functionTypeText(item.windowFunctionType) }}</text>
            <text v-if="item.avgHandleTime" class="tag">{{ item.avgHandleTime }}分钟/人</text>
          </view>
          <view class="doctor-row" @tap="showDoctorPicker(item)">
            <text class="doctor-label">值班医生</text>
            <text v-if="item.doctorName" class="doctor-name">{{ item.doctorName }}</text>
            <text v-else class="doctor-name empty">点击分配</text>
            <text class="doctor-arrow">›</text>
          </view>
        </view>
        <view class="card-actions">
          <text class="action-text" @tap.stop="goToService(item)">配置服务</text>
          <text class="action-text" @tap.stop="goToEdit(item)">修改</text>
          <text class="action-text danger" @tap.stop="handleDelete(item)">删除</text>
        </view>
      </view>
      <EmptyState v-if="!loading && windowList.length === 0" icon="list" title="暂无窗口记录" />
    </view>

    <view class="fab-btn" @tap="goToAdd">
      <text class="fab-icon">+</text>
    </view>

    <!-- 医生选择弹窗 -->
    <view v-if="doctorPickerVisible" class="picker-mask" @tap="doctorPickerVisible = false">
      <view class="picker-popup" @tap.stop>
        <text class="picker-title">{{ pickerWindow.windowName }} - 分配医生</text>
        <text class="picker-subtitle">分配后医生将获得该窗口的对应权限</text>
        <view v-for="doc in doctorList" :key="doc.userId" class="picker-option" @tap="handleAssignDoctor(doc)">
          <text class="option-text">{{ doc.realName }}（{{ maskPhone(doc.phone) }}）</text>
          <text v-if="pickerWindow.doctorName === doc.realName" class="option-check">✓</text>
        </view>
        <view v-if="pickerWindow.doctorId" class="picker-option" @tap="handleRemoveDoctor">
          <text class="option-text option-clear">清除当前医生（设为后勤）</text>
        </view>
        <view class="picker-cancel" @tap="doctorPickerVisible = false">
          <text class="cancel-text">取消</text>
        </view>
      </view>
    </view>

    <CustomTabBar />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getWindowList, deleteWindow as deleteWindowApi, updateWindow, assignDoctorToWindow, removeDoctorFromWindow } from '@/api/window.js'
import { getUserList } from '@/api/user-manage.js'
import { WINDOW_FUNCTION_TYPES } from '@/utils/constants.js'
import EmptyState from '@/components/EmptyState/EmptyState.vue'
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue'

const windowList = ref([])
const loading = ref(false)
const doctorPickerVisible = ref(false)
const pickerWindow = ref({})
const doctorList = ref([])

function functionTypeText(type) {
  const found = WINDOW_FUNCTION_TYPES.find(t => t.value === type)
  return found ? found.label : type || ''
}

function maskPhone(phone) {
  if (!phone || phone.length !== 11) return phone || ''
  return phone.slice(0, 3) + '****' + phone.slice(7)
}

onShow(() => { loadWindows() })

async function loadWindows() {
  loading.value = true
  try {
    const data = await getWindowList()
    windowList.value = Array.isArray(data) ? data : (data.records || [])
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function goToAdd() { uni.navigateTo({ url: '/pages/admin/window/edit' }) }
function goToEdit(item) { uni.navigateTo({ url: `/pages/admin/window/edit?id=${item.id}` }) }
function goToService(item) { uni.navigateTo({ url: `/pages/admin/window/service?windowCode=${item.windowCode}` }) }

async function toggleStatus(item) {
  const newStatus = item.status === 0 ? 1 : 0
  const label = newStatus === 0 ? '恢复服务' : '暂停服务'
  uni.showModal({
    title: label,
    content: `确定要${label}「${item.windowName}」吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await updateWindow(item.id, { status: newStatus })
          uni.showToast({ title: '操作成功', icon: 'success' })
          loadWindows()
        } catch (e) {}
      }
    }
  })
}

function handleDelete(item) {
  uni.showModal({
    title: '确认删除',
    content: `确定要删除窗口「${item.windowName}」吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteWindowApi(item.id)
          uni.showToast({ title: '删除成功', icon: 'success' })
          loadWindows()
        } catch (e) {}
      }
    }
  })
}

async function showDoctorPicker(window) {
  pickerWindow.value = window
  try {
    const data = await getUserList({ page: 1, size: 100 })
    doctorList.value = (data.records || data || []).filter(u =>
      u.status !== '冻结' && u.roleCodes && u.roleCodes.some(r => r.startsWith('DOCTOR_'))
    )
  } catch (e) {
    doctorList.value = []
  }
  doctorPickerVisible.value = true
}

async function handleAssignDoctor(doc) {
  doctorPickerVisible.value = false
  try {
    await assignDoctorToWindow(pickerWindow.value.id, doc.userId)
    uni.showToast({ title: `已分配${doc.realName}`, icon: 'success' })
    loadWindows()
  } catch (e) {}
}

async function handleRemoveDoctor() {
  if (!pickerWindow.value.doctorId) {
    doctorPickerVisible.value = false
    return
  }
  doctorPickerVisible.value = false
  try {
    await removeDoctorFromWindow(pickerWindow.value.id)
    uni.showToast({ title: '已清除', icon: 'success' })
    loadWindows()
  } catch (e) {}
}
</script>

<style lang="scss" scoped>
.window-list-page { min-height: 100vh; background: $color-bg-page; padding: $spacing-lg; padding-bottom: 140rpx; }
.card-list { margin-top: $spacing-md; }
.window-card {
  background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md;
  margin-bottom: $spacing-md; box-shadow: $shadow-card;
}
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: $spacing-sm; }
.window-code { font-size: $font-size-base; font-weight: 600; color: $color-text-primary; }
.status-toggle { padding: 6rpx 20rpx; border-radius: 20rpx; }
.status-toggle.active { background: rgba(7,193,96,0.1); }
.status-toggle.active .toggle-text { color: #07C160; }
.status-toggle.paused { background: rgba(255,153,0,0.1); }
.status-toggle.paused .toggle-text { color: #FF9900; }
.toggle-text { font-size: $font-size-xs; font-weight: 500; }
.status-toggle:active { opacity: 0.7; }
.card-body { margin-bottom: $spacing-sm; }
.window-name { font-size: $font-size-base; color: $color-text-primary; display: block; margin-bottom: $spacing-xs; }
.card-tags { display: flex; flex-wrap: wrap; gap: $spacing-xs; }
.doctor-row {
  display: flex; align-items: center; gap: $spacing-sm;
  margin-top: $spacing-sm; padding: $spacing-sm 0;
  border-top: 1rpx solid $color-border-light;
}
.doctor-row:active { opacity: 0.7; }
.doctor-label { font-size: $font-size-xs; color: $color-text-placeholder; }
.doctor-name { font-size: $font-size-sm; color: $color-primary; font-weight: 500; flex: 1; }
.doctor-name.empty { color: $color-text-placeholder; }
.doctor-arrow { font-size: $font-size-lg; color: $color-text-placeholder; }
.tag { padding: 2rpx 12rpx; font-size: $font-size-xs; background: $color-bg-grey; border-radius: $radius-sm; color: $color-text-secondary; }
.card-actions {
  display: flex; justify-content: flex-end; gap: $spacing-md;
  padding-top: $spacing-sm; border-top: 1rpx solid $color-border-light;
}
.action-text { font-size: $font-size-sm; color: $color-text-secondary; }
.action-text.danger { color: $color-danger; }
.fab-btn {
  position: fixed; right: $spacing-lg; bottom: 160rpx; width: 100rpx; height: 100rpx;
  background: $color-primary; border-radius: 50%; display: flex; align-items: center;
  justify-content: center; box-shadow: $shadow-lg;
}
.fab-icon { font-size: 56rpx; color: #FFFFFF; }

.picker-mask {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5); z-index: 999;
  display: flex; align-items: flex-end;
}
.picker-popup {
  width: 100%; background: #FFFFFF; border-radius: 24rpx 24rpx 0 0;
  padding: $spacing-lg;
  padding-bottom: calc(#{$spacing-lg} + env(safe-area-inset-bottom));
  max-height: 70vh;
  overflow-y: auto;
}
.picker-title {
  font-size: $font-size-base; font-weight: 600; color: $color-text-primary;
  text-align: center; display: block; margin-bottom: $spacing-xs;
}
.picker-subtitle {
  font-size: $font-size-xs; color: $color-text-placeholder;
  text-align: center; display: block; margin-bottom: $spacing-md;
}
.picker-option {
  display: flex; align-items: center; padding: $spacing-md 0;
  border-bottom: 1rpx solid $color-border-light;
}
.picker-option:last-of-type { border-bottom: none; }
.picker-option:active { opacity: 0.7; }
.option-text { flex: 1; font-size: $font-size-base; color: $color-text-primary; }
.option-clear { color: $color-danger; }
.option-check { font-size: $font-size-lg; color: $color-primary; }
.picker-cancel {
  margin-top: $spacing-md; padding: $spacing-md 0;
  text-align: center; border-top: 1rpx solid $color-border-light;
}
.cancel-text { font-size: $font-size-base; color: $color-text-secondary; }
</style>
