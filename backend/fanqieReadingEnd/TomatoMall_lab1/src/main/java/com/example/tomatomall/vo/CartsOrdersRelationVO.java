package com.example.tomatomall.vo;

import com.example.tomatomall.po.CartsOrdersRelation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CartsOrdersRelationVO {
    private Integer id;

    private Integer cartItemId;

    private Integer orderId;

    public CartsOrdersRelation toPO(){
        CartsOrdersRelation cartsOrdersRelation=new CartsOrdersRelation();
        cartsOrdersRelation.setId(this.id);
        return cartsOrdersRelation;
    }
}