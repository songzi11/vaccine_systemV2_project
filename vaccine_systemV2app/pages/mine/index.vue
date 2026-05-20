<template>
  <view class="mine-page">
    <view class="user-card">
      <view class="avatar">{{ avatarText }}</view>
      <view class="user-info">
        <text class="user-name">{{ userStore.userInfo?.realName || '未设置' }}</text>
        <text class="user-phone">{{ maskedPhone }}</text>
        <text class="user-role">{{ roleText }}</text>
      </view>
    </view>

    <view class="menu-list">
      <view class="menu-item" @tap="goChangePassword">
        <text class="menu-label">修改密码</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @tap="goAbout">
        <text class="menu-label">关于系统</text>
        <text class="menu-arrow">›</text>
      </view>
    </view>

    <button class="logout-btn" @click="handleLogout">退出登录</button>

    <CustomTabBar />
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/store/user.js'
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue'

const userStore = useUserStore()

const avatarText = computed(() => {
  return (userStore.userInfo?.realName || '?').charAt(0)
})

const maskedPhone = computed(() => {
  const phone = userStore.userInfo?.phone || ''
  if (phone.length === 11) return phone.slice(0, 3) + '****' + phone.slice(7)
  return phone
})

const roleText = computed(() => {
  if (userStore.isSuperAdmin) return '系统管理员'
  if (userStore.isBusinessAdmin) return '业务主管'
  const map = {
    DOCTOR_SIGNIN: '签到医生',
    DOCTOR_PRECHECK: '预检医生',
    DOCTOR_REGISTER: '登记医生',
    DOCTOR_VACCINATE: '接种医生',
    DOCTOR_OBSERVE: '留观医生',
    DOCTOR_STOCK: '库存管理'
  }
  const roles = userStore.userInfo?.roles || []
  const primary = roles.find(r => r.startsWith('DOCTOR_'))
  if (primary) return map[primary] || '医生'
  if (userStore.isUser) return '家长'
  return '用户'
})

function handleLogout() {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) userStore.logout()
    }
  })
}

function goChangePassword() {
  uni.navigateTo({ url: '/pages/auth/change-password' })
}

function goAbout() {
  uni.navigateTo({ url: '/pages/about/index' })
}
</script>

<style lang="scss" scoped>
.mine-page { min-height: 100vh; padding: $spacing-lg; padding-bottom: 140rpx; }
.user-card { background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-xl $spacing-lg; display: flex; align-items: center; margin-bottom: $spacing-lg; box-shadow: $shadow-card; }
.avatar { width: 120rpx; height: 120rpx; border-radius: 50%; background: $color-primary; color: #FFFFFF; font-size: 48rpx; display: flex; align-items: center; justify-content: center; margin-right: $spacing-md; }
.user-info { display: flex; flex-direction: column; }
.user-name { font-size: $font-size-lg; font-weight: 600; }
.user-phone { font-size: $font-size-sm; color: $color-text-secondary; margin-top: $spacing-xs; }
.user-role { font-size: $font-size-xs; color: $color-primary; margin-top: 4rpx; }
.logout-btn { margin-top: 80rpx; background: #FFFFFF; border: 1rpx solid $color-border; color: $color-text-secondary; border-radius: $radius-lg; height: 88rpx; line-height: 88rpx; font-size: $font-size-base; }
.menu-list { background: $color-bg-white; border-radius: $radius-lg; box-shadow: $shadow-card; overflow: hidden; }
.menu-item { display: flex; justify-content: space-between; align-items: center; padding: $spacing-md $spacing-lg; border-bottom: 1rpx solid $color-border-light; }
.menu-item:last-child { border-bottom: none; }
.menu-item:active { background: $color-bg-grey; }
.menu-label { font-size: $font-size-base; color: $color-text-primary; }
.menu-arrow { font-size: 36rpx; color: $color-text-placeholder; }
</style>
