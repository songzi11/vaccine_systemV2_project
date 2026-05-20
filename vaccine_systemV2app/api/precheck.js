import { get, post } from '@/utils/request.js'

export function getQueue(params) {
  return get('/api/v1/precheck/queue', params, { showLoading: false })
}

export function executePrecheck(data) {
  return post('/api/v1/precheck/assess', data, { showLoading: true })
}

export function getPreCheckRecord(appointmentId) {
  return get(`/api/v1/precheck/records/${appointmentId}`)
}
