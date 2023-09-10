package com.scj.foolRpcServer.constant;

import com.scj.foolRpcServer.cache.FoolCache;
import com.scj.foolRpcServer.cache.LocalCache;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.concurrent.TimeUnit;

/**
 * @author suchangjie.NANKE
 * @Title: constant
 * @date 2023/8/25 19:55
 * @description 常量
 */
public class FRSConstant {

    /**
     * _
     */
    public static String UNDER_LINE = "_";

    /**
     * 缓存
     */
    public static FoolCache foolCache = new LocalCache();

    /**
     * 心跳时间间隔
     */
    public static long PING_PONG_TIME_GAP = 20;

    /**
     * 心跳时间间隔
     */
    public static TimeUnit PING_PONG_TIME_UNIT = TimeUnit.SECONDS;

    /**
     * PING PONG处理线程池
     * 处理线程数量为8个
     */
    public static NioEventLoopGroup EXECUTORS = new NioEventLoopGroup(8);

    /**
     * 普通任务处理线程池
     * 处理线程数量为4个
     */
    public static EventLoopGroup COMMON_EXECUTORS = new DefaultEventLoopGroup(4);

    /**
     * redis 存储前缀
     */
    public static String REDIS_PRE = "NANKE_FRS_";

    /**
     * redis 存储中缀
     * 存储app_class信息
     */
    public static String APP = "app_";

    /**
     * redis 存储中缀
     * 存储class_app信息
     */
    public static String CLASS = "class_";

    /**
     * redis 存储中缀
     * 存储app_ip+port信息
     */
    public static String IP_LIST = "ip_list_";

    /**
     * redis 存储中缀
     * 存储channel_ip+port信息
     */
    public static String CHANNEL = "channel_";

    /**
     * redis 存储 app_class 的键值
     */
    public static String APP_CLASS = "app_class";

    /**
     * 默认过期时间
     * 3天
     */
    public static Long EXPIRE_TIME = (long) (3 * 24 * 60 * 60);



}

