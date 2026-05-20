<template>
  <view class="register-page">
    <uni-nav-bar title="注册" :border="false" />

    <!-- 注册须知 -->
    <view class="notice-card">
      <text class="notice-text">注册即表示您同意《用户服务协议》和《隐私政策》</text>
    </view>

    <!-- 角色选择 -->
    <view class="role-selector">
      <view class="role-option" :class="{ active: form.roleType === 'PARENT' }" @tap="form.roleType = 'PARENT'">
        <text>家长注册</text>
      </view>
      <view class="role-option" :class="{ active: form.roleType === 'DOCTOR' }" @tap="form.roleType = 'DOCTOR'">
        <text>医生注册</text>
      </view>
    </view>

    <!-- 表单 -->
    <view class="form-section">
      <uni-forms ref="formRef" :modelValue="form" :rules="rules" validate-trigger="bind">
        <!-- 手机号 -->
        <uni-forms-item label="手机号" name="phone" required>
          <uni-easyinput v-model="form.phone" type="number" maxlength="11" placeholder="请输入手机号" />
        </uni-forms-item>

        <!-- 注册验证码（仅医生） -->
        <uni-forms-item v-if="form.roleType === 'DOCTOR'" label="注册验证码" name="verifyCode" required>
          <uni-easyinput v-model="form.verifyCode" type="number" maxlength="6" placeholder="请输入6位注册验证码" />
          <text class="verify-hint">请向业务主管索取验证码</text>
        </uni-forms-item>

        <!-- 密码 -->
        <uni-forms-item label="密码" name="password" required>
          <uni-easyinput v-model="form.password" type="password" placeholder="6-20位，需含字母和数字" />
        </uni-forms-item>

        <!-- 确认密码 -->
        <uni-forms-item label="确认密码" name="confirmPassword" required>
          <uni-easyinput v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" />
        </uni-forms-item>

        <!-- 真实姓名 -->
        <uni-forms-item label="真实姓名" name="realName" required>
          <uni-easyinput v-model="form.realName" placeholder="2-50位中文" />
        </uni-forms-item>
      </uni-forms>

      <!-- 注册按钮 -->
      <button class="submit-btn" :loading="submitting" @tap="handleRegister">注册</button>

      <!-- 底部链接 -->
      <view class="bottom-links">
        <text class="link" @tap="navigateBack">已有账号？去登录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { register } from '@/api/auth.js'

const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  roleType: 'PARENT',
  phone: '',
  password: '',
  confirmPassword: '',
  realName: '',
  verifyCode: ''
})

const rules = {
  phone: { rules: [{ required: true, errorMessage: '请输入手机号' }, { pattern: /^1[3-9]\d{9}$/, errorMessage: '手机号格式不正确' }] },
  verifyCode: { rules: [{ required: true, errorMessage: '请输入验证码' }, { pattern: /^\d{6}$/, errorMessage: '验证码为6位数字' }] },
  password: { rules: [{ required: true, errorMessage: '请输入密码' }, { pattern: /^(?=.*[a-zA-Z])(?=.*\d).{6,20}$/, errorMessage: '6-20位，需含字母和数字' }] },
  confirmPassword: { rules: [{ required: true, errorMessage: '请确认密码' }, { validateFunction: (rule, value, data, callback) => { if (value !== form.password) { callback('两次密码不一致') } return true } }] },
  realName: { rules: [{ required: true, errorMessage: '请输入真实姓名' }, { pattern: /^[\u4e00-\u9fa5·]{2,50}$/, errorMessage: '姓名仅支持中文，2-50位' }] }
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data = { phone: form.phone, password: form.password, realName: form.realName, roleType: form.roleType }
    if (form.roleType === 'DOCTOR') data.verifyCode = form.verifyCode
    await register(data)
    uni.showToast({ title: '注册成功', icon: 'success' })
    setTimeout(() => navigateBack(), 1500)
  } catch (e) {
    // 错误已由 interceptor 处理
  } finally {
    submitting.value = false
  }
}

function navigateBack() {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  background-color: $color-bg-page;
  padding: 0 $spacing-lg;

  .notice-card {
    margin: $spacing-lg $spacing-lg 0;
    padding: $spacing-md $spacing-lg;
    background-color: $color-primary-light;
    border-radius: $radius-md;

    .notice-text {
      font-size: $font-size-sm;
      color: $color-text-secondary;
      line-height: 1.6;
    }
  }

  .role-selector {
    display: flex;
    margin: $spacing-lg $spacing-lg 0;
    gap: $spacing-md;

    .role-option {
      flex: 1;
      text-align: center;
      padding: $spacing-md 0;
      background: $color-bg-white;
      border-radius: $radius-lg;
      font-size: $font-size-base;
      color: $color-text-secondary;
      border: 2rpx solid $color-border;
      transition: all 0.2s;

      &.active {
        background: $color-primary;
        color: #FFFFFF;
        border-color: $color-primary;
        font-weight: 600;
      }
    }
  }

  .form-section {
    padding: $spacing-lg;
    background-color: $color-bg-white;
    border-radius: $radius-lg;
    margin-top: $spacing-lg;

    .verify-hint {
      font-size: $font-size-xs;
      color: $color-text-placeholder;
      margin-top: $spacing-xs;
      display: block;
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
