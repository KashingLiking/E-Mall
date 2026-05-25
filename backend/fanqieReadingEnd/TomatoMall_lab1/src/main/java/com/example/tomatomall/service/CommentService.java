package com.example.tomatomall.service;

import com.example.tomatomall.vo.CommentVO;
import java.util.List;

public interface CommentService {
    CommentVO addComment(CommentVO commentVO);
    List<CommentVO> getCommentsByProduct(Integer productId);
    List<CommentVO> getCommentsByUser(Integer userId);
    String deleteComment(Integer commentId, Integer userId);
}