/**
 * URL 工具 —— 商城端统一资源地址解析
 *
 * 后端本地存储返回的 fileUrl 是相对路径（如 /files/static/xxx.jpg），
 * 在部分 WebView / 跨设备访问场景下，相对路径会基于错误的 base URL 解析导致加载失败。
 * 这里统一将相对路径补全为绝对地址（基于当前 origin）。
 */

/** 将后端返回的相对资源路径解析为可直接加载的绝对 URL */
export function resolveFileUrl(url?: string | null): string {
  if (!url) return ''
  // 已是完整 URL（http/https/data/blob）直接返回
  if (/^(https?:|data:|blob:)/i.test(url)) return url
  // 相对路径：补全当前 origin（兼容 /files/static、/images、/api 前缀）
  if (url.startsWith('/')) {
    return window.location.origin + url
  }
  return url
}

/** 头像 URL 解析（带默认头像兜底，避免空字符串导致 van-image 无占位） */
export function resolveAvatar(url?: string | null): string {
  return resolveFileUrl(url) || ''
}
