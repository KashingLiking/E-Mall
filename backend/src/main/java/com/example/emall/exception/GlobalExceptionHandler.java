package com.example.emall.exception;

import com.example.emall.vo.ResponseVO;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = EMallException.class)
    public ResponseVO<String> handleAIExternalException(EMallException e) {
        e.printStackTrace();
        return ResponseVO.buildFailure(e.getErrorCode(),e.getMessage());
    }
}
