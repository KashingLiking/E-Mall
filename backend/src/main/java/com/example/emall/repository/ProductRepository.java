package com.example.emall.repository;

import com.example.emall.po.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findById(@Param("id") String id);

    @EntityGraph(attributePaths = {"specifications", "stockpile"})
    Optional<Product> findWithAssociationsById(Integer id);

    @Query("SELECT p FROM Product p WHERE p.title LIKE %:title%")
    List<Product> findByTitleContaining(@Param("title") String title);
}