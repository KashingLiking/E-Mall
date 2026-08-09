package com.example.emall.vo;

import com.example.emall.po.CartsOrdersRelation;
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