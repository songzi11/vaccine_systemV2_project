import { get } from '@/utils/request.js'

export function getVaccineList(params) {
  return get('/api/v1/public/vaccines', params, { needToken: false })
}
