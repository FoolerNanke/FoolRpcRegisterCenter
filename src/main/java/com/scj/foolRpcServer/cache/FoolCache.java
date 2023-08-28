package com.scj.foolRpcServer.cache;

import io.netty.channel.Channel;

/**
 * @author suchangjie.NANKE
 * @Title: Cache
 * @date 2023/8/25 19:56
 * @description 缓存，存储所有信息
 */
public interface FoolCache {

    /**
     * 下游将自己的服务注册到注册中心
     * @param appName 应用名称
     * @param fullClassName 注册类信息
     * @param ip_port ip_port
     * @param version 版本
     * @param channel webIO链接通道
     * @return 该 channel and ip 是否首次添加
     */
    boolean register(String appName, String fullClassName
            , String ip_port, String version, Channel channel);

    /**
     * 获取下游服务地址
     * @param fullClassName 全类名
     * @param version 版本号
     * @return ip
     */
    String getService(String fullClassName, String version);

    /**
     * channel 不通 移除对应 ip
     * @param channel
     */
    void remove(Channel channel);
}
