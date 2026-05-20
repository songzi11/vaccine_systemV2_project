<template>
  <view class="vaccine-edit-page">
    <view class="nav-bar">
      <uni-icons type="back" :size="20" @click="uni.navigateBack()" />
      <text class="nav-title">{{ isEdit ? '编辑疫苗' : '新增疫苗' }}</text>
    </view>

    <view class="form-section">
      <text class="section-title">基本信息</text>
      <view class="form-item">
        <text class="form-label">疫苗编码 *</text>
        <uni-easyinput v-model="form.vaccineCode" placeholder="请输入疫苗编码" :disabled="isEdit" />
      </view>
      <view class="form-item">
        <text class="form-label">疫苗名称 *</text>
        <uni-easyinput v-model="form.vaccineName" placeholder="请输入疫苗名称" />
      </view>
      <view class="form-item">
        <text class="form-label">疫苗类别 *</text>
        <picker :range="['一类疫苗', '二类疫苗']" @change="onCategoryChange">
          <view class="picker-value">{{ categoryLabel || '请选择疫苗类别' }}</view>
        </picker>
      </view>
      <view class="form-item">
        <text class="form-label">生产厂家</text>
        <uni-easyinput v-model="form.manufacturer" placeholder="请输入生产厂家（选填）" />
      </view>
      <view class="form-item">
        <text class="form-label">规格</text>
        <uni-easyinput v-model="form.specification" placeholder="请输入规格（选填）" />
      </view>
      <view class="form-item">
        <text class="form-label">状态 *</text>
        <picker :range="['上架', '下架']" @change="onStatusChange">
          <view class="picker-value">{{ form.shelfStatus === 0 ? '上架' : form.shelfStatus === 1 ? '下架' : '请选择状态' }}</view>
        </picker>
      </view>
    </view>

    <view class="form-section">
      <text class="section-title">接种规则</text>
      <view class="form-row">
        <view class="form-item half">
          <text class="form-label">最小月龄 *</text>
          <uni-easyinput type="number" v-model="form.minAge" placeholder="月" />
        </view>
        <view class="form-item half">
          <text class="form-label">最大月龄 *</text>
          <uni-easyinput type="number" v-model="form.maxAge" placeholder="月" />
        </view>
      </view>
      <view class="form-row">
        <view class="form-item half">
          <text class="form-label">接种剂次 *</text>
          <uni-easyinput type="number" v-model="form.doses" placeholder="次" />
        </view>
        <view class="form-item half">
          <text class="form-label">接种间隔（天） *</text>
          <uni-easyinput type="number" v-model="form.intervalDays" placeholder="天" />
        </view>
      </view>
    </view>

    <view class="form-section">
      <text class="section-title">疫苗说明</text>
      <view class="form-item">
        <text class="form-label">描述</text>
        <uni-easyinput type="textarea" v-model="form.description" placeholder="疫苗描述（选填）" />
      </view>
      <view class="form-item">
        <text class="form-label">接种程序说明</text>
        <uni-easyinput type="textarea" v-model="form.programDesc" placeholder="接种程序说明（选填）" />
      </view>
      <view class="form-item">
        <text class="form-label">禁忌症说明</text>
        <uni-easyinput type="textarea" v-model="form.contraindications" placeholder="禁忌症说明（选填）" />
      </view>
      <view class="form-item">
        <text class="form-label">不良反应说明</text>
        <uni-easyinput type="textarea" v-model="form.adverseReactions" placeholder="不良反应说明（选填）" />
      </view>
    </view>

    <view class="bottom-actions">
      <button class="btn-cancel" @click="uni.navigateBack()">取消</button>
      <button class="btn-confirm" :loading="submitting" @click="handleSubmit">保存</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { createVaccine, updateVaccine, getVaccineList } from '@/api/vaccine-manage.js'

const isEdit = ref(false)
const vaccineId = ref('')
const submitting = ref(false)

const form = reactive({
  vaccineCode: '', vaccineName: '', category: '', manufacturer: '',
  specification: '', shelfStatus: 0, minAge: '', maxAge: '',
  doses: '', intervalDays: '', description: '', programDesc: '',
  contraindications: '', adverseReactions: ''
})

