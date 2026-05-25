package com.example.tomatomall.po;

import com.example.tomatomall.vo.CartItemVO;
import com.example.tomatomall.vo.CartItemsVO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "carts")
public class Carts {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "cartItemId")
    private Integer id;

    @Basic
    @Column(name = "quantity", nullable = false, columnDefinition = "INT DEFAULT 1")
    private Integer quantity = 1;  // Added default value

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false )
    private Account account;

    @OneToMany(mappedBy = "cartItem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CartsOrdersRelation> cartsOrdersRelations = new ArrayList<>();

    public CartItemVO toItemVO() {
        CartItemVO cartItemVO = new CartItemVO();
        cartItemVO.setCartItemId(this.id);
        cartItemVO.setProductId(this.product.getId());
        cartItemVO.setTitle(this.product.getTitle());
        cartItemVO.setPrice(this.product.getPrice());
        cartItemVO.setDescription(this.product.getDescription());
        cartItemVO.setCover(this.product.getCover());
        cartItemVO.setDetail(this.product.getDetail());
        cartItemVO.setQuantity(this.quantity);
        return cartItemVO;
    }
}