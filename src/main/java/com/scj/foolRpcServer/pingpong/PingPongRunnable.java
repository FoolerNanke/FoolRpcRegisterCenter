package com.scj.foolRpcServer.pingpong;

import com.scj.foolRpcBase.constant.Constant;
import com.scj.foolRpcBase.entity.FoolProtocol;
import com.scj.foolRpcBase.entity.FoolRegisterReq;
import com.scj.foolRpcServer.constant.FRSConstant;
import com.scj.foolRpcServer.constant.PromiseCache;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author suchangjie.NANKE
 * @Title: PingPongScheduler
 * @date 2023/8/27 16:08
 * @description 心跳检测定时器
 */

@Slf4j
public class PingPongRunnable implements Runnable {

    private final NioEventLoopGroup eventExecutors;

    private final Channel channel;

    private final String ip_port;

    public PingPongRunnable(NioEventLoopGroup eventExecutors, Channel channel, String ip_port) {
        this.eventExecutors = eventExecutors;
        this.channel = channel;
        this.ip_port = ip_port;
    }

    @Override
    public void run() {
        /*
        检验当前时刻是否需要发送心跳检测
        eg:上一秒可能刚刚与下游进行了一次通信
           则认为此时不需要进行心跳检测了
         */
        long time = System.currentTimeMillis();
        Long val = FRSConstant.ipMap.get(ip_port);
        if (val > time){
            eventExecutors.schedule(this
                    , val - time
                    , FRSConstant.PING_PONG_TIME_UNIT);
            log.info("心跳成功 ip_port:{}", channel.remoteAddress().toString());
            return;
        }
        /*
        设置请求体
        发送心跳请求
         */
        FoolProtocol<FoolRegisterReq> foolProtocol = new FoolProtocol<>();
        foolProtocol.setData(new FoolRegisterReq());
        //设置请求类型为 PING 类型
        foolProtocol.setRemoteType(Constant.REGISTER_PING_REQ);
        Promise<Object> promise = PromiseCache.handNewReq(foolProtocol);
        channel.writeAndFlush(foolProtocol);
        /*
         获取请求结果
         */
        try {
            promise.get(Constant.TIME_OUT, TimeUnit.MILLISECONDS);
            // 成功获取到下游响应 说明没毛病
            // 将本任务再次加入执行线程池
            // 延长时间
            FRSConstant.ipMap.put(ip_port, System.currentTimeMillis() + FRSConstant.PING_PONG_TIME_GAP);
            eventExecutors.schedule(this
                    , FRSConstant.PING_PONG_TIME_GAP
                    , FRSConstant.PING_PONG_TIME_UNIT);
            log.info("心跳成功 ip_port:{}", channel.remoteAddress().toString());
        } catch (InterruptedException
                 | ExecutionException
                 | TimeoutException e) {
            log.info("下游响应异常 error:{} ip_port:{} 移除该ip_port"
                    , e.getMessage()
                    , channel.remoteAddress().toString());
            FRSConstant.foolCache.remove(channel);
        }
    }
}
