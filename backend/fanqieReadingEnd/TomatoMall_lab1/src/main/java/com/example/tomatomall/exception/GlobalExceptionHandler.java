package com.example.tomatomall.exception;

import com.example.tomatomall.vo.ResponseVO;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = TomatomallException.class)
    public ResponseVO<String> handleAIExternalException(TomatomallException e) {
        e.printStackTrace();
        return ResponseVO.buildFailure(e.getErrorCode(),e.getMessage());
    }
}
