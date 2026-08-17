import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { OrderRecord } from '@/types'

const STORAGE_KEY = 'zbt_local_orders'

function loadLocalOrders(): OrderRecord[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : []
  } catch {
    return []
  }
}

function saveLocalOrders(orders: OrderRecord[]) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(orders))
}

/** 离线订单缓存 — 当后端不可达时本地暂存 */
export const useOrderStore = defineStore('order', () => {
  const localOrders = ref<OrderRecord[]>(loadLocalOrders())

  function addLocalOrder(order: OrderRecord) {
    localOrders.value.unshift(order)
    saveLocalOrders(localOrders.value)
  }

  function removeLocalOrder(index: number) {
    localOrders.value.splice(index, 1)
    saveLocalOrders(localOrders.value)
  }

  function clearLocalOrders() {
    localOrders.value = []
    saveLocalOrders([])
  }

  return {
    localOrders,
    addLocalOrder, removeLocalOrder, clearLocalOrders
  }
})
