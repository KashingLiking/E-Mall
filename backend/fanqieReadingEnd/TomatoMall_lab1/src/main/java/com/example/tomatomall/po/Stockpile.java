package com.example.tomatomall.po;

import com.example.tomatomall.vo.StockpileVO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "stockpiles")
public class Stockpile{

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Integer id;

    @Basic
    @Column(name="amount",nullable = false)
    private Integer amount;

    @Basic
    @Column(name = "frozen", nullable = false)
    private Integer frozen;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id",  nullable = false, unique = true)
    private Product product;

    public StockpileVO toVO(){
        StockpileVO stockpileVO=new StockpileVO();
        stockpileVO.setAmount(this.amount);
        stockpileVO.setFrozen(this.frozen);
        return stockpileVO;
    }
}