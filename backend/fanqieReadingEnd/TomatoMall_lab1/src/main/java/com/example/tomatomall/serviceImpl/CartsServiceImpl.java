package com.example.tomatomall.serviceImpl;

import com.example.tomatomall.exception.TomatomallException;
import com.example.tomatomall.po.*;
import com.example.tomatomall.repository.*;
import com.example.tomatomall.service.CartsService;
import com.example.tomatomall.util.SecurityUtil;
import com.example.tomatomall.vo.*;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
                throw TomatomallException.productsNotEnough();
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
            throw TomatomallException.cartProductNotExits();
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
            throw TomatomallException.cartProductNotExits();
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
        OrdersVO ordersVO = new OrdersVO();
        ordersVO.setUserName(account.getName());
        ordersVO.setTotalAmount(totalPrice(checkoutRequestVO.getCartItemIds()));
        if (Objects.equals(ordersVO.getTotalAmount(), BigDecimal.ZERO)){
            return null;
        }//2. 系统获取用户购物车商品，验证商品库存，计算订单总金额，创建订单，并返回订单信息
        ordersVO.setPaymentMethod(checkoutRequestVO.getPaymentMethod());
        ordersVO.setCreateTime(LocalDateTime.now());
        ordersVO.setStatus("PENDING");
        Orders orders = ordersVO.toPO();
        orders.setAccount(account);
        Orders order_front = ordersRepository.save(orders);
        for(String cartItemId : checkoutRequestVO.getCartItemIds()){
            Optional<Carts> carts = cartsRepository.findById(Integer.valueOf(cartItemId));
            Carts cartItem = carts.get();
            CartsOrdersRelation relation = new CartsOrdersRelation();
            relation.setCartItem(cartItem);
            relation.setOrders(orders);
            cartsOrdersRelationRepository.save(relation);
        }
        return order_front.toVO();
    }

    public BigDecimal totalPrice(List<String> cartItemIds) {
        BigDecimal total = BigDecimal.ZERO;

        for (String itemId : cartItemIds) {
            Carts carts = cartsRepository.findById(Integer.parseInt(itemId)).get();
            BigDecimal price = carts.getProduct().getPrice();    // 获取商品单价
            int quantity = carts.getQuantity();        // 获取商品数量
            if(quantity>carts.getProduct().getStockpile().getAmount()){
                total = BigDecimal.ZERO;
                break;
            }
            BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(quantity));
            total = total.add(itemTotal);
        }
        return total;
    }
}