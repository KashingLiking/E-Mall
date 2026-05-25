package com.example.tomatomall.enums;

import java.math.BigDecimal;

public enum MembershipLevel {
    BRONZE("青铜会员", new BigDecimal("0"), new BigDecimal("1000")),
    SILVER("白银会员", new BigDecimal("1000"), new BigDecimal("5000")),
    GOLD("黄金会员", new BigDecimal("5000"), new BigDecimal("10000")),
    PLATINUM("白金会员", new BigDecimal("10000"), new BigDecimal("50000")),
    DIAMOND("钻石会员", new BigDecimal("50000"), null);

    private final String name;
    private final BigDecimal minAmount;
    private final BigDecimal maxAmount;

    MembershipLevel(String name, BigDecimal minAmount, BigDecimal maxAmount) {
        this.name = name;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public static MembershipLevel fromTotalSpent(BigDecimal totalSpent) {
        if (totalSpent == null) {
            return BRONZE;
        }

        for (MembershipLevel level : values()) {
            if ((level.minAmount == null || totalSpent.compareTo(level.minAmount) >= 0) &&
                    (level.maxAmount == null || totalSpent.compareTo(level.maxAmount) < 0)) {
                return level;
            }
        }
        return DIAMOND; // 如果超过所有等级，返回最高等级
    }
}