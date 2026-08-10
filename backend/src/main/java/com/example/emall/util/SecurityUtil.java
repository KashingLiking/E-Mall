package com.example.emall.util;

import com.example.emall.po.Account;
import com.example.emall.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class SecurityUtil {

    @Autowired
    HttpServletRequest httpServletRequest;

    @Autowired
    TokenUtil tokenUtil;

    @Autowired
    AccountRepository accountRepository;

    public Account getCurrentAccount() {
        String token = httpServletRequest.getHeader("token");
        if (token == null || token.isEmpty()) {
            return null;
        }
        if (!tokenUtil.verifyToken(token)) {
            return null;
        }
        return tokenUtil.getAccount(token);
    }
}
