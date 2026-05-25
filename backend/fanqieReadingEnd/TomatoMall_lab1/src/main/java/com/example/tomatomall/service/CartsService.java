package com.example.tomatomall.service;

import com.example.tomatomall.vo.CartItemVO;
import com.example.tomatomall.vo.CartItemsVO;
import com.example.tomatomall.vo.CheckoutRequestVO;
import com.example.tomatomall.vo.OrdersVO;

import java.util.List;

public interface CartsService {
    CartItemVO addCartProduct(Integer productId , Integer quantity);

    String deleteCartProduct(Integer id);

    String updateCartItemQuantity(Integer cartItemId,Integer quantity);;

    CartItemsVO getCartItems();

    OrdersVO createOrder(CheckoutRequestVO checkoutRequestVO);
}