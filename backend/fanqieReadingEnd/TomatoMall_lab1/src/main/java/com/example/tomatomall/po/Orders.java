package com.example.tomatomall.po;

import com.example.tomatomall.vo.OrdersVO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Orders {


    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "orderId")
    private Integer id;  // 修正了变量名大小写

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;  // 改为BigDecimal类型表示金额

    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;  // 支付方式

    @Column(name = "status", nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'PENDING'")
    private String status = "PENDING";  // 订单状态

    @Column(name = "create_time", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createTime;  // 创建时间

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private Account account;

    @OneToMany(mappedBy = "orders", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CartsOrdersRelation> cartsOrdersRelation;

    public OrdersVO toVO() {
        OrdersVO ordersVO = new OrdersVO();
        ordersVO.setOrderId(this.id);
        ordersVO.setUserName(this.account.getName());
        ordersVO.setTotalAmount(this.totalAmount);
        ordersVO.setPaymentMethod(this.paymentMethod);
        ordersVO.setStatus(this.status);
        ordersVO.setCreateTime(this.createTime);
        return ordersVO;
    }

}