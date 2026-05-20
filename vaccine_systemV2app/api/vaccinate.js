import { get, post } from '@/utils/request.js'

export function getQueue(params) {
  return get('/api/v1/vaccinate/queue', params, { showLoading: false })
}

export function verifyInfo(appointmentId) {
  return get(`/api/v1/vaccinate/${appointmentId}/verify`)
}

export function executeVaccinate(data) {
  return post('/api/v1/vaccinate/execute', data, { showLoading: true })
}

export function getRecords(params) {
  return get('/api/v1/user/vaccination-records', params)
}

export function getChildRecords(childId) {
  return get(`/api/v1/user/children/${childId}/vaccination-records`)
}

export function getFefoBatch(vaccineId) {
  return get(`/api/v1/vaccinate/fefo-batch/${vaccineId}`)
}
