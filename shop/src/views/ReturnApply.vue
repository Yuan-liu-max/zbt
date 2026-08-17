<template>
  <div class="return-page page-container--no-tabbar">
    <van-nav-bar title="申请退款" left-text="返回" left-arrow @click-left="$router.back()" />
    <van-form @submit="submitReturn">
      <van-cell-group inset title="退款信息">
        <van-field name="orderId" label="订单号" :model-value="orderId" readonly />
        <van-field name="reason" label="退款原因" v-model="reason" placeholder="请选择" is-link readonly @click="showReason = true" />
        <van-field name="amount" label="退款金额" :model-value="'¥' + amount" readonly />
        <van-field name="remark" label="补充说明" v-model="remark" placeholder="选填" type="textarea" rows="3" />
      </van-cell-group>
      <div style="margin: 24px">
        <van-button round block type="danger" native-type="submit" :loading="submitting">提交申请</van-button>
      </div>
    </van-form>

    <van-action-sheet v-model:show="showReason" title="退款原因" :actions="reasonList" @select="onReasonSelect" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { shopOrderApi } from '@/api/services'

const route = useRoute()
const router = useRouter()

const orderId = ref((route.query.orderId as string) || '')
const amount = ref((route.query.amount as string) || '0')
const reason = ref('')
const remark = ref('')
const submitting = ref(false)
const showReason = ref(false)
const reasonList = [
  { name: '不想要了' }, { name: '商品与描述不符' }, { name: '质量问题' },
  { name: '卖家发错货' }, { name: '其他原因' }
]

function onReasonSelect(item: { name: string }) {
  reason.value = item.name
  showReason.value = false
}

async function submitReturn() {
  if (!reason.value) { showToast('请选择退款原因'); return }
  submitting.value = true
  try {
    await shopOrderApi.applyReturn(
      Number(orderId.value),
      reason.value,
      amount.value ? Number(amount.value) : undefined
    )
    showToast('退款申请已提交')
    router.back()
  } catch { /* 错误已在拦截器 */ }
  finally { submitting.value = false }
}
</script>

<style scoped>
.return-page { min-height: 100vh; background: var(--bg-page); }
</style>
