<template>
  <view class="notice-list-page">

    <view class="tab-bar">
      <view
        v-for="tab in tabs" :key="tab.value"
        class="tab-item" :class="{ active: currentTab === tab.value }"
        @tap="switchTab(tab.value)"
      >
        <text class="tab-text">{{ tab.label }}</text>
        <view v-if="tab.count > 0" class="tab-badge">{{ tab.count > 99 ? '99+' : tab.count }}</view>
      </view>
    </view>

    <view class="card-list">
      <view v-for="item in filteredList" :key="item.id" class="notice-card">
        <view class="card-header">
          <text class="notice-title">{{ item.title }}</text>
          <view class="status-tags">
            <text class="type-tag" :class="typeTagClass(item.noticeType)">{{ NOTICE_TYPE_TEXT[item.noticeType] || item.noticeType }}</text>
          </view>
        </view>
        <view class="card-body">
          <text class="card-meta">发布人：{{ item.publisherName }}</text>
          <text class="card-meta">生效：{{ formatDate(item.startTime) }} 至 {{ formatDate(item.endTime) }}</text>
          <text class="card-content">{{ item.content }}</text>
        </view>
        <view class="card-actions">
          <text class="action-text" @tap.stop="viewDetail(item)">查看详情</text>
          <text v-if="currentTab === 'pending'" class="action-text" @tap.stop="goApprove(item)">审批</text>
          <text v-if="currentTab === 'offline' && isWithinValidity(item)" class="action-text success" @tap.stop="handleRepublish(item)">重新上架</text>
          <text v-if="currentTab === 'published' || currentTab === 'expired'" class="action-text warning" @tap.stop="handleOffline(item)">下架</text>
          <text class="action-text danger" @tap.stop="handleDelete(item)">删除</text>
        </view>
      </view>
      <EmptyState v-if="!loading && filteredList.length === 0" icon="list" :title="emptyText" />
    </view>

    <view v-if="userStore.isBusinessAdmin" class="fab-btn" @tap="goPublish">
      <text class="fab-icon">+</text>
    </view>

    <CustomTabBar />
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getNoticeList, deleteNotice, updateNotice } from '@/api/notice-manage.js'
import { useUserStore } from '@/store/user.js'
import StatusTag from '@/components/StatusTag/StatusTag.vue'
import EmptyState from '@/components/EmptyState/EmptyState.vue'
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue'
import { NOTICE_TYPE_TEXT } from '@/utils/constants.js'

const userStore = useUserStore()
const allNotices = ref([])
const loading = ref(false)
const currentTab = ref('published')

const tabs = computed(() => [
  { value: 'pending', label: '待审批', count: allNotices.value.filter(n => n.statusCode === 0).length },
  { value: 'published', label: '已发布', count: allNotices.value.filter(n => getDisplayStatus(n) === 1).length },
  { value: 'offline', label: '已下架', count: allNotices.value.filter(n => n.statusCode === 2).length },
  { value: 'rejected', label: '已拒绝', count: allNotices.value.filter(n => n.statusCode === 3).length },
  { value: 'expired', label: '已过期', count: allNotices.value.filter(n => getDisplayStatus(n) === 4).length }
])

const emptyText = computed(() => {
  const map = { pending: '暂无待审批公告', published: '暂无已发布公告', offline: '暂无已下架公告', rejected: '暂无已拒绝公告', expired: '暂无已过期公告' }
  return map[currentTab.value] || '暂无公告记录'
})

function formatDate(val) {
  if (!val) return ''
  if (Array.isArray(val)) return val.join('-')
  return String(val)
}

function getDisplayStatus(item) {
  if (item.statusCode === 1 && item.endTime) {
    const end = Array.isArray(item.endTime)
      ? new Date(item.endTime[0], item.endTime[1] - 1, item.endTime[2])
      : new Date(item.endTime)
    if (end < new Date()) return 4
  }
  return item.statusCode
}

const filteredList = computed(() => {
  switch (currentTab.value) {
    case 'pending': return allNotices.value.filter(n => n.statusCode === 0)
    case 'published': return allNotices.value.filter(n => getDisplayStatus(n) === 1)
    case 'offline': return allNotices.value.filter(n => n.statusCode === 2)
    case 'rejected': return allNotices.value.filter(n => n.statusCode === 3)
    case 'expired': return allNotices.value.filter(n => getDisplayStatus(n) === 4)
    default: return allNotices.value
  }
})

function typeTagClass(type) {
  if (type === 'SYSTEM') return 'tag-blue'
  if (type === 'INTERNAL') return 'tag-green'
  return 'tag-green'
}

