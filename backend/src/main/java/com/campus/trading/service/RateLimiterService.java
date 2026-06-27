package com.campus.trading.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 简易内存限流服务
 * 防止AI接口被恶意刷取,造成API费用损失
 */
@Slf4j
@Service
public class RateLimiterService {

    /**
     * 限流窗口记录
     */
    private static class Window {
        final long windowStart;
        final int count;

        Window(long windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    /**
     * 尝试获取请求许可
     *
     * @param key           限流标识 (如 "userId:apiName")
     * @param maxRequests   时间窗口内最大请求次数
     * @param windowSeconds 时间窗口长度 (秒)
     * @return true=放行, false=触发限流
     */
    public boolean tryAcquire(String key, int maxRequests, int windowSeconds) {
        long now = System.currentTimeMillis();
        long windowMillis = windowSeconds * 1000L;

        Window result = windows.compute(key, (k, existing) -> {
            // 窗口不存在或已过期 → 新建窗口,计数=1
            if (existing == null || now - existing.windowStart > windowMillis) {
                return new Window(now, 1);
            }
            // 窗口内已达上限 → 保持原样,不增加计数
            if (existing.count >= maxRequests) {
                return existing;
            }
            // 窗口内未达上限 → 计数+1
            return new Window(existing.windowStart, existing.count + 1);
        });

        if (result.count > maxRequests) {
            long waitSeconds = (windowMillis - (now - result.windowStart)) / 1000 + 1;
            log.warn("AI接口限流触发: key={}, 需等待约{}秒, 窗口内已请求{}次",
                    key, waitSeconds, result.count);
            return false;
        }
        return true;
    }

    /**
     * 获取剩余可用次数 (用于提示用户)
     */
    public int getRemaining(String key, int maxRequests, int windowSeconds) {
        long now = System.currentTimeMillis();
        long windowMillis = windowSeconds * 1000L;

        Window window = windows.get(key);
        if (window == null || now - window.windowStart > windowMillis) {
            return maxRequests;
        }
        return Math.max(0, maxRequests - window.count);
    }
}
