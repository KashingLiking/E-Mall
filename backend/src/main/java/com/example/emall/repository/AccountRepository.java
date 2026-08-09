package com.example.emall.repository;

import com.example.emall.po.Account;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AccountRepository extends JpaRepository<Account, Integer>{
    Account findByUsername(String username);
    Account findByUsernameAndPassword(String username, String password);
}
