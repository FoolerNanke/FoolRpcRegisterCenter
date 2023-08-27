package com.scj.foolRpcServer.cache;


import com.scj.foolRpcServer.cache.local.LocalCaffeineCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import io.netty.channel.Channel;

/**
 * @author suchangjie.NANKE
 * @Title: LocalCache
 * @date 2023/8/25 19:57
 * @description 本地缓存
 */

@Slf4j
@Component
public class LocalCache implements FoolCache{

    /**
     * 缓存
     */
    private final LocalCaffeineCache cache;

    public LocalCache(){
        cache = new LocalCaffeineCache();
    }

    @Override
    public boolean register(String appName, String fullClassName
            , String ip_port, String version, Channel channel) {
        cache.save(appName, version, ip_port, fullClassName, channel);
        log.info("成功注册 app:{}, class:{}, version:{}"
                , appName, fullClassName, version);
        return true;
    }

    @Override
    public String getService(String fullClassName, String version) {
        String ip = cache.getIp(fullClassName, version);
        if (ip == null || ip.equals("")){
            return "";
        } else {
            log.info("给出IP{}", ip);
            return ip;
        }
    }
}
