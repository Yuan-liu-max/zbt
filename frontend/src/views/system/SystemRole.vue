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
            <div class="role-name">{{ role.roleName }}</div>
            <div class="role-desc">{{ role.remark || '暂无描述' }}</div>
            <a-popconfirm v-if="!isBuiltinRole(role)" title="确定删除该角色吗？" @confirm="handleDeleteRole(role)">
              <DeleteOutlined class="role-delete" @click.stop />
            </a-popconfirm>
            <a-tooltip v-else title="内置角色不可删除">
              <DeleteOutlined class="role-delete role-delete-disabled" @click.stop />
            </a-tooltip>
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
                  <span class="field-label">数据权限：</span>
                  <a-select v-model:value="editDataScope" style="width: 180px">
                    <a-select-option v-for="opt in dataScopeOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</a-select-option>
                  </a-select>
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
              <a-tab-pane key="data" tab="按钮权限">
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
              <a-tab-pane key="action" tab="API 权限">
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
        <a-form-item label="角色编码" required>
          <a-input v-model:value="addRoleCode" placeholder="如 ROLE_XXX" />
        </a-form-item>
        <a-form-item label="角色名称" required>
          <a-input v-model:value="addRoleName" placeholder="请输入角色名称" />
        </a-form-item>
        <a-form-item label="数据权限">
          <a-select v-model:value="addDataScope" style="width: 100%">
            <a-select-option v-for="opt in dataScopeOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="角色描述">
          <a-input v-model:value="addRoleDesc" placeholder="请输入角色描述" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import type { RoleItem, SysPermissionNode } from '@/types/system'
import { roleApi, permApi } from '@/api/system'

// 角色列表（映射后端 Role 字段）
interface RoleListItem {
  id: string
  roleCode: string
  roleName: string
  dataScope: string
  status: string
  remark: string
}

const roleList = ref<RoleListItem[]>([])
const selectedRoleId = ref<string | null>(null)

// 数据权限选项
const dataScopeOptions = [
  { value: 'ALL', label: '全部数据' },
  { value: 'REGION', label: '区域数据' },
  { value: 'STORE', label: '门店数据' },
  { value: 'SELF', label: '仅本人数据' },
  { value: 'CUSTOM', label: '自定义数据' },
  { value: 'NONE', label: '无数据权限' },
]

// 系统预置角色（不可删除），按 roleCode 前缀匹配
const BUILTIN_ROLE_PREFIXES = ['ROLE_ADMIN', 'ROLE_HQ', 'ROLE_REGIONAL', 'ROLE_MANAGER', 'ROLE_ASSOCIATE']
const isBuiltinRole = (role: RoleListItem) => BUILTIN_ROLE_PREFIXES.some(prefix => role.roleCode.startsWith(prefix))

// 编辑表单
const editName = ref('')
const editDesc = ref('')
const editDataScope = ref('ALL')
const saveLoading = ref(false)

// 权限标签页
const activeTab = ref('menu')

// 权限树数据（后端 /permissions/tree）
interface SimpleTreeNode {
  title: string
  key: string
  children?: SimpleTreeNode[]
}

const permTreeData = ref<SysPermissionNode[]>([])

// 按 permType 过滤权限树：菜单/按钮/API
const toSimpleTree = (nodes: SysPermissionNode[], permType: string): SimpleTreeNode[] => {
  const result: SimpleTreeNode[] = []
  for (const node of nodes) {
    const children = node.children ? toSimpleTree(node.children, permType) : []
    if (node.permType === permType || children.length > 0) {
      result.push({ title: node.permName, key: String(node.id), children })
    }
  }
  return result
}

const menuTreeData = computed<SimpleTreeNode[]>(() => toSimpleTree(permTreeData.value, 'MENU'))
const dataTreeData = computed<SimpleTreeNode[]>(() => toSimpleTree(permTreeData.value, 'BUTTON'))
const actionTreeData = computed<SimpleTreeNode[]>(() => toSimpleTree(permTreeData.value, 'API'))

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
const addRoleCode = ref('')
const addDataScope = ref('ALL')
const handleAddRole = () => {
  addRoleCode.value = ''
  addRoleName.value = ''
  addRoleDesc.value = ''
  addDataScope.value = 'ALL'
  addModalVisible.value = true
}
const handleAddRoleOk = async () => {
  if (!addRoleCode.value.trim()) { message.warning('请输入角色编码'); return }
  if (!addRoleName.value.trim()) { message.warning('请输入角色名称'); return }
  // 规范化为大写，未带前缀时自动补 ROLE_
  let roleCode = addRoleCode.value.trim().toUpperCase()
  if (!roleCode.startsWith('ROLE_')) roleCode = `ROLE_${roleCode}`
  if (!/^ROLE_[A-Z0-9_]+$/.test(roleCode)) { message.warning('角色编码仅支持大写字母、数字、下划线'); return }
  saveLoading.value = true
  try {
    await roleApi.create({
      roleCode,
      roleName: addRoleName.value.trim(),
      dataScope: addDataScope.value,
      remark: addRoleDesc.value.trim() || undefined,
    })
    message.success('新增成功')
    addModalVisible.value = false
    await loadRoles()
  } catch {
    message.error('新增失败')
  } finally {
    saveLoading.value = false
  }
}

