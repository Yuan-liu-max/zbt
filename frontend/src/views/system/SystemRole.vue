<template>
  <div class="page-container">
    <div class="page-header">
      <h2>角色权限</h2>
    </div>

    <div class="role-layout">
      <!-- 左侧：角色列表 -->
      <div class="role-sidebar">
        <div class="sidebar-header">
          <span class="sidebar-title">角色列表</span>
          <a-button type="primary" size="small" @click="handleAddRole">
            <PlusOutlined /> 新增
          </a-button>
        </div>
        <div class="role-list">
          <div
            v-for="role in roleList"
            :key="role.id"
            class="role-item"
            :class="{ active: selectedRoleId === role.id }"
            @click="handleSelectRole(role)"
          >
            <div class="role-name">{{ role.name }}</div>
            <div class="role-desc">{{ role.description || '暂无描述' }}</div>
          </div>
        </div>
      </div>

      <!-- 右侧：权限配置 -->
      <div class="role-main">
        <template v-if="selectedRoleId">
          <div class="role-info-card">
            <div class="role-info-header">
              <div class="role-info-fields">
                <div class="info-field">
                  <span class="field-label">角色名称：</span>
                  <a-input v-model:value="editName" placeholder="请输入角色名称" style="width: 200px" />
                </div>
                <div class="info-field">
                  <span class="field-label">角色描述：</span>
                  <a-input v-model:value="editDesc" placeholder="请输入角色描述" style="width: 300px" />
                </div>
              </div>
              <a-space>
                <a-button @click="handleReset">重置</a-button>
                <a-button type="primary" @click="handleSave">保存</a-button>
              </a-space>
            </div>
          </div>

          <div class="permission-card">
            <a-tabs v-model:activeKey="activeTab">
              <a-tab-pane key="menu" tab="菜单权限">
                <div class="tree-toolbar">
                  <a-checkbox v-model:checked="menuCheckAll" @change="handleMenuCheckAll">全选</a-checkbox>
                </div>
                <a-tree
                  v-model:checkedKeys="menuCheckedKeys"
                  :tree-data="menuTreeData"
                  checkable
                  :selectable="false"
                  default-expand-all
                />
              </a-tab-pane>
              <a-tab-pane key="data" tab="数据权限">
                <div class="tree-toolbar">
                  <a-checkbox v-model:checked="dataCheckAll" @change="handleDataCheckAll">全选</a-checkbox>
                </div>
                <a-tree
                  v-model:checkedKeys="dataCheckedKeys"
                  :tree-data="dataTreeData"
                  checkable
                  :selectable="false"
                  default-expand-all
                />
              </a-tab-pane>
              <a-tab-pane key="action" tab="操作权限">
                <div class="tree-toolbar">
                  <a-checkbox v-model:checked="actionCheckAll" @change="handleActionCheckAll">全选</a-checkbox>
                </div>
                <a-tree
                  v-model:checkedKeys="actionCheckedKeys"
                  :tree-data="actionTreeData"
                  checkable
                  :selectable="false"
                  default-expand-all
                />
              </a-tab-pane>
            </a-tabs>
          </div>
        </template>
        <div v-else class="empty-state">
          <a-empty description="请先选择一个角色" />
        </div>
      </div>
    </div>

    <!-- 新增角色弹窗 -->
    <a-modal v-model:open="addModalVisible" title="新增角色" @ok="handleAddRoleOk" :confirm-loading="saveLoading" width="400px">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="角色名称" required>
          <a-input v-model:value="addRoleName" placeholder="请输入角色名称" />
        </a-form-item>
        <a-form-item label="角色描述">
          <a-input v-model:value="addRoleDesc" placeholder="请输入角色描述" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import type { RoleItem } from '@/types/system'
import { roleApi } from '@/api/system'

// 角色列表
const roleList = ref<RoleItem[]>([])
const selectedRoleId = ref<string | null>(null)

// 编辑表单
const editName = ref('')
const editDesc = ref('')
const saveLoading = ref(false)

// 权限标签页
const activeTab = ref('menu')

// 权限树数据（简化版，不使用 enabled 属性）
interface SimpleTreeNode {
  title: string
  key: string
  children?: SimpleTreeNode[]
}

