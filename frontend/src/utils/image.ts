export const API_BASE = 'http://localhost:8080'

/** 把后端返回的 /uploads/xxx 转成浏览器可访问的完整地址 */
export function resolveImageUrl(path?: string | null): string {
  if (!path) return ''
  if (
    path.startsWith('http://') ||
    path.startsWith('https://') ||
    path.startsWith('blob:') ||
    path.startsWith('data:')
  ) {
    return path
  }
  if (path.startsWith('/')) {
    return `${API_BASE}${path}`
  }
  return `${API_BASE}/${path}`
}
