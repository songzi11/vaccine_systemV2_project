import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { post, get, put } from '@/utils/request.js'
import {
  setToken,
  getToken,
  removeToken,
  markManualLogout,
  setUserInfoCache,
  getUserInfoCache,
  removeUserInfoCache
} from '@/utils/auth.js'
import { ROLE_CODES, FLOW_DOCTOR_ROLES, ADMIN_ROLES } from '@/utils/constants.js'

export const useUserStore = defineStore('user', () => {
  // ==================== State ====================
  const token = ref(getToken() || '')
  const userInfo = ref(getUserInfoCache() || null)
  const isLoggedIn = computed(() => !!token.value)

  // ==================== Getters（角色判断） ====================
  const isFlowDoctor = computed(() =>
    userInfo.value?.roles?.some(r => FLOW_DOCTOR_ROLES.includes(r)) || false
  )
  const isSigninDoctor = computed(() =>
    userInfo.value?.roles?.includes(ROLE_CODES.DOCTOR_SIGNIN) || false
  )
  const isPrecheckDoctor = computed(() =>
    userInfo.value?.roles?.includes(ROLE_CODES.DOCTOR_PRECHECK) || false
  )
  const isRegisterDoctor = computed(() =>
    userInfo.value?.roles?.includes(ROLE_CODES.DOCTOR_REGISTER) || false
  )
  const isVaccinateDoctor = computed(() =>
    userInfo.value?.roles?.includes(ROLE_CODES.DOCTOR_VACCINATE) || false
  )
  const isObserveDoctor = computed(() =>
    userInfo.value?.roles?.includes(ROLE_CODES.DOCTOR_OBSERVE) || false
  )
  const isStockDoctor = computed(() =>
    userInfo.value?.roles?.includes(ROLE_CODES.DOCTOR_STOCK) || false
  )
  const isBusinessAdmin = computed(() =>
    userInfo.value?.roles?.includes(ROLE_CODES.DOCTOR_BUSINESS_ADMIN) || false
  )
  const isSuperAdmin = computed(() =>
    userInfo.value?.roles?.includes(ROLE_CODES.SUPER_ADMIN) || false
  )
  const isAdminRole = computed(() =>
    userInfo.value?.roles?.some(r => ADMIN_ROLES.includes(r)) || false
  )
  const isDoctor = computed(() =>
    userInfo.value?.roles?.some(r => r.startsWith('DOCTOR_')) || false
  )
  const isUser = computed(() => {
    const roles = userInfo.value?.roles || []
    return roles.includes(ROLE_CODES.USER) && !isDoctor.value && !isAdminRole.value
  })
  const currentWindowName = computed(() => userInfo.value?.windowName || null)
  const currentWindowCode = computed(() => userInfo.value?.windowCode || null)

  /**
   * 根据角色返回 TabBar 配置
   * 家长端使用原生 TabBar（不经过此 getter）
   * 医生端和管理员端使用 CustomTabBar（通过此 getter 获取配置）
   */
  const tabBarConfig = computed(() => {
    const roles = userInfo.value?.roles || []
    if (roles.includes(ROLE_CODES.SUPER_ADMIN)) {
      return [
        { text: '首页', icon: 'home', path: '/pages/index/index' },
        { text: '我的', icon: 'person', path: '/pages/mine/index' }
      ]
    }
    if (roles.includes(ROLE_CODES.DOCTOR_BUSINESS_ADMIN)) {
      return [
        { text: '首页', icon: 'home', path: '/pages/index/index' },
        { text: '我的', icon: 'person', path: '/pages/mine/index' }
      ]
    }
    // 流程医生（签到/预检/登记/接种/留观）— 工作台指向各自队列
    if (roles.some(r => FLOW_DOCTOR_ROLES.includes(r))) {
      let workPath = '/pages/queue/precheck'
      if (roles.includes(ROLE_CODES.DOCTOR_SIGNIN)) workPath = '/pages/queue/signin'
      if (roles.includes(ROLE_CODES.DOCTOR_REGISTER)) workPath = '/pages/queue/vaccinate'
      if (roles.includes(ROLE_CODES.DOCTOR_VACCINATE)) workPath = '/pages/queue/vaccinate'
      if (roles.includes(ROLE_CODES.DOCTOR_OBSERVE)) workPath = '/pages/queue/observe'
      return [
        { text: '首页', icon: 'home', path: '/pages/index/index' },
        { text: '工作台', icon: 'list', path: workPath },
        { text: '记录', icon: 'document', path: '/pages/record/list' },
        { text: '我的', icon: 'person', path: '/pages/mine/index' }
      ]
    }
    // 库存医生
    if (roles.includes(ROLE_CODES.DOCTOR_STOCK)) {
      return [
        { text: '首页', icon: 'home', path: '/pages/index/index' },
        { text: '库存', icon: 'box', path: '/pages/stock/summary' },
        { text: '批次', icon: 'grid', path: '/pages/stock/batches' },
        { text: '我的', icon: 'person', path: '/pages/mine/index' }
      ]
    }
    // 默认（不应到达）
    return []
  })

  // ==================== Actions ====================

  /**
   * 登录
   * @param {string} phone
   * @param {string} password
   * @returns {Promise<{token: string, userInfo: Object}>}
   */
  async function login(phone, password) {
    const data = await post('/api/v1/public/auth/login', { phone, password }, { needToken: false })
    const jwt = data.token
    token.value = jwt
    setToken(jwt)
    // 登录成功后拉取用户信息
    await fetchProfile()
    return data
  }

  /**
   * 登出
   */
  async function logout() {
    markManualLogout()
    token.value = ''
    userInfo.value = null
    removeToken()
    removeUserInfoCache()
    uni.reLaunch({ url: '/pages/auth/login' })
  }

  /**
   * 拉取用户信息
   */
  async function fetchProfile() {
    const data = await get('/api/v1/user/auth/me')
    userInfo.value = data
    setUserInfoCache(data)
    return data
  }

  /**
   * 更新用户信息
   */
  async function updateProfile(profileData) {
    const data = await put('/api/v1/user/profile', profileData)
    userInfo.value = { ...userInfo.value, ...data }
    setUserInfoCache(userInfo.value)
    return data
  }

  /**
   * 短信验证码登录
   * @param {string} phone
   * @param {string} smsCode
   */
  async function smsLogin(phone, smsCode) {
    const data = await post('/api/v1/public/auth/sms-login', { phone, smsCode }, { needToken: false })
    const jwt = data.token
    token.value = jwt
    setToken(jwt)
    await fetchProfile()
    return data
  }

  return {
    // State
    token,
    userInfo,
    isLoggedIn,
    // Getters
    isUser,
    isFlowDoctor,
    isSigninDoctor,
    isPrecheckDoctor,
    isRegisterDoctor,
    isVaccinateDoctor,
    isObserveDoctor,
    isStockDoctor,
    isBusinessAdmin,
    isSuperAdmin,
    isAdminRole,
    isDoctor,
    tabBarConfig,
    currentWindowName,
    currentWindowCode,
    // Actions
    login,
    smsLogin,
    logout,
    fetchProfile,
    updateProfile
  }
})
