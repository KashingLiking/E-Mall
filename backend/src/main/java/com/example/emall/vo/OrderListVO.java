package com.example.emall.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OrderListVO {
    private List<OrdersVO> list;
    private long total;
}
