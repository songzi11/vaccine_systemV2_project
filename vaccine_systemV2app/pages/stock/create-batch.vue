<template>
  <view class="page">
    <view class="nav-bar">
      <uni-icons type="back" :size="20" @click="uni.navigateBack()" />
      <text class="nav-title">新建批次入库</text>
    </view>

    <!-- 入库表单 -->
    <view class="section">
      <text class="section-title">疫苗信息</text>

      <view class="form-item">
        <text class="form-label">选择疫苗 *</text>
        <picker :range="vaccineOptions" range-key="label" @change="onVaccineChange">
          <view class="picker-value">
            <text :class="{ placeholder: !form.vaccineId }">{{ selectedVaccineLabel }}</text>
            <uni-icons type="bottom" :size="14" color="#999" />
          </view>
        </picker>
      </view>

      <view class="form-item">
        <text class="form-label">批次号 *</text>
        <uni-easyinput v-model="form.batchNo" placeholder="请输入批次号" />
      </view>

      <view class="form-item">
        <text class="form-label">生产厂家</text>
        <uni-easyinput v-model="form.manufacturer" placeholder="选填" />
      </view>
    </view>

    <view class="section">
      <text class="section-title">日期与数量</text>

      <view class="form-item">
        <text class="form-label">生产日期</text>
        <picker mode="date" @change="onProductionDateChange">
          <view class="picker-value">
            <text :class="{ placeholder: !form.productionDate }">{{ form.productionDate || '请选择生产日期' }}</text>
            <uni-icons type="bottom" :size="14" color="#999" />
          </view>
        </picker>
      </view>

      <view class="form-item">
        <text class="form-label">有效期 *</text>
        <picker mode="date" :start="todayStr" @change="onExpiryDateChange">
          <view class="picker-value">
            <text :class="{ placeholder: !form.expiryDate }">{{ form.expiryDate || '请选择有效期' }}</text>
            <uni-icons type="bottom" :size="14" color="#999" />
          </view>
        </picker>
      </view>

      <view class="form-item">
        <text class="form-label">预警天数</text>
        <uni-easyinput v-model="form.warningDays" type="number" placeholder="默认30天" />
      </view>

      <view class="form-item">
        <text class="form-label">入库总数 *</text>
        <uni-easyinput v-model="form.quantity" type="number" placeholder="请输入入库总数" />
      </view>
    </view>

    <!-- 操作 -->
    <view class="bottom-bar">
      <button class="btn-cancel" @click="uni.navigateBack()">取消</button>
      <button class="btn-confirm" :loading="submitting" @click="handleSubmit">确认入库</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getVaccineList } from '@/api/vaccine.js'
import { createBatch } from '@/api/stock.js'

const vaccineOptions = ref([])
const submitting = ref(false)
const todayStr = new Date().toISOString().split('T')[0]

const form = reactive({
  vaccineId: '',
  batchNo: '',
  manufacturer: '',
  productionDate: '',
  expiryDate: '',
  warningDays: '',
  quantity: ''
})

const selectedVaccineLabel = computed(() => {
  const found = vaccineOptions.value.find(v => v.value === form.vaccineId)
  return found ? found.label : '请选择疫苗'
})

onMounted(async () => {
  try {
    const data = await getVaccineList({ size: 200 })
    const list = data.records || (Array.isArray(data) ? data : [])
    vaccineOptions.value = list.map(v => ({
      value: v.id,
      label: `${v.vaccineName}（${v.category === 'CLASS_I' ? '一类' : '二类'}）`
    }))
  } catch (e) {
    console.error('加载疫苗列表失败', e)
  }
})

function onVaccineChange(e) { form.vaccineId = vaccineOptions.value[e.detail.value]?.value || '' }
function onProductionDateChange(e) { form.productionDate = e.detail.value }
function onExpiryDateChange(e) { form.expiryDate = e.detail.value }

async function handleSubmit() {
  if (!form.vaccineId) { uni.showToast({ title: '请选择疫苗', icon: 'none' }); return }
  if (!form.batchNo.trim()) { uni.showToast({ title: '请输入批次号', icon: 'none' }); return }
  if (!form.expiryDate) { uni.showToast({ title: '请选择有效期', icon: 'none' }); return }
  const qty = Number(form.quantity)
  if (!qty || qty <= 0) { uni.showToast({ title: '请输入有效的入库总数', icon: 'none' }); return }

  submitting.value = true
  try {
    await createBatch({
      vaccineId: Number(form.vaccineId),
      batchNo: form.batchNo.trim(),
      manufacturer: form.manufacturer || null,
      productionDate: form.productionDate || null,
      expiryDate: form.expiryDate,
      warningDays: form.warningDays ? Number(form.warningDays) : 30,
      quantity: qty
    })
    uni.showToast({ title: '入库成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) {
    uni.showToast({ title: e.message || '入库失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; padding: $spacing-lg; background: $color-bg-page; padding-bottom: 140rpx; }
.nav-bar { display: flex; align-items: center; gap: $spacing-sm; margin-bottom: $spacing-lg; }
.nav-title { font-size: $font-size-lg; font-weight: 600; }

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
.btn-confirm { flex: 2; background: $color-primary; color: #FFF; border: none; border-radius: $radius-lg; height: 88rpx; line-height: 88rpx; }
</style>
