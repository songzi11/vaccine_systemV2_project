import { defineStore } from 'pinia'
import { CALL_TIMEOUT_MIN } from '@/utils/constants.js'

export const useCallingStore = defineStore('calling', () => {
  const currentCalledItem = ref(null)
  const calledHistory = ref([])
  const waitingCount = ref(0)
  const callTimer = ref(null)
  const callRemainingSeconds = ref(0)

  function startCallTimer(onTimeout) {
    stopCallTimer()
    callRemainingSeconds.value = CALL_TIMEOUT_MIN * 60
    callTimer.value = setInterval(() => {
      callRemainingSeconds.value--
      if (callRemainingSeconds.value <= 0) {
        stopCallTimer()
        if (onTimeout) onTimeout()
      }
    }, 1000)
  }

  function stopCallTimer() {
    if (callTimer.value) {
      clearInterval(callTimer.value)
      callTimer.value = null
    }
    callRemainingSeconds.value = 0
  }

  function setCurrentCalled(item) {
    currentCalledItem.value = item
  }

  function clearCurrentCalled() {
    currentCalledItem.value = null
    stopCallTimer()
  }

  return { currentCalledItem, calledHistory, waitingCount, callRemainingSeconds, startCallTimer, stopCallTimer, setCurrentCalled, clearCurrentCalled }
})
