// src/api/order.ts
import { axios } from "@/utils/request"; // 你自己的 axios 封装路径，根据项目实际调整

// 1️⃣ 提交订单
export const createOrder = (data: {
    cartItemIds: string[];
    shippingAddress: {
        name: string;
        phone: string;
        zipCode: string;
        detail: string;
    };
    paymentMethod: string;
}) => {
    return axios.post("/api/cart/checkout", data);
};

// 2️⃣ 发起支付
export const payOrder = (orderId: number) => {
    return axios.post<ResponseVO<AliPayVO>>(`/api/orders/${orderId}/pay`);
};

interface ResponseVO<T> {
    code: string;
    message: string;
    data: T;
}

interface AliPayVO {
    paymentForm: string;
    orderId: string;
    totalAmount: number;
    paymentMethod: string;
}

// 获取订单列表
export const getOrderList = (params?: { page?: number; size?: number }) => {
    return axios.get("/api/orders", { params });
};

// 申请退款
export const applyRefund = (orderId: number, data: { reason: string }) => {
    return axios.post(`/api/orders/${orderId}/refund/apply`, data);
};
// 管理员审批退款
export const approveRefund = (orderId: number) => {
    return axios.post(`/api/orders/${orderId}/refund/approve`);
};
// 管理员拒绝退款
export const rejectRefund = (orderId: number, data: { reason: string }) => {
    return axios.post(`/api/orders/${orderId}/refund/reject`, data);
};

// 修改订单信息
export const updateOrder = (orderId: number, data: any) => {
    return axios.put(`/api/orders/${orderId}`, data);
};
