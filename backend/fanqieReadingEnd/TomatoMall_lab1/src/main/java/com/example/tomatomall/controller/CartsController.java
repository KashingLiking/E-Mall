package com.example.tomatomall.controller;

import com.example.tomatomall.service.CartsService;
import com.example.tomatomall.vo.*;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartsController {

    @Resource
    private CartsService cartsService;

    /**
     * 加入商品到购物车
     */
    @PostMapping
    public ResponseVO<CartItemVO> addCartProduct(@RequestBody Map<String, Integer> request) {
        Integer productId = request.get("productId");
        Integer quantity = request.get("quantity");
        return ResponseVO.buildSuccess(cartsService.addCartProduct(productId, quantity));
    }

    /**
     * 删除购物车商品
     */
    @DeleteMapping("/{cartItemId}")
    public ResponseVO<String> deleteCartProduct(@PathVariable(value = "cartItemId") Integer cartItemId) {
        return ResponseVO.buildSuccess(cartsService.deleteCartProduct(cartItemId));
    }

    /**
     * 修改购物车商品数量
     */
    @PatchMapping("/{cartItemId}")
    public ResponseVO<String> updateCartItemQuantity(
            @PathVariable("cartItemId") Integer cartItemId,
            @RequestBody Map<String, Integer> request) {
        Integer quantity = request.get("quantity");
        return ResponseVO.buildSuccess(cartsService.updateCartItemQuantity(cartItemId,quantity));
    }

    /**
     * 获取购物车商品列表
     */
    @GetMapping
    public ResponseVO<CartItemsVO> getCartItems() {
        return ResponseVO.buildSuccess(cartsService.getCartItems());
    }

    /**
     * 提交订单
     */
    @PostMapping("/checkout")
    public ResponseVO<OrdersVO> createOrder(@RequestBody CheckoutRequestVO checkoutRequestVO) {
        return ResponseVO.buildSuccess(cartsService.createOrder(checkoutRequestVO));
    }
}