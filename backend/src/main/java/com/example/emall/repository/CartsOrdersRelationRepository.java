package com.example.emall.repository;

import com.example.emall.po.CartsOrdersRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CartsOrdersRelationRepository extends JpaRepository<CartsOrdersRelation, Integer> {

    @Query("SELECT r.cartItem.id FROM CartsOrdersRelation r WHERE r.orders.id = :orderId AND r.cartItem IS NOT NULL")
    List<Integer> findCartIdsByOrderId(@Param("orderId") Integer orderId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM carts_orders_relation WHERE order_id = :orderId", nativeQuery = true)
    void deleteByOrderId(@Param("orderId") Integer orderId);
}
