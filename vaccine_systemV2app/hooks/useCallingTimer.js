import { ref, onUnmounted } from 'vue'
import { CALL_TIMEOUT_MIN } from '@/utils/constants.js'

export function useCallingTimer(onTimeout) {
  const remainingSeconds = ref(CALL_TIMEOUT_MIN * 60)
  const isRunning = ref(false)
  let timer = null

  function start() {
    stop()
    remainingSeconds.value = CALL_TIMEOUT_MIN * 60
    isRunning.value = true
    timer = setInterval(() => {
      remainingSeconds.value--
      if (remainingSeconds.value <= 0) {
        stop()
        if (onTimeout) onTimeout()
      }
    }, 1000)
  }

  function stop() {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
    isRunning.value = false
  }

  onUnmounted(stop)

  return { remainingSeconds, isRunning, start, stop }
}
