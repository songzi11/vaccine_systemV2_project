<template>
  <view class="detail-page">
    <uni-nav-bar title="预约详情" :border="false" />

    <view v-if="detail" class="detail-content">
      <!-- 状态卡片 -->
      <view class="status-card" :style="{ backgroundColor: statusColor }">
        <text class="status-text">{{ statusText }}</text>
        <text class="vaccine-name">{{ detail.vaccineName }}</text>
        <!-- 待签到：提示去预检窗口 -->
        <view v-if="detail.status === 1" class="window-info">
          <text class="window-text">{{ detail.windowName || '预检窗口' }}</text>
          <text v-if="detail.doctorName" class="doctor-text">值班医生：{{ detail.doctorName }}</text>
          <text v-else class="doctor-text">到达后请前往预检窗口</text>
        </view>
        <!-- 预检通过：提示去接种窗口 -->
        <view v-else-if="detail.status === 7" class="window-info">
          <text class="window-text">{{ detail.windowName || '接种窗口' }}</text>
          <text v-if="detail.doctorName" class="doctor-text">值班医生：{{ detail.doctorName }}</text>
          <text v-else class="doctor-text">请前往接种窗口等候</text>
        </view>
        <!-- 其他进行中：显示具体窗口和医生 -->
        <view v-else-if="detail.windowName" class="window-info">
          <text class="window-text">请前往 {{ detail.windowName }}</text>
          <text v-if="detail.doctorName" class="doctor-text">值班医生：{{ detail.doctorName }}</text>
        </view>
      </view>

      <!-- 预约信息 -->
      <view class="info-section">
        <text class="section-title">预约信息</text>
        <view class="info-row">
          <text class="info-label">预约编号</text>
          <text class="info-value">{{ detail.id }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">预约日期</text>
          <text class="info-value">{{ detail.appointmentDate }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">预约时段</text>
          <text class="info-value">{{ formatTimeSlot(detail.timeSlot) }}</text>
        </view>
      </view>

      <!-- 儿童信息 -->
      <view class="info-section">
        <text class="section-title">儿童信息</text>
        <view class="info-row">
          <text class="info-label">姓名</text>
          <text class="info-value">{{ detail.childName }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">性别</text>
          <text class="info-value">{{ detail.childGender === 1 ? '男' : '女' }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">出生日期</text>
          <text class="info-value">{{ detail.childBirthDate }}</text>
        </view>
      </view>

      <!-- 疫苗信息 -->
      <view class="info-section">
        <text class="section-title">疫苗信息</text>
        <view class="info-row">
          <text class="info-label">疫苗名称</text>
          <text class="info-value">{{ detail.vaccineName }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">疫苗类型</text>
          <text class="info-value">{{ detail.vaccineCategory === 'CLASS_I' ? '一类·免费' : '二类·自费' }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">厂家</text>
          <text class="info-value">{{ detail.manufacturer }}</text>
        </view>
      </view>

      <!-- 底部操作 -->
      <view class="bottom-actions">
        <button v-if="canCancel" class="btn-cancel" @tap="handleCancel">取消预约</button>
        <button v-if="canViewGuide" class="btn-guide" @tap="goToGuide">查看流程指引</button>
      </view>
    </view>

    <view v-else-if="loadError" class="error-tip">
      <text class="error-text">{{ loadError }}</text>
      <button class="retry-btn" @tap="loadDetail">重试</button>
    </view>

    <view v-else class="loading-tip">
      <text>加载中...</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getAppointmentDetail, cancelAppointment } from '@/api/appointment.js'
import { APPOINTMENT_STATUS_TEXT } from '@/utils/constants.js'
import { formatTimeSlot } from '@/utils/format.js'

const detail = ref(null)
const appointmentId = ref(null)
const loadError = ref('')

const statusText = computed(() => APPOINTMENT_STATUS_TEXT[detail.value?.status] || '未知')

const statusColor = computed(() => {
  const status = detail.value?.status
  if (status === 1) return '#07C160'   // 待签到
  if (status === 6) return '#1989FA'   // 预检中
  if (status === 7) return '#FF9900'   // 接种中
  if (status === 10) return '#07C160'  // 已完成
  if (status === 9) return '#EE0A24'   // 已取消
  return '#999999'
})

const canCancel = computed(() => {
  const status = detail.value?.status
  return status === 1 // 待签到
})

const canViewGuide = computed(() => {
  const status = detail.value?.status
  return [1, 6, 7, 10].includes(status)
})

onShow(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.$page?.options || currentPage.options || {}
  appointmentId.value = options.id
  if (appointmentId.value) {
    loadDetail()
  }
})

async function loadDetail() {
  loadError.value = ''
  try {
    detail.value = await getAppointmentDetail(appointmentId.value)
  } catch (e) {
    console.error('加载预约详情失败', e)
    loadError.value = e.message || '加载失败，请重试'
  }
}

function handleCancel() {
  uni.showModal({
    title: '确认取消',
    content: '确定要取消该预约吗？取消后需重新预约。',
    success: async (res) => {
      if (res.confirm) {
        try {
          // 乐观更新
          const oldStatus = detail.value.status
          detail.value.status = 9
          await cancelAppointment(appointmentId.value)
          uni.showToast({ title: '已取消', icon: 'success' })
        } catch {
          // 回滚
          await loadDetail()
        }
      }
    }
  })
}

function goToGuide() {
  uni.setStorageSync('guideAppointmentId', appointmentId.value)
  uni.navigateTo({ url: `/pages/appointment/guide?id=${appointmentId.value}` })
}
</script>

<style lang="scss" scoped>
.detail-page {
  min-height: 100vh;
  background-color: $color-bg-page;
}

.detail-content {
  padding: 0 $spacing-md;
}

.status-card {
  padding: $spacing-xl;
  border-radius: $radius-lg;
  text-align: center;
  margin: $spacing-md 0;

  .status-text {
    display: block;
    font-size: $font-size-xl;
    font-weight: $font-weight-bold;
    color: #FFFFFF;
    margin-bottom: $spacing-sm;
  }

  .vaccine-name {
    display: block;
    font-size: $font-size-base;
    color: rgba(255, 255, 255, 0.9);
  }

  .window-info {
    margin-top: $spacing-md;
    padding-top: $spacing-md;
    border-top: 1rpx solid rgba(255, 255, 255, 0.3);

    .window-text {
      display: block;
      font-size: $font-size-lg;
      font-weight: $font-weight-bold;
      color: #FFFFFF;
    }

    .doctor-text {
      display: block;
      font-size: $font-size-sm;
      color: rgba(255, 255, 255, 0.85);
      margin-top: 4rpx;
    }
  }
}

.info-section {
  background-color: $color-bg-white;
  border-radius: $radius-lg;
  padding: $spacing-lg;
  margin-bottom: $spacing-md;
}

.section-title {
  display: block;
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
  color: $color-text-primary;
  margin-bottom: $spacing-md;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-sm 0;
  border-bottom: 1rpx solid $color-border-light;

  &:last-child { border-bottom: none; }
}

.info-label {
  font-size: $font-size-sm;
  color: $color-text-secondary;
}

.info-value {
  font-size: $font-size-sm;
  color: $color-text-primary;
}

.bottom-actions {
  padding: $spacing-xl $spacing-md;
  padding-bottom: calc(#{$spacing-xl} + env(safe-area-inset-bottom));
}

.btn-cancel {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background-color: #FFFFFF;
  color: $color-danger;
  font-size: $font-size-lg;
  border: 2rpx solid $color-danger;
  border-radius: $radius-lg;
  margin-bottom: $spacing-md;

  &::after { border: none; }
}

.btn-guide {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background-color: $color-primary;
  color: #FFFFFF;
  font-size: $font-size-lg;
  border: none;
  border-radius: $radius-lg;

  &::after { border: none; }
}

.loading-tip {
  text-align: center;
  padding: 120rpx;

  text {
    font-size: $font-size-sm;
    color: $color-text-placeholder;
  }
}

.error-tip {
  text-align: center;
  padding: 120rpx 48rpx;

  .error-text {
    display: block;
    font-size: $font-size-base;
    color: $color-text-secondary;
    margin-bottom: $spacing-lg;
  }

  .retry-btn {
    width: 240rpx;
    height: 72rpx;
    line-height: 72rpx;
    background-color: $color-primary;
    color: #FFFFFF;
    font-size: $font-size-base;
    border: none;
    border-radius: $radius-round;

    &::after { border: none; }
  }
}
</style>
