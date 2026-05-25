package com.example.tomatomall.po;

import com.example.tomatomall.enums.MembershipLevel;
import com.example.tomatomall.enums.RoleEnum;
import com.example.tomatomall.vo.AccountVO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Account {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "userid")
    private int userid;

    @Basic
    @Column(name="username",nullable = false)
    private String username;

    @Basic
    @Column(name="password",nullable = false)
    private String password;

    @Basic
    @Column(name="name",nullable = false)
    private String name;

    @Basic
    @Column(name="role",nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleEnum role;

    @Basic
    @Column(name="avatar")
    private String avatar;

    @Basic
    @Column(name="telephone")
    private String telephone;

    @Basic
    @Column(name="email")
    private String email;

    @Basic
    @Column(name="location")
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_level")
    private MembershipLevel membershipLevel = MembershipLevel.BRONZE;

    @Basic
    @Column(name = "total_spent", nullable = false, columnDefinition = "DECIMAL(10,2) DEFAULT 0.00")
    private BigDecimal totalSpent = BigDecimal.ZERO;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Orders> orders = new ArrayList<>();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Carts> carts = new ArrayList<>();

    public AccountVO toVO(){
        AccountVO accountVO=new AccountVO();
        accountVO.setUsername(this.username);
        accountVO.setPassword(this.password);
        accountVO.setName(this.name);
        accountVO.setAvatar(this.avatar);
        accountVO.setRole(this.role);
        accountVO.setTelephone(this.telephone);
        accountVO.setEmail(this.email);
        accountVO.setLocation(this.location);
        accountVO.setTotalSpent(this.totalSpent);
        accountVO.setMembershipLevel(this.membershipLevel);
        return accountVO;
    }
}
