package com.sharpcrm.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存工具类
 * 特性：统一 key 前缀、过期时间、缓存穿透防护（空值缓存）、本地降级（Redis 不可用时内存兜底）
 */
@Component
public class RedisCache {

    /** 空值标记，用于缓存穿透防护 */
    private static final String NULL_VALUE = "__NULL__";

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    /** Redis 不可用时的本地降级缓存（ConcurrentHashMap 保证并发安全） */
    private final Map<String, Object> localCache = new ConcurrentHashMap<>();

    /** 本地缓存的过期时间戳（毫秒），与 Redis 的 TTL 语义对齐 */
    private final Map<String, Long> localExpireAt = new ConcurrentHashMap<>();

    private boolean redisAvailable() {
        if (redisTemplate == null) {
            return false;
        }
        try {
            redisTemplate.hasKey("__health__");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            if (redisAvailable()) {
                redisTemplate.opsForValue().set(key, value, timeout, unit);
            }
        } catch (Exception ignored) {
            // Redis 异常时静默降级
        }
        localCache.put(key, value);
        localExpireAt.put(key, System.currentTimeMillis() + unit.toMillis(timeout));
    }

    public void set(String key, Object value) {
        set(key, value, 30, TimeUnit.MINUTES);
    }

    /**
     * 获取缓存，找不到返回 null（含空值标记也返回 null）
     */
    public Object get(String key) {
        try {
            if (redisAvailable()) {
                Object value = redisTemplate.opsForValue().get(key);
                if (value != null) {
                    return NULL_VALUE.equals(value) ? null : value;
                }
            }
        } catch (Exception ignored) {
            // 降级读取本地缓存
        }
        Object local = localCache.get(key);
        if (local == null) {
            return null;
        }
        // 本地降级同样遵循 TTL：过期即清除，避免永久缓存脏数据
        Long expireAt = localExpireAt.get(key);
        if (expireAt != null && System.currentTimeMillis() > expireAt) {
            localCache.remove(key);
            localExpireAt.remove(key);
            return null;
        }
        return NULL_VALUE.equals(local) ? null : local;
    }

    /**
     * 缓存空值防穿透，短过期时间
     */
    public void setNull(String key, long timeout, TimeUnit unit) {
        try {
            if (redisAvailable()) {
                redisTemplate.opsForValue().set(key, NULL_VALUE, timeout, unit);
            }
        } catch (Exception ignored) {
            // 降级
        }
        localCache.put(key, NULL_VALUE);
        localExpireAt.put(key, System.currentTimeMillis() + unit.toMillis(timeout));
    }

    public void delete(String key) {
        try {
            if (redisAvailable()) {
                redisTemplate.delete(key);
            }
        } catch (Exception ignored) {
            // 降级
        }
        localCache.remove(key);
        localExpireAt.remove(key);
    }

    public boolean setIfAbsent(String key, Object value, long timeout, TimeUnit unit) {
        try {
            if (redisAvailable()) {
                Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
                return Boolean.TRUE.equals(ok);
            }
        } catch (Exception ignored) {
            // 降级
        }
        // 本地降级：简单并发控制
        if (localCache.containsKey(key)) {
            return false;
        }
        localCache.put(key, value);
        return true;
    }

    public Long increment(String key, long delta) {
        try {
            if (redisAvailable()) {
                return redisTemplate.opsForValue().increment(key, delta);
            }
        } catch (Exception ignored) {
            // 降级
        }
        Object local = localCache.getOrDefault(key, 0);
        long next = (local instanceof Number n ? n.longValue() : 0) + delta;
        localCache.put(key, next);
        return next;
    }
}
