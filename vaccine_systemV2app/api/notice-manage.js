import { get, post, put, del } from '@/utils/request.js'

export function getNoticeList(params) {
  return get('/api/v1/admin/notices', params)
}

export function publishNotice(data) {
  return post('/api/v1/admin/notices', data, { showLoading: true })
}

export function deleteNotice(noticeId) {
  return del(`/api/v1/admin/notices/${noticeId}`, {}, { showLoading: true })
}

export function getNoticeFeedback(noticeId, params) {
  return get(`/api/v1/admin/notices/${noticeId}/feedback`, params)
}

export function updateNotice(id, data) {
  return put(`/api/v1/admin/notices/${id}`, data, { showLoading: true })
}
