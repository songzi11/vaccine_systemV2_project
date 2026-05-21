<template>
  <view class="publish-page">
    <view class="nav-bar">
      <uni-icons type="back" :size="20" @click="uni.navigateBack()" />
      <text class="nav-title">发布公告</text>
    </view>

    <view class="form-section">
      <view class="form-item">
        <text class="form-label">公告标题 *</text>
        <uni-easyinput v-model="form.title" placeholder="请输入公告标题" />
      </view>
      <view class="form-item">
        <text class="form-label">公告内容 *</text>
        <uni-easyinput type="textarea" v-model="form.content" placeholder="请输入公告内容" :inputBorder="false" />
      </view>
      <view class="form-item">
        <text class="form-label">公告类型 *</text>
        <picker :range="typeOptions" range-key="label" @change="onTypeChange">
          <view class="picker-value">{{ typeLabel || '请选择公告类型' }}</view>
        </picker>
      </view>
      <view class="form-item" v-if="form.noticeType === NOTICE_TYPE.PERSONAL">
        <text class="form-label">目标用户ID *</text>
        <uni-easyinput v-model="form.targetUserId" placeholder="请输入目标用户ID" type="number" />
      </view>
      <view class="form-item">
        <text class="form-label">生效时间 *</text>
        <uni-datetime-picker type="date" v-model="form.startTime" :clear-icon="false" />
      </view>
      <view class="form-item">
        <text class="form-label">失效时间 *</text>
        <uni-datetime-picker type="date" v-model="form.endTime" :clear-icon="false" />
      </view>
    </view>

    <view class="bottom-actions">
      <button class="btn-cancel" @click="uni.navigateBack()">取消</button>
      <button class="btn-confirm" :loading="submitting" @click="handleSubmit">提交审批</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { publishNotice } from '@/api/notice-manage.js'
import { NOTICE_TYPE, NOTICE_TYPE_TEXT } from '@/utils/constants.js'

const submitting = ref(false)
const typeOptions = [
  { value: NOTICE_TYPE.SYSTEM, label: NOTICE_TYPE_TEXT[NOTICE_TYPE.SYSTEM] },
  { value: NOTICE_TYPE.PERSONAL, label: NOTICE_TYPE_TEXT[NOTICE_TYPE.PERSONAL] },
  { value: NOTICE_TYPE.INTERNAL, label: NOTICE_TYPE_TEXT[NOTICE_TYPE.INTERNAL] }
]

const form = reactive({
  title: '', content: '', noticeType: '',
  startTime: '', endTime: '', targetUserId: ''
})

const typeLabel = computed(() => {
  const found = typeOptions.find(t => t.value === form.noticeType)
  return found ? found.label : ''
})

function onTypeChange(e) {
  form.noticeType = typeOptions[e.detail.value]?.value || ''
}

async function handleSubmit() {
  if (!form.title) { uni.showToast({ title: '请输入公告标题', icon: 'none' }); return }
  if (!form.content) { uni.showToast({ title: '请输入公告内容', icon: 'none' }); return }
  if (!form.noticeType) { uni.showToast({ title: '请选择公告类型', icon: 'none' }); return }
  if (form.noticeType === NOTICE_TYPE.PERSONAL && !form.targetUserId) { uni.showToast({ title: '请输入目标用户ID', icon: 'none' }); return }
  if (!form.startTime || !form.endTime) { uni.showToast({ title: '请选择生效/失效时间', icon: 'none' }); return }
  if (form.startTime >= form.endTime) { uni.showToast({ title: '失效时间须晚于生效时间', icon: 'none' }); return }

  submitting.value = true
  const payload = { ...form }
  if (payload.noticeType !== NOTICE_TYPE.PERSONAL) delete payload.targetUserId
  else payload.targetUserId = Number(payload.targetUserId)
  try {
    await publishNotice(payload)
    uni.showToast({ title: '已提交审批', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) { /* handled by interceptor */ } finally { submitting.value = false }
}
</script>

<style lang="scss" scoped>
.publish-page { min-height: 100vh; padding: $spacing-lg; background: $color-bg-page; }
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
