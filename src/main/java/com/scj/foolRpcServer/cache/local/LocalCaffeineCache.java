package com.scj.foolRpcServer.cache.local;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.netty.channel.Channel;
import org.springframework.stereotype.Component;
import java.time.Duration;


/**
 * @author suchangjie.NANKE
 * @Title: IpCache
 * @date 2023/8/26 12:58
 * @description 基于jdk map 实现的缓存
 */
@Component
public class LocalCaffeineCache {

    /**
     * key:class
     * value:ip_list
     */
    private final Cache<String, IpList> class_ip_list;

    /**
     * key:失效ip
     */
    private final Cache<String, String> expire_ips;

    /**
     * key:channel
     * value:ip_port
     */
    private final Cache<Channel, String> channel_ipPort;

    public LocalCaffeineCache(){
        // cache 3 天未出现访问则过期对应数据
        class_ip_list = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofDays(3))
                .build();

        // 不设过期时间
        expire_ips = Caffeine.newBuilder()
                .build();

        // cache 3 天未出现访问则过期对应数据
        channel_ipPort = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofDays(3))
                .build();
    }

    /**
     * 信息存储
     * @param ip_port 下游主机的IP+PORT
     * @param className 服务类名称
     * @return 该 channel and ip 是否首次添加
     */
    public boolean save(String ip_port, String className, Channel channel){
        // 存储 class:ips
        IpList iplist = class_ip_list.get(className, (String s) -> new IpList());
        assert iplist != null;
        iplist.add(ip_port);

        // 存储channel:ipPort
        channel_ipPort.put(channel, ip_port);
        // 从失效队列中删除该数据
        expire_ips.invalidate(ip_port);
        return true;
    }

    /**
     * channel 在经过心跳检测后判断下游已经断开链接
     * 此时需要移除 channel ip
     * @param channel WEB IO 通道
     */
    public void remove(Channel channel){
        // 移除 channel:ip_port
        String ip_port = channel_ipPort.getIfPresent(channel);
        channel_ipPort.invalidate(channel);
        /*
         * 加入失效ip列表
         */
        if (ip_port != null) {
            expire_ips.put(ip_port, "");
        }
    }

    public String getIp(String className){
        IpList ipList = class_ip_list.getIfPresent(className);
        if (ipList == null) {
            return "";
        }
        return ipList.get();
    }
}
