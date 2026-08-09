package com.example.emall.serviceImpl;

import com.example.emall.exception.EMallException;
import com.example.emall.po.Account;
import com.example.emall.po.Carts;
import com.example.emall.po.CartsOrdersRelation;
import com.example.emall.po.Orders;
import com.example.emall.po.Product;
import com.example.emall.repository.CartsOrdersRelationRepository;
import com.example.emall.repository.CartsRepository;
import com.example.emall.repository.OrdersRepository;
import com.example.emall.repository.ProductRepository;
import com.example.emall.service.AccountService;
import com.example.emall.service.OrdersService;
import com.example.emall.util.SecurityUtil;
import com.example.emall.vo.AliPayVO;
import com.example.emall.vo.OrderListVO;
import com.example.emall.vo.OrdersVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrdersServiceImpl implements OrdersService {

    @Autowired
    private OrdersRepository ordersRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CartsRepository cartsRepository;
    @Autowired
    private CartsOrdersRelationRepository cartsOrdersRelationRepository;
    @Autowired
    private AccountService accountService;
    @Autowired
    private SecurityUtil securityUtil;

    @Override
    @Transactional
    public AliPayVO putAfford(Integer orderId) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> EMallException.orderNotExits());

        Account currentAccount = securityUtil.getCurrentAccount();
        if (currentAccount == null) {
            throw EMallException.notLogin();
        }
        if (order.getAccount().getUserid() != currentAccount.getUserid()) {
            throw EMallException.notLogin();
        }

        if ("PAID".equals(order.getStatus())) {
            log.warn("订单已支付，跳过处理: {}", orderId);
        } else {
            order.setStatus("PAID");
            order.setPaymentMethod("MOCK");
            ordersRepository.save(order);
            deductInventory(orderId);
            removePurchasedCartItems(orderId);
            accountService.addToTotalSpent(order.getAccount().getUserid(), order.getTotalAmount());
        }

        AliPayVO aliPayVO = new AliPayVO();
        aliPayVO.setPaymentForm("模拟支付成功");
        aliPayVO.setOrderId(String.valueOf(order.getId()));
        aliPayVO.setTotalAmount(order.getTotalAmount());
        aliPayVO.setPaymentMethod("MOCK");
        return aliPayVO;
    }

    @Override
    public OrderListVO getOrdersForCurrentUser(Integer page, Integer size) {
        Account currentAccount = securityUtil.getCurrentAccount();
        if (currentAccount == null) {
            throw EMallException.notLogin();
        }

        int pageNum = page != null && page > 0 ? page - 1 : 0;
        int pageSize = size != null && size > 0 ? size : 10;
        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));

        Page<Orders> orderPage = ordersRepository.findByAccount(currentAccount, pageable);
        List<OrdersVO> list = orderPage.getContent().stream()
                .map(Orders::toVO)
                .collect(Collectors.toList());

        OrderListVO result = new OrderListVO();
        result.setList(list);
        result.setTotal(orderPage.getTotalElements());
        return result;
    }

    private void deductInventory(Integer orderId) {
        Orders orders = ordersRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> EMallException.orderNotExits());
        List<CartsOrdersRelation> relations = orders.getCartsOrdersRelation();
        for (CartsOrdersRelation relation : relations) {
            Carts cartItem = relation.getCartItem();
            if (cartItem == null) continue;
            Product product = cartItem.getProduct();
            int currentStock = product.getStockpile().getAmount();
            int requiredQuantity = cartItem.getQuantity();
            if (currentStock < requiredQuantity) {
                throw EMallException.productsNotEnough();
            }
            product.getStockpile().setAmount(currentStock - requiredQuantity);
            product.setSoldQuantity((product.getSoldQuantity() != null ? product.getSoldQuantity() : 0) + requiredQuantity);
            productRepository.save(product);
        }
    }

    private void removePurchasedCartItems(Integer orderId) {
        List<Integer> cartIds = cartsOrdersRelationRepository.findCartIdsByOrderId(orderId);
        cartsOrdersRelationRepository.deleteByOrderId(orderId);
        if (!cartIds.isEmpty()) {
            cartsRepository.deleteByIds(cartIds);
        }
    }
}
