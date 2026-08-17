<template>
  <div class="page-container">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <h2>您好，{{ welcomeName }} 👋</h2>
        <p>我可以帮你快速处理工作任务，提供智能建议和数据分析，<br />让工作更高效、更轻松。</p>
        <div class="welcome-actions">
          <a-button type="primary" size="large" @click="handleStartChat">
            开始提问
          </a-button>
          <a-button size="large" @click="handleGuide">
            使用指南
          </a-button>
        </div>
      </div>
      <div class="welcome-illustration">
        <div class="ai-robot">
          <div class="robot-body">
            <div class="robot-face">
              <div class="robot-eye left"></div>
              <div class="robot-eye right"></div>
            </div>
          </div>
          <div class="robot-platform"></div>
        </div>
      </div>
    </div>

    <!-- 智能工具 -->
    <div class="section-card">
      <div class="section-title">智能工具</div>
      <div class="tools-grid">
        <div v-for="tool in tools" :key="tool.id" class="tool-card" @click="handleToolClick(tool)">
          <div class="tool-icon" :style="{ background: tool.color + '15', color: tool.color }">
            <component :is="getToolIcon(tool.icon)" />
          </div>
          <div class="tool-info">
            <div class="tool-name">{{ tool.name }}</div>
            <div class="tool-desc">{{ tool.description }}</div>
          </div>
          <a-button class="tool-btn" @click.stop="handleToolUse(tool)">立即使用</a-button>
        </div>
      </div>
    </div>

    <!-- 最近对话 + 推荐场景 -->
    <div class="bottom-row">
      <!-- 最近对话 -->
      <div class="section-card flex-1">
        <div class="section-header">
          <span class="section-title">最近对话</span>
          <a class="view-all" @click="handleViewAll">查看全部</a>
        </div>
        <div class="conversation-list">
          <div v-for="item in conversations" :key="item.id" class="conversation-item" @click="handleConversationClick(item)">
            <div class="conversation-icon">
              <MessageOutlined />
            </div>
            <div class="conversation-info">
              <div class="conversation-title">{{ item.title }}</div>
            </div>
            <div class="conversation-time">{{ item.time }}</div>
          </div>
        </div>
      </div>

      <!-- 推荐场景 -->
      <div class="section-card flex-1">
        <div class="section-header">
          <span class="section-title">推荐场景</span>
          <a class="refresh-link" @click="handleRefresh">
            换一批 <ReloadOutlined />
          </a>
        </div>
        <div class="scenario-list">
          <div v-for="item in scenarios" :key="item.id" class="scenario-item" @click="handleScenarioClick(item)">
            <div class="scenario-icon" :style="{ background: item.color + '15', color: item.color }">
              <component :is="getScenarioIcon(item.icon)" />
            </div>
            <div class="scenario-info">
              <div class="scenario-title">{{ item.title }}</div>
              <div class="scenario-desc">{{ item.description }}</div>
            </div>
            <RightOutlined class="scenario-arrow" />
          </div>
        </div>
      </div>
    </div>

    <!-- 温馨提示 -->
    <div class="notice-bar">
      <InfoCircleOutlined class="notice-icon" />
      <span>温馨提示：AI生成的内容仅供参考，请结合实际业务情况进行判断和决策</span>
      <a class="notice-close" @click="showNotice = false">
        <CloseOutlined />
      </a>
    </div>

    <!-- 使用指南弹窗 -->
    <a-modal v-model:open="guideVisible" title="使用指南" :footer="null" width="640">
      <div class="guide-content">
        <div class="guide-item">
          <div class="guide-title">智能问答</div>
          <div class="guide-desc">在「智能问答」中输入业务问题，AI 快速给出专业解答，并自动保存到历史记录。</div>
        </div>
        <div class="guide-item">
          <div class="guide-title">智能建议</div>
          <div class="guide-desc">选择分析类型（员工/货品/场景/门店综合）与分析对象，点击「开始分析」，生成对应建议。</div>
        </div>
        <div class="guide-item">
          <div class="guide-title">数据分析</div>
          <div class="guide-desc">选择门店与时间范围，生成销售趋势、渠道分布等可视化分析报表。</div>
        </div>
        <div class="guide-item">
          <div class="guide-title">文档生成</div>
          <div class="guide-desc">选择提示词模板并填写内容要点，AI 生成对应文档。</div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, markRaw, onMounted, type Component } from 'vue'
