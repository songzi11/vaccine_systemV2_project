import { get, post, put, del } from '@/utils/request.js'

export function getWindowList(params) {
  return get('/api/v1/admin/windows', params)
}

export function createWindow(data) {
  return post('/api/v1/admin/windows', data, { showLoading: true })
}

export function updateWindow(id, data) {
  return put(`/api/v1/admin/windows/${id}`, data, { showLoading: true })
}

export function deleteWindow(id) {
  return del(`/api/v1/admin/windows/${id}`, {}, { showLoading: true })
}

export function saveWindowService(windowId, data) {
  return post(`/api/v1/admin/windows/${windowId}/service`, data, { showLoading: true })
}

export function getWindowService(windowCode) {
  return get(`/api/v1/admin/windows/service/${windowCode}`)
}

export function getWindowsByType(functionType) {
  return get('/api/v1/admin/windows/type', { functionType })
}

export function assignDoctorToWindow(windowId, doctorId) {
  return post(`/api/v1/admin/windows/${windowId}/assign-doctor`, { doctorId }, { showLoading: true })
}

export function removeDoctorFromWindow(windowId) {
  return post(`/api/v1/admin/windows/${windowId}/remove-doctor`, {}, { showLoading: true })
}
