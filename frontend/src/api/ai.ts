import request from '@/utils/request'

export const aiApi = {
  getTools: (params?: any): Promise<any> => request.get('/ai/tools', { params }),
  getAdvice: (type: string, id: string): Promise<any> => request.get(`/ai/advice/${type}/${id}`),
  getResults: (params?: any): Promise<any> => request.get('/ai/results', { params }),
  getPromptTemplates: (params?: any): Promise<any> => request.get('/ai/prompt-templates', { params }),
}