import { useRouter } from 'vue-router'
import {
  MessageOutlined, FileTextOutlined, BarChartOutlined, BulbOutlined,
  RightOutlined, ReloadOutlined, InfoCircleOutlined, CloseOutlined,
  LineChartOutlined, UserOutlined, ShoppingOutlined, PercentageOutlined
} from '@ant-design/icons-vue'
import type { AiTool, RecentConversation, RecommendScenario } from '@/types/ai'
import { aiApi } from '@/api/ai'
import { useAuthStore } from '@/stores/useAuthStore'

// 路由
const router = useRouter()

// 欢迎语用户名（读当前登录用户）
const authStore = useAuthStore()
const welcomeName = computed(() => {
  const info: any = authStore.userInfo
  return info?.realName || info?.username || '管理员'
})

// 数据
const tools = ref<AiTool[]>([])
const conversations = ref<RecentConversation[]>([])
const scenarios = ref<RecommendScenario[]>([])
const showNotice = ref(true)
const guideVisible = ref(false)

// 工具页面路由映射
const toolRouteMap: Record<string, string> = {
  '智能问答': '/ai/chat',
  '文档生成': '/ai/doc',
  '数据分析': '/ai/analysis',
  '智能建议': '/ai/suggest',
  '员工建议': '/ai/suggest',
  '货品建议': '/ai/suggest',
  '场景建议': '/ai/suggest',
  '门店综合建议': '/ai/suggest',
  '任务评分': '/ai/suggest',
}

// 图标与配色映射（UI 展示属性，由前端按工具类型分配）
const iconMap: Record<string, Component> = {
  MessageOutlined: markRaw(MessageOutlined),
  FileTextOutlined: markRaw(FileTextOutlined),
  BarChartOutlined: markRaw(BarChartOutlined),
  BulbOutlined: markRaw(BulbOutlined),
  LineChartOutlined: markRaw(LineChartOutlined),
  UserOutlined: markRaw(UserOutlined),
  ShoppingOutlined: markRaw(ShoppingOutlined),
  PercentageOutlined: markRaw(PercentageOutlined),
}
const toolStyleMap: Record<string, { icon: string; color: string }> = {
  '员工建议': { icon: 'UserOutlined', color: '#1890ff' },
  '货品建议': { icon: 'ShoppingOutlined', color: '#52c41a' },
  '场景建议': { icon: 'PercentageOutlined', color: '#fa8c16' },
  '门店综合建议': { icon: 'BarChartOutlined', color: '#722ed1' },
  '任务评分': { icon: 'LineChartOutlined', color: '#eb2f96' },
  '数据分析': { icon: 'LineChartOutlined', color: '#13c2c2' },
  '文档生成': { icon: 'FileTextOutlined', color: '#13c2c2' },
}

const getToolIcon = (icon: string) => iconMap[icon] || MessageOutlined
const getScenarioIcon = (icon: string) => iconMap[icon] || MessageOutlined

