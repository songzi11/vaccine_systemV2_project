/**
 * 格式化工具函数
 */

/**
 * 格式化日期
 * @param {string|Date} date - 日期
 * @param {string} fmt - 格式模板（默认 YYYY-MM-DD HH:mm:ss）
 * @returns {string}
 */
export function formatDate(date, fmt = 'YYYY-MM-DD HH:mm:ss') {
  if (!date) return ''
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''

  const map = {
    'YYYY': d.getFullYear(),
    'MM': String(d.getMonth() + 1).padStart(2, '0'),
    'DD': String(d.getDate()).padStart(2, '0'),
    'HH': String(d.getHours()).padStart(2, '0'),
    'mm': String(d.getMinutes()).padStart(2, '0'),
    'ss': String(d.getSeconds()).padStart(2, '0')
  }

  let result = fmt
  for (const [key, value] of Object.entries(map)) {
    result = result.replace(key, value)
  }
  return result
}

/**
 * 格式化相对时间（刚刚/X分钟前/X小时前/X天前）
 * @param {string|Date} date
 * @returns {string}
 */
export function formatRelativeTime(date) {
  if (!date) return ''
  const d = new Date(date)
  const now = new Date()
  const diff = now.getTime() - d.getTime()

  if (diff < 60 * 1000) return '刚刚'
  if (diff < 60 * 60 * 1000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 24 * 60 * 60 * 1000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 30 * 24 * 60 * 60 * 1000) return `${Math.floor(diff / 86400000)}天前`
  return formatDate(date, 'YYYY-MM-DD')
}

/**
 * 手机号脱敏：138****0001
 * @param {string} phone
 * @returns {string}
 */
export function maskPhone(phone) {
  if (!phone || phone.length < 7) return phone || ''
  return phone.slice(0, 3) + '****' + phone.slice(-4)
}

/**
 * 身份证号脱敏：320***********1234
 * @param {string} idCard
 * @returns {string}
 */
export function maskIdCard(idCard) {
  if (!idCard || idCard.length < 8) return idCard || ''
  return idCard.slice(0, 3) + '*'.repeat(idCard.length - 7) + idCard.slice(-4)
}

/**
 * 格式化有效期至
 * @param {string|Date} expiryDate
 * @returns {string}
 */
export function formatExpiryDate(expiryDate) {
  return formatDate(expiryDate, 'YYYY-MM-DD')
}

/**
 * 格式化时长（毫秒 → X分X秒）
 * @param {number} ms
 * @returns {string}
 */
export function formatDuration(ms) {
  if (ms <= 0) return '0分0秒'
  const minutes = Math.floor(ms / 60000)
  const seconds = Math.floor((ms % 60000) / 1000)
  return `${minutes}分${seconds}秒`
}

export function formatTimeSlot(timeSlot) {
  if (timeSlot === 'AM') return '上午'
  if (timeSlot === 'PM') return '下午'
  return timeSlot || ''
}
