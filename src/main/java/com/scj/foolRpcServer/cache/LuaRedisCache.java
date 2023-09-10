package com.scj.foolRpcServer.cache;

import com.scj.foolRpcServer.cache.luaRedis.RemoteLuaCache;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("luaRedisCache")
public class LuaRedisCache implements FoolCache{

    @Autowired
    private RemoteLuaCache cache;

    @Override
    public boolean register(String appName, String fullClassName, String ip_port, String version, Channel channel) {
        cache.registerMustSuccess(fullClassName, ip_port, channel);
        return true;
    }

    @Override
    public String getService(String fullClassName, String version) {
        List<String> ips = cache.getIps(fullClassName);
        if (ips == null || ips.isEmpty()) {
            return "";
        }
        return ips.get(0);
    }

    @Override
    public void remove(Channel channel) {
        cache.remove(channel.id().toString());
    }
}
