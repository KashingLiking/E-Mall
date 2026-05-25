package com.example.tomatomall.controller;
import com.alipay.api.internal.util.AlipaySignature;
import com.example.tomatomall.service.OrdersService;
import com.example.tomatomall.vo.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrdersController {

    @Resource
    private OrdersService ordersService;

    @Value("${alipay.alipay-public-key}")
    private String alipayPublicKey;
    @Value("${alipay.charset}")
    private String charset;
    @Value("${alipay.sign-type}")
    private String signType;

    @PostMapping("/{orderId}/pay")
    public ResponseVO<AliPayVO> putAfford(
            @PathVariable("orderId") Integer orderId) {
        return ResponseVO.buildSuccess(ordersService.putAfford(orderId));
    }

    @PostMapping("/notify")
    public String handleAlipayNotify(HttpServletRequest request) {
        try {
            // 1. 转换请求参数
            Map<String, String> params = request.getParameterMap().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()[0]));
            // 2. 验证签名
            boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayPublicKey, charset, signType);
            if (!signVerified) {
                throw new RuntimeException("支付宝回调签名验证失败");
            }

            // 3. 处理支付成功逻辑
            if ("TRADE_SUCCESS".equals(params.get("trade_status"))) {
                ordersService.handlePaymentSuccess(params.get("out_trade_no"), params.get("trade_no"), new BigDecimal(params.get("total_amount")) // 金额
                );
            }
            return "success";
        } catch (Exception e) {
            return "fail";
        }
    }
}