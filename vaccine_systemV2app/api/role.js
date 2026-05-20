import { get, post, put, del } from '@/utils/request.js'

export function getRoleList(params) {
  return get('/api/v1/admin/roles', params)
}

export function createRole(data) {
  return post('/api/v1/admin/roles', data, { showLoading: true })
}

export function updateRole(id, data) {
  return put(`/api/v1/admin/roles/${id}`, data, { showLoading: true })
}

export function deleteRole(id) {
  return del(`/api/v1/admin/roles/${id}`, {}, { showLoading: true })
}

export function getRolePermissions(roleId) {
  return get(`/api/v1/admin/roles/${roleId}/permissions`)
}

export function assignRoles(userId, roleIds) {
  return post(`/api/v1/admin/users/${userId}/assign-roles`, { roleIds }, { showLoading: true })
}

export function getUserRoles(userId) {
  return get(`/api/v1/admin/users/${userId}/roles`)
}
