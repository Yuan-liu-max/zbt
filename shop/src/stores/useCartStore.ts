import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { ProductItem } from '@/types'
import { serverCartApi } from '@/api/services'
import { useUserStore } from './useUserStore'

export interface CartEntry {
  cartId?: string | number  // 服务端购物车记录ID
  product: ProductItem
  quantity: number
  checked: boolean
}

const STORAGE_KEY_PREFIX = 'zbt_cart'

/** 按用户隔离购物车缓存 key，避免跨用户数据污染 */
function getStorageKey(): string {
  try {
    const raw = localStorage.getItem('zbt_user')
    if (raw) {
      const user = JSON.parse(raw)
      if (user.userId) return `${STORAGE_KEY_PREFIX}_${user.userId}`
    }
  } catch { /* ignore */ }
  return `${STORAGE_KEY_PREFIX}_guest`
}

function loadFromStorage(): CartEntry[] {
  try {
    const raw = localStorage.getItem(getStorageKey())
    return raw ? JSON.parse(raw) : []
  } catch {
    return []
  }
}

function saveToStorage(cart: CartEntry[]) {
  localStorage.setItem(getStorageKey(), JSON.stringify(cart))
}

export const useCartStore = defineStore('cart', () => {
  const items = ref<CartEntry[]>(loadFromStorage())

  const totalCount = computed(() => items.value.reduce((sum, e) => sum + e.quantity, 0))
  const checkedItems = computed(() => items.value.filter(e => e.checked))
  const checkedTotal = computed(() =>
    checkedItems.value.reduce((sum, e) => sum + (e.product.price || 0) * e.quantity, 0)
  )

  function persist() {
    saveToStorage(items.value)
  }

  /** 从服务端加载购物车并合并本地数据 */
  async function loadFromServer() {
    const userStore = useUserStore()
    if (!userStore.isLoggedIn) return
    try {
      const serverItems = await serverCartApi.list()
      // 将服务端数据转换为 CartEntry 格式
      const serverEntries: CartEntry[] = (serverItems || []).map((item: any) => ({
        cartId: item.id,
        product: {
          id: item.productId,
          name: item.productName || item.name,
          code: item.productCode || item.code,
          price: item.price,
          imageUrl: item.imageUrl,
          stock: item.stock,
          storeName: item.storeName,
          categoryName: item.categoryName,
          material: item.material,
          weight: item.weight,
          description: item.description,
        } as ProductItem,
        quantity: item.quantity || 1,
        checked: item.checked === 1 || item.checked === true
      }))
      // 服务端数据优先，仅保留当前登录用户在本地新增的未同步商品
      const localOnly = items.value.filter(e => !e.cartId)
      items.value = [...serverEntries, ...localOnly]
      persist()
    } catch {
      // 服务端不可用时使用本地数据
    }
  }

  /** 登录后同步本地购物车到服务端 */
  async function syncToServer() {
    const userStore = useUserStore()
    if (!userStore.isLoggedIn) return
    const localOnly = items.value.filter(e => !e.cartId)
    if (localOnly.length === 0) return
    try {
      const syncData = localOnly.map(e => ({
        productId: e.product.id,
        quantity: e.quantity
      }))
      await serverCartApi.sync(syncData)
      // 重新加载服务端数据
      await loadFromServer()
    } catch {
      // 同步失败保留本地数据
    }
  }

  /** 添加商品到购物车 */
  async function add(product: ProductItem, quantity = 1) {
    const userStore = useUserStore()
    if (userStore.isLoggedIn) {
      try {
        await serverCartApi.add(product.id, quantity)
        await loadFromServer()
        return
      } catch { /* fallback to local */ }
    }

    const exist = items.value.find(e => String(e.product.id) === String(product.id))
    if (exist) {
      exist.quantity += quantity
    } else {
      items.value.push({ product, quantity, checked: true })
    }
    persist()
  }

  /** 更新数量 */
  async function updateQuantity(productId: string | number, quantity: number) {
    const userStore = useUserStore()
    const entry = items.value.find(e => String(e.product.id) === String(productId))
    if (userStore.isLoggedIn && entry?.cartId) {
      try {
        await serverCartApi.updateQuantity(entry.cartId, quantity)
        await loadFromServer()
        return
      } catch { /* fallback */ }
    }
    if (entry) {
      entry.quantity = Math.max(1, quantity)
      persist()
    }
  }

  /** 切换选中 */
  async function toggleCheck(productId: string | number) {
    const userStore = useUserStore()
    const entry = items.value.find(e => String(e.product.id) === String(productId))
    if (!entry) return
    entry.checked = !entry.checked
    if (userStore.isLoggedIn && entry.cartId) {
      try {
        await serverCartApi.toggleCheck(entry.cartId, entry.checked ? 1 : 0)
      } catch { /* ignore */ }
    }
    persist()
  }

  /** 全选/取消全选 */
  async function toggleAll(checked: boolean) {
    const userStore = useUserStore()
    items.value.forEach(e => e.checked = checked)
    if (userStore.isLoggedIn) {
      try {
        await serverCartApi.checkAll(checked ? 1 : 0)
      } catch { /* ignore */ }
    }
    persist()
  }

  /** 是否全选 */
  const isAllChecked = computed(() =>
    items.value.length > 0 && items.value.every(e => e.checked)
  )

  /** 删除商品 */
  async function remove(productId: string | number) {
    const userStore = useUserStore()
    const entry = items.value.find(e => String(e.product.id) === String(productId))
    if (userStore.isLoggedIn && entry?.cartId) {
      try {
        await serverCartApi.remove(entry.cartId)
      } catch { /* ignore */ }
    }
    items.value = items.value.filter(e => String(e.product.id) !== String(productId))
    persist()
  }

  /** 删除已选商品（结算后清空） */
  async function removeChecked() {
    const userStore = useUserStore()
    if (userStore.isLoggedIn) {
      try {
        await serverCartApi.removeChecked()
      } catch { /* ignore */ }
    }
    items.value = items.value.filter(e => !e.checked)
    persist()
  }

  /** 清空购物车 */
  function clear() {
    items.value = []
    persist()
  }

  return {
    items, totalCount, checkedItems, checkedTotal, isAllChecked,
    add, updateQuantity, toggleCheck, toggleAll, remove, removeChecked, clear,
    loadFromServer, syncToServer
  }
})
