<template>
  <view class="service-page">
    <view class="nav-bar">
      <uni-icons type="back" :size="20" @click="uni.navigateBack()" />
      <text class="nav-title">配置窗口服务</text>
    </view>

    <view class="info-card">
      <view class="info-row">
        <text class="info-label">窗口编码</text>
        <text class="info-value">{{ windowCode }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">窗口名称</text>
        <text class="info-value">{{ windowName }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">职能类型</text>
        <text class="info-value">{{ functionLabel }}</text>
      </view>
    </view>

    <view class="form-section">
      <view class="form-item">
        <text class="form-label">业务名称 *</text>
        <uni-easyinput v-model="form.businessName" placeholder="请输入业务名称" />
      </view>
      <view class="form-item">
        <text class="form-label">业务描述</text>
        <uni-easyinput type="textarea" v-model="form.businessDesc" placeholder="请输入业务描述（选填）" />
      </view>
      <view class="form-item">
        <text class="form-label">业务详情说明</text>
        <uni-easyinput type="textarea" v-model="form.businessDetail" placeholder="请输入业务详情说明（选填）" />
      </view>
      <view class="form-item">
        <text class="form-label">预估办理时间（分钟）</text>
        <uni-easyinput type="number" v-model="form.estimatedTime" placeholder="请输入预估时间" />
      </view>
      <view class="form-item">
        <text class="form-label">温馨提示</text>
        <uni-easyinput type="textarea" v-model="form.tips" placeholder="温馨提示内容（选填）" />
      </view>
      <view class="form-item">
        <text class="form-label">需携带物品</text>
        <uni-easyinput type="textarea" v-model="form.requiredItems" placeholder="需携带物品说明（选填）" />
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
import { saveWindowService, getWindowService, getWindowList } from '@/api/window.js'
import { WINDOW_FUNCTION_TYPES } from '@/utils/constants.js'

const windowCode = ref('')
const windowName = ref('')
const windowFunction = ref('')
const submitting = ref(false)

const form = reactive({
  businessName: '', businessDesc: '', businessDetail: '',
  estimatedTime: '', tips: '', requiredItems: ''
})

const functionLabel = computed(() => {
  const found = WINDOW_FUNCTION_TYPES.find(t => t.value === windowFunction.value)
  return found ? found.label : windowFunction.value
})

async function handleSubmit() {
  if (!form.businessName) { uni.showToast({ title: '请输入业务名称', icon: 'none' }); return }
  submitting.value = true
  try {
    const data = {
      businessName: form.businessName,
      businessDesc: form.businessDesc || null,
      businessDetail: form.businessDetail || null,
      estimatedTime: form.estimatedTime ? parseInt(form.estimatedTime) : null,
      tips: form.tips || null,
      requiredItems: form.requiredItems || null
    }
    await saveWindowService(windowCode.value, data)
    uni.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) { /* handled by interceptor */ } finally { submitting.value = false }
}

onLoad(async (query) => {
  if (!query.windowCode) { uni.showToast({ title: '缺少窗口信息', icon: 'none' }); uni.navigateBack(); return }
  windowCode.value = query.windowCode
  try {
    const [listData, svcData] = await Promise.all([
      getWindowList(),
      getWindowService(query.windowCode)
    ])
    const list = Array.isArray(listData) ? listData : (listData.records || [])
    const found = list.find(w => w.windowCode === query.windowCode)
    if (found) {
      windowName.value = found.windowName || ''
      windowFunction.value = found.windowFunctionType || ''
    }
    if (svcData) {
      Object.assign(form, {
        businessName: svcData.businessName || '',
        businessDesc: svcData.businessDesc || '',
        businessDetail: svcData.businessDetail || '',
        estimatedTime: svcData.estimatedTime ?? '',
        tips: svcData.tips || '',
        requiredItems: svcData.requiredItems || ''
      })
    }
  } catch (e) { console.error('加载窗口服务信息失败', e) }
})
</script>

<style lang="scss" scoped>
.service-page { min-height: 100vh; padding: $spacing-lg; background: $color-bg-page; }
.nav-bar { display: flex; align-items: center; gap: $spacing-sm; margin-bottom: $spacing-lg; }
.nav-title { font-size: $font-size-lg; font-weight: 600; }
.info-card { background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md; margin-bottom: $spacing-md; box-shadow: $shadow-card; }
.info-row { display: flex; justify-content: space-between; padding: 8rpx 0; }
.info-label { font-size: $font-size-sm; color: $color-text-secondary; }
.info-value { font-size: $font-size-sm; color: $color-text-primary; font-weight: 500; }
.form-section { background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md; margin-bottom: $spacing-md; box-shadow: $shadow-card; }
.form-item { margin-bottom: $spacing-md; }
.form-label { font-size: $font-size-sm; color: $color-text-secondary; margin-bottom: $spacing-xs; display: block; }
.bottom-actions { display: flex; gap: $spacing-md; margin-top: $spacing-lg; }
.btn-cancel { flex: 1; background: transparent; border: 1rpx solid $color-border; color: $color-text-secondary; border-radius: $radius-lg; height: 88rpx; line-height: 88rpx; }
.btn-confirm { flex: 2; background: $color-primary; color: #FFF; border: none; border-radius: $radius-lg; height: 88rpx; line-height: 88rpx; }
</style>
