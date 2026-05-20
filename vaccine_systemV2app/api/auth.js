/**
 * 认证相关 API
 * 对应 API 接口设计文档中的公开接口和用户接口
 */
import { post, get, put } from '@/utils/request.js'

/** 发送短信验证码 */
export function sendSmsCode(phone) {
  return post('/api/v1/public/auth/sms-code', { phone }, { needToken: false })
}

/** 密码登录 */
export function loginByPassword(phone, password) {
  return post('/api/v1/public/auth/login', { phone, password }, { needToken: false })
}

/** 短信验证码登录 */
export function loginBySmsCode(phone, smsCode) {
  return post('/api/v1/public/auth/sms-login', { phone, smsCode }, { needToken: false })
}

/** 用户注册 */
export function register(data) {
  return post('/api/v1/public/auth/register', data, { needToken: false })
}

/** 用户登出 */
export function logout() {
  return post('/api/v1/user/logout')
}

/** 修改密码 */
export function changePassword(oldPassword, newPassword) {
  return put('/api/v1/user/password', { oldPassword, newPassword })
}

/** 重置密码（短信验证码方式） */
export function resetPassword(phone, smsCode, newPassword) {
  return post('/api/v1/user/password/reset', { phone, smsCode, newPassword }, { needToken: false })
}

/** 获取用户信息 */
export function getUserProfile() {
  return get('/api/v1/user/auth/me')
}

/** 更新用户信息 */
export function updateUserProfile(data) {
  return put('/api/v1/user/profile', data)
}
