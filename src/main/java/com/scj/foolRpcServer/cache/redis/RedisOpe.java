package com.scj.foolRpcServer.cache.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author suchangjie.NANKE
 * @Title: RedisTemplate
 * @date 2023/8/29 23:01
 * @description redis操作工具类
 */
@Component
@Slf4j
public class RedisOpe {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 缓存value
     * 带超时时间
     * @param key 键
     * @param value 值
     * @param time 过期时间
     * @param timeUnit 时间单位
     * @return 缓存是否成功
     */
    public boolean cacheValueWithExpireTime(String key, Object value, long time, TimeUnit timeUnit) {
        try {
            if (time > 0) {
                // 如果有设置超时时间的话
                redisTemplate.opsForValue().set(key, value, time, timeUnit);
            } else {
                redisTemplate.opsForValue().set(key, value);
            }
            return true;
        } catch (Throwable e) {
            log.error("缓存[" + key + "]失败, value[" + value + "] " + e.getMessage());
        }
        return false;
    }

    /**
     * 缓存value
     * 无超时时间
     * @param key 键
     * @param value 值
     * @return 缓存是否成功
     */
    public boolean cacheValue(String key, Object value) {
        return cacheValueWithExpireTime(key, value, 0, null);
    }

    /**
     * 获取缓存
     * @param key 键
     * @return 值
     */
    public Object getValue(String key){
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Throwable e){
            log.error("获取缓存[{}]失败, error = {}", key, e.getMessage());
        }
        return null;
    }

    /**
     * list 添加
     * @param key 键
     * @param value 值
     * @return 是否成功
     */
    public boolean cacheList(String key, Object value){
        try {
            redisTemplate.boundListOps(key).leftPush(value);
            return true;
        } catch (Throwable e) {
            log.error("缓存List[" + key + "]失败, value[" + value + "] " + e.getMessage());
        }
        return false;
    }
}
