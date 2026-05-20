<template>
  <view class="page">
    <view class="nav-bar">
      <uni-icons type="back" :size="20" @click="uni.navigateBack()" />
      <text class="nav-title">不良反应上报</text>
    </view>

    <!-- 基本信息 -->
    <view class="info-card">
      <text class="card-title">基本信息</text>
      <view class="info-row"><text class="info-label">儿童姓名</text><text class="info-value">{{ childName }}</text></view>
      <view class="info-row"><text class="info-label">疫苗名称</text><text class="info-value">{{ vaccineName }}</text></view>
    </view>

    <!-- 反应信息 -->
    <view class="section">
      <text class="section-title">反应信息</text>

      <view class="form-item">
        <text class="form-label">反应类型 *</text>
        <picker :range="reactionTypes" range-key="label" @change="onReactionTypeChange">
          <view class="picker-value">
            <text :class="{ placeholder: !form.reactionType }">{{ selectedReactionLabel }}</text>
            <uni-icons type="bottom" :size="14" color="#999" />
          </view>
        </picker>
      </view>

      <view class="form-item">
        <text class="form-label">严重程度 *</text>
        <view class="radio-options">
          <view v-for="opt in severityOptions" :key="opt.value" class="radio-item" :class="{ active: form.severity === opt.value }" @click="form.severity = opt.value">
            <text>{{ opt.label }}</text>
          </view>
        </view>
      </view>

      <view class="form-item">
        <text class="form-label">发生时间 *</text>
        <uni-datetime-picker type="datetime" v-model="form.occurTime" :clear-icon="false">
          <view class="picker-value">
            <text :class="{ placeholder: !form.occurTime }">{{ form.occurTime || '请选择时间' }}</text>
            <uni-icons type="calendar" :size="14" color="#999" />
          </view>
        </uni-datetime-picker>
      </view>

      <view class="form-item">
        <text class="form-label">详细描述 *</text>
        <uni-easyinput v-model="form.description" type="textarea" placeholder="请描述不良反应的具体表现" :maxlength="500" />
      </view>

      <view class="form-item">
        <text class="form-label">处理措施</text>
        <uni-easyinput v-model="form.handleMeasures" type="textarea" placeholder="选填" />
      </view>

      <view class="form-item">
        <text class="form-label">处理结果</text>
        <picker :range="handleResults" range-key="label" @change="onHandleResultChange">
          <view class="picker-value">
            <text :class="{ placeholder: !form.handleResult }">{{ selectedHandleResultLabel }}</text>
            <uni-icons type="bottom" :size="14" color="#999" />
          </view>
        </picker>
      </view>
    </view>

    <!-- 操作 -->
    <view class="bottom-bar">
      <button class="btn-cancel" @click="uni.navigateBack()">取消</button>
      <button class="btn-confirm" :loading="submitting" @click="handleSubmit">提交上报</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { reportAdverse } from '@/api/observe.js'
import { ADVERSE_REACTION_TYPES, SEVERITY_LEVELS, HANDLE_RESULTS } from '@/utils/constants.js'

const appointmentId = ref('')
const childName = ref('')
const vaccineName = ref('')
const submitting = ref(false)

const reactionTypes = ADVERSE_REACTION_TYPES
const severityOptions = SEVERITY_LEVELS
const handleResults = HANDLE_RESULTS

const form = reactive({
  reactionType: '',
  severity: '',
  occurTime: '',
  description: '',
  handleMeasures: '',
  handleResult: ''
})

onLoad((query) => {
  appointmentId.value = query.appointmentId || ''
  childName.value = query.childName ? decodeURIComponent(query.childName) : ''
  vaccineName.value = query.vaccineName ? decodeURIComponent(query.vaccineName) : ''
})

const selectedReactionLabel = computed(() => {
  const found = reactionTypes.find(r => r.value === form.reactionType)
  return found ? found.label : '请选择反应类型'
})

const selectedHandleResultLabel = computed(() => {
  const found = handleResults.find(r => r.value === form.handleResult)
  return found ? found.label : '请选择处理结果'
})

function onReactionTypeChange(e) {
  form.reactionType = reactionTypes[e.detail.value]?.value || ''
}

function onHandleResultChange(e) {
  form.handleResult = handleResults[e.detail.value]?.value || ''
}

async function handleSubmit() {
  if (!form.reactionType) { uni.showToast({ title: '请选择反应类型', icon: 'none' }); return }
  if (!form.severity) { uni.showToast({ title: '请选择严重程度', icon: 'none' }); return }
  if (!form.occurTime) { uni.showToast({ title: '请选择发生时间', icon: 'none' }); return }
  if (!form.description) { uni.showToast({ title: '请填写详细描述', icon: 'none' }); return }

  submitting.value = true
  try {
    await reportAdverse({
      appointmentId: Number(appointmentId.value),
      ...form
    })
    uni.showToast({ title: '上报成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) {
    uni.showToast({ title: e.message || '上报失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; padding: $spacing-lg; background: $color-bg-page; padding-bottom: 140rpx; }
.nav-bar { display: flex; align-items: center; gap: $spacing-sm; margin-bottom: $spacing-lg; }
.nav-title { font-size: $font-size-lg; font-weight: 600; }

.info-card {
  background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md;
  margin-bottom: $spacing-md; box-shadow: $shadow-card;
}
.card-title { font-size: $font-size-base; font-weight: 600; display: block; margin-bottom: $spacing-sm; }
.info-row { display: flex; justify-content: space-between; padding: $spacing-sm 0; }
.info-label { font-size: $font-size-sm; color: $color-text-secondary; }
.info-value { font-size: $font-size-base; color: $color-text-primary; }

.section { background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md; margin-bottom: $spacing-md; }
.section-title { font-size: $font-size-base; font-weight: 600; margin-bottom: $spacing-md; display: block; }

.form-item { margin-bottom: $spacing-md; }
.form-label { font-size: $font-size-sm; color: $color-text-secondary; margin-bottom: $spacing-xs; display: block; }

.picker-value {
  display: flex; justify-content: space-between; align-items: center;
  padding: $spacing-sm $spacing-md; border: 1rpx solid $color-border; border-radius: $radius-lg;
  font-size: $font-size-base;
  .placeholder { color: $color-text-placeholder; }
}

.radio-options { display: flex; gap: $spacing-sm; margin-top: $spacing-xs; }
.radio-item {
  flex: 1; text-align: center; padding: 16rpx; border-radius: $radius-lg;
  border: 1rpx solid $color-border; font-size: $font-size-sm;
  &.active { border-color: $color-primary; background: $color-primary-light; color: $color-primary; }
}

.bottom-bar {
  position: fixed; bottom: 0; left: 0; right: 0;
  display: flex; gap: $spacing-md;
  padding: $spacing-md $spacing-lg;
  padding-bottom: calc(#{$spacing-md} + env(safe-area-inset-bottom));
  background: #FFFFFF;
}
.btn-cancel { flex: 1; background: transparent; border: 1rpx solid $color-border; color: $color-text-secondary; border-radius: $radius-lg; height: 88rpx; line-height: 88rpx; }
.btn-confirm { flex: 2; background: $color-danger; color: #FFF; border: none; border-radius: $radius-lg; height: 88rpx; line-height: 88rpx; }
</style>
