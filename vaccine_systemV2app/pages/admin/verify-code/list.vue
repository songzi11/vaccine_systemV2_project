<template>
  <view class="verify-page">
    <view class="action-bar">
      <button class="generate-btn" @tap="handleGenerate" :loading="generating">生成验证码</button>
    </view>

    <view v-if="codeList.length > 0" class="code-list">
      <view v-for="item in codeList" :key="item.id" class="code-card">
        <view class="code-header">
          <text class="code-value">{{ item.code }}</text>
          <text class="status-tag" :class="statusClass(item.statusCode)">{{ item.statusText }}</text>
        </view>
        <view class="code-info">
          <text class="info-row">创建者：{{ item.creatorName || '未知' }}</text>
          <text class="info-row">创建时间：{{ formatTime(item.createTime) }}</text>
          <text v-if="item.usedByName" class="info-row">使用者：{{ item.usedByName }}</text>
          <text v-if="item.usedAt" class="info-row">使用时间：{{ formatTime(item.usedAt) }}</text>
        </view>
        <view v-if="item.statusCode === 0" class="code-action">
          <text class="action-text revoke" @tap="handleRevoke(item)">撤销</text>
        </view>
      </view>
    </view>

    <EmptyState v-else-if="!loading" icon="list" title="暂无验证码" />

    <CustomTabBar />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { generateVerifyCode, getVerifyCodeList, revokeVerifyCode } from '@/api/verify-code.js'
import EmptyState from '@/components/EmptyState/EmptyState.vue'
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue'

const codeList = ref([])
const loading = ref(false)
const generating = ref(false)

function statusClass(status) {
  return { 0: 'unused', 1: 'used', 2: 'revoked' }[status] || ''
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

async function loadList() {
  loading.value = true
  try {
    codeList.value = await getVerifyCodeList() || []
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

async function handleGenerate() {
  generating.value = true
  try {
    const data = await generateVerifyCode()
    uni.showToast({ title: `验证码：${data.code}`, icon: 'none', duration: 3000 })
    loadList()
  } catch (e) {} finally {
    generating.value = false
  }
}

function handleRevoke(item) {
  uni.showModal({
    title: '确认撤销',
    content: `确定撤销验证码 ${item.code} 吗？`,
    success: async (res) => {
      if (!res.confirm) return
      try {
        await revokeVerifyCode(item.id)
        uni.showToast({ title: '已撤销', icon: 'success' })
        loadList()
      } catch (e) {}
    }
  })
}

onShow(() => { loadList() })
</script>

<style lang="scss" scoped>
.verify-page { min-height: 100vh; background: $color-bg-page; padding: $spacing-lg; padding-bottom: 140rpx; }
.action-bar { margin-bottom: $spacing-md; }
.generate-btn { width: 100%; height: 80rpx; line-height: 80rpx; background: $color-primary; color: #FFFFFF; font-size: $font-size-base; border: none; border-radius: $radius-lg; }
.code-list { margin-top: $spacing-sm; }
.code-card { background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md; margin-bottom: $spacing-md; box-shadow: $shadow-card; }
.code-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: $spacing-sm; }
.code-value { font-size: 48rpx; font-weight: 700; color: $color-text-primary; letter-spacing: 8rpx; font-family: monospace; }
.status-tag { font-size: $font-size-xs; padding: 4rpx 16rpx; border-radius: $radius-sm; }
.status-tag.unused { color: $color-success; background: rgba(7,193,96,0.1); }
.status-tag.used { color: $color-text-placeholder; background: $color-bg-grey; }
.status-tag.revoked { color: $color-danger; background: rgba(238,10,36,0.1); }
.code-info { margin-bottom: $spacing-xs; }
.info-row { font-size: $font-size-xs; color: $color-text-secondary; display: block; }
.code-action { display: flex; justify-content: flex-end; padding-top: $spacing-sm; border-top: 1rpx solid $color-border-light; }
.action-text.revoke { font-size: $font-size-sm; color: $color-danger; }
</style>
