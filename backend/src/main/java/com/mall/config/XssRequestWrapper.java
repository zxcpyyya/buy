package com.mall.config;

import com.mall.common.xss.XssUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

/**
 * XSS防护请求包装器
 * 
 * <p>包装HttpServletRequest，过滤所有输入参数
 *
 * @author xiu
 */
public class XssRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] cachedBody;

    public XssRequestWrapper(HttpServletRequest request) {
        super(request);
        // 缓存请求体（只能读取一次）
        try {
            this.cachedBody = StreamUtils.copyToByteArray(request.getInputStream());
        } catch (Exception e) {
            throw new RuntimeException("Failed to cache request body", e);
        }
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) {
            return null;
        }

        String[] filteredValues = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            filteredValues[i] = filterValue(values[i]);
        }
        return filteredValues;
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return filterValue(value);
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        return filterValue(value);
    }

    @Override
    public jakarta.servlet.ServletInputStream getInputStream() {
        return new CachedBodyServletInputStream(this.cachedBody);
    }

    /**
     * 获取请求体（已过滤XSS）
     */
    public String getCachedBody() {
        String body = new String(cachedBody, StandardCharsets.UTF_8);
        return filterValue(body);
    }

    /**
     * 过滤单个值
     */
    private String filterValue(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        // 检查是否包含XSS
        if (XssUtil.containsXss(value)) {
            return XssUtil.filter(value);
        }

        return value;
    }
}
