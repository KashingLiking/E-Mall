package com.example.tomatomall.repository;

import com.example.tomatomall.po.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    // 支持String类型ID查询
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findById(@Param("id") String id);

    // 关联查询
    @EntityGraph(attributePaths = {"specifications", "stockpile"})
    Optional<Product> findWithAssociationsById(Integer id);
}