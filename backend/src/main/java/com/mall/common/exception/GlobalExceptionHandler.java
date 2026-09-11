package com.mall.common.exception;

import com.mall.common.ratelimit.TokenBucketLimiter;
import com.mall.common.xss.XssUtil;
import com.mall.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 
 * <p>统一处理各类异常，返回标准化错误响应
 *
 * @author xiu
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: path={}, code={}, message={}", 
            request.getRequestURI(), e.getCode(), e.getMessage());

        int status = e.getCode();
        // 如果是401/403，保持原状态码
        if (status != 401 && status != 403 && status >= 200 && status < 600) {
            return ResponseEntity.status(status)
                .body(Result.error(String.valueOf(status), e.getMessage()));
        }

        // 业务错误码映射到HTTP状态码
        HttpStatus httpStatus = mapToHttpStatus(e.getCode());
        return ResponseEntity.status(httpStatus)
            .body(Result.error(String.valueOf(e.getCode()), e.getMessage()));
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String errors = e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining("; "));

        log.warn("参数校验失败: {}", errors);

        // 检查是否包含XSS攻击
        if (XssUtil.containsXss(errors)) {
            log.error("检测到XSS攻击: {}", errors);
            return Result.error("A0430", "输入包含非法字符");
        }

        return Result.error("A0401", errors);
    }

    /**
     * 参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        String errors = e.getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining("; "));

        log.warn("参数绑定失败: {}", errors);
        return Result.error("A0401", errors);
    }

    /**
     * 参数类型不匹配
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String paramName = e.getName();
        String expectedType = e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown";

        log.warn("参数类型不匹配: param={}, expected={}", paramName, expectedType);

        // 检查是否包含XSS攻击
        if (XssUtil.containsXss(paramName)) {
            log.error("检测到XSS攻击: {}", paramName);
            return Result.error("A0430", "输入包含非法字符");
        }

        return Result.error("A0402", String.format("参数'%s'类型错误，期望%s", paramName, expectedType));
    }

    /**
     * 缺少必需参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMissingParameterException(MissingServletRequestParameterException e) {
        String paramName = e.getParameterName();

        log.warn("缺少必需参数: {}", paramName);
        return Result.error("A0410", String.format("缺少必需参数: %s", paramName));
    }

    /**
     * 请求方法不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<Void> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持: {}", e.getMessage());
        return Result.error("A0404", "不支持的请求方法: " + e.getMethod());
    }

    /**
     * 404未找到
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("接口不存在: {}", e.getRequestURL());
        return Result.error("A0404", "接口不存在");
    }

    /**
     * 限流异常
     */
    @ExceptionHandler(RateLimitException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public Result<Void> handleRateLimitException(RateLimitException e, HttpServletRequest request) {
        String clientIp = getClientIp(request);
        long remaining = TokenBucketLimiter.getRemainingBlockTime(clientIp);

        log.warn("触发限流: ip={}, path={}, remaining={}s", 
            clientIp, request.getRequestURI(), remaining);

        return Result.error("B0211", String.format("请求过于频繁，请%d秒后再试", remaining > 0 ? remaining : 60));
    }

    /**
     * XSS攻击异常
     */
    @ExceptionHandler(XssAttackException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleXssAttackException(XssAttackException e) {
        log.error("检测到XSS攻击: {}", e.getMessage());

        // 记录攻击日志（可用于风控分析）
        logSecurityEvent("XSS_ATTACK", e.getMessage());

        return Result.error("A0430", "输入包含非法字符，已被过滤");
    }

    /**
     * 登录异常（防爆破）
     */
    @ExceptionHandler(LoginFailedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleLoginFailedException(LoginFailedException e, HttpServletRequest request) {
        String clientIp = getClientIp(request);

        // 记录失败次数
        TokenBucketLimiter.recordFailLogin(clientIp, 5, 600);

        log.warn("登录失败: ip={}, message={}, failCount={}", 
            clientIp, e.getMessage(), TokenBucketLimiter.getRemainingBlockTime(clientIp));

        return Result.error("A0210", e.getMessage());
    }

    /**
     * IP被封禁异常
     */
    @ExceptionHandler(IpBlockedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleIpBlockedException(IpBlockedException e, HttpServletRequest request) {
        String clientIp = getClientIp(request);
        long remaining = TokenBucketLimiter.getRemainingBlockTime(clientIp);

        log.warn("IP被封禁: ip={}, remaining={}s", clientIp, remaining);

        return Result.error("A0301", String.format("访问过于频繁，请%d秒后再试", remaining));
    }

    /**
     * SQL注入异常
     */
    @ExceptionHandler(SqlInjectionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleSqlInjectionException(SqlInjectionException e) {
        log.error("检测到SQL注入攻击: {}", e.getMessage());

        // 记录攻击日志
        logSecurityEvent("SQL_INJECTION", e.getMessage());

        return Result.error("A0430", "请求参数非法");
    }

    /**
     * 其他未处理异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: path={}, error={}", request.getRequestURI(), e.getMessage(), e);

        return Result.error("B0001", "系统繁忙，请稍后再试");
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 映射业务错误码到HTTP状态码
     */
    private HttpStatus mapToHttpStatus(int code) {
        if (code >= 400 && code < 500) {
            return HttpStatus.BAD_REQUEST;
        } else if (code >= 500) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.OK;
    }

    /**
     * 记录安全事件（可用于风控系统对接）
     */
    private void logSecurityEvent(String eventType, String details) {
        // 可以接入风控系统、SIEM等
        log.warn("SECURITY_EVENT: type={}, details={}, timestamp={}", 
            eventType, details, System.currentTimeMillis());
    }

    // ========== 自定义异常类 ==========

    /**
     * 限流异常
     */
    public static class RateLimitException extends RuntimeException {
        public RateLimitException(String message) {
            super(message);
        }
    }

    /**
     * XSS攻击异常
     */
    public static class XssAttackException extends RuntimeException {
        public XssAttackException(String message) {
            super(message);
        }
    }

    /**
     * 登录失败异常
     */
    public static class LoginFailedException extends RuntimeException {
        public LoginFailedException(String message) {
            super(message);
        }
    }

    /**
     * IP封禁异常
     */
    public static class IpBlockedException extends RuntimeException {
        public IpBlockedException(String message) {
            super(message);
        }
    }

    /**
     * SQL注入异常
     */
    public static class SqlInjectionException extends RuntimeException {
        public SqlInjectionException(String message) {
            super(message);
        }
    }
}
