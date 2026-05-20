<template>
  <view class="role-edit-page">
    <view class="nav-bar">
      <uni-icons type="back" :size="20" @click="uni.navigateBack()" />
      <text class="nav-title">{{ isEdit ? '编辑角色' : '创建角色' }}</text>
    </view>

    <view class="form-section">
      <view class="form-item">
        <text class="form-label">角色名称 *</text>
        <uni-easyinput v-model="form.roleName" placeholder="请输入角色名称" />
      </view>
      <view class="form-item">
        <text class="form-label">角色编码 *</text>
        <uni-easyinput v-model="form.roleCode" placeholder="请输入角色编码" :disabled="isEdit" />
      </view>
    </view>

    <view class="form-section">
      <text class="section-title">权限配置</text>
      <view v-for="group in permissionGroups" :key="group.label" class="permission-group">
        <view class="group-header">
          <text class="group-label">{{ group.label }}</text>
        </view>
        <view v-for="perm in group.permissions" :key="perm.value" class="permission-item">
          <label class="checkbox-label">
            <checkbox :value="perm.value" :checked="selectedPermissions.includes(perm.value)" @tap="togglePermission(perm.value)" />
            <text class="checkbox-text">{{ perm.label }}</text>
          </label>
        </view>
      </view>
    </view>

    <view class="bottom-actions">
      <button class="btn-cancel" @click="uni.navigateBack()">取消</button>
      <button class="btn-confirm" :loading="submitting" @click="handleSubmit">保存</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { createRole, updateRole, getRolePermissions } from '@/api/role.js'
import { PERMISSION_GROUPS } from '@/utils/constants.js'

const isEdit = ref(false)
const roleId = ref('')
const submitting = ref(false)
const permissionGroups = PERMISSION_GROUPS
const selectedPermissions = ref([])

const form = reactive({ roleName: '', roleCode: '' })

function togglePermission(value) {
  const idx = selectedPermissions.value.indexOf(value)
  if (idx >= 0) {
    selectedPermissions.value.splice(idx, 1)
  } else {
    selectedPermissions.value.push(value)
  }
}

async function handleSubmit() {
  if (!form.roleName) { uni.showToast({ title: '请输入角色名称', icon: 'none' }); return }
  if (!form.roleCode) { uni.showToast({ title: '请输入角色编码', icon: 'none' }); return }
  submitting.value = true
  try {
    const data = { roleName: form.roleName, roleCode: form.roleCode, permissions: selectedPermissions.value }
    if (isEdit.value) await updateRole(roleId.value, data)
    else await createRole(data)
    uni.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) { /* handled */ } finally { submitting.value = false }
}

onLoad(async (query) => {
  if (query.id) {
    isEdit.value = true
    roleId.value = query.id
    try {
      const perms = await getRolePermissions(query.id)
      selectedPermissions.value = Array.isArray(perms) ? perms : []
    } catch (e) { console.error('加载角色权限失败', e) }
  }
})
</script>

<style lang="scss" scoped>
.role-edit-page { min-height: 100vh; padding: $spacing-lg; background: $color-bg-page; }
.nav-bar { display: flex; align-items: center; gap: $spacing-sm; margin-bottom: $spacing-lg; }
.nav-title { font-size: $font-size-lg; font-weight: 600; }
.form-section { background: $color-bg-white; border-radius: $radius-lg; padding: $spacing-md; margin-bottom: $spacing-md; box-shadow: $shadow-card; }
.section-title { font-size: $font-size-base; font-weight: 600; margin-bottom: $spacing-md; display: block; }
.form-item { margin-bottom: $spacing-md; }
.form-label { font-size: $font-size-sm; color: $color-text-secondary; margin-bottom: $spacing-xs; display: block; }
.permission-group { margin-bottom: $spacing-md; }
.group-header { margin-bottom: $spacing-xs; }
.group-label { font-size: $font-size-sm; font-weight: 600; color: $color-text-primary; }
.permission-item { padding: 8rpx 0; }
.checkbox-label { display: flex; align-items: center; gap: $spacing-sm; }
.checkbox-text { font-size: $font-size-sm; color: $color-text-secondary; }
.bottom-actions { display: flex; gap: $spacing-md; margin-top: $spacing-lg; }
.btn-cancel { flex: 1; background: transparent; border: 1rpx solid $color-border; color: $color-text-secondary; border-radius: $radius-lg; height: 88rpx; line-height: 88rpx; }
.btn-confirm { flex: 2; background: $color-primary; color: #FFF; border: none; border-radius: $radius-lg; height: 88rpx; line-height: 88rpx; }
</style>