// 加载已分配权限
const loadRolePermissions = async (roleId: string) => {
  const perms = await roleApi.getPermissions(roleId)
  const keys = perms.map(p => String(p.id))
  const keysOf = (tree: SimpleTreeNode[]) => getAllKeys(tree)
  menuCheckedKeys.value = keys.filter(k => keysOf(menuTreeData.value).includes(k))
  dataCheckedKeys.value = keys.filter(k => keysOf(dataTreeData.value).includes(k))
  actionCheckedKeys.value = keys.filter(k => keysOf(actionTreeData.value).includes(k))
  menuCheckAll.value = menuTreeData.value.length > 0 && menuCheckedKeys.value.length === keysOf(menuTreeData.value).length
  dataCheckAll.value = dataTreeData.value.length > 0 && dataCheckedKeys.value.length === keysOf(dataTreeData.value).length
  actionCheckAll.value = actionTreeData.value.length > 0 && actionCheckedKeys.value.length === keysOf(actionTreeData.value).length
}

// 选择角色
const handleSelectRole = async (role: RoleListItem) => {
  selectedRoleId.value = role.id
  editName.value = role.roleName
  editDesc.value = role.remark || ''
  editDataScope.value = role.dataScope || 'ALL'
  try {
    await loadRolePermissions(role.id)
  } catch (error) {
    console.error('加载角色权限失败', error)
  }
}

// 删除角色
const handleDeleteRole = async (role: RoleListItem) => {
  if (isBuiltinRole(role)) { message.warning('内置角色不可删除'); return }
  try {
    await roleApi.delete(role.id)
    message.success('删除成功')
    if (selectedRoleId.value === role.id) {
      selectedRoleId.value = null
      editName.value = ''
      editDesc.value = ''
    }
    await loadRoles()
  } catch {
    message.error('删除失败')
  }
}

// 重置
const handleReset = async () => {
  if (selectedRoleId.value) {
    const role = roleList.value.find(r => r.id === selectedRoleId.value)
    if (role) {
      await handleSelectRole(role)
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
    const role = roleList.value.find(r => r.id === selectedRoleId.value)
    await roleApi.update(selectedRoleId.value, {
      roleName: editName.value.trim(),
      dataScope: editDataScope.value,
      status: role?.status,
      remark: editDesc.value.trim() || undefined,
    })
    // 保存权限分配
    const allKeys = Array.from(new Set([
      ...menuCheckedKeys.value,
      ...dataCheckedKeys.value,
      ...actionCheckedKeys.value,
    ]))
    await roleApi.assignPermissions(selectedRoleId.value, allKeys)
    message.success('保存成功')
    await loadRoles()
  } catch (error) { console.error('保存失败', error) }
  finally { saveLoading.value = false }
}

// 加载角色列表
const loadRoles = async () => {
  try {
    const res = await roleApi.getList()
    roleList.value = (res.list || []).map((r: RoleItem) => ({
      id: String(r.id),
      roleCode: r.roleCode || '',
      roleName: r.roleName,
      dataScope: r.dataScope || 'ALL',
      status: r.status || 'ENABLED',
      remark: r.remark || '',
    }))
  } catch (error) { console.error('加载角色列表失败', error) }
}

onMounted(async () => {
  await loadRoles()
  try {
    permTreeData.value = await permApi.getTree()
  } catch (error) { console.error('加载权限树失败', error) }
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
.role-delete { float: right; color: #999; font-size: 13px; cursor: pointer; margin-top: -2px; }
.role-delete:hover { color: #ff4d4f; }
.role-delete-disabled { color: #d9d9d9; cursor: not-allowed; }
.role-delete-disabled:hover { color: #d9d9d9; }
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
