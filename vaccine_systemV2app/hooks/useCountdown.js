import { ref, onUnmounted } from 'vue'

export function useCountdown(startTime) {
  const elapsed = ref(0)
  const canFinish = ref(false)
  const remaining = ref(0)
  let timer = null

  function start(totalSeconds) {
    stop()
    elapsed.value = 0
    canFinish.value = false
    const start = startTime ? new Date(startTime).getTime() : Date.now()
    timer = setInterval(() => {
      elapsed.value = Math.floor((Date.now() - start) / 1000)
      remaining.value = Math.max(totalSeconds - elapsed.value, 0)
      canFinish.value = elapsed.value >= totalSeconds
    }, 1000)
  }

  function stop() {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
  }

  onUnmounted(stop)

  return { elapsed, canFinish, remaining, start, stop }
}
