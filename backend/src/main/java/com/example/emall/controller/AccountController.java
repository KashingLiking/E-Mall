package com.example.emall.controller;

import com.example.emall.exception.EMallException;
import com.example.emall.po.Account;
import com.example.emall.repository.AccountRepository;
import com.example.emall.service.AccountService;
import com.example.emall.util.TokenUtil;
import com.example.emall.vo.AccountVO;
import com.example.emall.vo.ResponseVO;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Resource
    AccountService accountService;
    @Autowired
    private TokenUtil tokenUtil;
    @Autowired
    AccountRepository accountRepository;

    @GetMapping("/{username}")
    public ResponseVO<AccountVO> getUser(@RequestHeader("token") String token, @PathVariable String username) {
        if (!tokenUtil.verifyToken(token)) {
            throw EMallException.notLogin();
        }
        AccountVO accountVO = accountService.getInformation(username);
        if (accountVO == null) {
            throw EMallException.usernameNotExits();
        }
        return ResponseVO.buildSuccess(accountVO);
    }

    @PostMapping
    public ResponseVO<String> createUser(@RequestBody AccountVO accountVO) {
        return ResponseVO.buildSuccess(accountService.register(accountVO));
    }

    @PutMapping
    public ResponseVO<String> updateUser(@RequestBody AccountVO accountVO) {
        return ResponseVO.buildSuccess(accountService.updateInformation(accountVO));
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @PostMapping("/login")
    public ResponseVO<String> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String token = accountService.login(request.username, request.password);
        Account account = accountRepository.findByUsername(request.username);
        httpRequest.getSession().setAttribute("currentAccount", account);
        return ResponseVO.buildSuccess(token);
    }
}
