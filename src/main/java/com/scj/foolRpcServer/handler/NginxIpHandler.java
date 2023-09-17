package com.scj.foolRpcServer.handler;

import com.scj.foolRpcServer.constant.FRSConstant;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import io.netty.util.AttributeKey;
import org.checkerframework.checker.units.qual.C;
import org.springframework.stereotype.Component;

/**
 * @author: suchangjie.NANKE
 * @Title: HAProxyMessageDecoder
 * @date: 2023/9/17
 * @description: 获取代理 nginx 代理前的客户端真实 ip
 */
@Component
public class NginxIpHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HAProxyMessage) {
            HAProxyMessage haProxyMessage = (HAProxyMessage) msg;
            String clientIp = haProxyMessage.sourceAddress();
            Integer clientPort = haProxyMessage.sourcePort();
            // 在这里，您可以将客户端的真实 IP 和端口传递给后续处理器
            // 例如，将它们设置为 ChannelHandlerContext 的属性
            ctx.channel().attr(AttributeKey.valueOf(FRSConstant.CLIENT_REAL_IP)).set(clientIp);
            ctx.channel().attr(AttributeKey.valueOf(FRSConstant.CLIENT_REAL_PORT)).set(clientPort);
            // 继续处理其他消息
            ctx.fireChannelRead(msg);
        } else {
            super.channelRead(ctx, msg);
        }
    }
}
