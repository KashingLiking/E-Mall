package com.example.emall.service;

import com.example.emall.vo.ProductVO;
import com.example.emall.vo.StockpileVO;

import java.util.List;

public interface ProductService {
    List<ProductVO> getProducts();

    ProductVO getInformation(Integer id);

    String updateInformation(ProductVO productVO);

    ProductVO addProduct(ProductVO productVO);

    String deleteProduct(Integer id);

    String updateStockpile(Integer productId, Integer amount);

    StockpileVO getStockpile(Integer productId);

    List<ProductVO> searchByTitle(String title);
}
