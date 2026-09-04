package com.mall.common.result;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一响应结果封装类
 * 
 * @author xiu
 * @date 2026/09/03
 */
@Data
public class Result<T> implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 状态码
     */
    private Integer code;
    
    /**
     * 错误码
     */
    private String errorCode;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 数据
     */
    private T data;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    public Result() {
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        return result;
    }
    
    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }
    
    /**
     * 成功响应（带消息和数据）
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }
    
    /**
     * 失败响应
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }
    
    /**
     * 失败响应（带错误码）
     */
    public static <T> Result<T> error(String errorCode, String errorMessage) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setErrorCode(errorCode);
        result.setErrorMessage(errorMessage);
        result.setMessage(errorMessage);
        return result;
    }
    
    /**
     * 失败响应（带错误码和消息）
     */
    public static <T> Result<T> error(Integer code, String errorCode, String errorMessage) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setErrorCode(errorCode);
        result.setErrorMessage(errorMessage);
        result.setMessage(errorMessage);
        return result;
    }
    
    /**
     * 自定义状态码成功响应
     */
    public static <T> Result<T> success(Integer code, String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }
}
