import { get, put } from '@/utils/request.js'

export function getConfigList() {
  return get('/api/v1/admin/configs')
}

export function updateConfig(id, value) {
  return put(`/api/v1/admin/configs/${id}`, { configValue: value }, { showLoading: true })
}
