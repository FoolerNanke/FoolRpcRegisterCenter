package com.scj.foolRpcServer.cache.luaRedis;

import com.scj.foolRpcServer.constant.FRSConstant;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RemoteLuaCache {

    @Autowired
    private RedisLua redisLua;

    /**
     * 信息存储byLua
     * 不保证全部成功
     * @param ip_port 下游主机的IP+PORT
     * @param className 服务类名称
     * @return 该 channel and ip 是否首次添加
     */
    public boolean register(String className, String ip_port, Channel channel){
        try {
            return redisLua.registerClassByLua(className, ip_port, channel.id().toString());
        } catch (Throwable t){
            log.error("注册Class信息失败 className = {}, ip_port = {}, error = {}"
                    , className, ip_port, t.getMessage());
            return false;
        }
    }

    /**
     * 信息存储byLua
     * 线程池保证全部成功
     * @param ip_port 下游主机的IP+PORT
     * @param className 服务类名称
     */
    public void registerMustSuccess(String className, String ip_port, Channel channel){
        FRSConstant.COMMON_EXECUTORS.submit(new Runnable() {
            private long timeGap = 1;
            @Override
            public void run() {
                // 存储 app:class
                boolean res = register(className, ip_port, channel);
                if (!res){
                    // 重新尝试
                    FRSConstant.COMMON_EXECUTORS.schedule(this, timeGap, TimeUnit.SECONDS);
                    timeGap *= 2;
                }
            }
        });
    }

    /**
     * 获取可用ip列表
     * @param className 类名
     * @return ip列表
     */
    public List<String> getIps(String className){
        try {
            return redisLua.getIps(className,
                    FRSConstant.REDIS_PRE + FRSConstant.EXPIRE_SET);
        } catch (Throwable t){
            log.error("获取有效ip列表失败, className = {}, error = {}", className, t.getMessage());
            return null;
        }
    }

    /**
     * 移除channel绑定的ip
     * @param channel_id channel id
     * @return 是否成功
     */
    public boolean remove(String channel_id) {
        try {
            return redisLua.removeIp(channel_id,
                    FRSConstant.REDIS_PRE + FRSConstant.EXPIRE_SET);
        } catch (Throwable t){
            log.error("移除ip失败, channel_id = {}, error = {}", channel_id, t.getMessage());
            return false;
        }
    }
}
