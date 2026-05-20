<template>
  <view class="assign-page">
    <view class="nav-bar">
      <uni-icons type="back" :size="20" @click="uni.navigateBack()" />
      <text class="nav-title">分配角色</text>
    </view>

    <view class="search-section">
      <view class="search-row">
        <uni-easyinput v-model="searchKeyword" placeholder="手机号/姓名搜索用户" />
        <button class="search-btn" @tap="searchUser">搜索</button>
      </view>
    </view>

    <view v-if="selectedUser" class="user-card">
      <view class="user-info">
        <text class="user-name">{{ selectedUser.realName || '未设置' }}</text>
        <text class="user-phone">{{ maskPhone(selectedUser.phone) }}</text>
      </view>
      <view class="current-roles">
        <text class="roles-label">当前角色：</text>
        <text v-for="role in currentRoles" :key="role" class="role-tag">{{ role }}</text>
        <text v-if="currentRoles.length === 0" class="no-role">无角色</text>
      </view>
    </view>

    <view v-if="selectedUser" class="form-section">
      <text class="section-title">角色选择</text>
      <view v-for="role in allRoles" :key="role.id" class="role-item">
        <label class="checkbox-label">
          <checkbox :value="String(role.id)" :checked="selectedRoleIds.includes(String(role.id))" @tap="toggleRole(String(role.id))" />
          <text class="checkbox-text">{{ role.roleName }}（{{ role.roleCode }}）</text>
        </label>
      </view>
    </view>

    <view v-if="selectedUser" class="bottom-actions">
      <button class="btn-cancel" @click="uni.navigateBack()">取消</button>
      <button class="btn-confirm" :loading="submitting" @click="handleSubmit">保存</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { assignRoles, getUserRoles, getRoleList } from '@/api/role.js'
import { getUserList } from '@/api/user-manage.js'

const searchKeyword = ref('')
const selectedUser = ref(null)
const currentRoles = ref([])
const selectedRoleIds = ref([])
const allRoles = ref([])
const submitting = ref(false)

function maskPhone(phone) {
  if (!phone || phone.length !== 11) return phone || ''
  return phone.slice(0, 3) + '****' + phone.slice(7)
}

async function searchUser() {
  if (!searchKeyword.value) { uni.showToast({ title: '请输入搜索关键词', icon: 'none' }); return }
  try {
    const data = await getUserList({ keyword: searchKeyword.value, page: 1, size: 10 })
    if (data.records?.length) {
      selectedUser.value = data.records[0]
      await loadUserRoles(selectedUser.value.id)
    } else {
      uni.showToast({ title: '未找到用户', icon: 'none' })
      selectedUser.value = null
    }
  } catch (e) { /* handled */ }
}

async function loadUserRoles(userId) {
  try {
    const roles = await getUserRoles(userId)
    currentRoles.value = (roles || []).map(r => r.roleName || r.roleCode)
    selectedRoleIds.value = (roles || []).map(r => String(r.id))
  } catch (e) {
    currentRoles.value = []
    selectedRoleIds.value = []
  }
}

async function loadAllRoles() {
  try {
    const data = await getRoleList({ page: 1, size: 100 })
    allRoles.value = data.records || []
  } catch (e) { /* handled */ }
}

function toggleRole(roleId) {
  const idx = selectedRoleIds.value.indexOf(roleId)
  if (idx >= 0) selectedRoleIds.value.splice(idx, 1)
  else selectedRoleIds.value.push(roleId)
}

async function handleSubmit() {
  if (!selectedUser.value) { uni.showToast({ title: '请先搜索用户', icon: 'none' }); return }
  submitting.value = true
  try {
    await assignRoles(selectedUser.value.id, selectedRoleIds.value.map(Number))
    uni.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) { /* handled by interceptor */ } finally { submitting.value = false }
}

onShow(() => { loadAllRoles() })
</script>

<style lang="scss" scoped>
.assign-page { min-height: 100vh; padding: $spacing-lg; background: $color-bg-page; }
.nav-bar { display: flex; align-items: center; gap: $spacing-sm; margin-bottom: $spacing-lg; }
.nav-title { font-size: $font-size-lg; font-weight: 600; }
.search-section { background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md; margin-bottom: $spacing-md; box-shadow: $shadow-card; }
.search-row { display: flex; gap: $spacing-sm; }
.search-btn { flex-shrink: 0; padding: 0 $spacing-lg; background: $color-primary; color: #FFF; font-size: $font-size-sm; border: none; border-radius: $radius-md; height: 70rpx; line-height: 70rpx; }
.user-card { background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md; margin-bottom: $spacing-md; box-shadow: $shadow-card; }
.user-info { display: flex; gap: $spacing-md; margin-bottom: $spacing-sm; }
.user-name { font-size: $font-size-base; font-weight: 600; }
.user-phone { font-size: $font-size-sm; color: $color-text-secondary; }
.current-roles { display: flex; flex-wrap: wrap; align-items: center; gap: $spacing-xs; }
.roles-label { font-size: $font-size-sm; color: $color-text-secondary; }
.role-tag { padding: 2rpx 12rpx; font-size: $font-size-xs; background: $color-primary-light; color: $color-primary; border-radius: $radius-sm; }
.no-role { font-size: $font-size-xs; color: $color-text-placeholder; }
.form-section { background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md; margin-bottom: $spacing-md; box-shadow: $shadow-card; }
.section-title { font-size: $font-size-base; font-weight: 600; margin-bottom: $spacing-md; display: block; }
.role-item { padding: 8rpx 0; }
.checkbox-label { display: flex; align-items: center; gap: $spacing-sm; }
.checkbox-text { font-size: $font-size-sm; color: $color-text-primary; }
.bottom-actions { display: flex; gap: $spacing-md; margin-top: $spacing-lg; }
.btn-cancel { flex: 1; background: transparent; border: 1rpx solid $color-border; color: $color-text-secondary; border-radius: $radius-lg; height: 88rpx; line-height: 88rpx; }
.btn-confirm { flex: 2; background: $color-primary; color: #FFF; border: none; border-radius: $radius-lg; height: 88rpx; line-height: 88rpx; }
</style>
