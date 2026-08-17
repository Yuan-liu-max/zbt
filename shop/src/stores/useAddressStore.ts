import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { AddressItem } from '@/types'
import { addressApi } from '@/api/address'

export const useAddressStore = defineStore('address', () => {
  const addresses = ref<AddressItem[]>([])
  const loading = ref(false)

  async function fetchAddresses() {
    loading.value = true
    try {
      const data = await addressApi.list()
      addresses.value = data || []
    } catch {
      addresses.value = []
    } finally {
      loading.value = false
    }
  }

  async function createAddress(data: Omit<AddressItem, 'id'>) {
    const addr = await addressApi.create(data)
    addresses.value.push(addr as unknown as AddressItem)
    return addr
  }

  async function updateAddress(id: string | number, data: Partial<AddressItem>) {
    await addressApi.update(id, data)
    const idx = addresses.value.findIndex(a => String(a.id) === String(id))
    if (idx >= 0) {
      addresses.value[idx] = { ...addresses.value[idx], ...data }
    }
  }

  async function deleteAddress(id: string | number) {
    await addressApi.delete(id)
    addresses.value = addresses.value.filter(a => String(a.id) !== String(id))
  }

  async function setDefault(id: string | number) {
    await addressApi.setDefault(id)
    addresses.value.forEach(a => {
      a.isDefault = String(a.id) === String(id)
    })
  }

  const defaultAddress = computed(() => addresses.value.find(a => a.isDefault) || addresses.value[0])

  return {
    addresses, loading, defaultAddress,
    fetchAddresses, createAddress, updateAddress, deleteAddress, setDefault
  }
})
