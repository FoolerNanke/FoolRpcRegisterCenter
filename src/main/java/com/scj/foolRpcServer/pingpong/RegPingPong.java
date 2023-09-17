package com.scj.foolRpcServer.pingpong;

import com.scj.foolRpcBase.constant.Constant;
import com.scj.foolRpcBase.runnable.PingPongHandler;
import com.scj.foolRpcServer.cache.FoolCache;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * @author suchangjie.NANKE
 * @Title: PingPongScheduler
 * @date 2023/8/27 16:08
 * @description 心跳检测定时器
 */
@Component
public class RegPingPong {

    private static final Random rand = new Random();

    @Autowired
    private FoolCache localCache;

    /**
     * 添加心跳请求托管
     * @param channel 通道
     */
    public void addPingPong(Channel channel, boolean random){
        long gap = Constant.PING_PONG_TIME_GAP;
        if (random) {
            // 随机固定间隔
            gap += rand.nextInt((int) Constant.PING_PONG_TIME_GAP);
        }
        new InnerPingPong(channel, gap);
    }

    /**
     * 内部子类
     */
    private class InnerPingPong extends PingPongHandler{

        public InnerPingPong(Channel channel, long gap) {
            super(channel, gap);
        }

        @Override
        public void handError(Throwable t) {
            super.handError(t);
            // 下游机器不存在了
            // 删除下游机器
            localCache.remove(channel);
        }
    }
}


