package com.scj.foolRpcServer.cache;

import io.netty.channel.Channel;

/**
 * @author suchangjie.NANKE
 * @Title: RedisCache
 * @date 2023/8/27 19:38
 * @description 使用redis进行存储
 */
public class RedisCache implements FoolCache {

    @Override
    public boolean register(String appName, String fullClassName, String ip_port, String version, Channel channel) {
        return false;
    }

    @Override
    public String getService(String fullClassName, String version) {
        return null;
    }

    @Override
    public void remove(Channel channel) {

    }
}
