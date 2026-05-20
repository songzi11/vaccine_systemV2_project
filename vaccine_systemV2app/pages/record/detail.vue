<template>
  <view class="page">
    <view class="nav-bar">
      <uni-icons type="back" :size="20" @click="uni.navigateBack()" />
      <text class="nav-title">接种记录详情</text>
    </view>

    <!-- 接种信息 -->
    <view class="info-card">
      <text class="card-title">接种信息</text>
      <view class="info-row"><text class="info-label">注射号</text><text class="info-value">{{ record.injectionId }}</text></view>
      <view class="info-row"><text class="info-label">注射部位</text><text class="info-value">{{ record.injectionSite }}</text></view>
      <view class="info-row"><text class="info-label">接种时间</text><text class="info-value">{{ record.injectionTime }}</text></view>
      <view class="info-row"><text class="info-label">接种医生</text><text class="info-value">{{ record.doctorName }}</text></view>
    </view>

    <!-- 儿童信息 -->
    <view class="info-card">
      <text class="card-title" @click="goChildHistory">儿童信息</text>
      <view class="info-row"><text class="info-label">姓名</text><text class="info-value">{{ record.childName }}</text></view>
      <view class="info-row"><text class="info-label">性别</text><text class="info-value">{{ record.childGender }}</text></view>
      <view class="info-row"><text class="info-label">出生日期</text><text class="info-value">{{ record.childBirthDate }}</text></view>
    </view>

    <!-- 疫苗信息 -->
    <view class="info-card">
      <text class="card-title">疫苗信息</text>
      <view class="info-row"><text class="info-label">疫苗名称</text><text class="info-value">{{ record.vaccineName }}</text></view>
      <view class="info-row"><text class="info-label">批次号</text><text class="info-value">{{ record.batchNo }}</text></view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getRecordDetail } from '@/api/record.js'

const record = ref({})

onLoad(async (query) => {
  const id = query.id
  if (!id) return
  try {
    const data = await getRecordDetail(id)
    record.value = data || {}
  } catch (e) {
    console.error('加载记录详情失败', e)
  }
})

function goChildHistory() {
  if (record.value.childId) {
    uni.navigateTo({
      url: `/pages/record/child-history?childId=${record.value.childId}&childName=${encodeURIComponent(record.value.childName || '')}`
    })
  }
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; padding: $spacing-lg; background: $color-bg-page; }
.nav-bar { display: flex; align-items: center; gap: $spacing-sm; margin-bottom: $spacing-lg; }
.nav-title { font-size: $font-size-lg; font-weight: 600; }

.info-card {
  background: $color-bg-white;
  border-radius: $radius-lg;
  padding: $spacing-md;
  margin-bottom: $spacing-md;
  box-shadow: $shadow-card;
}

.card-title { font-size: $font-size-base; font-weight: 600; color: $color-text-primary; display: block; margin-bottom: $spacing-sm; }

.info-row {
  display: flex; justify-content: space-between;
  padding: $spacing-sm 0;
  border-bottom: 1rpx solid $color-border-light;
  &:last-child { border-bottom: none; }
}

.info-label { font-size: $font-size-sm; color: $color-text-secondary; }
.info-value { font-size: $font-size-base; color: $color-text-primary; }
</style>
