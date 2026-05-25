package com.example.tomatomall.controller;

import com.example.tomatomall.service.ProductService;
import com.example.tomatomall.vo.ProductVO;
import com.example.tomatomall.vo.ResponseVO;
import com.example.tomatomall.vo.StockpileVO;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Resource
    private ProductService productService;

    /**
     * 获取商品列表
     */
    @GetMapping
    public ResponseVO<List<ProductVO>> getProducts() {
        return ResponseVO.buildSuccess(productService.getProducts());
    }

    /**
     * 获取指定商品信息
     */
    @GetMapping("/{id}")
    public ResponseVO<ProductVO> getInformation(@PathVariable("id") Integer id) {
        return ResponseVO.buildSuccess(productService.getInformation(id));
    }

    /**
     * 更新商品信息
     */
    @PutMapping
    public ResponseVO<String> updateProduct(@RequestBody ProductVO productVO) {
        return ResponseVO.buildSuccess(productService.updateInformation(productVO));
    }

    /**
     * 增加商品
     */
    @PostMapping
    public ResponseVO<ProductVO> addProduct(@RequestBody ProductVO productVO) {
        return ResponseVO.buildSuccess(productService.addProduct(productVO));
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public ResponseVO<String> deleteProduct(@PathVariable(value = "id") Integer id) {
        return ResponseVO.buildSuccess(productService.deleteProduct(id));
    }

    /**
     * 调整指定商品的库存
     */
    @PatchMapping("/stockpile/{productId}")
    public ResponseVO<String> updateStockpile(
            @PathVariable Integer productId,
            @RequestBody Map<String, Integer> request) {
        Integer amount = request.get("amount");
        return ResponseVO.buildSuccess(productService.updateStockpile(productId, amount));
    }

    /**
     * 查询指定商品的库存
     */
    @GetMapping("/stockpile/{productId}")
    public ResponseVO<StockpileVO> getStockpile(@PathVariable Integer productId) {
        return ResponseVO.buildSuccess(productService.getStockpile(productId));
    }

    /**
     * 获取书籍打折信息
     */
    @GetMapping("/discount/{productId}")
    public ResponseVO<Double> getDicountNumber(@PathVariable Integer productId) {
        return ResponseVO.buildSuccess(productService.getDiscountNumber(productId));
    }

    @PostMapping("/recommend/{productId}")
    public ResponseVO<String> recommendProduct(@PathVariable Integer productId) {
        return ResponseVO.buildSuccess(productService.recommendProduct(productId));
    }

    @GetMapping("/sold/{productId}")
    public ResponseVO<Integer> getSoldQuantity(@PathVariable Integer productId) {
        return ResponseVO.buildSuccess(productService.getSoldQuantity(productId));
    }

    @PatchMapping("/updateDiscount/{productId}")
    public ResponseVO<String> updateDiscount(
            @PathVariable Integer productId,
            @RequestBody Map<String, Double> request) {
        Double discountNumber = request.get("discountNumber");
        return ResponseVO.buildSuccess(productService.updateDiscount(productId, discountNumber));
    }
}