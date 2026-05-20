<template>
  <view class="page">
    <view class="nav-bar">
      <uni-icons type="back" :size="20" @click="uni.navigateBack()" />
      <text class="nav-title">预检评估</text>
      <text class="nav-subtitle">{{ childName }} / {{ vaccineName }}</text>
    </view>

    <!-- 体征录入 -->
    <view class="section">
      <text class="section-title">体征录入</text>
      <view class="form-item">
        <text class="form-label">体温 * <text v-if="form.bodyTemperature > 37.3" class="temp-warning">（偏高！）</text></text>
        <uni-easyinput v-model="form.bodyTemperature" placeholder="请输入体温" type="digit" />
      </view>
      <view class="form-row">
        <view class="form-item half">
          <text class="form-label">体重（kg）</text>
          <uni-easyinput v-model="form.weight" placeholder="选填" type="digit" />
        </view>
        <view class="form-item half">
          <text class="form-label">身高（cm）</text>
          <uni-easyinput v-model="form.height" placeholder="选填" type="digit" />
        </view>
      </view>
    </view>

    <!-- 健康评估 -->
    <view class="section">
      <text class="section-title">健康评估</text>
      <view class="radio-group">
        <text class="form-label">健康状况 *</text>
        <view class="radio-options">
          <view v-for="opt in healthOptions" :key="opt.value" class="radio-item" :class="{ active: form.healthStatus === opt.value }" @click="form.healthStatus = opt.value">
            <text>{{ opt.label }}</text>
          </view>
        </view>
      </view>
      <view class="form-item">
        <text class="form-label">过敏史</text>
        <uni-easyinput v-model="form.allergyHistory" placeholder="无" />
      </view>
      <view class="form-item">
        <text class="form-label">近期用药</text>
        <uni-easyinput v-model="form.medicationRecent" placeholder="无" />
      </view>
      <view class="form-item">
        <text class="form-label">疾病史</text>
        <uni-easyinput v-model="form.diseaseHistory" placeholder="无" />
      </view>
      <view class="form-item">
        <text class="form-label">近期接种史</text>
        <uni-easyinput v-model="form.vaccinationRecent" placeholder="无" />
      </view>
    </view>

    <!-- 预检结果 -->
    <view class="section">
      <text class="section-title">预检结果 *</text>
      <view class="result-options">
        <view class="result-btn pass" :class="{ active: form.checkResult === 'PASS' }" @click="form.checkResult = 'PASS'">通过</view>
        <view class="result-btn fail" :class="{ active: form.checkResult === 'FAIL' }" @click="form.checkResult = 'FAIL'">不通过</view>
      </view>
      <view v-if="form.checkResult === 'FAIL'" class="form-item">
        <text class="form-label">不通过原因 *</text>
        <uni-easyinput v-model="form.failReason" type="textarea" placeholder="请输入不通过原因" />
      </view>
    </view>

    <!-- 操作 -->
    <view class="bottom-actions">
      <button class="btn-cancel" @click="uni.navigateBack()">取消</button>
      <button class="btn-confirm" :loading="submitting" @click="handleSubmit">提交评估</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { executePrecheck } from '@/api/precheck.js'
import { TEMPERATURE_THRESHOLD } from '@/utils/constants.js'

const appointmentId = ref('')
const childName = ref('')
const vaccineName = ref('')
const submitting = ref(false)

const healthOptions = [
  { label: '良好', value: 'GOOD' },
  { label: '一般', value: 'GENERAL' },
  { label: '较差', value: 'POOR' }
]

const form = reactive({
  bodyTemperature: '',
  weight: '',
  height: '',
  healthStatus: 'GOOD',
  allergyHistory: '无',
  medicationRecent: '无',
  diseaseHistory: '无',
  vaccinationRecent: '无',
  checkResult: 'PASS',
  failReason: ''
})

// uni-app 全局生命周期（onLoad）无需从 Vue 导入
onLoad((query) => {
  appointmentId.value = query.appointmentId || ''
  childName.value = query.childName ? decodeURIComponent(query.childName) : ''
  vaccineName.value = query.vaccineName ? decodeURIComponent(query.vaccineName) : ''
})

async function handleSubmit() {
  if (!form.bodyTemperature) {
    uni.showToast({ title: '请输入体温', icon: 'none' })
    return
  }
  const temp = parseFloat(form.bodyTemperature)
  if (temp > TEMPERATURE_THRESHOLD && form.checkResult !== 'FAIL') {
    uni.showModal({
      title: '体温异常',
      content: `当前体温 ${form.bodyTemperature}°C，超过 ${TEMPERATURE_THRESHOLD}°C 阈值，建议标记为不通过`,
      success: (res) => {
        if (res.confirm) form.checkResult = 'FAIL'
      }
    })
    return
  }
  if (form.checkResult === 'FAIL' && !form.failReason) {
    uni.showToast({ title: '请输入不通过原因', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    await executePrecheck({
      appointmentId: Number(appointmentId.value),
      bodyTemperature: temp,
      weight: form.weight ? parseFloat(form.weight) : undefined,
      height: form.height ? parseFloat(form.height) : undefined,
      healthStatus: form.healthStatus,
      allergyHistory: form.allergyHistory,
      medicationRecent: form.medicationRecent,
      diseaseHistory: form.diseaseHistory,
      vaccinationRecent: form.vaccinationRecent,
      result: form.checkResult,
      failReason: form.failReason || undefined
    })
    uni.showToast({ title: '评估完成', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) { /* handled by interceptor */ } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; padding: $spacing-lg; background: $color-bg-page; }
.nav-bar { display: flex; flex-direction: column; margin-bottom: $spacing-lg; }
.nav-title { font-size: $font-size-lg; font-weight: 600; }
.nav-subtitle { font-size: $font-size-sm; color: $color-text-secondary; margin-top: $spacing-xs; }
.section { background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md; margin-bottom: $spacing-md; }
.section-title { font-size: $font-size-base; font-weight: 600; margin-bottom: $spacing-md; }
.form-item { margin-bottom: $spacing-md; }
.form-label { font-size: $font-size-sm; color: $color-text-secondary; margin-bottom: $spacing-xs; display: block; }
.temp-warning { color: $color-danger; font-weight: 600; }
.form-row { display: flex; gap: $spacing-md; }
.half { flex: 1; }
.radio-options { display: flex; gap: $spacing-sm; margin-top: $spacing-xs; }
.radio-item {
  padding: 12rpx $spacing-md; border-radius: $radius-lg;
  border: 1rpx solid $color-border; font-size: $font-size-sm;
  &.active { border-color: $color-primary; background: $color-primary-light; color: $color-primary; }
}
.result-options { display: flex; gap: $spacing-md; margin-bottom: $spacing-md; }
.result-btn {
  flex: 1; text-align: center; padding: 20rpx; border-radius: $radius-lg;
  border: 1rpx solid $color-border; font-size: $font-size-base;
  &.pass.active { border-color: $color-success; background: rgba(7,193,96,0.1); color: $color-success; }
  &.fail.active { border-color: $color-danger; background: rgba(238,10,36,0.1); color: $color-danger; }
}
.bottom-actions { display: flex; gap: $spacing-md; margin-top: $spacing-lg; }
.btn-cancel { flex: 1; background: transparent; border: 1rpx solid $color-border; color: $color-text-secondary; border-radius: $radius-lg; height: 88rpx; line-height: 88rpx; }
.btn-confirm { flex: 2; background: $color-primary; color: #FFF; border: none; border-radius: $radius-lg; height: 88rpx; line-height: 88rpx; }
</style>
