import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getChildList, addChild, updateChild, deleteChild } from '@/api/child.js'

export const useChildStore = defineStore('child', () => {
  const children = ref([])
  const currentChild = ref(null)
  const loading = ref(false)

  async function fetchChildren() {
    loading.value = true
    try {
      children.value = await getChildList()
    } catch (e) {
      console.error('获取儿童列表失败', e)
    } finally {
      loading.value = false
    }
  }

  async function add(data) {
    const res = await addChild(data)
    await fetchChildren()
    return res
  }

  async function update(childId, data) {
    const res = await updateChild(childId, data)
    await fetchChildren()
    return res
  }

  async function remove(childId) {
    await deleteChild(childId)
    await fetchChildren()
  }

  return { children, currentChild, loading, fetchChildren, add, update, remove }
})