function isWithinValidity(item) {
  if (!item.endTime) return true
  const end = Array.isArray(item.endTime)
    ? new Date(item.endTime[0], item.endTime[1] - 1, item.endTime[2])
    : new Date(item.endTime)
  return end >= new Date()
}

function switchTab(value) {
  currentTab.value = value
}

onShow(() => { loadNotices() })

async function loadNotices() {
  loading.value = true
  try {
    const data = await getNoticeList()
    allNotices.value = Array.isArray(data) ? data : (data.records || [])
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function viewDetail(item) { uni.navigateTo({ url: `/pages/admin/notice/approve?id=${item.id}&readonly=1` }) }
function goApprove(item) { uni.navigateTo({ url: `/pages/admin/notice/approve?id=${item.id}` }) }
function goPublish() { uni.navigateTo({ url: '/pages/admin/notice/publish' }) }

function handleRepublish(item) {
  uni.showModal({
    title: '确认上架',
    content: `确定要重新上架公告「${item.title}」吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await updateNotice(item.id, { status: 1, startTime: item.startTime, endTime: item.endTime })
          uni.showToast({ title: '已上架', icon: 'success' })
          loadNotices()
        } catch (e) { /* handled */ }
      }
    }
  })
}

function handleOffline(item) {
  uni.showModal({
    title: '确认下架',
    content: `确定要下架公告「${item.title}」吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await updateNotice(item.id, { status: 2 })
          uni.showToast({ title: '已下架', icon: 'success' })
          loadNotices()
        } catch (e) { /* handled */ }
      }
    }
  })
}

function handleDelete(item) {
  uni.showModal({
    title: '确认删除',
    content: `确定要删除公告「${item.title}」吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteNotice(item.id)
          uni.showToast({ title: '删除成功', icon: 'success' })
          loadNotices()
        } catch (e) { /* handled */ }
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.notice-list-page { min-height: 100vh; background: $color-bg-page; padding: $spacing-lg; padding-bottom: 140rpx; }

.tab-bar {
  display: flex;
  background: $color-bg-white;
  border-radius: $radius-lg;
  padding: 6rpx;
  margin-bottom: $spacing-md;
  box-shadow: $shadow-card;
}
.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4rpx;
  padding: 16rpx 0;
  border-radius: $radius-md;
  position: relative;
  transition: all 0.2s;
}
.tab-item.active {
  background: $color-primary;
}
.tab-text {
  font-size: $font-size-sm;
  color: $color-text-secondary;
}
.tab-item.active .tab-text {
  color: #FFFFFF;
  font-weight: 600;
}
.tab-badge {
  min-width: 32rpx;
  height: 32rpx;
  line-height: 32rpx;
  text-align: center;
  font-size: $font-size-xs;
  background: #EE0A24;
  color: #FFF;
  border-radius: 16rpx;
  padding: 0 8rpx;
}
.tab-item.active .tab-badge {
  background: rgba(255,255,255,0.3);
}

.card-list { margin-top: $spacing-md; }
.notice-card { background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md; margin-bottom: $spacing-md; box-shadow: $shadow-card; }
.card-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: $spacing-sm; }
.notice-title { font-size: $font-size-base; font-weight: 600; color: $color-text-primary; flex: 1; margin-right: $spacing-sm; }
.status-tags { display: flex; gap: $spacing-xs; align-items: center; flex-shrink: 0; }
.type-tag { padding: 2rpx 12rpx; font-size: $font-size-xs; border-radius: $radius-sm; }
.tag-green { background: rgba(7,193,96,0.1); color: #07C160; }
.tag-blue { background: rgba(25,137,250,0.1); color: #1989FA; }
.card-body { margin-bottom: $spacing-sm; }
.card-meta { font-size: $font-size-xs; color: $color-text-placeholder; display: block; }
.card-content { font-size: $font-size-sm; color: $color-text-secondary; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; margin-top: 4rpx; }
.card-actions { display: flex; justify-content: flex-end; gap: $spacing-md; padding-top: $spacing-sm; border-top: 1rpx solid $color-border-light; }
.action-text { font-size: $font-size-sm; color: $color-text-secondary; }
.action-text.success { color: #07C160; }
.action-text.warning { color: #FF9900; }
.action-text.danger { color: $color-danger; }
.fab-btn { position: fixed; right: $spacing-lg; bottom: 160rpx; width: 100rpx; height: 100rpx; background: $color-primary; border-radius: 50%; display: flex; align-items: center; justify-content: center; box-shadow: $shadow-lg; }
.fab-icon { font-size: 56rpx; color: #FFFFFF; }
</style>
