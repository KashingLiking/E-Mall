package com.example.emall.repository;

import com.example.emall.po.Account;
import com.example.emall.po.Carts;
import com.example.emall.vo.CartItemVO;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartsRepository extends JpaRepository<Carts, Integer> {

    @Query("SELECT c FROM Carts c WHERE c.account.userid = :userId")
    List<Carts> findCartItemsByUserId(@Param("userId") Integer userId);

    @Query("SELECT c FROM Carts c JOIN FETCH c.product p JOIN FETCH p.stockpile WHERE c.id = :id")
    Optional<Carts> findByIdWithProductAndStockpile(@Param("id") Integer id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM carts WHERE cart_item_id IN (:ids)", nativeQuery = true)
    void deleteByIds(@Param("ids") List<Integer> ids);
}
