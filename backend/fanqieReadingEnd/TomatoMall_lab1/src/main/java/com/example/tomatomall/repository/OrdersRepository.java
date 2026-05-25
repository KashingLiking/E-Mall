package com.example.tomatomall.repository;

import com.example.tomatomall.po.Account;
import com.example.tomatomall.po.Orders;
import com.example.tomatomall.vo.OrdersVO;

import org.springframework.data.jpa.repository.JpaRepository;
public interface OrdersRepository extends JpaRepository<Orders, Integer>{

}