// 推荐场景独立静态配置（不复用工具列表）
const scenarioConfig: RecommendScenario[] = [
  { id: 'scenario-employee', title: '员工建议', description: 'AI员工画像分析与辅导建议', icon: 'UserOutlined', color: '#1890ff' },
  { id: 'scenario-product', title: '货品建议', description: 'AI货品运营分析与推荐', icon: 'ShoppingOutlined', color: '#52c41a' },
  { id: 'scenario-scene', title: '场景建议', description: 'AI场景问题诊断与优化', icon: 'PercentageOutlined', color: '#fa8c16' },
  { id: 'scenario-store', title: '门店综合建议', description: 'AI门店综合诊断报告', icon: 'BarChartOutlined', color: '#722ed1' },
  { id: 'scenario-analysis', title: '数据分析', description: 'AI经营数据分析与洞察', icon: 'LineChartOutlined', color: '#eb2f96' },
]

const shuffle = <T>(arr: T[]): T[] => {
  const a = [...arr]
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[a[i], a[j]] = [a[j], a[i]]
  }
  return a
}

// 加载真实数据
const loadData = async () => {
  try {
    const list = await aiApi.getTools()
    tools.value = list.map(t => {
      const style = toolStyleMap[t.name] || { icon: 'BulbOutlined', color: '#c8a44d' }
      return { id: t.id, name: t.name, description: t.description, icon: style.icon, color: style.color }
    })
  } catch (e) {
    tools.value = []
  }
  scenarios.value = shuffle(scenarioConfig)
  try {
    const history = await aiApi.getChatHistory()
    conversations.value = history.slice(0, 8).map(h => ({
      id: String(h.id),
      title: h.question.slice(0, 20),
      time: (h.createdAt || '').slice(5, 16),
    }))
  } catch (e) {
    conversations.value = []
  }
}

// 事件处理
const handleStartChat = () => {
  router.push('/ai/chat')
}

const handleGuide = () => {
  guideVisible.value = true
}

const handleToolClick = (tool: AiTool) => {
  handleToolUse(tool)
}

const handleToolUse = (tool: AiTool) => {
  const route = toolRouteMap[tool.name]
  if (route) {
    router.push(route)
  } else {
    router.push('/ai/chat')
  }
}

const handleViewAll = () => {
  router.push('/ai/chat')
}

const handleRefresh = () => {
  scenarios.value = shuffle(scenarioConfig)
}

const handleConversationClick = (_item: RecentConversation) => {
  router.push('/ai/chat')
}

