<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>组织架构</h2>
    </div>

    <!-- 操作栏 -->
    <div class="content-card" style="margin-bottom: 16px">
      <div class="toolbar">
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <template #icon><PlusOutlined /></template>
            新增部门
          </a-button>
          <a-button @click="handleExport">
            <template #icon><DownloadOutlined /></template>
            导出组织架构
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 主体区域：左右布局 -->
    <div class="org-main">
      <!-- 左侧：组织树 -->
      <div class="content-card org-left">
        <div class="org-tree-header">
          <h3>部门列表</h3>
          <a-input-search
            v-model:value="searchValue"
            placeholder="搜索部门"
            allow-clear
            style="width: 200px"
            @search="onSearch"
          />
        </div>
        <a-spin :spinning="loading">
          <a-tree
            v-if="filteredTreeData.length"
            :tree-data="filteredTreeData"
            :field-names="{ title: 'name', key: 'id', children: 'children' }"
            default-expand-all
            :selected-keys="selectedKeys"
            @select="onTreeSelect"
          >
            <template #title="{ name, memberCount }">
              <span>{{ name }}</span>
              <a-tag color="blue" style="margin-left: 8px; font-size: 12px">
                {{ memberCount }}人
              </a-tag>
            </template>
          </a-tree>
          <a-empty v-else description="暂无组织数据" />
        </a-spin>
      </div>

      <!-- 右侧：组织架构可视化 -->
      <div class="content-card org-right">
        <div class="org-chart-header">
          <h3>架构图</h3>
        </div>
        <div class="org-chart">
          <div v-if="orgTreeData.length" class="org-chart-content">
            <!-- 递归渲染组织架构图 -->
            <OrgChartNode :nodes="orgTreeData" />
          </div>
          <a-empty v-else description="暂无组织数据" />
        </div>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="editingNode ? '编辑部门' : '新增部门'"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
      :confirm-loading="modalLoading"
    >
      <a-form
        :model="formData"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
        ref="formRef"
        :rules="formRules"
      >
        <a-form-item label="部门名称" name="name">
          <a-input v-model:value="formData.name" placeholder="请输入部门名称" />
        </a-form-item>
        <a-form-item label="上级部门" name="parentId">
          <a-tree-select
            v-model:value="formData.parentId"
            :tree-data="parentTreeData"
            :field-names="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="请选择上级部门"
            allow-clear
            tree-default-expand-all
          />
        </a-form-item>
        <a-form-item label="成员数" name="memberCount">
          <a-input-number
            v-model:value="formData.memberCount"
            :min="0"
            style="width: 100%"
            placeholder="请输入成员数量"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, h, defineComponent } from 'vue'
import { message, type FormInstance } from 'ant-design-vue'
import { PlusOutlined, SearchOutlined, DownloadOutlined } from '@ant-design/icons-vue'
import type { OrgNode } from '@/types/system'
import { orgApi } from '@/api/system'

// ==================== 组织架构图表节点组件 ====================
const OrgChartNode = defineComponent({
  name: 'OrgChartNode',
  props: {
    nodes: {
      type: Array as () => OrgNode[],
      required: true,
    },
  },
  setup(props) {
    return () => {
      if (!props.nodes || props.nodes.length === 0) return null

      return h('div', { class: 'org-chart-level' }, [
        h('div', { class: 'org-chart-nodes' }, [
          ...props.nodes.map((node) =>
            h('div', { class: 'org-chart-node-wrapper', key: node.id }, [
              h('div', { class: 'org-chart-node' }, [
                h('div', { class: 'org-node-name' }, node.name),
                h('div', { class: 'org-node-count' }, `${node.memberCount}人`),
              ]),
              // 递归渲染子节点
              node.children && node.children.length > 0
                ? h(
                    'div',
                    { class: 'org-chart-connector' },
                    [
                      h('div', { class: 'org-chart-line-down' }),
                      h(OrgChartNode, { nodes: node.children }),
                    ]
                  )
                : null,
            ])
          ),
        ]),
      ])
    }
  },
})

// ==================== 数据 ====================
const loading = ref(false)
const orgTreeData = ref<OrgNode[]>([])
const searchValue = ref('')
const selectedKeys = ref<string[]>([])

// 弹窗相关
const modalVisible = ref(false)
const modalLoading = ref(false)
const editingNode = ref<OrgNode | null>(null)
const formRef = ref<FormInstance>()

const formData = reactive({
  name: '',
  parentId: null as string | null,
  memberCount: 0,
})

const formRules = {
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  memberCount: [{ required: true, message: '请输入成员数量', trigger: 'blur' }],
}

// ==================== 计算属性 ====================

// 过滤后的树数据（用于搜索）
const filteredTreeData = computed(() => {
  if (!searchValue.value) return orgTreeData.value
  return filterTree(orgTreeData.value, searchValue.value)
})

// 用于上级部门选择的树数据（排除当前编辑节点及其子节点）
const parentTreeData = computed(() => {
  if (!editingNode.value) return orgTreeData.value
  return removeNode(orgTreeData.value, editingNode.value.id)
})

// ==================== 方法 ====================

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    orgTreeData.value = await orgApi.getTree()
  } finally {
    loading.value = false
  }
}

// 搜索过滤树
function filterTree(nodes: OrgNode[], keyword: string): OrgNode[] {
  return nodes.reduce<OrgNode[]>((acc, node) => {
    const childMatches = node.children ? filterTree(node.children, keyword) : []
    if (node.name.includes(keyword) || childMatches.length > 0) {
      acc.push({
        ...node,
        children: childMatches.length > 0 ? childMatches : node.children,
      })
    }
    return acc
  }, [])
}

