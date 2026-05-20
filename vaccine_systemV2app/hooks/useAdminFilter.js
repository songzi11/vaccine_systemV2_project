import { ref, computed } from 'vue'
import { useAdminStore } from '@/store/admin.js'

export function useAdminFilter(module) {
  const adminStore = useAdminStore()
  const keyword = ref('')
  const filtersLoaded = ref(false)

  const currentFilters = computed(() => {
    const map = { schedule: adminStore.scheduleFilters, window: adminStore.windowFilters, vaccine: adminStore.vaccineFilters, notice: adminStore.noticeFilters }
    return map[module] || {}
  })

  function applyFilters(newFilters) {
    adminStore.saveFilters(module, { ...newFilters, page: 1 })
  }

  function resetAll() {
    keyword.value = ''
    adminStore.resetFilters(module)
  }

  function initFromStore() {
    if (!filtersLoaded.value) {
      keyword.value = currentFilters.value.keyword || ''
      filtersLoaded.value = true
    }
  }

  return { keyword, currentFilters, applyFilters, resetAll, initFromStore }
}
