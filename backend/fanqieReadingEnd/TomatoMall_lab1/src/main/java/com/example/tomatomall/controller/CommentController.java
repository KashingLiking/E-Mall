package com.example.tomatomall.controller;

import com.example.tomatomall.po.Account;
import com.example.tomatomall.service.CommentService;
import com.example.tomatomall.util.SecurityUtil;
import com.example.tomatomall.vo.CommentVO;
import com.example.tomatomall.vo.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private SecurityUtil securityUtil;

    @PostMapping
    public ResponseVO<CommentVO> addComment(@RequestBody CommentVO commentVO) {
        return ResponseVO.buildSuccess(commentService.addComment(commentVO));
    }

    @GetMapping("/product/{productId}")
    public ResponseVO<List<CommentVO>> getProductComments(@PathVariable Integer productId) {
        return ResponseVO.buildSuccess(commentService.getCommentsByProduct(productId));
    }

    @GetMapping("/user/{userId}")
    public ResponseVO<List<CommentVO>> getUserComments(@PathVariable Integer userId) {
        return ResponseVO.buildSuccess(commentService.getCommentsByUser(userId));
    }

    @DeleteMapping("/{commentId}")
    public ResponseVO<String> deleteComment(@PathVariable Integer commentId) {
        Account account = securityUtil.getCurrentAccount();
        return ResponseVO.buildSuccess(commentService.deleteComment(commentId, account.getUserid()));
    }
}