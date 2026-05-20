import { ref } from 'vue'
import { POLLING_INTERVAL } from '@/utils/constants.js'

export function useQueue(fetchFn, interval = POLLING_INTERVAL.QUEUE) {
  const list = ref([])
  const loading = ref(false)
  const error = ref(null)
  let timer = null
  let isFirstLoad = true

  async function refresh() {
    if (isFirstLoad) {
      loading.value = true
      isFirstLoad = false
    }
    error.value = null
    try {
      list.value = await fetchFn()
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  function startPolling() {
    isFirstLoad = true
    refresh()
    timer = setInterval(refresh, interval)
  }

  function stopPolling() {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
  }

  return { list, loading, error, refresh, startPolling, stopPolling }
}
