package com.mall.interceptor;

import com.mall.common.annotation.OperationLog;
import com.mall.context.AdminContext;
import com.mall.entity.SysOperationLogDO;
import com.mall.mapper.SysOperationLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * 操作日志拦截器
 * 自动记录带有 @OperationLog 注解的方法调用
 *
 * @author mall
 */
@Slf4j
public class OperationLogInterceptor implements HandlerInterceptor {

    private final SysOperationLogMapper operationLogMapper;
    private final ThreadPoolTaskExecutor taskExecutor;

    public OperationLogInterceptor() {
        this.operationLogMapper = null; // 延迟初始化
        this.taskExecutor = null;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 异步记录日志
        try {
            recordOperationLog(request, response, handler, ex);
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        }
    }

    private void recordOperationLog(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (!(handler instanceof org.springframework.web.method.HandlerMethod handlerMethod)) {
            return;
        }

        OperationLog operationLog = handlerMethod.getMethodAnnotation(OperationLog.class);
        if (operationLog == null) {
            return;
        }

        Long startTime = (Long) request.getAttribute("startTime");
        long executeTime = startTime != null ? System.currentTimeMillis() - startTime : 0;

        SysOperationLogDO logDO = new SysOperationLogDO();
        logDO.setUserId(AdminContext.getAdminId());
        logDO.setUsername(AdminContext.getUsername());
        logDO.setOperation(operationLog.operation());
        logDO.setModule(operationLog.module());
        logDO.setDescription(operationLog.description());
        logDO.setRequestMethod(request.getMethod());
        logDO.setRequestUrl(request.getRequestURI());
        logDO.setIpAddress(getIpAddress(request));
        logDO.setExecuteTime((int) executeTime);
        logDO.setStatus(ex != null ? 0 : 1);
        logDO.setErrorMessage(ex != null ? ex.getMessage() : null);

        if (operationLog.logParams()) {
            logDO.setRequestParams(request.getQueryString());
        }

        // 异步保存日志
        if (operationLogMapper != null) {
            CompletableFuture.runAsync(() -> {
                try {
                    operationLogMapper.insert(logDO);
                } catch (Exception e) {
                    OperationLogInterceptor.log.error("保存操作日志失败", e);
                }
            }, taskExecutor);
        }
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
