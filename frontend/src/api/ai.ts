import request from '@/utils/request'
import type { AiTool } from '@/types/ai'

export interface AiResultItem {
  id: number
  businessType: string
  relatedId: number
  outputText?: string
  outputJson?: string
  status: string
  modelName?: string
  createdAt?: string
}

export interface ChatReply {
  reply: string
  modelName?: string
  tokenUsage?: string
}

export interface AiChatHistoryItem {
  id: number
  question: string
  answer: string
  modelName?: string
  createdAt?: string
}

export const aiApi = {
  getTools: (params?: any): Promise<AiTool[]> => request.get('/ai/tools', { params }),
  getAdvice: (type: string, id: number): Promise<Record<string, any>> => request.get(`/ai/advice/${type}/${id}`),
  getResults: (params?: { businessType?: string; relatedId?: number }): Promise<AiResultItem[]> =>
    request.get('/ai/results', { params }),
  chat: (question: string): Promise<ChatReply> => request.post('/ai/chat', { question }),
  getChatHistory: (): Promise<AiChatHistoryItem[]> => request.get('/ai/chat-history'),
  clearChatHistory: (): Promise<void> => request.delete('/ai/chat-history'),
  getPromptTemplates: (params?: any): Promise<any[]> => request.get('/ai/prompt-templates', { params }),
  generateDoc: (data: { templateId?: number; content: string }): Promise<{ content: string; modelName?: string; tokenUsage?: string }> =>
    request.post('/ai/doc/generate', data),
  scoreTask: (taskId: number): Promise<Record<string, any>> => request.post(`/ai/score/task/${taskId}`),
  convertToTask: (aiResultId: number, storeId: number, assigneeId: number): Promise<any> =>
    request.post(`/ai/result/${aiResultId}/convert`, null, { params: { storeId, assigneeId } }),
}
