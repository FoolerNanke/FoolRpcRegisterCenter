package com.scj.foolRpcServer.cache;

import com.scj.foolRpcServer.constant.FRSConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

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
     * 存储类信息
     * key:fullClassName_version
     * value:app
     */
    private final Map<String, String> classCache;

    /**
     * 存储应用下的类信息
     * key:app_version
     * value:Map<>(fullClassName)
     */
    private final Map<String, Map<String, String>> appCache;

    /**
     * 存储应用下的IP信息
     * key:app_version
     * value:Map<>(ip)
     */
    private final Map<String, CopyOnWriteArrayList<String>> ipCache;

    public LocalCache(){
        classCache = new ConcurrentHashMap<>();
        appCache = new ConcurrentHashMap<>();
        ipCache = new ConcurrentHashMap<>();
    }

    @Override
    public boolean register(String appName, String fullClassName, String ip, String version, long gap) {
        String app_version = appName + FRSConstant.UNDER_LINE + version;
        String class_version = fullClassName + FRSConstant.UNDER_LINE + version;
        // 存储 class_version : app
        classCache.put(class_version, app_version);
        // 存储 app_version : { fullClassName : "" }
        Map<String, String> map = appCache.get(app_version);
        if (map == null){
            synchronized (appCache){
                map = appCache.get(app_version);
                if (map == null){
                    map = new ConcurrentHashMap<>();
                    appCache.put(app_version, map);
                }
            }
        }
        map.put(class_version, "");
        // 存储 app_version : { ip : "" }
        CopyOnWriteArrayList<String> ips = ipCache.get(app_version);
        if (ips == null){
            synchronized (ipCache){
                ips = ipCache.get(app_version);
                if (ips == null){
                    ips = new CopyOnWriteArrayList<>();
                    ipCache.put(app_version, ips);
                }
            }
        }
        ips.add(ip);
        log.info("成功注册 app{}, class{}, version{}", appName, fullClassName, version);
        return true;
    }

    @Override
    public String getService(String fullClassName, String version, long gap) {
        String class_version = fullClassName + FRSConstant.UNDER_LINE + version;
        String appName = classCache.get(class_version);
        // 不存在该应用
        if (appName == null || appName.equals("")) {
            return "";
        }
        CopyOnWriteArrayList<String> ips = ipCache.get(appName);
        assert ips != null;
        if (ips.size() == 0){
            return "";
        } else {
            log.info("给出IP{}", ips.get(0));
            return ips.get(0);
        }
    }

}
