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
            <a-select-option value="enabled">启用</a-select-option>
            <a-select-option value="disabled">禁用</a-select-option>
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
            <a-tag :color="record.status === 'enabled' ? 'green' : 'red'">
              {{ record.status === 'enabled' ? '启用' : '禁用' }}
            </a-tag>
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
                    <a-menu-item key="disable" v-if="record.status === 'enabled'">
                      禁用用户
                    </a-menu-item>
                    <a-menu-item key="enable" v-else>
                      启用用户
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
          <a-input v-model:value="formData.username" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item label="手机号" name="phone">
          <a-input v-model:value="formData.phone" placeholder="请输入手机号" />
        </a-form-item>
        <a-form-item label="所属部门" name="department">
          <a-input v-model:value="formData.department" placeholder="请输入所属部门" />
        </a-form-item>
        <a-form-item label="角色" name="role">
          <a-select v-model:value="formData.role" placeholder="请选择角色">
            <a-select-option v-for="item in roleOptions" :key="item.id" :value="item.name">
              {{ item.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="用户状态" name="status">
          <a-select v-model:value="formData.status" placeholder="请选择状态">
            <a-select-option value="enabled">启用</a-select-option>
            <a-select-option value="disabled">禁用</a-select-option>
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
import { userApi, roleApi } from '@/api/mock/system'

// 搜索表单
const searchForm = reactive({
  username: '',
  phone: '',
  status: undefined as UserStatus | undefined
})

// 表格数据
const tableData = ref<UserItem[]>([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

// 表格列配置
const columns = [
  { title: '用户名', dataIndex: 'username', key: 'username', width: 120 },
  { title: '手机号', dataIndex: 'phone', key: 'phone', width: 130 },
  { title: '所属部门', dataIndex: 'department', key: 'department', width: 120 },
  { title: '角色', dataIndex: 'role', key: 'role', width: 120 },
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
  department: '',
  role: '' as string,
  status: 'enabled' as UserStatus
})

const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  department: [{ required: true, message: '请输入所属部门', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  status: [{ required: true, message: '请选择用户状态', trigger: 'change' }]
}

// 角色选项
const roleOptions = ref<{ id: string; name: string }[]>([])

// 加载角色列表
const loadRoles = async () => {
  try {
    roleOptions.value = await roleApi.getList()
  } catch {
    // ignore
  }
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      username: searchForm.username || undefined,
      phone: searchForm.phone || undefined,
      status: searchForm.status,
      page: pagination.current,
      pageSize: pagination.pageSize
    }
    const res = await userApi.getList(params)
    tableData.value = res.list
    pagination.total = res.total
  } catch (error) {
    message.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置搜索
const handleReset = () => {
  searchForm.username = ''
  searchForm.phone = ''
  searchForm.status = undefined
  handleSearch()
}

// 分页
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadData()
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
  formData.department = record.department
  formData.role = record.role
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
const handleMoreAction = (key: string, record: UserItem) => {
  if (key === 'disable') {
    Modal.confirm({
      title: '禁用用户',
      content: `确定要禁用用户「${record.username}」吗？`,
      onOk() {
        record.status = 'disabled'
        message.success('用户已禁用')
        loadData()
      }
    })
  } else if (key === 'enable') {
    record.status = 'enabled'
    message.success('用户已启用')
    loadData()
  } else if (key === 'delete') {
    Modal.confirm({
      title: '删除用户',
      content: `确定要删除用户「${record.username}」吗？此操作不可恢复。`,
      okText: '删除',
      okType: 'danger',
      onOk() {
        const idx = tableData.value.findIndex(item => item.id === record.id)
        if (idx !== -1) tableData.value.splice(idx, 1)
        pagination.total -= 1
        message.success('删除成功')
      }
    })
  }
}

// 弹窗确认
const handleModalOk = async () => {
  try {
    await formRef.value?.validateFields()
    modalLoading.value = true

    // 模拟延迟
    await new Promise(r => setTimeout(r, 400))

    if (isEdit.value) {
      // 更新本地数据
      const item = tableData.value.find(i => i.id === formData.id)
      if (item) {
        item.username = formData.username
        item.phone = formData.phone
        item.department = formData.department
        item.role = formData.role
        item.status = formData.status
      }
      message.success('更新成功')
    } else {
      // 新增到本地数据
      const newItem: UserItem = {
        id: String(Date.now()),
        username: formData.username,
        phone: formData.phone,
        department: formData.department,
        role: formData.role,
        status: formData.status,
        createdAt: new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')
      }
      tableData.value.unshift(newItem)
      pagination.total += 1
      message.success('新增成功')
    }

    modalVisible.value = false
  } catch (error) {
    // 表单验证失败，不做处理
  } finally {
    modalLoading.value = false
  }
}

// 重置表单
const resetForm = () => {
  formData.id = ''
  formData.username = ''
  formData.phone = ''
  formData.department = ''
  formData.role = ''
  formData.status = 'enabled'
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
