package com.example.tomatomall.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;

import com.example.tomatomall.po.Account;
import com.example.tomatomall.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class TokenUtil {
    private static final long EXPIRE_TIME = 24 * 60 * 60 * 1000;

    @Autowired
    AccountRepository accountRepository;

    public String getToken(Account account) {
        Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
        return JWT.create()
                .withAudience(account.getUsername())
                .withExpiresAt(date)
                .sign(Algorithm.HMAC256(account.getPassword()));
    }

    public boolean verifyToken(String token) {
        try {
            String userName= JWT.decode(token).getAudience().get(0);
            Account account= accountRepository.findByUsername(userName);
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(account.getPassword())).build();
            jwtVerifier.verify(token);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public Account getAccount(String token){
        String userName=JWT.decode(token).getAudience().get(0);
        return accountRepository.findByUsername(userName);
    }
}
