package com.scj.foolRpcServer.cache.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
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
            Boolean res = redisTemplate.opsForValue().setIfAbsent(key, value);
            if (Boolean.FALSE.equals(res)) return false;
            if (time > 0) {
                redisTemplate.expire(key, time, timeUnit);
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
     * 移除一个键
     * @param key
     */
    public boolean rmKey(String key){
        try {
            return Boolean.TRUE.equals(redisTemplate.delete(key));
        } catch (Throwable e){
            log.error("删除缓存[{}]失败, error = {}", key, e.getMessage());
        }
        return false;
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

    /**
     *
     * @param mapName
     * @param key
     * @param value
     * @return
     */
    public boolean cacheMap(String mapName, String key, String value){
        try {
            redisTemplate.boundHashOps(mapName).put(key, value);
            return true;
        } catch (Throwable e) {
            log.error("缓存map[" + mapName + ":" + key + "]失败, value[" + value + "] " + e.getMessage());
        }
        return false;
    }

    /**
     *
     * @param key
     * @param value
     * @return
     */
    public boolean cacheZSet(String key, String value){
        try {
            redisTemplate.boundZSetOps(key).add(value, 0);
            return true;
        } catch (Throwable e) {
            log.error("缓存map[" + key + "]失败, value[" + value + "] " + e.getMessage());
        }
        return false;
    }

    /**
     * 无过期时间set存储
     * @param key
     * @param value
     * @return
     */
    public boolean cacheSet(String key, String value){
        try {
            Boolean member = redisTemplate.boundSetOps(key).isMember(value);
            if (Boolean.TRUE.equals(member)) return true;
            Long add = redisTemplate.boundSetOps(key).add(value, 0);
            return add != null && add == 1;
        } catch (Throwable e) {
            log.error("缓存map[" + key + "]失败, value[" + value + "] " + e.getMessage());
        }
        return false;
    }

    /**
     * set 删除值
     * @param key
     * @param value
     * @return
     */
    public boolean rmSetValue(String key, String value){
        try {
            redisTemplate.boundSetOps(key).remove(value);
            return true;
        } catch (Throwable e) {
            log.error("缓存map[" + key + "]失败, value[" + value + "] " + e.getMessage());
        }
        return false;
    }

    /**
     * set 获取列表
     * @param key 键
     * @return 列表
     */
    public Set<Object> getSetWithOutKey2(String key, String key2){
        try {
            return redisTemplate.boundSetOps(key).diff(key2);
        } catch (Throwable e) {
            log.error("缓存set[" + key + "]失败" + e.getMessage());
        }
        return null;
    }
}
