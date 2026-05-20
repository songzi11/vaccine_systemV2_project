<template>
  <!-- 底部 Tab 栏 -->
  <view class="bottom-tabbar" v-if="tabList.length > 0">
    <view
      v-for="(tab, index) in tabList"
      :key="index"
      class="tabbar-item"
      :class="{ active: currentIndex === index }"
      @tap="switchTab(tab, index)"
    >
      <view class="icon-wrap">
        <image
          v-if="currentIndex === index && tab.activeIcon"
          :src="tab.activeIcon"
          class="tab-icon"
          mode="aspectFit"
        />
        <uni-icons
          v-else
          :type="TAB_ICONS[tab.icon] || tab.icon"
          :size="48"
          :color="currentIndex === index ? activeColor : color"
        />
      </view>
      <text class="tabbar-text" :style="{ color: currentIndex === index ? activeColor : color }">
        {{ tab.text }}
      </text>
      <view v-if="currentIndex === index" class="active-indicator" />
    </view>
  </view>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useUserStore } from '@/store/user.js'
import { TAB_ICONS, TAB_COLOR, TAB_ACTIVE_COLOR } from '@/utils/tabBar.js'

const userStore = useUserStore()
const color = TAB_COLOR
const activeColor = TAB_ACTIVE_COLOR

const tabList = ref(userStore.tabBarConfig)

watch(() => userStore.userInfo, () => {
  tabList.value = userStore.tabBarConfig
})

const currentPath = computed(() => {
  const pages = getCurrentPages()
  if (pages.length > 0) {
    return '/' + pages[pages.length - 1].route
  }
  return ''
})

const currentIndex = ref(0)

function updateCurrentIndex() {
  const idx = tabList.value.findIndex(tab => currentPath.value === tab.path)
  if (idx !== -1) {
    currentIndex.value = idx
  }
}

watch(currentPath, updateCurrentIndex, { immediate: true })
watch(tabList, updateCurrentIndex, { immediate: true })

function switchTab(tab, index) {
  if (currentIndex.value === index) return
  currentIndex.value = index
  uni.reLaunch({ url: tab.path })
}

</script>

<style lang="scss" scoped>
/* 底部 Tab 栏 */
.bottom-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 110rpx;
  background-color: #FFFFFF;
  display: flex;
  align-items: flex-start;
  justify-content: space-around;
  padding-top: 10rpx;
  padding-bottom: env(safe-area-inset-bottom);
  z-index: 999;
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.tabbar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  height: 80rpx;
  position: relative;
  transition: all 0.2s ease;
}

.icon-wrap {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16rpx;
  transition: all 0.2s ease;
}

.tabbar-item.active .icon-wrap {
  background: rgba(7, 193, 96, 0.1);
  transform: scale(1.05);
}

.tab-icon {
  width: 48rpx;
  height: 48rpx;
}

.tabbar-text {
  font-size: 20rpx;
  margin-top: 4rpx;
  letter-spacing: 0.5rpx;
  transition: all 0.2s ease;
}

.tabbar-item.active .tabbar-text {
  font-weight: 700;
  font-size: 21rpx;
}

.active-indicator {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 40rpx;
  height: 6rpx;
  border-radius: 3rpx;
  background: #07C160;
}
</style>
