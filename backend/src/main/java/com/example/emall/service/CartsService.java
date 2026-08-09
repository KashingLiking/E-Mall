package com.example.emall.service;

import com.example.emall.vo.CartItemVO;
import com.example.emall.vo.CartItemsVO;
import com.example.emall.vo.CheckoutRequestVO;
import com.example.emall.vo.OrdersVO;

import java.util.List;

public interface CartsService {
    CartItemVO addCartProduct(Integer productId , Integer quantity);

    String deleteCartProduct(Integer id);

    String updateCartItemQuantity(Integer cartItemId,Integer quantity);;

    CartItemsVO getCartItems();

    OrdersVO createOrder(CheckoutRequestVO checkoutRequestVO);
}