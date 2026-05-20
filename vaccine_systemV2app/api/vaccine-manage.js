import { get, post, put, del } from '@/utils/request.js'

export function getVaccineList(params) {
  return get('/api/v1/admin/vaccines', params)
}

export function createVaccine(data) {
  return post('/api/v1/admin/vaccines', data, { showLoading: true })
}

export function updateVaccine(id, data) {
  return put(`/api/v1/admin/vaccines/${id}`, data, { showLoading: true })
}

export function deleteVaccine(id) {
  return del(`/api/v1/admin/vaccines/${id}`, {}, { showLoading: true })
}

export function updateShelfStatus(id, status) {
  return put(`/api/v1/admin/vaccines/${id}/shelf-status`, { status }, { showLoading: true })
}
