import { ref, reactive } from 'vue'
import type { PageResult, PageParams } from '@/types/common'

export interface PaginationConfig {
  current: number
  pageSize: number
  total: number
  showSizeChanger: boolean
  showQuickJumper: boolean
  showTotal: (total: number) => string
}

export interface UseCrudTableOptions<T, S extends Record<string, any>> {
  searchForm: S
  loadFn: (params: PageParams & S) => Promise<PageResult<T>>
  deleteFn?: (id: string) => Promise<unknown>
  onDeleteSuccess?: () => void
}

/**
 * 通用 CRUD 表格逻辑封装
 */
export function useCrudTable<T extends { id?: string }, S extends Record<string, any>>(
  options: UseCrudTableOptions<T, S>
) {
  const { searchForm, loadFn, deleteFn, onDeleteSuccess } = options

  // 保存初始值快照，reset 时从此恢复（而非从已被修改的 searchForm 拷贝）
  const initialValues = { ...searchForm } as S

  const tableData = ref<T[]>([])
  const loading = ref(false)
  const pagination = reactive<PaginationConfig>({
    current: 1,
    pageSize: 10,
    total: 0,
    showSizeChanger: true,
    showQuickJumper: true,
    showTotal: (total: number) => `共 ${total} 条`,
  })

  const buildParams = (): PageParams & S => {
    const params: any = { ...searchForm }
    // 移除空值
    Object.keys(params).forEach((key) => {
      if (params[key] === '' || params[key] === undefined || params[key] === null) {
        delete params[key]
      }
    })
    params.page = pagination.current
    params.pageSize = pagination.pageSize
    return params
  }

  const loadData = async () => {
    loading.value = true
    try {
      const res = await loadFn(buildParams())
      tableData.value = res.list || []
      pagination.total = res.total || 0
    } catch (error) {
      console.error('加载数据失败', error)
    } finally {
      loading.value = false
    }
  }

  const handleSearch = () => {
    pagination.current = 1
    loadData()
  }

  const handleReset = (resetForm?: () => void) => {
    Object.keys(initialValues).forEach((key) => {
      ;(searchForm as any)[key] = (initialValues as any)[key]
    })
    resetForm?.()
    handleSearch()
  }

  const handleTableChange = (pag: any) => {
    pagination.current = pag.current
    pagination.pageSize = pag.pageSize
    loadData()
  }

  const handleDelete = async (id: string) => {
    if (!deleteFn) return
    try {
      await deleteFn(id)
      onDeleteSuccess?.()
      loadData()
    } catch (error) {
      console.error('删除失败', error)
    }
  }

  return {
    tableData,
    loading,
    pagination,
    loadData,
    handleSearch,
    handleReset,
    handleTableChange,
    handleDelete,
  }
}
