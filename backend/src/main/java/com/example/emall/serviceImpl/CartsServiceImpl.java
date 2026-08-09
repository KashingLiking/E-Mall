package com.example.emall.serviceImpl;

import com.example.emall.exception.EMallException;
import com.example.emall.po.*;
import com.example.emall.repository.*;
import com.example.emall.service.CartsService;
import com.example.emall.util.SecurityUtil;
import com.example.emall.vo.*;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Service
public class CartsServiceImpl implements CartsService {

    @Autowired
    private CartsRepository cartsRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartsOrdersRelationRepository cartsOrdersRelationRepository;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private OrdersRepository ordersRepository;

    @Override
    @Transactional
    public CartItemVO addCartProduct(Integer productId , Integer quantity) {
        Carts carts = new Carts();
        Optional<Product> product = productRepository.findById(productId);
        if(product.isPresent()){
            carts.setQuantity(quantity);
            carts.setAccount(securityUtil.getCurrentAccount());
            carts.setProduct(product.get());
            Carts retCarts = cartsRepository.save(carts);
            if(carts.getQuantity()>product.get().getStockpile().getAmount()){
                throw EMallException.productsNotEnough();
            }
            return retCarts.toItemVO();
        }
        return null;
    }

    @Override
    @Transactional
    public String deleteCartProduct(Integer cartItemId) {
        try {
            cartsRepository.deleteById(cartItemId);
            return "删除成功";
        }catch (Exception e) {
            throw EMallException.cartProductNotExits();
        }
    }


    @Override
    @Transactional
    public String updateCartItemQuantity(Integer cartItemId, Integer quantity) {
        Optional<Carts> cartsOptional = cartsRepository.findById(cartItemId);
        if (cartsOptional.isPresent()) {
            Carts carts = cartsOptional.get();
            carts.setQuantity(quantity);
            return "修改数量成功";
        }else{
            throw EMallException.cartProductNotExits();
        }
    }

    @Override
    public CartItemsVO getCartItems() {
        List<Carts> cartsList = cartsRepository.findCartItemsByUserId(securityUtil.getCurrentAccount().getUserid());
        List<CartItemVO> cartItemVOList = convertToCartItemVOList(cartsList);
        CartItemsVO cartItemsVO = new CartItemsVO();
        cartItemsVO.setCartItemVOList(cartItemVOList);
        cartItemsVO.calculateTotalAmount(cartItemVOList);
        cartItemsVO.calculateTotalQuantity(cartItemVOList);
        return cartItemsVO;
    }

    public List<CartItemVO> convertToCartItemVOList(List<Carts> cartsList) {
        if (cartsList == null) {
            return Collections.emptyList();
        }

        return cartsList.stream()
                .map(Carts::toItemVO)  // 使用方法引用调用toItemVO()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrdersVO createOrder(CheckoutRequestVO checkoutRequestVO) {
        Account account = securityUtil.getCurrentAccount();
        if (checkoutRequestVO.getCartItemIds() == null || checkoutRequestVO.getCartItemIds().isEmpty()) {
            throw EMallException.cartProductNotExits();
        }
        OrdersVO ordersVO = new OrdersVO();
        ordersVO.setUserName(account.getName());
        ordersVO.setTotalAmount(totalPrice(checkoutRequestVO.getCartItemIds()));
        ordersVO.setPaymentMethod(checkoutRequestVO.getPaymentMethod());
        ordersVO.setCreateTime(LocalDateTime.now());
        ordersVO.setStatus("PENDING");
        Orders orders = ordersVO.toPO();
        orders.setAccount(account);
        Orders order_front = ordersRepository.save(orders);
        for(String cartItemId : checkoutRequestVO.getCartItemIds()){
            Optional<Carts> carts = cartsRepository.findByIdWithProductAndStockpile(Integer.valueOf(cartItemId));
            if (!carts.isPresent()) {
                throw EMallException.cartProductNotExits();
            }
            Carts cartItem = carts.get();
            if (cartItem.getAccount() == null || cartItem.getAccount().getUserid() != account.getUserid()) {
                throw EMallException.notLogin();
            }
            CartsOrdersRelation relation = new CartsOrdersRelation();
            relation.setCartItem(cartItem);
            relation.setOrders(order_front);
            cartsOrdersRelationRepository.save(relation);
        }
        return order_front.toVO();
    }

    public BigDecimal totalPrice(List<String> cartItemIds) {
        BigDecimal total = BigDecimal.ZERO;

        for (String itemId : cartItemIds) {
            Carts carts = cartsRepository.findByIdWithProductAndStockpile(Integer.parseInt(itemId))
                    .orElseThrow(EMallException::cartProductNotExits);
            BigDecimal price = carts.getProduct().getPrice();
            int quantity = carts.getQuantity();
            if (quantity > carts.getProduct().getStockpile().getAmount()) {
                throw EMallException.productsNotEnough();
            }
            BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(quantity));
            total = total.add(itemTotal);
        }
        return total;
    }
}