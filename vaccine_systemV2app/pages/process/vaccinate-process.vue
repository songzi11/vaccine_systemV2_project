<template>
  <view class="vaccinate-process">
    <view class="nav-bar">
      <uni-icons type="back" :size="20" @click="uni.navigateBack()" />
      <text class="nav-title">疫苗接种</text>
    </view>

    <!-- 预约信息 -->
    <InfoCard title="预约信息" :fields="appointmentFields" />
    <!-- 儿童信息 -->
    <InfoCard title="儿童信息" :fields="childFields" />
    <!-- 预检结果 -->
    <InfoCard title="预检结果" :fields="precheckFields" />
    <!-- 批次信息 -->
    <InfoCard title="批次信息" :fields="batchFields" />

    <!-- 注射部位选择 -->
    <view class="section">
      <text class="section-title">注射部位 *</text>
      <view class="site-grid">
        <view v-for="site in injectionSites" :key="site.value" class="site-btn" :class="{ active: selectedSite === site.value }" @click="selectedSite = site.value">
          <text>{{ site.label }}</text>
        </view>
      </view>
    </view>

    <!-- 温馨提示 -->
    <view class="tip-section">
      <text class="tip-text">请核对疫苗名称、确认儿童身份、检查有效期后再执行接种</text>
    </view>

    <!-- 操作按钮 -->
    <view class="bottom-bar">
      <button class="btn-cancel" @click="uni.navigateBack()">取消</button>
      <button class="btn-confirm" :loading="submitting" @click="handleConfirm">执行接种</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { verifyInfo, executeVaccinate } from '@/api/vaccinate.js'
import { INJECTION_SITES } from '@/utils/constants.js'
import { formatTimeSlot } from '@/utils/format.js'
import InfoCard from '@/components/InfoCard/InfoCard.vue'

const queueId = ref('')
const appointmentId = ref('')
const batchId = ref('')
const childName = ref('')
const vaccineName = ref('')
const selectedSite = ref('')
const submitting = ref(false)
const injectionSites = INJECTION_SITES

onLoad((query) => {
  queueId.value = query.queueId || ''
  appointmentId.value = query.appointmentId || ''
  childName.value = query.childName ? decodeURIComponent(query.childName) : ''
  vaccineName.value = query.vaccineName ? decodeURIComponent(query.vaccineName) : ''
  loadVerifyInfo()
})

const appointmentFields = ref([])
const childFields = ref([])
const precheckFields = ref([])
const batchFields = ref([])

async function loadVerifyInfo() {
  try {
    const data = await verifyInfo(appointmentId.value)
    batchId.value = data.batchId || ''
    appointmentFields.value = [
      { label: '预约号', value: data.appointmentNo || '' },
      { label: '日期', value: data.appointmentDate || '' },
      { label: '时段', value: formatTimeSlot(data.timeSlot) },
      { label: '疫苗', value: data.vaccineName || '' }
    ]
    childFields.value = [
      { label: '姓名', value: data.childName || '' },
      { label: '性别', value: data.childGender === 1 ? '男' : data.childGender === 2 ? '女' : '未知' },
      { label: '出生日期', value: data.childBirthDate || '' }
    ]
    precheckFields.value = [
      { label: '体温', value: data.bodyTemperature != null ? data.bodyTemperature + '°C' : '' },
      { label: '健康状况', value: data.healthStatus || '' }
    ]
    batchFields.value = [
      { label: '批次号', value: data.batchNo || '' },
      { label: '厂家', value: data.manufacturer || '' },
      { label: '有效期', value: data.expiryDate || '' },
      { label: '总数', value: String(data.totalStock || 0) },
      { label: '可用数', value: String(data.availableStock || 0) },
      { label: '锁定数', value: String(data.lockedStock || 0) }
    ]
  } catch (e) {
    console.error('加载核验信息失败', e)
  }
}

function isExpired(dateStr) {
  if (!dateStr) return false
  return new Date(dateStr) < new Date()
}

function handleConfirm() {
  if (!selectedSite.value) {
    uni.showToast({ title: '请选择注射部位', icon: 'none' })
    return
  }
  uni.showModal({
    title: '确认接种',
    content: `确认对 ${childName.value} 接种 ${vaccineName.value}？`,
    success: async (res) => {
      if (res.confirm) await doVaccinate()
    }
  })
}

async function doVaccinate() {
  submitting.value = true
  try {
    const result = await executeVaccinate({
      appointmentId: Number(appointmentId.value),
      batchId: Number(batchId.value),
      injectionSite: selectedSite.value
    })
    uni.showToast({ title: '接种成功', icon: 'success' })
    setTimeout(() => {
      uni.redirectTo({
        url: `/pages/process/vaccinate-success?injectionNo=${encodeURIComponent(result.injectionNo || '')}&site=${encodeURIComponent(selectedSite.value)}&batchNo=${encodeURIComponent(batchFields.value.find(f => f.label === '批次号')?.value || '')}&childName=${encodeURIComponent(childName.value)}`
      })
    }, 1000)
  } catch (e) {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.vaccinate-process {
  min-height: 100vh;
  background: $color-bg-page;
  padding: $spacing-lg;
  padding-bottom: 140rpx;
}

.nav-bar { display: flex; align-items: center; gap: $spacing-sm; margin-bottom: $spacing-lg; }
.nav-title { font-size: $font-size-lg; font-weight: 600; }

.section {
  background: $color-bg-white;
  border-radius: $radius-lg;
  padding: $spacing-md;
  margin-bottom: $spacing-md;
}

.section-title { font-size: $font-size-base; font-weight: 600; margin-bottom: $spacing-md; }

.site-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-sm;
}

.site-btn {
  text-align: center;
  padding: 20rpx;
  border-radius: $radius-lg;
  border: 1rpx solid $color-border;
  font-size: $font-size-base;
  &.active { border-color: $color-primary; background: $color-primary-light; color: $color-primary; }
}

.tip-section {
  padding: $spacing-md;
  background: rgba(255, 153, 0, 0.08);
  border-radius: $radius-lg;
  margin-bottom: $spacing-md;
}

.tip-text { font-size: $font-size-sm; color: $color-warning; }

.bottom-bar {
  position: fixed;
  bottom: 0; left: 0; right: 0;
  display: flex; gap: $spacing-md;
  padding: $spacing-md $spacing-lg;
  padding-bottom: calc(#{$spacing-md} + env(safe-area-inset-bottom));
  background: #FFFFFF;
}

.btn-cancel { flex: 1; background: transparent; border: 1rpx solid $color-border; color: $color-text-secondary; border-radius: $radius-lg; height: 88rpx; line-height: 88rpx; }
.btn-confirm { flex: 2; background: $color-danger; color: #FFF; border: none; border-radius: $radius-lg; height: 88rpx; line-height: 88rpx; font-weight: 600; }
</style>
