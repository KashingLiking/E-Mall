package com.example.emall.vo;

import com.example.emall.po.Orders;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class OrdersVO {

    private Integer orderId;

    private String userName;

    private BigDecimal totalAmount;

    private String paymentMethod;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private String status;

    public Orders toPO(){
        Orders orders=new Orders();
        orders.setId(this.orderId);
        orders.setTotalAmount(this.totalAmount);
        orders.setPaymentMethod(this.paymentMethod);
        orders.setStatus(this.status);
        orders.setCreateTime(this.createTime);
        return orders;
    }
}
