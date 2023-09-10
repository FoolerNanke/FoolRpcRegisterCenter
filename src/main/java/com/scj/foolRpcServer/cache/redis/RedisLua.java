package com.scj.foolRpcServer.cache.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class RedisLua {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final String noExpireTime = "0";

    /**
     * 不存在则不插入
     * lua 原子操作
     * @param mapKey map 键
     * @param key 值 键
     * @param value 值
     * @param expireTime = 0 表示不设过期时间 (单位为秒)
     * @return success = 1 fail = 0
     * 设值成功 但过期时间设置失败依旧返回成功
     *
     */
    public boolean hashSetIfNotExistWithExpireTime(String mapKey, String key, String value, String expireTime){
        DefaultRedisScript<Integer> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/hsetnx.lua")));
        redisScript.setResultType(Integer.class);
        List<String> keys = Arrays.asList(mapKey, key, value, expireTime);
        return Objects.equals(redisTemplate.execute(redisScript, keys), 1);
    }

    /**
     * 存在则不插入
     * lua 原子操作
     * @param mapKey map 键
     * @param key 值 键
     * @param value 值
     * @return success = 1 fail = 0
     * 设值成功 但过期时间设置失败依旧返回成功
     *
     */
    public boolean hashSetIfNotExistWithOutExpireTime(String mapKey, String key, String value){
        return hashSetIfNotExistWithExpireTime(mapKey, key, value, noExpireTime);
    }

    /**
     * 不存在则插入
     * lua 原子操作
     * @param key 键
     * @param value 值
     * @param expireTime 过期时间
     * @return success = 1 fail = 0
     * 设值成功 但过期时间设置失败依旧返回成功
     */
    public boolean setAddIfNotExistWithExpireTime(String key, String value, String expireTime){
        DefaultRedisScript<Integer> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/sadd.lua")));
        redisScript.setResultType(Integer.class);
        List<String> keys = Arrays.asList(key, value, expireTime);
        return Objects.equals(redisTemplate.execute(redisScript, keys), 1);
    }

    /**
     * 存在则不插入
     * lua 原子操作
     * @param key 值 键
     * @param value 值
     * @return success = 1 fail = 0
     * 设值成功 但过期时间设置失败依旧返回成功
     *
     */
    public boolean setAddIfNotExistWithOutExpireTime(String key, String value){
        return setAddIfNotExistWithExpireTime(key, value, noExpireTime);
    }

}
