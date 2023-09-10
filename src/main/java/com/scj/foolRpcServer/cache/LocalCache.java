package com.scj.foolRpcServer.cache;


import com.scj.foolRpcServer.cache.local.LocalCaffeineCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import io.netty.channel.Channel;

/**
 * @author suchangjie.NANKE
 * @Title: LocalCache
 * @date 2023/8/25 19:57
 * @description 本地缓存
 */

@Slf4j
@Component("localCache")
public class LocalCache implements FoolCache{

    /**
     * 缓存
     */
    @Autowired
    private LocalCaffeineCache cache;

    /**
     * @param appName 应用名称
     * @param fullClassName 注册类信息
     * @param ip_port ip_port
     * @param version 版本
     * @param channel webIO链接通道
     * @return 该 channel and ip 是否首次添加
     */
    @Override
    public boolean register(String appName, String fullClassName
            , String ip_port, String version, Channel channel) {
        boolean save = cache.save(ip_port, fullClassName, channel);
        log.info("成功注册 app:{}, class:{}, version:{}"
                , appName, fullClassName, version);
        return save;
    }

    @Override
    public String getService(String fullClassName, String version) {
        String ip = cache.getIp(fullClassName);
        if (ip == null || ip.isEmpty()){
            return "";
        } else {
            log.info("给出IP{}", ip);
            return ip;
        }
    }

    @Override
    public void remove(Channel channel) {
        cache.remove(channel);
    }
}
