import { get, post } from '@/utils/request.js'

export function getTodayList(params) {
  return get('/api/v1/precheck/queue', params, { showLoading: false })
}

export function executeSignin(data) {
  return post('/api/v1/precheck/signin', data, { showLoading: true })
}
