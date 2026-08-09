import router from '@/router'

export function logout() {
  sessionStorage.removeItem('token')
  sessionStorage.removeItem('username')
  sessionStorage.removeItem('userId')
  sessionStorage.removeItem('userRole')
  sessionStorage.removeItem('userAvatar')
  router.push('/login')
}
