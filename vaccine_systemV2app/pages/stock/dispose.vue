<template>
  <view class="page">
    <view class="nav-bar">
      <uni-icons type="back" :size="20" @click="uni.navigateBack()" />
      <text class="nav-title">疫苗销毁</text>
    </view>

    <!-- 批次信息（只读） -->
    <view class="info-card">
      <text class="card-title">批次信息</text>
      <view class="info-row"><text class="info-label">批次号</text><text class="info-value">{{ batch.batchNo }}</text></view>
      <view class="info-row"><text class="info-label">疫苗名称</text><text class="info-value">{{ batch.vaccineName }}</text></view>
      <view class="info-row"><text class="info-label">总数</text><text class="info-value">{{ batch.totalStock }}</text></view>
      <view class="info-row"><text class="info-label">可用数</text><text class="info-value">{{ batch.availableStock }}</text></view>
      <view class="info-row"><text class="info-label">锁定数</text><text class="info-value">{{ batch.lockedStock }}</text></view>
    </view>

    <!-- 销毁表单 -->
    <view class="section">
      <text class="section-title">销毁信息</text>

      <view class="form-item">
        <text class="form-label">销毁原因 *</text>
        <picker :range="reasonOptions" range-key="label" @change="onReasonChange">
          <view class="picker-value">
            <text :class="{ placeholder: !form.disposeReason }">{{ selectedReasonLabel }}</text>
            <uni-icons type="bottom" :size="14" color="#999" />
          </view>
        </picker>
      </view>

      <view class="form-item">
        <text class="form-label">销毁数量 *</text>
        <uni-easyinput v-model="form.disposeQuantity" type="number" :placeholder="quantityPlaceholder" />
      </view>

      <view class="form-item">
        <text class="form-label">备注</text>
        <uni-easyinput v-model="form.remark" type="textarea" placeholder="选填" />
      </view>
    </view>

    <!-- 操作 -->
    <view class="bottom-bar">
      <button class="btn-cancel" @click="uni.navigateBack()">取消</button>
      <button class="btn-confirm" :loading="submitting" @click="handleSubmit">确认销毁</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getBatchDetail, disposeBatch } from '@/api/stock.js'

const batchId = ref('')
const batch = ref({})
const submitting = ref(false)

const quantityPlaceholder = computed(() => `最大可用数: ${batch.value.availableStock || 0}`)

const reasonOptions = [
  { label: '过期', value: 'EXPIRED' },
  { label: '损坏', value: 'DAMAGED' },
  { label: '质量问题', value: 'QUALITY_ISSUE' },
  { label: '其他', value: 'OTHER' }
]

const form = reactive({
  disposeReason: '',
  disposeQuantity: '',
  remark: ''
})

const selectedReasonLabel = computed(() => {
  const found = reasonOptions.find(r => r.value === form.disposeReason)
  return found ? found.label : '请选择销毁原因'
})

onLoad(async (query) => {
  batchId.value = query.batchId || ''
  try {
    const data = await getBatchDetail(batchId.value)
    batch.value = data || {}
  } catch (e) {
    console.error('加载批次信息失败', e)
  }
})

function onReasonChange(e) { form.disposeReason = reasonOptions[e.detail.value]?.value || '' }

async function handleSubmit() {
  if (!form.disposeReason) { uni.showToast({ title: '请选择销毁原因', icon: 'none' }); return }
  const qty = Number(form.disposeQuantity)
  if (!qty || qty <= 0) { uni.showToast({ title: '请输入销毁数量', icon: 'none' }); return }
  if (qty > (batch.value.availableStock || 0)) { uni.showToast({ title: '数量不能超过可用数', icon: 'none' }); return }

  uni.showModal({
    title: '确认销毁',
    content: `确认销毁 ${batch.value.vaccineName} ${qty} 支？此操作不可撤销。`,
    success: async (res) => {
      if (!res.confirm) return
      submitting.value = true
      try {
        await disposeBatch(batchId.value, {
          quantity: qty,
          reason: form.disposeReason,
          remark: form.remark
        })
        uni.showToast({ title: '销毁成功', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 1500)
      } catch (e) {
        uni.showToast({ title: e.message || '销毁失败', icon: 'none' })
      } finally {
        submitting.value = false
      }
    }
  })
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
