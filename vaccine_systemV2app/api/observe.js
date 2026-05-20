import { get, post, put } from '@/utils/request.js'

export function getQueue(params) {
  return get('/api/v1/observe/queue', params, { showLoading: false })
}

export function getStatus(injectionId) {
  return get(`/api/v1/observe/${injectionId}`)
}

export function finishObserve(appointmentId, data) {
  return put(`/api/v1/observe/${appointmentId}/finish`, data || {}, { showLoading: true })
}

export function reportAdverse(data) {
  return post('/api/v1/observe/adverse-reaction', data, { showLoading: true })
}

export function handleAdverse(reactionId, data) {
  return put(`/api/v1/observe/adverse-reaction/${reactionId}/handle`, data, { showLoading: true })
}

export function startObserve(data) {
  return post('/api/v1/observe/start', data, { showLoading: true })
}

export function getAdverseReactions(observeRecordId) {
  return get(`/api/v1/observe/adverse-reaction/${observeRecordId}`)
}
