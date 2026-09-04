package com.mall.common.exception;

import lombok.Getter;

/**
 * 业务异常
 *
 * @author mall
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;
    private final String message;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(String message) {
        this(400, message);
    }

    public static BusinessException of(int code, String message) {
        return new BusinessException(code, message);
    }

    public static BusinessException of(String message) {
        return new BusinessException(message);
    }
}
