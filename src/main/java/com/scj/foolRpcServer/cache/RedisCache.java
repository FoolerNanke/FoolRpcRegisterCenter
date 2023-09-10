package com.scj.foolRpcServer.cache;

import com.scj.foolRpcServer.cache.redis.RemoteRedisCache;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author suchangjie.NANKE
 * @Title: RedisCache
 * @date 2023/8/27 19:38
 * @description 使用redis进行存储
 */
@Component("redisCache")
@Slf4j
public class RedisCache implements FoolCache {

    /**
     * redis缓存
     */
    @Autowired
    private RemoteRedisCache cache;

    @Override
    public boolean register(String appName, String fullClassName, String ip_port, String version, Channel channel) {
        // 线程池保证成功
        cache.saveMustSuccess(appName, fullClassName, ip_port, version, channel);
        return true;
    }

    @Override
    public String getService(String fullClassName, String version) {
        return null;
    }

    @Override
    public void remove(Channel channel) {

    }
}
