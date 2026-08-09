package com.example.emall.repository;

import com.example.emall.po.SeckillActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SeckillActivityRepository extends JpaRepository<SeckillActivity, Integer> {

    @Query("SELECT a FROM SeckillActivity a JOIN FETCH a.product")
    List<SeckillActivity> findAllWithProduct();
}
