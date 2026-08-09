package com.example.emall.repository;

import com.example.emall.po.Account;
import com.example.emall.po.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrdersRepository extends JpaRepository<Orders, Integer> {
    Page<Orders> findByAccount(Account account, Pageable pageable);

    @Query("SELECT o FROM Orders o JOIN FETCH o.cartsOrdersRelation r JOIN FETCH r.cartItem c JOIN FETCH c.product p JOIN FETCH p.stockpile WHERE o.id = :id")
    Optional<Orders> findByIdWithDetails(@Param("id") Integer id);
}
