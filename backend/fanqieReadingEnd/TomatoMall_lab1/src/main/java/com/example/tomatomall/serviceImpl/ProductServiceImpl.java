package com.example.tomatomall.serviceImpl;

import com.example.tomatomall.po.Product;
import com.example.tomatomall.po.Specification;
import com.example.tomatomall.po.Stockpile;
import com.example.tomatomall.repository.ProductRepository;
import com.example.tomatomall.service.ProductService;
import com.example.tomatomall.vo.CommentVO;
import com.example.tomatomall.vo.ProductVO;
import com.example.tomatomall.vo.SpecificationVO;
import com.example.tomatomall.vo.StockpileVO;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<ProductVO> getProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(this::convertToProductVO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductVO getInformation(Integer id) {
        Optional<Product> productOptional = productRepository.findById(id);
        if(productOptional.isPresent()){
            Product product = productOptional.get();

            // 显式初始化关联实体
            Hibernate.initialize(product.getSpecifications());
            Hibernate.initialize(product.getStockpile());

            ProductVO productVO = product.toVO();

            // 转换Specifications
            List<SpecificationVO> specificationVOs = product.getSpecifications()
                    .stream()
                    .map(Specification::toVO)
                    .collect(Collectors.toList());
            productVO.setSpecifications(specificationVOs);

            // 转换Stockpile
            if(product.getStockpile() != null) {
                productVO.setStockpile(product.getStockpile().toVO());
            }

            return productVO;
        }
        return null; // 或者抛出异常
    }

    @Override
    @Transactional
    public String updateInformation(ProductVO productVO) {
        Optional<Product> productOptional = productRepository.findById(productVO.getId());
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            product.setTitle(productVO.getTitle());
            product.setPrice(productVO.getPrice());
            product.setRate(productVO.getRate());
            product.setDescription(productVO.getDescription());
            product.setCover(productVO.getCover());
            product.setDetail(productVO.getDetail());
            product.setDiscountNumber(product.getDiscountNumber());
            product.setBookType(productVO.getBookType());
            // 更新规格信息
            if (productVO.getSpecifications() != null) {
                product.getSpecifications().clear();
                productVO.getSpecifications().forEach(specVO -> {
                    Specification spec = specVO.toPO();
                    spec.setProduct(product);
                    product.getSpecifications().add(spec);
                });
            }

            // 更新库存信息
            if (productVO.getStockpile() != null && product.getStockpile() != null) {
                Stockpile stockpile = product.getStockpile();
                stockpile.setAmount(productVO.getStockpile().getAmount());
                stockpile.setFrozen(productVO.getStockpile().getFrozen());
            }

            productRepository.save(product);
            return "更新成功";
        }
        return "商品不存在";
    }

    @Override
    @Transactional
    public ProductVO addProduct(ProductVO productVO) {
        try {
            if(productVO.getDiscountNumber()==null){
                productVO.setDiscountNumber(1.0);
            }
            if(productVO.getRecommendCount()==null){
                productVO.setRecommendCount(0);
            }
            Product product = productVO.toPO();

            // 处理规格
            if (productVO.getSpecifications() != null) {
                productVO.getSpecifications().forEach(specVO -> {
                    Specification spec = specVO.toPO();
                    spec.setProduct(product);
                    product.getSpecifications().add(spec);
                });
            }

            if (productVO.getStockpile() == null) {
                // 如果没有提供库存信息，创建默认库存
                StockpileVO defaultStock = new StockpileVO();
                defaultStock.setAmount(0);
                defaultStock.setFrozen(0);
                productVO.setStockpile(defaultStock);
            }

            Stockpile stockpile = productVO.getStockpile().toPO();
            stockpile.setProduct(product);
            product.setStockpile(stockpile);

            productRepository.save(product);
            return product.toVO();
        } catch (Exception e) {
            log.error("失败",e);
            return null;
        }
    }

    @Override
    @Transactional
    public String deleteProduct(Integer id) {
        try {
            productRepository.deleteById(id);
            return "删除成功";
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    @Transactional
    public String updateStockpile(Integer productId, Integer amount) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isPresent() && productOptional.get().getStockpile() != null) {
            Product product = productOptional.get();
            Stockpile stockpile = product.getStockpile();
            stockpile.setAmount(amount);
            stockpile.setFrozen(product.getStockpile().getFrozen());
            return "调整库存成功";
        }
        return "商品不存在";
    }

    @Override
    public StockpileVO getStockpile(Integer productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isPresent() && productOptional.get().getStockpile() != null) {
            Product product = productOptional.get();
            return product.getStockpile().toVO();
        }
        return null;
    }

    // 辅助方法：将Product实体转换为ProductVO
    private ProductVO convertToProductVO(Product product) {
        ProductVO productVO = product.toVO();
        productVO.setId(product.getId());

        // 设置规格VO
        if (product.getSpecifications() != null) {
            List<SpecificationVO> specVOs = product.getSpecifications().stream()
                    .map(spec -> {
                        SpecificationVO specVO = spec.toVO();
                        specVO.setId(spec.getId());
                        specVO.setProduct_id(spec.getProduct().getId());
                        return specVO;
                    })
                    .collect(Collectors.toList());
            productVO.setSpecifications(specVOs);
        }

        // 设置库存VO
        if (product.getStockpile() != null) {
            StockpileVO stockpileVO = product.getStockpile().toVO();
            stockpileVO.setId(product.getStockpile().getId());
            stockpileVO.setProduct_id(product.getStockpile().getProduct().getId());
            productVO.setStockpile(stockpileVO);
        }

        return productVO;
    }

    @Override
    public ProductVO getCommentInformation(Integer id) {
        Optional<Product> productOptional = productRepository.findById(id);
        if(productOptional.isPresent()){
            Product product = productOptional.get();

            // 显式初始化关联实体
            Hibernate.initialize(product.getSpecifications());
            Hibernate.initialize(product.getStockpile());
            Hibernate.initialize(product.getComments());

            ProductVO productVO = product.toVO();

            // 转换Specifications
            List<SpecificationVO> specificationVOs = product.getSpecifications()
                    .stream()
                    .map(Specification::toVO)
                    .collect(Collectors.toList());
            productVO.setSpecifications(specificationVOs);

            // 转换Stockpile
            if(product.getStockpile() != null) {
                productVO.setStockpile(product.getStockpile().toVO());
            }

            // 转换Comments
            List<CommentVO> commentVOs = product.getComments()
                    .stream()
                    .map(comment -> {
                        CommentVO commentVO = new CommentVO();
                        commentVO.setId(comment.getId());
                        commentVO.setUserId(comment.getAccount().getUserid());
                        commentVO.setUsername(comment.getAccount().getUsername());
                        commentVO.setAvatar(comment.getAccount().getAvatar());
                        commentVO.setContent(comment.getContent());
                        commentVO.setRating(comment.getRating());
                        commentVO.setCreatedAt(comment.getCreatedAt());
                        commentVO.setUpdatedAt(comment.getUpdatedAt());
                        return commentVO;
                    })
                    .collect(Collectors.toList());
            productVO.setComments(commentVOs);

            return productVO;
        }
        return null; // 或者抛出异常
    }

    @Override
    public Double getDiscountNumber(Integer productId){
        Optional<Product> productOptional = productRepository.findById(productId);
        if(productOptional.isPresent()){
            return productOptional.get().getDiscountNumber();
        }
        return 1.0;
    }

    @Override
    @Transactional
    public String recommendProduct(Integer productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            product.setRecommendCount(product.getRecommendCount() + 1); // 推荐数+1
            productRepository.save(product);
            return "推荐成功";
        }
        return "商品不存在";
    }

    @Override
    public Integer getSoldQuantity(Integer productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        return productOptional.map(Product::getSoldQuantity).orElse(0);
    }

    @Override
    @Transactional
    public String updateDiscount(Integer productId, Double discountNumber) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isPresent()) {
            // Validate discount number (typically between 0 and 1)
            if (discountNumber <= 0 || discountNumber > 1) {
                return "折扣数值必须在0到1之间";
            }

            Product product = productOptional.get();
            product.setDiscountNumber(discountNumber);
            productRepository.save(product);
            return "折扣更新成功";
        }
        return "商品不存在";
    }
}