package com.example.tomatomall.vo;

import com.example.tomatomall.po.Carts;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class CartItemVO {
    private int cartItemId;

    private Integer productId;

    private String title;

    private BigDecimal price;

    private String description;

    private String cover;

    private String detail;

    private Integer quantity;
}


