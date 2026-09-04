package com.mall.common.exception;

import com.mall.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 
 * 
 * 1. 统一异常处理
 * 2. 区分异常类型
 * 3. 日志记录完善
 * 4. 返回统一格式
 * 
 * @author xiu
 * @date 2026/09/03
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常, errorCode={}, errorMessage={}", e.getErrorCode(), e.getErrorMessage());
        return Result.error(e.getErrorCode(), e.getErrorMessage());
    }
    
    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        
        log.warn("参数校验失败, errorMessage={}", errorMessage);
        return Result.error("A0410", errorMessage);
    }
    
    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBindException(BindException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        
        log.warn("参数绑定失败, errorMessage={}", errorMessage);
        return Result.error("A0410", errorMessage);
    }
    
    /**
     * 处理404异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<?> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("请求路径不存在, path={}", e.getRequestURL());
        return Result.error("A0404", "请求路径不存在");
    }
    
    /**
     * 处理请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<?> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持, method={}", e.getMethod());
        return Result.error("A0405", "请求方法不支持");
    }
    
    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        log.error("系统异常, errorMessage={}", e.getMessage(), e);
        return Result.error("B0001", "系统繁忙，请稍后重试");
    }
    
    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleNullPointerException(NullPointerException e) {
        log.error("空指针异常, errorMessage={}", e.getMessage(), e);
        return Result.error("B0310", "系统内部错误");
    }
    
    /**
     * 处理数组越界异常
     */
    @ExceptionHandler(ArrayIndexOutOfBoundsException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleArrayIndexOutOfBoundsException(ArrayIndexOutOfBoundsException e) {
        log.error("数组越界异常, errorMessage={}", e.getMessage(), e);
        return Result.error("B0310", "系统内部错误");
    }
    
    /**
     * 处理类型转换异常
     */
    @ExceptionHandler(ClassCastException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleClassCastException(ClassCastException e) {
        log.error("类型转换异常, errorMessage={}", e.getMessage(), e);
        return Result.error("B0310", "系统内部错误");
    }
    
    /**
     * 处理算术异常
     */
    @ExceptionHandler(ArithmeticException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleArithmeticException(ArithmeticException e) {
        log.error("算术异常, errorMessage={}", e.getMessage(), e);
        return Result.error("B0310", "系统内部错误");
    }
}
