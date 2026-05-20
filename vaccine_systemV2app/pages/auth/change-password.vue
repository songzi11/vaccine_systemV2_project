<template>
  <view class="change-page">
    <view class="form-section">
      <uni-forms ref="formRef" :modelValue="form" :rules="rules" validate-trigger="bind">
        <uni-forms-item label="当前密码" name="oldPassword" required>
          <uni-easyinput v-model="form.oldPassword" type="password" placeholder="请输入当前密码" />
        </uni-forms-item>

        <uni-forms-item label="新密码" name="newPassword" required>
          <uni-easyinput v-model="form.newPassword" type="password" placeholder="6-20位，需含字母和数字" />
        </uni-forms-item>

        <uni-forms-item label="确认密码" name="confirmPassword" required>
          <uni-easyinput v-model="form.confirmPassword" type="password" placeholder="请再次输入新密码" />
        </uni-forms-item>
      </uni-forms>

      <button class="submit-btn" :loading="submitting" @tap="handleChange">确认修改</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { changePassword } from '@/api/auth.js'
import { removeToken, removeUserInfoCache } from '@/utils/auth.js'

const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  oldPassword: { rules: [{ required: true, errorMessage: '请输入当前密码' }] },
  newPassword: { rules: [{ required: true, errorMessage: '请输入新密码' }, { pattern: /^(?=.*[a-zA-Z])(?=.*\d).{6,20}$/, errorMessage: '6-20位，需含字母和数字' }] },
  confirmPassword: { rules: [{ required: true, errorMessage: '请确认密码' }, { validateFunction: (rule, value, data, callback) => { if (value !== form.newPassword) { callback('两次密码不一致') } return true } }] }
}

async function handleChange() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await changePassword(form.oldPassword, form.newPassword)
    uni.showToast({ title: '修改成功，请重新登录', icon: 'success' })
    removeToken()
    removeUserInfoCache()
    setTimeout(() => {
      uni.reLaunch({ url: '/pages/auth/login' })
    }, 1500)
  } catch {
    // 错误已由 interceptor 处理
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.change-page {
  min-height: 100vh;
  background-color: $color-bg-page;
  padding: 0 $spacing-lg;

  .form-section {
    padding: $spacing-lg;
    background-color: $color-bg-white;
    border-radius: $radius-lg;
    margin-top: $spacing-lg;
  }

  .submit-btn {
    width: 100%;
    margin-top: $spacing-lg;
    height: 88rpx;
    line-height: 88rpx;
    background-color: $color-primary;
    color: #FFFFFF;
    font-size: $font-size-lg;
    border: none;
    border-radius: $radius-lg;

    &::after { border: none; }
  }
}
</style>
