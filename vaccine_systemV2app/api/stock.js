import { get, post, put } from '@/utils/request.js'

export function getSummary() {
  return get('/api/v1/stock/hospital')
}

export function getBatches(params) {
  return get('/api/v1/stock/batches', params)
}

export function getBatchDetail(batchId) {
  return get(`/api/v1/stock/batches/${batchId}`)
}

export function createTransfer(data) {
  return post('/api/v1/stock/transfer', data, { showLoading: true })
}

export function getTransferRecords(params) {
  return get('/api/v1/stock/transfer/records', params)
}

export function disposeBatch(batchId, data) {
  return post(`/api/v1/stock/batches/${batchId}/dispose`, data, { showLoading: true })
}

export function getAlerts(params) {
  return get('/api/v1/stock/alerts', params)
}

export function handleAlert(alertId) {
  return put(`/api/v1/stock/alerts/${alertId}/handle`)
}

export function createBatch(data) {
  return post('/api/v1/stock/batches', data, { showLoading: true })
}
