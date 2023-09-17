package com.scj.foolRpcServer.handler;

import com.scj.foolRpcServer.constant.FRSConstant;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author: suchangjie.NANKE
 * @Title: HAProxyMessageDecoder
 * @date: 2023/9/17
 * @description: 获取代理 nginx 代理前的客户端真实 ip
 */
@Component
@Slf4j
public class NginxIpHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        if (msg instanceof HAProxyMessage) {
            HAProxyMessage haProxyMessage = (HAProxyMessage) msg;
            // 客户端真实ip
            String clientIp = haProxyMessage.sourceAddress();
            // 客户端真实端口
            Integer clientPort = haProxyMessage.sourcePort();
            // 将真实ip写入到 channel 的属性当中
            ctx.channel().attr(AttributeKey.valueOf(FRSConstant.CLIENT_REAL_IP)).set(clientIp);
            // 将真实port写入到 channel 的属性中
            ctx.channel().attr(AttributeKey.valueOf(FRSConstant.CLIENT_REAL_PORT)).set(clientPort);
            log.info("nginx转发 客户端真实ip:{} 真实端口:{}", clientIp, clientPort);
        }
        ctx.fireChannelRead(msg);
    }
}
