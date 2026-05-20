<template>
  <view class="vaccine-detail-page">
    <uni-nav-bar title="疫苗详情" :border="false" />

    <view v-if="vaccine" class="detail-content">
      <!-- 疫苗名称 + 类型 -->
      <view class="header-section">
        <text class="vaccine-name">{{ vaccine.vaccineName }}</text>
        <view class="category-tag" :class="vaccine.category === 'CLASS_I' ? 'class-i' : 'class-ii'">
          <text>{{ vaccine.category === 'CLASS_I' ? '一类·免费' : '二类·自费' }}</text>
        </view>
      </view>

      <!-- 基本信息 -->
      <view class="info-section">
        <text class="section-title">基本信息</text>
        <view class="info-row">
          <text class="info-label">生产厂家</text>
          <text class="info-value">{{ vaccine.manufacturer }}</text>
        </view>
        <view v-if="vaccine.specification" class="info-row">
          <text class="info-label">疫苗规格</text>
          <text class="info-value">{{ vaccine.specification }}</text>
        </view>
        <view v-if="vaccine.minAgeMonth != null" class="info-row">
          <text class="info-label">适用年龄</text>
          <text class="info-value">{{ vaccine.minAgeMonth }}-{{ vaccine.maxAgeMonth }}月龄</text>
        </view>
        <view v-if="vaccine.doses" class="info-row">
          <text class="info-label">接种剂次</text>
          <text class="info-value">共{{ vaccine.doses }}剂，间隔{{ vaccine.intervalDays }}天</text>
        </view>
        <view v-if="vaccine.category === 'CLASS_II' && vaccine.price" class="info-row">
          <text class="info-label">参考价格</text>
          <text class="info-value price">¥{{ vaccine.price }}</text>
        </view>
      </view>

      <!-- 疫苗说明 -->
      <view class="info-section">
        <text class="section-title">疫苗说明</text>
        <text class="description">{{ vaccine.description || '暂无说明' }}</text>
      </view>

      <!-- 底部操作 -->
      <view class="bottom-bar">
        <button class="btn-appoint" @tap="goToCreate">立即预约</button>
      </view>
    </view>

    <view v-else class="loading-tip">
      <text>加载中...</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getVaccineList } from '@/api/vaccine.js'

const vaccine = ref(null)
const vaccineId = ref(null)

onShow(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.$page?.options || currentPage.options || {}
  vaccineId.value = options.id
  if (vaccineId.value) {
    loadDetail()
  }
})

async function loadDetail() {
  try {
    const data = await getVaccineList({ id: vaccineId.value })
    const records = data.records || data || []
    vaccine.value = records.find(v => v.id == vaccineId.value) || records[0] || null
  } catch (e) {
    console.error('加载疫苗详情失败', e)
  }
}

function goToCreate() {
  uni.navigateTo({ url: `/pages/appointment/create?vaccineId=${vaccineId.value}` })
}
</script>

<style lang="scss" scoped>
.vaccine-detail-page {
  min-height: 100vh;
  background-color: $color-bg-page;
}

.detail-content {
  padding: 0 $spacing-md;
}

.header-section {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-xl $spacing-lg;
  background-color: $color-bg-white;
  border-radius: $radius-lg;
  margin: $spacing-md 0;

  .vaccine-name { font-size: $font-size-xl; font-weight: $font-weight-bold; color: $color-text-primary; flex: 1; }
}

.category-tag {
  padding: 8rpx 20rpx;
  border-radius: $radius-sm;
  font-size: $font-size-sm;
  flex-shrink: 0;
}
.class-i { background: $color-success-light; color: $color-success; }
.class-ii { background: $color-info-light; color: $color-info; }

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

.info-label { font-size: $font-size-sm; color: $color-text-secondary; }
.info-value { font-size: $font-size-sm; color: $color-text-primary; }
.info-value.price { color: $color-danger; font-weight: 600; }

.description {
  font-size: $font-size-sm;
  color: $color-text-regular;
  line-height: 1.8;
}

.bottom-bar {
  padding: $spacing-xl $spacing-md;
  padding-bottom: calc(#{$spacing-xl} + env(safe-area-inset-bottom));
}

.btn-appoint {
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

  text { font-size: $font-size-sm; color: $color-text-placeholder; }
}
</style>
