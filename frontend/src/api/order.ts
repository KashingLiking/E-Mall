import { axios } from "@/utils/request";

export const createOrder = (data: {
    cartItemIds: string[];
    shippingAddress: {
        name: string;
        phone: string;
        detail: string;
    };
    paymentMethod: string;
}) => {
    return axios.post("/api/cart/checkout", data);
};

export const payOrder = (orderId: number) => {
    return axios.post(`/api/orders/${orderId}/pay`);
};

export const getOrderList = (params?: { page?: number; size?: number }) => {
    return axios.get("/api/orders", { params });
};
