// 头像地址处理：统一使用相对路径 /files/static/... 或 /images/...，
// 由 Nginx(/files/、/images/) 与 vite 代理(/files、/images)统一转发到后端，
// 无需再拼接 /api 前缀；MinIO 等外部存储返回完整 URL，直接使用。
export const resolveAvatarUrl = (avatar?: string | null): string => {
  if (!avatar) return ''
  return avatar
}
