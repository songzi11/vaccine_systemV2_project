<template>
  <view class="window-edit-page">
    <view class="nav-bar">
      <uni-icons type="back" :size="20" @click="uni.navigateBack()" />
      <text class="nav-title">{{ isEdit ? '编辑窗口' : '新增窗口' }}</text>
    </view>

    <view class="form-section">
      <view class="form-item">
        <text class="form-label">窗口编码 *</text>
        <uni-easyinput v-model="form.windowCode" placeholder="如 SIGNIN_01" :disabled="isEdit" />
      </view>
      <view class="form-item">
        <text class="form-label">窗口名称 *</text>
        <uni-easyinput v-model="form.windowName" placeholder="请输入窗口名称" />
      </view>
      <view class="form-item">
        <text class="form-label">职能类型 *</text>
        <picker :range="functionTypeOptions" range-key="label" @change="onFunctionTypeChange">
          <view class="picker-value">{{ functionTypeLabel || '请选择职能类型' }}</view>
        </picker>
      </view>
      <view class="form-item">
        <text class="form-label">平均处理时间（分钟）</text>
        <uni-easyinput type="number" v-model="form.avgHandleTime" placeholder="默认5分钟" />
      </view>
      <view class="form-item">
        <text class="form-label">排序</text>
        <uni-easyinput type="number" v-model="form.sortOrder" placeholder="排序号，越小越靠前" />
      </view>
      <view v-if="isEdit" class="form-item">
        <text class="form-label">状态</text>
        <picker :range="statusOptions" range-key="label" @change="onStatusChange">
          <view class="picker-value">{{ statusLabel || '请选择状态' }}</view>
        </picker>
      </view>
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
import { createWindow, updateWindow, getWindowList } from '@/api/window.js'
import { WINDOW_FUNCTION_TYPES } from '@/utils/constants.js'

const isEdit = ref(false)
const windowId = ref('')
const submitting = ref(false)
const functionTypeOptions = WINDOW_FUNCTION_TYPES
const statusOptions = [
  { value: 0, label: '开放' },
  { value: 1, label: '关闭' }
]

const form = reactive({
  windowCode: '', windowName: '', windowFunctionType: '',
  avgHandleTime: '', sortOrder: '', status: 0
})

const functionTypeLabel = computed(() => {
  const found = WINDOW_FUNCTION_TYPES.find(t => t.value === form.windowFunctionType)
  return found ? found.label : ''
})

const statusLabel = computed(() => {
  const found = statusOptions.find(t => t.value === form.status)
  return found ? found.label : ''
})

function onFunctionTypeChange(e) {
  form.windowFunctionType = WINDOW_FUNCTION_TYPES[e.detail.value]?.value || ''
}

function onStatusChange(e) {
  form.status = statusOptions[e.detail.value]?.value ?? 0
}

async function handleSubmit() {
  if (!form.windowCode) { uni.showToast({ title: '请输入窗口编码', icon: 'none' }); return }
  if (!form.windowName) { uni.showToast({ title: '请输入窗口名称', icon: 'none' }); return }
  if (!form.windowFunctionType) { uni.showToast({ title: '请选择职能类型', icon: 'none' }); return }

  submitting.value = true
  try {
    const data = {
      windowCode: form.windowCode,
      windowName: form.windowName,
      windowFunctionType: form.windowFunctionType,
      avgHandleTime: form.avgHandleTime ? parseInt(form.avgHandleTime) : null,
      sortOrder: form.sortOrder ? parseInt(form.sortOrder) : null
    }
    if (isEdit.value) {
      data.status = form.status
      await updateWindow(windowId.value, data)
    } else {
      await createWindow(data)
    }
    uni.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) { /* handled by interceptor */ } finally { submitting.value = false }
}

onLoad(async (query) => {
  if (query.id) {
    isEdit.value = true
    windowId.value = query.id
    try {
      const data = await getWindowList()
      const list = Array.isArray(data) ? data : (data.records || [])
      const found = list.find(w => String(w.id) === String(query.id))
      if (found) {
        Object.assign(form, {
          windowCode: found.windowCode || '',
          windowName: found.windowName || '',
          windowFunctionType: found.windowFunctionType || '',
          avgHandleTime: found.avgHandleTime ?? '',
          sortOrder: found.sortOrder ?? '',
          status: Number(found.status) || 0
        })
      }
    } catch (e) { console.error('加载窗口详情失败', e) }
  }
})
</script>

<style lang="scss" scoped>
.window-edit-page { min-height: 100vh; padding: $spacing-lg; background: $color-bg-page; }
.nav-bar { display: flex; align-items: center; gap: $spacing-sm; margin-bottom: $spacing-lg; }
.nav-title { font-size: $font-size-lg; font-weight: 600; }
.form-section { background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md; margin-bottom: $spacing-md; box-shadow: $shadow-card; }
.form-item { margin-bottom: $spacing-md; }
.form-label { font-size: $font-size-sm; color: $color-text-secondary; margin-bottom: $spacing-xs; display: block; }
.picker-value { font-size: $font-size-base; color: $color-text-primary; padding: 12rpx $spacing-sm; background: $color-bg-grey; border-radius: $radius-sm; }
.bottom-actions { display: flex; gap: $spacing-md; margin-top: $spacing-lg; }
.btn-cancel { flex: 1; background: transparent; border: 1rpx solid $color-border; color: $color-text-secondary; border-radius: $radius-lg; height: 88rpx; line-height: 88rpx; }
.btn-confirm { flex: 2; background: $color-primary; color: #FFF; border: none; border-radius: $radius-lg; height: 88rpx; line-height: 88rpx; }
</style>
