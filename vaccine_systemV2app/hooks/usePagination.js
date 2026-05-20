/**
 * 通用分页 Hook
 * 用于所有列表页面的分页加载逻辑
 *
 * 使用示例：
 * const { pageData, loadData, loadMore, refresh, hasMore } = usePagination(fetchFn)
 *
 * @param {Function} fetchFn - 数据获取函数，接收 { page, size } 参数，返回 { records, total }
 * @param {Object} options - 配置项
 * @param {number} options.pageSize - 每页条数（默认 20）
 * @param {Object} options.extraParams - 额外请求参数
 */
import { ref, reactive } from 'vue'

export function usePagination(fetchFn, options = {}) {
  const { pageSize = 20, extraParams = {} } = options

  const pageData = reactive({
    records: [],
    total: 0,
    page: 1,
    size: pageSize,
    pages: 0
  })

  const loading = ref(false)
  const hasMore = ref(true)

  /**
   * 加载数据
   * @param {boolean} append - 是否追加模式（上拉加载更多）
   * @param {Object} params - 额外参数（覆盖 extraParams）
   */
  async function loadData(append = false, params = {}) {
    if (loading.value) return

    if (!append) {
      pageData.page = 1
    }

    loading.value = true
    try {
      const result = await fetchFn({
        page: pageData.page,
        size: pageData.size,
        ...extraParams,
        ...params
      })

      const records = result.records || result || []
      const total = result.total || 0

      if (append) {
        pageData.records = [...pageData.records, ...records]
      } else {
        pageData.records = records
      }

      pageData.total = total
      pageData.pages = Math.ceil(total / pageData.size) || 1
      hasMore.value = pageData.page < pageData.pages
    } catch (err) {
      console.error('分页加载失败:', err)
      if (!append) {
        pageData.records = []
        pageData.total = 0
      }
    } finally {
      loading.value = false
    }
  }

  /** 上拉加载更多 */
  function loadMore(params = {}) {
    if (!hasMore.value || loading.value) return
    pageData.page++
    loadData(true, params)
  }

  /** 下拉刷新 */
  function refresh(params = {}) {
    loadData(false, params)
  }

  /** 重置 */
  function reset() {
    pageData.records = []
    pageData.total = 0
    pageData.page = 1
    pageData.pages = 0
    hasMore.value = true
    loading.value = false
  }

  return {
    pageData,
    loading,
    hasMore,
    loadData,
    loadMore,
    refresh,
    reset
  }
}
