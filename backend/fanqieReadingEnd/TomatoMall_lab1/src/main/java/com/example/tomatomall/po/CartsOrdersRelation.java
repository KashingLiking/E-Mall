package com.example.tomatomall.po;

import com.example.tomatomall.vo.CartsOrdersRelationVO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cartsOrdersRelation")  // Changed to match your table name
public class CartsOrdersRelation {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cartitem_id", referencedColumnName = "cartItemId", nullable = false)
    private Carts cartItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", referencedColumnName = "orderId", nullable = false)
    private Orders orders;

    public CartsOrdersRelationVO toVO() {
        CartsOrdersRelationVO cartsOrdersRelationVO = new CartsOrdersRelationVO();
        cartsOrdersRelationVO.setId(this.id);
        return cartsOrdersRelationVO;
    }
}