import { axios } from '@/utils/request'
import { SECKILL_MODULE } from './_prefix'

export interface SeckillActivityVO {
  id?: number
  productId?: number
  productTitle?: string
  productCover?: string
  originalPrice?: number
  seckillPrice: number
  seckillStock: number
  remainingStock?: number
  startTime: string
  endTime: string
  status?: string
}

export const getSeckillActivities = () => {
  return axios.get(SECKILL_MODULE)
}

export const createSeckillActivity = (data: SeckillActivityVO) => {
  return axios.post(SECKILL_MODULE, data)
}

export const updateSeckillActivity = (id: number, data: Partial<SeckillActivityVO>) => {
  return axios.put(`${SECKILL_MODULE}/${id}`, data)
}

export const deleteSeckillActivity = (id: number) => {
  return axios.delete(`${SECKILL_MODULE}/${id}`)
}

export const buySeckill = (activityId: number) => {
  return axios.post(`${SECKILL_MODULE}/${activityId}/buy`)
}
