import { message } from 'ant-design-vue'

/**
 * 下载文件的通用方法
 * @param url 下载地址
 * @param filename 文件名
 */
export const downloadFile = (url: string, filename?: string) => {
  // 创建隐藏的 a 标签触发下载
  const a = document.createElement('a')
  a.href = url
  a.download = filename || ''
  a.style.display = 'none'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
}

/**
 * 通用导出提示（暂无导出端点的模块使用）
 * @param moduleName 模块名称，如"客户"、"库存"
 */
export const exportComingSoon = (moduleName?: string) => {
  const text = moduleName ? `${moduleName}导出功能` : '导出功能'
  message.info(`${text}即将上线，敬请期待`)
}

/**
 * 导出报表分数数据
 * @param month 月份，格式 YYYY-MM
 */
export const exportReportScores = async (month: string) => {
  try {
    // 构建导出URL，直接在新窗口打开下载
    const exportUrl = `/api/reports/scores/export?month=${month}`
    window.open(exportUrl, '_blank')
    message.success('导出任务已提交，请稍后查看下载')
  } catch (error) {
    message.error('导出失败，请重试')
    console.error('导出报表分数失败:', error)
  }
}

/**
 * 通过 Blob 下载文件（适用于需要认证的接口）
 * @param url 接口地址
 * @param filename 文件名
 */
export const downloadViaBlob = async (url: string, filename: string) => {
  try {
    const response = await fetch(url, {
      credentials: 'include', // 携带 HttpOnly Cookie
    })

    if (!response.ok) {
      throw new Error(`下载失败: ${response.statusText}`)
    }

    const blob = await response.blob()
    const blobUrl = window.URL.createObjectURL(blob)
    downloadFile(blobUrl, filename)
    window.URL.revokeObjectURL(blobUrl)
    message.success('导出成功')
  } catch (error) {
    message.error('导出失败，请重试')
    console.error('下载文件失败:', error)
  }
}
