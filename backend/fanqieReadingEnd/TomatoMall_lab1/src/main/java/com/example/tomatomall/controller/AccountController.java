package com.example.tomatomall.controller;

import com.example.tomatomall.enums.MembershipLevel;
import com.example.tomatomall.exception.TomatomallException;
import com.example.tomatomall.po.Account;
import com.example.tomatomall.repository.AccountRepository;
import com.example.tomatomall.service.AccountService;
import com.example.tomatomall.util.TokenUtil;
import com.example.tomatomall.vo.AccountVO;
import com.example.tomatomall.vo.ResponseVO;
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
    /**
     * 获取用户详情
     */
    @GetMapping("/{username}")
    public ResponseVO<AccountVO> getUser(@RequestHeader("token") String token,@PathVariable String username) {
        if (!tokenUtil.verifyToken(token)) {
            throw TomatomallException.notLogin();
        }
        AccountVO accountVO = accountService.getInformation(username);
        if (accountVO == null) {
            throw TomatomallException.usernameNotExits();
        }
        return ResponseVO.buildSuccess(accountVO);
    }

    /**
     * 创建新的用户
     */
    @PostMapping
    public ResponseVO<String> createUser(@RequestBody AccountVO accountVO) {
        return ResponseVO.buildSuccess(accountService.register(accountVO));
    }

    /**
     * 更新用户信息
     */
    @PutMapping
    public ResponseVO<String> updateUser(@RequestBody AccountVO accountVO) {
        return ResponseVO.buildSuccess(accountService.updateInformation(accountVO));
    }

    /**
     * 登录
     */
    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
    @PostMapping("/login")
    public ResponseVO<String> login( @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String token = accountService.login(request.username, request.password);
        Account account = accountRepository.findByUsername(request.username);
        httpRequest.getSession().setAttribute("currentAccount", account);
        return ResponseVO.buildSuccess(token);
    }

    @GetMapping("/{username}/membership")
    public ResponseVO<MembershipLevel> getMembershipLevel(
            @RequestHeader("token") String token,
            @PathVariable String username) {
        if (!tokenUtil.verifyToken(token)) {
            throw TomatomallException.notLogin();
        }

        Account account = accountRepository.findByUsername(username);
        if (account == null) {
            throw TomatomallException.usernameNotExits();
        }

        return ResponseVO.buildSuccess(accountService.getMembershipLevel(account.getUserid()));
    }
}
