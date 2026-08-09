package com.example.emall.serviceImpl;

import com.example.emall.exception.EMallException;
import com.example.emall.po.Product;
import com.example.emall.po.Specification;
import com.example.emall.po.Stockpile;
import com.example.emall.repository.ProductRepository;
import com.example.emall.service.ProductService;
import com.example.emall.vo.ProductVO;
import com.example.emall.vo.SpecificationVO;
import com.example.emall.vo.StockpileVO;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            Hibernate.initialize(product.getSpecifications());
            Hibernate.initialize(product.getStockpile());

            ProductVO productVO = product.toVO();
            List<SpecificationVO> specificationVOs = product.getSpecifications()
                    .stream()
                    .map(Specification::toVO)
                    .collect(Collectors.toList());
            productVO.setSpecifications(specificationVOs);

            if (product.getStockpile() != null) {
                productVO.setStockpile(product.getStockpile().toVO());
            }
            return productVO;
        }
        return null;
    }

    @Override
    @Transactional
    public String updateInformation(ProductVO productVO) {
        Optional<Product> productOptional = productRepository.findById(productVO.getId());
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            product.setTitle(productVO.getTitle());
            product.setPrice(productVO.getPrice());
            if (productVO.getRate() != null) {
                product.setRate(productVO.getRate());
            }
            product.setDescription(productVO.getDescription());
            product.setCover(productVO.getCover());
            product.setDetail(productVO.getDetail());
            product.setDiscountNumber(product.getDiscountNumber());
            if (productVO.getBookType() != null) {
                product.setBookType(productVO.getBookType());
            }
            if (productVO.getSpecifications() != null) {
                product.getSpecifications().clear();
                productVO.getSpecifications().forEach(specVO -> {
                    Specification spec = specVO.toPO();
                    spec.setProduct(product);
                    product.getSpecifications().add(spec);
                });
            }
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
        if (productVO.getTitle() == null || productVO.getTitle().trim().isEmpty()
                || productVO.getPrice() == null
                || productVO.getBookType() == null) {
            throw EMallException.productRequiredFieldsMissing();
        }
        try {
            if (productVO.getRate() == null) {
                productVO.setRate(0.0);
            }
            if (productVO.getDiscountNumber() == null) {
                productVO.setDiscountNumber(1.0);
            }
            if (productVO.getRecommendCount() == null) {
                productVO.setRecommendCount(0);
            }
            if (productVO.getSoldQuantity() == null) {
                productVO.setSoldQuantity(0);
            }
            if (productVO.getDescription() == null) {
                productVO.setDescription("");
            }
            if (productVO.getCover() == null) {
                productVO.setCover("");
            }
            if (productVO.getDetail() == null) {
                productVO.setDetail("");
            }
            Product product = productVO.toPO();

            if (productVO.getSpecifications() != null) {
                productVO.getSpecifications().forEach(specVO -> {
                    if (specVO.getValue() == null || String.valueOf(specVO.getValue()).trim().isEmpty()) {
                        return;
                    }
                    Specification spec = specVO.toPO();
                    spec.setProduct(product);
                    product.getSpecifications().add(spec);
                });
            }

            if (productVO.getStockpile() == null) {
                // 默认库存，方便走通下单支付闭环；管理员可后续再改
                StockpileVO defaultStock = new StockpileVO();
                defaultStock.setAmount(100);
                defaultStock.setFrozen(0);
                productVO.setStockpile(defaultStock);
            }

            Stockpile stockpile = productVO.getStockpile().toPO();
            stockpile.setProduct(product);
            product.setStockpile(stockpile);

            productRepository.save(product);
            return product.toVO();
        } catch (EMallException e) {
            throw e;
        } catch (Exception e) {
            log.error("失败", e);
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

    @Override
    public List<ProductVO> searchByTitle(String title) {
        return productRepository.findByTitleContaining(title)
                .stream()
                .map(this::convertToProductVO)
                .collect(Collectors.toList());
    }

    private ProductVO convertToProductVO(Product product) {
        ProductVO productVO = product.toVO();
        productVO.setId(product.getId());

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

        if (product.getStockpile() != null) {
            StockpileVO stockpileVO = product.getStockpile().toVO();
            stockpileVO.setId(product.getStockpile().getId());
            stockpileVO.setProduct_id(product.getStockpile().getProduct().getId());
            productVO.setStockpile(stockpileVO);
        }

        return productVO;
    }
}
