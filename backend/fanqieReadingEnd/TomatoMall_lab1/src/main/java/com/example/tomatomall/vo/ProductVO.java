package com.example.tomatomall.vo;

import java.math.BigDecimal;
import java.util.List;

import com.example.tomatomall.enums.BookTypeEnum;
import com.example.tomatomall.po.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductVO {
    private Integer id;

    private String title;

    private BigDecimal price;

    private Double rate;

    private String description;

    private String cover;

    private String detail;

    private List<SpecificationVO> specifications; // 新增

    private StockpileVO stockpile;

    private List<CommentVO> comments;

    private Double discountNumber;

    private BookTypeEnum bookType;

    private Integer recommendCount;

    private Integer soldQuantity;

    public Product toPO(){
        Product product=new Product();
        product.setId(this.id);
        product.setTitle(this.title);
        product.setPrice(this.price);
        product.setRate(this.rate);
        product.setDescription(this.description);
        product.setCover(this.cover);
        product.setDetail(this.detail);
        product.setDiscountNumber(this.discountNumber);
        product.setBookType(this.bookType);
        product.setRecommendCount(this.recommendCount);
        product.setSoldQuantity(this.soldQuantity);
        return product;
    }
}