/**
 * Token 存储与权限检查工具
 * 使用 uni.setStorageSync / uni.getStorageSync 进行持久化
 */

const TOKEN_KEY = 'token'
const USER_INFO_KEY = 'userInfo'
const MANUAL_LOGOUT_KEY = 'manualLogout'

// ==================== Token 管理 ====================

/** 存储 Token */
export function setToken(token) {
  uni.setStorageSync(TOKEN_KEY, token)
  clearManualLogout()
}

/** 获取 Token */
export function getToken() {
  return uni.getStorageSync(TOKEN_KEY) || ''
}

/** 移除 Token */
export function removeToken() {
  uni.removeStorageSync(TOKEN_KEY)
}

/** 标记当前是用户主动退出登录 */
export function markManualLogout() {
  uni.setStorageSync(MANUAL_LOGOUT_KEY, '1')
}

/** 清除主动退出登录标记 */
export function clearManualLogout() {
  uni.removeStorageSync(MANUAL_LOGOUT_KEY)
}

/** 是否正在处理用户主动退出登录 */
export function isManualLogout() {
  return uni.getStorageSync(MANUAL_LOGOUT_KEY) === '1'
}

// ==================== 用户信息缓存 ====================

/** 缓存用户信息（用于启动时快速展示） */
export function setUserInfoCache(userInfo) {
  uni.setStorageSync(USER_INFO_KEY, JSON.stringify(userInfo))
}

/** 获取缓存的用户信息 */
export function getUserInfoCache() {
  try {
    const raw = uni.getStorageSync(USER_INFO_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

/** 移除用户信息缓存 */
export function removeUserInfoCache() {
  uni.removeStorageSync(USER_INFO_KEY)
}

// ==================== 权限检查 ====================

/**
 * 检查当前用户是否拥有指定权限码
 * @param {string} permissionCode - 权限码（如 'appointment.signin'）
 * @param {Array<string>} userRoles - 用户角色列表
 * @returns {boolean}
 */
export function hasPermission(permissionCode, userRoles = []) {
  if (!permissionCode || !userRoles.length) return false

  const userInfoCache = getUserInfoCache()
  const roles = userRoles.length ? userRoles : (userInfoCache?.roles || [])
  if (!roles.length) return false

  // 从权限码推断所需角色前缀
  const permissionRoleMap = {
    'appointment.signin': ['DOCTOR_SIGNIN', 'DOCTOR_PRECHECK'],
    'appointment.confirm': ['DOCTOR_SIGNIN', 'DOCTOR_PRECHECK'],
    'appointment.book': ['USER'],
    'appointment.cancel.own': ['USER'],
    'appointment.view.today': ['DOCTOR_SIGNIN', 'DOCTOR_PRECHECK'],
    'appointment.view.queue': ['DOCTOR_PRECHECK'],
    'appointment.view.register': ['DOCTOR_REGISTER'],
    'appointment.view.vaccinate': ['DOCTOR_VACCINATE'],
    'appointment.view.observe': ['DOCTOR_OBSERVE'],
    'precheck.assess': ['DOCTOR_PRECHECK'],
    'precheck.contraindication': ['DOCTOR_PRECHECK'],
    'precheck.result.view': ['DOCTOR_PRECHECK'],
    'register.verify': ['DOCTOR_REGISTER'],
    'register.batch.assign': ['DOCTOR_REGISTER'],
    'register.queue.manage': ['DOCTOR_REGISTER'],
    'register.view': ['DOCTOR_REGISTER'],
    'register.save': ['DOCTOR_REGISTER'],
    'vaccinate.execute': ['DOCTOR_VACCINATE'],
    'vaccinate.record': ['DOCTOR_VACCINATE'],
    'vaccinate.verify': ['DOCTOR_VACCINATE'],
    'vaccinate.site.select': ['DOCTOR_VACCINATE'],
    'vaccinate.id.generate': ['DOCTOR_VACCINATE'],
    'vaccinate.view': ['DOCTOR_VACCINATE'],
    'record.view.own': ['USER', 'DOCTOR_VACCINATE'],
    'record.view.child': ['DOCTOR_VACCINATE'],
    'stock.deduct': ['DOCTOR_VACCINATE'],
    'observe.finish': ['DOCTOR_OBSERVE'],
    'observe.manage': ['DOCTOR_OBSERVE'],
    'adverse.report': ['DOCTOR_OBSERVE'],
    'adverse.handle': ['DOCTOR_OBSERVE'],
    'child.add.own': ['USER'],
    'child.edit.own': ['USER'],
    'child.delete.own': ['USER'],
    'doctor.schedule.create': ['DOCTOR_BUSINESS_ADMIN'],
    'doctor.schedule.edit': ['DOCTOR_BUSINESS_ADMIN'],
    'doctor.schedule.delete': ['DOCTOR_BUSINESS_ADMIN'],
    'doctor.schedule.view': ['DOCTOR_BUSINESS_ADMIN'],
    'window.manage': ['DOCTOR_BUSINESS_ADMIN'],
    'vaccine.catalog.manage': ['DOCTOR_BUSINESS_ADMIN'],
    'notice.manage': ['DOCTOR_BUSINESS_ADMIN'],
    'notice.audit': ['SUPER_ADMIN'],
    'stats.view': ['SUPER_ADMIN', 'DOCTOR_BUSINESS_ADMIN'],
    'user.manage': ['SUPER_ADMIN'],
    'user.freeze': ['SUPER_ADMIN']
  }

  const requiredRoles = permissionRoleMap[permissionCode]
  if (!requiredRoles) return false // 未定义映射的权限码默认拒绝

  return roles.some(role => requiredRoles.includes(role))
}

/**
 * 检查当前用户是否拥有指定角色
 * @param {string} roleCode - 角色编码
 * @param {Array<string>} userRoles - 用户角色列表
 * @returns {boolean}
 */
export function hasRole(roleCode, userRoles = []) {
  const userInfoCache = getUserInfoCache()
  const roles = userRoles.length ? userRoles : (userInfoCache?.roles || [])
  return roles.includes(roleCode)
}
