import { get, post, put, del } from '@/utils/request.js'

export function getChildList() {
  return get('/api/v1/user/children')
}

export function addChild(data) {
  return post('/api/v1/user/children', data, { showLoading: true })
}

export function updateChild(childId, data) {
  return put(`/api/v1/user/children/${childId}`, data, { showLoading: true })
}

export function deleteChild(childId) {
  return del(`/api/v1/user/children/${childId}`, {}, { showLoading: true })
}

export function getChildDetail(childId) {
  return get(`/api/v1/user/children/${childId}`)
}
