package com.scj.foolRpcServer.cache.luaRedis;

import com.scj.foolRpcServer.constant.FRSConstant;
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

@Component
public class luaRedisOpe {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

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