const menuTreeData = ref<SimpleTreeNode[]>([
  { title: '商品管理', key: 'product', children: [
    { title: '商品列表', key: 'product-list' },
    { title: '商品分类', key: 'product-category' },
    { title: '商品品牌', key: 'product-brand' },
    { title: '商品规格', key: 'product-spec' },
  ]},
  { title: '订单管理', key: 'order', children: [
    { title: '订单列表', key: 'order-list' },
    { title: '订单详情', key: 'order-detail' },
  ]},
  { title: '库存管理', key: 'stock', children: [
    { title: '库存列表', key: 'stock-list' },
    { title: '库存预警', key: 'stock-warning' },
  ]},
  { title: '营销管理', key: 'marketing', children: [
    { title: '营销活动', key: 'marketing-activity' },
    { title: '促销管理', key: 'marketing-promotion' },
  ]},
  { title: '人力资源', key: 'hr', children: [
    { title: '员工管理', key: 'hr-employee' },
    { title: '绩效考核', key: 'hr-performance' },
  ]},
])

const dataTreeData = ref<SimpleTreeNode[]>([
  { title: '全部数据', key: 'data-all', children: [
    { title: '本部门数据', key: 'data-dept' },
    { title: '本部门及下级', key: 'data-dept-child' },
    { title: '仅本人数据', key: 'data-self' },
  ]},
  { title: '商品数据', key: 'data-product', children: [
    { title: '查看商品价格', key: 'data-product-price' },
    { title: '查看成本价', key: 'data-product-cost' },
  ]},
  { title: '订单数据', key: 'data-order', children: [
    { title: '查看订单金额', key: 'data-order-amount' },
    { title: '导出订单数据', key: 'data-order-export' },
  ]},
])

const actionTreeData = ref<SimpleTreeNode[]>([
  { title: '商品操作', key: 'action-product', children: [
    { title: '新增商品', key: 'action-product-add' },
    { title: '编辑商品', key: 'action-product-edit' },
    { title: '删除商品', key: 'action-product-delete' },
    { title: '上架/下架', key: 'action-product-status' },
  ]},
  { title: '订单操作', key: 'action-order', children: [
    { title: '确认订单', key: 'action-order-confirm' },
    { title: '取消订单', key: 'action-order-cancel' },
    { title: '退款处理', key: 'action-order-refund' },
  ]},
  { title: '系统操作', key: 'action-system', children: [
    { title: '用户管理', key: 'action-system-user' },
    { title: '角色管理', key: 'action-system-role' },
    { title: '系统配置', key: 'action-system-config' },
  ]},
])

// 选中状态
const menuCheckedKeys = ref<string[]>([])
const dataCheckedKeys = ref<string[]>([])
const actionCheckedKeys = ref<string[]>([])
const menuCheckAll = ref(false)
const dataCheckAll = ref(false)
const actionCheckAll = ref(false)

// 获取所有 key
const getAllKeys = (tree: SimpleTreeNode[]): string[] => {
  const keys: string[] = []
  const traverse = (nodes: SimpleTreeNode[]) => {
    nodes.forEach(node => {
      keys.push(node.key)
      if (node.children) traverse(node.children)
    })
  }
  traverse(tree)
  return keys
}

// 全选处理
const handleMenuCheckAll = (e: any) => {
  menuCheckedKeys.value = e.target.checked ? getAllKeys(menuTreeData.value) : []
}
const handleDataCheckAll = (e: any) => {
  dataCheckedKeys.value = e.target.checked ? getAllKeys(dataTreeData.value) : []
}
const handleActionCheckAll = (e: any) => {
  actionCheckedKeys.value = e.target.checked ? getAllKeys(actionTreeData.value) : []
}

// 新增角色
const addModalVisible = ref(false)
const addRoleName = ref('')
const addRoleDesc = ref('')
const handleAddRole = () => { addRoleName.value = ''; addRoleDesc.value = ''; addModalVisible.value = true }
const handleAddRoleOk = () => {
  if (!addRoleName.value.trim()) { message.warning('请输入角色名称'); return }
  roleList.value.push({ id: String(Date.now()), name: addRoleName.value, description: addRoleDesc.value, permissions: [] })
  message.success('新增成功')
  addModalVisible.value = false
}

