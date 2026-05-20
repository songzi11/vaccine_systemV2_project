import { ref } from 'vue'
import { createAppointment, cancelAppointment } from '@/api/appointment.js'
import { useAppointmentStore } from '@/store/appointment.js'

export function useAppointment() {
  const appointmentStore = useAppointmentStore()
  const submitting = ref(false)

  async function create(data) {
    submitting.value = true
    try {
      return await createAppointment(data)
    } finally {
      submitting.value = false
    }
  }

  async function cancel(id) {
    submitting.value = true
    try {
      return await cancelAppointment(id)
    } finally {
      submitting.value = false
    }
  }

  return {
    appointments: appointmentStore.appointments,
    currentAppointment: appointmentStore.currentAppointment,
    submitting,
    create,
    cancel,
    fetchAppointments: appointmentStore.fetchAppointments,
    fetchDetail: appointmentStore.fetchDetail,
    fetchGuide: appointmentStore.fetchGuide,
    fetchQueue: appointmentStore.fetchQueue
  }
}
