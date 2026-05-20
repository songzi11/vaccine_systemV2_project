import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getAppointmentList, getAppointmentDetail, getAppointmentGuide, getAppointmentQueue } from '@/api/appointment.js'

export const useAppointmentStore = defineStore('appointment', () => {
  const appointments = ref([])
  const currentAppointment = ref(null)
  const guideData = ref(null)
  const queueData = ref(null)
  const filters = ref({ status: '', page: 1, size: 20 })
  const total = ref(0)
  const loading = ref(false)

  async function fetchAppointments(reset = true) {
    if (reset) filters.value.page = 1
    loading.value = true
    try {
      const data = await getAppointmentList(filters.value)
      appointments.value = data.records || data || []
      total.value = data.total || 0
    } catch (e) {
      console.error('获取预约列表失败', e)
    } finally {
      loading.value = false
    }
  }

  async function fetchDetail(id) {
    currentAppointment.value = await getAppointmentDetail(id)
    return currentAppointment.value
  }

  async function fetchGuide(id) {
    guideData.value = await getAppointmentGuide(id)
    return guideData.value
  }

  async function fetchQueue(id) {
    queueData.value = await getAppointmentQueue(id)
    return queueData.value
  }

  return { appointments, currentAppointment, guideData, queueData, filters, total, loading, fetchAppointments, fetchDetail, fetchGuide, fetchQueue }
})
