import { axios } from '@/utils/request'

/** 上传图片，返回形如 /uploads/xxx.jpg 的相对路径 */
export const uploadImage = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return axios.post('/api/upload', formData, {
    timeout: 30000,
    headers: {
      'token': sessionStorage.getItem('token'),
    }
  })
}
