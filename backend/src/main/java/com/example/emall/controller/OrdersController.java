package com.example.emall.controller;

import com.example.emall.service.OrdersService;
import com.example.emall.vo.AliPayVO;
import com.example.emall.vo.OrderListVO;
import com.example.emall.vo.ResponseVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/orders")
public class OrdersController {

    @Resource
    private OrdersService ordersService;

    @GetMapping
    public ResponseVO<OrderListVO> getOrders(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseVO.buildSuccess(ordersService.getOrdersForCurrentUser(page, size));
    }

    @PostMapping("/{orderId}/pay")
    public ResponseVO<AliPayVO> putAfford(@PathVariable("orderId") Integer orderId) {
        return ResponseVO.buildSuccess(ordersService.putAfford(orderId));
    }
}
