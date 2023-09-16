package com.scj.foolRpcServer.pingpong;

import com.scj.foolRpcBase.runnable.PingPongHandler;
import com.scj.foolRpcServer.constant.FRSConstant;
import io.netty.channel.Channel;

/**
 * @author suchangjie.NANKE
 * @Title: PingPongScheduler
 * @date 2023/8/27 16:08
 * @description 心跳检测定时器
 */
public class RegPingPong extends PingPongHandler {

    /**
     * 添加心跳请求托管
     * @param channel 通道
     */
    public static void addPingPong(Channel channel){
        new RegPingPong(channel);
    }

    public RegPingPong(Channel channel) {
        super(channel);
    }

    @Override
    public void handError(Throwable t) {
        super.handError(t);
        // 下游机器不存在了
        // 删除下游机器
        FRSConstant.foolCache.remove(channel);
    }
}
