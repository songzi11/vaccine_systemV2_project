<template>
  <view class="config-page">
    <uni-nav-bar title="系统配置" :border="false" />

    <view class="config-list">
      <view v-for="item in configList" :key="item.id" class="config-card" @tap="openEdit(item)">
        <view class="config-info">
          <text class="config-name">{{ item.configDesc || item.configKey }}</text>
          <text class="config-key">{{ item.configKey }}</text>
          <text class="config-desc">{{ item.configDesc }}</text>
        </view>
        <view class="config-value">
          <text class="value-text">{{ item.configValue }}</text>
          <uni-icons type="right" :size="14" color="#C0C0C0" />
        </view>
      </view>
      <EmptyState v-if="!loading && configList.length === 0" icon="settings" title="暂无配置项" />
    </view>

    <uni-popup ref="editPopup" type="dialog">
      <view class="popup-content">
        <text class="popup-title">编辑配置</text>
        <text class="popup-name">{{ editingItem.configDesc || editingItem.configKey }}</text>
        <text class="popup-key">{{ editingItem.configKey }}</text>
        <uni-easyinput v-model="editValue" placeholder="请输入配置值" />
        <view class="popup-actions">
          <button class="btn-cancel" @click="closeEdit">取消</button>
          <button class="btn-confirm" @click="saveEdit">保存</button>
        </view>
      </view>
    </uni-popup>

    <CustomTabBar />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getConfigList, updateConfig } from '@/api/config.js'
import { useUserStore } from '@/store/user.js'
import EmptyState from '@/components/EmptyState/EmptyState.vue'
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue'

const userStore = useUserStore()
const configList = ref([])
const loading = ref(false)
const editPopup = ref(null)
const editingItem = ref({})
const editValue = ref('')

onShow(async () => {
  if (!userStore.isAdminRole) {
    uni.showToast({ title: '无权限访问', icon: 'none' })
    uni.navigateBack()
    return
  }
  loading.value = true
  try {
    const data = await getConfigList()
    configList.value = data || []
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
})

function openEdit(item) {
  editingItem.value = item
  editValue.value = item.configValue
  editPopup.value?.open()
}

function closeEdit() {
  editPopup.value?.close()
}

async function saveEdit() {
  try {
    await updateConfig(editingItem.value.id, editValue.value)
    uni.showToast({ title: '保存成功', icon: 'success' })
    editingItem.value.configValue = editValue.value
    closeEdit()
  } catch (e) { /* handled */ }
}
</script>

<style lang="scss" scoped>
.config-page { min-height: 100vh; background: $color-bg-page; padding: $spacing-lg; padding-bottom: 140rpx; }
.config-list { margin-top: $spacing-md; }
.config-card { background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md; margin-bottom: $spacing-md; box-shadow: $shadow-card; display: flex; justify-content: space-between; align-items: center; }
.config-info { flex: 1; margin-right: $spacing-md; }
.config-name { font-size: $font-size-base; font-weight: 600; color: $color-text-primary; display: block; }
.config-key { font-size: $font-size-xs; color: $color-text-placeholder; display: block; margin-top: 4rpx; }
.config-desc { font-size: $font-size-xs; color: $color-text-secondary; display: block; margin-top: 4rpx; }
.config-value { display: flex; align-items: center; gap: $spacing-xs; flex-shrink: 0; }
.value-text { font-size: $font-size-sm; color: $color-primary; font-weight: 500; max-width: 200rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.popup-content { background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-lg; width: 600rpx; }
.popup-title { font-size: $font-size-lg; font-weight: 600; margin-bottom: $spacing-md; display: block; }
.popup-name { font-size: $font-size-base; color: $color-text-primary; display: block; }
.popup-key { font-size: $font-size-xs; color: $color-text-placeholder; margin-bottom: $spacing-md; display: block; }
.popup-actions { display: flex; gap: $spacing-md; margin-top: $spacing-lg; }
.btn-cancel { flex: 1; background: transparent; border: 1rpx solid $color-border; color: $color-text-secondary; border-radius: $radius-md; height: 72rpx; line-height: 72rpx; font-size: $font-size-sm; }
.btn-confirm { flex: 1; background: $color-primary; color: #FFF; border: none; border-radius: $radius-md; height: 72rpx; line-height: 72rpx; font-size: $font-size-sm; }
</style>
