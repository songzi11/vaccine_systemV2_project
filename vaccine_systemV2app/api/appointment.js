import { get, post, put } from '@/utils/request.js'

export function createAppointment(data) {
  return post('/api/v1/user/appointments', data, { showLoading: true })
}

export function cancelAppointment(id) {
  return put(`/api/v1/user/appointments/${id}/cancel`, {}, { showLoading: true })
}

export function getAppointmentList(params) {
  return get('/api/v1/user/appointments', params)
}

export function getAppointmentDetail(id) {
  return get(`/api/v1/user/appointments/${id}`)
}

export function getAppointmentGuide(id) {
  return get(`/api/v1/user/appointments/${id}/guide`)
}

export function getAppointmentQueue(id) {
  return get(`/api/v1/user/appointments/${id}/queue`)
}

export function getAppointmentsByDate(date) {
  return get('/api/v1/user/appointments/by-date', { date })
}

export function getSlotAvailability(vaccineId, date) {
  return get('/api/v1/user/appointments/slot-availability', { vaccineId, date })
}

export function getMyTodayStats() {
  return get('/api/v1/user/appointments/my-stats')
}
