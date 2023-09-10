package com.scj.foolRpcServer.cache.luaRedis;

import com.scj.foolRpcServer.constant.FRSConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
@Slf4j
public class RedisLua {

    @Autowired
    private RedisTemplate<String, Object> redisTemplateV2;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

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
        return Objects.equals(redisTemplateV2.execute(redisScript, keys), 1);
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
        return Objects.equals(redisTemplateV2.execute(redisScript, keys), 1);
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

    /**
     * string 设值
     * @param key 键
     * @param value 值
     * @param expireTime 过期时间
     * @return
     */
    public boolean stringSetWithExpireTime(String key, String value, String expireTime) {
        DefaultRedisScript<Integer> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/stringAdd.lua")));
        redisScript.setResultType(Integer.class);
        List<String> keys = Arrays.asList(key, value, expireTime);
        return Objects.equals(redisTemplateV2.execute(redisScript, keys), 1);
    }

    /**
     * string 设值
     * @param key 键
     * @param value 值
     * @return
     */
    public boolean stringSetWithOutExpireTime(String key, String value){
        return stringSetWithExpireTime(key, value, noExpireTime);
    }

    /**
     * 类注册
     * @param className
     * @param ip_port
     * @param channel_id
     * @return
     * local className = KEYS[1]
     * local ip_port = KEYS[2]
     * local channel_id = KEYS[3]
     * local expireSet = KEYS[4]
     * local expireTime = KEYS[5]
     */
    public boolean registerClassByLua(String className, String ip_port, String channel_id){
        DefaultRedisScript<Integer> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/registerClass.lua")));
        redisScript.setResultType(Integer.class);
        List<String> keys = Arrays.asList(
                FRSConstant.REDIS_PRE + FRSConstant.CLASS + className,
                ip_port,
                FRSConstant.REDIS_PRE + FRSConstant.CHANNEL + channel_id,
                FRSConstant.REDIS_PRE + FRSConstant.EXPIRE_SET,
                FRSConstant.EXPIRE_TIME.toString());
        return Objects.equals(redisTemplateV2.execute(redisScript, keys), 1);
    }

    /**
     * 获取可用IP列表
     * 获取时会将删除ip列表中的失效ip
     * 并回写到redis宏
     * @param className      类名
     * @param expireListName 过期ip集合名
     * @return 可用IP列表
     */
    public List<String> getIps(String className, String expireListName) {
        DefaultRedisScript<List> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/getIps.lua")));
        redisScript.setResultType(List.class);
        List<String> keys = Arrays.asList(className, expireListName);
        return stringRedisTemplate.execute(redisScript, keys);
    }

    public void test() {
        DefaultRedisScript<Integer> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/removeIpPort.lua")));
        redisScript.setResultType(Integer.class);
        List<String> keys = Arrays.asList("channel_id", "expireSet");
        Integer execute = stringRedisTemplate.execute(redisScript, new StringRedisSerializer(),new GenericToStringSerializer<>(Integer.class),keys);
        System.out.println(execute);
    }
}
