package com.example.emall.serviceImpl;

import com.example.emall.enums.MembershipLevel;
import com.example.emall.exception.EMallException;
import com.example.emall.po.Account;
import com.example.emall.repository.AccountRepository;
import com.example.emall.service.AccountService;
import com.example.emall.util.SecurityUtil;
import com.example.emall.util.TokenUtil;
import com.example.emall.vo.AccountVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AccountServiceImpl implements AccountService {
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    TokenUtil tokenUtil;
    @Autowired
    SecurityUtil securityUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Override
    public String register(AccountVO accountVO) {
        Account account = accountRepository.findByUsername(accountVO.getUsername());
        if (account != null) {
            throw EMallException.usernameAlreadyExists();
        }
        Account newAccount = accountVO.toPO();
        String rawPassword = accountVO.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        newAccount.setPassword(encodedPassword);
        accountRepository.save(newAccount);
        return "注册成功";
    }

    @Override
    public String login(String username, String password) {
        Account account = accountRepository.findByUsername(username);
        if (account == null) {
            throw EMallException.usernameOrPasswordError();
        }
        if(!passwordEncoder.matches(password, account.getPassword())){
            throw EMallException.usernameOrPasswordError();
        }
        return tokenUtil.getToken(account);
    }

    @Override
    public AccountVO getInformation(String username) {
        Account account = accountRepository.findByUsername(username);
        return account.toVO();
    }

    @Override
    public String updateInformation(AccountVO accountVO) {
        Account account=securityUtil.getCurrentAccount();
        if (accountVO.getUsername()!=null){
            account.setUsername(accountVO.getUsername());
        }
        if (accountVO.getPassword()!=null){
            String encodedPassword = passwordEncoder.encode(accountVO.getPassword());
            account.setPassword(encodedPassword);
        }
        if (accountVO.getName()!=null){
            account.setName(accountVO.getName());
        }
        if (accountVO.getAvatar()!=null){
            account.setAvatar(accountVO.getAvatar());
        }
        if (accountVO.getTelephone()!=null){
            account.setTelephone(accountVO.getTelephone());
        }
        if (accountVO.getEmail()!=null){
            account.setEmail(accountVO.getEmail());
        }
        if (accountVO.getLocation()!=null){
            account.setLocation(accountVO.getLocation());
        }
        accountRepository.save(account);
        return "更新成功";
    }

    @Override
    @Transactional
    public void addToTotalSpent(Integer userId, BigDecimal amount) {
        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> EMallException.usernameNotExits());

        BigDecimal newTotal = account.getTotalSpent().add(amount);
        account.setTotalSpent(newTotal);
        accountRepository.save(account);
        updateMembershipLevel(userId);
    }

    @Transactional
    private void updateMembershipLevel(Integer userId) {
        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> EMallException.usernameNotExits());

        MembershipLevel newLevel = MembershipLevel.fromTotalSpent(account.getTotalSpent());
        if (!newLevel.equals(account.getMembershipLevel())) {
            account.setMembershipLevel(newLevel);
            accountRepository.save(account);
        }
    }
}
