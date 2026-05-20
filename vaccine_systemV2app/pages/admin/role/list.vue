<template>
  <view class="role-page">
    <view class="filter-bar">
      <text v-for="f in filters" :key="f.value" class="filter-chip" :class="{ active: activeFilter === f.value }" @tap="activeFilter = f.value">{{ f.label }}</text>
    </view>

    <view v-if="filteredList.length > 0" class="user-list">
      <view v-for="user in filteredList" :key="user.userId" class="user-card">
        <view class="user-header">
          <view class="user-info">
            <text class="user-name">{{ user.realName || '未设置' }}</text>
            <text class="user-phone">{{ user.phone }}</text>
          </view>
          <text class="user-status" :class="user.status === '正常' ? 'normal' : 'abnormal'">{{ user.status }}</text>
        </view>
        <view class="role-tags">
          <text v-if="user.roleCodes && user.roleCodes.length > 0" v-for="code in user.roleCodes" :key="code" class="role-tag">{{ ROLE_LABELS[code] || code }}</text>
          <text v-else class="no-role">未分配角色</text>
        </view>
      </view>
    </view>

    <EmptyState v-else-if="!loading" icon="list" title="暂无用户数据" />

    <CustomTabBar />
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getUserList } from '@/api/user-manage.js'
import EmptyState from '@/components/EmptyState/EmptyState.vue'
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue'

const ROLE_LABELS = {
  USER: '家长',
  DOCTOR_SIGNIN: '签到医生',
  DOCTOR_PRECHECK: '预检医生',
  DOCTOR_REGISTER: '登记医生',
  DOCTOR_VACCINATE: '接种医生',
  DOCTOR_OBSERVE: '留观医生',
  DOCTOR_STOCK: '库存管理',
  DOCTOR_BUSINESS_ADMIN: '业务主管',
  SUPER_ADMIN: '系统管理员'
}

const userList = ref([])
const loading = ref(false)
const activeFilter = ref('all')

const filters = [
  { label: '全部', value: 'all' },
  { label: '家长', value: 'USER' },
  { label: '医生', value: 'DOCTOR' },
  { label: '仓管', value: 'DOCTOR_STOCK' },
  { label: '主管', value: 'DOCTOR_BUSINESS_ADMIN' }
]

const DOCTOR_ROLES = ['DOCTOR_SIGNIN','DOCTOR_PRECHECK','DOCTOR_REGISTER','DOCTOR_VACCINATE','DOCTOR_OBSERVE']

const filteredList = computed(() => {
  if (activeFilter.value === 'all') return userList.value
  if (activeFilter.value === 'DOCTOR') return userList.value.filter(u => u.roleCodes?.some(c => DOCTOR_ROLES.includes(c)))
  return userList.value.filter(u => u.roleCodes?.includes(activeFilter.value))
})

onShow(async () => {
  loading.value = true
  try {
    const data = await getUserList({ page: 1, size: 200 })
    userList.value = Array.isArray(data) ? data : (data.records || [])
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
.role-page { min-height: 100vh; background: $color-bg-page; padding: $spacing-lg; padding-bottom: 140rpx; }
.filter-bar { display: flex; gap: $spacing-sm; margin-bottom: $spacing-md; }
.filter-chip { font-size: $font-size-sm; padding: $spacing-xs $spacing-md; background: $color-bg-white; border-radius: $radius-lg; color: $color-text-secondary; box-shadow: $shadow-card; }
.filter-chip.active { background: $color-primary; color: #FFFFFF; }
.user-list { margin-top: $spacing-md; }
.user-card { background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md; margin-bottom: $spacing-md; box-shadow: $shadow-card; }
.user-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: $spacing-sm; }
.user-info { display: flex; align-items: baseline; gap: $spacing-md; }
.user-name { font-size: $font-size-base; font-weight: 600; color: $color-text-primary; }
.user-phone { font-size: $font-size-sm; color: $color-text-secondary; }
.user-status { font-size: $font-size-xs; padding: 2rpx 12rpx; border-radius: $radius-sm; }
.user-status.normal { color: $color-success; background: rgba(7,193,96,0.1); }
.user-status.abnormal { color: $color-danger; background: rgba(238,10,36,0.1); }
.role-tags { display: flex; flex-wrap: wrap; gap: $spacing-xs; }
.role-tag { font-size: $font-size-xs; padding: 4rpx 16rpx; background: rgba(7,193,96,0.1); color: $color-primary; border-radius: $radius-sm; }
.no-role { font-size: $font-size-xs; color: $color-text-placeholder; }
</style>
