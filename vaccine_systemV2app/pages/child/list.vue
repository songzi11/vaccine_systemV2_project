<template>
  <view class="child-list-page">
    <!-- 数量提示 -->
    <view class="count-tip">
      <text class="tip-text">已添加 {{ children.length }}/5 位儿童</text>
    </view>

    <!-- 儿童列表 -->
    <view class="child-list" v-if="children.length > 0">
      <view v-for="child in children" :key="child.id" class="swipe-item">
        <uni-swipe-action>
          <uni-swipe-action-item :right-options="swipeOptions" @click="onSwipeClick($event, child)">
            <ChildCard :child="child" @click="navigateToEdit(child)" />
          </uni-swipe-action-item>
        </uni-swipe-action>
      </view>
    </view>

    <EmptyState v-else icon="person" title="暂无儿童信息" description="请添加儿童信息以预约接种" actionText="添加儿童" @action="navigateToAdd" />

    <!-- 底部添加按钮 -->
    <view v-if="children.length < 5" class="fab" @tap="navigateToAdd">
      <text class="fab-text">+</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useChild } from '@/hooks/useChild.js'
import { useChildStore } from '@/store/child.js'
import ChildCard from '@/components/ChildCard/ChildCard.vue'
import EmptyState from '@/components/EmptyState/EmptyState.vue'

const { fetchChildren, deleteChild } = useChild()
const childStore = useChildStore()
const children = computed(() => childStore.children)

const swipeOptions = [{ text: '删除', style: { backgroundColor: '#EE0A24' } }]

onShow(() => {
  fetchChildren()
})

function navigateToAdd() {
  uni.navigateTo({ url: '/pages/child/add' })
}

function navigateToEdit(child) {
  uni.navigateTo({ url: `/pages/child/add?id=${child.id}` })
}

function onSwipeClick(e, child) {
  if (e.index === 0) {
    uni.showModal({
      title: '确认删除',
      content: `确定要删除 ${child.name} 的信息吗？`,
      success: async (res) => {
        if (res.confirm) {
          try {
            await deleteChild(child.id)
            uni.showToast({ title: '已删除', icon: 'success' })
          } catch {
            // 错误已由 interceptor 处理
          }
        }
      }
    })
  }
}
</script>

<style lang="scss" scoped>
.child-list-page {
  min-height: 100vh;
  background-color: $color-bg-page;
}

.nav-add {
  font-size: $font-size-base;
  color: $color-primary;
  margin-right: $spacing-md;
}

.count-tip {
  padding: $spacing-sm $spacing-lg;
  background-color: $color-primary-light;
}

.tip-text {
  font-size: $font-size-sm;
  color: $color-text-secondary;
}

.child-list {
  padding: $spacing-md;
}

.swipe-item {
  margin-bottom: $spacing-sm;
}

.fab {
  position: fixed;
  right: $spacing-lg;
  bottom: calc(120rpx + env(safe-area-inset-bottom));
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background-color: $color-primary;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 16rpx rgba(7, 193, 96, 0.4);
}

.fab-text {
  font-size: 56rpx;
  color: #FFFFFF;
  line-height: 1;
  margin-top: -4rpx;
}
</style>
