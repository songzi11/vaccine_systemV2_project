import { ref } from 'vue'
import { useChildStore } from '@/store/child.js'

export function useChild() {
  const childStore = useChildStore()
  const submitting = ref(false)

  async function addChild(data) {
    submitting.value = true
    try {
      return await childStore.add(data)
    } finally {
      submitting.value = false
    }
  }

  async function updateChild(childId, data) {
    submitting.value = true
    try {
      return await childStore.update(childId, data)
    } finally {
      submitting.value = false
    }
  }

  async function deleteChild(childId) {
    submitting.value = true
    try {
      return await childStore.remove(childId)
    } finally {
      submitting.value = false
    }
  }

  return { children: childStore.children, currentChild: childStore.currentChild, submitting, fetchChildren: childStore.fetchChildren, addChild, updateChild, deleteChild }
}
