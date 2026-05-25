import {axios} from '../utils/request'
import {CART_MODULE} from './_prefix'

type CartInfo = {
    productId: number,
    title: string,
    price: number,
    description: string,
    cover: string,
    detail: string,
    quantity: number,
}

// 获取购物车商品列表
export const getCart = () => {
    return axios.get(`${CART_MODULE}`)
        .then(res => {
            console.log("get cart:",res);
            return res;
        })
}

// 添加商品到购物车
export const addBookToCart = (productId : number, quantity : number) => {
    return axios.post(`${CART_MODULE}`, { productId, quantity },)
        .then(res => {
            console.log("添加商品成功：", res);
            return res;
        })
        .catch(error => {
            console.error('添加书籍到购物车失败:', error);
            throw error; // 可以选择抛出错误，或者执行其他错误处理逻辑
        });
}

// 删除购物车商品
export const deleteCart = (cartItemId: number) => {
    return axios.delete(`${CART_MODULE}/${cartItemId}`).then(res => {
        return res
    })
}

// 修改购物车商品数量
export const getProduct = (cartItemId: number, quantity: number) => {
    return axios.patch(`${CART_MODULE}/${cartItemId}`, {
        quantity: quantity,
    }).then(res => {
        return res
    })
}