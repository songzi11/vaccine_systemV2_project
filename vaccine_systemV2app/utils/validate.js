/**
 * 表单校验规则与正则表达式
 */

/** 中国大陆手机号正则 */
export const PHONE_REGEX = /^1[3-9]\d{9}$/

/** 18位身份证号正则 */
export const ID_CARD_REGEX = /^[1-9]\d{5}(19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/

/** 6位数字验证码正则 */
export const SMS_CODE_REGEX = /^\d{6}$/

/** 密码正则：6-20位，必须包含字母和数字 */
export const PASSWORD_REGEX = /^(?=.*[a-zA-Z])(?=.*\d).{6,20}$/

/** 中文姓名正则：2-50位中文或· */
export const NAME_REGEX = /^[\u4e00-\u9fa5·]{2,50}$/

/**
 * 校验手机号
 * @param {string} phone
 * @returns {boolean}
 */
export function isPhone(phone) {
  return PHONE_REGEX.test(phone)
}

/**
 * 校验身份证号
 * @param {string} idCard
 * @returns {boolean}
 */
export function isIdCard(idCard) {
  return ID_CARD_REGEX.test(idCard)
}

/**
 * 校验密码强度
 * @param {string} password
 * @returns {{ valid: boolean, message: string }}
 */
export function checkPassword(password) {
  if (!password) return { valid: false, message: '请输入密码' }
  if (password.length < 6 || password.length > 20) {
    return { valid: false, message: '密码长度为6-20位' }
  }
  if (!PASSWORD_REGEX.test(password)) {
    return { valid: false, message: '密码需包含字母和数字' }
  }
  return { valid: true, message: '' }
}

/**
 * 校验儿童出生日期（不可超过今天）
 * @param {string} birthDate - YYYY-MM-DD
 * @returns {boolean}
 */
export function isValidBirthDate(birthDate) {
  if (!birthDate) return false
  const birth = new Date(birthDate)
  const today = new Date()
  today.setHours(23, 59, 59, 999)
  return birth.getTime() <= today.getTime()
}
