package com.example.tomatomall.vo;

import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class  ResponseVO<T> implements Serializable {

    private String code;

    private String msg;

    private T data;

    public static <T> ResponseVO<T> buildSuccess(T result) {
        return new ResponseVO<T>("200", null, result);
    }

    public static <T> ResponseVO<T> buildFailure(String code, String msg) {
        return new ResponseVO<T>(code, msg, null);
    }
}
