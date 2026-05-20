<template>
  <view class="login-page">
    <!-- 顶部品牌区域 -->
    <view class="login-header">
      <text class="login-title">疫苗管理系统</text>
      <text class="login-subtitle">预防接种智慧服务平台</text>
    </view>

    <!-- 登录方式切换 -->
    <view class="login-form">
      <uni-segmented-control
        :current="loginMode"
        :values="['密码登录', '验证码登录']"
        @clickItem="onModeChange"
        style-type="text"
        active-color="#07C160"
      />

      <!-- 密码登录表单 -->
      <view v-if="loginMode === 0" class="form-section">
        <uni-forms ref="passwordFormRef" :model="passwordForm" :rules="passwordRules">
          <uni-forms-item name="phone">
            <uni-easyinput
              v-model="passwordForm.phone"
              placeholder="请输入手机号"
              type="number"
              maxlength="11"
              :inputBorder="false"
              prefixIcon="person"
            />
          </uni-forms-item>
          <uni-forms-item name="password">
            <uni-easyinput
              v-model="passwordForm.password"
              placeholder="请输入密码"
              type="password"
              :inputBorder="false"
              prefixIcon="locked"
            />
          </uni-forms-item>
        </uni-forms>
      </view>

      <!-- 验证码登录表单 -->
      <view v-else class="form-section">
        <uni-forms ref="smsFormRef" :model="smsForm" :rules="smsRules">
          <uni-forms-item name="phone">
            <uni-easyinput
              v-model="smsForm.phone"
              placeholder="请输入手机号"
              type="number"
              maxlength="11"
              :inputBorder="false"
              prefixIcon="person"
            />
          </uni-forms-item>
          <uni-forms-item name="smsCode">
            <view class="sms-input-row">
              <uni-easyinput
                v-model="smsForm.smsCode"
                placeholder="请输入验证码"
                type="number"
                maxlength="6"
                :inputBorder="false"
                prefixIcon="chat"
                class="sms-input"
              />
              <view
                class="sms-btn"
                :class="{ disabled: smsCountdown > 0 }"
                @tap="onSendSms"
              >
                <text class="sms-btn-text">
                  {{ smsCountdown > 0 ? `${smsCountdown}s` : '获取验证码' }}
                </text>
              </view>
            </view>
          </uni-forms-item>
        </uni-forms>
      </view>

      <!-- 登录按钮 -->
      <button class="login-btn" :loading="loading" @tap="onLogin">登录</button>

      <!-- 底部链接 -->
      <view class="login-links">
        <text class="link-text" @tap="goTo('/pages/auth/register')">注册账号</text>
        <text class="link-text" @tap="goTo('/pages/auth/forgot-password')">忘记密码</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onUnmounted } from 'vue'
import { useAuth } from '@/hooks/useAuth.js'
import { PHONE_REGEX, SMS_CODE_REGEX } from '@/utils/validate.js'

const loginMode = ref(0) // 0: 密码登录, 1: 验证码登录

const { loading, smsCountdown, passwordLogin, smsCodeLogin, sendSmsCode, clearSmsTimer } = useAuth()

// ==================== 密码登录表单 ====================
const passwordFormRef = ref(null)
const passwordForm = ref({ phone: '', password: '' })
const passwordRules = {
  phone: { rules: [{ required: true, errorMessage: '请输入手机号' }, { pattern: PHONE_REGEX, errorMessage: '请输入正确的手机号' }] },
  password: { rules: [{ required: true, errorMessage: '请输入密码' }, { minLength: 6, maxLength: 20, errorMessage: '密码长度为6-20位' }] }
}

// ==================== 验证码登录表单 ====================
const smsFormRef = ref(null)
const smsForm = ref({ phone: '', smsCode: '' })
const smsRules = {
  phone: { rules: [{ required: true, errorMessage: '请输入手机号' }, { pattern: PHONE_REGEX, errorMessage: '请输入正确的手机号' }] },
  smsCode: { rules: [{ required: true, errorMessage: '请输入验证码' }, { pattern: SMS_CODE_REGEX, errorMessage: '请输入6位验证码' }] }
}

// ==================== 事件处理 ====================

function onModeChange(e) {
  loginMode.value = e.currentIndex
}

async function onLogin() {
  try {
    if (loginMode.value === 0) {
      // 密码登录
      await passwordFormRef.value.validate()
      await passwordLogin(passwordForm.value.phone, passwordForm.value.password)
    } else {
      // 验证码登录
      await smsFormRef.value.validate()
      await smsCodeLogin(smsForm.value.phone, smsForm.value.smsCode)
    }
  } catch (err) {
    // 表单校验失败或登录接口错误（已由 interceptor 处理）
    console.warn('登录失败:', err.message)
  }
}

async function onSendSms() {
  const phone = smsForm.value.phone
  if (!PHONE_REGEX.test(phone)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  try {
    await sendSmsCode(phone)
    uni.showToast({ title: '验证码已发送', icon: 'success' })
  } catch {
    // 错误已由 interceptor 处理
  }
}

function goTo(url) {
  uni.navigateTo({ url })
}

onUnmounted(() => {
  clearSmsTimer()
})
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  padding: 0 $spacing-xl;
}

.login-header {
  padding-top: 180rpx;
  padding-bottom: 80rpx;
  text-align: center;
}

.login-title {
  font-size: 56rpx;
  font-weight: $font-weight-bold;
  color: $color-text-primary;
}

.login-subtitle {
  display: block;
  font-size: $font-size-base;
  color: $color-text-secondary;
  margin-top: $spacing-sm;
}

.login-form {
  margin-top: $spacing-xl;
  background: #FFFFFF;
  border-radius: $radius-lg;
  padding: $spacing-lg $spacing-md;
  box-shadow: 0 8rpx 32rpx rgba(0,0,0,0.08);
}

.form-section {
  margin-top: $spacing-lg;
  padding: 0 $spacing-md;
  border: 1rpx solid $color-border-light;
  border-radius: $radius-md;
}

.sms-input-row {
  display: flex;
  align-items: center;
}

.sms-input {
  flex: 1;
}

.sms-btn {
  padding: 16rpx 24rpx;
  white-space: nowrap;

  &.disabled {
    opacity: 0.5;
  }
}

.sms-btn-text {
  font-size: $font-size-sm;
  color: $color-primary;
}

.login-btn {
  margin-top: $spacing-xl;
  background-color: $color-primary;
  color: $color-text-white;
  border: none;
  border-radius: $radius-round;
  font-size: $font-size-lg;
  height: 88rpx;
  line-height: 88rpx;

  &::after {
    border: none;
  }
}

.login-links {
  display: flex;
  justify-content: space-between;
  margin-top: $spacing-lg;
  padding: 0 $spacing-md;
}

.link-text {
  font-size: $font-size-sm;
  color: $color-info;
}
</style>
