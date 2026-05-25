package com.example.tomatomall.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class CommentVO {
    private Integer id;
    private Integer productId;
    private Integer userId;
    private String username; // 评论者用户名
    private String avatar; // 评论者头像
    private String content;
    private Integer rating;
    private Date createdAt;
    private Date updatedAt;
}