const categoryLabel = computed(() => {
  if (form.category === 'CLASS_I') return '一类疫苗'
  if (form.category === 'CLASS_II') return '二类疫苗'
  return ''
})

function onCategoryChange(e) {
  form.category = e.detail.value === 0 ? 'CLASS_I' : 'CLASS_II'
}

function onStatusChange(e) {
  form.shelfStatus = parseInt(e.detail.value)
}

async function handleSubmit() {
  if (!form.vaccineCode) { uni.showToast({ title: '请输入疫苗编码', icon: 'none' }); return }
  if (!form.vaccineName) { uni.showToast({ title: '请输入疫苗名称', icon: 'none' }); return }
  if (!form.category) { uni.showToast({ title: '请选择疫苗类别', icon: 'none' }); return }
  const minAge = parseInt(form.minAge)
  const maxAge = parseInt(form.maxAge)
  if (isNaN(minAge) || isNaN(maxAge) || minAge > maxAge) { uni.showToast({ title: '月龄范围不合法', icon: 'none' }); return }
  if (!form.doses || parseInt(form.doses) <= 0) { uni.showToast({ title: '剂次须大于0', icon: 'none' }); return }
  if (!form.intervalDays || parseInt(form.intervalDays) <= 0) { uni.showToast({ title: '间隔天数须大于0', icon: 'none' }); return }

  submitting.value = true
  try {
    const data = { ...form, minAge, maxAge, doses: parseInt(form.doses), intervalDays: parseInt(form.intervalDays) }
    if (isEdit.value) await updateVaccine(vaccineId.value, data)
    else await createVaccine(data)
    uni.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) { /* handled by interceptor */ } finally { submitting.value = false }
}

onLoad(async (query) => {
  if (query.id) {
    isEdit.value = true
    vaccineId.value = query.id
    try {
      const res = await getVaccineList({ id: query.id })
      if (res.records?.length) {
        const item = res.records[0]
        Object.assign(form, {
          vaccineCode: item.vaccineCode, vaccineName: item.vaccineName,
          category: item.category, manufacturer: item.manufacturer || '',
          specification: item.specification || '', shelfStatus: item.shelfStatus,
          minAge: item.minAge, maxAge: item.maxAge,
          doses: item.doses, intervalDays: item.intervalDays,
          description: item.description || '', programDesc: item.programDesc || '',
          contraindications: item.contraindications || '', adverseReactions: item.adverseReactions || ''
        })
      }
    } catch (e) { console.error('加载疫苗详情失败', e) }
  }
})
</script>

<style lang="scss" scoped>
.vaccine-edit-page { min-height: 100vh; padding: $spacing-lg; background: $color-bg-page; }
.nav-bar { display: flex; align-items: center; gap: $spacing-sm; margin-bottom: $spacing-lg; }
.nav-title { font-size: $font-size-lg; font-weight: 600; }
.form-section { background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md; margin-bottom: $spacing-md; box-shadow: $shadow-card; }
.section-title { font-size: $font-size-base; font-weight: 600; margin-bottom: $spacing-md; display: block; }
.form-item { margin-bottom: $spacing-md; }
.form-label { font-size: $font-size-sm; color: $color-text-secondary; margin-bottom: $spacing-xs; display: block; }
.picker-value { font-size: $font-size-base; color: $color-text-primary; padding: 12rpx $spacing-sm; background: $color-bg-grey; border-radius: $radius-sm; }
.form-row { display: flex; gap: $spacing-md; }
.half { flex: 1; }
.bottom-actions { display: flex; gap: $spacing-md; margin-top: $spacing-lg; }
.btn-cancel { flex: 1; background: transparent; border: 1rpx solid $color-border; color: $color-text-secondary; border-radius: $radius-lg; height: 88rpx; line-height: 88rpx; }
.btn-confirm { flex: 2; background: $color-primary; color: #FFF; border: none; border-radius: $radius-lg; height: 88rpx; line-height: 88rpx; }
</style>
