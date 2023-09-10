package com.scj.foolRpcServer;

import com.scj.foolRpcBase.entity.FoolRegisterReq;
import com.scj.foolRpcServer.cache.luaRedis.RedisLua;
import com.scj.foolRpcServer.cache.redis.RedisOpe;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class FRSApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(FRSApplication.class, args);
        RedisOpe bean = run.getBean(RedisOpe.class);
        FoolRegisterReq req = new FoolRegisterReq();
        req.setAppName("appName");
        req.setTimeStamp(1234L);
        req.setVersion("0.0.0.1");
        req.setFullClassName("fullclassname");
        bean.cacheValue("name",req);
        System.out.println(bean.getValue("name"));
        bean.cacheList("names", "scj");
        bean.cacheValue("aaa", "bbb");

//        System.out.println(bean.save("app", "version", "ip_port", "className", "channel"));
//        bean.test();

        RedisLua lua = run.getBean(RedisLua.class);
        lua.test();
    }

}
