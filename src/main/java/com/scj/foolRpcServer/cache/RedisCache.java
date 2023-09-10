package com.scj.foolRpcServer.cache;

import com.scj.foolRpcServer.cache.redis.RemoteRedisCache;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

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
        cache.saveMustSuccess(ip_port, fullClassName, channel);
        return true;
    }

    /**
     * 获取ip地址
     * @param className 全类名
     * @param version 版本号
     * @return 目前默认返回第一个ip
     */
    @Override
    public String getService(String className, String version) {
        List<String> ips = cache.getIps(className);
        if (ips == null || ips.isEmpty()) {
            return "";
        }
        return ips.get(0);
    }

    /**
     * 心跳检测不合格
     * @param channel WEB IO 通道
     */
    @Override
    public void remove(Channel channel) {
        cache.remove(channel.id().toString());
    }
}
