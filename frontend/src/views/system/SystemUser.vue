<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>用户管理</h2>
      <a-space>
        <a-button type="primary" @click="handleAdd">
          <PlusOutlined /> 新增用户
        </a-button>
      </a-space>
    </div>

    <!-- 搜索表单 -->
    <div class="content-card search-card">
      <a-form layout="inline" :model="searchForm" @finish="handleSearch">
        <a-form-item label="用户名">
          <a-input
            v-model:value="searchForm.username"
            placeholder="请输入用户名"
            allow-clear
            style="width: 160px"
          />
        </a-form-item>
        <a-form-item label="手机号">
          <a-input
            v-model:value="searchForm.phone"
            placeholder="请输入手机号"
            allow-clear
            style="width: 160px"
          />
        </a-form-item>
        <a-form-item label="用户状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 130px"
          >
            <a-select-option value="ENABLED">启用</a-select-option>
            <a-select-option value="DISABLED">禁用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button @click="handleReset">重置</a-button>
            <a-button type="primary" html-type="submit">
              <SearchOutlined /> 查询
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>

    <!-- 数据表格 -->
    <div class="content-card table-card">
      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="id"
        :scroll="{ x: 1100 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'ENABLED' ? 'green' : 'red'">
              {{ record.status === 'ENABLED' ? '启用' : '禁用' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'roleNames'">
            {{ (record.roleNames || []).join('、') || '-' }}
          </template>
          <template v-if="column.key === 'action'">
            <div class="action-btns">
              <a @click="handleEdit(record)" class="action-link">编辑</a>
              <a-divider type="vertical" />
              <a @click="handleResetPassword(record)" class="action-link">重置密码</a>
              <a-divider type="vertical" />
              <a-dropdown :trigger="['click']">
                <a class="action-link">
                  更多 <DownOutlined style="font-size: 10px" />
                </a>
                <template #overlay>
                  <a-menu @click="({ key }: any) => handleMoreAction(key, record)">
                    <a-menu-item key="disable" v-if="record.status === 'ENABLED'">
                      禁用用户
                    </a-menu-item>
                    <a-menu-item key="enable" v-else>
                      启用用户
                    </a-menu-item>
                    <a-menu-item key="forceLogout">
                      强制下线
                    </a-menu-item>
                    <a-menu-item key="delete" class="danger-menu-item">
                      删除用户
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </div>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑用户' : '新增用户'"
      @ok="handleModalOk"
      :confirm-loading="modalLoading"
      width="520px"
      :body-style="{ paddingTop: '20px' }"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="用户名" name="username">
          <a-input v-model:value="formData.username" placeholder="请输入用户名" :disabled="isEdit" />
        </a-form-item>
        <a-form-item label="手机号" name="phone">
          <a-input v-model:value="formData.phone" placeholder="请输入手机号" />
        </a-form-item>
        <a-form-item label="职位" name="position">
          <a-input v-model:value="formData.position" placeholder="请输入职位" />
        </a-form-item>
        <a-form-item v-if="!isEdit" label="初始密码" name="password">
          <a-input-password v-model:value="formData.password" placeholder="默认 123456" />
        </a-form-item>
        <a-form-item label="角色" name="role">
          <a-select v-model:value="formData.role" placeholder="请选择角色">
            <a-select-option v-for="item in roleOptions" :key="item.id" :value="item.id">
              {{ item.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="用户状态" name="status">
          <a-select v-model:value="formData.status" placeholder="请选择状态">
            <a-select-option value="ENABLED">启用</a-select-option>
            <a-select-option value="DISABLED">禁用</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { PlusOutlined, SearchOutlined, DownOutlined } from '@ant-design/icons-vue'
import type { UserItem, UserStatus } from '@/types/system'
import { userApi, roleApi } from '@/api/system'
import { useCrudTable } from '@/composables/useCrudTable'

// 搜索表单
const searchForm = reactive({
  username: '',
  phone: '',
  status: undefined as UserStatus | undefined
})

// 表格数据
const { tableData, loading, pagination, loadData, handleSearch, handleTableChange } = useCrudTable({
  searchForm,
  loadFn: (params) => userApi.getList({
    keyword: params.username || params.phone || undefined,
    status: params.status,
    page: params.page,
    pageSize: params.pageSize,
  }),
})

// 表格列配置
const columns = [
  { title: '用户名', dataIndex: 'username', key: 'username', width: 120 },
  { title: '手机号', dataIndex: 'phone', key: 'phone', width: 130 },
  { title: '职位', dataIndex: 'position', key: 'position', width: 120 },
  { title: '角色', dataIndex: 'roleNames', key: 'roleNames', width: 150 },
  { title: '用户状态', dataIndex: 'status', key: 'status', width: 90, align: 'center' as const },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 160 },
  { title: '操作', key: 'action', width: 180, fixed: 'right' as const }
]

// 弹窗相关
const modalVisible = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const formRef = ref()
const formData = reactive({
  id: '',
  username: '',
  phone: '',
  position: '',
  role: '' as string,
  password: '',
  status: 'ENABLED' as UserStatus
})

const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
}

// 角色选项
const roleOptions = ref<{ id: string; name: string }[]>([])

// 加载角色列表
const loadRoles = async () => {
  try {
    const res = await roleApi.getList()
    roleOptions.value = (res.list || []).map((r: any) => ({ id: String(r.id), name: r.roleName || r.name }))
  } catch {
    roleOptions.value = []
  }
}

// 重置搜索
const handleReset = () => {
  searchForm.username = ''
  searchForm.phone = ''
  searchForm.status = undefined
  handleSearch()
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  resetForm()
  modalVisible.value = true
}

// 编辑
const handleEdit = (record: UserItem) => {
  isEdit.value = true
  formData.id = record.id
  formData.username = record.username
  formData.phone = record.phone
  formData.position = record.position || ''
  formData.role = record.roleIds?.[0] ? String(record.roleIds[0]) : ''
  formData.status = record.status
  modalVisible.value = true
}

// 重置密码
const handleResetPassword = (record: UserItem) => {
  Modal.confirm({
    title: '重置密码',
    content: `确定要重置用户「${record.username}」的密码吗？`,
    onOk() {
      message.success('密码已重置为默认密码：123456')
    }
  })
}

// 更多操作
const handleMoreAction = async (key: string, record: UserItem) => {
  if (key === 'disable') {
    Modal.confirm({
      title: '禁用用户',
      content: `确定要禁用用户「${record.username}」吗？`,
      onOk: async () => {
        try {
          await userApi.update(record.id, { status: 'DISABLED' })
          message.success('用户已禁用')
          loadData()
        } catch (e) {
          message.error('操作失败')
        }
      }
    })
  } else if (key === 'enable') {
    try {
      await userApi.update(record.id, { status: 'ENABLED' })
      message.success('用户已启用')
      loadData()
    } catch (e) {
      message.error('操作失败')
    }
  } else if (key === 'forceLogout') {
    Modal.confirm({
      title: '强制下线',
      content: `确定要强制下线用户「${record.username}」吗？该用户的所有登录态将立即失效，需要重新登录。`,
      okText: '确认下线',
      okType: 'danger',
      onOk: async () => {
        try {
          await userApi.forceLogout(record.id)
          message.success('强制下线成功，用户需重新登录')
          loadData()
        } catch (e) {
          message.error('操作失败')
        }
      }
    })
  } else if (key === 'delete') {
    Modal.confirm({
      title: '删除用户',
      content: `确定要删除用户「${record.username}」吗？此操作不可恢复。`,
      okText: '删除',
      okType: 'danger',
      onOk: async () => {
        try {
          await userApi.delete(record.id)
          message.success('删除成功')
          loadData()
        } catch (e) {
          message.error('删除失败')
        }
      }
    })
  }
}

// 弹窗确认
const handleModalOk = async () => {
  try {
    await formRef.value?.validateFields()
    modalLoading.value = true

    const payload: any = {
      phone: formData.phone,
      position: formData.position,
    }
    const roleIds = formData.role ? [Number(formData.role)] : []

    if (isEdit.value) {
      payload.status = formData.status
      payload.roleIds = roleIds
      await userApi.update(formData.id, payload)
      message.success('更新成功')
    } else {
      payload.username = formData.username
      payload.realName = formData.username
      payload.password = formData.password || '123456'
      payload.roleIds = roleIds
      await userApi.create(payload)
      message.success('新增成功')
    }

    modalVisible.value = false
    loadData()
  } catch (error: any) {
    message.error(error?.message || '操作失败')
  } finally {
    modalLoading.value = false
  }
}

// 重置表单
const resetForm = () => {
  formData.id = ''
  formData.username = ''
  formData.phone = ''
  formData.position = ''
  formData.role = ''
  formData.password = ''
  formData.status = 'ENABLED'
}

onMounted(() => {
  loadRoles()
  loadData()
})
</script>

<style scoped>
.page-container {
  padding: 24px;
  min-height: calc(100vh - 64px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.content-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 16px;
}

.table-card {
  padding: 16px;
  overflow: hidden;
}

.search-card {
  padding: 16px 24px;
}

.search-card :deep(.ant-form) {
  flex-wrap: wrap;
}

.search-card :deep(.ant-form-item) {
  margin-bottom: 12px;
  margin-right: 0;
}

.action-link {
  font-size: 13px;
  color: #1890ff;
  padding: 2px 6px;
  border-radius: 4px;
  transition: all 0.2s;
  cursor: pointer;
}

.action-link:hover {
  color: #40a9ff;
  background: #e6f7ff;
}

.action-btns {
  display: flex;
  align-items: center;
  gap: 2px;
  white-space: nowrap;
}

.action-btns :deep(.ant-divider-vertical) {
  margin: 0 2px;
}

.table-card :deep(.ant-table-wrapper) {
  overflow-x: auto;
}

.table-card :deep(.ant-table) {
  min-width: 1000px;
}

:deep(.danger-menu-item) {
  color: #ff4d4f;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-container {
    padding: 16px;
  }

  .content-card {
    padding: 16px;
    margin-bottom: 12px;
  }

  .table-card {
    padding: 12px;
  }

  .search-card {
    padding: 12px 16px;
  }

  .search-card :deep(.ant-form-item) {
    width: 100%;
  }

  .search-card :deep(.ant-form-item-control) {
    flex: 1;
  }

  .table-card :deep(.ant-table) {
    font-size: 13px;
    min-width: 800px;
  }

  .table-card :deep(.ant-table-thead > tr > th),
  .table-card :deep(.ant-table-tbody > tr > td) {
    padding: 10px 8px;
  }
}

@media (max-width: 576px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .page-header h2 {
    font-size: 18px;
  }

  .table-card :deep(.ant-table) {
    font-size: 12px;
    min-width: 700px;
  }
}
</style>
