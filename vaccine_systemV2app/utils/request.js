/**
 * HTTP 请求封装
 * - JWT Token 自动注入（Authorization: Bearer <token>）
 * - X-Request-Id 防重放（UUID v4）
 * - 统一错误处理（业务码 + HTTP状态码）
 * - Loading 控制
 */

import { getToken, removeToken, isManualLogout } from './auth.js'

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

// 请求队列，用于 loading 控制
let requestCount = 0

function showLoading() {
  if (requestCount === 0) {
    uni.showLoading({ title: '加载中...', mask: true })
  }
  requestCount++
}

function hideLoading() {
  requestCount--
  if (requestCount <= 0) {
    requestCount = 0
    uni.hideLoading()
  }
}

/**
 * 生成 UUID v4
 */
function generateUUID() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = Math.random() * 16 | 0
    const v = c === 'x' ? r : (r & 0x3 | 0x8)
    return v.toString(16)
  })
}

/**
 * 核心请求方法
 * @param {Object} options - 请求配置
 * @param {string} options.url - 请求路径（如 /api/v1/user/profile）
 * @param {string} options.method - HTTP 方法（GET/POST/PUT/DELETE）
 * @param {Object} options.data - 请求参数（GET 为 query，POST/PUT 为 body）
 * @param {boolean} options.showLoading - 是否显示 loading（默认 true）
 * @param {boolean} options.needToken - 是否需要 Token（默认 true）
 * @param {boolean} options.needRequestId - 是否需要防重放 ID（默认 false）
 * @returns {Promise<any>} 响应 data 字段
 */
function request(options) {
  const {
    url,
    method = 'GET',
    data = {},
    showLoading: show = false,
    needToken = true,
    needRequestId = false
  } = options

  if (show) showLoading()

  const header = {
    'Content-Type': 'application/json'
  }

  // Token 注入
  if (needToken) {
    const token = getToken()
    if (token) {
      header['Authorization'] = `Bearer ${token}`
    }
  }

  // 防重放 ID（写操作）
  if (needRequestId) {
    header['X-Request-Id'] = generateUUID()
  }

  return new Promise((resolve, reject) => {
    uni.request({
      url: `${BASE_URL}${url}`,
      method,
      data,
      header,
      timeout: 10000,
      success(res) {
        if (show) hideLoading()

        const { statusCode, data: responseData } = res

        // HTTP 层错误
        if (statusCode === 401) {
          handleUnauthorized()
          reject(new Error('登录已过期，请重新登录'))
          return
        }
        if (statusCode === 403) {
          handleForbidden()
          reject(new Error('无操作权限'))
          return
        }
        if (statusCode === 429) {
          uni.showToast({ title: '操作过于频繁', icon: 'none' })
          reject(new Error('操作过于频繁'))
          return
        }
        if (statusCode >= 500) {
          uni.showToast({ title: '系统异常，请稍后重试', icon: 'none' })
          reject(new Error('系统异常'))
          return
        }

        // 业务层错误
        if (responseData.code !== undefined && responseData.code !== 200) {
          handleBusinessError(responseData.code, responseData.message)
          reject(new Error(responseData.message || '请求失败'))
          return
        }

        // 成功：返回 data 字段
        resolve(responseData.data !== undefined ? responseData.data : responseData)
      },
      fail(err) {
        if (show) hideLoading()

        if (err.errMsg && err.errMsg.includes('request:fail')) {
          uni.showToast({ title: '网络连接失败，请检查网络', icon: 'none' })
        }
        reject(err)
      }
    })
  })
}

/**
 * 处理 401 未授权
 */
function handleUnauthorized() {
  removeToken()
  if (isManualLogout()) {
    return
  }
  uni.showToast({ title: '登录已过期', icon: 'none' })
  setTimeout(() => {
    uni.reLaunch({ url: '/pages/auth/login' })
  }, 1500)
}

/**
 * 处理 403 禁止访问
 * 清除本地 token 并跳转登录页，避免无限弹窗
 */
function handleForbidden() {
  removeToken()
  uni.showToast({ title: '无操作权限，请重新登录', icon: 'none' })
  setTimeout(() => {
    uni.reLaunch({ url: '/pages/auth/login' })
  }, 1500)
}

/**
 * 处理业务错误码
 * @param {number} code - 业务错误码
 * @param {string} message - 错误消息
 */
function handleBusinessError(code, message) {
  // Token 过期/无效
  if (code === 1001) {
    handleUnauthorized()
    return
  }

  // 默认显示服务端返回的消息
  uni.showToast({ title: message || '操作失败', icon: 'none' })
}

/**
 * GET 请求
 */
export function get(url, data = {}, options = {}) {
  return request({ url, method: 'GET', data, ...options })
}

/**
 * POST 请求
 */
export function post(url, data = {}, options = {}) {
  return request({ url, method: 'POST', data, needRequestId: true, ...options })
}

/**
 * PUT 请求
 */
export function put(url, data = {}, options = {}) {
  return request({ url, method: 'PUT', data, needRequestId: true, ...options })
}

/**
 * DELETE 请求
 */
export function del(url, data = {}, options = {}) {
  return request({ url, method: 'DELETE', data, needRequestId: true, ...options })
}

/**
 * 上传文件
 */
export function upload(url, filePath, formData = {}, options = {}) {
  const { showLoading: show = true, needToken = true } = options
  if (show) showLoading()

  const header = {}
  if (needToken) {
    const token = getToken()
    if (token) header['Authorization'] = `Bearer ${token}`
  }

  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: `${BASE_URL}${url}`,
      filePath,
      name: 'file',
      formData,
      header,
      success(res) {
        if (show) hideLoading()
        try {
          const data = JSON.parse(res.data)
          if (data.code === 200) {
            resolve(data.data)
          } else {
            handleBusinessError(data.code, data.message)
            reject(new Error(data.message))
          }
        } catch (e) {
          reject(e)
        }
      },
      fail(err) {
        if (show) hideLoading()
        reject(err)
      }
    })
  })
}

export default request
