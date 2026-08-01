<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>会员管理</h2>
      <a-space>
        <a-button type="primary" @click="handleAddLevel">
          <PlusOutlined /> 新增会员等级
        </a-button>
        <a-button @click="handleLevelSettings">
          <SettingOutlined /> 会员等级设置
        </a-button>
      </a-space>
    </div>

    <!-- 搜索表单 -->
    <div class="content-card search-card">
      <a-form layout="inline" :model="searchForm" @finish="handleSearch">
        <a-form-item label="会员等级">
          <a-select
            v-model:value="searchForm.level"
            placeholder="请选择会员等级"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="钻石会员">钻石会员</a-select-option>
            <a-select-option value="VIP会员">VIP会员</a-select-option>
            <a-select-option value="普通会员">普通会员</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="会员姓名">
          <a-input
            v-model:value="searchForm.name"
            placeholder="请输入会员姓名"
            allow-clear
            style="width: 150px"
          />
        </a-form-item>
        <a-form-item label="手机号码">
          <a-input
            v-model:value="searchForm.phone"
            placeholder="请输入手机号"
            allow-clear
            style="width: 150px"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 120px"
          >
            <a-select-option value="enabled">启用</a-select-option>
            <a-select-option value="disabled">停用</a-select-option>
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

    <!-- 会员统计卡片 -->
    <div class="member-stats">
      <div class="stat-card vip">
        <div class="stat-icon">
          <CrownOutlined />
        </div>
        <div class="stat-info">
          <div class="stat-label">VIP会员</div>
          <div class="stat-value">{{ memberStats.vipCount }} <span class="stat-unit">人</span></div>
        </div>
      </div>
      <div class="stat-card normal">
        <div class="stat-icon">
          <UserOutlined />
        </div>
        <div class="stat-info">
          <div class="stat-label">普通会员</div>
          <div class="stat-value">{{ memberStats.normalCount }} <span class="stat-unit">人</span></div>
        </div>
      </div>
      <div class="stat-card diamond">
        <div class="stat-icon">
          <StarOutlined />
        </div>
        <div class="stat-info">
          <div class="stat-label">钻石会员</div>
          <div class="stat-value">{{ memberStats.diamondCount }} <span class="stat-unit">人</span></div>
        </div>
      </div>
      <div class="stat-card total">
        <div class="stat-icon">
          <TeamOutlined />
        </div>
        <div class="stat-info">
          <div class="stat-label">会员总数</div>
          <div class="stat-value">{{ memberStats.totalCount }} <span class="stat-unit">人</span></div>
        </div>
      </div>
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
        :scroll="{ x: 900 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            <a-tag :color="getLevelColor(record.name)">{{ record.name }}</a-tag>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'enabled' ? 'green' : 'red'">
              {{ record.status === 'enabled' ? '启用' : '停用' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <div class="action-btns">
              <a @click="handleEdit(record)" class="action-link">编辑</a>
              <a-divider type="vertical" />
              <a v-if="record.status === 'enabled'" @click="handleDisable(record)" class="action-link danger">停用</a>
              <a v-else @click="handleEnable(record)" class="action-link">启用</a>
            </div>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑会员等级' : '新增会员等级'"
      @ok="handleModalOk"
      :confirm-loading="modalLoading"
      width="600px"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="会员等级" name="name">
          <a-input v-model:value="formData.name" placeholder="请输入会员等级名称" />
        </a-form-item>
        <a-form-item label="会员标识" name="标识">
          <a-input v-model:value="formData['标识']" placeholder="请输入会员标识（如VIP、Diamond）" />
        </a-form-item>
        <a-form-item label="积分倍数" name="pointsMultiplier">
          <a-input-number v-model:value="formData.pointsMultiplier" :min="1" :max="10" style="width: 100%" />
        </a-form-item>
        <a-form-item label="折扣率" name="discount">
          <a-input-number v-model:value="formData.discount" :min="1" :max="10" :step="0.5" :precision="1" style="width: 100%" />
        </a-form-item>
        <a-form-item label="专属权益" name="benefits">
          <a-textarea v-model:value="formData.benefits" :rows="3" placeholder="请输入专属权益，多个用逗号分隔" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  SearchOutlined,
  SettingOutlined,
  CrownOutlined,
  UserOutlined,
  StarOutlined,
  TeamOutlined
} from '@ant-design/icons-vue'
import type { MemberLevel, MemberQueryParams, MemberStats, MemberLevelStatus } from '@/types/customer'
import { memberApi } from '@/api/customer'

// 会员统计
const memberStats = reactive<MemberStats>({
  vipCount: 0,
  normalCount: 0,
  diamondCount: 0,
  totalCount: 0
})

// 搜索表单
const searchForm = reactive({
  level: undefined as string | undefined,
  name: '',
  phone: '',
  status: undefined as MemberLevelStatus | undefined
})

// 表格数据
const tableData = ref<MemberLevel[]>([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

// 表格列配置
const columns = [
  { title: '会员等级', dataIndex: 'name', key: 'name', width: 120 },
  { title: '会员标识', dataIndex: '标识', key: '标识', width: 100 },
  { title: '会员数量', dataIndex: 'memberCount', key: 'memberCount', width: 100, align: 'right' as const },
  { title: '累计消费(元)', dataIndex: 'totalConsumption', key: 'totalConsumption', width: 130, align: 'right' as const },
  { title: '积分倍数', dataIndex: 'pointsMultiplier', key: 'pointsMultiplier', width: 90, align: 'center' as const },
  { title: '折扣率', dataIndex: 'discount', key: 'discount', width: 80, align: 'center' as const },
  { title: '专属权益', dataIndex: 'benefits', key: 'benefits', width: 200 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 70, align: 'center' as const },
  { title: '操作', key: 'action', width: 120, fixed: 'right' as const }
]

// 弹窗相关
const modalVisible = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const formRef = ref()
const formData = reactive({
  id: '',
  name: '',
  '标识': '',
  pointsMultiplier: 1,
  discount: 9.5,
  benefits: ''
})

const formRules = {
  name: [{ required: true, message: '请输入会员等级名称', trigger: 'blur' }],
  '标识': [{ required: true, message: '请输入会员标识', trigger: 'blur' }]
}

// 等级颜色
const getLevelColor = (name: string) => {
  const map: Record<string, string> = {
    '钻石会员': 'purple',
    'VIP会员': 'gold',
    '普通会员': 'blue'
  }
  return map[name] || 'default'
}

// 加载统计
const loadStats = async () => {
  try {
    const stats = await memberApi.getStats()
    Object.assign(memberStats, stats)
  } catch (error) {
    console.error('加载统计失败', error)
  }
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params: MemberQueryParams = {
      level: searchForm.level,
      name: searchForm.name || undefined,
      phone: searchForm.phone || undefined,
      status: searchForm.status,
      page: pagination.current,
      pageSize: pagination.pageSize
    }
    const res = await memberApi.getList(params)
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

// 重置
const handleReset = () => {
  searchForm.level = undefined
  searchForm.name = ''
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

// 新增等级
const handleAddLevel = () => {
  isEdit.value = false
  resetForm()
  modalVisible.value = true
}

// 等级设置
const handleLevelSettings = () => {
  message.info('会员等级设置功能开发中...')
}

// 编辑
const handleEdit = (record: MemberLevel) => {
  isEdit.value = true
  formData.id = record.id
  formData.name = record.name
  formData['标识'] = record['标识']
  formData.pointsMultiplier = record.pointsMultiplier
  formData.discount = record.discount
  formData.benefits = record.benefits
  modalVisible.value = true
}

// 停用
const handleDisable = async (record: MemberLevel) => {
  try {
    await memberApi.update(record.id, { status: 'disabled' })
    message.success('已停用')
    loadData()
  } catch (error) {
    message.error('操作失败')
  }
}

// 启用
const handleEnable = async (record: MemberLevel) => {
  try {
    await memberApi.update(record.id, { status: 'enabled' })
    message.success('已启用')
    loadData()
  } catch (error) {
    message.error('操作失败')
  }
}

// 弹窗确认
const handleModalOk = async () => {
  try {
    await formRef.value?.validateFields()
    modalLoading.value = true

    const submitData = {
      name: formData.name,
      '标识': formData['标识'],
      pointsMultiplier: formData.pointsMultiplier,
      discount: formData.discount,
      benefits: formData.benefits
    }

    if (isEdit.value) {
      await memberApi.update(formData.id, submitData)
      message.success('更新成功')
    } else {
      await memberApi.create(submitData)
      message.success('新增成功')
    }

    modalVisible.value = false
    loadData()
    loadStats()
  } catch (error) {
    console.error('表单验证失败', error)
  } finally {
    modalLoading.value = false
  }
}

// 重置表单
const resetForm = () => {
  formData.id = ''
  formData.name = ''
  formData['标识'] = ''
  formData.pointsMultiplier = 1
  formData.discount = 9.5
  formData.benefits = ''
}

onMounted(() => {
  loadStats()
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

/* 会员统计卡片 */
.member-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.stat-card.vip .stat-icon {
  background: linear-gradient(135deg, #ffd700, #ffb300);
  color: #fff;
}

.stat-card.normal .stat-icon {
  background: linear-gradient(135deg, #69c0ff, #1890ff);
  color: #fff;
}

.stat-card.diamond .stat-icon {
  background: linear-gradient(135deg, #b37feb, #722ed1);
  color: #fff;
}

.stat-card.total .stat-icon {
  background: linear-gradient(135deg, #95de64, #52c41a);
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #333;
}

.stat-unit {
  font-size: 14px;
  font-weight: 400;
  color: #999;
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

.action-link.danger {
  color: #ff4d4f;
}

.action-link.danger:hover {
  color: #ff7875;
  background: #fff1f0;
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
  min-width: 800px;
}

/* 响应式 */
@media (max-width: 992px) {
  .member-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}

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

  .member-stats {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .stat-card {
    padding: 16px;
  }

  .stat-icon {
    width: 48px;
    height: 48px;
    font-size: 20px;
  }

  .stat-value {
    font-size: 24px;
  }

  .table-card :deep(.ant-table) {
    font-size: 13px;
    min-width: 700px;
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
    min-width: 600px;
  }
}
</style>
