package com.scj.foolRpcServer.cache.redis;

import com.scj.foolRpcServer.constant.FRSConstant;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author suchangjie.NANKE
 * @Title: RedisCache
 * @date 2023/8/29 22:53
 * @description 使用redis实现存储
 */

@Component
@Slf4j
public class RemoteRedisCache {

    @Autowired
    private RedisOpe redisOpe;

    /**
     * 信息存储
     * @param ip_port 下游主机的IP+PORT
     * @param className 服务类名称
     * @return 该 channel and ip 是否首次添加
     */
    public boolean saveByOriginRedis(String ip_port, String className, Channel channel){
        try {
            // 存储 class:ip_port
            boolean class_ip_port_save = redisOpe.cacheSet(FRSConstant.REDIS_PRE + FRSConstant.CLASS + className, ip_port);
            // 存储 channel_id:ip_port
            boolean channel_ip_port_save = redisOpe.cacheValue(FRSConstant.REDIS_PRE + FRSConstant.CHANNEL + channel.id().toString(), ip_port);
            // 修改失效队列
            redisOpe.rmSetValue(FRSConstant.REDIS_PRE + FRSConstant.EXPIRE_SET, ip_port);
            return class_ip_port_save && channel_ip_port_save;
        } catch (Throwable t){
            log.error("origin redis 插入失败", t);
            return false;
        }
    }

    /**
     * 信息存储byLua
     * 保证全部成功
     * 无返回值
     * @param ip_port 下游主机的IP+PORT
     * @param className 服务类名称
     */
    public void saveMustSuccess(String ip_port, String className, Channel channel){
        FRSConstant.COMMON_EXECUTORS.submit(new Runnable() {
            private long timeGap = 1;
            @Override
            public void run() {
                // 存储 app:class
                boolean res = saveByOriginRedis(ip_port, className, channel);
                if (!res){
                    // 重新尝试
                    FRSConstant.COMMON_EXECUTORS.schedule(this, timeGap, TimeUnit.SECONDS);
                    timeGap *= 2;
                }
            }
        });
    }

    /**
     * 获取可用IP list
     * @param className 类名
     * @return ip set
     */
    public List<String> getIps(String className) {
        Set<Object> set = redisOpe.getSetWithOutKey2(className, FRSConstant.REDIS_PRE + FRSConstant.EXPIRE_SET);
        if (set == null || set.isEmpty()) {
            return null;
        }
        List<String> ips = new ArrayList<>();
        for (Object o : set) {
            ips.add(o.toString());
        }
        return ips;
    }

    /**
     * 移除通道
     * @param channelId 通道ID
     */
    public void remove(String channelId) {
        // 获取关联通道
        String ip_port = (String) redisOpe.getValue(channelId);
        // 删除通道
        redisOpe.rmKey(channelId);
        // 失效果队列中添加该值
        if (ip_port != null){
            redisOpe.cacheSet(FRSConstant.REDIS_PRE + FRSConstant.EXPIRE_SET
                    , ip_port);
        }
    }
}
