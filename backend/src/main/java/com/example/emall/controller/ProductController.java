package com.example.emall.controller;

import com.example.emall.service.ProductService;
import com.example.emall.vo.ProductVO;
import com.example.emall.vo.ResponseVO;
import com.example.emall.vo.StockpileVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Resource
    private ProductService productService;

    @GetMapping
    public ResponseVO<List<ProductVO>> getProducts() {
        return ResponseVO.buildSuccess(productService.getProducts());
    }

    @GetMapping("/search")
    public ResponseVO<List<ProductVO>> searchByTitle(@RequestParam("title") String title) {
        return ResponseVO.buildSuccess(productService.searchByTitle(title));
    }

    @GetMapping("/{id}")
    public ResponseVO<ProductVO> getInformation(@PathVariable("id") Integer id) {
        return ResponseVO.buildSuccess(productService.getInformation(id));
    }

    @PutMapping
    public ResponseVO<String> updateProduct(@RequestBody ProductVO productVO) {
        return ResponseVO.buildSuccess(productService.updateInformation(productVO));
    }

    @PostMapping
    public ResponseVO<ProductVO> addProduct(@RequestBody ProductVO productVO) {
        return ResponseVO.buildSuccess(productService.addProduct(productVO));
    }

    @DeleteMapping("/{id}")
    public ResponseVO<String> deleteProduct(@PathVariable(value = "id") Integer id) {
        return ResponseVO.buildSuccess(productService.deleteProduct(id));
    }

    @PatchMapping("/stockpile/{productId}")
    public ResponseVO<String> updateStockpile(
            @PathVariable Integer productId,
            @RequestBody Map<String, Integer> request) {
        Integer amount = request.get("amount");
        return ResponseVO.buildSuccess(productService.updateStockpile(productId, amount));
    }

    @GetMapping("/stockpile/{productId}")
    public ResponseVO<StockpileVO> getStockpile(@PathVariable Integer productId) {
        return ResponseVO.buildSuccess(productService.getStockpile(productId));
    }
}
