package com.example.tomatomall.vo;

import com.example.tomatomall.po.Advertisements;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class AdvertisementsVO {
    private Integer id;

    private String title;

    private String content;

    private String imgUrl;

    private String productId;
    public Advertisements toPO(){
        Advertisements advertisements=new Advertisements();
        advertisements.setId(this.id);
        advertisements.setTitle(this.title);
        advertisements.setContent(this.content);
        advertisements.setImgUrl(this.imgUrl);
        advertisements.setProductId(this.productId);
        return advertisements;
    }
}
