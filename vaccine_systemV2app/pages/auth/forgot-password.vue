<template>
  <view class="forgot-page">
    <uni-nav-bar title="忘记密码" :border="false" />

    <view class="form-section">
      <uni-forms ref="formRef" :modelValue="form" :rules="rules" validate-trigger="bind">
        <uni-forms-item label="手机号" name="phone" required>
          <uni-easyinput v-model="form.phone" type="number" maxlength="11" placeholder="请输入手机号" />
        </uni-forms-item>

        <uni-forms-item label="验证码" name="smsCode" required>
          <view class="sms-row">
            <uni-easyinput v-model="form.smsCode" type="number" maxlength="6" placeholder="请输入验证码" />
            <button class="sms-btn" :disabled="smsCountdown > 0" @tap="handleSendSms">
              {{ smsCountdown > 0 ? `${smsCountdown}s` : '获取验证码' }}
            </button>
          </view>
        </uni-forms-item>

        <uni-forms-item label="新密码" name="newPassword" required>
          <uni-easyinput v-model="form.newPassword" type="password" placeholder="6-20位，需含字母和数字" />
        </uni-forms-item>

        <uni-forms-item label="确认密码" name="confirmPassword" required>
          <uni-easyinput v-model="form.confirmPassword" type="password" placeholder="请再次输入新密码" />
        </uni-forms-item>
      </uni-forms>

      <button class="submit-btn" :loading="submitting" @tap="handleReset">重置密码</button>

      <view class="bottom-links">
        <text class="link" @tap="navigateBack">想起密码了？去登录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onUnmounted } from 'vue'
import { sendSmsCode, resetPassword } from '@/api/auth.js'
import { useAuth } from '@/hooks/useAuth.js'

const { smsCountdown, clearSmsTimer } = useAuth()

const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  phone: '',
  smsCode: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  phone: { rules: [{ required: true, errorMessage: '请输入手机号' }, { pattern: /^1[3-9]\d{9}$/, errorMessage: '手机号格式不正确' }] },
  smsCode: { rules: [{ required: true, errorMessage: '请输入验证码' }, { pattern: /^\d{6}$/, errorMessage: '验证码为6位数字' }] },
  newPassword: { rules: [{ required: true, errorMessage: '请输入新密码' }, { pattern: /^(?=.*[a-zA-Z])(?=.*\d).{6,20}$/, errorMessage: '6-20位，需含字母和数字' }] },
  confirmPassword: { rules: [{ required: true, errorMessage: '请确认密码' }, { validateFunction: (rule, value, data, callback) => { if (value !== form.newPassword) { callback('两次密码不一致') } return true } }] }
}

async function handleSendSms() {
  if (smsCountdown.value > 0) return
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  try {
    await sendSmsCode(form.phone)
    uni.showToast({ title: '验证码已发送', icon: 'success' })
  } catch {
    // 错误已由 interceptor 处理
  }
}

async function handleReset() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await resetPassword(form.phone, form.smsCode, form.newPassword)
    uni.showToast({ title: '重置成功，请重新登录', icon: 'success' })
    setTimeout(() => {
      uni.reLaunch({ url: '/pages/auth/login' })
    }, 1500)
  } catch {
    // 错误已由 interceptor 处理
  } finally {
    submitting.value = false
  }
}

function navigateBack() {
  uni.navigateBack()
}

onUnmounted(() => {
  clearSmsTimer()
})
</script>

<style lang="scss" scoped>
.forgot-page {
  min-height: 100vh;
  background-color: $color-bg-page;
  padding: 0 $spacing-lg;

  .form-section {
    padding: $spacing-lg;
    background-color: $color-bg-white;
    border-radius: $radius-lg;
    margin-top: $spacing-lg;

    .sms-row {
      display: flex;
      align-items: center;
      gap: $spacing-sm;

      .sms-btn {
        flex-shrink: 0;
        padding: 0 $spacing-md;
        font-size: $font-size-sm;
        color: $color-primary;
        background: none;
        border: 2rpx solid $color-primary;
        border-radius: $radius-lg;

        &::after { border: none; }
      }
    }
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

  .bottom-links {
    text-align: center;
    margin-top: $spacing-xl;

    .link {
      font-size: $font-size-sm;
      color: $color-primary;
    }
  }
}
</style>
