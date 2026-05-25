import {axios} from '../utils/request'
import {ADVERTISEMENT_MODULE} from './_prefix'

type UpdateInfo = {
    id: number
    title: string,
    content: string,
    imgUrl: string,
    productId: string,
}

type CreateAdvertisement = {
    title: string,
    content: string,
    imgUrl: string,
    productId: string,
}

//获取所有广告信息
export const getAdvertisement = () =>{
    return axios.get(`${ADVERTISEMENT_MODULE}`)
        .then(res => {
            return res
        })
}

//更新广告信息
export const updateAdvertisement = (updateInfo : UpdateInfo) =>{
    return axios.put(`${ADVERTISEMENT_MODULE}`, updateInfo, {headers: {'Content-Type': 'application/json'}})
        .then(res => {
            return res
        })
}

//创建广告
export const addAdvertisement = (advertisementInfo : CreateAdvertisement) =>{
    return axios.post(`${ADVERTISEMENT_MODULE}`, advertisementInfo, {headers:{'Content-Type': 'application/json'}})
        .then(res => {
            return res
        })
}

//删除广告
export const deleteAdvertisement = (id : number) =>{
    return axios.delete(`${ADVERTISEMENT_MODULE}/${id}`)
        .then(res => {
            return res
        })
}