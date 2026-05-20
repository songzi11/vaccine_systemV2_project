import { get, post } from '@/utils/request.js'

export async function getNoticeList(params) {
  const data = await get('/api/v1/user/notices', params)
  const list = Array.isArray(data) ? data : (data.records || [])
  return list.map(item => ({
    ...item,
    type: item.noticeType || item.type,
    createdAt: item.createTime || item.createdAt
  }))
}

export function submitFeedback(noticeId, data) {
  return post(`/api/v1/public/notices/${noticeId}/feedback`, data, { showLoading: true })
}
