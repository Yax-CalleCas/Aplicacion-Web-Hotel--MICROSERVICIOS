package com.hotel.hotel.service.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final String code;
    private final int status;

    public BusinessException(String code, int status) {
        super(code);
        this.code = code;
        this.status = status;
    }

    public BusinessException(String code, String message, int status) {
        super(message);
        this.code = code;
        this.status = status;
    }
}