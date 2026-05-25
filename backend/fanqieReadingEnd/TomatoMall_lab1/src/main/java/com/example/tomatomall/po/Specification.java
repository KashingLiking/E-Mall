package com.example.tomatomall.po;

import com.example.tomatomall.vo.SpecificationVO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "specifications")
public class Specification{

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Integer id;

    @Basic
    @Column(name="item",nullable = false)
    private String item;

    @Basic
    @Column(name = "value", nullable = false)
    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    public SpecificationVO toVO(){
        SpecificationVO specificationVO=new SpecificationVO();
        specificationVO.setItem(this.item);
        specificationVO.setValue(this.value);
        return specificationVO;
    }
}