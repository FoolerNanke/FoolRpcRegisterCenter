package com.scj.foolRpcServer.cache.local;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.scj.foolRpcServer.constant.FRSConstant;

import io.netty.channel.Channel;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author suchangjie.NANKE
 * @Title: IpCache
 * @date 2023/8/26 12:58
 * @description 基于jdk map 实现的缓存
 */
public class LocalCaffeineCache {

    /**
     * key:app_version
     * value:class_version
     */
    private final Cache<String, Map<String, Integer>> app_class;

    /**
     * key:class_version
     * value: app_version
     */
    private final Cache<String, String> class_app;

    /**
     * key:app_version
     * value:ipList
     */
    private final Cache<String, IpList> app_ipList;

    /**
     * key:ip_Port
     * value:app_version
     */
    private final Cache<String, String> ipPort_app;

    /**
     * key:channel
     * value:ip_port
     */
    private final Cache<Channel, String> channel_ipPort;

    public LocalCaffeineCache(){
        // cache 3 天未出现访问则过期对应数据
        app_class = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofDays(3))
                .build();

        class_app = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofDays(3))
                .build();

        app_ipList = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofDays(3))
                .build();

        ipPort_app = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofDays(3))
                .build();

        channel_ipPort = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofDays(3))
                .build();
    }

    /**
     * 信息存储
     * @param app 应用名
     * @param version 版本
     * @param ip_port 下游主机的IP+PORT
     * @param className 服务类名称
     */
    public void save(String app, String version
            , String ip_port, String className, Channel channel){
        String app_version = app + FRSConstant.UNDER_LINE + version;
        String class_version = className + FRSConstant.UNDER_LINE + version;
        // 存储 app:class
        Map<String, Integer> map = app_class.get(app_version, (String s)->new ConcurrentHashMap<>());
        assert map != null;
        map.put(class_version, 0);

        // 存储class:app
        class_app.put(class_version, app_version);

        // 存储ipPort:app_version
        ipPort_app.put(ip_port, app_version);

        // 存储数据到 app_ipList
        IpList ipList = app_ipList.get(app_version, (String s)->new IpList());
        assert ipList != null;
        ipList.add(ip_port);

        // channel_ipPort 存储
        channel_ipPort.put(channel, ip_port);
    }

    /**
     * channel 在经过心跳检测后判断下游已经断开链接
     * 此时需要移除 channel ip app 等数据
     * @param channel WEB IO 通道
     */
    public void remove(Channel channel){
        // 移除 channel:ip_port
        String ip_port = channel_ipPort.getIfPresent(channel);
        channel_ipPort.invalidate(channel);
        /*
          从 ipList 中移除去 ip_port
         */
        if (ip_port ==null){
            return;
        }
        // 通过 ip_port 找到 app
        String app_version = ipPort_app.getIfPresent(ip_port);
        if (app_version == null){
            return;
        }
        // 通过 app 找到 ip_list
        IpList ipList = app_ipList.getIfPresent(app_version);
        if (ipList == null){
            return;
        }
        // 移除元素
        ipList.remove(ip_port);
    }

    public String getIp(String className, String version){
        String clazz_version = className  + FRSConstant.UNDER_LINE + version;
        // 通过 clazz_version 找到 app_version
        String app_version = class_app.getIfPresent(clazz_version);
        if (app_version == null) return "";
        // 通过 app_version 找到 ipList
        IpList ipList = app_ipList.getIfPresent(app_version);
        if (ipList == null) return "";
        return ipList.get();
    }
}
