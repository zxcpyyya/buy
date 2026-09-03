package com.mall.common.exception;

/**
 * 业务异常类
 * 
 * 遵循阿里Java开发规范：
 * 1. 异常命名以Exception结尾
 * 2. 错误码采用5位字符串格式
 * 3. 继承RuntimeException
 * 
 * @author mall
 * @date 2024/01/01
 */
public class BusinessException extends RuntimeException {
    
    /**
     * 错误码
     */
    private final String errorCode;
    
    /**
     * 错误信息
     */
    private final String errorMessage;
    
    /**
     * 构造函数
     *
     * @param errorMessage 错误信息
     */
    public BusinessException(String errorMessage) {
        super(errorMessage);
        this.errorCode = "B0001";
        this.errorMessage = errorMessage;
    }
    
    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param errorMessage 错误信息
     */
    public BusinessException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
    
    /**
     * 获取错误码
     *
     * @return 错误码
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * 获取错误信息
     *
     * @return 错误信息
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}
