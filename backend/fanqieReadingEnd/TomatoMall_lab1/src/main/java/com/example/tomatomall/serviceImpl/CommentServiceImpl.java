package com.example.tomatomall.serviceImpl;

import com.example.tomatomall.exception.TomatomallException;
import com.example.tomatomall.po.Account;
import com.example.tomatomall.po.Comment;
import com.example.tomatomall.po.Product;
import com.example.tomatomall.repository.AccountRepository;
import com.example.tomatomall.repository.CommentRepository;
import com.example.tomatomall.repository.ProductRepository;
import com.example.tomatomall.service.CommentService;
import com.example.tomatomall.util.SecurityUtil;
import com.example.tomatomall.vo.CommentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private SecurityUtil securityUtil;

    @Override
    @Transactional
    public CommentVO addComment(CommentVO commentVO) {
        Account account = securityUtil.getCurrentAccount();
        Product product = productRepository.findById(commentVO.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + commentVO.getProductId()));
        Comment comment = new Comment();
        comment.setProduct(product);
        comment.setAccount(account);
        comment.setContent(commentVO.getContent());
        comment.setRating(commentVO.getRating());

        commentRepository.save(comment);
        return convertToVO(comment);
    }

    @Override
    public List<CommentVO> getCommentsByProduct(Integer productId) {
        return commentRepository.findByProductId(productId).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentVO> getCommentsByUser(Integer userId) {
        return commentRepository.findByAccountId(userId).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String deleteComment(Integer commentId, Integer userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> TomatomallException.commentNotExits());

        if (comment.getAccount().getUserid()!=userId) {
            throw TomatomallException.noPermission();
        }

        commentRepository.delete(comment);
        return "评论删除成功";
    }

    private CommentVO convertToVO(Comment comment) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setProductId(comment.getProduct().getId());
        vo.setUserId(comment.getAccount().getUserid());
        vo.setUsername(comment.getAccount().getUsername());
        vo.setAvatar(comment.getAccount().getAvatar());
        vo.setContent(comment.getContent());
        vo.setRating(comment.getRating());
        vo.setCreatedAt(comment.getCreatedAt());
        vo.setUpdatedAt(comment.getUpdatedAt());
        return vo;
    }
}