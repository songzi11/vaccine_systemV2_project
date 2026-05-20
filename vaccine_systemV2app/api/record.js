import { get } from '@/utils/request.js'

export function getRecordList(params) {
  return get('/api/v1/user/vaccination-records', params)
}

export function getRecordDetail(id) {
  return get(`/api/v1/user/vaccination-records/${id}`)
}

export function getChildRecords(childId, params) {
  return get(`/api/v1/user/children/${childId}/vaccination-records`, params)
}
