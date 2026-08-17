import { ref } from 'vue'

/**
 * 通用详情弹窗逻辑封装
 */
export function useDetailModal<T extends Record<string, any>>() {
  const detailVisible = ref(false)
  const detailRecord = ref<T | null>(null)

  const openDetail = (record: T) => {
    detailRecord.value = record
    detailVisible.value = true
  }

  const closeDetail = () => {
    detailVisible.value = false
    detailRecord.value = null
  }

  return {
    detailVisible,
    detailRecord,
    openDetail,
    closeDetail,
  }
}
