package com.example.tomatomall.po;

import com.example.tomatomall.enums.BookTypeEnum;
import com.example.tomatomall.enums.RoleEnum;
import com.example.tomatomall.vo.ProductVO;
import com.example.tomatomall.vo.SpecificationVO;
import com.example.tomatomall.vo.StockpileVO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Integer id;

    @Basic
    @Column(name="title",nullable = false)
    private String title;

    @Basic
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Basic
    @Column(name="rate",nullable = false)
    private Double rate;

    @Basic
    @Column(name="description")
    private String description;

    @Basic
    @Column(name="cover")
    private String cover;

    @Basic
    @Column(name="detail")
    private String detail;

    @Basic
    @Column(name="discountNumber")
    private Double discountNumber = 1.0;

    @Basic
    @Column(name="BookType",nullable = false)
    @Enumerated(EnumType.STRING)
    private BookTypeEnum bookType;

    @Basic
    @Column(name = "recommend_count", columnDefinition = "int default 0")
    private Integer recommendCount = 0;

    @Basic
    @Column(name = "sold_quantity", columnDefinition = "int default 0")
    private Integer soldQuantity = 0;


    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Specification> specifications = new ArrayList<>();

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Stockpile stockpile;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Carts> carts = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    public ProductVO toVO(){
        ProductVO productVO=new ProductVO();
        productVO.setId(this.id);
        productVO.setTitle(this.title);
        productVO.setPrice(this.price);
        productVO.setRate(this.rate);
        productVO.setDescription(this.description);
        productVO.setCover(this.cover);
        productVO.setDetail(this.detail);
        productVO.setDiscountNumber(this.discountNumber);
        productVO.setBookType(this.bookType);
        productVO.setRecommendCount(this.recommendCount);
        productVO.setSoldQuantity(this.soldQuantity);
        return productVO;
    }
}