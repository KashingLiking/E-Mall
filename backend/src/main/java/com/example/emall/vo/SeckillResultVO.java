package com.example.emall.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeckillResultVO {

    private boolean success;
    private String message;
    private Integer orderId;
}
