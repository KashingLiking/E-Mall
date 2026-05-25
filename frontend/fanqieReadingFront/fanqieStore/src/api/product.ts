import {axios} from '../utils/request'
import {PRODUCT_MODULE} from './_prefix'

type Specification = {
    item: string,
    value: string,
};

type ProductInfo = {
    title: string,
    price: number,
    rate: number,
    description: string,
    cover: string,
    detail: string,
    bookType: string,
    specifications: Specification[];
}

// 更新信息
type Specifications = {
    id: number | null,
    item: string,
    value: string,
    product_id: number | null,
};
type UpdateInfo = {
    id: number,
    title: string,
    price: number,
    rate: number,
    description: string,
    cover: string,
    detail: string,
    bookType: string,
    specifications: Specifications[],
    recommendCount: number,
    stockpile: Stockpile,
}

// 库存信息
type Stockpile = {
    id: number | null,
    product_id: number | null,
    amount: number,
    frozen: number,
}

//获取商品列表
export const getProductInfo = () => {
    return axios.get(`${PRODUCT_MODULE}`)
        .then(res => {
            return res
        })
}

//获取指定商品信息
export const getSpecificProduct = (id : number) => {
    return axios.get(`${PRODUCT_MODULE}/${id}`)
        .then(res => {
            return res.data
        })
}

//更新商品信息
export const updateProduct = (updateInfo : UpdateInfo) => {
    return axios.put(`${PRODUCT_MODULE}`, updateInfo, {headers: {'Content-Type': 'application/json'}})
        .then(res => {
            return res
        })
}

//增加商品
export const addProduct = (productInfo : ProductInfo) => {
    return axios.post(`${PRODUCT_MODULE}`, productInfo, {headers:{'Content-Type': 'application/json'}})
        .then(res => {
            return res
        })
}

//删除商品
export const deleteProduct = (id: number) => {
    return axios.delete(`${PRODUCT_MODULE}/${id}`)
        .then(res => {
            return res
        })
}

//调整指定商品库存
export const adjustStockpile = (productId : number, amount : number) => {
    return axios.patch(`${PRODUCT_MODULE}/stockpile/${productId}`, {
        amount: amount,
    })
        .then(res => {
            return res
        })
}

//查询指定商品库存
export const getStockpile = (productId: number) => {
    return axios.get(`${PRODUCT_MODULE}/stockpile/${productId}`)
        .then(res => {
            return res
        })
}