import { get, post, put } from '@/utils/request.js'

export function getUserList(params) {
  return get('/api/v1/admin/users', params)
}

export function freezeUser(userId) {
  return put(`/api/v1/admin/users/${userId}/freeze`, {}, { showLoading: true })
}

export function unfreezeUser(userId) {
  return put(`/api/v1/admin/users/${userId}/unfreeze`, {}, { showLoading: true })
}
