package com.example.tomatomall.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CheckoutRequestVO {
    private List<String> cartItemIds;

    private ShippingAddress shippingAddress;  // 嵌套对象

    private String paymentMethod;

    private static class ShippingAddress{
        private String name;        // 收货人姓名

        private String phone;       // 电话

        private String postalCode;  // 邮编

        private String address;     // 详细地址
    }
}

