package com.example.emall.vo;

import com.example.emall.po.Stockpile;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StockpileVO {
    private Integer id;

    private Integer product_id;

    private Integer amount;

    private Integer frozen;

    public Stockpile toPO(){
        Stockpile stockpile=new Stockpile();
        stockpile.setAmount(this.amount);
        stockpile.setFrozen(this.frozen);
        return stockpile;
    }
}