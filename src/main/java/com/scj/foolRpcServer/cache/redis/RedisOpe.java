package com.scj.foolRpcServer.cache.redis;

import com.scj.foolRpcServer.constant.FRSConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
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
            redisTemplate.boundSetOps(key).add(value, 0);
            return true;
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
     * 信息存储
     * @param app 应用名
     * @param version 版本
     * @param ip_port 下游主机的IP+PORT
     * @param className 服务类名称
     * @param channel_id 通道ID
     * @return 是否存储成功
     */
    public boolean save(String app, String version
            , String ip_port, String className, String channel_id){
        String app_version = app + FRSConstant.UNDER_LINE + version;
        String class_version = className + FRSConstant.UNDER_LINE + version;

        DefaultRedisScript<String> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/save.lua")));
        redisScript.setResultType(String.class);
        List<String> keys = Arrays.asList(
                FRSConstant.REDIS_PRE + FRSConstant.APP + app_version,
                class_version,
                "0", // 本版本数据被调用的总次数
                FRSConstant.REDIS_PRE + FRSConstant.CLASS,
                class_version,
                app_version,
                FRSConstant.REDIS_PRE + FRSConstant.IP_LIST + app_version,
                ip_port,
                FRSConstant.REDIS_PRE + FRSConstant.CHANNEL,
                channel_id,
                ip_port
        );
        System.out.println(redisTemplate.execute(redisScript, keys));
        return true;
    }

    public boolean test(){
        DefaultRedisScript<Boolean> redisScript = new DefaultRedisScript<>();
        String input = "redis.call('SET', KEYS[1], KEYS[2])";
        // 从给定的字符串中获取字节序列
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);

        // 从输入缓冲区创建一个 `ByteArrayInputStream`
        InputStream in;
        in = new ByteArrayInputStream(bytes);

        // 做一点事

        //关闭输入流
        redisScript.setScriptSource(new ResourceScriptSource(new ByteArrayResource(input.getBytes(StandardCharsets.UTF_8))));
        redisScript.setResultType(Boolean.class);
        List<String> keys = Arrays.asList(
                "keyName", "values"
        );
        return Boolean.TRUE.equals(redisTemplate.execute(redisScript, keys));
    }


}
