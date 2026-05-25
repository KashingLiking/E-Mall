package com.example.tomatomall.serviceImpl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.example.tomatomall.exception.TomatomallException;
import com.example.tomatomall.po.Carts;
import com.example.tomatomall.po.CartsOrdersRelation;
import com.example.tomatomall.po.Orders;
import com.example.tomatomall.po.Product;
import com.example.tomatomall.repository.CartsRepository;
import com.example.tomatomall.repository.OrdersRepository;
import com.example.tomatomall.repository.ProductRepository;
import com.example.tomatomall.service.AccountService;
import com.example.tomatomall.service.OrdersService;
import com.example.tomatomall.util.SecurityUtil;
import com.example.tomatomall.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class OrdersServiceImpl implements OrdersService {

    @Autowired
    private OrdersRepository ordersRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private AccountService accountService;

    @Autowired
    private SecurityUtil securityUtil;
    @Value("${alipay.app-id}")
    private String appId;
    @Value("${alipay.private-key}")
    private String privateKey;
    @Value("${alipay.alipay-public-key}")
    private String alipayPublicKey;
    @Value("${alipay.server-url}")
    private String serverUrl;
    @Value("${alipay.charset}")
    private String charset;
    @Value("${alipay.sign-type}")
    private String signType;
    @Value("${alipay.notify-url}")
    private String notifyUrl;
    @Value("${alipay.return-url}")
    private String returnUrl;
    private static final String FORMAT = "JSON";
    @Override
    @Transactional
    public AliPayVO putAfford(Integer orderId) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        String paymentForm = generateAlipayForm(order);

        order.setStatus("WAITING_PAYMENT");
        ordersRepository.save(order);
        AliPayVO aliPayVO = new AliPayVO();
        aliPayVO.setPaymentForm(paymentForm);
        aliPayVO.setOrderId(String.valueOf(order.getId()));
        aliPayVO.setTotalAmount(order.getTotalAmount());
        aliPayVO.setPaymentMethod("Alipay");

        return aliPayVO;
    }

    @Override
    @Transactional
    public void handlePaymentSuccess(String orderId, String alipayTradeNo, BigDecimal amount) {
        // 查询订单
        Orders order = ordersRepository.findById(Integer.parseInt(orderId))
                .orElseThrow(() -> new RuntimeException("订单不存在: " + orderId));
        if ("PAID".equals(order.getStatus())) {
            log.warn("订单已支付，跳过处理: {}", orderId);
            return;
        }
        if (order.getTotalAmount().compareTo(amount) != 0) {
            throw new RuntimeException("支付金额与订单金额不符");
        }
        order.setStatus("PAID");
        order.setId(Integer.valueOf(orderId));
        ordersRepository.save(order);

        deductInventory(order.getId());
        accountService.addToTotalSpent(order.getAccount().getUserid(), order.getTotalAmount());
    }

    private void deductInventory(Integer orderId) {
        Optional<Orders> order = ordersRepository.findById(orderId);
        if(!order.isPresent()){
            throw TomatomallException.orderNotExits();
        }
        Orders orders = order.get();
        List<CartsOrdersRelation> relations = orders.getCartsOrdersRelation();
        for (CartsOrdersRelation relation : relations ) {
            Carts cartItem = relation.getCartItem();
            Product product = cartItem.getProduct();
            int currentStock = product.getStockpile().getAmount();
            int requiredQuantity = cartItem.getQuantity();
            if (currentStock < requiredQuantity) {
                throw TomatomallException.productsNotEnough();
            }
            product.getStockpile().setAmount(currentStock - requiredQuantity);
            product.setSoldQuantity((product.getSoldQuantity() != null ? product.getSoldQuantity() : 0) + requiredQuantity);
            productRepository.save(product);
        }
    }

    private String generateAlipayForm(Orders order) {
        try {
            AlipayClient client = new DefaultAlipayClient(
                    serverUrl, appId, privateKey, "JSON", charset, alipayPublicKey, signType);

            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setNotifyUrl(notifyUrl);
            request.setReturnUrl(returnUrl);

            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", order.getId());
            bizContent.put("total_amount", order.getTotalAmount());
            bizContent.put("subject", "订单支付-" + order.getId());
            bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
            request.setBizContent(bizContent.toString());

            return client.pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            throw new RuntimeException("支付宝支付创建失败", e);
        }
    }
}