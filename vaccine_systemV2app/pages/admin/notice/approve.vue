<template>
  <view class="approve-page">
    <view class="nav-bar">
      <uni-icons type="back" :size="20" @click="uni.navigateBack()" />
      <text class="nav-title">{{ isReadonly ? '公告详情' : '审批公告' }}</text>
    </view>

    <view class="info-section">
      <view class="info-row">
        <text class="info-label">标题</text>
        <text class="info-value">{{ notice.title }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">类型</text>
        <text class="info-value">{{ NOTICE_TYPE_TEXT[notice.noticeType] }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">发布人</text>
        <text class="info-value">{{ notice.publisherName }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">生效时间</text>
        <text class="info-value">{{ formatDate(notice.startTime) }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">失效时间</text>
        <text class="info-value">{{ formatDate(notice.endTime) }}</text>
      </view>
    </view>

    <view class="content-section">
      <text class="section-title">公告内容</text>
      <text class="notice-content">{{ notice.content }}</text>
    </view>

    <view v-if="notice.statusCode === 3 && notice.auditReason" class="reject-section">
      <text class="section-title">拒绝原因</text>
      <text class="reject-text">{{ notice.auditReason }}</text>
    </view>

    <view v-if="!isReadonly && notice.statusCode === 0" class="form-section">
      <text class="section-title">审批意见</text>
      <radio-group @change="onResultChange">
        <label class="radio-item"><radio value="1" :checked="auditResult === 1" /> 通过</label>
        <label class="radio-item"><radio value="0" :checked="auditResult === 0" /> 拒绝</label>
      </radio-group>
      <view class="form-item" style="margin-top: $spacing-md;">
        <uni-easyinput type="textarea" v-model="auditComment" placeholder="审批意见（选填）" />
      </view>
    </view>

    <view v-if="!isReadonly && notice.statusCode === 0" class="bottom-actions">
      <button class="btn-cancel" @click="uni.navigateBack()">取消</button>
      <button class="btn-confirm" :loading="submitting" @click="handleSubmit">提交审批</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getNoticeList, updateNotice } from '@/api/notice-manage.js'
import { NOTICE_TYPE_TEXT } from '@/utils/constants.js'

const isReadonly = ref(false)
const noticeId = ref('')
const submitting = ref(false)
const auditResult = ref(null)
const auditComment = ref('')
function formatDate(val) {
  if (!val) return ''
  if (Array.isArray(val)) return val.join('-')
  return String(val)
}

const notice = reactive({
  title: '', content: '', noticeType: '',
  publisherName: '', startTime: '', endTime: '',
  statusCode: 0, auditReason: ''
})

function onResultChange(e) {
  auditResult.value = parseInt(e.detail.value)
}

async function handleSubmit() {
  if (auditResult.value === null) { uni.showToast({ title: '请选择审批结果', icon: 'none' }); return }
  submitting.value = true
  try {
    if (auditResult.value === 1) {
      // 通过审批 → status=1(已发布)，传入生效/失效时间
      await updateNotice(noticeId.value, {
        status: 1,
        startTime: notice.startTime,
        endTime: notice.endTime
      })
    } else {
      // 拒绝 → status=3(已拒绝)
      await updateNotice(noticeId.value, {
        status: 3
      })
    }
    uni.showToast({ title: '审批完成', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) { /* handled by interceptor */ } finally { submitting.value = false }
}

onLoad(async (query) => {
  if (query.readonly) isReadonly.value = true
  if (query.id) {
    noticeId.value = query.id
    try {
      const data = await getNoticeList()
      const list = Array.isArray(data) ? data : (data.records || [])
      const found = list.find(n => String(n.id) === String(query.id))
      if (found) {
        Object.assign(notice, found)
      }
    } catch (e) { console.error('加载公告详情失败', e) }
  }
})
</script>

<style lang="scss" scoped>
.approve-page { min-height: 100vh; padding: $spacing-lg; background: $color-bg-page; }
.nav-bar { display: flex; align-items: center; gap: $spacing-sm; margin-bottom: $spacing-lg; }
.nav-title { font-size: $font-size-lg; font-weight: 600; }
.info-section { background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md; margin-bottom: $spacing-md; box-shadow: $shadow-card; }
.info-row { display: flex; justify-content: space-between; padding: 8rpx 0; }
.info-label { font-size: $font-size-sm; color: $color-text-secondary; flex-shrink: 0; margin-right: $spacing-md; }
.info-value { font-size: $font-size-sm; color: $color-text-primary; text-align: right; flex: 1; }
.content-section, .reject-section { background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md; margin-bottom: $spacing-md; box-shadow: $shadow-card; }
.section-title { font-size: $font-size-base; font-weight: 600; margin-bottom: $spacing-sm; display: block; }
.notice-content { font-size: $font-size-sm; color: $color-text-primary; line-height: 1.8; }
.reject-text { font-size: $font-size-sm; color: $color-danger; line-height: 1.6; }
.form-section { background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md; margin-bottom: $spacing-md; box-shadow: $shadow-card; }
.form-item { margin-bottom: $spacing-md; }
.radio-item { display: flex; align-items: center; gap: $spacing-sm; font-size: $font-size-base; padding: 8rpx 0; }
.bottom-actions { display: flex; gap: $spacing-md; margin-top: $spacing-lg; }
.btn-cancel { flex: 1; background: transparent; border: 1rpx solid $color-border; color: $color-text-secondary; border-radius: $radius-lg; height: 88rpx; line-height: 88rpx; }
.btn-confirm { flex: 2; background: $color-primary; color: #FFF; border: none; border-radius: $radius-lg; height: 88rpx; line-height: 88rpx; }
</style>
