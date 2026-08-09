package com.example.emall.vo;

import com.example.emall.enums.MembershipLevel;
import com.example.emall.enums.RoleEnum;
import com.example.emall.po.Account;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class AccountVO {
    private Integer id;

    private String username;

    private String password;

    private String name;

    private RoleEnum role;

    private String avatar;

    private String telephone;

    private String email;

    private String location;

    private BigDecimal totalSpent;

    private MembershipLevel membershipLevel;

    public Account toPO(){
        Account account=new Account();
        account.setUsername(this.username);
        account.setPassword(this.password);
        account.setName(this.name);
        account.setAvatar(this.avatar);
        account.setRole(this.role);
        account.setTelephone(this.telephone);
        account.setEmail(this.email);
        account.setLocation(this.location);
        account.setTotalSpent(this.totalSpent != null ? this.totalSpent : BigDecimal.ZERO);
        account.setMembershipLevel(this.membershipLevel != null ? this.membershipLevel : MembershipLevel.BRONZE);
        return account;
    }
}