// 从树中移除指定节点
function removeNode(nodes: OrgNode[], targetId: string): OrgNode[] {
  return nodes
    .filter((node) => node.id !== targetId)
    .map((node) => ({
      ...node,
      children: node.children ? removeNode(node.children, targetId) : undefined,
    }))
}

// 在树中查找节点
function findNode(nodes: OrgNode[], id: string): OrgNode | null {
  for (const node of nodes) {
    if (node.id === id) return node
    if (node.children) {
      const found = findNode(node.children, id)
      if (found) return found
    }
  }
  return null
}

// 在树中更新节点
function updateNodeInTree(nodes: OrgNode[], id: string, data: Partial<OrgNode>): OrgNode[] {
  return nodes.map((node) => {
    if (node.id === id) {
      return { ...node, ...data }
    }
    if (node.children) {
      return { ...node, children: updateNodeInTree(node.children, id, data) }
    }
    return node
  })
}

// 在树中添加节点
function addNodeToTree(nodes: OrgNode[], parentId: string | null, newNode: OrgNode): OrgNode[] {
  if (!parentId) {
    return [...nodes, newNode]
  }
  return nodes.map((node) => {
    if (node.id === parentId) {
      return { ...node, children: [...(node.children || []), newNode] }
    }
    if (node.children) {
      return { ...node, children: addNodeToTree(node.children, parentId, newNode) }
    }
    return node
  })
}

// 搜索
const onSearch = (value: string) => {
  searchValue.value = value
}

// 树节点选中
const onTreeSelect = (keys: string[]) => {
  selectedKeys.value = keys
}

// 新增
const handleAdd = () => {
  editingNode.value = null
  formData.name = ''
  formData.parentId = selectedKeys.value.length ? selectedKeys.value[0] : null
  formData.memberCount = 0
  modalVisible.value = true
}

// 导出
const handleExport = () => {
  const flatten = (nodes: OrgNode[], prefix = ''): string[] => {
    const lines: string[] = []
    nodes.forEach((node, index) => {
      const connector = index === nodes.length - 1 ? '└── ' : '├── '
      const childPrefix = index === nodes.length - 1 ? '    ' : '│   '
      lines.push(`${prefix}${connector}${node.name} (${node.memberCount}人)`)
      if (node.children) {
        lines.push(...flatten(node.children, prefix + childPrefix))
      }
    })
    return lines
  }

  const output = orgTreeData.value
    .map((node) => {
      const lines = [node.name + ` (${node.memberCount}人)`]
      if (node.children) lines.push(...flatten(node.children))
      return lines.join('\n')
    })
    .join('\n\n')

  const blob = new Blob([output], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `组织架构_${new Date().toLocaleDateString('zh-CN')}.txt`
  a.click()
  URL.revokeObjectURL(url)
  message.success('组织架构导出成功')
}

// 弹窗确认
const handleModalOk = async () => {
  try {
    await formRef.value?.validateFields()
  } catch {
    return
  }

  modalLoading.value = true
  try {
    // 模拟保存延迟
    await new Promise((resolve) => setTimeout(resolve, 500))

    if (editingNode.value) {
      // 编辑
      orgTreeData.value = updateNodeInTree(orgTreeData.value, editingNode.value.id, {
        name: formData.name,
        parentId: formData.parentId,
        memberCount: formData.memberCount,
      })
      message.success('部门信息更新成功')
    } else {
      // 新增
      const newId = Date.now().toString()
      const parentLevel = formData.parentId
        ? (findNode(orgTreeData.value, formData.parentId)?.level || 0) + 1
        : 1
      const newNode: OrgNode = {
        id: newId,
        name: formData.name,
        parentId: formData.parentId,
        level: parentLevel,
        memberCount: formData.memberCount,
        children: [],
      }
      orgTreeData.value = addNodeToTree(orgTreeData.value, formData.parentId, newNode)
      message.success('部门新增成功')
    }

    modalVisible.value = false
  } finally {
    modalLoading.value = false
  }
}

// 弹窗取消
const handleModalCancel = () => {
  modalVisible.value = false
  formRef.value?.resetFields()
}

// ==================== 生命周期 ====================
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-container {
  padding: 24px;
}

.page-header {
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.content-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 左右布局 */
.org-main {
  display: flex;
  gap: 16px;
}

.org-left {
  width: 360px;
  flex-shrink: 0;
}

.org-right {
  flex: 1;
  min-width: 0;
}

.org-tree-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.org-tree-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

/* 架构图 */
.org-chart-header {
  margin-bottom: 16px;
}

.org-chart-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.org-chart {
  overflow: auto;
  min-height: 400px;
}

.org-chart-content {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.org-chart-level {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.org-chart-nodes {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.org-chart-node-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.org-chart-node {
  background: linear-gradient(135deg, #1890ff, #40a9ff);
  color: #fff;
  padding: 12px 20px;
  border-radius: 8px;
  text-align: center;
  min-width: 100px;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
  transition: transform 0.2s, box-shadow 0.2s;
}

.org-chart-node:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.4);
}

.org-node-name {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
}

.org-node-count {
  font-size: 12px;
  opacity: 0.85;
}

.org-chart-connector {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 8px;
}

.org-chart-line-down {
  width: 2px;
  height: 16px;
  background: #d9d9d9;
}
</style>
