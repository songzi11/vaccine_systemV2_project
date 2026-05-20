import { defineStore } from 'pinia'
import { reactive } from 'vue'

export const useAdminStore = defineStore('admin', () => {
  const scheduleFilters = reactive({ doctorId: '', windowId: '', date: '', page: 1, size: 20 })
  const windowFilters = reactive({ functionType: '', status: '', page: 1, size: 20 })
  const vaccineFilters = reactive({ category: '', status: '', page: 1, size: 20 })
  const noticeFilters = reactive({ type: '', status: '', page: 1, size: 20 })

  function saveFilters(module, filters) {
    const target = { schedule: scheduleFilters, window: windowFilters, vaccine: vaccineFilters, notice: noticeFilters }[module]
    if (target) Object.assign(target, filters)
  }

  function resetFilters(module) {
    const defaults = { schedule: { doctorId: '', windowId: '', date: '', page: 1, size: 20 }, window: { functionType: '', status: '', page: 1, size: 20 }, vaccine: { category: '', status: '', page: 1, size: 20 }, notice: { type: '', status: '', page: 1, size: 20 } }
    const target = { schedule: scheduleFilters, window: windowFilters, vaccine: vaccineFilters, notice: noticeFilters }[module]
    if (target && defaults[module]) Object.assign(target, defaults[module])
  }

  return { scheduleFilters, windowFilters, vaccineFilters, noticeFilters, saveFilters, resetFilters }
})
