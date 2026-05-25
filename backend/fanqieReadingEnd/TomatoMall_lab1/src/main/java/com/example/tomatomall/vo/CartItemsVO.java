package com.example.tomatomall.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CartItemsVO {
    private List<CartItemVO> cartItemVOList;

    private Integer total = 0 ;

    private BigDecimal totalAmount = new BigDecimal(0);

    public void calculateTotalQuantity(List<CartItemVO> cartItemVOList) {
        for (CartItemVO cartItemVO : cartItemVOList) {
            total += cartItemVO.getQuantity();
        }
    }

    public void calculateTotalAmount(List<CartItemVO> cartItemVOList) {
        for (CartItemVO item : cartItemVOList) {
            BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }
    }
}

