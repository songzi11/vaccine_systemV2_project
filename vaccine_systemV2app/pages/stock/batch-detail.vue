<template>
  <view class="page">
    <view class="nav-bar">
      <uni-icons type="back" :size="20" @click="uni.navigateBack()" />
      <text class="nav-title">批次详情</text>
    </view>

    <!-- 批次信息 -->
    <view class="info-card">
      <text class="card-title">批次信息</text>
      <view class="info-row"><text class="info-label">批次号</text><text class="info-value">{{ batch.batchNo }}</text></view>
      <view class="info-row"><text class="info-label">疫苗名称</text><text class="info-value">{{ batch.vaccineName }}</text></view>
      <view class="info-row"><text class="info-label">类型</text><text class="info-value">{{ batch.vaccineType === 'CLASS_I' ? '一类' : '二类' }}</text></view>
      <view class="info-row"><text class="info-label">厂家</text><text class="info-value">{{ batch.manufacturer }}</text></view>
      <view class="info-row"><text class="info-label">生产日期</text><text class="info-value">{{ batch.productionDate }}</text></view>
      <view class="info-row"><text class="info-label">有效期</text><text class="info-value" :class="{ expired: isExpired(batch.expiryDate) }">{{ batch.expiryDate }}</text></view>
      <view class="info-row"><text class="info-label">状态</text><text class="info-value">{{ batch.status }}</text></view>
    </view>

    <!-- 库存信息 -->
    <view class="info-card">
      <text class="card-title">库存信息</text>
      <view class="info-row"><text class="info-label">总数</text><text class="info-value">{{ batch.totalStock }}</text></view>
      <view class="info-row"><text class="info-label">可用数</text><text class="info-value">{{ batch.availableStock }}</text></view>
      <view class="info-row"><text class="info-label">锁定数</text><text class="info-value">{{ batch.lockedStock }}</text></view>
      <view class="progress-bar"><view class="progress-fill" :style="{ width: remainRatio + '%' }" /></view>
      <text class="remain-text">可用比例: {{ remainRatio }}%</text>
    </view>

    <!-- 预警信息 -->
    <view v-if="batch.alertInfo" class="alert-card">
      <text class="alert-title">预警信息</text>
      <text class="alert-type">{{ batch.alertInfo.alertType }}</text>
      <text class="alert-detail">{{ batch.alertInfo.detail }}</text>
      <text class="alert-status">处理状态: {{ batch.alertInfo.handled ? '已处理' : '未处理' }}</text>
    </view>

    <!-- 操作按钮 -->
    <view class="actions">
      <button class="btn-transfer" @click="goTransfer">调拨</button>
      <button class="btn-dispose" @click="goDispose">销毁</button>
      <button class="btn-back" @click="uni.navigateBack()">返回列表</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getBatchDetail } from '@/api/stock.js'

const batchId = ref('')
const batch = ref({})

const remainRatio = computed(() => {
  const total = batch.value.totalStock || 1
  return Math.round(((batch.value.availableStock || 0) / total) * 100)
})

function isExpired(dateStr) {
  if (!dateStr) return false
  return new Date(dateStr) < new Date()
}

onLoad(async (query) => {
  batchId.value = query.batchId || ''
  try {
    const data = await getBatchDetail(batchId.value)
    batch.value = data || {}
  } catch (e) {
    console.error('加载批次详情失败', e)
  }
})

function goTransfer() {
  uni.navigateTo({ url: `/pages/stock/transfer?batchId=${batchId.value}` })
}

function goDispose() {
  uni.navigateTo({ url: `/pages/stock/dispose?batchId=${batchId.value}` })
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; padding: $spacing-lg; background: $color-bg-page; }
.nav-bar { display: flex; align-items: center; gap: $spacing-sm; margin-bottom: $spacing-lg; }
.nav-title { font-size: $font-size-lg; font-weight: 600; }

.info-card {
  background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md;
  margin-bottom: $spacing-md; box-shadow: $shadow-card;
}
.card-title { font-size: $font-size-base; font-weight: 600; display: block; margin-bottom: $spacing-sm; }
.info-row { display: flex; justify-content: space-between; padding: $spacing-sm 0; border-bottom: 1rpx solid $color-border-light; &:last-child { border-bottom: none; } }
.info-label { font-size: $font-size-sm; color: $color-text-secondary; }
.info-value { font-size: $font-size-base; color: $color-text-primary; &.expired { color: $color-danger; } }

.progress-bar { height: 8rpx; background: $color-border-light; border-radius: 4rpx; overflow: hidden; margin-top: $spacing-sm; }
.progress-fill { height: 100%; background: $color-primary; border-radius: 4rpx; }
.remain-text { font-size: $font-size-xs; color: $color-text-placeholder; margin-top: 4rpx; display: block; }

.alert-card {
  background: rgba(238, 10, 36, 0.06); border-radius: $radius-lg; padding: $spacing-md;
  margin-bottom: $spacing-md; border-left: 4rpx solid $color-danger;
}
.alert-title { font-size: $font-size-base; font-weight: 600; color: $color-danger; display: block; margin-bottom: $spacing-xs; }
.alert-type { font-size: $font-size-sm; color: $color-text-primary; display: block; }
.alert-detail { font-size: $font-size-sm; color: $color-text-secondary; display: block; margin-top: 4rpx; }
.alert-status { font-size: $font-size-xs; color: $color-text-placeholder; display: block; margin-top: 4rpx; }

.actions { display: flex; gap: $spacing-md; margin-top: $spacing-lg; }
.btn-transfer, .btn-dispose {
  flex: 1; height: 80rpx; line-height: 80rpx;
  border-radius: $radius-lg; font-size: $font-size-base; border: none;
}
.btn-transfer { background: $color-info; color: #FFF; }
.btn-dispose { background: $color-danger; color: #FFF; }
.btn-back {
  flex: 1; height: 80rpx; line-height: 80rpx;
  background: transparent; border: 1rpx solid $color-border;
  color: $color-text-secondary; border-radius: $radius-lg; font-size: $font-size-base;
}
</style>
