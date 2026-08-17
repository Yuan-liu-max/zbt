<template>
  <a-popconfirm
    :title="title"
    :ok-text="okText"
    :cancel-text="cancelText"
    @confirm="onConfirm"
  >
    <a-button type="link" size="small" danger class="danger-link action-link">
      <slot>删除</slot>
    </a-button>
  </a-popconfirm>
</template>

<script setup lang="ts">
interface Props {
  title?: string
  okText?: string
  cancelText?: string
}

const props = withDefaults(defineProps<Props>(), {
  title: '确定要删除吗？',
  okText: '确定',
  cancelText: '取消',
})

const emit = defineEmits<{
  (e: 'confirm'): void
}>()

const onConfirm = () => {
  emit('confirm')
}
</script>

<style scoped>
.danger-link {
  color: #ff4d4f;
}
.danger-link:hover {
  color: #ff7875;
}
.action-link {
  font-size: 13px;
}
</style>
