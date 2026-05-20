import { get } from '@/utils/request.js'

export function getVaccinationStats(params) {
  return get('/api/v1/admin/stats/vaccination', params)
}

export function getStockStats(params) {
  return get('/api/v1/admin/stats/stock', params)
}

export function getEfficiencyStats(params) {
  return get('/api/v1/admin/stats/efficiency', params)
}

export function getAnomalyStats(params) {
  return get('/api/v1/admin/stats/anomaly', params)
}
