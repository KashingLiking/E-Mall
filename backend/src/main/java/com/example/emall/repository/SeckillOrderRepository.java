package com.example.emall.repository;

import com.example.emall.po.SeckillOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeckillOrderRepository extends JpaRepository<SeckillOrder, Integer> {

    boolean existsByActivityIdAndAccountUserid(Integer activityId, int userid);
}
