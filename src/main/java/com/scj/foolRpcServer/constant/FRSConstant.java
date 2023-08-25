package com.scj.foolRpcServer.constant;

import com.scj.foolRpcServer.cache.FoolCache;
import com.scj.foolRpcServer.cache.LocalCache;

/**
 * @author suchangjie.NANKE
 * @Title: constant
 * @date 2023/8/25 19:55
 * @description 常量
 */
public interface FRSConstant {

    /**
     * _
     */
    String UNDER_LINE = "_";

    /**
     * 缓存
     */
    FoolCache foolCache = new LocalCache();
}
