package com.example.emall.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CheckoutRequestVO {
    private List<String> cartItemIds;

    private ShippingAddress shippingAddress;

    private String paymentMethod;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ShippingAddress {
        private String name;
        private String phone;
        private String address;
    }
}

