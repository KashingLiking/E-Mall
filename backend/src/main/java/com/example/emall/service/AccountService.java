package com.example.emall.service;

import com.example.emall.vo.AccountVO;

import java.math.BigDecimal;

public interface AccountService {
    String register(AccountVO accountVO);

    String login(String username,String password);

    AccountVO getInformation(String username);

    String updateInformation(AccountVO accountVO);

    void addToTotalSpent(Integer userId, BigDecimal amount);
}
