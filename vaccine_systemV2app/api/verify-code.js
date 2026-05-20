import { get, post, put } from '@/utils/request.js'

export function generateVerifyCode() {
  return post('/api/v1/admin/verify-codes', {}, { showLoading: true })
}

export function getVerifyCodeList() {
  return get('/api/v1/admin/verify-codes')
}

export function revokeVerifyCode(id) {
  return put(`/api/v1/admin/verify-codes/${id}/revoke`, {}, { showLoading: true })
}
