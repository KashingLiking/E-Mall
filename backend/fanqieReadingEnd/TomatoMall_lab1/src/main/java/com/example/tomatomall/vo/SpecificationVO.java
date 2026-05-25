package com.example.tomatomall.vo;

import com.example.tomatomall.po.Specification;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SpecificationVO {
    private Integer id;

    private String item;

    private String value;

    private Integer product_id;

    public Specification toPO(){
        Specification specification=new Specification();
        specification.setItem(this.item);
        specification.setValue(this.value);
        return specification;
    }
}


