<template>
  <view class="page">
    <view class="nav-bar">
      <uni-icons type="back" :size="20" @click="uni.navigateBack()" />
      <text class="nav-title">签到确认</text>
    </view>

    <!-- 预约信息 -->
    <InfoCard title="预约信息" :fields="appointmentFields" />
    <!-- 儿童信息 -->
    <InfoCard title="儿童信息" :fields="childFields" />

    <!-- 身份核验（选填） -->
    <view class="verify-section">
      <text class="section-label">身份证号（选填）</text>
      <uni-easyinput v-model="idCard" placeholder="请输入儿童身份证号" type="idcard" maxlength="18" />
    </view>

    <!-- 操作按钮 -->
    <view class="bottom-actions">
      <button class="btn-cancel" @click="uni.navigateBack()">取消</button>
      <button class="btn-confirm" :loading="submitting" @click="handleSubmit">确认签到</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { executeSignin } from '@/api/signin.js'
import InfoCard from '@/components/InfoCard/InfoCard.vue'
import { formatTimeSlot } from '@/utils/format.js'

const appointmentId = ref('')
const childName = ref('')
const vaccineName = ref('')
const appointmentDate = ref('')
const timeSlot = ref('')
const appointmentNo = ref('')
const idCard = ref('')
const submitting = ref(false)

// uni-app 全局生命周期（onLoad）无需从 Vue 导入
onLoad((query) => {
  appointmentId.value = query.appointmentId || ''
  childName.value = query.childName ? decodeURIComponent(query.childName) : ''
  vaccineName.value = query.vaccineName ? decodeURIComponent(query.vaccineName) : ''
  appointmentDate.value = query.appointmentDate || ''
  timeSlot.value = query.timeSlot || ''
  appointmentNo.value = query.appointmentNo || ''
})

const appointmentFields = computed(() => [
  { label: '预约号', value: appointmentNo.value },
  { label: '疫苗', value: vaccineName.value },
  { label: '日期', value: appointmentDate.value },
  { label: '时段', value: formatTimeSlot(timeSlot.value) }
])

const childFields = computed(() => [
  { label: '姓名', value: childName.value }
])

async function handleSubmit() {
  submitting.value = true
  try {
    await executeSignin({ appointmentId: Number(appointmentId.value) })
    uni.showToast({ title: '签到成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) {
    // 错误已在request拦截器处理
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; padding: $spacing-lg; background: $color-bg-page; }
.nav-bar { display: flex; align-items: center; gap: $spacing-sm; margin-bottom: $spacing-lg; }
.nav-title { font-size: $font-size-lg; font-weight: 600; }
.verify-section { margin-bottom: $spacing-lg; }
.section-label { font-size: $font-size-base; font-weight: 600; margin-bottom: $spacing-sm; display: block; }
.bottom-actions { display: flex; gap: $spacing-md; margin-top: $spacing-xl; }
.btn-cancel {
  flex: 1; background: transparent; border: 1rpx solid $color-border;
  color: $color-text-secondary; border-radius: $radius-lg; height: 88rpx; line-height: 88rpx;
}
.btn-confirm {
  flex: 2; background: $color-primary; color: #FFFFFF; border: none;
  border-radius: $radius-lg; height: 88rpx; line-height: 88rpx;
}
</style>
