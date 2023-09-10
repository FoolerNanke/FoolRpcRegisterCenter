package com.scj.foolRpcServer.cache.luaRedis;

import com.scj.foolRpcServer.cache.luaRedis.RedisLua;
import com.scj.foolRpcServer.constant.FRSConstant;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RemoteLuaCache {

    @Autowired
    private RedisLua redisLua;

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
                    , channel.id().toString(), ip_port
                    , FRSConstant.EXPIRE_TIME.toString());
            return res1 && res2 && res3 && res4;
        } catch (Throwable t){
            log.error("lua 插入失败", t);
            return false;
        }
    }
}
