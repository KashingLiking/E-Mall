package com.example.emall.exception;

public class EMallException extends RuntimeException {
    private String errorCode; // 添加一个用于存储错误代码的字段

    // 修改构造函数以接受错误代码和错误消息
    public EMallException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    // 修改静态方法以包含错误代码
    public static EMallException usernameAlreadyExists() {
        return new EMallException("404", "用户名已存在");
    }

    public static EMallException usernameOrPasswordError() {
        return new EMallException("404", "用户名或密码错误");
    }

    public static EMallException usernameNotExits() {
        return new EMallException("404", "用户名不存在");
    }

    public static EMallException notLogin() {
        return new EMallException("401", "用户未登录");
    }

    public static EMallException productsNotExits() {
        return new EMallException("400", "商品不存在");
    }

    public static EMallException productRequiredFieldsMissing() {
        return new EMallException("400", "书名、分类、价格为必填项");
    }

    public static EMallException productsNotEnough() {
        return new EMallException("405", "商品不够");
    }

    public static EMallException cartProductNotExits() {return new EMallException("400", "购物车商品不存在");}

    public static EMallException orderNotExits() {return new EMallException("400", "订单不存在");}

    public static EMallException commentNotExits() {
        return new EMallException("400","评论不存在");
    }

    public static EMallException noPermission() {
        return new EMallException("403","无权限操作");
    }

    public static EMallException seckillActivityNotExits() {
        return new EMallException("400", "秒杀活动不存在");
    }

    public static EMallException seckillSoldOut() {
        return new EMallException("405", "库存不足，秒杀失败");
    }

    public static EMallException seckillAlreadyPurchased() {
        return new EMallException("409", "您已参与过该活动，每人限购一件");
    }

    public static EMallException seckillNotStarted() {
        return new EMallException("400", "秒杀活动尚未开始");
    }

    public static EMallException seckillEnded() {
        return new EMallException("400", "秒杀活动已结束");
    }
}