// 选择角色
const handleSelectRole = (role: RoleItem) => {
  selectedRoleId.value = role.id
  editName.value = role.name
  editDesc.value = role.description || ''
  // 根据角色设置权限
  const isAdmin = role.name === '超级管理员'
  if (isAdmin) {
    menuCheckedKeys.value = getAllKeys(menuTreeData.value)
    dataCheckedKeys.value = getAllKeys(dataTreeData.value)
    actionCheckedKeys.value = getAllKeys(actionTreeData.value)
  } else {
    menuCheckedKeys.value = ['product', 'product-list', 'order', 'order-list']
    dataCheckedKeys.value = ['data-all', 'data-dept']
    actionCheckedKeys.value = ['action-product', 'action-product-add']
  }
  menuCheckAll.value = menuCheckedKeys.value.length === getAllKeys(menuTreeData.value).length
  dataCheckAll.value = dataCheckedKeys.value.length === getAllKeys(dataTreeData.value).length
  actionCheckAll.value = actionCheckedKeys.value.length === getAllKeys(actionTreeData.value).length
}

// 重置
const handleReset = () => {
  if (selectedRoleId.value) {
    const role = roleList.value.find(r => r.id === selectedRoleId.value)
    if (role) {
      editName.value = role.name
      editDesc.value = role.description || ''
      handleSelectRole(role)
      message.info('已重置')
    }
  }
}

// 保存
const handleSave = async () => {
  if (!selectedRoleId.value) return
  if (!editName.value.trim()) { message.warning('请输入角色名称'); return }
  saveLoading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 500))
    const index = roleList.value.findIndex(r => r.id === selectedRoleId.value)
    if (index !== -1) {
      roleList.value[index].name = editName.value
      roleList.value[index].description = editDesc.value
    }
    message.success('保存成功')
  } catch { message.error('保存失败') }
  finally { saveLoading.value = false }
}

onMounted(async () => {
  try {
    roleList.value = await roleApi.getList()
  } catch { message.error('加载角色列表失败') }
})
</script>

<style scoped>
.page-container { padding: 24px; min-height: calc(100vh - 64px); }
.page-header { margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 20px; font-weight: 600; }
.role-layout { display: flex; gap: 16px; min-height: calc(100vh - 160px); }
.role-sidebar { width: 280px; background: #fff; border-radius: 12px; display: flex; flex-direction: column; overflow: hidden; }
.sidebar-header { display: flex; justify-content: space-between; align-items: center; padding: 16px; border-bottom: 1px solid #f0f0f0; }
.sidebar-title { font-weight: 600; font-size: 15px; }
.role-list { flex: 1; overflow-y: auto; padding: 8px; }
.role-item { padding: 12px; border-radius: 8px; cursor: pointer; margin-bottom: 4px; transition: all 0.2s; }
.role-item:hover { background: #f5f5f5; }
.role-item.active { background: #fff7e6; border-left: 3px solid #c8a44d; }
.role-name { font-size: 14px; font-weight: 500; color: #333; margin-bottom: 4px; }
.role-desc { font-size: 12px; color: #999; }
.role-main { flex: 1; background: #fff; border-radius: 12px; padding: 24px; overflow-y: auto; }
.role-info-card { margin-bottom: 20px; padding-bottom: 16px; border-bottom: 1px solid #f0f0f0; }
.role-info-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; }
.role-info-fields { display: flex; gap: 24px; flex-wrap: wrap; }
.info-field { display: flex; align-items: center; gap: 8px; }
.field-label { font-size: 13px; color: #666; white-space: nowrap; }
.permission-card { margin-top: 16px; }
.tree-toolbar { margin-bottom: 12px; padding-bottom: 12px; border-bottom: 1px solid #f0f0f0; }
.empty-state { display: flex; align-items: center; justify-content: center; min-height: 300px; }
@media (max-width: 992px) { .role-layout { flex-direction: column; } .role-sidebar { width: 100%; } }
@media (max-width: 768px) { .page-container { padding: 16px; } .role-main { padding: 16px; } }
</style>
