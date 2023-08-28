package com.scj.foolRpcServer.constant;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.scj.foolRpcBase.entity.FoolProtocol;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;

import java.time.Duration;

/**
 * @author suchangjie.NANKE
 * @Title: PromiseCache
 * @date 2023/8/27 16:45
 * @description 请求缓存对象
 */
public class PromiseCache {

    /**
     * 请求回复 promise 存储对象
     * key:reqId
     * value:Promise<Object>
     */
    private static final Cache<Long, Promise<Object>> PromiseMap =
            Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(1)).build();

    /**
     * 存储请求:Promise对象
     * @param foolProtocol 存储协议
     */
    public static Promise<Object> handNewReq(FoolProtocol<?> foolProtocol){
        Promise<Object> promise = new DefaultPromise<>(FRSConstant.EXECUTORS.next());
        PromiseMap.put(foolProtocol.getReqId(), promise);
        return promise;
    }

    /**
     * 根据响应获取对应的PromiseMap对象
     * @param foolProtocol 响应
     * @return Promise<FoolResponse>
     */
    public static Promise<Object> getPromise(FoolProtocol<?> foolProtocol){
        return PromiseMap.getIfPresent(foolProtocol.getReqId());
    }
}
