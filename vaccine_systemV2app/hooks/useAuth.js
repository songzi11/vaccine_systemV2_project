/**
 * 认证 Hook
 * 封装登录/登出逻辑，供页面组件使用
 */
import { ref } from 'vue'
import { useUserStore } from '@/store/user.js'
import { post } from '@/utils/request.js'
import { ERROR_CODES } from '@/utils/constants.js'

export function useAuth() {
  const userStore = useUserStore()
  const loading = ref(false)
  const smsLoading = ref(false)
  const smsCountdown = ref(0)
  let smsTimer = null

  /**
   * 密码登录
   * @param {string} phone
   * @param {string} password
   */
  async function passwordLogin(phone, password) {
    loading.value = true
    try {
      await userStore.login(phone, password)
      navigateAfterLogin()
    } catch (err) {
      // 错误已由 request.js 的 handleBusinessError 处理
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 短信验证码登录
   * @param {string} phone
   * @param {string} smsCode
   */
  async function smsCodeLogin(phone, smsCode) {
    loading.value = true
    try {
      await userStore.smsLogin(phone, smsCode)
      navigateAfterLogin()
    } catch (err) {
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 发送短信验证码
   * @param {string} phone
   */
  async function sendSmsCode(phone) {
    if (smsCountdown.value > 0) return

    smsLoading.value = true
    try {
      await post('/api/v1/public/sms/send', { phone }, { needToken: false })
      startCountdown()
    } catch (err) {
      throw err
    } finally {
      smsLoading.value = false
    }
  }

  /**
   * 登出
   */
  async function logout() {
    try {
      await userStore.logout()
    } catch (err) {
      console.error('登出失败:', err)
    }
  }

  /**
   * 登录后导航
   */
  function navigateAfterLogin() {
    const roles = userStore.userInfo?.roles || []
    const redirect = uni.getStorageSync('loginRedirect')

    if (redirect) {
      uni.removeStorageSync('loginRedirect')
      uni.reLaunch({ url: redirect })
      return
    }

    const isDoctor = roles.some(role => role.startsWith('DOCTOR_'))
    const isAdmin = roles.includes('SUPER_ADMIN') || roles.includes('DOCTOR_BUSINESS_ADMIN')

    if (isDoctor && !isAdmin) {
      uni.reLaunch({ url: '/pages/index/index' })
      return
    }

    uni.switchTab({ url: '/pages/index/index' })
  }

  /**
   * 短信倒计时
   */
  function startCountdown() {
    smsCountdown.value = 60
    smsTimer = setInterval(() => {
      smsCountdown.value--
      if (smsCountdown.value <= 0) {
        clearInterval(smsTimer)
        smsTimer = null
      }
    }, 1000)
  }

  /** 清理倒计时 */
  function clearSmsTimer() {
    if (smsTimer) {
      clearInterval(smsTimer)
      smsTimer = null
      smsCountdown.value = 0
    }
  }

  return {
    loading,
    smsLoading,
    smsCountdown,
    passwordLogin,
    smsCodeLogin,
    sendSmsCode,
    logout,
    clearSmsTimer
  }
}
