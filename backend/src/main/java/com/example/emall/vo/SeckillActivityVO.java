package com.example.emall.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class SeckillActivityVO {

    private Integer id;
    private Integer productId;
    private String productTitle;
    private String productCover;
    private BigDecimal originalPrice;
    private BigDecimal seckillPrice;
    private Integer seckillStock;
    private Integer remainingStock;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
}
