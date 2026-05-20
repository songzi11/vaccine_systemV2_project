import { get, post, put, del } from '@/utils/request.js'

export function getScheduleByDate(date) {
  return get('/api/v1/schedule/date', { date })
}

export function getDailyView(date) {
  return get('/api/v1/schedule/daily-view', { date })
}

export function toggleScheduleStatus(data) {
  return post('/api/v1/schedule/toggle', data, { showLoading: true })
}

export function getScheduleDetail(id) {
  return get(`/api/v1/schedule/${id}`)
}

export function createSchedule(data) {
  return post('/api/v1/schedule', data, { showLoading: true })
}

export function updateSchedule(id, data) {
  return put(`/api/v1/schedule/${id}`, data, { showLoading: true })
}

export function deleteSchedule(id) {
  return del(`/api/v1/schedule/${id}`, {}, { showLoading: true })
}

export function getWindowList() {
  return get('/api/v1/admin/windows')
}

export function getDoctorList(params) {
  return get('/api/v1/admin/users', params)
}

export function assignDoctorToWindow(userId, windowId) {
  return post(`/api/v1/admin/users/${userId}/assign-window`, { windowId }, { showLoading: true })
}