const handleScenarioClick = (item: RecommendScenario) => {
  const route = toolRouteMap[item.title]
  if (route) router.push(route)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-container {
  padding: 24px;
  min-height: calc(100vh - 64px);
}

/* 欢迎横幅 */
.welcome-banner {
  background: linear-gradient(135deg, #fff9e6 0%, #fff3cc 100%);
  border-radius: 16px;
  padding: 40px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  overflow: hidden;
}

.welcome-content h2 {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin-bottom: 12px;
}

.welcome-content p {
  font-size: 14px;
  color: #666;
  line-height: 1.8;
  margin-bottom: 24px;
}

.welcome-actions {
  display: flex;
  gap: 12px;
}

.welcome-actions .ant-btn-primary {
  background: linear-gradient(135deg, #c8a44d, #e8d59a);
  border: none;
  border-radius: 8px;
  padding: 8px 24px;
  font-weight: 600;
}

.welcome-actions .ant-btn-default {
  border-radius: 8px;
  padding: 8px 24px;
}

.welcome-illustration {
  flex-shrink: 0;
}

/* AI 机器人插图 */
.ai-robot {
  position: relative;
  width: 200px;
  height: 160px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.robot-body {
  width: 120px;
  height: 100px;
  background: linear-gradient(135deg, #c8a44d, #e8d59a);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(200, 164, 77, 0.3);
}

.robot-face {
  display: flex;
  gap: 20px;
}

.robot-eye {
  width: 24px;
  height: 24px;
  background: #fff;
  border-radius: 50%;
  position: relative;
}

.robot-eye::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 10px;
  height: 10px;
  background: #333;
  border-radius: 50%;
}

.robot-platform {
  width: 140px;
  height: 20px;
  background: linear-gradient(180deg, #e8d59a, #c8a44d);
  border-radius: 50%;
  margin-top: -10px;
  box-shadow: 0 4px 12px rgba(200, 164, 77, 0.2);
}

/* 通用区块 */
.section-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.view-all, .refresh-link {
  font-size: 14px;
  color: #999;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}

.view-all:hover, .refresh-link:hover {
  color: #c8a44d;
}

/* 智能工具 */
.tools-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.tool-card {
  background: #fafafa;
  border-radius: 12px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tool-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.tool-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.tool-info {
  flex: 1;
}

.tool-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.tool-desc {
  font-size: 13px;
  color: #999;
  line-height: 1.6;
}

.tool-btn {
  border-radius: 8px;
  color: #c8a44d;
  border-color: #c8a44d;
}

.tool-btn:hover {
  color: #b8943d;
  border-color: #b8943d;
}

/* 底部双列 */
.bottom-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.flex-1 {
  flex: 1;
}

/* 最近对话 */
.conversation-list {
  display: flex;
  flex-direction: column;
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  transition: background 0.2s;
}

.conversation-item:last-child {
  border-bottom: none;
}

.conversation-item:hover {
  background: #fafafa;
}

.conversation-icon {
  width: 36px;
  height: 36px;
  background: #e6f7ff;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1890ff;
  font-size: 16px;
  flex-shrink: 0;
}

.conversation-info {
  flex: 1;
  min-width: 0;
}

.conversation-title {
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-time {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
}

/* 推荐场景 */
.scenario-list {
  display: flex;
  flex-direction: column;
}

.scenario-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 0;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  transition: background 0.2s;
}

.scenario-item:last-child {
  border-bottom: none;
}

.scenario-item:hover {
  background: #fafafa;
}

.scenario-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.scenario-info {
  flex: 1;
  min-width: 0;
}

.scenario-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.scenario-desc {
  font-size: 12px;
  color: #999;
}

.scenario-arrow {
  color: #ccc;
  font-size: 12px;
}

/* 温馨提示 */
.notice-bar {
  background: #fffbe6;
  border: 1px solid #ffe58f;
  border-radius: 8px;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #8c6e00;
}

.notice-icon {
  color: #faad14;
  font-size: 16px;
  flex-shrink: 0;
}

.notice-close {
  margin-left: auto;
  color: #999;
  cursor: pointer;
  font-size: 12px;
}

.notice-close:hover {
  color: #333;
}

/* 使用指南 */
.guide-content { display: flex; flex-direction: column; gap: 16px; }
.guide-item { padding: 12px 16px; background: #fafafa; border-radius: 8px; }
.guide-title { font-size: 14px; font-weight: 600; color: #333; margin-bottom: 4px; }
.guide-desc { font-size: 13px; color: #666; line-height: 1.7; }

/* 响应式 */
@media (max-width: 1200px) {
  .tools-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .page-container {
    padding: 16px;
  }

  .welcome-banner {
    padding: 24px;
    flex-direction: column;
    text-align: center;
  }

  .welcome-content h2 {
    font-size: 22px;
  }

  .welcome-actions {
    justify-content: center;
  }

  .welcome-illustration {
    margin-top: 24px;
  }

  .tools-grid {
    grid-template-columns: 1fr;
  }

  .bottom-row {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .section-card {
    padding: 16px;
    margin-bottom: 16px;
  }

  .section-title {
    font-size: 16px;
    margin-bottom: 16px;
  }
}

@media (max-width: 576px) {
  .welcome-content h2 {
    font-size: 20px;
  }

  .welcome-content p {
    font-size: 13px;
  }

  .robot-body {
    width: 80px;
    height: 70px;
  }

  .robot-eye {
    width: 16px;
    height: 16px;
  }

  .robot-eye::after {
    width: 7px;
    height: 7px;
  }

  .robot-platform {
    width: 100px;
    height: 14px;
  }

  .notice-bar {
    font-size: 12px;
    padding: 10px 12px;
  }
}
</style>
