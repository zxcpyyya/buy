package com.mall.config;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

import java.io.IOException;

/**
 * 可重复读取的ServletInputStream
 * 
 * <p>用于XSS过滤时缓存请求体，支持多次读取
 *
 * @author xiu
 */
public class CachedBodyServletInputStream extends ServletInputStream {

    private final byte[] cachedBody;
    private int position = 0;

    public CachedBodyServletInputStream(byte[] cachedBody) {
        this.cachedBody = cachedBody;
    }

    @Override
    public boolean isFinished() {
        return position >= cachedBody.length;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        throw new UnsupportedOperationException("setReadListener is not supported");
    }

    @Override
    public int read() throws IOException {
        if (position >= cachedBody.length) {
            return -1;
        }
        return cachedBody[position++] & 0xff;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (position >= cachedBody.length) {
            return -1;
        }

        int remaining = cachedBody.length - position;
        int length = Math.min(len, remaining);
        System.arraycopy(cachedBody, position, b, off, length);
        position += length;
        return length;
    }
}
