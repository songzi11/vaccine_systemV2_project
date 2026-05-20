import { defineStore } from 'pinia'

export const useQueueStore = defineStore('queue', () => {
  const currentQueue = ref([])
  const pollingTimer = ref(null)
  const isPolling = ref(false)
  const lastRefreshTime = ref(null)

  function startPolling(fetchFn, interval = 30000) {
    if (isPolling.value) return
    isPolling.value = true
    refreshQueue(fetchFn)
    pollingTimer.value = setInterval(() => {
      refreshQueue(fetchFn)
    }, interval)
  }

  function stopPolling() {
    if (pollingTimer.value) {
      clearInterval(pollingTimer.value)
      pollingTimer.value = null
    }
    isPolling.value = false
  }

  async function refreshQueue(fetchFn) {
    try {
      currentQueue.value = await fetchFn()
      lastRefreshTime.value = new Date()
    } catch (e) {
      console.error('队列刷新失败', e)
    }
  }

  return { currentQueue, isPolling, lastRefreshTime, startPolling, stopPolling, refreshQueue }
})
