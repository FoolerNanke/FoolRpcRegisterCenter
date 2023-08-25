package com.scj.foolRpcServer.cache;

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
     * @param version 版本
     * @param gap 时间间隔
     * @return 是否注册成功
     */
    boolean register(String appName, String fullClassName, String ip, String version, long gap);

    /**
     * 获取下游服务地址
     * @param fullClassName 全类名
     * @param version 版本号
     * @param gap 时间间隔
     * @return ip
     */
    String getService(String fullClassName, String version, long gap);
}
