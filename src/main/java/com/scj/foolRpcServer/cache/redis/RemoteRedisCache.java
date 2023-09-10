package com.scj.foolRpcServer.cache.redis;

import com.scj.foolRpcServer.constant.FRSConstant;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    @Autowired
    private RedisLua redisLua;

    /**
     * 信息存储
     * 不可重试
     * @param ip_port 下游主机的IP+PORT
     * @param className 服务类名称
     * @return 该 channel and ip 是否首次添加
     */
    public boolean saveByOriginRedis(String ip_port, String className, Channel channel){
        try {
            // 存储 class:ip_port
            boolean class_ip_port_save = redisOpe.cacheSet(FRSConstant.REDIS_PRE + FRSConstant.CLASS + className, ip_port);
            // 存储 channel_id:ip_port
            boolean channel_ip_port_save = redisOpe.cacheValue(FRSConstant.REDIS_PRE + FRSConstant.CHANNEL + channel.id(), ip_port);
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
     * 不保证全部成功
     * @param app 应用名
     * @param version 版本
     * @param ip_port 下游主机的IP+PORT
     * @param className 服务类名称
     * @return 该 channel and ip 是否首次添加
     */
    public boolean saveByLua(String app, String version
            , String ip_port, String className, Channel channel){
        String app_version = app + FRSConstant.UNDER_LINE + version;
        String class_version = className + FRSConstant.UNDER_LINE + version;
        try {
            // 存储 app:class
            boolean res1 = redisLua.hashSetIfNotExistWithExpireTime(
                    FRSConstant.REDIS_PRE + FRSConstant.APP + app_version
                    , class_version, String.valueOf(0) // 记录调用次数
                    , FRSConstant.EXPIRE_TIME.toString());

            // 存储 class:app
            boolean res2 = redisLua.hashSetIfNotExistWithExpireTime(
                    FRSConstant.REDIS_PRE + FRSConstant.CLASS
                    , class_version, app_version
                    , FRSConstant.EXPIRE_TIME.toString());

            // 存储数据到 app_ipList
            boolean res3 = redisLua.setAddIfNotExistWithExpireTime(
                    FRSConstant.REDIS_PRE + FRSConstant.IP_LIST + app_version
                    , ip_port, FRSConstant.EXPIRE_TIME.toString());

            // channel_ipPort 存储
            boolean res4 = redisLua.hashSetIfNotExistWithExpireTime(
                    FRSConstant.REDIS_PRE + FRSConstant.CHANNEL
                    , channel.id().asLongText(), ip_port
                    , FRSConstant.EXPIRE_TIME.toString());
            return res1 && res2 && res3 && res4;
        } catch (Throwable t){
            log.error("lua 插入失败", t);
            return false;
        }
    }

    /**
     * 信息存储byLua
     * 保证全部成功
     * 无返回值
     * @param app 应用名
     * @param version 版本
     * @param ip_port 下游主机的IP+PORT
     * @param className 服务类名称
     */
    public void saveMustSuccess(String app, String version
            , String ip_port, String className, Channel channel){
        FRSConstant.COMMON_EXECUTORS.submit(new Runnable() {
            private long timeGap = 1;
            @Override
            public void run() {
                // 存储 app:class
                boolean res = saveByLua(app, version, ip_port, className, channel);
                if (!res){
                    // 重新尝试
                    FRSConstant.COMMON_EXECUTORS.schedule(this, timeGap, TimeUnit.SECONDS);
                    timeGap *= 2;
                }
            }
        });
    }
}
