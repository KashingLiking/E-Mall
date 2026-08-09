package com.example.emall.service;

import com.example.emall.vo.AliPayVO;
import com.example.emall.vo.OrderListVO;

public interface OrdersService {
    AliPayVO putAfford(Integer orderId);

    OrderListVO getOrdersForCurrentUser(Integer page, Integer size);
}
